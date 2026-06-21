#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 git diff 恢复当前源文件中 i18n key 对应的真实中文。
输出 mapping.json: { module: { key: 中文 } }
"""
import re
import json
import subprocess
from pathlib import Path

ROOT = Path('C:/Users/wanghanpeng/myprojects/RiverFlow')
SRC_DIR = ROOT / 'riverflow-ui' / 'src'
OUT = ROOT / 'riverflow-ui' / 'scripts' / 'zh-mapping.json'

CHINESE_RE = re.compile(r'[\u4e00-\u9fa5]')
KEY_RE = re.compile(r"\$?t\(\s*['\"]([a-zA-Z0-9_]+\.[^'\"]+)['\"]\s*\)")
STRING_RE = re.compile(r"'([^'\\\n]*(?:\\.[^'\\\n]*)*)'|\"([^\"\\\n]*(?:\\.[^\"\\\n]*)*)\"")


def extract_chinese_from_line(line: str) -> list:
    """从一行中提取所有包含中文的字符串片段或文本节点"""
    results = []
    # 字符串字面量
    for m in STRING_RE.finditer(line):
        text = m.group(1) if m.group(1) is not None else m.group(2)
        if CHINESE_RE.search(text):
            results.append(text)
    # >中文< 模板文本
    for m in re.finditer(r'>(\s*[\u4e00-\u9fa5][\u4e00-\u9fa5\s]*?)<', line):
        text = m.group(1).strip()
        if text and text not in results:
            results.append(text)
    # 也提取 label="中文" 属性值中的中文
    for m in re.finditer(r'\s[a-zA-Z-:]+="([^"]*[\u4e00-\u9fa5][^"]*)"', line):
        text = m.group(1)
        if text not in results:
            results.append(text)
    return results


def extract_keys_from_line(line: str) -> list:
    return [m.group(1).split('.', 1)[1] for m in KEY_RE.finditer(line)]


def parse_diff(path: Path) -> dict:
    """解析单个文件的 git diff，返回 key -> 中文 映射"""
    rel = path.relative_to(ROOT).as_posix()
    result = subprocess.run(
        ['git', 'diff', '--', rel],
        cwd=ROOT,
        capture_output=True,
        text=True,
        encoding='utf-8'
    )
    diff = result.stdout
    mapping = {}
    lines = diff.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        # 跳过 diff 元数据行
        if line.startswith('@@') or line.startswith('diff') or line.startswith('index') or line.startswith('---') or line.startswith('+++'):
            i += 1
            continue
        if line.startswith('-') and not line.startswith('---'):
            # 收集连续删除行
            minus_lines = [line[1:]]
            j = i + 1
            while j < len(lines) and lines[j].startswith('-') and not lines[j].startswith('---'):
                minus_lines.append(lines[j][1:])
                j += 1
            # 收集连续添加行
            plus_lines = []
            while j < len(lines) and lines[j].startswith('+') and not lines[j].startswith('+++'):
                plus_lines.append(lines[j][1:])
                j += 1

            # 提取中文和 key
            all_cn = []
            for l in minus_lines:
                all_cn.extend(extract_chinese_from_line(l))
            all_keys = []
            for l in plus_lines:
                all_keys.extend(extract_keys_from_line(l))

            # 配对：按顺序一一对应
            if all_cn and all_keys:
                n = min(len(all_cn), len(all_keys))
                for idx in range(n):
                    mapping[all_keys[idx]] = all_cn[idx]
                # 如果中文多，剩余中文无法配对；如果 key 多，剩余 key 无法配对
            i = j
        else:
            i += 1
    return mapping


def main():
    # 获取修改过的前端文件（不包括新增 untracked）
    result = subprocess.run(
        ['git', 'status', '--short'],
        cwd=ROOT,
        capture_output=True,
        text=True,
        encoding='utf-8'
    )
    files = []
    for line in result.stdout.splitlines():
        if not line.startswith(' M') and not line.startswith('M '):
            continue
        p = line[3:].strip()
        if p.startswith('riverflow-ui/src/') and (p.endswith('.vue') or p.endswith('.js')):
            files.append(ROOT / p)

    all_mapping = {}
    for path in files:
        mapping = parse_diff(path)
        if not mapping:
            continue
        # 推断 module
        content = path.read_text(encoding='utf-8')
        mod_match = re.search(r"\$?t\(\s*['\"]([a-zA-Z0-9_]+)\.", content)
        module = mod_match.group(1) if mod_match else path.stem
        all_mapping.setdefault(module, {}).update(mapping)
        print(f'{path.relative_to(ROOT)}: {len(mapping)} mappings -> module {module}')

    OUT.write_text(json.dumps(all_mapping, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f'\n已保存映射: {OUT}')
    total = sum(len(v) for v in all_mapping.values())
    print(f'共恢复 {total} 条 key->中文 映射')
    for mod, m in sorted(all_mapping.items()):
        print(f'  {mod}: {len(m)}')


if __name__ == '__main__':
    main()
