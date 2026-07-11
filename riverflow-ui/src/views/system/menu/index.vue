<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <h1 class="title">菜单管理</h1>
      <p class="subtitle">系统菜单与按钮权限维护</p>
      <button v-permission="'system:menu:add'" class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增菜单
      </button>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" row-key="id" :tree-props="{ children: 'children' }" :empty-text="'暂无数据'">
        <el-table-column prop="menuName" label="菜单名称" min-width="180">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span style="margin-left: 8px">{{ row.menuName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 0" type="info">目录</el-tag>
            <el-tag v-else-if="row.menuType === 1" type="primary">菜单</el-tag>
            <el-tag v-else type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" min-width="160" />
        <el-table-column prop="path" label="路由路径" min-width="160" />
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
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
              <button v-permission="'system:menu:edit'" class="action-btn primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button v-permission="'system:menu:add'" class="action-btn success" title="新增子菜单" @click="handleAddChild(row)">
                <el-icon><CirclePlus /></el-icon>
              </button>
              <button v-permission="'system:menu:delete'" class="action-btn danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentMenuOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择上级菜单（为空则为顶层）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 2" label="菜单图标">
          <el-input v-model="form.icon" placeholder="请输入 Element Plus 图标名，如 Setting" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 2" label="路由路径">
          <el-input v-model="form.path" placeholder="请输入路由路径，如 /system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 1" label="组件路径">
          <el-input v-model="form.component" placeholder="请输入组件路径，如 system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
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
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/api/system/menu'
import { Plus, Search, Edit, Delete, CirclePlus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const isEdit = ref(false)
const allMenus = ref([])

const form = reactive({
  id: null,
  parentId: null,
  menuType: 1,
  menuName: '',
  icon: '',
  path: '',
  component: '',
  perms: '',
  sortNo: 0,
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  perms: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

const tableData = ref([])

const parentMenuOptions = computed(() => {
  // 目录和菜单可作为父级，按钮不能
  return buildMenuTree(allMenus.value.filter(m => m.menuType !== 2))
})

onMounted(() => {
  handleSearch()
})

async function handleSearch() {
  loading.value = true
  try {
    const res = await getMenuList({ status: 1 })
    // 统一将 id/parentId 转为字符串，避免后端 Long 序列化与前端 Number 不一致导致树形匹配失败
    allMenus.value = (res || []).map(m => ({
      ...m,
      id: String(m.id),
      parentId: m.parentId == null || String(m.parentId) === '0' ? null : String(m.parentId)
    }))
    tableData.value = buildMenuTree(allMenus.value)
  } finally {
    loading.value = false
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
  return sortTree(tree)
}

function sortTree(nodes) {
  if (!nodes || nodes.length === 0) return nodes
  const sorted = [...nodes].sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
  sorted.forEach(node => {
    if (node.children && node.children.length > 0) {
      node.children = sortTree(node.children)
    }
  })
  return sorted
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  Object.assign(form, { id: null, parentId: null, menuType: 1, menuName: '', icon: '', path: '', component: '', perms: '', sortNo: 0, status: 1 })
  dialogVisible.value = true
}

function handleAddChild(row) {
  isEdit.value = false
  dialogTitle.value = '新增子菜单'
  Object.assign(form, { id: null, parentId: String(row.id), menuType: 1, menuName: '', icon: '', path: '', component: '', perms: '', sortNo: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId != null ? String(row.parentId) : null,
    menuType: row.menuType,
    menuName: row.menuName,
    icon: row.icon,
    path: row.path,
    component: row.component,
    perms: row.perms,
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
      await updateMenu(form)
      ElMessage.success('修改成功')
    } else {
      await createMenu(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    handleSearch()
  } catch (e) {
    console.error('保存菜单失败', e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteMenu(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    })
    .catch(() => {})
}
</script>
