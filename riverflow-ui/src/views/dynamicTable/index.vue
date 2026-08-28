<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">动态表设计器</h1>
      <p class="subtitle">管理动态数据表结构，设计字段并生成 CRUD API</p>
      <button class="btn-primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建表
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="表编码">
            <el-input v-model="queryForm.tableCode" placeholder="请输入表编码" clearable />
          </el-form-item>
          <el-form-item label="表名称">
            <el-input v-model="queryForm.tableName" placeholder="请输入表名称" clearable />
          </el-form-item>
          <el-form-item label="所属数据源">
            <el-select v-model="queryForm.dsId" placeholder="全部" clearable style="width: 180px">
              <el-option
                v-for="ds in datasourceOptions"
                :key="ds.id"
                :label="ds.dsName"
                :value="ds.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="已生成" :value="1" />
              <el-option label="草稿" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">查询</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableList" stripe v-loading="loading" class="rf-data-table" :fit="false" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="tableCode" label="表编码" width="260">
          <template #default="{ row }">
            <span class="rf-code">{{ row.tableCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" label="表名称" min-width="180" />
        <el-table-column prop="dsName" label="所属数据源" width="170">
          <template #default="{ row }">
            {{ row.dsName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="columnCount" label="字段数" width="120" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.columnCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 1" class="rf-status success"><span class="dot"></span>已生成</span>
            <span v-else class="rf-status draft"><span class="dot"></span>草稿</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="195">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="设计表" @click="handleDesign(row)">
                <el-icon><EditPen /></el-icon>
              </button>
              <button class="action-btn warning" title="创建表" @click="handleCreatePhysical(row)">
                <el-icon><Coin /></el-icon>
              </button>
              <button v-if="row.status !== 1" class="action-btn success" title="发布" @click="handlePublish(row)">
                <el-icon><Check /></el-icon>
              </button>
              <button class="action-btn" title="生成API" @click="handleGenApi(row)">
                <el-icon><Promotion /></el-icon>
              </button>
              <button class="action-btn info" title="查看数据" @click="handleViewData(row)">
                <el-icon><View /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="handleDelete(row)">
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
          @update:page-size="loadList"
          @update:current-page="loadList"
        />
      </div>
    </div>

    <!-- 新建/编辑表弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      top="5vh"
      destroy-on-close
      :close-on-click-modal="false"
      class="edit-dialog"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
            <el-form-item label="表编码" prop="tableCode">
              <el-input v-model="form.tableCode" placeholder="如 t_business_info" :disabled="!!form.id" />
            </el-form-item>
            <el-form-item label="表名称" prop="tableName">
              <el-input v-model="form.tableName" placeholder="如 业务申办信息表" />
            </el-form-item>
            <el-form-item label="数据源" prop="dsId">
              <el-select v-model="form.dsId" placeholder="请选择数据源" style="width: 100%">
                <el-option
                  v-for="ds in datasourceOptions"
                  :key="ds.id"
                  :label="ds.dsName"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="表用途说明" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="字段设计" name="columns">
          <TableDesigner ref="designerRef" v-model="columns" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 动态表数据管理抽屉 -->
    <DynamicCrudDrawer
      ref="crudDrawerRef"
      :table-id="currentCrudTable.id"
      :table-code="currentCrudTable.tableCode"
      :table-name="currentCrudTable.tableName"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTableList,
  createTable,
  getTableDetail,
  getTableColumns,
  saveTableColumns,
  generateApi,
  deleteTable,
  createPhysicalTable,
  publishTable
} from '@/api/dynamicTable'
import { getDatasourceList } from '@/api/datasource'
import TableDesigner from '@/components/TableDesigner/index.vue'
import DynamicCrudDrawer from '@/components/DynamicCrudDrawer/index.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建表')
const formRef = ref(null)
const designerRef = ref(null)
const submitLoading = ref(false)
const activeTab = ref('base')
const crudDrawerRef = ref(null)
const currentCrudTable = reactive({
  id: null,
  tableCode: '',
  tableName: ''
})

const tableList = ref([])
const datasourceOptions = ref([])

const queryForm = reactive({
  tableCode: '',
  tableName: '',
  dsId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  tableCode: '',
  tableName: '',
  dsId: null,
  remark: '',
  status: 0
})

const formRules = {
  tableCode: [{ required: true, message: '请输入表编码', trigger: 'blur' }],
  tableName: [{ required: true, message: '请输入表名称', trigger: 'blur' }],
  dsId: [{ required: true, message: '请选择数据源', trigger: 'change' }]
}

const columns = ref([])

const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 19) : '-'

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (queryForm.tableCode) params.tableCode = queryForm.tableCode
    if (queryForm.tableName) params.tableName = queryForm.tableName
    if (queryForm.dsId !== null && queryForm.dsId !== undefined && queryForm.dsId !== '') params.dsId = queryForm.dsId
    if (queryForm.status !== null && queryForm.status !== undefined && queryForm.status !== '') params.status = queryForm.status
    const res = await getTableList(params)
    tableList.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

async function loadDatasourceOptions() {
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceOptions.value = res.list || res.records || res || []
  } catch (e) {
    datasourceOptions.value = []
  }
}

