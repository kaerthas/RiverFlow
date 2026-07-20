<template>
  <div class="api-mgr-page" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- 左侧面板：应用目录 -->
    <Transition name="sidebar">
      <div v-show="!sidebarCollapsed" class="app-sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">
            <el-icon :size="18"><FolderOpened /></el-icon>
            <span>{{ $t('apiMgr.应用目录_3744ff9a') }}</span>
          </div>
          <div class="sidebar-actions">
            <el-button type="primary" size="small" circle @click="handleAddApp">
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button size="small" text circle @click="sidebarCollapsed = true">
              <el-icon><Fold /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="app-search">
          <el-input v-model="appKeyword" :placeholder="$t('apiMgr.搜索应用_d1ab74e1')" clearable prefix-icon="Search" size="small" />
        </div>
        <div v-loading="appLoading" class="app-list">
          <div class="app-item all-apps" :class="{ active: !selectedAppId }" @click="selectApp(null)">
            <div class="app-icon all">
              <el-icon :size="20"><Grid /></el-icon>
            </div>
            <div class="app-info">
              <div class="app-name">{{ $t('apiMgr.全部接口_27e6033c_1') }}</div>
              <div class="app-meta">{{ totalApiCount }} 个接口</div>
            </div>
          </div>
          <div
            v-for="app in filteredApps"
            :key="app.id"
            class="app-item"
            :class="{ active: selectedAppId === app.id }"
            @click="selectApp(app)"
          >
            <div class="app-icon">
              <el-icon :size="18"><Folder /></el-icon>
            </div>
            <div class="app-info">
              <div class="app-name">{{ app.appName }}</div>
              <div class="app-meta">{{ app.appCode }} · {{ app.apiCount || 0 }} 个接口</div>
            </div>
            <div class="app-actions">
              <el-button link type="primary" size="small" @click.stop="handleEditApp(app)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button link type="danger" size="small" @click.stop="handleDeleteApp(app)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-if="filteredApps.length === 0" class="app-empty">{{ $t('apiMgr.暂无应用_3534c3d1') }}</div>
        </div>
      </div>
    </Transition>

    <!-- 右侧面板：API 列表 -->
    <div class="api-main">
      <div class="rf-list-header">
        <div class="header-left">
          <el-button v-if="sidebarCollapsed" class="expand-btn" size="small" text circle @click="sidebarCollapsed = false">
            <el-icon><Expand /></el-icon>
          </el-button>
          <div>
            <h1 class="title">{{ currentAppName }}</h1>
            <p class="subtitle">{{ currentAppDesc }}</p>
          </div>
        </div>
        <button v-if="selectedAppId" class="btn-primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>{{ $t('apiMgr.注册接口_d0302e84_1') }}</button>
      </div>

      <!-- 搜索栏 -->
      <div class="rf-search-bar">
        <div class="search-fields">
          <el-form :model="queryForm" inline>
            <el-form-item :label="$t('apiMgr.接口编码_7abd4801')">
              <el-input v-model="queryForm.apiCode" :placeholder="$t('apiMgr.请输入接口编_faddc5d0_1')" clearable />
            </el-form-item>
            <el-form-item :label="$t('apiMgr.接口名称_34cab80c')">
              <el-input v-model="queryForm.apiName" :placeholder="$t('apiMgr.请输入接口名_03591524_1')" clearable />
            </el-form-item>
          </el-form>
        </div>
        <div class="search-actions">
          <button class="btn-search" @click="handleSearch">
            <el-icon><Search /></el-icon>{{ $t('apiMgr.查询_bee912d7') }}</button>
          <button class="btn-reset" @click="handleReset">{{ $t('apiMgr.重置_4b9c3271') }}</button>
        </div>
      </div>

      <div class="rf-table-card">
        <el-table :data="apiList" stripe v-loading="loading" class="rf-data-table" style="width: 100%" :empty-text="$t('apiMgr.暂无数据_21efd88b')">
          <el-table-column type="index" label="#" width="52" align="center" />
          <el-table-column prop="apiCode" :label="$t('apiMgr.接口编码_7abd4801_1')" width="220">
            <template #default="{ row }">
              <span class="rf-code">{{ row.apiCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="apiName" :label="$t('apiMgr.接口名称_34cab80c_1')" min-width="160" class-name="cell-wrap" />
          <el-table-column prop="apiType" :label="$t('apiMgr.类型_226b0912')" width="90" align="center">
            <template #default="{ row }">
              <span :class="['rf-tag', row.apiType]">
                {{ { proxy: '代理', sql: 'SQL', script: '脚本', data: '数据', plugin: '插件' }[row.apiType] || row.apiType }}
              </span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('apiMgr.代理接口_e05503d8')" min-width="260" class-name="cell-wrap">
            <template #default="{ row }">
              <div class="endpoint-row">
                <span :class="['rf-tag', row.openMethod?.toLowerCase()]">{{ row.openMethod }}</span>
                <span class="rf-code">/open{{ row.openPath || '/' + row.apiCode }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="authType" :label="$t('apiMgr.认证方式_b33c7279')" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.authType === 'none' ? 'info' : 'warning'" size="small">
                {{ { none: '无', basic: 'Basic', token: 'Token', sign: 'AK/SK', oauth2: 'OAuth2' }[row.authType] || row.authType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('apiMgr.状态_3fea7ca7')" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-if="selectedAppId" v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
              <el-tag v-else :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="!selectedAppId" :label="$t('apiMgr.所属应用_729d94f0')" width="140" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.appId" type="primary" size="small">{{ getAppName(row.appId) }}</el-tag>
              <span v-else class="rf-text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('apiMgr.流程触发_88bf557e')" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.triggerEnabled === 1" type="success" size="small">{{ $t('apiMgr.已启用_53ace430') }}</el-tag>
              <el-tag v-else type="info" size="small">{{ $t('apiMgr.未启用_4637765b') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="selectedAppId" :label="$t('apiMgr.操作_2b6bc0f2')" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <div class="rf-actions">
                <button class="action-btn success" :title="$t('apiMgr.调试_b7c0bfff')" @click="handleDebug(row)">
                  <el-icon><Promotion /></el-icon>
                </button>
                <button class="action-btn primary" :title="$t('apiMgr.编辑_95b351c8')" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </button>
                <button class="action-btn danger" :title="$t('apiMgr.删除_2f4aaddd')" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="rf-pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @update:page-size="handlePageChange"
            @update:current-page="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 应用编辑弹窗 -->
    <el-dialog v-model="appDialogVisible" :title="appDialogTitle" width="520px" top="20vh" destroy-on-close :close-on-click-modal="false">
      <el-form ref="appFormRef" :model="appForm" :rules="appFormRules" label-width="90px">
        <el-form-item :label="$t('apiMgr.应用编码_db5afb91')" prop="appCode">
          <el-input v-model="appForm.appCode" :placeholder="$t('apiMgr.如_88c36537')" :disabled="!!appForm.id" />
        </el-form-item>
        <el-form-item :label="$t('apiMgr.应用名称_27c3862a')" prop="appName">
          <el-input v-model="appForm.appName" :placeholder="$t('apiMgr.如用户中心_27cba917')" />
        </el-form-item>
        <el-form-item :label="$t('apiMgr.应用图标_e27bfdd7')" prop="icon">
          <el-input v-model="appForm.icon" :placeholder="$t('apiMgr.图标名如_0adce4cd')" />
        </el-form-item>
        <el-form-item label="AppKey">
          <el-input v-model="appForm.appKey" :placeholder="$t('apiMgr.应用标识_5964f03b')">
            <template #append>
              <el-button @click="generateAppKey">{{ $t('apiMgr.生成_4dfe7036') }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="AppSecret">
          <el-input v-model="appForm.appSecret" type="password" show-password :placeholder="$t('apiMgr.应用密钥_b4771143')">
            <template #append>
              <el-button @click="generateAppSecret">{{ $t('apiMgr.生成_4dfe7036_1') }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('apiMgr.排序号_1d6033bc')" prop="sortNo">
          <el-input-number v-model="appForm.sortNo" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('apiMgr.状态_3fea7ca7_1')" prop="status">
          <el-radio-group v-model="appForm.status">
            <el-radio :label="1">{{ $t('apiMgr.启用_7854b52a_1') }}</el-radio>
            <el-radio :label="0">{{ $t('apiMgr.禁用_710ad08b') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('apiMgr.应用描述_3fb775c0')" prop="description">
          <el-input v-model="appForm.description" type="textarea" :rows="3" :placeholder="$t('apiMgr.请输入应用描_e7817865')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appDialogVisible = false">{{ $t('apiMgr.取消_625fb26b') }}</el-button>
        <el-button type="primary" :loading="appSubmitLoading" @click="handleSubmitApp">{{ $t('apiMgr.保存_be5fbbe3') }}</el-button>
      </template>
    </el-dialog>

    <!-- 注册/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px" top="5vh" destroy-on-close :close-on-click-modal="false" class="edit-dialog">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="$t('apiMgr.基本信息_9e5ffa06')" name="base">
          <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.所属应用_729d94f0_1')" prop="appId">
                  <el-select v-model="form.appId" :placeholder="$t('apiMgr.请选择所属应_626a47de_1')" :disabled="!!selectedAppId" clearable style="width: 100%">
                    <el-option v-for="app in appList" :key="app.id" :label="app.appName" :value="app.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.接口编码_7abd4801_1')" prop="apiCode">
                  <el-input v-model="form.apiCode" :placeholder="$t('apiMgr.如_fc69b56a')" :disabled="!!form.id" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.接口名称_34cab80c_1')" prop="apiName">
                  <el-input v-model="form.apiName" :placeholder="$t('apiMgr.如统一认证平_21b90353')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.接口类型_27405e34')" prop="apiType">
                  <el-select v-model="form.apiType" :placeholder="$t('apiMgr.请选择_708c9d6d')" style="width: 100%">
                    <el-option :label="$t('apiMgr.代理接口_e05503d8_1')" value="proxy" />
                    <el-option :label="$t('apiMgr.服务_37781985')" value="sql" />
                    <el-option :label="$t('apiMgr.数据服务_5fd032b3')" value="data" />
                    <el-option :label="$t('apiMgr.脚本服务_6b82c823')" value="script" />
                    <el-option :label="$t('apiMgr.插件接口_6da131a3')" value="plugin" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.请求方式_3b4c5cab')" prop="method">
                  <el-select v-model="form.method" :placeholder="$t('apiMgr.请选择_708c9d6d_1')" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.代理后方式_94271905')" prop="openMethod">
                  <el-select v-model="form.openMethod" :placeholder="$t('apiMgr.请选择_708c9d6d_1')" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item v-if="form.apiType === 'sql'" :label="$t('apiMgr.语句_bdf402ca')" prop="url">
              <el-input v-model="form.url" type="textarea" :rows="4" :placeholder="$t('apiMgr.请输入语句如_2ccbf03a')" />
              <div class="sql-safety-hint">
                <el-alert :closable="false" type="warning" show-icon :title="$t('apiMgr.安全限制_0e6e6819')">
                  <div>仅允许 SELECT/INSERT/UPDATE/DELETE；禁止 DROP/TRUNCATE/ALTER/GRANT 等；UPDATE/DELETE 必须带 WHERE。</div>
                </el-alert>
              </div>
            </el-form-item>
            <el-form-item v-else-if="form.apiType === 'plugin'" :label="$t('apiMgr.插件配置_4e530c4c')" prop="url">
              <el-input v-model="form.url" type="textarea" :rows="4" placeholder='请输入插件配置 JSON，如 {"operation":"upload","bucket":"xxx"}' />
            </el-form-item>
            <el-form-item v-else :label="$t('apiMgr.原始请求地址_3a16cb56')" prop="url">
              <el-input v-model="form.url" placeholder="http(s)://..." />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.代理后路径_fd55b5eb')" prop="openPath">
                  <el-input v-model="form.openPath" :placeholder="$t('apiMgr.如_4e4ba692')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Content-Type">
                  <el-select v-model="form.contentType" :placeholder="$t('apiMgr.请选择_708c9d6d_1')" style="width: 100%">
                    <el-option label="application/json" value="application/json" />
                    <el-option label="application/x-www-form-urlencoded" value="application/x-www-form-urlencoded" />
                    <el-option label="multipart/form-data" value="multipart/form-data" />
                    <el-option label="text/xml" value="text/xml" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="proxy-path-hint">{{ $t('apiMgr.外部调用和调_7e88ce87') }}</div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.认证方式_b33c7279_1')">
                  <el-select v-model="form.authType" :placeholder="$t('apiMgr.请选择_708c9d6d_1')" style="width: 100%">
                    <el-option :label="$t('apiMgr.无_d81bb206')" value="none" />
                    <el-option label="Basic Auth" value="basic" />
                    <el-option label="Token" value="token" />
                    <el-option :label="$t('apiMgr.签名_ad7f1029')" value="sign" />
                    <el-option label="OAuth2" value="oauth2" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="$t('apiMgr.白名单_10f18c2a')">
                  <el-input v-model="form.allowedIps" :placeholder="$t('apiMgr.可选如_abc9d4a2')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.超时_a5047dab')">
                  <el-input-number v-model="form.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.数据源_c11322c9')">
                  <el-select v-model="form.dsId" :placeholder="$t('apiMgr.类型时选择_c0c82c0b')" clearable style="width: 100%">
                    <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.dsName" :value="ds.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('apiMgr.重试次数_d5f41392')">
                  <el-input-number v-model="form.retryTimes" :min="0" :max="5" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 脚本类型配置 -->
            <template v-if="form.apiType === 'script'">
              <el-form-item :label="$t('apiMgr.绑定脚本_9eb19875')">
                <el-select v-model="form.scriptId" :placeholder="$t('apiMgr.请选择要绑定_f05e25b3')" clearable style="width: 100%">
                  <el-option v-for="s in scriptOptions" :key="s.id" :label="`${s.scriptName} (${s.scriptCode})`" :value="s.id" />
                </el-select>
              </el-form-item>
            </template>

            <!-- 插件类型配置 -->
            <template v-if="form.apiType === 'plugin'">
              <el-form-item :label="$t('apiMgr.插件类型_f64c5b65')" prop="pluginType">
                <el-select v-model="form.pluginType" :placeholder="$t('apiMgr.请选择已加载_a6c15e5f')" clearable style="width: 100%">
                  <el-option v-for="p in pluginOptions" :key="p.pluginType" :label="`${p.pluginName} (${p.pluginType})`" :value="p.pluginType" />
                </el-select>
                <div v-if="pluginOptions.length === 0" style="color: #f56c6c; font-size: 12px; margin-top: 4px;">
                  未检测到已加载的插件，请检查插件管理页面是否已上传并启用
                </div>
              </el-form-item>
            </template>

            <el-form-item v-if="form.apiType === 'proxy'" :label="$t('apiMgr.代理设置_4d2b1f67')">
              <el-switch v-model="form.proxyEnabled" :active-value="1" :inactive-value="0" />
              <template v-if="form.proxyEnabled === 1">
                <el-input v-model="form.proxyHost" :placeholder="$t('apiMgr.代理主机_66f4e979')" style="width: 180px; margin-left: 12px" />
                <el-input-number v-model="form.proxyPort" :placeholder="$t('apiMgr.端口_c76cfefe')" :min="1" :max="65535" style="width: 120px; margin-left: 8px" />
              </template>
            </el-form-item>

            <el-divider content-position="left">{{ $t('apiMgr.流程触发配置_7e870113') }}</el-divider>
            <el-form-item :label="$t('apiMgr.启用流程触发_664e52e0')">
              <el-switch v-model="form.triggerEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <template v-if="form.triggerEnabled === 1">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item :label="$t('apiMgr.触发流程_14cb1543')">
                    <el-select v-model="form.triggerFlowCode" :placeholder="$t('apiMgr.请选择要触发_420a004e')" clearable style="width: 100%">
                      <el-option v-for="flow in flowDefinitionOptions" :key="flow.flowCode" :label="`${flow.flowName} (v${flow.version})`" :value="flow.flowCode" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="$t('apiMgr.业务主键字段_0ec93770')">
                    <el-input v-model="form.triggerBizKeyField" :placeholder="$t('apiMgr.请求参数中的_741ef2cd')" />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="$t('apiMgr.参数配置_664ecc90')" name="params">
          <div class="param-toolbar">
            <el-radio-group v-model="paramTab" size="small">
              <el-radio-button label="header">Header</el-radio-button>
              <el-radio-button label="query">Query</el-radio-button>
              <el-radio-button label="body">Body</el-radio-button>
              <el-radio-button label="response">Response</el-radio-button>
            </el-radio-group>
            <el-button type="primary" size="small" @click="addParam">
              <el-icon><Plus /></el-icon>{{ $t('apiMgr.添加_b58c7549') }}</el-button>
          </div>
          <el-table :data="filteredParams" stripe size="small" border>
            <el-table-column :label="$t('apiMgr.参数键_8b233552')" width="180">
              <template #default="{ row }">
                <div class="param-key-cell" :style="{ paddingLeft: (row._level * 20) + 'px' }">
                  <el-input v-model="row.paramKey" size="small" placeholder="key" />
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="$t('apiMgr.参数名称_5f49be98')" width="140">
              <template #default="{ row }">
                <el-input v-model="row.paramName" size="small" :placeholder="$t('apiMgr.名称_d7ec2d3f')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('apiMgr.数据类型_185f7bf6')" width="110">
              <template #default="{ row }">
                <el-select v-model="row.dataType" size="small" style="width: 100%">
                  <el-option label="string" value="string" />
                  <el-option label="int" value="int" />
                  <el-option label="long" value="long" />
                  <el-option label="double" value="double" />
                  <el-option label="boolean" value="boolean" />
                  <el-option label="object" value="object" />
                  <el-option label="array" value="array" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('apiMgr.必填_537b39a8')" width="60" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isRequired" :true-label="1" :false-label="0" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('apiMgr.默认值_225f3ed0')" width="140">
              <template #default="{ row }">
                <el-input v-model="row.defaultValue" size="small" :placeholder="$t('apiMgr.默认值_225f3ed0_1')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('apiMgr.操作_2b6bc0f2_1')" width="140" align="center">
              <template #default="{ row }">
                <el-button v-if="row.dataType === 'object' || row.dataType === 'array'" link type="primary" size="small" @click="addChildParam(row)">+子项</el-button>
                <el-button link type="danger" size="small" @click="removeParam(row)">{{ $t('apiMgr.删除_2f4aaddd_1') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('apiMgr.取消_625fb26b_1') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('apiMgr.保存_be5fbbe3_1') }}</el-button>
      </template>
    </el-dialog>

    <!-- 调试弹窗 -->
    <el-dialog v-model="debugDialogVisible" :title="$t('apiMgr.接口调试_f7015e9f')" width="700px" destroy-on-close>
      <ApiDebugger :url="debugRow?.url" :method="debugRow?.method" :api-type="debugRow?.apiType" />
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiCatalogList,
  saveApiCatalog,
  updateApiCatalog,
  deleteApiCatalog,
  getApiParams,
  saveApiParams,
  getApiScriptList,
  getApiPluginList
} from '@/api/apiMgr'
import { getApiAppList, getApiAppListAll, saveApiApp, updateApiApp, deleteApiApp, getApiAppCounts } from '@/api/apiApp'
import { getDatasourceList } from '@/api/datasource'
import { getFlowDefinitionList } from '@/api/workflow'
import ApiDebugger from '@/components/ApiDebugger/index.vue'

/* ========== 应用目录相关 ========== */
const sidebarCollapsed = ref(false)
const appLoading = ref(false)
const appList = ref([])
const appKeyword = ref('')
const selectedAppId = ref(null)
const totalApiCount = ref(0)

const filteredApps = computed(() => {
  if (!appKeyword.value) return appList.value
  const kw = appKeyword.value.toLowerCase()
  return appList.value.filter(app =>
    (app.appName && app.appName.toLowerCase().includes(kw)) ||
    (app.appCode && app.appCode.toLowerCase().includes(kw))
  )
})

const currentAppName = computed(() => {
  if (!selectedAppId.value) return t('apiMgr.全部接口_27e6033c')
  const app = appList.value.find(a => a.id === selectedAppId.value)
  return app ? app.appName : t('apiMgr.接口列表_7d3a5003')
})

const currentAppDesc = computed(() => {
  if (!selectedAppId.value) return t('apiMgr.管理系统所有_30866e9f')
  const app = appList.value.find(a => a.id === selectedAppId.value)
  return app ? (app.description || t('apiMgr.暂无描述_8c3ec9df')) : ''
})

function getAppName(appId) {
  if (!appId) return '-'
  const app = appList.value.find(a => String(a.id) === String(appId))
  return app ? app.appName : '-'
}

async function loadAppList() {
  appLoading.value = true
  try {
    const res = await getApiAppList({ page: 1, size: 999, status: 1 })
    const list = res.list || res.records || res || []
    // 批量获取 API 数量
    const appIds = list.map(a => a.id).filter(Boolean)
    if (appIds.length > 0) {
      try {
        const countRes = await getApiAppCounts(appIds)
        const counts = countRes || {}
        list.forEach(app => {
          app.apiCount = counts[app.id] || 0
        })
      } catch (e) {
        // 忽略统计错误
      }
    }
    appList.value = list
  } finally {
    appLoading.value = false
  }
}

function selectApp(app) {
  selectedAppId.value = app ? app.id : null
  pagination.page = 1
  loadList()
}

/* 应用弹窗 */
const appDialogVisible = ref(false)
const appDialogTitle = ref(t('apiMgr.新增应用_35d9fd51'))
const appFormRef = ref(null)
const appSubmitLoading = ref(false)
const appForm = reactive({
  id: null,
  appCode: '',
  appName: '',
  appKey: '',
  appSecret: '',
  description: '',
  icon: '',
  sortNo: 0,
  status: 1
})
const appFormRules = {
  appCode: [{ required: true, message: t('apiMgr.请输入应用编_09347efa'), trigger: 'blur' }],
  appName: [{ required: true, message: t('apiMgr.请输入应用名_dc34614a'), trigger: 'blur' }]
}

function handleAddApp() {
  appDialogTitle.value = t('apiMgr.新增应用_35d9fd51_1')
  Object.assign(appForm, {
    id: null,
    appCode: '',
    appName: '',
    appKey: '',
    appSecret: '',
    description: '',
    icon: '',
    sortNo: 0,
    status: 1
  })
  appDialogVisible.value = true
}

function handleEditApp(app) {
  appDialogTitle.value = t('apiMgr.编辑应用_396fb46d')
  Object.assign(appForm, {
    id: app.id,
    appCode: app.appCode,
    appName: app.appName,
    appKey: app.appKey || '',
    appSecret: app.appSecret || '',
    description: app.description,
    icon: app.icon,
    sortNo: app.sortNo,
    status: app.status
  })
  appDialogVisible.value = true
}

async function handleDeleteApp(app) {
  try {
    await ElMessageBox.confirm(`确认删除应用「${app.appName}」？`, t('apiMgr.删除确认_50eaf94d'), { type: 'warning' })
    await deleteApiApp(app.id)
    ElMessage.success(t('apiMgr.删除成功_0007d170'))
    if (selectedAppId.value === app.id) {
      selectedAppId.value = null
    }
    loadAppList()
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

function generateRandomString(length = 16) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

function generateAppKey() {
  appForm.appKey = 'ak_' + generateRandomString(16)
}

function generateAppSecret() {
  appForm.appSecret = generateRandomString(32)
}

async function handleSubmitApp() {
  const valid = await appFormRef.value.validate().catch(() => false)
  if (!valid) return
  appSubmitLoading.value = true
  try {
    if (appForm.id) {
      await updateApiApp(appForm)
    } else {
      await saveApiApp(appForm)
    }
    ElMessage.success(t('apiMgr.保存成功_3b108349'))
    appDialogVisible.value = false
    loadAppList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    appSubmitLoading.value = false
  }
}

/* ========== 接口列表相关（保留原有逻辑） ========== */
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('apiMgr.注册接口_d0302e84'))
const formRef = ref(null)
const submitLoading = ref(false)
const activeTab = ref('base')
const paramTab = ref('header')
const debugDialogVisible = ref(false)
const debugRow = ref(null)

const queryForm = reactive({
  apiCode: '',
  apiName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const apiList = ref([])
const datasourceOptions = ref([])
const flowDefinitionOptions = ref([])
const form = reactive({
  id: null,
  appId: null,
  apiCode: '',
  apiName: '',
  apiType: 'proxy',
  method: 'POST',
  url: '',
  openPath: '',
  openMethod: 'POST',
  contentType: 'application/json',
  authType: 'none',
  allowedIps: '',
  dsId: null,
  scriptId: null,
  pluginType: '',
  timeout: 30000,
  retryTimes: 0,
  proxyEnabled: 0,
  proxyHost: '',
  proxyPort: null,
  triggerEnabled: 0,
  triggerFlowId: null,
  triggerFlowCode: '',
  triggerBizKeyField: '',
  status: 0
})

const scriptOptions = ref([])
const pluginOptions = ref([])

const formRules = {
  appId: [{ required: true, message: t('apiMgr.请选择所属应_626a47de'), trigger: 'change' }],
  apiCode: [{ required: true, message: t('apiMgr.请输入接口编_faddc5d0'), trigger: 'blur' }],
  apiName: [{ required: true, message: t('apiMgr.请输入接口名_03591524'), trigger: 'blur' }],
  apiType: [{ required: true, message: t('apiMgr.请选择接口类_b639bb0b'), trigger: 'change' }],
  method: [{ required: true, message: t('apiMgr.请选择请求方_cba68b57'), trigger: 'change' }],
  url: [{ required: true, message: t('apiMgr.请输入请求地_e98b8137'), trigger: 'blur' }],
  openPath: [{ required: true, message: t('apiMgr.请输入代理后_680945af'), trigger: 'blur' }],
  openMethod: [{ required: true, message: t('apiMgr.请选择代理后_91f7a990'), trigger: 'change' }]
}

const allParams = ref([])

function ensureClientId(param) {
  if (!param.clientId) {
    param.clientId = 'c_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  }
}

function getParamLevel(param) {
  if (!param.parentId || param.parentId === '0' || param.parentId === 0 || param.parentId === 'null') return 0
  const parent = allParams.value.find(p =>
    p.clientId === param.parentId ||
    p.id === param.parentId ||
    String(p.id) === String(param.parentId)
  )
  return parent ? getParamLevel(parent) + 1 : 0
}

const filteredParams = computed(() => {
  const typeParams = allParams.value.filter(p => p.paramType === paramTab.value)
  typeParams.forEach(ensureClientId)
  typeParams.forEach(p => {
    p._level = getParamLevel(p)
  })

  function collectWithChildren(parentId, result) {
    const children = typeParams
      .filter(p => {
        if (!parentId || parentId === '0' || parentId === 0) {
          return !p.parentId || p.parentId === '0' || p.parentId === 0
        }
        return p.parentId === parentId
      })
      .sort((a, b) => (a.sortNo || 0) - (b.sortNo || 0))
    for (const child of children) {
      result.push(child)
      collectWithChildren(child.clientId, result)
    }
  }

  const result = []
  collectWithChildren(null, result)
  return result
})

function addParam() {
  const newParam = {
    paramType: paramTab.value,
    parentId: '0',
    paramKey: '',
    paramName: '',
    dataType: 'string',
    isRequired: 0,
    defaultValue: '',
    sortNo: allParams.value.length + 1
  }
  ensureClientId(newParam)
  allParams.value.push(newParam)
}

function addChildParam(parent) {
  const newParam = {
    paramType: paramTab.value,
    parentId: parent.clientId,
    paramKey: '',
    paramName: '',
    dataType: 'string',
    isRequired: 0,
    defaultValue: '',
    sortNo: allParams.value.length + 1
  }
  ensureClientId(newParam)
  allParams.value.push(newParam)
}

function removeParam(row) {
  const idsToDelete = new Set()

  function collectIds(target) {
    idsToDelete.add(target.clientId)
    allParams.value.forEach(p => {
      if (p.parentId === target.clientId || p.parentId === target.id || String(p.parentId) === String(target.id)) {
        collectIds(p)
      }
    })
  }

  collectIds(row)
  allParams.value = allParams.value.filter(p => !idsToDelete.has(p.clientId))
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (selectedAppId.value) params.appId = selectedAppId.value
    if (queryForm.apiCode) params.apiCode = queryForm.apiCode
    if (queryForm.apiName) params.apiName = queryForm.apiName
    const res = await getApiCatalogList(params)
    apiList.value = res?.list || res?.records || res || []
    pagination.total = Number(res?.total) || 0
    // 同时更新全部接口计数（用于左侧"全部接口"卡片）
    if (!selectedAppId.value) {
      totalApiCount.value = pagination.total
    }
  } catch (e) {
    console.error('加载接口列表失败', e)
    apiList.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadList()
}

function handlePageChange() {
  loadList()
}

function handleReset() {
  queryForm.apiCode = ''
  queryForm.apiName = ''
  pagination.page = 1
  loadList()
}

async function loadDatasourceOptions() {
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceOptions.value = res.list || res.records || res || []
  } catch (e) {
    datasourceOptions.value = []
  }
}

function handleAdd() {
  dialogTitle.value = t('apiMgr.注册接口_d0302e84_1')
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, {
    id: null,
    appId: selectedAppId.value,
    apiCode: '',
    apiName: '',
    apiType: 'proxy',
    method: 'POST',
    url: '',
    openPath: '',
    openMethod: 'POST',
    contentType: 'application/json',
    authType: 'none',
    allowedIps: '',
    dsId: null,
    scriptId: null,
    pluginType: '',
    timeout: 30000,
    retryTimes: 0,
    proxyEnabled: 0,
    proxyHost: '',
    proxyPort: null,
    triggerEnabled: 0,
    triggerFlowId: null,
    triggerFlowCode: '',
    triggerBizKeyField: '',
    status: 0
  })
  allParams.value = []
  dialogVisible.value = true
}

async function loadScriptOptions() {
  try {
    const res = await getApiScriptList({ page: 1, size: 999, status: 1 })
    scriptOptions.value = res.list || res.records || res || []
  } catch (e) {
    scriptOptions.value = []
  }
}

async function loadPluginOptions() {
  try {
    const res = await getApiPluginList()
    pluginOptions.value = res.plugins || []
  } catch (e) {
    pluginOptions.value = []
  }
}

async function handleEdit(row) {
  dialogTitle.value = `编辑接口 - ${row.apiName}`
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, { ...row })
  if (!form.openPath) form.openPath = `/${row.apiCode}`
  if (!form.openMethod) form.openMethod = row.method || 'POST'
  allParams.value = []
  dialogVisible.value = true
  await nextTick()
  try {
    const params = await getApiParams(row.id)
    allParams.value = Array.isArray(params) ? params : []
    allParams.value.forEach(ensureClientId)
    allParams.value.forEach(p => {
      if (p.parentId && p.parentId !== '0' && p.parentId !== 0) {
        const parent = allParams.value.find(pp =>
          pp.id === p.parentId || String(pp.id) === String(p.parentId)
        )
        if (parent) {
          p.parentId = parent.clientId
        }
      }
    })
    if (allParams.value.length > 0) {
      const hasBody = allParams.value.some(p => p.paramType === 'body')
      const hasQuery = allParams.value.some(p => p.paramType === 'query')
      const hasHeader = allParams.value.some(p => p.paramType === 'header')
      const hasResponse = allParams.value.some(p => p.paramType === 'response')
      if (hasBody) paramTab.value = 'body'
      else if (hasQuery) paramTab.value = 'query'
      else if (hasHeader) paramTab.value = 'header'
      else if (hasResponse) paramTab.value = 'response'
    }
  } catch (e) {
    allParams.value = []
  }
}

function validateSqlSafety(sql) {
  if (!sql || !sql.trim()) return { passed: true }
  const statements = sql.split(';').filter(s => s.trim())
  const allowed = ['SELECT', 'INSERT', 'UPDATE', 'DELETE']
  const blacklist = ['DROP', 'TRUNCATE', 'ALTER', 'CREATE', 'RENAME', 'GRANT', 'REVOKE', 'EXEC', 'EXECUTE', 'CALL']
  for (const stmt of statements) {
    // 去除注释
    let clean = stmt.replace(/\/\*[\s\S]*?\*\//g, ' ').replace(/--.*$/gm, ' ')
    // 去除字符串常量
    clean = clean.replace(/'(?:[^']|'')*'/g, "''")
    const first = clean.trim().match(/^\s*(\w+)/)
    if (!first) return { passed: false, message: t('apiMgr.无法识别语句_bf796fdb') }
    const type = first[1].toUpperCase()
    if (!allowed.includes(type)) {
      return { passed: false, message: `不允许执行 ${type}，仅允许 SELECT/INSERT/UPDATE/DELETE` }
    }
    const upper = clean.toUpperCase()
    for (const kw of blacklist) {
      if (new RegExp(`\\b${kw}\\b`).test(upper)) {
        return { passed: false, message: `SQL 包含禁止关键字 ${kw}` }
      }
    }
    if ((type === 'UPDATE' || type === 'DELETE') && !/\bWHERE\b/i.test(clean)) {
      return { passed: false, message: t('apiMgr.必须包含条件_8e66938e') }
    }
  }
  return { passed: true }
}

async function handleSubmit() {
  if (activeTab.value !== 'base') {
    activeTab.value = 'base'
    await nextTick()
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.openPath && !form.openPath.startsWith('/')) {
    form.openPath = '/' + form.openPath
  }
  if (!form.openPath || form.openPath.trim() === '/' || form.openPath.trim() === '') {
    ElMessage.warning(t('apiMgr.代理后路径不_461bb8ae'))
    return
  }
  if (form.apiType === 'sql' && form.url) {
    const sqlCheck = validateSqlSafety(form.url)
    if (!sqlCheck.passed) {
      ElMessage.warning(t('apiMgr.校验失败_b9a94845') + sqlCheck.message)
      return
    }
  }
  submitLoading.value = true
  try {
    let apiId = form.id
    if (form.id) {
      await updateApiCatalog(form)
    } else {
      const res = await saveApiCatalog(form)
      apiId = res
    }
    const validParams = allParams.value.filter(p => p.paramKey).map(p => ({
      ...p,
      clientId: p.clientId,
      parentClientId: (p.parentId && p.parentId !== '0' && p.parentId !== 0) ? String(p.parentId) : null,
      parentId: 0
    }))
    if (apiId && validParams.length) {
      await saveApiParams(apiId, validParams)
    }
    ElMessage.success(t('apiMgr.保存成功_3b108349_1'))
    dialogVisible.value = false
    loadList()
    // 如果创建了接口且选中了应用，刷新应用列表以更新数量
    if (form.appId) {
      loadAppList()
    }
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除接口「${row.apiName}」？`, t('apiMgr.删除确认_50eaf94d_1'), { type: 'warning' })
    await deleteApiCatalog(row.id)
    ElMessage.success(t('apiMgr.删除成功_0007d170_1'))
    loadList()
    loadAppList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateApiCatalog({ ...row, status: row.status })
    ElMessage.success(`接口已${row.status === 1 ? t('apiMgr.启用_7854b52a') : t('apiMgr.停用_5c56a889')}`)
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

function handleDebug(row) {
  debugRow.value = { ...row }
  debugRow.value.url = `/api/open${row.openPath || '/' + row.apiCode}`
  debugRow.value.method = row.openMethod || row.method || 'POST'
  debugDialogVisible.value = true
}

async function loadFlowDefinitionOptions() {
  try {
    const res = await getFlowDefinitionList({ page: 1, size: 999, status: 1 })
    flowDefinitionOptions.value = res.list || res.records || res || []
  } catch (e) {
    flowDefinitionOptions.value = []
  }
}

async function loadTotalApiCount() {
  try {
    const res = await getApiCatalogList({ page: 1, size: 1 })
    totalApiCount.value = Number(res.total) || 0
  } catch (e) {
    totalApiCount.value = 0
  }
}

onMounted(() => {
  loadDatasourceOptions()
  loadFlowDefinitionOptions()
  loadScriptOptions()
  loadPluginOptions()
  loadAppList()
  loadTotalApiCount()
  loadList()
})
</script>

<style scoped lang="scss">
.api-mgr-page {
  display: flex;
  height: calc(100vh - 100px);
  gap: 16px;
  position: relative;
}

/* 左侧面板 */
.app-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f0f0f0;

  .sidebar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .sidebar-actions {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.app-search {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.app-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.app-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  position: relative;

  &:hover {
    background: #f5f7fa;

    .app-actions {
      opacity: 1;
    }
  }

  &.active {
    background: #ecf5ff;
    border-left: 3px solid #409eff;

    .app-icon {
      color: #409eff;
    }

    .app-name {
      color: #409eff;
      font-weight: 600;
    }
  }

  &.all-apps {
    .app-icon.all {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: #f0f9ff;
      color: #409eff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.app-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.app-info {
  flex: 1;
  min-width: 0;

  .app-name {
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .app-meta {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.app-actions {
  opacity: 0;
  transition: opacity 0.2s;
  display: flex;
  gap: 2px;
}

.app-empty {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 40px 0;
}

/* 左侧过渡动画 */
.sidebar-enter-active,
.sidebar-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.sidebar-enter-from,
.sidebar-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.sidebar-leave-active {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
}

/* 右侧面板 */
.api-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;

  .rf-list-header {
    flex-shrink: 0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .expand-btn {
        flex-shrink: 0;
      }
    }
  }

  .rf-search-bar {
    flex-shrink: 0;
  }

  .rf-table-card {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .el-table {
      flex: 1;
      overflow: auto;
    }

    .rf-pagination {
      flex-shrink: 0;
      padding-top: 12px;
    }
  }
}

/* 保持原有样式 */
.param-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.endpoint-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  .rf-code {
    white-space: normal;
    word-break: break-all;
  }

  .endpoint-url {
    font-size: 13px;
    color: var(--rf-text-secondary);
    word-break: break-all;
    &.endpoint-sql {
      color: var(--rf-text-muted);
      font-style: italic;
    }
  }
}

.proxy-path-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--rf-text-muted);
}
</style>
