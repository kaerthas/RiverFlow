<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Document /></el-icon>
      事项管理
    </div>

    <div class="rf-card">
      <!-- 搜索栏 -->
      <el-form :model="queryForm" inline class="search-form">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增事项
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" stripe v-loading="loading" size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="itemCode" label="事项编码" width="160" />
        <el-table-column prop="itemName" label="事项名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="regionName" label="所属区划" width="140" />
        <el-table-column prop="catalogCode" label="国家基本编码" width="140" />
        <el-table-column prop="serviceObj" label"办理对象" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.serviceObj === 0" size="small">个人</el-tag>
            <el-tag v-else type="success" size="small">法人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleBindFlow(row)">绑定流程</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
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
  regionCode: '',
  itemCode: '',
  itemName: '',
  catalogCode: '',
  serviceObj: 0
})

const formRules = {
  regionCode: [{ required: true, message: '请选择区划', trigger: 'change' }],
  itemCode: [{ required: true, message: '请输入事项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入事项名称', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const regionOptions = ref([
  { regionCode: '610000', regionName: '陕西省', children: [
    { regionCode: '610100', regionName: '西安市', children: [
      { regionCode: '610102', regionName: '新城区' },
      { regionCode: '610103', regionName: '碑林区' },
      { regionCode: '610104', regionName: '莲湖区' }
    ]},
    { regionCode: '610200', regionName: '铜川市' }
  ]}
])

const tableData = ref([
  { id: 1, itemCode: '610000-001', itemName: '残疾人证新办', regionName: '陕西省/西安市/新城区', catalogCode: 'A001', serviceObj: 0, status: 1 },
  { id: 2, itemCode: '610000-002', itemName: '困难残疾人生活补贴', regionName: '陕西省/西安市/碑林区', catalogCode: 'A002', serviceObj: 0, status: 1 },
  { id: 3, itemCode: '610000-003', itemName: '火化证明办理', regionName: '陕西省/铜川市', catalogCode: 'B001', serviceObj: 0, status: 1 }
])

function handleSearch() {
  loading.value = true
  setTimeout(() => { loading.value = false }, 500)
}

function handleReset() {
  queryForm.regionCode = ''
  queryForm.itemCode = ''
  queryForm.itemName = ''
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增事项'
  Object.assign(form, { id: null, regionCode: '', itemCode: '', itemName: '', catalogCode: '', serviceObj: 0 })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑事项'
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleSubmit() {
  formRef.value.validate((valid) => {
    if (!valid) return
    ElMessage.success('保存成功')
    dialogVisible.value = false
    handleSearch()
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除事项「${row.itemName}」？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
    handleSearch()
  })
}

function handleStatusChange(row) {
  ElMessage.success(`事项已${row.status === 1 ? '启用' : '停用'}`)
}

function handleBindFlow(row) {
  ElMessage.info(`正在为「${row.itemName}」绑定流程`)
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.search-form {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
