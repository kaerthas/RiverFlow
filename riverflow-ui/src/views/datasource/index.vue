<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><DataAnalysis /></el-icon>
      数据源管理
    </div>
    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增数据源</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="ds in datasourceList" :key="ds.id">
          <div class="ds-card" :class="{ active: ds.status === 1 }">
            <div class="ds-header">
              <el-icon :size="32" :color="ds.status === 1 ? '#1677FF' : '#8C8C8C'"><Coin /></el-icon>
              <div class="ds-status">
                <el-tag v-if="ds.status === 1" type="success" size="small">在线</el-tag>
                <el-tag v-else type="info" size="small">离线</el-tag>
              </div>
            </div>
            <h4 class="ds-name">{{ ds.dsName }}</h4>
            <p class="ds-code">{{ ds.dsCode }} | {{ ds.dbType }}</p>
            <p class="ds-url" :title="ds.url">{{ ds.url }}</p>
            <div class="ds-actions">
              <el-button link type="primary" size="small" @click="handleTest(ds)">测试连接</el-button>
              <el-button link type="warning" size="small" @click="handleReload(ds)">重载</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(ds)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(ds)">删除</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="数据源编码" prop="dsCode">
          <el-input v-model="form.dsCode" placeholder="如 master、biz_db" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="数据源名称" prop="dsName">
          <el-input v-model="form.dsName" placeholder="如 主库(MySQL)" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="请选择" style="width: 100%" @change="onDbTypeChange">
            <el-option label="MySQL" value="mysql" />
            <el-option label="Oracle" value="oracle" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="SQL Server" value="sqlserver" />
            <el-option label="达梦" value="dm" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="驱动类" prop="driverClass">
          <el-input v-model="form.driverClass" placeholder="驱动类全限定名" :disabled="isBuiltInType" />
        </el-form-item>
        <el-form-item label="连接URL" prop="url">
          <el-input v-model="form.url" type="textarea" :rows="2" :placeholder="urlPlaceholder" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="数据库用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :rules="passwordRule">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '数据库密码'"
            show-password
            autocomplete="new-password"
            readonly
            @focus="($event) => $event.target.removeAttribute('readonly')"
          />
        </el-form-item>
        <!-- 自定义驱动 JAR 上传 -->
        <el-form-item v-if="isCustomType" label="驱动 JAR" prop="driverJarPath">
          <el-upload
            ref="uploadRef"
            :http-request="customUpload"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept=".jar"
          >
            <el-button type="primary" :icon="Upload">上传驱动 JAR</el-button>
          </el-upload>
          <div v-if="form.driverJarPath" class="driver-jar-info">
            <el-icon><Document /></el-icon>
            <span class="jar-name">已上传: {{ jarFileName }}</span>
            <el-button link type="danger" size="small" @click="removeDriverJar">移除</el-button>
          </div>
          <div class="upload-tip">请上传对应数据库的 JDBC 驱动 JAR 包（最大 50MB）</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, Plus, Coin, Upload, Document } from '@element-plus/icons-vue'
import {
  getDatasourceList,
  createDatasource,
  updateDatasource,
  deleteDatasource,
  testConnection,
  reloadDatasource,
  uploadDriverJar
} from '@/api/datasource'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据源')
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)
const uploadRef = ref(null)

const datasourceList = ref([])

const form = reactive({
  id: null,
  dsCode: '',
  dsName: '',
  dbType: '',
  driverClass: '',
  url: '',
  username: '',
  password: '',
  driverJarPath: ''
})

const dbTypeDriverMap = {
  mysql: 'com.mysql.cj.jdbc.Driver',
  oracle: 'oracle.jdbc.driver.OracleDriver',
  postgresql: 'org.postgresql.Driver'
}

const dbTypeUrlPlaceholderMap = {
  mysql: 'jdbc:mysql://host:port/db',
  oracle: 'jdbc:oracle:thin:@host:port:SID',
  postgresql: 'jdbc:postgresql://host:port/db',
  other: 'jdbc:xxx://host:port/db'
}

const isBuiltInType = computed(() => {
  return form.dbType && form.dbType !== 'other'
})

const isCustomType = computed(() => {
  return form.dbType === 'other'
})

