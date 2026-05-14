<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">事项管理</h1>
      <p class="subtitle">事项信息维护与流程绑定</p>
      <button class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增事项
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="区划">
            <el-cascader
              v-model="queryForm.regionCode"
              :options="regionOptions"
              :props="{ checkStrictly: true, value: 'regionCode', label: 'regionName' }"
              placeholder="选择区划"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="事项编码">
            <el-input v-model="queryForm.itemCode" placeholder="请输入事项编码" clearable />
          </el-form-item>
          <el-form-item label="事项名称">
            <el-input v-model="queryForm.itemName" placeholder="请输入事项名称" clearable />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon> 查询
        </button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :fit="false" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="itemCode" label="事项编码" width="240">
          <template #default="{ row }">
            <span class="rf-code">{{ row.itemCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="事项名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="regionName" label="所属区划" width="160" />
        <el-table-column prop="catalogCode" label="国家基本编码" width="180" />
        <el-table-column prop="serviceObj" label="办理对象" width="110">
          <template #default="{ row }">
            <span v-if="row.serviceObj === 0" class="rf-tag manual">个人</span>
            <span v-else class="rf-tag event">法人</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn success" @click="handleBindFlow(row)">
                <el-icon><Link /></el-icon>
              </button>
              <button class="action-btn danger" @click="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close class="edit-dialog">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
        <el-form-item label="所属区划" prop="regionCode">
          <el-cascader
            v-model="form.regionCode"
            :options="regionOptions"
            :props="{ checkStrictly: true, value: 'regionCode', label: 'regionName' }"
            placeholder="选择区划"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="事项编码" prop="itemCode">
          <el-input v-model="form.itemCode" placeholder="请输入事项编码" />
        </el-form-item>
        <el-form-item label="事项名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入事项名称" />
        </el-form-item>
        <el-form-item label="国家基本编码">
          <el-input v-model="form.catalogCode" placeholder="请输入国家基本编码" />
        </el-form-item>
        <el-form-item label="办理对象">
          <el-radio-group v-model="form.serviceObj">
            <el-radio :label="0">个人</el-radio>
            <el-radio :label="1">法人</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getItemList, createItem, updateItem, deleteItem, getRegionTree } from '@/api/item'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增事项')
const formRef = ref(null)

const queryForm = reactive({
  regionCode: '',
  itemCode: '',
  itemName: ''
})

const form = reactive({
  id: null,
  regionCode: [],
  itemCode: '',
  itemName: '',
  catalogCode: '',
  serviceObj: 0
})

const formRules = {
  regionCode: [{ required: true, message: '请选择区划', trigger: 'change', type: 'array' }],
  itemCode: [{ required: true, message: '请输入事项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入事项名称', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const regionOptions = ref([])

const tableData = ref([])

async function handleSearch() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      itemCode: queryForm.itemCode,
      itemName: queryForm.itemName
    }
    if (queryForm.regionCode && queryForm.regionCode.length) {
      params.regionCode = queryForm.regionCode[queryForm.regionCode.length - 1]
    }
    const res = await getItemList(params)
    tableData.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.regionCode = ''
  queryForm.itemCode = ''
  queryForm.itemName = ''
  pagination.page = 1
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增事项'
  Object.assign(form, { id: null, regionCode: [], itemCode: '', itemName: '', catalogCode: '', serviceObj: 0 })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑事项'
  // 区划回显为数组，简化处理直接用 regionCode
  Object.assign(form, { ...row, regionCode: row.regionCode ? [row.regionCode] : [] })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const payload = {
      ...form,
      regionCode: Array.isArray(form.regionCode) ? form.regionCode[form.regionCode.length - 1] : form.regionCode
    }
    if (form.id) {
      await updateItem(payload)
      ElMessage.success('修改成功')
    } else {
      await createItem(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (error) {
    // 错误已由 request 拦截器提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除事项「${row.itemName}」？`, '提示', { type: 'warning' })
    await deleteItem(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (e) {
    // 取消或接口失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateItem({ ...row, status: row.status })
    ElMessage.success(`事项已${row.status === 1 ? '启用' : '停用'}`)
  } catch (e) {
    // 失败时回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

function handleBindFlow(row) {
  ElMessage.info(`正在为「${row.itemName}」绑定流程（功能开发中）`)
}

async function loadRegionTree() {
  try {
    const res = await getRegionTree()
    regionOptions.value = res || []
  } catch (e) {
    regionOptions.value = []
  }
}

onMounted(() => {
  loadRegionTree()
  handleSearch()
})
</script>
