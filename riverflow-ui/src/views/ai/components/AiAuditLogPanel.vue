<template>
  <div class="ai-audit-log-page">
    <div class="audit-header">
      <div class="header-title">{{ $t('aiAuditLogPanel._a_i调用审计日志_21') }}</div>
    </div>

    <div class="audit-query">
      <el-form :inline="true" size="small">
        <el-form-item :label="$t('aiAuditLogPanel.场景_2')">
          <el-select v-model="query.scene" clearable :placeholder="$t('aiAuditLogPanel.全部_3')" style="width: 120px">
            <el-option :label="$t('aiAuditLogPanel.流程生成_4')" value="flow" />
            <el-option :label="$t('aiAuditLogPanel.条件生成_5')" value="condition" />
            <el-option :label="$t('aiAuditLogPanel.映射推荐_6')" value="mapping" />
            <el-option :label="$t('aiAuditLogPanel.脚本生成_7')" value="script" />
            <el-option :label="$t('aiAuditLogPanel.接口文档解析_8')" value="api-doc" />
            <el-option :label="$t('aiAuditLogPanel.对话_9')" value="chat" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('aiAuditLogPanel.用户_i_d_10')">
          <el-input v-model="query.userId" :placeholder="$t('aiAuditLogPanel.用户_i_d_11')" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item :label="$t('aiAuditLogPanel.状态_12')">
          <el-select v-model="query.success" clearable :placeholder="$t('aiAuditLogPanel.全部_13')" style="width: 100px">
            <el-option :label="$t('aiAuditLogPanel.成功_14')" :value="1" />
            <el-option :label="$t('aiAuditLogPanel.失败_15')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">{{ $t('aiAuditLogPanel.查询_22') }}</el-button>
          <el-button @click="resetQuery">{{ $t('aiAuditLogPanel.重置_23') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" size="small" border height="calc(100vh - 340px)">
      <el-table-column prop="scene" :label="$t('aiAuditLogPanel.场景_16')" width="100" />
      <el-table-column prop="userId" :label="$t('aiAuditLogPanel.用户_17')" width="100" />
      <el-table-column prop="provider" label="Provider" width="100" />
      <el-table-column prop="model" :label="$t('aiAuditLogPanel.模型_18')" width="120" />
      <el-table-column prop="totalTokens" label="Token" width="80" />
      <el-table-column prop="responseTimeMs" :label="$t('aiAuditLogPanel.耗时ms_19')" width="90" />
      <el-table-column prop="success" :label="$t('aiAuditLogPanel.状态_20')" width="70">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
            {{ row.success === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('aiAuditLogPanel.时间_19fcb9eb')" width="160" />
      <el-table-column prop="inputSummary" :label="$t('aiAuditLogPanel.输入摘要_33b75129')" show-overflow-tooltip />
      <el-table-column prop="outputSummary" :label="$t('aiAuditLogPanel.输出摘要_c2de5f5a')" show-overflow-tooltip />
    </el-table>

    <div class="audit-pagination">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  scene: '',
  userId: '',
  success: null,
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  loadData()
})

function resetQuery() {
  query.scene = ''
  query.userId = ''
  query.success = null
  query.pageNum = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await request.get('/ai/audit/list', { params })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    ElMessage.error(err.message || t('aiAuditLogPanel.查询审计日志失败_1'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-audit-log-page {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.audit-header {
  margin-bottom: 16px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.audit-query {
  margin-bottom: 16px;
}
.audit-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