const urlPlaceholder = computed(() => {
  return dbTypeUrlPlaceholderMap[form.dbType] || 'jdbc:xxx://host:port/db'
})

const jarFileName = computed(() => {
  if (!form.driverJarPath) return ''
  const parts = form.driverJarPath.split(/[/\\]/)
  return parts[parts.length - 1] || form.driverJarPath
})

const formRules = computed(() => ({
  dsCode: [{ required: true, message: '请输入数据源编码', trigger: 'blur' }],
  dsName: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  url: [{ required: true, message: '请输入连接URL', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  driverClass: [{ required: true, message: '请输入驱动类名', trigger: 'blur' }],
  driverJarPath: [{ required: isCustomType.value, message: '请上传驱动 JAR 包', trigger: 'change' }]
}))

// 密码校验规则（新增必填，编辑可选）
const passwordRule = computed(() => {
  return isEdit.value
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

function onDbTypeChange(val) {
  // 内置类型自动填充驱动类
  if (dbTypeDriverMap[val]) {
    form.driverClass = dbTypeDriverMap[val]
  } else if (val === 'other') {
    form.driverClass = ''
  }
  // 清空已上传的驱动 JAR
  form.driverJarPath = ''
}

function beforeUpload(file) {
  if (!form.dsCode) {
    ElMessage.error('请先填写数据源编码')
    return false
  }
  if (!form.driverClass) {
    ElMessage.error('请先填写驱动类名')
    return false
  }
  const isJar = file.name.toLowerCase().endsWith('.jar')
  if (!isJar) {
    ElMessage.error('只能上传 JAR 文件')
    return false
  }
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

async function customUpload(options) {
  try {
    const formData = new FormData()
    formData.append('dsCode', form.dsCode)
    formData.append('driverClass', form.driverClass)
    formData.append('file', options.file)
    const jarPath = await uploadDriverJar(formData)
    form.driverJarPath = jarPath
    ElMessage.success('驱动 JAR 上传成功')
  } catch (e) {
    options.onError(e)
    throw e
  }
}

function removeDriverJar() {
  form.driverJarPath = ''
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceList.value = res.list || res.records || res || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    dsCode: '',
    dsName: '',
    dbType: '',
    driverClass: '',
    url: '',
    username: '',
    password: '',
    driverJarPath: ''
  })
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

function handleAdd() {
  dialogTitle.value = '新增数据源'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(ds) {
  dialogTitle.value = '编辑数据源'
  isEdit.value = true
  Object.assign(form, { ...ds, password: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateDatasource(form)
      ElMessage.success('修改成功')
    } else {
      await createDatasource(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(ds) {
  try {
    await ElMessageBox.confirm(`确认删除数据源「${ds.dsName}」？`, '删除确认', { type: 'warning' })
    await deleteDatasource(ds.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleTest(ds) {
  try {
    await testConnection(ds.id)
    ElMessage.success(`数据源「${ds.dsName}」连接成功`)
  } catch (e) {
    // 失败已由 request 拦截器提示
  }
}

async function handleReload(ds) {
  try {
    await ElMessageBox.confirm(
      `重载将重建数据源「${ds.dsName}」的连接池并注册到动态路由，期间该数据源上的查询会短暂中断。是否继续？`,
      '重载确认',
      { type: 'warning', confirmButtonText: '重载', cancelButtonText: '取消' }
    )
    await reloadDatasource(ds.id)
    ElMessage.success(`数据源「${ds.dsName}」重载成功`)
  } catch (e) {
    // 取消或失败（失败已由 request 拦截器提示）
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.toolbar { margin-bottom: 16px; }

.ds-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #F0F0F0;
  transition: all 0.3s;

  &:hover, &.active {
    border-color: #1677FF;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.08);
  }

  .ds-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .ds-name {
    margin: 0 0 4px;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
  }

  .ds-code {
    margin: 0 0 8px;
    font-size: 13px;
    color: #8C8C8C;
  }

  .ds-url {
    margin: 0 0 16px;
    font-size: 12px;
    color: #BFBFBF;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .ds-actions {
    display: flex;
    gap: 8px;
  }
}

.driver-jar-info {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #262626;
  font-size: 13px;

  .jar-name {
    flex: 1;
    word-break: break-all;
  }
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #8C8C8C;
}
</style>
