<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">{{ $t('dynamicTable.动态表设计器_40e83ea4') }}</h1>
      <p class="subtitle">{{ $t('dynamicTable.管理动态数据表结构设计字段并生成_CRUD_API_40e83ea4') }}</p>
      <button class="btn-primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>{{ $t('dynamicTable.新建表_6cf9db59_1') }}</button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <!-- 暂无搜索条件 -->
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="loadList">{{ $t('dynamicTable.查询_bee912d7') }}</button>
        <button class="btn-reset" @click="handleReset">{{ $t('dynamicTable.重置_4b9c3271') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableList" stripe v-loading="loading" class="rf-data-table" :fit="false" :empty-text="$t('dynamicTable.暂无数据_21efd88b')">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="tableCode" :label="$t('dynamicTable.表编码_9d28223c')" width="260">
          <template #default="{ row }">
            <span class="rf-code">{{ row.tableCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" :label="$t('dynamicTable.表名称_ce702830')" min-width="180" />
        <el-table-column prop="dsName" :label="$t('dynamicTable.所属数据源_bec6d5bc')" width="170">
          <template #default="{ row }">
            {{ row.dsName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="columnCount" :label="$t('dynamicTable.字段数_62613f52')" width="120" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.columnCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('dynamicTable.状态_3fea7ca7')" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 1" class="rf-status success"><span class="dot"></span>{{ $t('dynamicTable.已生成_c2ad1f29') }}</span>
            <span v-else class="rf-status draft"><span class="dot"></span>{{ $t('dynamicTable.草稿_22b4334f') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('dynamicTable.创建时间_eca37cb0')" width="195">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('dynamicTable.操作_2b6bc0f2')" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" :title="$t('dynamicTable.设计表_13ad7854')" @click="handleDesign(row)">
                <el-icon><EditPen /></el-icon>
              </button>
              <button class="action-btn warning" :title="$t('dynamicTable.创建表_b9cdb1ae')" @click="handleCreatePhysical(row)">
                <el-icon><Coin /></el-icon>
              </button>
              <button v-if="row.status !== 1" class="action-btn success" :title="$t('dynamicTable.发布_83611abd')" @click="handlePublish(row)">
                <el-icon><Check /></el-icon>
              </button>
              <button class="action-btn" :title="$t('dynamicTable.生成_cc716fdf')" @click="handleGenApi(row)">
                <el-icon><Promotion /></el-icon>
              </button>
              <button class="action-btn info" :title="$t('dynamicTable.查看数据_c00a96da')" @click="handleViewData(row)">
                <el-icon><View /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('dynamicTable.删除_2f4aaddd')" @click="handleDelete(row)">
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
        <el-tab-pane :label="$t('dynamicTable.基本信息_9e5ffa06')" name="base">
          <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
            <el-form-item :label="$t('dynamicTable.表编码_9d28223c_1')" prop="tableCode">
              <el-input v-model="form.tableCode" :placeholder="$t('dynamicTable.如_cf3df70b')" :disabled="!!form.id" />
            </el-form-item>
            <el-form-item :label="$t('dynamicTable.表名称_ce702830_1')" prop="tableName">
              <el-input v-model="form.tableName" :placeholder="$t('dynamicTable.如业务申办信_82915b9d')" />
            </el-form-item>
            <el-form-item :label="$t('dynamicTable.数据源_c11322c9')" prop="dsId">
              <el-select v-model="form.dsId" :placeholder="$t('dynamicTable.请选择数据源_9acb966d_1')" style="width: 100%">
                <el-option
                  v-for="ds in datasourceOptions"
                  :key="ds.id"
                  :label="ds.dsName"
                  :value="ds.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('dynamicTable.备注_2432b575')">
              <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="$t('dynamicTable.表用途说明_a13e5d7c')" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="$t('dynamicTable.字段设计_d89f9f29')" name="columns">
          <TableDesigner ref="designerRef" v-model="columns" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('dynamicTable.取消_625fb26b') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('dynamicTable.保存_be5fbbe3') }}</el-button>
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
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
const dialogTitle = ref(t('dynamicTable.新建表_6cf9db59'))
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
  tableCode: [{ required: true, message: t('dynamicTable.请输入表编码_a9a8429d'), trigger: 'blur' }],
  tableName: [{ required: true, message: t('dynamicTable.请输入表名称_87701aa1'), trigger: 'blur' }],
  dsId: [{ required: true, message: t('dynamicTable.请选择数据源_9acb966d'), trigger: 'change' }]
}

const columns = ref([])

const formatTime = (time) => time ? time.replace('T', ' ').substring(0, 19) : '-'

async function loadList() {
  loading.value = true
  try {
    const res = await getTableList({ page: pagination.page, size: pagination.size })
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

function handleReset() {
  pagination.page = 1
  loadList()
}

function handleCreate() {
  dialogTitle.value = t('dynamicTable.新建表_6cf9db59_1')
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
    ElMessage.success(t('dynamicTable.保存成功_3b108349'))
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
    await ElMessageBox.confirm(`确认删除表「${row.tableName}」？`, t('dynamicTable.删除确认_50eaf94d'), { type: 'warning' })
    await deleteTable(row.id)
    ElMessage.success(t('dynamicTable.删除成功_0007d170'))
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
