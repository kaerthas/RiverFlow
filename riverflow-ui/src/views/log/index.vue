<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">操作日志</h1>
        <p class="subtitle">查看系统操作记录与审计信息</p>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="模块">
            <el-input v-model="queryForm.module" placeholder="模块名称" clearable />
          </el-form-item>
          <el-form-item label="操作">
            <el-input v-model="queryForm.operation" placeholder="操作描述" clearable />
          </el-form-item>
          <el-form-item label="操作用户">
            <el-input v-model="queryForm.username" placeholder="用户名" clearable />
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
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="150" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center" />
        <el-table-column prop="requestUrl" label="请求 URL" min-width="220" show-overflow-tooltip />
        <el-table-column prop="responseCode" label="响应状态" width="100" align="center" />
        <el-table-column prop="executeTime" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>

      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:current-page="handleSearch"
          @update:page-size="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listOperationLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref([])

const queryForm = reactive({
  module: '',
  operation: '',
  username: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  handleSearch()
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await listOperationLogs({
      page: pagination.page,
      size: pagination.size,
      module: queryForm.module,
      operation: queryForm.operation,
      username: queryForm.username
    })
    tableData.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    // request.js 已统一提示错误，此处无需重复
    console.error('加载操作日志失败', e)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.module = ''
  queryForm.operation = ''
  queryForm.username = ''
  pagination.page = 1
  handleSearch()
}
</script>

<style scoped>
/* 无额外 scoped 样式，复用 rf-list-page 全局样式 */
</style>
