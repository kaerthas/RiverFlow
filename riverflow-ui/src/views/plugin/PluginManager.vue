<template>
  <div class="plugin-manager">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('pluginManager.插件管理_f20cdea7') }}</span>
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
              <el-button type="primary" icon="Upload">{{ $t('pluginManager.上传插件_214cf777') }}</el-button>
            </el-upload>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('pluginManager.插件名称_ce411cbe')">
          <el-input v-model="searchForm.pluginName" :placeholder="$t('pluginManager.请输入插件名_c2196c87')" clearable />
        </el-form-item>
        <el-form-item :label="$t('pluginManager.分类_d0771a42')">
          <el-select v-model="searchForm.category" :placeholder="$t('pluginManager.请选择分类_8bb820b8')" clearable>
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('pluginManager.状态_3fea7ca7')">
          <el-select v-model="searchForm.status" :placeholder="$t('pluginManager.请选择状态_e1c965ef')" clearable>
            <el-option :label="$t('pluginManager.已启用_53ace430')" value="enabled" />
            <el-option :label="$t('pluginManager.已禁用_1c1ed981')" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPlugins">{{ $t('pluginManager.查询_bee912d7') }}</el-button>
          <el-button @click="resetSearch">{{ $t('pluginManager.重置_4b9c3271') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="plugins" v-loading="loading" border stripe>
        <el-table-column prop="pluginName" :label="$t('pluginManager.插件名称_ce411cbe_1')" width="180" />
        <el-table-column prop="pluginType" :label="$t('pluginManager.类型标识_5613a66e')" width="120" />
        <el-table-column prop="pluginScope" :label="$t('pluginManager.作用域_4705b884')" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.pluginScope === 'both' ? 'success' : row.pluginScope === 'api' ? 'primary' : 'info'">
              {{ { node: '节点', api: '接口', both: '两者' }[row.pluginScope] || row.pluginScope || '节点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" :label="$t('pluginManager.分类_d0771a42_1')" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('pluginManager.描述_3bdd08ad')" show-overflow-tooltip />
        <el-table-column prop="fileSize" :label="$t('pluginManager.文件大小_396b7d3f')" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('pluginManager.状态_3fea7ca7_1')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
              {{ row.status === 'enabled' ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loaded" :label="$t('pluginManager.加载状态_0915992e')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.loaded ? 'success' : 'warning'">
              {{ row.loaded ? '已加载' : '未加载' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('pluginManager.上传时间_cae25527')" width="180" />
        <el-table-column :label="$t('pluginManager.操作_2b6bc0f2')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'disabled'" 
              type="success" 
              size="small" 
              @click="enablePlugin(row)"
            >{{ $t('pluginManager.启用_7854b52a') }}</el-button>
            <el-button 
              v-else 
              type="warning" 
              size="small" 
              @click="disablePlugin(row)"
            >{{ $t('pluginManager.禁用_710ad08b') }}</el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="reloadPlugin(row)"
            >{{ $t('pluginManager.重载_aaeb5463') }}</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deletePlugin(row)"
            >{{ $t('pluginManager.删除_2f4aaddd') }}</el-button>
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
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
    ElMessage.error(t('pluginManager.加载插件列表_bf0bcfe9'))
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
    console.error(t('pluginManager.加载分类失败_87b0ac09'), error)
  }
}

const beforeUpload = (file) => {
  const isJar = file.name.endsWith('.jar')
  if (!isJar) {
    ElMessage.error(t('pluginManager.只能上传文件_30a533ea'))
    return false
  }
  
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error(t('pluginManager.文件大小不能_b90f5a36'))
    return false
  }
  
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success(t('pluginManager.插件上传成功_b7140c04'))
    loadPlugins()
    loadCategories()
  } else {
    ElMessage.error(response.msg || t('pluginManager.插件上传失败_d2578c40'))
  }
}

const handleUploadError = () => {
  ElMessage.error(t('pluginManager.插件上传失败_d2578c40_1'))
}

const enablePlugin = async (row) => {
  try {
    await ElMessageBox.confirm(t('pluginManager.确定要启用该_26f14467'), t('pluginManager.提示_02d9819d'), {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/enable/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success(t('pluginManager.插件启用成功_605a9317'))
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('pluginManager.插件启用失败_d932aa57'))
    }
  }
}

const disablePlugin = async (row) => {
  try {
    await ElMessageBox.confirm(t('pluginManager.确定要禁用该_8f903af0'), t('pluginManager.提示_02d9819d_1'), {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/disable/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success(t('pluginManager.插件已禁用_c0f58904'))
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('pluginManager.插件禁用失败_5809e95b'))
    }
  }
}

const reloadPlugin = async (row) => {
  try {
    await ElMessageBox.confirm(t('pluginManager.确定要重新加_c4deae67'), t('pluginManager.提示_02d9819d_1'), {
      type: 'warning'
    })
    
    await request({
      url: `/plugin/reload/${row.id}`,
      method: 'post'
    })
    
    ElMessage.success(t('pluginManager.插件重新加载_d6993121'))
    loadPlugins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('pluginManager.插件重新加载_b8a14a16'))
    }
  }
}

const deletePlugin = async (row) => {
  try {
    await ElMessageBox.confirm(
      t('pluginManager.确定要删除该_17e0abc7'),
      t('pluginManager.提示_02d9819d_1'),
      {
        type: 'warning',
        confirmButtonText: t('pluginManager.确定_38cf16f2'),
        cancelButtonText: t('pluginManager.取消_625fb26b')
      }
    )
    
    await request({
      url: `/plugin/delete/${row.id}`,
      method: 'delete'
    })
    
    ElMessage.success(t('pluginManager.插件删除成功_64c09e31'))
    loadPlugins()
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('pluginManager.插件删除失败_dac0b403'))
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
