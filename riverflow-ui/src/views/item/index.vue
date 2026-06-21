<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">{{ $t('item.事项管理_b3d49744') }}</h1>
      <p class="subtitle">{{ $t('item.事项信息维护_2fb5cd33') }}</p>
      <button class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>{{ $t('item.新增事项_4fe0c271_1') }}</button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item :label="$t('item.区划_e48efb46')">
            <el-cascader
              v-model="queryForm.regionCode"
              :options="regionOptions"
              :props="{ checkStrictly: true, value: 'regionCode', label: 'regionName' }"
              :placeholder="$t('item.选择区划_33fa23df')"
              clearable
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item :label="$t('item.事项编码_ff4ea1da')">
            <el-input v-model="queryForm.itemCode" :placeholder="$t('item.请输入事项编_b332e483_1')" clearable />
          </el-form-item>
          <el-form-item :label="$t('item.事项名称_97d7ea73')">
            <el-input v-model="queryForm.itemName" :placeholder="$t('item.请输入事项名_ca773a8a_1')" clearable />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>{{ $t('item.查询_bee912d7') }}</button>
        <button class="btn-reset" @click="handleReset">{{ $t('item.重置_4b9c3271') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :fit="false" :empty-text="$t('item.暂无数据_21efd88b')">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="itemCode" :label="$t('item.事项编码_ff4ea1da_1')" width="240">
          <template #default="{ row }">
            <span class="rf-code">{{ row.itemCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" :label="$t('item.事项名称_97d7ea73_1')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="regionName" :label="$t('item.所属区划_4418eb03')" width="160" />
        <el-table-column prop="catalogCode" :label="$t('item.国家基本编码_2037ce83')" width="180" />
        <el-table-column prop="serviceObj" :label="$t('item.办理对象_da4e0934')" width="110">
          <template #default="{ row }">
            <span v-if="row.serviceObj === 0" class="rf-tag manual">{{ $t('item.个人_6a0e0419') }}</span>
            <span v-else class="rf-tag event">{{ $t('item.法人_e1a43702') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('item.状态_3fea7ca7')" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('item.操作_2b6bc0f2')" width="120" fixed="right">
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
        <el-form-item :label="$t('item.所属区划_4418eb03_1')" prop="regionCode">
          <el-cascader
            v-model="form.regionCode"
            :options="regionOptions"
            :props="{ checkStrictly: true, value: 'regionCode', label: 'regionName' }"
            :placeholder="$t('item.选择区划_33fa23df_1')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('item.事项编码_ff4ea1da_1')" prop="itemCode">
          <el-input v-model="form.itemCode" :placeholder="$t('item.请输入事项编_b332e483_1')" />
        </el-form-item>
        <el-form-item :label="$t('item.事项名称_97d7ea73_1')" prop="itemName">
          <el-input v-model="form.itemName" :placeholder="$t('item.请输入事项名_ca773a8a_1')" />
        </el-form-item>
        <el-form-item :label="$t('item.国家基本编码_2037ce83_1')">
          <el-input v-model="form.catalogCode" :placeholder="$t('item.请输入国家基_db104d2a')" />
        </el-form-item>
        <el-form-item :label="$t('item.办理对象_da4e0934_1')">
          <el-radio-group v-model="form.serviceObj">
            <el-radio :label="0">{{ $t('item.个人_6a0e0419_1') }}</el-radio>
            <el-radio :label="1">{{ $t('item.法人_e1a43702_1') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('item.取消_625fb26b') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('item.确定_38cf16f2') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getItemList, createItem, updateItem, deleteItem, getRegionTree } from '@/api/item'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('item.新增事项_4fe0c271'))
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
  regionCode: [{ required: true, message: t('item.请选择区划_d74e9da8'), trigger: 'change', type: 'array' }],
  itemCode: [{ required: true, message: t('item.请输入事项编_b332e483'), trigger: 'blur' }],
  itemName: [{ required: true, message: t('item.请输入事项名_ca773a8a'), trigger: 'blur' }]
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
  dialogTitle.value = t('item.新增事项_4fe0c271_1')
  Object.assign(form, { id: null, regionCode: [], itemCode: '', itemName: '', catalogCode: '', serviceObj: 0 })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = t('item.编辑事项_28f1987b')
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
      ElMessage.success(t('item.修改成功_69be6717'))
    } else {
      await createItem(payload)
      ElMessage.success(t('item.新增成功_a5bfd70d'))
    }
    dialogVisible.value = false
    handleSearch()
  } catch (error) {
    // 错误已由 request 拦截器提示
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除事项「${row.itemName}」？`, t('item.提示_02d9819d'), { type: 'warning' })
    await deleteItem(row.id)
    ElMessage.success(t('item.删除成功_0007d170'))
    handleSearch()
  } catch (e) {
    // 取消或接口失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateItem({ ...row, status: row.status })
    ElMessage.success(`事项已${row.status === 1 ? t('item.启用_7854b52a') : t('item.停用_5c56a889')}`)
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
