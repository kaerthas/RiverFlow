<template>
  <div class="rf-list-page">
    <!-- 页面头部 -->
    <div class="rf-list-header">
      <div>
        <h1 class="title">{{ $t('definition.流程定义_300d6075') }}</h1>
        <p class="subtitle">{{ $t('definition.管理和配置业务流程模板支持拖拽编排与可视化设计_300d6075') }}</p>
      </div>
      <button class="btn-primary" @click="handleCreate">
        <el-icon :size="16"><Plus /></el-icon>
        <span>{{ $t('definition.新建流程_c53fe0f6') }}</span>
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="$t('definition.搜索流程名称_a84013dd')"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" :placeholder="$t('definition.全部状态_443483c9')" clearable style="width: 140px" @change="handleSearch">
          <el-option :label="$t('definition.已发布_dca0c13b_1')" :value="1" />
          <el-option :label="$t('definition.草稿_22b4334f_1')" :value="0" />
          <el-option :label="$t('definition.已下线_0a666759_1')" :value="2" />
        </el-select>
        <el-select v-model="searchForm.triggerType" :placeholder="$t('definition.全部触发方式_c2f5f720')" clearable style="width: 150px" @change="handleSearch">
          <el-option :label="$t('definition.手动_2a3e7f5c_1')" value="manual" />
          <el-option :label="$t('definition.定时_72ebfe28_1')" value="cron" />
          <el-option :label="$t('definition.事件_10b2761d_1')" value="event" />
        </el-select>
        <el-select v-model="searchForm.executionMode" :placeholder="$t('definition.全部模式_dc52ed41')" clearable style="width: 130px" @change="handleSearch">
          <el-option :label="$t('definition.异步_8b5a247d_1')" value="ASYNC" />
          <el-option :label="$t('definition.同步_6a620e3c_1')" value="SYNC" />
        </el-select>
        <el-checkbox v-model="searchForm.showAllVersions" @change="handleSearch" style="margin-left: 8px">{{ $t('definition.显示所有版本_fc34e1fd') }}</el-checkbox>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>{{ $t('definition.查询_bee912d7') }}</span>
        </button>
        <button class="btn-reset" @click="handleReset">
          <el-icon><RefreshRight /></el-icon>
          <span>{{ $t('definition.重置_4b9c3271') }}</span>
        </button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :fit="false" max-height="480">
        <el-table-column type="index" label="#" width="52" align="center" />

        <el-table-column prop="flowCode" :label="$t('definition.流程编码_45668968')" width="260">
          <template #default="{ row }">
            <span class="rf-code">{{ row.flowCode }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="flowName" :label="$t('definition.流程名称_794d65af')" min-width="200" show-overflow-tooltip />

        <el-table-column prop="itemCode" :label="$t('definition.绑定事项_f04f3eca')" width="150">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-secondary)">{{ row.itemCode || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="triggerType" :label="$t('definition.触发方式_159dbc2f')" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.triggerType || 'manual']">{{ triggerLabel(row.triggerType) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="executionMode" :label="$t('definition.执行模式_94fa6c8c')" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.executionMode === 'SYNC' ? 'sync' : 'async']">
              {{ executionModeLabel(row.executionMode) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="version" :label="$t('definition.版本_fe2df04a')" width="80" align="center">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-muted)">v{{ row.version }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" :label="$t('definition.状态_3fea7ca7')" width="120" align="center">
          <template #default="{ row }">
            <span :class="['rf-status', statusClass(row.status)]">
              <span class="dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" :label="$t('definition.创建时间_eca37cb0')" width="195">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('definition.操作_2b6bc0f2')" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <el-tooltip :content="$t('definition.设计_b08890a6')" placement="top">
                <button class="action-btn primary" @click="handleDesign(row)">
                  <el-icon><EditPen /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 0" :content="$t('definition.发布_83611abd')" placement="top">
                <button class="action-btn success" @click="handlePublish(row)">
                  <el-icon><Promotion /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 1" :content="$t('definition.下线_4805dd77')" placement="top">
                <button class="action-btn warning" @click="handleOffline(row)">
                  <el-icon><Download /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip :content="$t('definition.创建新版本_217cc9bd_1')" placement="top">
                <button class="action-btn info" @click="handleCopyVersion(row)">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 1 && row.executionMode === 'SYNC'" :content="$t('definition.同步调试_a8c5fe4b')" placement="top">
                <button class="action-btn success" @click="handleSyncDebug(row)">
                  <el-icon><VideoPlay /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip :content="$t('definition.查看历史版本_97dd460e')" placement="top">
                <button class="action-btn" @click="handleViewVersions(row)">
                  <el-icon><Clock /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip :content="$t('definition.编辑_95b351c8')" placement="top">
                <button class="action-btn" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip :content="$t('definition.删除_2f4aaddd')" placement="top">
                <button class="action-btn danger" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态插槽：替换表格默认的暂无数据 -->
        <template #empty>
          <div class="rf-empty">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.2" style="margin-bottom: 16px">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M3 9h18" />
              <path d="M9 21V9" />
            </svg>
            <div class="empty-title">{{ $t('definition.暂无流程定义_8aa8992f') }}</div>
            <div class="empty-desc">点击右上角「新建流程」创建第一个业务流程</div>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" :title="$t('definition.编辑流程_96273602')" width="860px" destroy-on-close class="edit-dialog">
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <el-form-item :label="$t('definition.流程编码_45668968_1')">
          <el-input v-model="editForm.flowCode" disabled />
        </el-form-item>
        <el-form-item :label="$t('definition.流程名称_794d65af_1')">
          <el-input v-model="editForm.flowName" :placeholder="$t('definition.请输入流程名_e9a00996')" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('definition.绑定事项_f04f3eca_1')">
          <el-input v-model="editForm.itemCode" :placeholder="$t('definition.请输入事项编_b332e483')" />
        </el-form-item>
        <el-form-item :label="$t('definition.执行模式_94fa6c8c_1')">
          <el-select v-model="editForm.executionMode" style="width: 100%">
            <el-option :label="$t('definition.异步支持定时_1e58fb26')" value="ASYNC" />
            <el-option :label="$t('definition.同步仅短链路_df7ef374')" value="SYNC" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('definition.触发方式_159dbc2f_1')">
          <el-select v-model="editForm.triggerType" style="width: 100%">
            <el-option :label="$t('definition.手动触发_0cc990ba')" value="manual" />
            <el-option :label="$t('definition.定时触发_16c7773f')" value="cron" />
            <el-option :label="$t('definition.事件触发_79ff3e87')" value="event" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('definition.流程入参_7b2f0dde')">
          <div class="param-table-wrapper">
            <div class="param-toolbar">
              <el-button type="primary" size="small" @click="addFlowParam">
                <el-icon><Plus /></el-icon>{{ $t('definition.添加参数_52288dd0') }}</el-button>
            </div>
            <el-table :data="flowParams" size="small" border style="width: 100%">
              <el-table-column :label="$t('definition.参数键_8b233552')" width="220">
                <template #default="{ row }">
                  <div :class="['param-key-cell', getFlowParamLevel(row) > 0 ? 'has-indent' : '']" :style="{ paddingLeft: (getFlowParamLevel(row) * 28 + 8) + 'px' }">
                    <el-input v-model="row.paramKey" size="small" :placeholder="$t('definition.如_c16060e6')" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.参数名称_5f49be98')" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" :placeholder="$t('definition.名称_d7ec2d3f')" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.数据类型_185f7bf6')" width="100">
                <template #default="{ row }">
                  <el-select v-model="row.dataType" size="small" style="width: 100%">
                    <el-option label="string" value="string" />
                    <el-option label="int" value="int" />
                    <el-option label="double" value="double" />
                    <el-option label="boolean" value="boolean" />
                    <el-option label="object" value="object" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.默认值_225f3ed0')" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.defaultValue" size="small" :placeholder="$t('definition.默认值_225f3ed0_1')" :disabled="row.dataType === 'object'" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.操作_2b6bc0f2_1')" width="120" align="center">
                <template #default="{ row, $index }">
                  <el-button v-if="row.dataType === 'object'" link type="primary" size="small" @click="addChildFlowParam($index)">+子项</el-button>
                  <el-button link type="danger" size="small" @click="removeFlowParam($index)">{{ $t('definition.删除_2f4aaddd_1') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="flowParams.length === 0" class="param-empty">
              暂无参数，点击「添加参数」配置流程默认入参
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('definition.流程出参_a04fd8f5')">
          <div class="param-table-wrapper">
            <div class="param-toolbar">
              <el-button type="primary" size="small" @click="addFlowOutputParam">
                <el-icon><Plus /></el-icon>{{ $t('definition.添加参数_52288dd0_1') }}</el-button>
            </div>
            <el-table :data="flowOutputParams" size="small" border style="width: 100%">
              <el-table-column :label="$t('definition.参数键_8b233552_1')" width="220">
                <template #default="{ row }">
                  <div :class="['param-key-cell', getFlowOutputParamLevel(row) > 0 ? 'has-indent' : '']" :style="{ paddingLeft: (getFlowOutputParamLevel(row) * 28 + 8) + 'px' }">
                    <el-input v-model="row.paramKey" size="small" :placeholder="$t('definition.如_472d2b8a')" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.参数名称_5f49be98_1')" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" :placeholder="$t('definition.名称_d7ec2d3f_1')" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.数据类型_185f7bf6_1')" width="100">
                <template #default="{ row }">
                  <el-select v-model="row.dataType" size="small" style="width: 100%">
                    <el-option label="string" value="string" />
                    <el-option label="int" value="int" />
                    <el-option label="double" value="double" />
                    <el-option label="boolean" value="boolean" />
                    <el-option label="object" value="object" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.默认值_225f3ed0_1')" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.defaultValue" size="small" :placeholder="$t('definition.默认值_225f3ed0_1')" :disabled="row.dataType === 'object'" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('definition.操作_2b6bc0f2_1')" width="120" align="center">
                <template #default="{ row, $index }">
                  <el-button v-if="row.dataType === 'object'" link type="primary" size="small" @click="addChildFlowOutputParam($index)">+子项</el-button>
                  <el-button link type="danger" size="small" @click="removeFlowOutputParam($index)">{{ $t('definition.删除_2f4aaddd_1') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="flowOutputParams.length === 0" class="param-empty">
              暂无参数，点击「添加参数」配置流程输出参数
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">{{ $t('definition.取消_625fb26b') }}</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="editLoading">{{ $t('definition.保存_be5fbbe3') }}</el-button>
      </template>
    </el-dialog>

    <!-- 同步调试弹窗 -->
    <el-dialog v-model="syncDebugVisible" :title="$t('definition.同步流程调试_6fd1c7df')" width="640px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="syncDebugFormRef" :model="syncDebugForm" label-width="100px">
        <el-form-item :label="$t('definition.流程编码_45668968_1')">
          <el-input v-model="syncDebugForm.flowCode" disabled />
        </el-form-item>
        <el-form-item :label="$t('definition.流程名称_794d65af_1')">
          <el-input v-model="syncDebugForm.flowName" disabled />
        </el-form-item>
        <el-form-item :label="$t('definition.业务主键_21cb4583')">
          <el-input v-model="syncDebugForm.businessKey" :placeholder="$t('definition.可选如办件流_cf60ef7d')" />
        </el-form-item>
        <el-form-item :label="$t('definition.超时_a5047dab')">
          <el-input-number v-model="syncDebugForm.timeoutMs" :min="1000" :max="120000" :step="1000" style="width: 160px" />
          <span style="margin-left: 8px; color: var(--rf-text-muted); font-size: 12px">默认 30000ms，最大 120000ms</span>
        </el-form-item>
        <el-form-item :label="$t('definition.上下文变量_50334fc7')">
          <el-input
            v-model="syncDebugForm.variablesJson"
            type="textarea"
            :rows="6"
            :placeholder="$t('definition.请输入格式的_897c6dbc')"
          />
        </el-form-item>
      </el-form>

      <!-- 执行结果 -->
      <div v-if="syncDebugResult !== null" class="sync-result">
        <div class="sync-result-header">
          <span class="sync-result-title">{{ $t('definition.执行结果_adaf94c0') }}</span>
          <el-tag v-if="syncDebugSuccess" type="success" size="small">{{ $t('definition.成功_330363df') }}</el-tag>
          <el-tag v-else type="danger" size="small">{{ $t('definition.失败_acd5cb84') }}</el-tag>
        </div>
        <pre class="sync-result-body">{{ JSON.stringify(syncDebugResult, null, 2) }}</pre>
      </div>

      <template #footer>
        <el-button @click="syncDebugVisible = false">{{ $t('definition.关闭_b15d9127') }}</el-button>
        <el-button type="primary" @click="confirmSyncDebug" :loading="syncDebugLoading">{{ $t('definition.执行_1a6aa24e') }}</el-button>
      </template>
    </el-dialog>

    <!-- 历史版本弹窗 -->
    <el-dialog v-model="versionVisible" :title="`历史版本 - ${versionFlowName}`" width="720px" destroy-on-close>
      <el-table :data="versionData" v-loading="versionLoading" size="small">
        <el-table-column prop="version" :label="$t('definition.版本_fe2df04a_1')" width="80" align="center">
          <template #default="{ row }">
            <span class="rf-mono">v{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flowName" :label="$t('definition.流程名称_794d65af_1')" min-width="180" />
        <el-table-column prop="status" :label="$t('definition.状态_3fea7ca7_1')" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-status', statusClass(row.status)]">
              <span class="dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('definition.创建时间_eca37cb0_1')" width="180">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('definition.操作_2b6bc0f2_1')" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDesign(row)">{{ $t('definition.设计_b08890a6_1') }}</el-button>
            <el-button link type="primary" size="small" @click="handleCopyVersion(row)">{{ $t('definition.复制_79d3abe9') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFlowDefinitionList,
  publishFlowDefinition,
  offlineFlowDefinition,
  deleteFlowDefinition,
  saveFlowDefinition,
  copyFlowDefinition,
  getFlowVersions,
  executeSyncFlow
} from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({ id: null, flowCode: '', flowName: '', itemCode: '', executionMode: 'ASYNC', triggerType: 'manual', inputParams: '', outputParams: '' })

// 流程入参表格数据（与 editForm.inputParams 双向转换）
const flowParams = ref([])

// 流程出参表格数据（与 editForm.outputParams 双向转换）
const flowOutputParams = ref([])

const searchForm = reactive({ keyword: '', status: '', triggerType: '', executionMode: '', showAllVersions: false })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

// 同步调试
const syncDebugVisible = ref(false)
const syncDebugLoading = ref(false)
const syncDebugFormRef = ref(null)
const syncDebugForm = reactive({ flowCode: '', flowName: '', businessKey: '', timeoutMs: 30000, variablesJson: '' })
const syncDebugResult = ref(null)
const syncDebugSuccess = ref(false)

// 历史版本
const versionVisible = ref(false)
const versionLoading = ref(false)
const versionFlowName = ref('')
const versionData = ref([])

function triggerLabel(type) {
  const map = { manual: t('definition.手动_2a3e7f5c'), cron: t('definition.定时_72ebfe28'), event: t('definition.事件_10b2761d') }
  return map[type] || t('definition.手动_2a3e7f5c_1')
}

function executionModeLabel(mode) {
  const map = { ASYNC: t('definition.异步_8b5a247d'), SYNC: t('definition.同步_6a620e3c') }
  return map[mode] || t('definition.异步_8b5a247d_1')
}

function statusLabel(status) {
  const map = { 1: t('definition.已发布_dca0c13b'), 0: t('definition.草稿_22b4334f'), 2: t('definition.已下线_0a666759') }
  return map[status] || t('definition.未知_1622dc9b')
}

function statusClass(status) {
  const map = { 1: 'published', 0: 'draft', 2: 'offline' }
  return map[status] || 'draft'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFlowDefinitionList({
      page: pagination.page,
      size: pagination.size,
      flowName: searchForm.keyword || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      triggerType: searchForm.triggerType || undefined,
      executionMode: searchForm.executionMode || undefined,
      showAllVersions: searchForm.showAllVersions || undefined
    })
    if (res && res.records) {
      tableData.value = res.records
      pagination.total = Number(res.total) || 0
    }
  } catch (e) {
    console.error(t('definition.加载失败_866b795e'), e)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.triggerType = ''
  searchForm.showAllVersions = false
  pagination.page = 1
  handleSearch()
}

function handleCreate() {
  router.push('/workflow/designer')
}

function handleDesign(row) {
  router.push({ path: '/workflow/designer', query: { id: row.id } })
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布流程「${row.flowName}」v${row.version}？发布后该版本将不可修改。`, t('definition.发布确认_9861a6d5'), { type: 'warning' })
    const newId= await publishFlowDefinition(row.id)
    // 如果返回了新的ID，说明创建了新版，跳转过去
    if (newId && newId !== row.id) {
      ElMessage.success(t('definition.已创建新版本_1a0cf537'))
      router.push({ path: '/workflow/designer', query: { id: newId } })
    } else {
      row.status = 1
      ElMessage.success(t('definition.流程发布成功_634bc1ae'))
    }
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('definition.发布失败_bd774adc') + (e.message || e))
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线流程「${row.flowName}」v${row.version}？`, t('definition.下线确认_8d8d8e6d'), { type: 'warning' })
    await offlineFlowDefinition(row.id)
    row.status = 2
    ElMessage.success(t('definition.流程已下线_058d3d10'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('definition.下线失败_77988ef3') + (e.message || e))
  }
}

async function handleCopyVersion(row) {
  try {
    await ElMessageBox.confirm(`基于「${row.flowName}」v${row.version} 创建新版本？`, t('definition.创建新版本_217cc9bd'), { type: 'info' })
    const newId = await copyFlowDefinition(row.id)
    ElMessage.success(t('definition.新版本创建成_0b09427e'))
    router.push({ path: '/workflow/designer', query: { id: newId } })
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('definition.创建失败_a2bddca3') + (e.message || e))
  }
}

async function handleViewVersions(row) {
  versionFlowName.value = row.flowName
  versionVisible.value = true
  versionLoading.value = true
  try {
    const res = await getFlowVersions(row.flowCode)
    versionData.value = res || []
  } catch (e) {
    ElMessage.error(t('definition.加载历史版本_b9b487c7'))
  } finally {
    versionLoading.value = false
  }
}

function handleEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    flowCode: row.flowCode,
    flowName: row.flowName,
    itemCode: row.itemCode,
    executionMode: row.executionMode || 'ASYNC',
    triggerType: row.triggerType || 'manual',
    version: row.version,
    inputParams: row.inputParams || '',
    outputParams: row.outputParams || ''
  })
  flowParams.value = parseInputParamsToTable(row.inputParams)
  flowOutputParams.value = parseInputParamsToTable(row.outputParams)
  editVisible.value = true
}

