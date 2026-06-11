<template>
  <div class="json-viewer">
    <JsonNode :data="parsed" :depth="0" />
  </div>
</template>

<script setup>
import { computed, h, ref } from 'vue'

const props = defineProps({
  data: { type: [String, Object, Array, Number, Boolean], default: '' }
})

const parsed = computed(() => {
  if (typeof props.data === 'string') {
    try {
      return JSON.parse(props.data)
    } catch {
      return props.data
    }
  }
  return props.data
})

/* ========== 递归节点组件 ========== */
const JsonNode = {
  name: 'JsonNode',
  props: {
    data: null,
    depth: { type: Number, default: 0 },
    comma: { type: Boolean, default: false }
  },
  setup(props) {
    const collapsed = ref(false)
    const toggle = () => { collapsed.value = !collapsed.value }

    const isObject = computed(() =>
      props.data !== null && typeof props.data === 'object' && !Array.isArray(props.data)
    )
    const isArray = computed(() => Array.isArray(props.data))
    const isPrimitive = computed(() => !isObject.value && !isArray.value)

    const entries = computed(() => {
      if (isObject.value) return Object.entries(props.data)
      if (isArray.value) return props.data.map((v, i) => [i, v])
      return []
    })

    const typeClass = computed(() => {
      const d = props.data
      if (d === null) return 'null'
      const t = typeof d
      if (t === 'string') return 'string'
      if (t === 'number') return 'number'
      if (t === 'boolean') return 'boolean'
      return ''
    })

    const preview = computed(() => {
      if (isObject.value) {
        const keys = Object.keys(props.data)
        return keys.length ? `{${keys.slice(0, 3).join(', ')}${keys.length > 3 ? '...' : ''}}` : '{}'
      }
      if (isArray.value) {
        return `[${props.data.length}]`
      }
      return ''
    })

    const formatPrimitive = (val) => {
      if (val === null) return 'null'
      if (typeof val === 'string') return `"${val}"`
      return String(val)
    }

    return { collapsed, toggle, isObject, isArray, isPrimitive, entries, typeClass, preview, formatPrimitive }
  },
  render(ctx) {
    const { data, depth, comma, collapsed, toggle, isObject, isArray, isPrimitive, entries, typeClass, preview, formatPrimitive } = ctx
    const indent = { paddingLeft: `${depth * 16}px` }

    // 基本类型
    if (isPrimitive) {
      return h('span', { class: ['json-value', typeClass] }, [formatPrimitive(data), comma ? ',' : ''])
    }

    // 对象 / 数组
    const isArr = isArray
    const openBracket = isArr ? '[' : '{'
    const closeBracket = isArr ? ']' : '}'
    const count = isArr ? data.length : Object.keys(data).length

    const children = []

    // 折叠状态：显示 toggle + 预览
    if (collapsed) {
      children.push(
        h('span', { class: 'json-line', style: indent }, [
          h('span', { class: 'json-toggle', onClick: toggle }, [
            h('span', { class: 'toggle-icon' }, '+')
          ]),
          h('span', { class: 'json-bracket' }, openBracket),
          h('span', { class: 'json-preview' }, preview),
          h('span', { class: 'json-bracket' }, closeBracket),
          comma ? ',' : ''
        ])
      )
      return h('div', { class: 'json-node' }, children)
    }

    // 展开状态
    children.push(
      h('span', { class: 'json-line', style: indent }, [
        count > 0 ? h('span', { class: 'json-toggle', onClick: toggle }, [
          h('span', { class: 'toggle-icon' }, '-')
        ]) : h('span', { class: 'json-toggle-placeholder' }),
        h('span', { class: 'json-bracket' }, openBracket)
      ])
    )

    // 子节点
    entries.forEach(([key, val], idx) => {
      const isLast = idx === entries.length - 1
      const childIndent = { paddingLeft: `${(depth + 1) * 16}px` }

      children.push(
        h('div', { class: 'json-line', style: childIndent }, [
          !isArr ? h('span', { class: 'json-key' }, [`"${key}"`, ': ']) : null,
          h(JsonNode, {
            data: val,
            depth: depth + 1,
            comma: !isLast
          })
        ])
      )
    })

    // 闭合括号
    children.push(
      h('span', { class: 'json-line', style: indent }, [
        h('span', { class: 'json-toggle-placeholder' }),
        h('span', { class: 'json-bracket' }, closeBracket),
        comma ? ',' : ''
      ])
    )

    return h('div', { class: 'json-node' }, children)
  }
}
</script>

<style scoped lang="scss">
.json-viewer {
  font-family: 'Menlo', 'Monaco', 'Consolas', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #333;
  background: #fafafa;
  padding: 12px;
  max-height: 400px;
  overflow: auto;

  .json-line {
    display: flex;
    align-items: baseline;
    white-space: pre-wrap;
    word-break: break-all;
  }

  .json-toggle {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    margin-right: 4px;
    cursor: pointer;
    user-select: none;
    flex-shrink: 0;

    .toggle-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 12px;
      height: 12px;
      font-size: 10px;
      color: #666;
      border: 1px solid #ccc;
      border-radius: 2px;
      background: #fff;
      line-height: 1;
    }

    &:hover .toggle-icon {
      background: #e6f7ff;
      border-color: #1890ff;
      color: #1890ff;
    }
  }

  .json-toggle-placeholder {
    display: inline-block;
    width: 14px;
    margin-right: 4px;
    flex-shrink: 0;
  }

  .json-bracket {
    color: #333;
    font-weight: 600;
  }

  .json-key {
    color: #0969da;
    margin-right: 4px;
  }

  .json-value {
    &.string {
      color: #0a3069;
    }
    &.number {
      color: #0550ae;
    }
    &.boolean,
    &.null {
      color: #cf222e;
    }
  }

  .json-preview {
    color: #8c959f;
    margin: 0 2px;
  }
}
</style>
