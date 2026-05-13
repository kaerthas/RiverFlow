<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Grid /></el-icon>
      动态表设计器
    </div>
    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon> 新建表</el-button>
      </div>
      <el-table :data="tableList" stripe size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="tableCode" label="表编码" width="160" />
        <el-table-column prop="tableName" label="表名称" min-width="180" />
        <el-table-column prop="dsName" label="所属数据源" width="140" />
        <el-table-column prop="columnCount" label="字段数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">已生成</el-tag>
            <el-tag v-else size="small">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDesign(row)">设计表</el-button>
            <el-button link type="primary" size="small" @click="handleGenApi(row)">生成API</el-button>
            <el-button link type="primary" size="small" @click="handlePreviewData(row)">数据</el-button>
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

const tableList = ref([
  { id: 1, tableCode: 't_business_info', tableName: '业务申办信息表', dsName: '主库(mysql)', columnCount: 24, status: 1, createTime: '2024-05-10 09:30:00' },
  { id: 2, tableCode: 't_material_info', tableName: '申请材料信息表', dsName: '主库(mysql)', columnCount: 12, status: 1, createTime: '2024-05-08 14:20:00' },
  { id: 3, tableCode: 't_cremation_info', tableName: '火化信息登记表', dsName: '主库(mysql)', columnCount: 18, status: 0, createTime: '2024-05-12 11:00:00' }
])

function handleCreate() { ElMessage.info('新建表弹窗待实现') }
function handleDesign(row) { ElMessage.info(`设计表: ${row.tableName}`) }
function handleGenApi(row) { ElMessage.success(`已为「${row.tableName}」生成CRUD接口`) }
function handlePreviewData(row) { ElMessage.info(`查看表数据: ${row.tableName}`) }
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除表「${row.tableName}」？`, '删除确认', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped lang="scss">
.toolbar { margin-bottom: 16px; }
</style>
