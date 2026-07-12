<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">角色管理</h1>
      <p class="subtitle">系统角色与权限分配</p>
      <button v-permission="'system:role:add'" class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增角色
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="角色编码">
            <el-input v-model="queryForm.roleCode" placeholder="请输入角色编码" clearable />
          </el-form-item>
          <el-form-item label="角色名称">
            <el-input v-model="queryForm.roleName" placeholder="请输入角色名称" clearable />
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
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button v-permission="'system:role:edit'" class="action-btn primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button v-permission="'system:role:edit'" class="action-btn success" title="分配权限" @click="handleAssignMenu(row)">
                <el-icon><Key /></el-icon>
              </button>
              <button v-permission="'system:role:delete'" class="action-btn danger" @click="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.roleDesc" type="textarea" rows="3" placeholder="请输入角色描述" />
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

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配权限" width="600px" destroy-on-close>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/system/role'
import { getAllMenus } from '@/api/system/menu'
import { getUserInfo } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { Plus, Search, Edit, Delete, Key } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const menuTreeRef = ref(null)
const isEdit = ref(false)

const queryForm = reactive({
  roleCode: '',
  roleName: '',
  status: null
})

const form = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  roleDesc: '',
  sortNo: 0,
  status: 1
})

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const menuTreeData = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)
const userStore = useUserStore()

onMounted(() => {
  handleSearch()
})

async function handleSearch() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      roleCode: queryForm.roleCode,
      roleName: queryForm.roleName,
      status: queryForm.status
    }
    const res = await getRoleList(params)
    tableData.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.roleCode = ''
  queryForm.roleName = ''
  queryForm.status = null
  pagination.page = 1
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  Object.assign(form, { id: null, roleCode: '', roleName: '', roleDesc: '', sortNo: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(form, {
    id: row.id,
    roleCode: row.roleCode,
    roleName: row.roleName,
    roleDesc: row.roleDesc,
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
      await updateRole(form)
      ElMessage.success('修改成功')
    } else {
      await createRole(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (e) {
    console.error('保存角色失败', e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    })
    .catch(() => {})
}

async function handleAssignMenu(row) {
  currentRoleId.value = row.id
  try {
    const [menuRes, roleMenuRes] = await Promise.all([getAllMenus(), getRoleMenus(row.id)])
    // 统一将 id/parentId 转为字符串，与后端 Long 序列化保持一致
    const normalizedMenus = (menuRes || []).map(m => ({
      ...m,
      id: String(m.id),
      parentId: m.parentId == null || String(m.parentId) === '0' ? null : String(m.parentId)
    }))
    menuTreeData.value = buildMenuTree(normalizedMenus)
    checkedMenuIds.value = (roleMenuRes || []).map(id => String(id))
    menuDialogVisible.value = true
    // 弹窗渲染完成后手动设置选中状态，避免 default-checked-keys 类型/时序问题
    // 注意：父子联动模式下，setCheckedKeys 设置父节点会自动勾选所有子节点，
    // 因此只传入叶子节点，父节点会根据子节点状态自动半选/全选
    nextTick(() => {
      if (menuTreeRef.value) {
        const allNodes = flattenTree(menuTreeData.value)
        const leafIds = checkedMenuIds.value.filter(id => {
          const node = allNodes.find(n => String(n.id) === String(id))
          return node && (!node.children || node.children.length === 0)
        })
        menuTreeRef.value.setCheckedKeys(leafIds, false)
      }
    })
  } catch (e) {
    console.error('加载菜单失败', e)
  }
}

async function handleSubmitMenus() {
  if (!menuTreeRef.value) return
  // 只取叶子节点，避免父节点半选/全选状态干扰
  const leafKeys = menuTreeRef.value.getCheckedKeys(true) || []
  if (!leafKeys.length) {
    ElMessage.warning('请至少选择一个权限')
    return
  }
  // 自动补齐祖先节点，保证左侧菜单树能正常渲染
  const menuIdSet = new Set(leafKeys.map(id => String(id)))
  const allNodes = flattenTree(menuTreeData.value)
  leafKeys.forEach(leafId => {
    const node = allNodes.find(n => String(n.id) === String(leafId))
    if (node) {
      let parentId = node.parentId
      while (parentId) {
        menuIdSet.add(String(parentId))
        const parent = allNodes.find(n => String(n.id) === String(parentId))
        parentId = parent ? parent.parentId : null
      }
    }
  })
  const menuIds = Array.from(menuIdSet)
  try {
    await assignRoleMenus(currentRoleId.value, menuIds)
    ElMessage.success('权限分配成功')
    menuDialogVisible.value = false
    // 刷新当前登录用户的权限/菜单，确保页面按钮立即响应
    try {
      const userInfo = await getUserInfo()
      userStore.setUserInfo(userInfo)
    } catch (e) {
      console.error('刷新当前用户权限失败', e)
    }
  } catch (e) {
    ElMessage.error('权限分配失败')
    console.error('分配权限失败', e)
  }
}

function buildMenuTree(menus) {
  if (!menus || menus.length === 0) return []
  const menuMap = {}
  // 深拷贝避免修改原始数组，同时统一初始化 children
  const list = menus.map(m => ({ ...m, children: [] }))
  list.forEach(m => {
    menuMap[m.id] = m
  })
  const tree = []
  list.forEach(m => {
    const parentId = m.parentId
    if (parentId && parentId !== '0' && menuMap[parentId]) {
      menuMap[parentId].children.push(m)
    } else {
      tree.push(m)
    }
  })
  return tree.sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
}

function flattenTree(nodes, result = []) {
  nodes.forEach(node => {
    result.push(node)
    if (node.children && node.children.length) {
      flattenTree(node.children, result)
    }
  })
  return result
}
</script>
