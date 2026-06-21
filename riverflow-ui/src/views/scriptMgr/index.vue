<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">{{ $t('scriptMgr.脚本管理_a1fb7f16') }}</h1>
        <p class="subtitle">维护 Groovy 脚本库，供接口注册与工作流节点复用</p>
      </div>
      <button class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>{{ $t('scriptMgr.新增脚本_8b9bede9_1') }}</button>
    </div>

    <!-- 搜索栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item :label="$t('scriptMgr.脚本编码_bc7ef2af')">
            <el-input v-model="queryForm.scriptCode" :placeholder="$t('scriptMgr.请输入脚本编_23ce028c_1')" clearable />
          </el-form-item>
          <el-form-item :label="$t('scriptMgr.脚本名称_50fb61ef')">
            <el-input v-model="queryForm.scriptName" :placeholder="$t('scriptMgr.请输入脚本名_fb7b9876_1')" clearable />
          </el-form-item>
          <el-form-item :label="$t('scriptMgr.脚本类型_8c4d119a')">
            <el-select v-model="queryForm.scriptType" :placeholder="$t('scriptMgr.请选择_708c9d6d')" clearable style="width: 160px">
              <el-option :label="$t('scriptMgr.结果处理_497201e0_1')" value="result" />
              <el-option :label="$t('scriptMgr.格式化_b70b53b8_1')" value="format" />
              <el-option :label="$t('scriptMgr.请求头_be47bd27_1')" value="header" />
              <el-option :label="$t('scriptMgr.条件判断_56c64d53_1')" value="condition" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>{{ $t('scriptMgr.查询_bee912d7') }}</button>
        <button class="btn-reset" @click="handleReset">{{ $t('scriptMgr.重置_4b9c3271') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="scriptList" stripe v-loading="loading" class="rf-data-table" :fit="false" :empty-text="$t('scriptMgr.暂无数据_21efd88b')">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="scriptCode" :label="$t('scriptMgr.脚本编码_bc7ef2af_1')" width="200">
          <template #default="{ row }">
            <span class="rf-code">{{ row.scriptCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scriptName" :label="$t('scriptMgr.脚本名称_50fb61ef_1')" min-width="320" show-overflow-tooltip />
        <el-table-column prop="scriptType" :label="$t('scriptMgr.类型_226b0912')" width="110" align="center">
          <template #default="{ row }">
            <span class="rf-tag" :class="row.scriptType">
              {{ typeMap[row.scriptType] || row.scriptType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('scriptMgr.状态_3fea7ca7')" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('scriptMgr.创建时间_eca37cb0')" width="170" align="center">
          <template #default="{ row }">
            <span class="rf-time">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('scriptMgr.操作_2b6bc0f2')" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" :title="$t('scriptMgr.编辑_95b351c8')" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('scriptMgr.删除_2f4aaddd')" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" top="5vh" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('scriptMgr.脚本编码_bc7ef2af_1')" prop="scriptCode">
              <el-input v-model="form.scriptCode" :placeholder="$t('scriptMgr.如_4f4c1dda')" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('scriptMgr.脚本名称_50fb61ef_1')" prop="scriptName">
              <el-input v-model="form.scriptName" :placeholder="$t('scriptMgr.如统一结果处_a8e818eb')" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('scriptMgr.脚本类型_8c4d119a_1')" prop="scriptType">
              <el-select v-model="form.scriptType" :placeholder="$t('scriptMgr.请选择_708c9d6d_1')" style="width: 100%">
                <el-option :label="$t('scriptMgr.结果处理_497201e0_1')" value="result" />
                <el-option :label="$t('scriptMgr.格式化_b70b53b8_1')" value="format" />
                <el-option :label="$t('scriptMgr.请求头_be47bd27_1')" value="header" />
                <el-option :label="$t('scriptMgr.条件判断_56c64d53_1')" value="condition" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('scriptMgr.状态_3fea7ca7_1')">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" :active-text="$t('scriptMgr.启用_7854b52a_1')" :inactive-text="$t('scriptMgr.停用_5c56a889_1')" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('scriptMgr.脚本内容_7be2dcb0')" prop="scriptContent">
          <el-input
            v-model="form.scriptContent"
            type="textarea"
            :rows="16"
            :placeholder="$t('scriptMgr.请输入脚本内_3ef37f1d')"
            style="font-family: monospace; font-size: 13px;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('scriptMgr.取消_625fb26b') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('scriptMgr.保存_be5fbbe3') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiScriptList,
  getApiScriptDetail,
  saveApiScript,
  updateApiScript,
  deleteApiScript
} from '@/api/apiMgr'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('scriptMgr.新增脚本_8b9bede9'))
const formRef = ref(null)
const submitLoading = ref(false)

const queryForm = reactive({
  scriptCode: '',
  scriptName: '',
  scriptType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const scriptList = ref([])

const typeMap = {
  result: t('scriptMgr.结果处理_497201e0'),
  format: t('scriptMgr.格式化_b70b53b8'),
  header: t('scriptMgr.请求头_be47bd27'),
  condition: t('scriptMgr.条件判断_56c64d53')
}

const form = reactive({
  id: null,
  scriptCode: '',
  scriptName: '',
  scriptType: 'result',
  scriptContent: '',
  status: 1
})

const formRules = {
  scriptCode: [{ required: true, message: t('scriptMgr.请输入脚本编_23ce028c'), trigger: 'blur' }],
  scriptName: [{ required: true, message: t('scriptMgr.请输入脚本名_fb7b9876'), trigger: 'blur' }],
  scriptType: [{ required: true, message: t('scriptMgr.请选择脚本类_bbcce657'), trigger: 'change' }],
  scriptContent: [{ required: true, message: t('scriptMgr.请输入脚本内_da1cb76e'), trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (queryForm.scriptCode) params.scriptCode = queryForm.scriptCode
    if (queryForm.scriptName) params.scriptName = queryForm.scriptName
    if (queryForm.scriptType) params.scriptType = queryForm.scriptType
    const res = await getApiScriptList(params)
    scriptList.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadList()
}

function handleReset() {
  queryForm.scriptCode = ''
  queryForm.scriptName = ''
  queryForm.scriptType = ''
  pagination.page = 1
  loadList()
}

function handleAdd() {
  dialogTitle.value = t('scriptMgr.新增脚本_8b9bede9_1')
  Object.assign(form, {
    id: null,
    scriptCode: '',
    scriptName: '',
    scriptType: 'result',
    scriptContent: '',
    status: 1
  })
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = `编辑脚本 - ${row.scriptName}`
  try {
    const res = await getApiScriptDetail(row.id)
    Object.assign(form, {
      id: res.id,
      scriptCode: res.scriptCode,
      scriptName: res.scriptName,
      scriptType: res.scriptType,
      scriptContent: res.scriptContent || '',
      status: res.status ?? 1
    })
    dialogVisible.value = true
  } catch (e) {
    // 错误已由 request 拦截器提示
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateApiScript(form)
      ElMessage.success(t('scriptMgr.修改成功_69be6717'))
    } else {
      await saveApiScript(form)
      ElMessage.success(t('scriptMgr.新增成功_a5bfd70d'))
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除脚本「${row.scriptName}」？`, t('scriptMgr.删除确认_50eaf94d'), { type: 'warning' })
    await deleteApiScript(row.id)
    ElMessage.success(t('scriptMgr.删除成功_0007d170'))
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateApiScript({ id: row.id, status: row.status })
    ElMessage.success(`脚本已${row.status === 1 ? t('scriptMgr.启用_7854b52a') : t('scriptMgr.停用_5c56a889')}`)
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.rf-tag {
  &.result {
    background: #dbeafe;
    color: #2563eb;
  }
  &.format {
    background: #fef3c7;
    color: #d97706;
  }
  &.header {
    background: #d1fae5;
    color: #059669;
  }
  &.condition {
    background: #f3e8ff;
    color: #7c3aed;
  }
}
</style>
