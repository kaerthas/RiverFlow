<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">用户管理</h1>
      <p class="subtitle">系统用户与角色维护</p>
      <button v-permission="'system:user:add'" class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="用户名">
            <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="queryForm.realName" placeholder="请输入真实姓名" clearable />
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
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button v-permission="'system:user:edit'" class="action-btn primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button v-permission="'system:user:delete'" class="action-btn danger" @click="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择部门"
            style="width: 100%"
            @change="handleDeptChange"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
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
import { getUserList, getUserDetail, createUser, updateUser, deleteUser } from '@/api/system/user'
import { getRoleList } from '@/api/system/role'
import { getAllDepts } from '@/api/system/dept'
import { Plus, Search, Edit, Delete, OfficeBuilding } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const isEdit = ref(false)

const queryForm = reactive({
  username: '',
  realName: '',
  status: null
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  roleIds: [],
  status: 1
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: !isEdit.value, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const roleOptions = ref([])
const deptOptions = ref([])

onMounted(() => {
  handleSearch()
  loadRoles()
  loadDepts()
})

async function loadDepts() {
  try {
    const res = await getAllDepts()
    deptOptions.value = res || []
  } catch (e) {
    console.error('加载部门失败', e)
  }
}

async function loadRoles() {
  try {
    const res = await getRoleList({ page: 1, size: 999, status: 1 })
    roleOptions.value = res.list || res.records || res || []
  } catch (e) {
    console.error('加载角色失败', e)
  }
}

async function handleSearch() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      username: queryForm.username,
      realName: queryForm.realName,
      status: queryForm.status
    }
    const res = await getUserList(params)
    tableData.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.status = null
  pagination.page = 1
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', deptId: null, deptName: '', roleIds: [], status: 1 })
  dialogVisible.value = true
}

function handleDeptChange(deptId) {
  const dept = findDeptById(deptOptions.value, deptId)
  form.deptName = dept ? dept.deptName : ''
}

function findDeptById(depts, id) {
  if (!id || !depts) return null
  for (const d of depts) {
    if (d.id === id) return d
    if (d.children) {
      const found = findDeptById(d.children, id)
      if (found) return found
    }
  }
  return null
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  let roleIds = []
  try {
    const res = await getUserDetail(row.id)
    roleIds = res.roleIds || []
  } catch (e) {
    console.error('获取用户详情失败', e)
  }
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    deptId: row.deptId,
    deptName: row.deptName,
    roleIds: roleIds,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateUser(form)
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (e) {
    console.error('保存用户失败', e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    })
    .catch(() => {})
}
</script>