function convertParamValue(val, type) {
  if (type === 'int') return parseInt(val, 10) || 0
  if (type === 'double') return parseFloat(val) || 0
  if (type === 'boolean') return val === 'true' || val === '1' || val === true
  if (type === 'object') return {}
  return val
}

function buildObjectValue(params, parentIdx) {
  const obj = {}
  params.forEach((p, idx) => {
    if (p.parentIndex !== parentIdx) return
    if (!p.paramKey || !p.paramKey.trim()) return
    if (p.dataType === 'object') {
      obj[p.paramKey] = buildObjectValue(params, idx)
    } else {
      obj[p.paramKey] = convertParamValue(p.defaultValue, p.dataType)
    }
  })
  return obj
}

function unflattenParams(params) {
  const result = {}
  params.forEach((p, idx) => {
    if (p.parentIndex !== undefined && p.parentIndex !== null) return
    if (!p.paramKey || !p.paramKey.trim()) return
    if (p.dataType === 'object') {
      result[p.paramKey] = buildObjectValue(params, idx)
    } else {
      result[p.paramKey] = convertParamValue(p.defaultValue, p.dataType)
    }
  })
  return result
}

function flattenObjectToTable(obj, parentIndex = null, result = []) {
  if (obj === null || typeof obj !== 'object' || Array.isArray(obj)) return result
  for (const [key, value] of Object.entries(obj)) {
    const currentIndex = result.length
    if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
      result.push({ paramKey: key, paramName: '', dataType: 'object', defaultValue: '', parentIndex })
      flattenObjectToTable(value, currentIndex, result)
    } else {
      let dataType = 'string'
      if (typeof value === 'number') dataType = Number.isInteger(value) ? 'int' : 'double'
      else if (typeof value === 'boolean') dataType = 'boolean'
      result.push({ paramKey: key, paramName: '', dataType, defaultValue: String(value), parentIndex })
    }
  }
  return result
}

