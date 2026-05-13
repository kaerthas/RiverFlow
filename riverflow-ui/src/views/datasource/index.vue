<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><DataAnalysis /></el-icon>
      数据源管理
    </div>
    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增数据源</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="ds in datasourceList" :key="ds.id">
          <div class="ds-card" :class="{ active: ds.status === 1 }">
            <div class="ds-header">
              <el-icon :size="32" :color="ds.status === 1 ? '#1677FF' : '#8C8C8C'"><Coin /></el-icon>
              <div class="ds-status">
                <el-tag v-if="ds.status === 1" type="success" size="small">在线</el-tag>
                <el-tag v-else type="info" size="small">离线</el-tag>
              </div>
            </div>
            <h4 class="ds-name">{{ ds.dsName }}</h4>
            <p class="ds-code">{{ ds.dsCode }} | {{ ds.dbType }}</p>
            <p class="ds-url" :title="ds.url">{{ ds.url }}</p>
            <div class="ds-actions">
              <el-button link type="primary" size="small" @click="handleTest(ds)">测试连接</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(ds)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(ds)">删除</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const datasourceList = ref([
  { id: 1, dsCode: 'master', dsName: '主库(MySQL)', dbType: 'mysql', url: 'jdbc:mysql://127.0.0.1:3306/riverflow', status: 1 },
  { id: 2, dsCode: 'biz_oracle', dsName: '业务库(Oracle)', dbType: 'oracle', url: 'jdbc:oracle:thin:@172.18.10.205:1521/orcl', status: 1 },
  { id: 3, dsCode: 'archive_pg', dsName: '归档库(PostgreSQL)', dbType: 'postgresql', url: 'jdbc:postgresql://127.0.0.1:5432/archive', status: 0 }
])

function handleAdd() { ElMessage.info('新增数据源弹窗待实现') }
function handleEdit(ds) { ElMessage.info(`编辑数据源: ${ds.dsName}`) }
function handleDelete(ds) {
  ElMessageBox.confirm(`确认删除数据源「${ds.dsName}」？`, '删除确认', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
  })
}
function handleTest(ds) {
  ElMessage.success(`数据源「${ds.dsName}」连接成功`)
}
</script>

<style scoped lang="scss">
.toolbar { margin-bottom: 16px; }

.ds-card {
  background: #FFFFFF;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #F0F0F0;
  transition: all 0.3s;

  &:hover, &.active {
    border-color: #1677FF;
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.08);
  }

  .ds-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .ds-name {
    margin: 0 0 4px;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
  }

  .ds-code {
    margin: 0 0 8px;
    font-size: 13px;
    color: #8C8C8C;
  }

  .ds-url {
    margin: 0 0 16px;
    font-size: 12px;
    color: #BFBFBF;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .ds-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
