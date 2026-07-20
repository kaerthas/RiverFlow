<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">{{ $t('pluginManager.插件管理_f20cdea7') }}</h1>
        <p class="subtitle">管理流程节点与接口复用的插件</p>
      </div>
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
          <button class="btn-primary">
            <el-icon><Upload /></el-icon>{{ $t('pluginManager.上传插件_214cf777') }}
          </button>
        </el-upload>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="searchForm" inline>
          <el-form-item :label="$t('pluginManager.插件名称_ce411cbe')">
            <el-input v-model="searchForm.pluginName" :placeholder="$t('pluginManager.请输入插件名_c2196c87')" clearable />
          </el-form-item>
          <el-form-item :label="$t('pluginManager.分类_d0771a42')">
            <el-select v-model="searchForm.category" :placeholder="$t('pluginManager.请选择分类_8bb820b8')" clearable style="width: 160px">
              <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('pluginManager.状态_3fea3ca7')">
            <el-select v-model="searchForm.status" :placeholder="$t('pluginManager.请选择状态_e1c965ef')" clearable style="width: 120px">
              <el-option :label="$t('pluginManager.已启用_53ace430')" value="enabled" />
              <el-option :label="$t('pluginManager.已禁用_1c1ed981')" value="disabled" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="loadPlugins">
          <el-icon><Search /></el-icon>{{ $t('pluginManager.查询_bee912d7') }}
        </button>
        <button class="btn-reset" @click="resetSearch">{{ $t('pluginManager.重置_4b9c3271') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="plugins" v-loading="loading" class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="pluginName" :label="$t('pluginManager.插件名称_ce411cbe_1')" min-width="180" />
        <el-table-column prop="pluginType" :label="$t('pluginManager.类型标识_5613a66e')" width="120" />
        <el-table-column prop="pluginScope" :label="$t('pluginManager.作用域_4705b884')" width="100" align="center">
          <template #default="{ row }">
            <span class="rf-tag" :class="row.pluginScope">{{ { node: '节点', api: '接口', both: '两者' }[row.pluginScope] || row.pluginScope || '节点' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" :label="$t('pluginManager.分类_d0771a42_1')" width="120" align="center">
          <template #default="{ row }">
            <span class="rf-tag">{{ row.category }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('pluginManager.描述_3bdd08ad')" min-width="200" class-name="cell-wrap" />
        <el-table-column prop="fileSize" :label="$t('pluginManager.文件大小_396b7d3f')" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('pluginManager.状态_3fea3ca7_1')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
              {{ row.status === 'enabled' ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loaded" :label="$t('pluginManager.加载状态_0915992e')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.loaded ? 'success' : 'warning'">
              {{ row.loaded ? '已加载' : '未加载' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('pluginManager.上传时间_cae25527')" width="180" />
        <el-table-column :label="$t('pluginManager.操作_2b6bc0f2')" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button
                v-if="row.status === 'disabled'"
                class="action-btn success"
                title="启用"
                @click="enablePlugin(row)"
              >
                <el-icon><Check /></el-icon>
              </button>
              <button
                v-else
                class="action-btn warning"
                title="禁用"
                @click="disablePlugin(row)"
              >
                <el-icon><Close /></el-icon>
              </button>
              <button class="action-btn primary" title="重载" @click="reloadPlugin(row)">
                <el-icon><RefreshRight /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="deletePlugin(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadPlugins"
          @current-change="loadPlugins"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Search, Check, Close, RefreshRight, Delete } from '@element-plus/icons-vue'
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
    console.error(t('pluginManager.加载插件列表_bf0bcfe9'), error)
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

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
}
</style>