function parseInputParamsToTable(inputParams) {
  if (!inputParams) return []
  try {
    const obj = JSON.parse(inputParams)
    if (typeof obj !== 'object' || obj === null || Array.isArray(obj)) return []
    return flattenObjectToTable(obj)
  } catch (e) {
    return []
  }
}

function addFlowParam() {
  flowParams.value.push({
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: undefined
  })
}

function getFlowParamLevel(row) {
  let level = 0
  let current = row
  while (current && current.parentIndex !== undefined && current.parentIndex !== null) {
    const parent = flowParams.value[current.parentIndex]
    if (!parent) break
    level++
    current = parent
  }
  return level
}

function addChildFlowParam(parentIndex) {
  const parent = flowParams.value[parentIndex]
  if (!parent) return
  flowParams.value.splice(parentIndex + 1, 0, {
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: parentIndex
  })
}

function removeFlowParam(index) {
  const target = flowParams.value[index]
  if (!target) return
  // 收集所有要删除的索引（自身 + 所有后代）
  const toDelete = new Set([index])
  function collect(parentIdx) {
    flowParams.value.forEach((p, idx) => {
      if (toDelete.has(idx)) return
      if (p.parentIndex === parentIdx) {
        toDelete.add(idx)
        collect(idx)
      }
    })
  }
  collect(index)
  // 按从大到小排序，依次删除
  const sorted = Array.from(toDelete).sort((a, b) => b - a)
  sorted.forEach(idx => {
    flowParams.value.splice(idx, 1)
  })
  // 更新幸存者的 parentIndex
  flowParams.value.forEach(p => {
    if (p.parentIndex === undefined || p.parentIndex === null) return
    let offset = 0
    sorted.forEach(d => {
      if (d < p.parentIndex) offset++
    })
    p.parentIndex -= offset
  })
}

