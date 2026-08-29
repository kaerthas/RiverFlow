<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">应用管理</h1>
        <p class="subtitle">以应用为中心组织和管理对外 API 接口</p>
      </div>
      <button class="btn-primary" @click="handleAddApp">
        <el-icon><Plus /></el-icon> 新建应用
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form inline>
          <el-form-item label="应用">
            <el-input v-model="keyword" placeholder="搜索应用名称 / 编码" clearable style="width: 240px" @keyup.enter="handleSearch" @clear="handleSearch" />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <span class="toolbar-total">共 {{ pagination.total }} 个应用</span>
        <el-radio-group v-model="viewMode" class="view-switch">
          <el-radio-button label="grid"><el-icon><Grid /></el-icon></el-radio-button>
          <el-radio-button label="list"><el-icon><List /></el-icon></el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-loading="loading" class="app-grid" v-if="viewMode === 'grid'">
      <!-- 全部接口入口 -->
      <div class="app-card all-card" @click="goApis()">
        <div class="app-card-top">
          <div class="app-icon all-icon">
            <el-icon :size="22"><Grid /></el-icon>
          </div>
          <el-icon class="go-icon"><ArrowRight /></el-icon>
        </div>
        <div class="app-name">全部接口</div>
        <div class="app-desc">跨应用浏览与检索所有已注册接口</div>
        <div class="app-card-footer">
          <span class="api-count">{{ totalApiCount }} 个接口</span>
        </div>
      </div>

      <!-- 应用卡片 -->
      <div v-for="app in filteredApps" :key="app.id" class="app-card" @click="goApis(app)">
        <div class="app-card-top">
          <div class="app-icon" :style="iconStyle(app)">
            <el-icon :size="22"><component :is="app.icon || 'Folder'" /></el-icon>
          </div>
          <span :class="['status-dot', app.status === 1 ? 'on' : 'off']">
            {{ app.status === 1 ? '启用' : '停用' }}
          </span>
        </div>
        <div class="app-name">{{ app.appName }}</div>
        <div class="app-code">{{ app.appCode }}</div>
        <div class="app-desc">{{ app.description || '暂无描述' }}</div>
        <div class="app-card-footer">
          <span class="api-count">{{ app.apiCount || 0 }} 个接口</span>
          <div class="app-actions" @click.stop>
            <el-button link type="primary" size="small" @click="handleEditApp(app)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteApp(app)">删除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && filteredApps.length === 0" description="暂无应用，点击右上角新建" style="grid-column: 1 / -1" />
    </div>

    <!-- 网格模式分页 -->
    <div v-if="viewMode === 'grid' && pagination.total > 0" class="grid-pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48, 96]"
        layout="total, sizes, prev, pager, next"
        background
        @update:page-size="handlePageSize"
        @update:current-page="loadAppList"
      />
    </div>

    <!-- 列表视图 -->
    <div v-else class="rf-table-card app-table-card">
      <el-table :data="filteredApps" stripe v-loading="loading" class="rf-data-table" style="width: 100%" empty-text="暂无应用">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column label="应用" min-width="200">
          <template #default="{ row }">
            <div class="app-cell" @click="goApis(row)">
              <div class="app-icon sm" :style="iconStyle(row)">
                <el-icon :size="16"><component :is="row.icon || 'Folder'" /></el-icon>
              </div>
              <span class="app-cell-name">{{ row.appName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="appCode" label="应用编码" width="180">
          <template #default="{ row }">
            <span class="rf-code">{{ row.appCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="接口数" width="90" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.apiCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" class-name="cell-wrap">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn success" title="管理接口" @click="goApis(row)">
                <el-icon><Promotion /></el-icon>
              </button>
              <button class="action-btn primary" title="编辑" @click="handleEditApp(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="handleDeleteApp(row)">
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
          @update:page-size="handlePageSize"
          @update:current-page="loadAppList"
        />
      </div>
    </div>

    <!-- 应用编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" top="20vh" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="form.appCode" placeholder="如 user-center" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="如 用户中心" />
        </el-form-item>
        <el-form-item label="应用图标" prop="icon">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Folder" />
        </el-form-item>
        <el-form-item label="AppKey">
          <el-input v-model="form.appKey" placeholder="应用标识">
            <template #append>
              <el-button @click="generateAppKey">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="AppSecret">
          <el-input v-model="form.appSecret" type="password" show-password placeholder="应用密钥">
            <template #append>
              <el-button @click="generateAppSecret">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入应用描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApiAppList, saveApiApp, updateApiApp, deleteApiApp, getApiAppCounts } from '@/api/apiApp'
import { getApiCatalogList } from '@/api/apiMgr'

const router = useRouter()

const loading = ref(false)
const appList = ref([])
const keyword = ref('')
const totalApiCount = ref(0)
const viewMode = ref('grid')

const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

const filteredApps = computed(() => appList.value)

/* 应用图标配色：按 appCode 稳定取色 */
const iconPalettes = [
  { background: 'linear-gradient(135deg, #dbeafe, #bfdbfe)', color: '#2563eb' },
  { background: 'linear-gradient(135deg, #d1fae5, #a7f3d0)', color: '#10b981' },
  { background: 'linear-gradient(135deg, #fef3c7, #fde68a)', color: '#f59e0b' },
  { background: 'linear-gradient(135deg, #fee2e2, #fecaca)', color: '#ef4444' },
  { background: 'linear-gradient(135deg, #ede9fe, #ddd6fe)', color: '#7c3aed' },
  { background: 'linear-gradient(135deg, #cffafe, #a5f3fc)', color: '#0891b2' }
]

function iconStyle(app) {
  const key = app.appCode || app.appName || 'x'
  let h = 0
  for (let i = 0; i < key.length; i++) {
    h = (h * 31 + key.charCodeAt(i)) >>> 0
  }
  return iconPalettes[h % iconPalettes.length]
}

async function loadAppList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (keyword.value) params.keyword = keyword.value
    const res = await getApiAppList(params)
    const list = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
    // 批量统计各应用接口数
    const appIds = list.map(a => a.id).filter(Boolean)
    if (appIds.length > 0) {
      try {
        const counts = await getApiAppCounts(appIds)
        list.forEach(app => {
          app.apiCount = (counts && counts[app.id]) || 0
        })
      } catch (e) {
        // 忽略统计错误
      }
    }
    appList.value = list
  } finally {
    loading.value = false
  }
}

async function loadTotalApiCount() {
  try {
    const res = await getApiCatalogList({ page: 1, size: 1 })
    totalApiCount.value = Number(res.total) || 0
  } catch (e) {
    totalApiCount.value = 0
  }
}

function handleSearch() {
  pagination.page = 1
  loadAppList()
}

/* 切换网格/列表视图时，重置到第一页并应用各自的默认分页大小 */
watch(viewMode, (mode) => {
  pagination.page = 1
  pagination.size = mode === 'grid' ? 12 : 10
  loadAppList()
})

function handlePageSize() {
  pagination.page = 1
  loadAppList()
}

function goApis(app) {
  if (app) {
    router.push({ path: '/api-mgr', query: { appId: app.id } })
  } else {
    router.push('/api-mgr')
  }
}

/* 应用新建/编辑 */
const dialogVisible = ref(false)
const dialogTitle = ref('新增应用')
const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({
  id: null,
  appCode: '',
  appName: '',
  appKey: '',
  appSecret: '',
  description: '',
  icon: '',
  sortNo: 0,
  status: 1
})
const formRules = {
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
}

function handleAddApp() {
  dialogTitle.value = '新增应用'
  Object.assign(form, {
    id: null,
    appCode: '',
    appName: '',
    appKey: '',
    appSecret: '',
    description: '',
    icon: '',
    sortNo: 0,
    status: 1
  })
  dialogVisible.value = true
}

function handleEditApp(app) {
  dialogTitle.value = '编辑应用'
  Object.assign(form, {
    id: app.id,
    appCode: app.appCode,
    appName: app.appName,
    appKey: app.appKey || '',
    appSecret: app.appSecret || '',
    description: app.description,
    icon: app.icon,
    sortNo: app.sortNo,
    status: app.status
  })
  dialogVisible.value = true
}

async function handleDeleteApp(app) {
  try {
    await ElMessageBox.confirm(`确认删除应用「${app.appName}」？`, '删除确认', { type: 'warning' })
    await deleteApiApp(app.id)
    ElMessage.success('删除成功')
    loadAppList()
  } catch (e) {
    // 取消或失败
  }
}

function generateRandomString(length = 16) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

function generateAppKey() {
  form.appKey = 'ak_' + generateRandomString(16)
}

function generateAppSecret() {
  form.appSecret = generateRandomString(32)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateApiApp(form)
    } else {
      await saveApiApp(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadAppList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadAppList()
  loadTotalApiCount()
})
</script>

<style scoped lang="scss">
.toolbar-total {
  font-size: 13px;
  color: var(--rf-text-muted);
  margin-right: 8px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.app-card {
  background: var(--rf-bg-card);
  border: 1px solid var(--rf-border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform var(--duration-base) var(--ease-spring),
    box-shadow var(--duration-base) var(--ease-out-quart);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }

  .app-card-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .app-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .all-icon {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
  }

  .go-icon {
    color: var(--rf-text-muted);
    margin-top: 4px;
  }

  .status-dot {
    font-size: 12px;
    padding: 2px 10px;
    border-radius: 999px;

    &.on {
      background: #d1fae5;
      color: #059669;
    }

    &.off {
      background: var(--rf-neutral-100);
      color: var(--rf-text-muted);
    }
  }

  .app-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--rf-text-main);
    letter-spacing: -0.01em;
  }

  .app-code {
    font-family: var(--font-mono, monospace);
    font-size: 12px;
    color: var(--rf-primary);
    background: var(--rf-primary-light);
    padding: 2px 8px;
    border-radius: 6px;
    align-self: flex-start;
  }

  .app-desc {
    font-size: 12px;
    color: var(--rf-text-muted);
    line-height: 1.6;
    min-height: 38px;
  }

  .app-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .api-count {
    font-size: 12px;
    font-weight: 500;
    color: var(--rf-text-secondary);
  }
}

.all-card {
  border-style: dashed;
  border-color: var(--rf-primary);
}

.app-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;

  .app-icon.sm {
    width: 30px;
    height: 30px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .app-cell-name {
    font-weight: 500;
    color: var(--rf-text-main);
  }
}

.grid-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid var(--rf-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
</style>
