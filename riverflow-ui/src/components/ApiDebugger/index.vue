<template>
  <div class="api-debugger">
    <el-form label-width="80px" size="small">
      <el-form-item label="代理后地址">
        <div class="debug-hint">此地址为平台统一暴露的代理路径，请求将经由平台转发处理</div>
        <el-input v-model="debugUrl" placeholder="http://...">
          <template #prepend>
            <el-select v-model="debugMethod" style="width: 90px">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item v-if="props.apiType !== 'sql'" label="Headers">
        <div v-for="(h, idx) in headers" :key="idx" class="kv-row">
          <el-input v-model="h.key" placeholder="Key" size="small" />
          <el-input v-model="h.value" placeholder="Value" size="small" />
          <el-button link type="danger" size="small" @click="headers.splice(idx, 1)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-button link type="primary" size="small" @click="headers.push({ key: '', value: '' })">
          <el-icon><Plus /></el-icon> 添加 Header
        </el-button>
      </el-form-item>

      <el-form-item label="请求参数">
        <el-radio-group v-model="paramType" size="small">
          <el-radio-button label="json">JSON</el-radio-button>
          <el-radio-button label="form">Form</el-radio-button>
          <el-radio-button label="text">Text</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="paramType === 'json'">
        <MonacoEditor v-model="bodyJson" language="json" height="160px" />
      </el-form-item>

      <el-form-item v-else-if="paramType === 'form'">
        <div v-for="(p, idx) in formParams" :key="idx" class="kv-row">
          <el-input v-model="p.key" placeholder="Key" size="small" />
          <el-input v-model="p.value" placeholder="Value" size="small" />
          <el-button link type="danger" size="small" @click="formParams.splice(idx, 1)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-button link type="primary" size="small" @click="formParams.push({ key: '', value: '' })">
          <el-icon><Plus /></el-icon> 添加参数
        </el-button>
      </el-form-item>

      <el-form-item v-else-if="paramType === 'text'">
        <el-input
          v-model="bodyText"
          type="textarea"
          :rows="6"
          placeholder="请输入纯文本内容"
          size="small"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="sending" @click="sendRequest">发送请求</el-button>
      </el-form-item>
    </el-form>

    <div v-if="response" class="response-panel">
      <div class="response-meta">
        <el-tag :type="response.ok ? 'success' : 'danger'">Status: {{ response.status }}</el-tag>
        <span class="time">{{ response.time }}ms</span>
      </div>
      <pre class="response-body">{{ responseBody }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MonacoEditor from '@/components/MonacoEditor/index.vue'

const props = defineProps({
  url: { type: String, default: '' },
  method: { type: String, default: 'GET' },
  apiType: { type: String, default: 'proxy' }
})

const debugUrl = ref(props.url)
const debugMethod = ref(props.method)
const paramType = ref('json')
const bodyJson = ref('{}')
const bodyText = ref('')
const headers = ref([{ key: 'Content-Type', value: 'application/json' }])
const formParams = ref([{ key: '', value: '' }])
const sending = ref(false)
const response = ref(null)

watch(() => props.url, (v) => { debugUrl.value = v })
watch(() => props.method, (v) => { debugMethod.value = v })

// 切换参数类型时自动更新默认 Content-Type
watch(paramType, (v) => {
  const contentTypeHeader = headers.value.find(h => h.key.toLowerCase() === 'content-type')
  if (v === 'json') {
    if (contentTypeHeader) contentTypeHeader.value = 'application/json'
    else headers.value.push({ key: 'Content-Type', value: 'application/json' })
  } else if (v === 'form') {
    if (contentTypeHeader) contentTypeHeader.value = 'application/x-www-form-urlencoded'
    else headers.value.push({ key: 'Content-Type', value: 'application/x-www-form-urlencoded' })
  } else if (v === 'text') {
    if (contentTypeHeader) contentTypeHeader.value = 'application/text'
    else headers.value.push({ key: 'Content-Type', value: 'application/text' })
  }
})

const responseBody = computed(() => {
  if (!response.value) return ''
  try {
    return JSON.stringify(JSON.parse(response.value.body), null, 2)
  } catch (e) {
    return response.value.body
  }
})

async function sendRequest() {
  if (!debugUrl.value) {
    ElMessage.warning('请输入请求地址')
    return
  }
  sending.value = true
  const start = Date.now()
  try {
    const hdr = {}
    headers.value.forEach(h => { if (h.key) hdr[h.key] = h.value })

    let body = undefined
    if (debugMethod.value !== 'GET' && debugMethod.value !== 'DELETE') {
      if (paramType.value === 'json') {
        body = bodyJson.value
      } else if (paramType.value === 'form') {
        const fd = new URLSearchParams()
        formParams.value.forEach(p => { if (p.key) fd.append(p.key, p.value) })
        body = fd.toString()
        hdr['Content-Type'] = 'application/x-www-form-urlencoded'
      } else if (paramType.value === 'text') {
        body = bodyText.value
      }
    }

    const res = await fetch(debugUrl.value, {
      method: debugMethod.value,
      headers: hdr,
      body
    })
    const text = await res.text()
    response.value = {
      ok: res.ok,
      status: res.status,
      body: text,
      time: Date.now() - start
    }
  } catch (e) {
    response.value = {
      ok: false,
      status: 0,
      body: e.message,
      time: Date.now() - start
    }
  } finally {
    sending.value = false
  }
}
</script>

<style scoped lang="scss">
.api-debugger {
  .kv-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
  }
  .debug-hint {
    margin-bottom: 8px;
    font-size: 12px;
    color: #8c8c8c;
    line-height: 1.5;
  }
  .response-panel {
    margin-top: 16px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    .response-meta {
      padding: 8px 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
      display: flex;
      align-items: center;
      gap: 12px;
      .time {
        color: #8c8c8c;
        font-size: 12px;
      }
    }
    .response-body {
      margin: 0;
      padding: 12px;
      max-height: 300px;
      overflow: auto;
      font-size: 12px;
      background: #fafafa;
    }
  }
}
</style>