function handleSearch() {
  pagination.page = 1
  loadList()
}

function handleReset() {
  queryForm.tableCode = ''
  queryForm.tableName = ''
  queryForm.dsId = null
  queryForm.status = null
  pagination.page = 1
  loadList()
}

function handleCreate() {
  dialogTitle.value = '新建表'
  activeTab.value = 'base'
  Object.assign(form, { id: null, tableCode: '', tableName: '', dsId: null, remark: '', status: 0 })
  columns.value = []
  dialogVisible.value = true
}

async function handleDesign(row) {
  dialogTitle.value = `编辑表 - ${row.tableName}`
  activeTab.value = 'base'
  Object.assign(form, { ...row })
  columns.value = []
  dialogVisible.value = true
  // 加载字段
  await nextTick()
  try {
    const cols = await getTableColumns(row.id)
    columns.value = Array.isArray(cols) ? cols : []
  } catch (e) {
    columns.value = []
  }
}

async function handleSubmit() {
  if (activeTab.value === 'columns') {
    activeTab.value = 'base'
    await nextTick()
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...form }
    const saved = await createTable(payload)
    const tableId = saved?.id || form.id
    if (tableId && columns.value.length) {
      const validCols = designerRef.value?.getValidColumns() || columns.value.filter(c => c.columnCode && c.columnName)
      if (validCols.length) {
        await saveTableColumns(tableId, validCols)
      }
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleCreatePhysical(row) {
  try {
    const res = await createPhysicalTable(row.id)
    ElMessage.success(`「${row.tableName}」物理表创建成功`)
    loadList()
  } catch (e) {
    // 失败已由 request 拦截器提示
  }
}

async function handlePublish(row) {
  try {
    await publishTable(row.id)
    ElMessage.success(`「${row.tableName}」已发布`)
    loadList()
  } catch (e) {
    // 失败已由 request 拦截器提示
  }
}

async function handleGenApi(row) {
  try {
    await generateApi(row.id)
    ElMessage.success(`已为「${row.tableName}」生成 CRUD 接口`)
    loadList()
  } catch (e) {
    // 失败已由 request 拦截器提示
  }
}

function handleViewData(row) {
  Object.assign(currentCrudTable, {
    id: row.id,
    tableCode: row.tableCode,
    tableName: row.tableName
  })
  nextTick(() => {
    crudDrawerRef.value?.open()
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除表「${row.tableName}」？`, '删除确认', { type: 'warning' })
    await deleteTable(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(() => {
  loadDatasourceOptions()
  loadList()
})
</script>

<style scoped lang="scss">
</style>
