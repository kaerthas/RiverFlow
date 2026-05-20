<template>
  <div class="minio-node-config">
    <el-form :model="config" label-width="120px" size="small">
      <el-divider content-position="left">连接配置</el-divider>
      
      <el-form-item label="MinIO地址">
        <el-input v-model="config.endpoint" placeholder="http://localhost:9000">
          <template #prepend>
            <el-icon><Link /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="Access Key">
        <el-input v-model="config.accessKey" placeholder="minioadmin" show-password />
      </el-form-item>

      <el-form-item label="Secret Key">
        <el-input v-model="config.secretKey" placeholder="minioadmin" show-password />
      </el-form-item>

      <el-form-item label="Bucket">
        <el-input v-model="config.bucket" placeholder="materials">
          <template #prepend>
            <el-icon><Folder /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-divider content-position="left">操作配置</el-divider>

      <el-form-item label="操作类型">
        <el-select v-model="config.operation" placeholder="请选择操作">
          <el-option label="上传文件" value="upload">
            <el-icon><Upload /></el-icon> 上传文件
          </el-option>
          <el-option label="下载文件" value="download">
            <el-icon><Download /></el-icon> 下载文件
          </el-option>
          <el-option label="删除文件" value="delete">
            <el-icon><Delete /></el-icon> 删除文件
          </el-option>
          <el-option label="查询文件" value="stat">
            <el-icon><View /></el-icon> 查询文件
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="对象名称">
        <el-input 
          v-model="config.objectName" 
          placeholder="${context.fileName}"
        >
          <template #prepend>
            <el-icon><Document /></el-icon>
          </template>
        </el-input>
        <div class="form-tip">支持使用 ${context.xxx} 引用上下文变量</div>
      </el-form-item>

      <template v-if="config.operation === 'upload'">
        <el-form-item label="文件路径">
          <el-input 
            v-model="config.filePath" 
            placeholder="${context.filePath}"
          >
            <template #prepend>
              <el-icon><Files /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="Content Type">
          <el-input 
            v-model="config.contentType" 
            placeholder="application/octet-stream"
          />
        </el-form-item>
      </template>

      <template v-if="config.operation === 'download'">
        <el-form-item label="下载路径">
          <el-input 
            v-model="config.downloadPath" 
            placeholder="/tmp/download/file.pdf"
          />
        </el-form-item>
      </template>

      <el-divider content-position="left">高级配置</el-divider>

      <el-form-item label="超时时间">
        <el-input-number 
          v-model="config.timeout" 
          :min="1000" 
          :max="300000"
          :step="1000"
        />
        <span class="unit">毫秒</span>
      </el-form-item>

      <el-form-item label="失败策略">
        <el-select v-model="config.failStrategy" placeholder="请选择">
          <el-option label="挂起流程" value="suspend" />
          <el-option label="跳过继续" value="skip" />
          <el-option label="重试" value="retry" />
        </el-select>
      </el-form-item>
    </el-form>

    <div class="config-preview">
      <el-collapse>
        <el-collapse-item title="配置预览（JSON）" name="preview">
          <pre>{{ JSON.stringify(config, null, 2) }}</pre>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { 
  Link, Folder, Upload, Download, Delete, View, 
  Document, Files 
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const config = ref({
  endpoint: 'http://localhost:9000',
  accessKey: 'minioadmin',
  secretKey: 'minioadmin',
  bucket: 'materials',
  operation: 'upload',
  objectName: '${context.fileName}',
  filePath: '${context.filePath}',
  contentType: 'application/octet-stream',
  timeout: 30000,
  failStrategy: 'suspend'
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    try {
      Object.assign(config.value, JSON.parse(newVal))
    } catch (e) {
      console.error('解析配置失败', e)
    }
  }
}, { immediate: true })

watch(config, (newConfig) => {
  emit('update:modelValue', JSON.stringify(newConfig.value))
}, { deep: true })
</script>

<style scoped lang="scss">
.minio-node-config {
  padding: 20px;

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .unit {
    margin-left: 8px;
    color: #606266;
  }

  .config-preview {
    margin-top: 20px;
    
    pre {
      background: #f5f7fa;
      padding: 10px;
      border-radius: 4px;
      font-size: 12px;
      overflow-x: auto;
    }
  }

  :deep(.el-divider__text) {
    font-weight: 600;
    color: #409eff;
  }
}
</style>
