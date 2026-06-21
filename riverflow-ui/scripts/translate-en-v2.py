#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用词典重新翻译英文语言包。
对包含中文的 value，用贪心算法替换中文部分为英文。
"""
import re
import json
from pathlib import Path

I18N_DIR = Path('C:/Users/wanghanpeng/myprojects/RiverFlow/riverflow-ui/src/i18n')
DICT_FILE = Path('C:/Users/wanghanpeng/myprojects/RiverFlow/riverflow-ui/scripts/en-dict.json')

CHINESE_RE = re.compile(r'[\u4e00-\u9fa5]+')


def load_dict():
    data = json.loads(DICT_FILE.read_text(encoding='utf-8'))
    # 按中文长度降序，保证长词优先匹配
    return dict(sorted(data.items(), key=lambda x: -len(x[0])))


def translate_text(text: str, dictionary: dict) -> str:
    """对文本中的中文片段用贪心词典翻译"""
    if not CHINESE_RE.search(text):
        return text
    # 先完整匹配
    if text in dictionary:
        return dictionary[text]

    result = []
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if not CHINESE_RE.match(ch):
            result.append(ch)
            i += 1
            continue
        # 找到连续中文
        j = i
        while j < n and CHINESE_RE.match(text[j]):
            j += 1
        cn_seg = text[i:j]
        # 完整匹配
        if cn_seg in dictionary:
            result.append(dictionary[cn_seg])
            i = j
            continue
        # 贪心分词
        translated = False
        for k in range(len(cn_seg), 0, -1):
            prefix = cn_seg[:k]
            if prefix in dictionary:
                result.append(dictionary[prefix])
                # 剩余部分递归翻译
                remaining = cn_seg[k:]
                if remaining:
                    result.append(translate_text(remaining, dictionary))
                i = j
                translated = True
                break
        if not translated:
            # 无词典匹配，保留中文
            result.append(cn_seg)
            i = j
    return ''.join(result)


def main():
    dictionary = load_dict()
    for zh_file in sorted((I18N_DIR / 'modules').glob('*.js')):
        en_file = I18N_DIR / 'modules' / 'en' / zh_file.name
        if not en_file.exists():
            continue
        zh_text = zh_file.read_text(encoding='utf-8')
        en_text = en_file.read_text(encoding='utf-8')

        # 解析中文包，建立 key -> value
        zh_values = {}
        for m in re.finditer(r"^\s+([a-zA-Z0-9_\u4e00-\u9fa5]+):\s*'([^']*)',", zh_text, re.MULTILINE):
            zh_values[m.group(1)] = m.group(2)

        new_lines = []
        for line in en_text.splitlines():
            m = re.match(r"^(\s+)([a-zA-Z0-9_\u4e00-\u9fa5]+):\s*'([^']*)',", line)
            if not m:
                new_lines.append(line)
                continue
            indent, key, old_en = m.group(1), m.group(2), m.group(3)
            # 优先用中文 value 翻译
            cn = zh_values.get(key, old_en)
            # 如果旧英文已经是纯英文且不是占位，保留
            if not CHINESE_RE.search(old_en) and '(EN)' not in old_en:
                new_en = old_en
            else:
                new_en = translate_text(cn, dictionary)
                # 如果翻译后仍有中文，保留原中文+EN占位标记
                if CHINESE_RE.search(new_en):
                    new_en = cn + ' (EN)'
            escaped = new_en.replace('\\', '\\\\').replace("'", "\\'")
            new_lines.append(f"{indent}{key}: '{escaped}',")

        en_file.write_text('\n'.join(new_lines) + '\n', encoding='utf-8')
        print(f'Translated {en_file.name}')

    print('Done')


if __name__ == '__main__':
    main()
