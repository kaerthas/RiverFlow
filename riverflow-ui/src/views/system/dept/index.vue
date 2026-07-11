<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">部门管理</h1>
      <p class="subtitle">组织架构维护</p>
      <button v-permission="'system:dept:add'" class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增部门
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="部门名称">
            <el-input v-model="queryForm.deptName" placeholder="请输入部门名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>查询
        </button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" row-key="id" :tree-props="{ children: 'children' }" :empty-text="'暂无数据'">
        <el-table-column prop="deptName" label="部门名称" min-width="180">
          <template #default="{ row }">
            <el-icon><OfficeBuilding /></el-icon>
            <span style="margin-left: 8px">{{ row.deptName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptCode" label="部门编码" min-width="140" />
        <el-table-column prop="leader" label="负责人" min-width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button v-permission="'system:dept:edit'" class="action-btn primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button v-permission="'system:dept:add'" class="action-btn success" title="新增子部门" @click="handleAddChild(row)">
                <el-icon><CirclePlus /></el-icon>
              </button>
              <button v-permission="'system:dept:delete'" class="action-btn danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="parentDeptOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择上级部门（为空则为顶层）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" placeholder="请输入部门编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptList, createDept, updateDept, deleteDept } from '@/api/system/dept'
import { Plus, Search, Edit, Delete, CirclePlus, OfficeBuilding } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref(null)
const isEdit = ref(false)
const allDepts = ref([])

const queryForm = reactive({
  deptName: '',
  status: null
})

const form = reactive({
  id: null,
  parentId: null,
  deptCode: '',
  deptName: '',
  leader: '',
  phone: '',
  email: '',
  sortNo: 0,
  status: 1
})

const formRules = {
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const tableData = ref([])

const parentDeptOptions = computed(() => {
  return buildTree(allDepts.value)
})

onMounted(() => {
  handleSearch()
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getDeptList({ deptName: queryForm.deptName, status: queryForm.status })
    allDepts.value = res || []
    tableData.value = buildTree(allDepts.value)
  } finally {
    loading.value = false
  }
}

function buildTree(depts) {
  const deptMap = {}
  depts.forEach(d => {
    d.children = []
    deptMap[d.id] = d
  })
  const tree = []
  depts.forEach(d => {
    if (d.parentId && d.parentId !== 0 && deptMap[d.parentId]) {
      deptMap[d.parentId].children.push(d)
    } else {
      tree.push(d)
    }
  })
  return tree.sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
}

function handleReset() {
  queryForm.deptName = ''
  queryForm.status = null
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增部门'
  Object.assign(form, { id: null, parentId: null, deptCode: '', deptName: '', leader: '', phone: '', email: '', sortNo: 0, status: 1 })
  dialogVisible.value = true
}

function handleAddChild(row) {
  isEdit.value = false
  dialogTitle.value = '新增子部门'
  Object.assign(form, { id: null, parentId: row.id, deptCode: '', deptName: '', leader: '', phone: '', email: '', sortNo: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    deptCode: row.deptCode,
    deptName: row.deptName,
    leader: row.leader,
    phone: row.phone,
    email: row.email,
    sortNo: row.sortNo,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateDept(form)
      ElMessage.success('修改成功')
    } else {
      await createDept(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (e) {
    console.error('保存部门失败', e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除部门 "${row.deptName}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteDept(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    })
    .catch(() => {})
}
</script>