// ========== 流程出参相关方法 ==========
function addFlowOutputParam() {
  flowOutputParams.value.push({
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: undefined
  })
}

function getFlowOutputParamLevel(row) {
  let level = 0
  let current = row
  while (current && current.parentIndex !== undefined && current.parentIndex !== null) {
    const parent = flowOutputParams.value[current.parentIndex]
    if (!parent) break
    level++
    current = parent
  }
  return level
}

function addChildFlowOutputParam(parentIndex) {
  const parent = flowOutputParams.value[parentIndex]
  if (!parent) return
  flowOutputParams.value.splice(parentIndex + 1, 0, {
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: parentIndex
  })
}

function removeFlowOutputParam(index) {
  const target = flowOutputParams.value[index]
  if (!target) return
  const toDelete = new Set([index])
  function collect(parentIdx) {
    flowOutputParams.value.forEach((p, idx) => {
      if (toDelete.has(idx)) return
      if (p.parentIndex === parentIdx) {
        toDelete.add(idx)
        collect(idx)
      }
    })
  }
  collect(index)
  const sorted = Array.from(toDelete).sort((a, b) => b - a)
  sorted.forEach(idx => {
    flowOutputParams.value.splice(idx, 1)
  })
  flowOutputParams.value.forEach(p => {
    if (p.parentIndex === undefined || p.parentIndex === null) return
    let offset = 0
    sorted.forEach(d => {
      if (d < p.parentIndex) offset++
    })
    p.parentIndex -= offset
  })
}

