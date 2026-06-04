<template>
  <div class="plugin-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>插件管理</span>
          <div class="header-actions">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :show-file-list="false"
              accept=".jar"
            >
              <el-button type="primary" icon="Upload">
                上传插件
              </el-button>
            </el-upload>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="插件名称">
          <el-input v-model="searchForm.pluginName" placeholder="请输入插件名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="已启用" value="enabled" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPlugins">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="plugins" v-loading="loading" border stripe>
        <el-table-column prop="pluginName" label="插件名称" width="180" />
        <el-table-column prop="pluginType" label="类型标识" width="120" />
        <el-table-column prop="pluginScope" label="作用域" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.pluginScope === 'both' ? 'success' : row.pluginScope === 'api' ? 'primary' : 'info'">
              {{ { node: '节点', api: '接口', both: '两者' }[row.pluginScope] || row.pluginScope || '节点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
              {{ row.status === 'enabled' ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loaded" label="加载状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.loaded ? 'success' : 'warning'">
              {{ row.loaded ? '已加载' : '未加载' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'disabled'" 
              type="success" 
              size="small" 
              @click="enablePlugin(row)"
            >
              启用
            </el-button>
            <el-button 
              v-else 
              type="warning" 
              size="small" 
              @click="disablePlugin(row)"
            >
              禁用
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="reloadPlugin(row)"
            >
              重载
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deletePlugin(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPlugins"
        @current-change="loadPlugins"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const uploadUrl = computed(() => {
  return '/api/plugin/upload'
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': 'Bearer ' + userStore.token
  }
})

const loading = ref(false)
const plugins = ref([])
const categories = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = ref({
  pluginName: '',
  category: '',
  status: ''
})

const loadPlugins = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/plugin/list',
      method: 'get',
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        ...searchForm.value
      }
    })
    
    plugins.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载插件列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await request({
      url: '/plugin/categories',
      method: 'get'
    })
    
    categories.value = res || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const beforeUpload = (file) => {
  const isJar = file.name.endsWith('.jar')
  if (!isJar) {
    ElMessage.error('只能上传JAR文件')
    return false
  }
  
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('插件上传成功')
    loadPlugins()
    loadCategories()
  } else {
    ElMessage.error(response.msg || '插件上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('插件上传失败')
}

const enablePlugin = async (row) => {
  try {
    await ElMessageBox.confirm('确定要启用该插件吗？', '提示', {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/enable/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success('插件启用成功')
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('插件启用失败')
    }
  }
}

const disablePlugin = async (row) => {
  try {
    await ElMessageBox.confirm('确定要禁用该插件吗？', '提示', {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/disable/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success('插件已禁用')
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('插件禁用失败')
    }
  }
}

const reloadPlugin = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重新加载该插件吗？', '提示', {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/reload/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success('插件重新加载成功')
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('插件重新加载失败')
    }
  }
}

const deletePlugin = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该插件吗？',
      '提示',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    await request({
      url: `/plugin/delete/${row.id}`,
      method: 'delete'
    })
    
    ElMessage.success('插件删除成功')
    loadPlugins()
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('插件删除失败')
    }
  }
}

const resetSearch = () => {
  searchForm.value = {
    pluginName: '',
    category: '',
    status: ''
  }
  pageNum.value = 1
  loadPlugins()
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

onMounted(() => {
  loadPlugins()
  loadCategories()
})
</script>

<style scoped lang="scss">
.plugin-manager {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
