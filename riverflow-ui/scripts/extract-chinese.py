#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
提取 riverflow-ui 中的中文文本，用于国际化。
输出按文件分组的中文条目到 JSON。
"""
import re
import json
from pathlib import Path

SRC_DIR = Path('C:/Users/wanghanpeng/myprojects/RiverFlow/riverflow-ui/src')
OUTPUT = Path('C:/Users/wanghanpeng/myprojects/RiverFlow/riverflow-ui/scripts/chinese-extracted.json')

CHINESE_RE = re.compile(r'[\u4e00-\u9fa5]')

# 匹配单行字符串（不跨行）
STRING_RE = re.compile(r"'([^'\\\n]*(?:\\.[^'\\\n]*)*)'|\"([^\"\\\n]*(?:\\.[^\"\\\n]*)*)\"")

# 匹配 Vue 模板属性值中的中文
ATTR_RE = re.compile(r'\s([a-zA-Z-:]+)="([^"]*[\u4e00-\u9fa5][^"]*)"')

# 匹配 Vue 模板纯文本节点中的中文（不包含属性、插值表达式等）
TEXT_RE = re.compile(r'>(\s*[\u4e00-\u9fa5][\u4e00-\u9fa5\s]*?)<')


def has_chinese(text):
    return bool(CHINESE_RE.search(text))


def looks_like_i18n_key(text: str) -> bool:
    """跳过已经被国际化的 key 字符串，如 'designer.未命名流程_xxx'"""
    return bool(re.match(r'^[a-zA-Z0-9_]+\.[\u4e00-\u9fa5a-zA-Z0-9_]+$', text))


def extract_js_strings(script):
    """提取 JS 中的字符串字面量（仅单行）"""
    results = []
    for m in STRING_RE.finditer(script):
        text = m.group(1) if m.group(1) is not None else m.group(2)
        if not has_chinese(text) or looks_like_i18n_key(text):
            continue
        # 跳过注释行中的字符串（简单判断）
        line_start = script.rfind('\n', 0, m.start()) + 1
        line = script[line_start:m.start()]
        if '//' in line:
            continue
        results.append({
            'type': 'js-string',
            'text': text,
            'line': script[:m.start()].count('\n') + 1,
            'raw': m.group(0)
        })
    return results


def extract_vue_template(template):
    """提取 Vue 模板中的中文"""
    results = []
    # 属性
    for m in ATTR_RE.finditer(template):
        attr_name, attr_value = m.group(1), m.group(2)
        if attr_name in ('class', 'style', 'class-name', 'ref', 'key'):
            continue
        if attr_name.startswith(':') or attr_name.startswith('v-bind:'):
            continue
        if looks_like_i18n_key(attr_value):
            continue
        results.append({
            'type': 'attr',
            'attr': attr_name,
            'text': attr_value,
            'line': template[:m.start()].count('\n') + 1,
            'raw': m.group(0)
        })
    # 文本节点
    for m in TEXT_RE.finditer(template):
        text = m.group(1).strip()
        if not text:
            continue
        if text.startswith('{{') and text.endswith('}}'):
            continue
        # 跳过包含属性特征的文本（避免把标签属性内容当作文本节点）
        if '=' in text or '"' in text or "'" in text:
            continue
        if looks_like_i18n_key(text):
            continue
        results.append({
            'type': 'text',
            'text': text,
            'line': template[:m.start()].count('\n') + 1,
            'raw': m.group(0)
        })
    return results


def extract_file(path: Path):
    rel = path.relative_to(SRC_DIR).as_posix()
    content = path.read_text(encoding='utf-8')
    results = []

    is_vue = path.suffix == '.vue'

    if is_vue:
        # 取根 <template>：从第一个 <template> 到最后一个 </template>
        template_start = content.find('<template')
        template_end = content.rfind('</template>')
        if template_start != -1 and template_end != -1 and template_end > template_start:
            template_part = content[template_start:template_end]
            # 去掉根 <template> 标签本身
            template_part = re.sub(r'^<template[^>]*>', '', template_part, count=1)
        else:
            template_part = ''
        script_match = re.search(r'<script[^>]*>(.*?)</script>', content, re.DOTALL)
        script_part = script_match.group(1) if script_match else ''
        results.extend(extract_js_strings(script_part))
        results.extend(extract_vue_template(template_part))
    else:
        results.extend(extract_js_strings(content))

    for r in results:
        r['file'] = rel

    return results


def main():
    all_results = []
    for path in sorted(SRC_DIR.rglob('*')):
        if path.suffix in ('.vue', '.js'):
            # 跳过 i18n 语言包自身
            rel = path.relative_to(SRC_DIR).as_posix()
            if rel.startswith('i18n/'):
                continue
            all_results.extend(extract_file(path))

    unique_texts = set(r['text'] for r in all_results)
    print(f'文件数: {len(set(r["file"] for r in all_results))}')
    print(f'中文出现次数: {len(all_results)}')
    print(f'唯一中文条数: {len(unique_texts)}')

    grouped = {}
    for r in all_results:
        grouped.setdefault(r['file'], []).append(r)

    OUTPUT.write_text(json.dumps(grouped, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f'已导出: {OUTPUT}')


if __name__ == '__main__':
    main()