async function confirmEdit() {
  editLoading.value = true
  try {
    // 将参数表格组装为 JSON
    const built = unflattenParams(flowParams.value)
    const inputParamsJson = Object.keys(built).length > 0 ? JSON.stringify(built) : ''
    editForm.inputParams = inputParamsJson

    const outputBuilt = unflattenParams(flowOutputParams.value)
    const outputParamsJson = Object.keys(outputBuilt).length > 0 ? JSON.stringify(outputBuilt) : ''
    editForm.outputParams = outputParamsJson

    await saveFlowDefinition({
      id: editForm.id,
      flowName: editForm.flowName,
      itemCode: editForm.itemCode,
      executionMode: editForm.executionMode,
      triggerType: editForm.triggerType,
      version: editForm.version,
      inputParams: editForm.inputParams || undefined,
      outputParams: editForm.outputParams || undefined
    })
    ElMessage.success(t('definition.保存成功_3b108349'))
    editVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error(t('definition.保存失败_40f90217') + (e.message || e))
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除流程「${row.flowName}」v${row.version}？删除后不可恢复。`, t('definition.删除确认_50eaf94d'), { type: 'warning' })
    await deleteFlowDefinition(row.id)
    ElMessage.success(t('definition.删除成功_0007d170'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('definition.删除失败_ad23f072') + (e.message || e))
  }
}

function handleSyncDebug(row) {
  // 如果流程定义了默认入参，格式化后带入调试窗口
  let defaultVars = ''
  if (row.inputParams) {
    try {
      defaultVars = JSON.stringify(JSON.parse(row.inputParams), null, 2)
    } catch (e) {
      defaultVars = row.inputParams
    }
  }
  Object.assign(syncDebugForm, {
    flowCode: row.flowCode,
    flowName: row.flowName,
    businessKey: '',
    timeoutMs: 30000,
    variablesJson: defaultVars
  })
  syncDebugResult.value = null
  syncDebugSuccess.value = false
  syncDebugVisible.value = true
}

async function confirmSyncDebug() {
  let variables = null
  if (syncDebugForm.variablesJson && syncDebugForm.variablesJson.trim()) {
    try {
      variables = JSON.parse(syncDebugForm.variablesJson.trim())
      if (typeof variables !== 'object' || variables === null || Array.isArray(variables)) {
        ElMessage.warning(t('definition.上下文变量必_31fffc87'))
        return
      }
    } catch (e) {
      ElMessage.warning(t('definition.上下文变量格_675893b7') + e.message)
      return
    }
  }

  syncDebugLoading.value = true
  syncDebugResult.value = null
  try {
    const res = await executeSyncFlow({
      flowCode: syncDebugForm.flowCode,
      businessKey: syncDebugForm.businessKey || undefined,
      variables: variables || undefined,
      timeoutMs: syncDebugForm.timeoutMs
    })
    syncDebugResult.value = res
    syncDebugSuccess.value = true
    ElMessage.success(t('definition.同步执行成功_1f5983d2'))
  } catch (e) {
    syncDebugResult.value = { error: e.message || t('definition.执行失败_1c83d797') }
    syncDebugSuccess.value = false
  } finally {
    syncDebugLoading.value = false
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.edit-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 12px;
    margin-right: 0;
    border-bottom: 1px solid #f1f5f9;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 700;
      color: var(--rf-text-main);
      letter-spacing: -0.01em;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 24px 20px;
    border-top: 1px solid #f1f5f9;
  }
}

.edit-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: var(--rf-text-secondary);
  }
}

// 空状态插槽样式
.rf-data-table {
  :deep(.el-table__empty-block) {
    // 去掉全局样式带来的虚线圆圈伪元素
    &::before {
      display: none !important;
    }
  }
}

.param-table-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}
.param-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
.param-empty {
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
  padding: 16px 0;
}
.param-key-cell {
  position: relative;
}
.param-key-cell.has-indent {
  border-left: 2px solid #94a3b8;
  margin-left: 4px;
  padding-left: 10px !important;
}

.rf-actions {
  .action-btn.info {
    color: #3b82f6;
    &:hover { background: #dbeafe; }
  }
}

.sync-result {
  margin-top: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;

  .sync-result-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;

    .sync-result-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--rf-text-main);
    }
  }

  .sync-result-body {
    margin: 0;
    padding: 14px;
    max-height: 320px;
    overflow: auto;
    font-size: 12px;
    line-height: 1.6;
    color: var(--rf-text-secondary);
    background: #ffffff;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  }
}
</style>
