<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><DataAnalysis /></el-icon>{{ $t('datasource.数据源管理_4feab28d') }}</div>
    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>{{ $t('datasource.新增数据源_9a50895a_1') }}</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="ds in datasourceList" :key="ds.id">
          <div class="ds-card" :class="{ active: ds.status === 1 }">
            <div class="ds-header">
              <el-icon :size="32" :color="ds.status === 1 ? '#1677FF' : '#8C8C8C'"><Coin /></el-icon>
              <div class="ds-status">
                <el-tag v-if="ds.status === 1" type="success" size="small">{{ $t('datasource.在线_68905cf3') }}</el-tag>
                <el-tag v-else type="info" size="small">{{ $t('datasource.离线_50d4a850') }}</el-tag>
              </div>
            </div>
            <h4 class="ds-name">{{ ds.dsName }}</h4>
            <p class="ds-code">{{ ds.dsCode }} | {{ ds.dbType }}</p>
            <p class="ds-url" :title="ds.url">{{ ds.url }}</p>
            <div class="ds-actions">
              <el-button link type="primary" size="small" @click="handleTest(ds)">{{ $t('datasource.测试连接_69e74756') }}</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(ds)">{{ $t('datasource.编辑_95b351c8') }}</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(ds)">{{ $t('datasource.删除_2f4aaddd') }}</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item :label="$t('datasource.数据源编码_7183fb32')" prop="dsCode">
          <el-input v-model="form.dsCode" :placeholder="$t('datasource.如_ff3e84c7')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="$t('datasource.数据源名称_2739083c')" prop="dsName">
          <el-input v-model="form.dsName" :placeholder="$t('datasource.如主库_53cbc46b')" />
        </el-form-item>
        <el-form-item :label="$t('datasource.数据库类型_84b916da')" prop="dbType">
          <el-select v-model="form.dbType" :placeholder="$t('datasource.请选择_708c9d6d')" style="width: 100%" @change="onDbTypeChange">
            <el-option label="MySQL" value="mysql" />
            <el-option label="Oracle" value="oracle" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="SQL Server" value="sqlserver" />
            <el-option :label="$t('datasource.达梦_a39b17f1')" value="dm" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('datasource.驱动类_d4723bcb')" prop="driverClass">
          <el-input v-model="form.driverClass" :placeholder="$t('datasource.驱动类全限定_368560a3')" />
        </el-form-item>
        <el-form-item :label="$t('datasource.连接_7c59946f')" prop="url">
          <el-input v-model="form.url" type="textarea" :rows="2" placeholder="jdbc:mysql://host:port/db" />
        </el-form-item>
        <el-form-item :label="$t('datasource.用户名_819767ad')" prop="username">
          <el-input v-model="form.username" :placeholder="$t('datasource.数据库用户名_c1b94f24')" />
        </el-form-item>
        <el-form-item :label="$t('datasource.密码_a8105204')" prop="password" :rules="passwordRule">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '数据库密码'"
            show-password
            autocomplete="new-password"
            readonly
            @focus="($event) => $event.target.removeAttribute('readonly')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('datasource.取消_625fb26b') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('datasource.确定_38cf16f2') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDatasourceList,
  createDatasource,
  updateDatasource,
  deleteDatasource,
  testConnection
} from '@/api/datasource'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('datasource.新增数据源_9a50895a'))
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)

const datasourceList = ref([])

const form = reactive({
  id: null,
  dsCode: '',
  dsName: '',
  dbType: '',
  driverClass: '',
  url: '',
  username: '',
  password: ''
})

const formRules = {
  dsCode: [{ required: true, message: t('datasource.请输入数据源_87dc3372'), trigger: 'blur' }],
  dsName: [{ required: true, message: t('datasource.请输入数据源_1389576d'), trigger: 'blur' }],
  dbType: [{ required: true, message: t('datasource.请选择数据库_262c627f'), trigger: 'change' }],
  url: [{ required: true, message: t('datasource.请输入连接_19a9ec08'), trigger: 'blur' }],
  username: [{ required: true, message: t('datasource.请输入用户名_08b1fa13'), trigger: 'blur' }]
}

// 密码校验规则（新增必填，编辑可选）
const passwordRule = computed(() => {
  return isEdit.value
    ? []
    : [{ required: true, message: t('datasource.请输入密码_e39ffe99'), trigger: 'blur' }]
})

const dbTypeDriverMap = {
  mysql: 'com.mysql.cj.jdbc.Driver',
  oracle: 'oracle.jdbc.OracleDriver',
  postgresql: 'org.postgresql.Driver',
  sqlserver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
  dm: 'dm.jdbc.driver.DmDriver'
}

function onDbTypeChange(val) {
  if (dbTypeDriverMap[val] && !form.driverClass) {
    form.driverClass = dbTypeDriverMap[val]
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceList.value = res.list || res.records || res || []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialogTitle.value = t('datasource.新增数据源_9a50895a_1')
  isEdit.value = false
  Object.assign(form, {
    id: null,
    dsCode: '',
    dsName: '',
    dbType: '',
    driverClass: '',
    url: '',
    username: '',
    password: ''
  })
  dialogVisible.value = true
}

function handleEdit(ds) {
  dialogTitle.value = t('datasource.编辑数据源_d1090e03')
  isEdit.value = true
  Object.assign(form, { ...ds, password: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateDatasource(form)
      ElMessage.success(t('datasource.修改成功_69be6717'))
    } else {
      await createDatasource(form)
      ElMessage.success(t('datasource.新增成功_a5bfd70d'))
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(ds) {
  try {
    await ElMessageBox.confirm(`确认删除数据源「${ds.dsName}」？`, t('datasource.删除确认_50eaf94d'), { type: 'warning' })
    await deleteDatasource(ds.id)
    ElMessage.success(t('datasource.删除成功_0007d170'))
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleTest(ds) {
  try {
    await testConnection(ds.id)
    ElMessage.success(`数据源「${ds.dsName}」连接成功`)
  } catch (e) {
    // 失败已由 request 拦截器提示
  }
}

onMounted(() => {
  loadList()
})
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
