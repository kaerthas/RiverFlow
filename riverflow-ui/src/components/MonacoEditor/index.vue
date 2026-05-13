<template>
  <div ref="editorContainer" class="monaco-editor-container" :style="{ height: props.height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

self.MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'json') return new jsonWorker()
    if (['css', 'scss', 'less'].includes(label)) return new cssWorker()
    if (['html', 'handlebars', 'razor'].includes(label)) return new htmlWorker()
    if (['typescript', 'javascript'].includes(label)) return new tsWorker()
    return new editorWorker()
  }
}

const props = defineProps({
  modelValue: { type: String, default: '' },
  language: { type: String, default: 'javascript' },
  theme: { type: String, default: 'vs' },
  height: { type: String, default: '200px' },
  readOnly: { type: Boolean, default: false },
  options: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'change'])

const editorContainer = ref(null)
let editor = null

function initEditor() {
  if (!editorContainer.value) return
  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: props.language,
    theme: props.theme,
    readOnly: props.readOnly,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    automaticLayout: true,
    fontSize: 13,
    tabSize: 2,
    folding: true,
    lineNumbers: 'on',
    renderLineHighlight: 'all',
    ...props.options
  })
  editor.onDidChangeModelContent(() => {
    const value = editor.getValue()
    emit('update:modelValue', value)
    emit('change', value)
  })
}

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getValue()) editor.setValue(val)
})

watch(() => props.language, (lang) => {
  if (editor) monaco.editor.setModelLanguage(editor.getModel(), lang)
})

onMounted(() => nextTick(() => initEditor()))
onUnmounted(() => { if (editor) { editor.dispose(); editor = null } })
</script>

<style scoped>
.monaco-editor-container {
  width: 100%;
  border: 1px solid #D9D9D9;
  border-radius: 4px;
  overflow: hidden;
}
</style>
