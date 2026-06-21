#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复 i18n 语言包：
1. 根据源文件中实际使用的 key 重建语言包
2. 从 git diff 恢复中文 value
3. 生成英文翻译
"""
import re
import json
import subprocess
import hashlib
from pathlib import Path

ROOT = Path('C:/Users/wanghanpeng/myprojects/RiverFlow')
SRC_DIR = ROOT / 'riverflow-ui' / 'src'
I18N_DIR = SRC_DIR / 'i18n'
MAPPING_FILE = ROOT / 'riverflow-ui' / 'scripts' / 'zh-mapping.json'

CHINESE_RE = re.compile(r'[\u4e00-\u9fa5]')
KEY_RE = re.compile(r"\$?t\(\s*['\"]([a-zA-Z0-9_]+\.[^'\"]+)['\"]\s*\)")
STRING_RE = re.compile(r"'([^'\\\n]*(?:\\.[^'\\\n]*)*)'|\"([^\"\\\n]*(?:\\.[^\"\\\n]*)*)\"")

COMMON_EN = {
    '确定': 'Confirm', '取消': 'Cancel', '保存': 'Save', '提交': 'Submit', '重置': 'Reset',
    '搜索': 'Search', '查询': 'Query', '新增': 'Add', '编辑': 'Edit', '删除': 'Delete',
    '移除': 'Remove', '操作': 'Operate', '详情': 'Detail', '状态': 'Status', '启用': 'Enable',
    '禁用': 'Disable', '已启用': 'Enabled', '已禁用': 'Disabled', '成功': 'Success', '失败': 'Fail',
    '提示': 'Tip', '警告': 'Warning', '错误': 'Error', '加载中...': 'Loading...', '暂无数据': 'No Data',
    '请选择': 'Please select', '请输入': 'Please input', '必填': 'Required', '是': 'Yes', '否': 'No',
    '返回': 'Back', '关闭': 'Close', '刷新': 'Refresh', '退出登录': 'Logout', '登录': 'Login',
    '用户名': 'Username', '密码': 'Password', '验证码': 'Captcha', '名称': 'Name', '编码': 'Code',
    '描述': 'Description', '类型': 'Type', '创建时间': 'Create Time', '更新时间': 'Update Time',
    '确认删除': 'Confirm Delete', '确认操作': 'Confirm Operation', '保存成功': 'Saved successfully',
    '删除成功': 'Deleted successfully', '操作成功': 'Operation successful', '操作失败': 'Operation failed',
    '网络错误': 'Network error', '服务器错误': 'Server error', '请求失败': 'Request failed',
    '未知错误': 'Unknown error', '校验失败': 'Validation failed', '完成': 'Complete',
    '运行': 'Run', '预览': 'Preview', '导入': 'Import', '导出': 'Export', '发布': 'Publish',
    '暂停': 'Pause', '恢复': 'Resume', '停止': 'Stop', '启动': 'Start', '调试': 'Debug',
    '应用': 'Application', '接口': 'API', '数据源': 'Datasource', '流程': 'Flow',
    '节点': 'Node', '变量': 'Variable', '条件': 'Condition', '脚本': 'Script',
    '定时': 'Timer', '插件': 'Plugin', '实例': 'Instance', '监控': 'Monitor',
    '星期一': 'Mon', '星期二': 'Tue', '星期三': 'Wed', '星期四': 'Thu',
    '星期五': 'Fri', '星期六': 'Sat', '星期日': 'Sun', '周一': 'Mon', '周二': 'Tue',
    '周三': 'Wed', '周四': 'Thu', '周五': 'Fri', '周六': 'Sat', '周日': 'Sun',
    '已完成': 'Completed', '运行中': 'Running', '失败': 'Failed', '草稿': 'Draft',
    '无': 'None', '全部': 'All', '序号': 'No.', '字段': 'Field', '表名': 'Table Name',
    '长度': 'Length', '小数位': 'Decimal', '主键': 'Primary Key', '必填': 'Required',
    '索引': 'Index', '默认值': 'Default Value', '数据类型': 'Data Type', '字段名称': 'Field Name',
    '字段编码': 'Field Code', '导入': 'Import', '解析': 'Parse', '粘贴': 'Paste',
    '复制': 'Copy', '粘贴语句': 'Paste SQL', '无法解析': 'Unable to parse',
    '注册接口': 'Register API', '所属应用': 'App', '请求方式': 'Method', '请求地址': 'Request URL',
    '代理后路径': 'Proxy Path', '认证方式': 'Auth Type', '流程触发': 'Flow Trigger',
    '应用编码': 'App Code', '应用名称': 'App Name', '应用图标': 'App Icon', '应用标识': 'App Key',
    '应用密钥': 'App Secret', '生成': 'Generate', '排序号': 'Sort No', '应用目录': 'App Directory',
    '搜索应用': 'Search App', '全部接口': 'All APIs', '暂无应用': 'No Apps', '接口编码': 'API Code',
    '接口名称': 'API Name', '代理接口': 'Proxy API', '状态': 'Status', '未启用': 'Disabled',
    '已发布': 'Published', '个人中心': 'Profile', '管理员': 'Admin', '提示': 'Tip',
    '数据大盘': 'Dashboard', '事项管理': 'Task Management', '数据源管理': 'Datasource Management',
    '动态表设计': 'Dynamic Table', '接口注册': 'API Registration', '工作流': 'Workflow',
    '流程定义': 'Flow Definition', '流程设计器': 'Flow Designer', '实例监控': 'Instance Monitor',
    '运行监控': 'Runtime Monitor', '插件管理': 'Plugin Management', '脚本管理': 'Script Management',
    '河狸流程编排平台': 'RiverFlow Platform', '未命名流程': 'Untitled Flow',
    '基础节点': 'Basic Nodes', '开始': 'Start', '流程起点': 'Flow Start', '结束': 'End',
    '流程终点': 'Flow End', '数据处理': 'Data Process', '接口调用': 'API Call',
    '调用外部': 'Call External API', '数据库': 'Database', '执行操作': 'Execute Operation',
    '脚本处理': 'Script Process', '控制流': 'Control Flow', '条件判断': 'Condition',
    '分支条件': 'Branch Condition', '定时等待': 'Timer Wait', '延迟或定时': 'Delay or Timer',
    '插件节点': 'Plugin Node', '节点已注册': 'Node Registered', '不允许的连线': 'Invalid Connection',
    '同步流程不支持': 'Sync flow not supported', '请先输入语句': 'Please enter statement first',
    '仅支持解析语': 'Only SQL parse supported', '未解析到字段': 'No fields parsed',
    '字段解析失败': 'Field parse failed', '该字段已在输': 'Field already in output',
    '加载接口目录': 'Load API catalog', '加载接口参数': 'Load API params',
    '接口目录中未': 'API not found in catalog', '流程入参': 'Flow Input',
    '系统变量': 'System Variables', '流程必须包含': 'Flow must contain',
    '流程验证通过': 'Validation passed', '已发布的流程': 'Published flow',
    '流程草稿已保': 'Flow draft saved', '保存失败': 'Save failed', '请先保存流程': 'Please save flow first',
    '流程发布成功': 'Flow published', '发布失败': 'Publish failed', '已创建新版本': 'New version created',
    '创建新版本失': 'Create version failed', '流程实例启动': 'Flow instance started',
    '无效的流程图': 'Invalid flow graph', '导入成功': 'Import success', '导入失败': 'Import failed',
    '加载流程数据': 'Load flow data', '请求失败': 'Request failed', '网络请求异常': 'Network request exception',
    '登录已过期请': 'Login expired', '安全验证': 'Security Verification',
    '刷新验证码': 'Refresh Captcha', '验证失败': 'Verification failed', '关闭': 'Close',
    '未知错误': 'Unknown error', '验证成功': 'Verification success',
    '新增数据': 'Add Data', '编辑数据': 'Edit Data', '无法获取主键': 'Unable to get primary key',
    '确认删除该条': 'Confirm delete this record?', '删除确认': 'Delete Confirm',
    '从导入字段': 'Import from SQL', '粘贴语句如': 'Paste CREATE TABLE statement',
    '未解析到有效': 'No valid fields parsed', 'SQL解析失败': 'SQL parse failed',
    '发送请求': 'Send Request', '代理后地址': 'Proxy Address', '请求参数': 'Request Params',
    '请输入请求地': 'Please enter request URL', '是': 'Yes', '否': 'No',
}


def collect_used_keys():
    """收集源文件中实际使用的所有 key"""
    modules = {}
    for path in sorted(SRC_DIR.rglob('*')):
        if path.suffix not in ('.vue', '.js'):
            continue
        if 'i18n' in path.parts:
            continue
        content = path.read_text(encoding='utf-8')
        for m in KEY_RE.finditer(content):
            full = m.group(1)
            mod, key = full.split('.', 1)
            modules.setdefault(mod, set()).add(key)
    return {k: sorted(v) for k, v in sorted(modules.items())}


def load_mapping():
    if MAPPING_FILE.exists():
        return json.loads(MAPPING_FILE.read_text(encoding='utf-8'))
    return {}


def cn_prefix_from_key(key: str) -> str:
    """从 key 中提取中文前缀（最多 6 个中文）"""
    # key 格式: 中文前缀_hash[_suffix]
    m = re.match(r'^([\u4e00-\u9fa5]+)_[a-f0-9]{8}', key)
    if m:
        return m.group(1)
    # 尝试匹配 _a_i 开头的英文+中文混合
    m = re.match(r'^(_a_i[a-zA-Z0-9\u4e00-\u9fa5]*?)_[a-f0-9]', key)
    if m:
        return m.group(1)
    return key


def translate_to_en(text: str) -> str:
    if text in COMMON_EN:
        return COMMON_EN[text]
    # 尝试最长匹配前缀
    for length in range(min(len(text), 12), 0, -1):
        prefix = text[:length]
        if prefix in COMMON_EN:
            return COMMON_EN[prefix] + text[length:]
    # 一些规则转换
    if text.endswith('成功'):
        return translate_to_en(text[:-2]) + ' successful' if text[:-2] else 'Success'
    if text.endswith('失败'):
        return translate_to_en(text[:-2]) + ' failed' if text[:-2] else 'Failed'
    if text.startswith('请输入'):
        return 'Please enter ' + translate_to_en(text[3:]).lower()
    if text.startswith('请选择'):
        return 'Please select ' + translate_to_en(text[3:]).lower()
    if text.startswith('确认'):
        return 'Confirm ' + translate_to_en(text[2:])
    if text.startswith('加载'):
        return 'Load ' + translate_to_en(text[2:])
    if text.endswith('吗？') or text.endswith('吗?'):
        return 'Are you sure to ' + translate_to_en(text[:-2]).lower() + '?'
    # 默认：返回中文 + (EN) 占位
    return text + ' (EN)'


def generate_locales(used_keys: dict, mapping: dict):
    (I18N_DIR / 'modules').mkdir(parents=True, exist_ok=True)
    (I18N_DIR / 'modules' / 'en').mkdir(parents=True, exist_ok=True)

    zh_modules = {}
    en_modules = {}
    fallback_count = 0

    for mod, keys in used_keys.items():
        zh = {}
        en = {}
        for key in keys:
            # 优先用 mapping 中的中文
            cn = mapping.get(mod, {}).get(key)
            if not cn:
                cn = cn_prefix_from_key(key)
                if cn != key:
                    fallback_count += 1
            zh[key] = cn
            en[key] = translate_to_en(cn)
        zh_modules[mod] = zh
        en_modules[mod] = en

    return zh_modules, en_modules, fallback_count


def write_module_file(path: Path, messages: dict):
    lines = ['export default {']
    for key, value in messages.items():
        escaped = value.replace('\\', '\\\\').replace("'", "\\'").replace('\n', '\\n')
        lines.append(f"  {key}: '{escaped}',")
    lines.append('}')
    path.write_text('\n'.join(lines) + '\n', encoding='utf-8')


def write_index_file(path: Path, modules: dict, lang: str):
    names = sorted(modules.keys())
    lines = []
    for name in names:
        suffix = f"./modules/{name}" if lang == 'zh' else f"./modules/en/{name}"
        lines.append(f"import {name} from '{suffix}'")
    lines.append('')
    lines.append('export default {')
    for name in names:
        lines.append(f'  {name},')
    lines.append('}')
    path.write_text('\n'.join(lines) + '\n', encoding='utf-8')


def main():
    print('Collecting used keys from source files...')
    used_keys = collect_used_keys()
    total_keys = sum(len(v) for v in used_keys.values())
    print(f'Found {len(used_keys)} modules, {total_keys} keys')

    print('Loading mapping from git diff...')
    mapping = load_mapping()
    mapping_total = sum(len(v) for v in mapping.values())
    print(f'Loaded {mapping_total} mappings')

    print('Generating locales...')
    zh_modules, en_modules, fallback_count = generate_locales(used_keys, mapping)
    print(f'Generated, {fallback_count} keys fallback to prefix')

    # 写入文件
    for mod, messages in zh_modules.items():
        write_module_file(I18N_DIR / 'modules' / f'{mod}.js', messages)
    for mod, messages in en_modules.items():
        write_module_file(I18N_DIR / 'modules' / 'en' / f'{mod}.js', messages)

    write_index_file(I18N_DIR / 'zh-CN.js', zh_modules, 'zh')
    write_index_file(I18N_DIR / 'en-US.js', en_modules, 'en')

    print('Done.')
    print(f'  modules: {len(zh_modules)}')
    print(f'  total keys: {total_keys}')
    print(f'  fallback to prefix: {fallback_count}')


if __name__ == '__main__':
    main()
