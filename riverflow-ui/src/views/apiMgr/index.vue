<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Link /></el-icon>
      接口注册与调试
    </div>
    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 注册接口</el-button>
      </div>
      <el-table :data="apiList" stripe size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="apiCode" label="接口编码" width="140" />
        <el-table-column prop="apiName" label="接口名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.method === 'GET' ? 'success' : 'primary'" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apiType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.apiType === 'proxy'" size="small">代理</el-tag>
            <el-tag v-else-if="row.apiType === 'sql'" type="warning" size="small">SQL</el-tag>
            <el-tag v-else type="success" size="small">数据</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDebug(row)">调试</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const apiList = ref([
  { id: 1, apiCode: 'API_001', apiName: '省里统一认证平台', method: 'POST', apiType: 'proxy', url: 'https://auth.xxx.gov.cn/token', status: 1 },
  { id: 2, apiCode: 'API_002', apiName: '协同调度中心', method: 'POST', apiType: 'proxy', url: 'https://ddpt.xxx.gov.cn/api/dispatch', status: 1 },
  { id: 3, apiCode: 'API_003', apiName: '中残申请接口', method: 'POST', apiType: 'proxy', url: 'https://cjr.xxx.gov.cn/api/apply', status: 1 },
  { id: 4, apiCode: 'SQL_001', apiName: '查询办件列表', method: 'GET', apiType: 'sql', url: '-', status: 1 }
])

function handleAdd() { ElMessage.info('注册接口弹窗待实现') }
function handleEdit(row) { ElMessage.info(`编辑接口: ${row.apiName}`) }
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除接口「${row.apiName}」？`, '删除确认', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
  })
}
function handleDebug(row) { ElMessage.info(`调试接口: ${row.apiName}`) }
function handleStatusChange(row) { ElMessage.success(`接口已${row.status === 1 ? '启用' : '停用'}`) }
</script>

<style scoped lang="scss">
.toolbar { margin-bottom: 16px; }
</style>
