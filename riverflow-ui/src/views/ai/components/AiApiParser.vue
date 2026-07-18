<template>
  <div class="ai-api-parser">
    <div class="parser-header">
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-info">
        <div class="header-title">{{ $t('aiApiParser.接口文档智能解析_8') }}</div>
        <div class="header-desc">{{ $t('aiApiParser.粘贴_open_a_p_9') }}</div>
      </div>
    </div>

    <div class="parser-body">
      <el-form label-position="top">
        <el-form-item :label="$t('aiApiParser.接口文档内容_5')">
          <div class="doc-upload-bar">
            <el-upload
              ref="uploadRef"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept=".json,.yaml,.yml,.md,.txt"
            >
              <el-button type="primary" plain size="small">
                <el-icon><Upload /></el-icon>上传接口文档
              </el-button>
            </el-upload>
            <span class="upload-tip">支持 .json / .yaml / .yml / .md / .txt，最大 5MB</span>
          </div>
          <el-input
            v-model="docContent"
            type="textarea"
            :rows="8"
            :placeholder="$t('aiApiParser.粘贴_open_a_p_6')"
          />
        </el-form-item>
        <el-form-item :label="$t('aiApiParser.解析选项_7')">
          <el-checkbox-group v-model="parseOptions">
            <el-checkbox label="extractParams">{{ $t('aiApiParser.提取参数_10') }}</el-checkbox>
            <el-checkbox label="extractResponses">{{ $t('aiApiParser.提取响应_11') }}</el-checkbox>
            <el-checkbox label="generateMapping">{{ $t('aiApiParser.生成映射推荐_12') }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <div class="parser-actions">
        <el-button type="primary" :loading="loading" @click="handleParse">
          <el-icon><MagicStick /></el-icon>{{ $t('aiApiParser.解析文档_13') }}</el-button>
      </div>

      <template v-if="result">
        <el-divider content-position="left">{{ $t('aiApiParser.解析结果_14') }}</el-divider>
        <div class="parse-result">
          <pre>{{ formattedResult }}</pre>
        </div>
        <div class="result-actions">
          <el-button type="primary" plain @click="copyResult">
            <el-icon><DocumentCopy /></el-icon>{{ $t('aiApiParser.复制_j_s_o_n_15') }}</el-button>
          <el-button type="primary" @click="goToApiMgr">
            <el-icon><Link /></el-icon>{{ $t('aiApiParser.去接口注册_16') }}</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Document, DocumentCopy, Link, Upload } from '@element-plus/icons-vue'
import { aiParseApiDoc } from '@/api/ai'

const router = useRouter()
const props = defineProps({
  provider: {
    type: String,
    default: ''
  },
  model: {
    type: String,
    default: ''
  },
  promptVersion: {
    type: String,
    default: 'v1'
  }
})
const docContent = ref('')
const parseOptions = ref(['extractParams', 'extractResponses'])
const loading = ref(false)
const result = ref(null)
const uploadRef = ref(null)

function handleFileChange(file) {
  const raw = file.raw
  if (!raw) return
  const maxSize = 5 * 1024 * 1024
  if (raw.size > maxSize) {
    ElMessage.warning('文件大小不能超过 5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    docContent.value = e.target.result
    ElMessage.success(`已读取文件：${raw.name}`)
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  reader.readAsText(raw)
}

const formattedResult = computed(() => {
  if (!result.value) return ''
  return JSON.stringify(result.value, null, 2)
})

async function handleParse() {
  if (!docContent.value.trim()) {
    ElMessage.warning(t('aiApiParser.请输入接口文档内_1'))
    return
  }
  loading.value = true
  try {
    const res = await aiParseApiDoc({
      docContent: docContent.value.trim(),
      options: parseOptions.value,
      provider: props.provider || undefined,
      model: props.model || undefined,
      promptVersion: props.promptVersion || undefined
    })
    result.value = res
  } catch (err) {
    ElMessage.error(err.message || t('aiApiParser.接口文档解析失败_2'))
  } finally {
    loading.value = false
  }
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(formattedResult.value).then(() => {
    ElMessage.success(t('aiApiParser.已复制_j_s_o_n_3'))
  }).catch(() => {
    ElMessage.error(t('aiApiParser.复制失败_4'))
  })
}

function goToApiMgr() {
  router.push('/api-mgr')
}
</script>

<style scoped>
.ai-api-parser {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
}
.parser-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.parser-body {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}
.parser-actions {
  text-align: right;
  margin-bottom: 12px;
}
.parse-result {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  max-height: 500px;
  overflow-y: auto;
}
.parse-result pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  color: #303133;
}
.result-actions {
  display: flex;
  gap: 10px;
}
.doc-upload-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
}
</style>
