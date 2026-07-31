<template>
  <div class="api-mgr-page" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- 左侧面板：应用目录 -->
    <Transition name="sidebar">
      <div v-show="!sidebarCollapsed" class="app-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">
            <el-icon :size="18"><FolderOpened /></el-icon>
            <span>应用目录</span>
          </div>
          <div class="sidebar-actions">
            <el-button type="primary" size="small" circle @click="handleAddApp">
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button size="small" text circle @click="sidebarCollapsed = true">
              <el-icon><Fold /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="app-search">
          <el-input v-model="appKeyword" placeholder="搜索应用" clearable prefix-icon="Search" size="small" />
        </div>
        <div v-loading="appLoading" class="app-list">
          <div class="app-item all-apps" :class="{ active: !selectedAppId }" @click="selectApp(null)">
            <div class="app-icon all">
              <el-icon :size="20"><Grid /></el-icon>
            </div>
            <div class="app-info">
              <div class="app-name">全部接口</div>
              <div class="app-meta">{{ totalApiCount }} 个接口</div>
            </div>
          </div>
          <div
            v-for="app in filteredApps"
            :key="app.id"
            class="app-item"
            :class="{ active: selectedAppId === app.id }"
            @click="selectApp(app)"
          >
            <div class="app-icon">
              <el-icon :size="18"><Folder /></el-icon>
            </div>
            <div class="app-info">
              <div class="app-name">{{ app.appName }}</div>
              <div class="app-meta">{{ app.appCode }} · {{ app.apiCount || 0 }} 个接口</div>
            </div>
            <div class="app-actions">
              <el-button link type="primary" size="small" @click.stop="handleEditApp(app)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button link type="danger" size="small" @click.stop="handleDeleteApp(app)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-if="filteredApps.length === 0" class="app-empty">
            暂无应用
          </div>
        </div>
      </div>
    </Transition>

    <!-- 右侧面板：API 列表 -->
    <div class="api-main">
      <div class="rf-list-header">
        <div class="header-left">
          <el-button v-if="sidebarCollapsed" class="expand-btn" size="small" text circle @click="sidebarCollapsed = false">
            <el-icon><Expand /></el-icon>
          </el-button>
          <div>
            <h1 class="title">{{ currentAppName }}</h1>
            <p class="subtitle">{{ currentAppDesc }}</p>
          </div>
        </div>
        <button v-if="selectedAppId" class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 注册接口
        </button>
      </div>

      <!-- 搜索栏 -->
      <div class="rf-search-bar">
        <div class="search-fields">
          <el-form :model="queryForm" inline>
            <el-form-item label="接口编码">
              <el-input v-model="queryForm.apiCode" placeholder="请输入接口编码" clearable />
            </el-form-item>
            <el-form-item label="接口名称">
              <el-input v-model="queryForm.apiName" placeholder="请输入接口名称" clearable />
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
        <el-table :data="apiList" stripe v-loading="loading" class="rf-data-table" style="width: 100%" empty-text="暂无数据">
          <el-table-column type="index" label="#" width="52" align="center" />
          <el-table-column prop="apiCode" label="接口编码" width="220">
            <template #default="{ row }">
              <span class="rf-code">{{ row.apiCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="apiName" label="接口名称" min-width="160" class-name="cell-wrap" />
          <el-table-column prop="apiType" label="类型" width="90" align="center">
            <template #default="{ row }">
              <span :class="['rf-tag', row.apiType]">
                {{ { proxy: '代理', sql: 'SQL', script: '脚本', data: '数据', plugin: '插件' }[row.apiType] || row.apiType }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="代理接口" min-width="260" class-name="cell-wrap">
            <template #default="{ row }">
              <div class="endpoint-row">
                <span :class="['rf-tag', row.openMethod?.toLowerCase()]">{{ row.openMethod }}</span>
                <span class="rf-code">/open{{ row.openPath || '/' + row.apiCode }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="authType" label="认证方式" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.authType === 'none' ? 'info' : 'warning'" size="small">
                {{ { none: '无', basic: 'Basic', token: 'Token', sign: 'AK/SK', oauth2: 'OAuth2' }[row.authType] || row.authType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-if="selectedAppId" v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
              <el-tag v-else :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="!selectedAppId" label="所属应用" width="140" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.appId" type="primary" size="small">{{ getAppName(row.appId) }}</el-tag>
              <span v-else class="rf-text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="流程触发" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.triggerEnabled === 1" type="success" size="small">已启用</el-tag>
              <el-tag v-else type="info" size="small">未启用</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="selectedAppId" label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <div class="rf-actions">
                <button class="action-btn success" title="调试" @click="handleDebug(row)">
                  <el-icon><Promotion /></el-icon>
                </button>
                <button class="action-btn primary" title="编辑" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
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
            @update:page-size="handlePageChange"
            @update:current-page="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 应用编辑弹窗 -->
    <el-dialog v-model="appDialogVisible" :title="appDialogTitle" width="520px" top="20vh" destroy-on-close :close-on-click-modal="false">
      <el-form ref="appFormRef" :model="appForm" :rules="appFormRules" label-width="90px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="appForm.appCode" placeholder="如 user-center" :disabled="!!appForm.id" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="appForm.appName" placeholder="如 用户中心" />
        </el-form-item>
        <el-form-item label="应用图标" prop="icon">
          <el-input v-model="appForm.icon" placeholder="Element Plus 图标名，如 Folder" />
        </el-form-item>
        <el-form-item label="AppKey">
          <el-input v-model="appForm.appKey" placeholder="应用标识">
            <template #append>
              <el-button @click="generateAppKey">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="AppSecret">
          <el-input v-model="appForm.appSecret" type="password" show-password placeholder="应用密钥">
            <template #append>
              <el-button @click="generateAppSecret">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="appForm.sortNo" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="appForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应用描述" prop="description">
          <el-input v-model="appForm.description" type="textarea" :rows="3" placeholder="请输入应用描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="appSubmitLoading" @click="handleSubmitApp">保存</el-button>
      </template>
    </el-dialog>

    <!-- 注册/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px" top="5vh" destroy-on-close :close-on-click-modal="false" class="edit-dialog">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="所属应用" prop="appId">
                  <el-select v-model="form.appId" placeholder="请选择所属应用" :disabled="!!selectedAppId" clearable style="width: 100%">
                    <el-option v-for="app in appList" :key="app.id" :label="app.appName" :value="app.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接口编码" prop="apiCode">
                  <el-input v-model="form.apiCode" placeholder="如 API_001" :disabled="!!form.id" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="接口名称" prop="apiName">
                  <el-input v-model="form.apiName" placeholder="如 统一认证平台" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接口类型" prop="apiType">
                  <el-select v-model="form.apiType" placeholder="请选择" style="width: 100%">
                    <el-option label="代理接口" value="proxy" />
                    <el-option label="SQL服务" value="sql" />
                    <el-option label="数据服务" value="data" />
                    <el-option label="脚本服务" value="script" />
                    <el-option label="插件接口" value="plugin" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="请求方式" prop="method">
                  <el-select v-model="form.method" placeholder="请选择" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="代理后方式" prop="openMethod">
                  <el-select v-model="form.openMethod" placeholder="请选择" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item v-if="form.apiType === 'sql'" label="SQL 语句" prop="url">
              <el-input v-model="form.url" type="textarea" :rows="4" placeholder="请输入 SQL 语句，如 INSERT INTO..." />
              <div class="sql-safety-hint">
                <el-alert :closable="false" type="warning" show-icon title="SQL 安全限制">
                  <div>仅允许 SELECT/INSERT/UPDATE/DELETE；禁止 DROP/TRUNCATE/ALTER/GRANT 等；UPDATE/DELETE 必须带 WHERE。</div>
                </el-alert>
              </div>
            </el-form-item>
            <el-form-item v-else-if="form.apiType === 'plugin'" label="插件配置" prop="url">
              <el-input v-model="form.url" type="textarea" :rows="4" placeholder='请输入插件配置 JSON，如 {"operation":"upload","bucket":"xxx"}' />
            </el-form-item>
            <el-form-item v-else label="原始请求地址" prop="url">
              <el-input v-model="form.url" placeholder="http(s)://..." />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="代理后路径" prop="openPath">
                  <el-input v-model="form.openPath" placeholder="如 /user/list" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Content-Type">
                  <el-select v-model="form.contentType" placeholder="请选择" style="width: 100%">
                    <el-option label="application/json" value="application/json" />
                    <el-option label="application/x-www-form-urlencoded" value="application/x-www-form-urlencoded" />
                    <el-option label="multipart/form-data" value="multipart/form-data" />
                    <el-option label="application/text" value="application/text" />
                    <el-option label="text/xml" value="text/xml" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="proxy-path-hint">外部调用和调试均使用上述代理后的路径和请求方式</div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="认证方式">
                  <el-select v-model="form.authType" placeholder="请选择" style="width: 100%">
                    <el-option label="无" value="none" />
                    <el-option label="Basic Auth" value="basic" />
                    <el-option label="Token" value="token" />
                    <el-option label="AK/SK 签名" value="sign" />
                    <el-option label="OAuth2" value="oauth2" />
                  </el-select>
                </el-form-item>
                <el-form-item label="IP 白名单">
                  <el-input v-model="form.allowedIps" placeholder="可选，如 10.0.0.0/24,192.168.1.10" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超时(ms)">
                  <el-input-number v-model="form.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="数据源">
                  <el-select v-model="form.dsId" placeholder="SQL类型时选择" clearable style="width: 100%">
                    <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.dsName" :value="ds.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="重试次数">
                  <el-input-number v-model="form.retryTimes" :min="0" :max="5" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="业务成功码">
                  <el-input v-model="form.successCode" placeholder="默认 200，多个用逗号分隔，如 200,0,1" />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 脚本类型配置 -->
            <template v-if="form.apiType === 'script'">
              <el-form-item label="绑定脚本">
                <el-select v-model="form.scriptId" placeholder="请选择要绑定的脚本" clearable style="width: 100%">
                  <el-option v-for="s in scriptOptions" :key="s.id" :label="`${s.scriptName} (${s.scriptCode})`" :value="s.id" />
                </el-select>
              </el-form-item>
            </template>

            <!-- 插件类型配置 -->
            <template v-if="form.apiType === 'plugin'">
              <el-form-item label="插件类型" prop="pluginType">
                <el-select v-model="form.pluginType" placeholder="请选择已加载的插件" clearable style="width: 100%">
                  <el-option v-for="p in pluginOptions" :key="p.pluginType" :label="`${p.pluginName} (${p.pluginType})`" :value="p.pluginType" />
                </el-select>
                <div v-if="pluginOptions.length === 0" style="color: #f56c6c; font-size: 12px; margin-top: 4px;">
                  未检测到已加载的插件，请检查插件管理页面是否已上传并启用
                </div>
              </el-form-item>
            </template>

            <el-form-item v-if="form.apiType === 'proxy'" label="代理设置">
              <el-switch v-model="form.proxyEnabled" :active-value="1" :inactive-value="0" />
              <template v-if="form.proxyEnabled === 1">
                <el-input v-model="form.proxyHost" placeholder="代理主机" style="width: 180px; margin-left: 12px" />
                <el-input-number v-model="form.proxyPort" placeholder="端口" :min="1" :max="65535" style="width: 120px; margin-left: 8px" />
              </template>
            </el-form-item>

            <el-divider content-position="left">流程触发配置</el-divider>
            <el-form-item label="启用流程触发">
              <el-switch v-model="form.triggerEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <template v-if="form.triggerEnabled === 1">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="触发流程">
                    <el-select v-model="form.triggerFlowCode" placeholder="请选择要触发的流程（自动使用最新发布版本）" clearable style="width: 100%">
                      <el-option v-for="flow in flowDefinitionOptions" :key="flow.flowCode" :label="`${flow.flowName} (v${flow.version})`" :value="flow.flowCode" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="业务主键字段">
                    <el-input v-model="form.triggerBizKeyField" placeholder="请求参数中的字段名，如 receiptNo" />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="参数配置" name="params">
          <div class="param-toolbar">
            <el-radio-group v-model="paramTab" size="small">
              <el-radio-button label="header">Header</el-radio-button>
              <el-radio-button label="query">Query</el-radio-button>
              <el-radio-button label="body">Body</el-radio-button>
              <el-radio-button label="response">Response</el-radio-button>
            </el-radio-group>
            <el-button type="primary" size="small" @click="addParam">
              <el-icon><Plus /></el-icon> 添加
            </el-button>
          </div>
          <el-table :data="filteredParams" stripe size="small" border>
            <el-table-column label="参数键" width="180">
              <template #default="{ row }">
                <div class="param-key-cell" :style="{ paddingLeft: (row._level * 20) + 'px' }">
                  <el-input v-model="row.paramKey" size="small" placeholder="key" />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="参数名称" width="140">
              <template #default="{ row }">
                <el-input v-model="row.paramName" size="small" placeholder="名称" />
              </template>
            </el-table-column>
            <el-table-column label="数据类型" width="110">
              <template #default="{ row }">
                <el-select v-model="row.dataType" size="small" style="width: 100%">
                  <el-option label="string" value="string" />
                  <el-option label="int" value="int" />
                  <el-option label="long" value="long" />
                  <el-option label="double" value="double" />
                  <el-option label="boolean" value="boolean" />
                  <el-option label="object" value="object" />
                  <el-option label="array" value="array" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="必填" width="60" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isRequired" :true-label="1" :false-label="0" />
              </template>
            </el-table-column>
            <el-table-column label="默认值" width="140">
              <template #default="{ row }">
                <el-input v-model="row.defaultValue" size="small" placeholder="默认值" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button v-if="row.dataType === 'object' || row.dataType === 'array'" link type="primary" size="small" @click="addChildParam(row)">+子项</el-button>
                <el-button link type="danger" size="small" @click="removeParam(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 调试弹窗 -->
    <el-dialog v-model="debugDialogVisible" title="接口调试" width="700px" destroy-on-close>
      <ApiDebugger :url="debugRow?.url" :method="debugRow?.method" :api-type="debugRow?.apiType" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiCatalogList,
  saveApiCatalog,
  updateApiCatalog,
  deleteApiCatalog,
  getApiParams,
  saveApiParams,
  getApiScriptList,
  getApiPluginList
} from '@/api/apiMgr'
import { getApiAppList, getApiAppListAll, saveApiApp, updateApiApp, deleteApiApp, getApiAppCounts } from '@/api/apiApp'
import { getDatasourceList } from '@/api/datasource'
import { getFlowDefinitionList } from '@/api/workflow'
import ApiDebugger from '@/components/ApiDebugger/index.vue'

/* ========== 应用目录相关 ========== */
const sidebarCollapsed = ref(false)
const appLoading = ref(false)
const appList = ref([])
const appKeyword = ref('')
const selectedAppId = ref(null)
const totalApiCount = ref(0)

const filteredApps = computed(() => {
  if (!appKeyword.value) return appList.value
  const kw = appKeyword.value.toLowerCase()
  return appList.value.filter(app =>
    (app.appName && app.appName.toLowerCase().includes(kw)) ||
    (app.appCode && app.appCode.toLowerCase().includes(kw))
  )
})

const currentAppName = computed(() => {
  if (!selectedAppId.value) return '全部接口'
  const app = appList.value.find(a => a.id === selectedAppId.value)
  return app ? app.appName : '接口列表'
})

const currentAppDesc = computed(() => {
  if (!selectedAppId.value) return '管理系统所有对外暴露的 API 接口'
  const app = appList.value.find(a => a.id === selectedAppId.value)
  return app ? (app.description || '暂无描述') : ''
})

function getAppName(appId) {
  if (!appId) return '-'
  const app = appList.value.find(a => String(a.id) === String(appId))
  return app ? app.appName : '-'
}

async function loadAppList() {
  appLoading.value = true
  try {
    const res = await getApiAppList({ page: 1, size: 999, status: 1 })
    const list = res.list || res.records || res || []
    // 批量获取 API 数量
    const appIds = list.map(a => a.id).filter(Boolean)
    if (appIds.length > 0) {
      try {
        const countRes = await getApiAppCounts(appIds)
        const counts = countRes || {}
        list.forEach(app => {
          app.apiCount = counts[app.id] || 0
        })
      } catch (e) {
        // 忽略统计错误
      }
    }
    appList.value = list
  } finally {
    appLoading.value = false
  }
}

function selectApp(app) {
  selectedAppId.value = app ? app.id : null
  pagination.page = 1
  loadList()
}

/* 应用弹窗 */
const appDialogVisible = ref(false)
const appDialogTitle = ref('新增应用')
const appFormRef = ref(null)
const appSubmitLoading = ref(false)
const appForm = reactive({
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
const appFormRules = {
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
}

function handleAddApp() {
  appDialogTitle.value = '新增应用'
  Object.assign(appForm, {
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
  appDialogVisible.value = true
}

function handleEditApp(app) {
  appDialogTitle.value = '编辑应用'
  Object.assign(appForm, {
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
  appDialogVisible.value = true
}

async function handleDeleteApp(app) {
  try {
    await ElMessageBox.confirm(`确认删除应用「${app.appName}」？`, '删除确认', { type: 'warning' })
    await deleteApiApp(app.id)
    ElMessage.success('删除成功')
    if (selectedAppId.value === app.id) {
      selectedAppId.value = null
    }
    loadAppList()
    loadList()
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
  appForm.appKey = 'ak_' + generateRandomString(16)
}

function generateAppSecret() {
  appForm.appSecret = generateRandomString(32)
}

async function handleSubmitApp() {
  const valid = await appFormRef.value.validate().catch(() => false)
  if (!valid) return
  appSubmitLoading.value = true
  try {
    if (appForm.id) {
      await updateApiApp(appForm)
    } else {
      await saveApiApp(appForm)
    }
    ElMessage.success('保存成功')
    appDialogVisible.value = false
    loadAppList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    appSubmitLoading.value = false
  }
}

/* ========== 接口列表相关（保留原有逻辑） ========== */
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('注册接口')
const formRef = ref(null)
const submitLoading = ref(false)
const activeTab = ref('base')
const paramTab = ref('header')
const debugDialogVisible = ref(false)
const debugRow = ref(null)

const queryForm = reactive({
  apiCode: '',
  apiName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const apiList = ref([])
const datasourceOptions = ref([])
const flowDefinitionOptions = ref([])
const form = reactive({
  id: null,
  appId: null,
  apiCode: '',
  apiName: '',
  apiType: 'proxy',
  method: 'POST',
  url: '',
  openPath: '',
  openMethod: 'POST',
  contentType: 'application/json',
  authType: 'none',
  allowedIps: '',
  dsId: null,
  scriptId: null,
  pluginType: '',
  timeout: 30000,
  retryTimes: 0,
  successCode: '200',
  proxyEnabled: 0,
  proxyHost: '',
  proxyPort: null,
  triggerEnabled: 0,
  triggerFlowId: null,
  triggerFlowCode: '',
  triggerBizKeyField: '',
  status: 0
})

const scriptOptions = ref([])
const pluginOptions = ref([])

const formRules = {
  appId: [{ required: true, message: '请选择所属应用', trigger: 'change' }],
  apiCode: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  apiName: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  apiType: [{ required: true, message: '请选择接口类型', trigger: 'change' }],
  method: [{ required: true, message: '请选择请求方式', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求地址', trigger: 'blur' }],
  openPath: [{ required: true, message: '请输入代理后路径', trigger: 'blur' }],
  openMethod: [{ required: true, message: '请选择代理后请求方式', trigger: 'change' }]
}

const allParams = ref([])

function ensureClientId(param) {
  if (!param.clientId) {
    param.clientId = 'c_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  }
}

function getParamLevel(param) {
  if (!param.parentId || param.parentId === '0' || param.parentId === 0 || param.parentId === 'null') return 0
  const parent = allParams.value.find(p =>
    p.clientId === param.parentId ||
    p.id === param.parentId ||
    String(p.id) === String(param.parentId)
  )
  return parent ? getParamLevel(parent) + 1 : 0
}

const filteredParams = computed(() => {
  const typeParams = allParams.value.filter(p => p.paramType === paramTab.value)
  typeParams.forEach(ensureClientId)
  typeParams.forEach(p => {
    p._level = getParamLevel(p)
  })

  function collectWithChildren(parentId, result) {
    const children = typeParams
      .filter(p => {
        if (!parentId || parentId === '0' || parentId === 0) {
          return !p.parentId || p.parentId === '0' || p.parentId === 0
        }
        return p.parentId === parentId
      })
      .sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
    for (const child of children) {
      result.push(child)
      collectWithChildren(child.clientId, result)
    }
  }

  const result = []
  collectWithChildren(null, result)
  return result
})

function addParam() {
  const newParam = {
    paramType: paramTab.value,
    parentId: '0',
    paramKey: '',
    paramName: '',
    dataType: 'string',
    isRequired: 0,
    defaultValue: '',
    sortNo: allParams.value.length + 1
  }
  ensureClientId(newParam)
  allParams.value.push(newParam)
}

function addChildParam(parent) {
  const newParam = {
    paramType: paramTab.value,
    parentId: parent.clientId,
    paramKey: '',
    paramName: '',
    dataType: 'string',
    isRequired: 0,
    defaultValue: '',
    sortNo: allParams.value.length + 1
  }
  ensureClientId(newParam)
  allParams.value.push(newParam)
}

function removeParam(row) {
  const idsToDelete = new Set()

  function collectIds(target) {
    idsToDelete.add(target.clientId)
    allParams.value.forEach(p => {
      if (p.parentId === target.clientId || p.parentId === target.id || String(p.parentId) === String(target.id)) {
        collectIds(p)
      }
    })
  }

  collectIds(row)
  allParams.value = allParams.value.filter(p => !idsToDelete.has(p.clientId))
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (selectedAppId.value) params.appId = selectedAppId.value
    if (queryForm.apiCode) params.apiCode = queryForm.apiCode
    if (queryForm.apiName) params.apiName = queryForm.apiName
    const res = await getApiCatalogList(params)
    apiList.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
    // 同时更新全部接口计数（用于左侧"全部接口"卡片）
    if (!selectedAppId.value) {
      totalApiCount.value = pagination.total
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadList()
}

function handlePageChange() {
  loadList()
}

function handleReset() {
  queryForm.apiCode = ''
  queryForm.apiName = ''
  pagination.page = 1
  loadList()
}

async function loadDatasourceOptions() {
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceOptions.value = res.list || res.records || res || []
  } catch (e) {
    datasourceOptions.value = []
  }
}

function handleAdd() {
  dialogTitle.value = '注册接口'
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, {
    id: null,
    appId: selectedAppId.value,
    apiCode: '',
    apiName: '',
    apiType: 'proxy',
    method: 'POST',
    url: '',
    openPath: '',
    openMethod: 'POST',
    contentType: 'application/json',
    authType: 'none',
    allowedIps: '',
    dsId: null,
    scriptId: null,
    pluginType: '',
    timeout: 30000,
    retryTimes: 0,
    successCode: '200',
    proxyEnabled: 0,
    proxyHost: '',
    proxyPort: null,
    triggerEnabled: 0,
    triggerFlowId: null,
    triggerFlowCode: '',
    triggerBizKeyField: '',
    status: 0
  })
  allParams.value = []
  dialogVisible.value = true
}

async function loadScriptOptions() {
  try {
    const res = await getApiScriptList({ page: 1, size: 999, status: 1 })
    scriptOptions.value = res.list || res.records || res || []
  } catch (e) {
    scriptOptions.value = []
  }
}

async function loadPluginOptions() {
  try {
    const res = await getApiPluginList()
    pluginOptions.value = res.plugins || []
  } catch (e) {
    pluginOptions.value = []
  }
}

async function handleEdit(row) {
  dialogTitle.value = `编辑接口 - ${row.apiName}`
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, { ...row })
  if (!form.openPath) form.openPath = `/${row.apiCode}`
  if (!form.openMethod) form.openMethod = row.method || 'POST'
  allParams.value = []
  dialogVisible.value = true
  await nextTick()
  try {
    const params = await getApiParams(row.id)
    allParams.value = Array.isArray(params) ? params : []
    allParams.value.forEach(ensureClientId)
    allParams.value.forEach(p => {
      if (p.parentId && p.parentId !== '0' && p.parentId !== 0) {
        const parent = allParams.value.find(pp =>
          pp.id === p.parentId || String(pp.id) === String(p.parentId)
        )
        if (parent) {
          p.parentId = parent.clientId
        }
      }
    })
    if (allParams.value.length > 0) {
      const hasBody = allParams.value.some(p => p.paramType === 'body')
      const hasQuery = allParams.value.some(p => p.paramType === 'query')
      const hasHeader = allParams.value.some(p => p.paramType === 'header')
      const hasResponse = allParams.value.some(p => p.paramType === 'response')
      if (hasBody) paramTab.value = 'body'
      else if (hasQuery) paramTab.value = 'query'
      else if (hasHeader) paramTab.value = 'header'
      else if (hasResponse) paramTab.value = 'response'
    }
  } catch (e) {
    allParams.value = []
  }
}

function validateSqlSafety(sql) {
  if (!sql || !sql.trim()) return { passed: true }
  const statements = sql.split(';').filter(s => s.trim())
  const allowed = ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
  const blacklist = ['DROP', 'TRUNCATE', 'ALTER', 'CREATE', 'RENAME', 'GRANT', 'REVOKE', 'EXEC', 'EXECUTE', 'CALL']
  for (const stmt of statements) {
    // 去除注释
    let clean = stmt.replace(/\/\*[\s\S]*?\*\//g, ' ').replace(/--.*$/gm, ' ')
    // 去除字符串常量
    clean = clean.replace(/'(?:[^']|'')*'/g, "''")
    const first = clean.trim().match(/^\s*(\w+)/)
    if (!first) return { passed: false, message: '无法识别 SQL 语句类型' }
    const type = first[1].toUpperCase()
    if (!allowed.includes(type)) {
      return { passed: false, message: `不允许执行 ${type}，仅允许 SELECT/INSERT/UPDATE/DELETE` }
    }
    const upper = clean.toUpperCase()
    for (const kw of blacklist) {
      if (new RegExp(`\\b${kw}\\b`).test(upper)) {
        return { passed: false, message: `SQL 包含禁止关键字 ${kw}` }
      }
    }
    if ((type === 'UPDATE' || type === 'DELETE') && !/\bWHERE\b/i.test(clean)) {
      return { passed: false, message: 'UPDATE/DELETE 必须包含 WHERE 条件' }
    }
  }
  return { passed: true }
}

async function handleSubmit() {
  if (activeTab.value !== 'base') {
    activeTab.value = 'base'
    await nextTick()
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.openPath && !form.openPath.startsWith('/')) {
    form.openPath = '/' + form.openPath
  }
  if (!form.openPath || form.openPath.trim() === '/' || form.openPath.trim() === '') {
    ElMessage.warning('代理后路径不能为空')
    return
  }
  if (form.apiType === 'sql' && form.url) {
    const sqlCheck = validateSqlSafety(form.url)
    if (!sqlCheck.passed) {
      ElMessage.warning('SQL 校验失败: ' + sqlCheck.message)
      return
    }
  }
  submitLoading.value = true
  try {
    let apiId = form.id
    if (form.id) {
      await updateApiCatalog(form)
    } else {
      const res = await saveApiCatalog(form)
      apiId = res
    }
    const validParams = allParams.value.filter(p => p.paramKey).map(p => ({
      ...p,
      clientId: p.clientId,
      parentClientId: (p.parentId && p.parentId !== '0' && p.parentId !== 0) ? String(p.parentId) : null,
      parentId: 0
    }))
    if (apiId && validParams.length) {
      await saveApiParams(apiId, validParams)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
    // 如果创建了接口且选中了应用，刷新应用列表以更新数量
    if (form.appId) {
      loadAppList()
    }
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除接口「${row.apiName}」？`, '删除确认', { type: 'warning' })
    await deleteApiCatalog(row.id)
    ElMessage.success('删除成功')
    loadList()
    loadAppList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateApiCatalog({ ...row, status: row.status })
    ElMessage.success(`接口已${row.status === 1 ? '启用' : '停用'}`)
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

function handleDebug(row) {
  debugRow.value = { ...row }
  debugRow.value.url = `/api/open${row.openPath || '/' + row.apiCode}`
  debugRow.value.method = row.openMethod || row.method || 'POST'
  debugDialogVisible.value = true
}

async function loadFlowDefinitionOptions() {
  try {
    const res = await getFlowDefinitionList({ page: 1, size: 999, status: 1 })
    flowDefinitionOptions.value = res.list || res.records || res || []
  } catch (e) {
    flowDefinitionOptions.value = []
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

onMounted(() => {
  loadDatasourceOptions()
  loadFlowDefinitionOptions()
  loadScriptOptions()
  loadPluginOptions()
  loadAppList()
  loadTotalApiCount()
  loadList()
})
</script>

<style scoped lang="scss">
.api-mgr-page {
  display: flex;
  height: calc(100vh - 100px);
  gap: 16px;
  position: relative;
}

/* 左侧面板 */
.app-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f0f0f0;

  .sidebar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .sidebar-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.app-search {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.app-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.app-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  position: relative;

  &:hover {
    background: #f5f7fa;

    .app-actions {
      opacity: 1;
    }
  }

  &.active {
    background: #ecf5ff;
    border-left: 3px solid #409eff;

    .app-icon {
      color: #409eff;
    }

    .app-name {
      color: #409eff;
      font-weight: 600;
    }
  }

  &.all-apps {
    .app-icon.all {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: #f0f9ff;
      color: #409eff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.app-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.app-info {
  flex: 1;
  min-width: 0;

  .app-name {
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .app-meta {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.app-actions {
  opacity: 0;
  transition: opacity 0.2s;
  display: flex;
  gap: 2px;
}

.app-empty {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 40px 0;
}

/* 左侧过渡动画 */
.sidebar-enter-active,
.sidebar-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.sidebar-enter-from,
.sidebar-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.sidebar-leave-active {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
}

/* 右侧面板 */
.api-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;

  .rf-list-header {
    flex-shrink: 0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .expand-btn {
        flex-shrink: 0;
      }
    }
  }

  .rf-search-bar {
    flex-shrink: 0;
  }

  .rf-table-card {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .el-table {
      flex: 1;
      overflow: auto;
    }

    .rf-pagination {
      flex-shrink: 0;
      padding-top: 12px;
    }
  }
}

/* 保持原有样式 */
.param-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.endpoint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .rf-code {
    white-space: normal;
    word-break: break-all;
  }

  .endpoint-url {
    font-size: 13px;
    color: var(--rf-text-secondary);
    word-break: break-all;
    &.endpoint-sql {
      color: var(--rf-text-muted);
      font-style: italic;
    }
  }
}

.proxy-path-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--rf-text-muted);
}
</style>
