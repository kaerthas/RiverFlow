<template>
  <div class="designer-page">
    <!-- 浮动顶部工具栏 -->
    <div class="floating-toolbar">
      <div class="toolbar-left">
        <div class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <div class="flow-info">
          <span class="flow-name">{{ flowName || '未命名流程' }}</span>
          <span :class="['flow-badge', flowStatus === 1 ? 'published' : 'draft']">
            {{ flowStatus === 1 ? '已发布' : '草稿' }}
          </span>
        </div>
      </div>

      <div class="toolbar-center">
        <div class="tool-group">
          <div class="tool-item" :class="{ active: isValid }" @click="handleValidate">
            <el-icon><CircleCheck /></el-icon>
            <span>验证</span>
          </div>
          <div class="tool-item" @click="handleImport">
            <el-icon><Upload /></el-icon>
            <span>导入</span>
          </div>
          <div class="tool-item" @click="handleExport">
            <el-icon><Download /></el-icon>
            <span>导出</span>
          </div>
        </div>
        <div class="tool-divider"></div>
        <div class="tool-group">
          <div class="tool-item icon-only" @click="handleZoomIn">
            <el-icon><ZoomIn /></el-icon>
          </div>
          <div class="tool-item icon-only" @click="handleZoomOut">
            <el-icon><ZoomOut /></el-icon>
          </div>
          <div class="tool-item icon-only" @click="handleFitView">
            <el-icon><FullScreen /></el-icon>
          </div>
        </div>
      </div>

      <div class="toolbar-right">
        <button v-if="flowStatus === 1" class="btn-secondary" @click="handleCopyAndEdit">
          <el-icon><CopyDocument /></el-icon> 创建新版本
        </button>
        <template v-else>
          <button class="btn-secondary" @click="handleSave">保存草稿</button>
          <button class="btn-primary" @click="handlePublish">发布流程</button>
        </template>
        <button class="btn-accent" @click="handleTestRun" :disabled="!flowId">
          <el-icon><VideoPlay /></el-icon> 运行
        </button>
      </div>
    </div>

    <div class="designer-body">
      <!-- 左侧浮动节点面板 -->
      <div class="node-panel">
        <div class="panel-header">
          <div class="panel-icon">
            <el-icon><Grid /></el-icon>
          </div>
          <span>组件库</span>
        </div>
        <div class="panel-content">
          <div class="node-group" v-for="group in nodeGroups" :key="group.name">
            <div class="group-title">{{ group.name }}</div>
            <div class="group-nodes">
              <div
                v-for="node in group.nodes"
                :key="node.type"
                class="node-item"
                :class="{ disabled: flowExecutionMode === 'SYNC' && node.type === 'timer' }"
                draggable="true"
                @dragstart="handleDragStart($event, node)"
              >
                <div class="node-glow" :style="{ background: node.color + '20' }"></div>
                <div class="node-accent" :style="{ background: node.color }"></div>
                <div class="node-icon-wrap" :style="{ color: node.color }">
                  <el-icon :size="16"><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-info">
                  <span class="node-label">{{ node.label }}</span>
                  <span class="node-desc">{{ node.desc }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 画布区域 -->
      <div class="canvas-wrapper" @drop="handleDrop" @dragover.prevent>
        <div ref="canvasRef" class="logic-flow-canvas"></div>
        <div class="canvas-watermark">
          <svg width="160" height="160" viewBox="0 0 24 24" fill="none" opacity="0.04">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </div>
        <!-- 画布角落装饰 -->
        <div class="canvas-corner top-left"></div>
        <div class="canvas-corner top-right"></div>
        <div class="canvas-corner bottom-left"></div>
        <div class="canvas-corner bottom-right"></div>
      </div>

      <!-- 右侧浮动属性面板 -->
      <div class="property-panel" :class="{ active: selectedNode || selectedEdge }">
        <div class="panel-header">
          <div class="panel-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <span>属性配置</span>
        </div>
        <div class="panel-content">
          <div v-if="selectedNode" class="property-form">
            <div class="section-title">基本信息</div>
            <el-form label-position="top" size="default">
              <el-form-item label="节点名称">
                <el-input v-model="selectedNode.properties.name" placeholder="请输入节点名称" @change="updateNodeProperties" />
              </el-form-item>
              <el-form-item label="节点编码">
                <el-input v-model="selectedNode.properties.code" placeholder="自动生成" disabled />
              </el-form-item>
            </el-form>

            <!-- API节点配置 -->
            <template v-if="selectedNode.type === 'api'">
              <div class="section-title">接口配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="绑定接口">
                  <el-select v-model="selectedNode.properties.apiCode" placeholder="选择已注册的接口" clearable style="width: 100%" @change="handleApiCodeChange">
                    <el-option v-for="api in apiCatalogOptions" :key="api.id" :label="api.apiName" :value="api.apiCode" />
                  </el-select>
                </el-form-item>
                <el-form-item label="超时时间(ms)">
                  <el-input-number v-model="selectedNode.properties.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="失败策略">
                  <el-radio-group v-model="selectedNode.properties.failStrategy" @change="updateNodeProperties">
                    <el-radio-button label="suspend">挂起</el-radio-button>
                    <el-radio-button label="skip">跳过</el-radio-button>
                    <el-radio-button label="retry">重试</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </template>

            <!-- DB节点配置 -->
            <template v-if="selectedNode.type === 'db'">
              <div class="section-title">数据库配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="数据源">
                  <el-select v-model="selectedNode.properties.dsCode" placeholder="选择数据源" clearable style="width: 100%" @change="updateNodeProperties">
                    <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.dsName" :value="ds.dsCode" />
                  </el-select>
                </el-form-item>
                <el-form-item label="操作类型">
                  <el-radio-group v-model="selectedNode.properties.operation" @change="updateNodeProperties">
                    <el-radio-button label="select">查询</el-radio-button>
                    <el-radio-button label="insert">插入</el-radio-button>
                    <el-radio-button label="update">更新</el-radio-button>
                    <el-radio-button label="delete">删除</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="SQL语句">
                  <el-input v-model="selectedNode.properties.sql" type="textarea" :rows="4" placeholder="支持SpEl占位符，如 SELECT * FROM table WHERE id = #{_businessKey}" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" size="small" :loading="parsingColumns" @click="parseSqlColumns">
                    <el-icon><Search /></el-icon> 解析返回字段
                  </el-button>
                  <el-button v-if="parsedColumns.length > 0" link type="primary" size="small" @click="addAllColumnsToOutput">
                    全部添加至输出映射
                  </el-button>
                </el-form-item>
                <!-- 解析出的字段列表 -->
                <div v-if="parsedColumns.length > 0" class="parsed-columns-section">
                  <div class="parsed-columns-list">
                    <div v-for="col in parsedColumns" :key="col.name" class="parsed-column-item">
                      <span class="col-name">{{ col.name }}</span>
                      <span class="col-type">{{ col.type }}</span>
                      <el-button link type="primary" size="small" @click="addOutputMappingFromColumn(col)">
                        添加
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-form-item label="结果变量名">
                  <el-input v-model="selectedNode.properties.resultVarName" placeholder="如 aData，查询结果将自动写入上下文供下游使用" @change="updateNodeProperties" />
                </el-form-item>
                <el-alert class="form-tip" type="info" :closable="false" show-icon>
                  <template #title>
                    查询结果会自动放入上下文变量 <code>{{ selectedNode.properties.resultVarName || '（未设置）' }}</code>，下游节点可通过 <code>context.get('变量名')</code> 读取；也可通过下方【输出映射】将字段精确映射到上下文
                  </template>
                </el-alert>
              </el-form>
            </template>

            <!-- 条件节点配置 -->
            <template v-if="selectedNode.type === 'condition'">
              <div class="section-title">条件配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="条件表达式(SpEL)">
                  <el-input v-model="selectedNode.properties.expression" type="textarea" :rows="3" placeholder="如: #{context.resultCode == 200}" @change="updateNodeProperties" />
                </el-form-item>
              </el-form>
              <el-alert class="form-tip" type="info" :closable="false" show-icon>
                <template #title>支持使用 <code>context.xxx</code> 访问上下文变量</template>
              </el-alert>
            </template>

            <!-- 定时节点配置 -->
            <template v-if="selectedNode.type === 'timer'">
              <div class="section-title">定时配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="等待方式">
                  <el-radio-group v-model="selectedNode.properties.timerType" @change="updateNodeProperties">
                    <el-radio-button label="delay">延迟</el-radio-button>
                    <el-radio-button label="fixed">指定时间</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="selectedNode.properties.timerType === 'delay'" label="延迟秒数">
                  <el-input-number v-model="selectedNode.properties.delaySeconds" :min="1" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item v-else label="指定时间">
                  <el-date-picker v-model="selectedNode.properties.fixedTime" type="datetime" placeholder="选择日期时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" @change="updateNodeProperties" />
                </el-form-item>
              </el-form>
            </template>

            <!-- foreach 循环节点配置 -->
            <template v-if="selectedNode.type === 'foreach'">
              <div class="section-title">循环配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="循环源表达式">
                  <el-input v-model="selectedNode.properties.sourceExpr" type="textarea" :rows="2" placeholder="如 context.orderList" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="循环变量">
                  <el-input v-model="selectedNode.properties.itemVar" placeholder="默认 item" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="下标变量">
                  <el-input v-model="selectedNode.properties.indexVar" placeholder="默认 index" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="结果变量">
                  <el-input v-model="selectedNode.properties.resultVar" :placeholder="'loopResult_' + selectedNode.id" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="最大迭代数">
                  <el-input-number v-model="selectedNode.properties.maxIterations" :min="1" :max="10000" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="超时时间(ms)">
                  <el-input-number v-model="selectedNode.properties.timeout" :min="1000" :max="600000" :step="1000" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="中断条件">
                  <el-input v-model="selectedNode.properties.breakExpr" type="textarea" :rows="2" placeholder="如 context.item.status == 'STOP'" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="空集合行为">
                  <el-radio-group v-model="selectedNode.properties.emptyAction" @change="updateNodeProperties">
                    <el-radio-button label="skip">跳过</el-radio-button>
                    <el-radio-button label="fail">报错</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="并行执行">
                  <el-switch v-model="selectedNode.properties.parallel" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item v-if="selectedNode.properties.parallel" label="并行度">
                  <el-input-number v-model="selectedNode.properties.parallelLimit" :min="2" :max="50" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
              </el-form>
            </template>

            <!-- while 条件循环节点配置 -->
            <template v-if="selectedNode.type === 'while'">
              <div class="section-title">条件循环配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="条件表达式">
                  <el-input v-model="selectedNode.properties.conditionExpr" type="textarea" :rows="2" placeholder="如 context.hasNextPage == true" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="最大迭代数">
                  <el-input-number v-model="selectedNode.properties.maxIterations" :min="1" :max="10000" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
                <el-form-item label="超时时间(ms)">
                  <el-input-number v-model="selectedNode.properties.timeout" :min="1000" :max="600000" :step="1000" style="width: 100%" @change="updateNodeProperties" />
                </el-form-item>
              </el-form>
            </template>

            <!-- end_foreach / end_while 循环结束节点配置 -->
            <template v-if="selectedNode.type === 'end_foreach' || selectedNode.type === 'end_while'">
              <div class="section-title">循环结束配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="关联循环">
                  <el-input v-model="selectedNode.properties.loopNodeId" placeholder="关联的循环节点ID" disabled />
                </el-form-item>
                <el-form-item label="聚合表达式">
                  <el-input v-model="selectedNode.properties.aggregateExpr" type="textarea" :rows="2" placeholder="默认 context.item" @change="updateNodeProperties" />
                </el-form-item>
              </el-form>
            </template>

            <!-- 脚本节点配置 -->
            <template v-if="selectedNode.type === 'script'">
              <div class="section-title">脚本配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="脚本内容 (Groovy)">
                  <MonacoEditor
                    v-model="selectedNode.properties.scriptContent"
                    language="java"
                    theme="vs-dark"
                    height="240px"
                    @change="updateNodeProperties"
                  />
                  <div style="margin-top: 8px; text-align: right;">
                    <el-button type="primary" size="small" @click="openScriptEditor">
                      <el-icon><Edit /></el-icon> 编辑脚本
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
              <el-alert class="form-tip" type="info" :closable="false" show-icon>
                <template #title>支持 Groovy 语法，可访问 <code>args</code> 上下文变量</template>
              </el-alert>

              <el-alert class="form-tip" type="success" :closable="false" show-icon>
                <template #title>脚本中 <code>context.set("key", value)</code> 会自动把变量写入上下文，下游节点可直接使用</template>
              </el-alert>
            </template>

            <!-- 插件节点配置 -->
            <template v-if="isPluginNode(selectedNode?.type)">
              <div class="section-title">插件配置</div>
              <el-form label-position="top" size="default">
                <template v-for="field in getPluginFields(selectedNode?.type)" :key="field.name">
                  <el-form-item :label="field.label" :required="field.required">
                    <el-input 
                      v-if="field.type === 'text'"
                      v-model="selectedNode.properties[field.name]" 
                      :placeholder="field.placeholder" 
                      @change="updateNodeProperties" 
                    />
                    <el-input 
                      v-else-if="field.type === 'password'"
                      v-model="selectedNode.properties[field.name]" 
                      type="password"
                      :placeholder="field.placeholder"
                      show-password
                      @change="updateNodeProperties" 
                    />
                    <el-input 
                      v-else-if="field.type === 'textarea'"
                      v-model="selectedNode.properties[field.name]" 
                      type="textarea"
                      :rows="3"
                      :placeholder="field.placeholder"
                      @change="updateNodeProperties" 
                    />
                    <el-select 
                      v-else-if="field.type === 'select'"
                      v-model="selectedNode.properties[field.name]" 
                      :placeholder="field.placeholder || '请选择'"
                      style="width: 100%"
                      @change="updateNodeProperties"
                    >
                      <el-option 
                        v-for="opt in field.options" 
                        :key="opt.value" 
                        :label="opt.label" 
                        :value="opt.value" 
                      />
                    </el-select>
                    <el-input-number 
                      v-else-if="field.type === 'number'"
                      v-model="selectedNode.properties[field.name]"
                      style="width: 100%"
                      @change="updateNodeProperties"
                    />
                  </el-form-item>
                  <el-alert v-if="field.tip" class="form-tip" type="info" :closable="false" show-icon style="margin-bottom: 12px;">
                    <template #title>{{ field.tip }}</template>
                  </el-alert>
                </template>
              </el-form>
            </template>

            <!-- 输入映射 -->
            <template v-if="selectedNode?.type === 'api' && currentApiParams.length > 0">
              <div class="section-title">接口入参绑定</div>
              <el-tabs v-model="apiParamActiveTab" class="api-param-tabs">
                <el-tab-pane v-for="ptype in ['body','header','query']" :key="ptype" :label="apiParamTypeLabels[ptype]" :name="ptype">
                  <div class="api-param-mapping-list">
                    <div v-for="param in apiParamsByType[ptype]" :key="param.id || param.paramKey" class="mapping-card api-param-card">
                      <div class="mapping-row api-param-row">
                        <span class="param-badge" :class="param.paramType">{{ param.paramType }}</span>
                        <div class="param-info">
                          <span class="param-key">{{ getApiParamDisplayKey(param) }}</span>
                          <span v-if="param.paramName" class="param-name">{{ param.paramName }}</span>
                          <span v-if="param.isRequired" class="param-required">*</span>
                        </div>
                        <el-icon class="mapping-arrow"><Right /></el-icon>
                        <el-input
                          :model-value="getInputSource(getApiTarget(param))"
                          @update:model-value="setInputSource(getApiTarget(param), $event)"
                          :placeholder="getInputType(getApiTarget(param)) === 'var' ? '来源(上下文变量)' : '输入常量值'"
                          size="small"
                          @change="updateNodeProperties"
                        >
                          <template #prefix>
                            <el-button
                              size="small"
                              :type="getInputType(getApiTarget(param)) === 'var' ? 'primary' : ''"
                              text
                              style="padding: 0 4px; font-size: 11px; margin-right: 4px;"
                              @click.stop="toggleInputType(getApiTarget(param))"
                            >
                              {{ getInputType(getApiTarget(param)) === 'var' ? '变量' : '常量' }}
                            </el-button>
                          </template>
                          <template #suffix>
                            <el-icon v-if="getInputType(getApiTarget(param)) === 'var'" class="var-picker-trigger" @click="openVarPicker(getApiTarget(param))"><ArrowDownBold /></el-icon>
                          </template>
                        </el-input>
                      </div>
                    </div>
                    <div v-if="apiParamsByType[ptype].length === 0" class="mapping-empty">暂无 {{ apiParamTypeLabels[ptype] }} 参数</div>
                  </div>
                </el-tab-pane>
              </el-tabs>

              <!-- 自定义映射 -->
              <div class="section-title">
                <span>自定义映射</span>
                <el-button link type="primary" size="small" @click="addInputMapping">
                  <el-icon><Plus /></el-icon> 添加
                </el-button>
              </div>
              <div class="mapping-list">
                <div v-for="(map, idx) in customInputMappings" :key="idx" class="mapping-card">
                  <div class="mapping-row">
                    <el-input v-model="map.source" :placeholder="(map.type || 'var') === 'var' ? '来源(上下文)' : '输入常量值'" size="small" @change="updateNodeProperties">
                      <template #prefix>
                        <el-button
                          size="small"
                          :type="(map.type || 'var') === 'var' ? 'primary' : ''"
                          text
                          style="padding: 0 4px; font-size: 11px; margin-right: 4px;"
                          @click.stop="map.type = (map.type || 'var') === 'var' ? 'const' : 'var'; updateNodeProperties()"
                        >
                          {{ (map.type || 'var') === 'var' ? '变量' : '常量' }}
                        </el-button>
                      </template>
                      <template #suffix>
                        <el-icon v-if="(map.type || 'var') === 'var'" class="var-picker-trigger" @click="openVarPicker(map.target)"><ArrowDownBold /></el-icon>
                      </template>
                    </el-input>
                    <el-icon class="mapping-arrow"><Right /></el-icon>
                    <el-input v-model="map.target" placeholder="目标(节点入参)" size="small" @change="updateNodeProperties" />
                  </div>
                  <el-icon class="mapping-delete" @click="removeCustomInputMapping(map.target)"><Close /></el-icon>
                </div>
                <div v-if="customInputMappings.length === 0" class="mapping-empty">暂无自定义映射</div>
              </div>
            </template>

            <!-- API 返回参数映射 -->
            <template v-if="selectedNode?.type === 'api' && currentResponseParams.length > 0">
              <div class="section-title">返回参数映射</div>
              <div class="mapping-tip">节点执行后，以下返回字段自动写入上下文</div>
              <div class="api-param-mapping-list">
                <div v-for="param in currentResponseParams" :key="param.id || param.paramKey" class="mapping-card api-param-card">
                  <div class="mapping-row api-param-row">
                    <span class="param-badge response">RESPONSE</span>
                    <div class="param-info">
                      <span class="param-key">{{ param.paramKey }}</span>
                      <span v-if="param.paramName" class="param-name">{{ param.paramName }}</span>
                    </div>
                    <el-icon class="mapping-arrow"><Right /></el-icon>
                    <el-input
                      :model-value="getApiOutputTarget(param.paramKey)"
                      @update:model-value="setApiOutputTarget(param.paramKey, $event)"
                      placeholder="context.变量名"
                      size="small"
                      @change="updateNodeProperties"
                    />
                  </div>
                </div>
              </div>
            </template>

            <!-- end 节点：流程返回参数绑定 -->
            <template v-if="selectedNode?.type === 'end' && filteredFlowOutputParams.length > 0">
              <div class="section-title">流程返回参数绑定</div>
              <div class="mapping-tip">选择上下文变量作为流程返回结果字段</div>
              <div class="api-param-mapping-list">
                <div v-for="param in filteredFlowOutputParams" :key="param.value" class="mapping-card api-param-card">
                  <div class="mapping-row api-param-row">
                    <el-input
                      :model-value="getEndParamSource(param.value.replace(/^context\./, ''))"
                      @update:model-value="setEndParamSource(param.value.replace(/^context\./, ''), $event)"
                      placeholder="context.变量名"
                      size="small"
                      @change="updateNodeProperties"
                    >
                      <template #suffix>
                        <el-icon class="var-picker-trigger" @click="openVarPickerForInputMapping(getEndParamMappingIndex(param.value.replace(/^context\./, '')))"><ArrowDownBold /></el-icon>
                      </template>
                    </el-input>
                    <el-icon class="mapping-arrow"><Right /></el-icon>
                    <div class="param-info">
                      <span class="param-key">{{ param.label }}</span>
                    </div>
                    <span class="param-badge" :class="param.type">{{ param.type }}</span>
                  </div>
                </div>
              </div>
            </template>

            <template v-else-if="!['start', 'api', 'end'].includes(selectedNode?.type)">
              <div class="section-title">
                <span>输入映射</span>
                <el-button link type="primary" size="small" @click="addInputMapping">
                  <el-icon><Plus /></el-icon> 添加
                </el-button>
              </div>
              <div class="mapping-tip">
                <template v-if="selectedNode?.type === 'script'">
                  为脚本注入 <code>args</code> 参数，脚本中通过 <code>args.xxx</code> 读取上游变量
                </template>
                <template v-else>
                  节点执行前，从上下文变量取值映射为节点入参
                </template>
              </div>
              <div class="mapping-list">
                <div v-for="(map, idx) in inputMappings" :key="idx" class="mapping-card">
                  <div class="mapping-row">
                    <el-input v-model="map.source" :placeholder="(map.type || 'var') === 'var' ? '来源变量，如 context.userId' : '输入常量值'" size="small" @change="updateNodeProperties">
                      <template #prefix>
                        <el-button
                          size="small"
                          :type="(map.type || 'var') === 'var' ? 'primary' : ''"
                          text
                          style="padding: 0 4px; font-size: 11px; margin-right: 4px;"
                          @click.stop="map.type = (map.type || 'var') === 'var' ? 'const' : 'var'; updateNodeProperties()"
                        >
                          {{ (map.type || 'var') === 'var' ? '变量' : '常量' }}
                        </el-button>
                      </template>
                      <template #suffix>
                        <el-icon v-if="(map.type || 'var') === 'var'" class="var-picker-trigger" @click="openVarPickerForInputMapping(idx)"><ArrowDownBold /></el-icon>
                      </template>
                    </el-input>
                    <el-icon class="mapping-arrow"><Right /></el-icon>
                    <el-input v-model="map.target" placeholder="节点入参字段" size="small" @change="updateNodeProperties" />
                  </div>
                  <el-icon class="mapping-delete" @click="removeInputMapping(idx)"><Close /></el-icon>
                </div>
                <div v-if="inputMappings.length === 0" class="mapping-empty">暂无输入映射，点击上方「添加」配置</div>
              </div>
            </template>

            <!-- 数据来源（数据血缘） -->
            <template v-if="dataSources.length > 0">
              <div class="section-title">数据来源</div>
              <div class="data-source-list">
                <div v-for="ds in dataSources" :key="ds.source + ds.target" class="data-source-item">
                  <span class="ds-tag">{{ ds.target }}</span>
                  <el-icon class="ds-arrow"><Right /></el-icon>
                  <span class="ds-from">{{ ds.fromNodeName }}</span>
                </div>
              </div>
            </template>

            <!-- 输出映射：script/start/end 节点不需要 -->
            <template v-if="!['script','start','end'].includes(selectedNode?.type)">
              <div class="section-title">
                <span>自定义输出映射</span>
                <el-button link type="primary" size="small" @click="addOutputMapping">
                  <el-icon><Plus /></el-icon> 添加
                </el-button>
              </div>
              <div class="mapping-tip">节点执行后，自定义字段写入上下文（高级用法）</div>
              <div class="mapping-list">
                <div v-for="(map, idx) in outputMappings" :key="idx" class="mapping-card">
                  <div class="mapping-row">
                    <el-input v-model="map.source" placeholder="来源：节点返回字段" size="small" @change="updateNodeProperties" />
                    <el-icon class="mapping-arrow"><Right /></el-icon>
                    <el-input v-model="map.target" placeholder="目标：context.变量名" size="small" @change="updateNodeProperties" />
                  </div>
                  <el-icon class="mapping-delete" @click="removeOutputMapping(idx)"><Close /></el-icon>
                </div>
                <div v-if="outputMappings.length === 0" class="mapping-empty">暂无自定义映射，通常无需手动配置</div>
              </div>
            </template>
          </div>

          <div v-else-if="selectedEdge" class="property-form">
            <div class="section-title">连线配置</div>
            <el-form label-position="top" size="default">
              <el-form-item label="条件类型">
                <el-radio-group v-model="selectedEdge.properties.conditionType" @change="updateEdgeProperties">
                  <el-radio-button label="default">默认</el-radio-button>
                  <el-radio-button label="success">成功</el-radio-button>
                  <el-radio-button label="fail">失败</el-radio-button>
                  <el-radio-button label="custom">自定义</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="selectedEdge.properties.conditionType === 'custom'" label="SpEL表达式">
                <el-input v-model="selectedEdge.properties.conditionExpression" type="textarea" :rows="2" placeholder="#{context.resultCode == 200}" @change="updateEdgeProperties" />
              </el-form-item>
              <el-form-item label="优先级">
                <el-input-number v-model="selectedEdge.properties.priority" :min="0" :max="100" style="width: 100%" @change="updateEdgeProperties" />
              </el-form-item>
            </el-form>
          </div>

          <div v-else class="empty-tip">
            <div class="empty-visual">
              <div class="empty-orbit">
                <div class="empty-planet"></div>
                <div class="empty-satellite s1"></div>
                <div class="empty-satellite s2"></div>
              </div>
            </div>
            <div class="empty-text">选择节点或连线</div>
            <div class="empty-desc">在画布上点击元素以编辑属性</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 测试运行弹窗 -->
    <el-dialog v-model="testRunVisible" title="测试运行流程" width="480px" class="flow-dialog">
      <el-form label-width="100px">
        <el-form-item label="业务主键">
          <el-input v-model="testBusinessKey" placeholder="请输入业务主键（如办件流水号）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testRunVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTestRun" :loading="testLoading">启动实例</el-button>
      </template>
    </el-dialog>

    <!-- 变量选择器弹窗 -->
    <el-dialog v-model="varPickerVisible" title="选择变量" width="560px" class="var-picker-dialog" destroy-on-close>
      <el-input v-model="varSearch" placeholder="搜索变量..." size="small" clearable style="margin-bottom: 12px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="var-picker-content">
        <div v-for="group in filteredVars" :key="group.name" class="var-group">
          <div class="var-group-title">{{ group.name }}</div>
          <div v-for="item in group.items" :key="item.value" class="var-item" @click="selectVar(item.value)">
            <el-tooltip :content="item.value" placement="top" :show-after="300">
              <div class="var-line" :style="{ paddingLeft: ((item.level || 0) * 24 + 12) + 'px' }">
                <span v-for="n in (item.level || 0)" :key="n" class="var-indent-mark"></span>
                <span class="var-name">{{ item.label }}</span>
                <span v-if="item.type" class="var-type" :class="'type-' + item.type">{{ item.type }}</span>
              </div>
            </el-tooltip>
          </div>
        </div>
        <div v-if="filteredVars.length === 0" class="var-empty">未找到匹配的变量</div>
      </div>
    </el-dialog>

    <!-- 脚本编辑弹窗 -->
    <el-dialog v-model="scriptEditorVisible" title="编辑脚本 (Groovy)" width="800px" class="script-editor-dialog" destroy-on-close>
      <MonacoEditor
        v-model="scriptEditorContent"
        language="java"
        theme="vs-dark"
        height="480px"
      />
      <template #footer>
        <el-button @click="scriptEditorVisible = false">取消</el-button>
        <el-button type="primary" @click="saveScriptContent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import LogicFlow, {
  RectNode, RectNodeModel,
  BezierEdge, BezierEdgeModel,
  h
} from '@logicflow/core'
import {
  Menu,
  SelectionSelect
} from '@logicflow/extension'
import '@logicflow/core/dist/style/index.css'
import '@logicflow/extension/lib/style/index.css'
import { saveFlowDefinition, saveFlowGraph, getFlowDefinitionDetail, publishFlowDefinition, startFlowInstance, copyFlowDefinition } from '@/api/workflow'
import { getDatasourceList } from '@/api/datasource'
import { getApiCatalogList, getApiParams } from '@/api/apiMgr'
import request from '@/utils/request'
import MonacoEditor from '@/components/MonacoEditor/index.vue'

const route = useRoute()
const router = useRouter()
const canvasRef = ref(null)
let lf = null
// 已注册的 LogicFlow 节点类型（防止兜底注册覆盖内置/插件节点）
const registeredNodeTypes = new Set()
// 批量删除循环体标志，防止 node:delete 递归级联
let isBatchDeletingLoop = false

const flowId = ref(route.query.id ? String(route.query.id) : null)
const flowName = ref('未命名流程')
const flowStatus = ref(0)
const flowExecutionMode = ref('ASYNC')
const version = ref(1)
const flowInputParams = ref([])
const flowOutputParams = ref([])

const filteredFlowOutputParams = computed(() => {
  return flowOutputParams.value.filter(p => p.type !== 'object')
})

function flattenInputParams(obj, prefix = 'context', level = 0) {
  const items = []
  if (obj === null || typeof obj !== 'object' || Array.isArray(obj)) return items
  for (const [key, value] of Object.entries(obj)) {
    const fullPath = `${prefix}.${key}`
    let type = 'string'
    if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
      type = 'object'
    } else if (typeof value === 'number') {
      type = Number.isInteger(value) ? 'int' : 'double'
    } else if (typeof value === 'boolean') {
      type = 'boolean'
    }
    items.push({ label: key, value: fullPath, level, type })
    if (type === 'object') {
      items.push(...flattenInputParams(value, fullPath, level + 1))
    }
  }
  return items
}

// 解析脚本内容中 context.set("key", ...) 或 context.set('key', ...) 的变量名
function parseScriptContextVars(script) {
  const vars = new Set()
  if (!script || typeof script !== 'string') return []
  const regex = /context\.set\s*\(\s*["']([^"']+)["']/g
  let match
  while ((match = regex.exec(script)) !== null) {
    vars.add(match[1])
  }
  return Array.from(vars)
}

// 从 SQL 语句中提取 SELECT 字段名（用于变量选择器字段推断）
function extractSqlSelectColumns(sql) {
  const columns = []
  if (!sql || typeof sql !== 'string') return columns
  const cleaned = sql
    .replace(/--.*$/gm, ' ')
    .replace(/\/\*[\s\S]*?\*\//g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  const match = cleaned.match(/SELECT\s+(.*?)\s+FROM\s/i)
  if (!match) return columns
  const parts = match[1].split(',')
  for (let part of parts) {
    part = part.trim()
    if (!part || part === '*') continue
    let alias = part
    const asMatch = part.match(/\s+AS\s+(\w+)$/i)
    if (asMatch) {
      alias = asMatch[1]
    } else {
      const dotParts = part.split('.')
      alias = dotParts[dotParts.length - 1]
      alias = alias.replace(/[()]/g, '').trim()
    }
    if (alias) columns.push(alias)
  }
  return columns
}

// 从 context.xxx 或裸变量名中提取根变量名
function extractContextVarName(expr) {
  if (!expr || typeof expr !== 'string') return ''
  const trimmed = expr.trim()
  if (trimmed.startsWith('context.')) {
    return trimmed.substring(8).split(/[.\[]/)[0]
  }
  return trimmed.split(/[.\[]/)[0]
}

// 找到产生指定上下文变量的上游节点
function findSourceNodeForVariable(varName, graphData, upstreamIds) {
  if (!varName || !graphData?.nodes) return null
  for (const node of graphData.nodes) {
    if (!upstreamIds.has(node.id)) continue
    if (node.type === 'db' && node.properties?.resultVarName === varName) {
      return node
    }
    if (node.type === 'script' && node.properties?.scriptContent) {
      const vars = parseScriptContextVars(node.properties.scriptContent)
      if (vars.includes(varName)) return node
    }
    if (node.type === 'api' && node.properties?.apiCode) {
      try {
        const outputMapping = node.outputMapping || node.properties?.outputMapping
        if (outputMapping) {
          const mappings = JSON.parse(outputMapping)
          if (mappings.some(m => m.target === `context.${varName}` || m.target === varName)) {
            return node
          }
        }
      } catch (e) { /* ignore */ }
    }
  }
  return null
}

// 推断 foreach 循环项的字段列表
function inferLoopItemFields(foreachNode, graphData, upstreamIds) {
  const sourceExpr = foreachNode.properties?.sourceExpr
  const varName = extractContextVarName(sourceExpr)
  if (!varName) return []
  const sourceNode = findSourceNodeForVariable(varName, graphData, upstreamIds)
  if (!sourceNode) return []
  if (sourceNode.type === 'db' && sourceNode.properties?.sql) {
    return extractSqlSelectColumns(sourceNode.properties.sql)
  }
  if (sourceNode.type === 'api' && sourceNode.properties?.apiCode) {
    const api = apiCatalogOptions.value.find(a => a.apiCode === sourceNode.properties.apiCode)
    if (api?.id) {
      const params = allApiParams.value.filter(p => p.apiId == api.id && p.paramType === 'response')
      return params.map(p => p.paramKey)
    }
  }
  return []
}
const isValid = ref(false)
const testRunVisible = ref(false)
const testBusinessKey = ref('TEST_' + Date.now())
const testLoading = ref(false)

const selectedNode = ref(null)
const selectedEdge = ref(null)
const inputMappings = ref([])
const outputMappings = ref([])
const datasourceOptions = ref([])
const apiCatalogOptions = ref([])
const parsedColumns = ref([])
const parsingColumns = ref(false)
const currentApiParams = ref([])
const currentResponseParams = ref([])
const allApiParams = ref([])

// 变量选择器
const varPickerVisible = ref(false)
const currentEditingTarget = ref('')
const currentEditingInputMappingIdx = ref(-1)
const varSearch = ref('')
const scriptOutputFields = ref('')

// 脚本编辑器
const scriptEditorVisible = ref(false)
const scriptEditorContent = ref('')

function openScriptEditor() {
  if (!selectedNode.value) return
  scriptEditorContent.value = selectedNode.value.properties.scriptContent || ''
  scriptEditorVisible.value = true
}

function saveScriptContent() {
  if (!selectedNode.value) return
  selectedNode.value.properties.scriptContent = scriptEditorContent.value
  updateNodeProperties()
  scriptEditorVisible.value = false
  ElMessage.success('脚本内容已保存')
}

const baseNodeGroups = [
  {
    name: '基础节点',
    nodes: [
      { type: 'start', label: '开始', desc: '流程起点', icon: 'VideoPlay', color: '#22c55e' },
      { type: 'end', label: '结束', desc: '流程终点', icon: 'CircleCheck', color: '#ef4444' }
    ]
  },
  {
    name: '数据处理',
    nodes: [
      { type: 'api', label: '接口调用', desc: '调用外部API', icon: 'Upload', color: '#3b82f6' },
      { type: 'db', label: '数据库', desc: '执行SQL操作', icon: 'Coin', color: '#8b5cf6' },
      { type: 'script', label: '脚本处理', desc: 'Groovy脚本', icon: 'DocumentCopy', color: '#ec4899' }
    ]
  },
  {
    name: '控制流',
    nodes: [
      { type: 'condition', label: '条件判断', desc: '分支条件', icon: 'Share', color: '#f59e0b' },
      { type: 'timer', label: '定时等待', desc: '延迟或定时', icon: 'Timer', color: '#06b6d4' },
      { type: 'foreach', label: '循环', desc: '遍历集合并执行循环体', icon: 'Refresh', color: '#8b5cf6' },
      // { type: 'end_foreach', label: '循环结束', desc: '结束循环体并聚合结果', icon: 'CircleClose', color: '#a78bfa' },
      { type: 'while', label: '条件循环', desc: '按条件重复执行循环体', icon: 'RefreshRight', color: '#06b6d4' }
      // { type: 'end_while', label: '条件循环结束', desc: '结束条件循环并聚合结果', icon: 'CircleClose', color: '#22d3ee' }
    ]
  }
]

const nodeGroups = ref([...baseNodeGroups])
const pluginSchemas = ref({})
const pluginOutputSchemas = ref({})

const loadPluginNodes = async () => {
  try {
    const res = await request({
      url: '/plugin/loaded',
      method: 'get'
    })
    
    if (res && res.plugins && res.plugins.length > 0) {
      const pluginNodes = res.plugins.map(plugin => {
        if (plugin.configSchema) {
          try {
            pluginSchemas.value[plugin.nodeType] = JSON.parse(plugin.configSchema)
          } catch (e) {
            console.error('解析插件schema失败', plugin.nodeType, e)
          }
        }
        if (plugin.outputSchema) {
          try {
            pluginOutputSchemas.value[plugin.nodeType] = JSON.parse(plugin.outputSchema)
          } catch (e) {
            console.error('解析插件输出schema失败', plugin.nodeType, e)
          }
        }
        
        return {
          type: plugin.nodeType,
          label: plugin.nodeName,
          desc: plugin.description || '插件节点',
          icon: plugin.icon || 'Box',
          color: '#6366f1',
          configTemplate: plugin.configTemplate
        }
      })
      
      const pluginGroup = {
        name: '插件节点',
        nodes: pluginNodes
      }
      
      nodeGroups.value = [...baseNodeGroups, pluginGroup]
      
      if (lf) {
        pluginNodes.forEach(node => {
          const NodeModel = class extends RectNodeModel {
            initNodeData(data) {
              super.initNodeData(data)
              this.width = 148
              this.height = 56
            }
            getNodeStyle() {
              const style = super.getNodeStyle()
              style.fill = 'transparent'
              style.stroke = 'transparent'
              style.strokeWidth = 0
              return style
            }
            getTextStyle() {
              const style = super.getTextStyle()
              style.color = '#334155'
              style.fontSize = 13
              style.fontWeight = 600
              return style
            }
          }

          const NodeView = class extends RectNode {
            getShape() {
              const { model } = this.props
              const { x, y, width, height, properties, isSelected } = model
              const color = properties.color || node.color
              const rx = 12
              const ry = 12
              const barW = 3
              const pad = 2

              return h('g', {}, [
                h('rect', {
                  x: x - width / 2,
                  y: y - height / 2,
                  width: width,
                  height: height,
                  rx,
                  ry,
                  fill: '#ffffff',
                  stroke: isSelected ? color + '60' : '#e2e8f0',
                  strokeWidth: isSelected ? 1.5 : 1
                }),
                h('rect', {
                  x: x - width / 2 + pad,
                  y: y - height / 2 + pad + 10,
                  width: barW,
                  height: height - pad * 2 - 20,
                  rx: barW / 2,
                  fill: color
                }),
                h('circle', {
                  cx: x - width / 2 + 14,
                  cy: y - height / 2 + 14,
                  r: 3,
                  fill: color,
                  opacity: 0.9
                })
              ])
            }
          }

          try {
            lf.register({ type: node.type, model: NodeModel, view: NodeView })
          } catch (e) {
            console.log('节点已注册:', node.type)
          }
        })
      }
    }
  } catch (error) {
    console.error('加载插件节点失败', error)
  }
}

const isPluginNode = (nodeType) => {
  if (!nodeType) return false
  const builtInTypes = ['start', 'end', 'api', 'db', 'script', 'condition', 'timer']
  return !builtInTypes.includes(nodeType)
}

const getPluginFields = (nodeType) => {
  const schema = pluginSchemas.value[nodeType]
  if (!schema || !schema.fields) {
    return []
  }
  return schema.fields
}

const getPluginOutputFields = (nodeType) => {
  const schema = pluginOutputSchemas.value[nodeType]
  if (!schema || !schema.fields) {
    return []
  }
  return schema.fields
}

// 注册 LogicFlow 扩展
LogicFlow.use(Menu)
LogicFlow.use(SelectionSelect)

function initLogicFlow() {
  if (!canvasRef.value) return

  lf = new LogicFlow({
    container: canvasRef.value,
    grid: {
      type: 'dot',
      size: 16,
      config: { color: 'rgba(148, 163, 184, 0.12)' }
    },
    keyboard: { enabled: true },
    snapline: true,
    stopScrollGraph: true,
    stopZoomGraph: false,
    metaKeyMultipleSelected: true,
    edgeType: 'bezier',
    style: {
      rect: { rx: 12, ry: 12 },
      circle: { r: 30 },
      diamond: { rx: 10, ry: 10 },
      bezier: { stroke: '#475569', strokeWidth: 2 },
      polyline: { stroke: '#475569', strokeWidth: 2 },
      line: { stroke: '#475569', strokeWidth: 2 },
      anchor: { r: 5, fill: '#3b82f6', stroke: '#ffffff', strokeWidth: 2 },
      outline: { stroke: '#3b82f6', strokeWidth: 2, strokeDasharray: '6 4' }
    },
    edgeText: {
      color: '#94a3b8',
      fontSize: 12,
      background: { fill: '#0B0D10', stroke: '#1e293b', strokeWidth: 1, rx: 6, ry: 6 }
    }
  })

  // 注册自定义节点
  registerCustomNodes()

  // 事件监听
  lf.on('element:click', async ({ data }) => {
    const edgeTypes = ['polyline', 'line', 'bezier']
    if (edgeTypes.includes(data.type)) {
      selectedNode.value = null
      selectedEdge.value = data
      currentApiParams.value = []
      currentResponseParams.value = []
      allApiParams.value = []
    } else {
      selectedEdge.value = null
      selectedNode.value = data
      // 从 properties 读取输入/输出映射（支持数组或JSON字符串）
      const rawInput = data.properties?.inputMapping
      const rawOutput = data.properties?.outputMapping
      try {
        inputMappings.value = rawInput ? (typeof rawInput === 'string' ? JSON.parse(rawInput) : rawInput) : []
      } catch (e) { inputMappings.value = [] }
      try {
        outputMappings.value = rawOutput ? (typeof rawOutput === 'string' ? JSON.parse(rawOutput) : rawOutput) : []
      } catch (e) { outputMappings.value = [] }
      // 切换节点时清空已解析的字段列表
      parsedColumns.value = []
      // 加载 API 参数定义
      currentApiParams.value = []
      currentResponseParams.value = []
      allApiParams.value = []
      if (data.type === 'api' && data.properties?.apiCode) {
        await loadApiParamsByCode(data.properties.apiCode)
        syncApiInputMappings()
      }
      // end 节点：自动为流程出参创建默认输入映射
      if (data.type === 'end' && filteredFlowOutputParams.value.length > 0) {
        for (const param of filteredFlowOutputParams.value) {
          const targetKey = param.value.replace(/^context\./, '')
          const source = param.value
          if (!inputMappings.value.some(m => m.target === targetKey)) {
            inputMappings.value.push({ source, target: targetKey })
          }
        }
        updateNodeProperties()
      }
      // Script 节点：从 outputMapping 反推字段声明
      scriptOutputFields.value = ''
      if (data.type === 'script' && outputMappings.value.length > 0) {
        const fields = outputMappings.value
          .filter(m => m.source && m.source.startsWith('result.'))
          .map(m => m.source.substring(7))
        scriptOutputFields.value = fields.join(',')
      }
    }
  })

  lf.on('blank:click', () => {
    selectedNode.value = null
    selectedEdge.value = null
    parsedColumns.value = []
    currentApiParams.value = []
    currentResponseParams.value = []
    allApiParams.value = []
  })

  lf.on('node:delete', (eventData) => {
    selectedNode.value = null
    selectedEdge.value = null
    parsedColumns.value = []
    currentResponseParams.value = []

    if (isBatchDeletingLoop) return
    const node = eventData?.data
    if (!node) return
    const type = node.type
    if (['foreach', 'while', 'end_foreach', 'end_while'].includes(type)) {
      const graphData = lf.getGraphData()
      let startId
      let endId
      if (type === 'foreach' || type === 'while') {
        startId = node.id
        const expectedEndType = type === 'foreach' ? 'end_foreach' : 'end_while'
        const endNode = graphData.nodes.find(n =>
          n.type === expectedEndType && n.properties?.loopNodeId === startId
        )
        endId = endNode?.id
      } else {
        endId = node.id
        startId = node.properties?.loopNodeId
      }
      if (startId && endId) {
        // 删除循环边界节点时，级联删除整个循环体
        deleteLoopContainer(startId)
      }
    }
  })

  lf.on('edge:delete', () => {
    selectedNode.value = null
    selectedEdge.value = null
    parsedColumns.value = []
    currentResponseParams.value = []
  })

  lf.on('connection:not-allowed', (data) => {
    ElMessage.warning(data.msg || '不允许的连线')
  })

  // 自定义右键菜单：循环边界节点不显示“删除”项，防止误删半个循环
  const defaultNodeMenu = [
    { text: '删除', className: 'lf-menu-delete-item', callback: (node) => lf.deleteNode(node.id) },
    { text: '编辑文本', callback: (node) => lf.graphModel.editText(node.id) },
    { text: '复制', callback: (node) => lf.cloneNode(node.id) }
  ]
  const loopBoundaryNodeMenu = [
    { text: '编辑文本', callback: (node) => lf.graphModel.editText(node.id) },
    { text: '复制', callback: (node) => lf.cloneNode(node.id) }
  ]
  const loopBoundaryTypes = ['foreach', 'while', 'end_foreach', 'end_while']

  function updateMenuByNodeType(nodeType) {
    const isLoopBoundary = loopBoundaryTypes.includes(nodeType)
    lf.setMenuConfig({ nodeMenu: isLoopBoundary ? loopBoundaryNodeMenu : defaultNodeMenu })
  }

  function setMenuClassByNodeType(nodeType) {
    const isLoopBoundary = loopBoundaryTypes.includes(nodeType)
    requestAnimationFrame(() => {
      const menu = document.querySelector('.lf-menu')
      if (!menu) return
      if (isLoopBoundary) menu.classList.add('loop-boundary-node')
      else menu.classList.remove('loop-boundary-node')
    })
  }

  lf.on('node:mousedown', (data) => updateMenuByNodeType(data?.data?.type))
  lf.on('node:contextmenu', (data) => {
    updateMenuByNodeType(data?.data?.type)
    setMenuClassByNodeType(data?.data?.type)
  })
  lf.on('blank:mousedown', () => updateMenuByNodeType(null))
  lf.on('blank:contextmenu', () => {
    updateMenuByNodeType(null)
    setMenuClassByNodeType(null)
  })

  // 监听画布变化，重新渲染循环容器框
  const loopContainerEvents = ['node:add', 'node:delete', 'node:drag', 'node:drop', 'node:mousemove', 'edge:add', 'edge:delete', 'graph:transform']
  loopContainerEvents.forEach(eventName => {
    lf.on(eventName, () => {
      requestAnimationFrame(renderLoopContainers)
    })
  })

  loadPluginNodes().then(() => {
    console.log('[Designer] init complete', { flowId: flowId.value })
    if (flowId.value) {
      loadFlowData()
    } else {
      console.log('[Designer] render default nodes')
      lf.render({
        nodes: [
          { id: 'start_1', type: 'start', x: 240, y: 320, text: '开始', properties: { name: '开始', code: 'start_1' } },
          { id: 'end_1', type: 'end', x: 640, y: 320, text: '结束', properties: { name: '结束', code: 'end_1' } }
        ],
        edges: []
      })
    }
  })
}

function registerCustomNodes() {
  const nodeConfig = {
    start:  { color: '#22c55e', label: '开始',  w: 140, h: 52 },
    end:    { color: '#ef4444', label: '结束',  w: 140, h: 52 },
    api:    { color: '#3b82f6', label: '接口',  w: 148, h: 56 },
    db:     { color: '#8b5cf6', label: '数据库', w: 148, h: 56 },
    script: { color: '#ec4899', label: '脚本',  w: 148, h: 56 },
    condition: { color: '#f59e0b', label: '条件', w: 148, h: 56 },
    timer:  { color: '#06b6d4', label: '定时',  w: 148, h: 56 },
    foreach: { color: '#8b5cf6', label: '循环', w: 148, h: 56 },
    end_foreach: { color: '#a78bfa', label: '循环结束', w: 148, h: 56 },
    while: { color: '#06b6d4', label: '条件循环', w: 148, h: 56 },
    end_while: { color: '#22d3ee', label: '条件循环结束', w: 148, h: 56 }
  }

  Object.entries(nodeConfig).forEach(([type, cfg]) => {
    const NodeModel = class extends RectNodeModel {
      initNodeData(data) {
        super.initNodeData(data)
        this.width = cfg.w
        this.height = cfg.h
      }
      getNodeStyle() {
        const style = super.getNodeStyle()
        style.fill = 'transparent'
        style.stroke = 'transparent'
        style.strokeWidth = 0
        return style
      }
      getTextStyle() {
        const style = super.getTextStyle()
        style.color = '#334155'
        style.fontSize = 13
        style.fontWeight = 600
        return style
      }
      getOutlineStyle() {
        const style = super.getOutlineStyle()
        style.stroke = '#3b82f6'
        style.strokeWidth = 2
        style.strokeDasharray = '6 4'
        return style
      }
    }

    const NodeView = class extends RectNode {
      getShape() {
        const { model } = this.props
        const { x, y, width, height, properties, isSelected } = model
        const color = properties.color || cfg.color
        const rx = 12
        const ry = 12
        const barW = 3
        const pad = 2

        // 发光滤镜
        const glowFilter = `drop-shadow(0 0 ${isSelected ? 10 : 4}px ${color}35)`
        const shadowFilter = 'drop-shadow(0 2px 10px rgba(0,0,0,0.08))'

        return h('g', {}, [
          // 1. 外发光层（选中时更强）
          h('rect', {
            x: x - width / 2 - 2,
            y: y - height / 2 - 2,
            width: width + 4,
            height: height + 4,
            rx: rx + 2,
            ry: ry + 2,
            fill: 'none',
            stroke: isSelected ? color : 'transparent',
            strokeWidth: isSelected ? 2 : 0,
            opacity: isSelected ? 0.6 : 0,
            filter: glowFilter
          }),
          // 2. 主体深色卡片 + 阴影
          h('rect', {
            x: x - width / 2,
            y: y - height / 2,
            width: width,
            height: height,
            rx,
            ry,
            fill: '#ffffff',
            stroke: isSelected ? color + '60' : '#e2e8f0',
            strokeWidth: isSelected ? 1.5 : 1,
            filter: shadowFilter
          }),
          // 3. 顶部渐变高光（模拟玻璃反光）
          h('rect', {
            x: x - width / 2 + pad,
            y: y - height / 2 + pad,
            width: width - pad * 2,
            height: (height - pad * 2) * 0.35,
            rx: rx - pad,
            ry: ry - pad,
            fill: 'url(#glassHighlight)',
            opacity: 0.04
          }),
          // 4. 左侧彩色竖条
          h('rect', {
            x: x - width / 2 + pad,
            y: y - height / 2 + pad + 10,
            width: barW,
            height: height - pad * 2 - 20,
            rx: barW / 2,
            fill: color
          }),
          // 5. 状态指示点
          h('circle', {
            cx: x - width / 2 + 14,
            cy: y - height / 2 + 14,
            r: 3,
            fill: color,
            opacity: 0.9
          })
        ])
      }

      // 添加 defs（全局只需要一次，但LogicFlow会在每个节点调用）
      getAttributes() {
        const attrs = super.getAttributes()
        return attrs
      }
    }

    lf.register({ type, model: NodeModel, view: NodeView })
  })

  // 注册贝塞尔曲线边，启用拖拽调整控制点
  lf.register({
    type: 'bezier',
    view: BezierEdge,
    model: class extends BezierEdgeModel {
      setAttributes() {
        this.isShowAdjustPoint = true
      }
      getAdjustPointStyle() {
        const style = super.getAdjustPointStyle()
        style.fill = '#3b82f6'
        style.stroke = '#ffffff'
        style.strokeWidth = 2
        return style
      }
    }
  })

  // 添加全局渐变定义
  const svg = lf.container.querySelector('svg')
  if (svg && !svg.querySelector('#glassHighlight')) {
    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs')
    const grad = document.createElementNS('http://www.w3.org/2000/svg', 'linearGradient')
    grad.setAttribute('id', 'glassHighlight')
    grad.setAttribute('x1', '0')
    grad.setAttribute('y1', '0')
    grad.setAttribute('x2', '0')
    grad.setAttribute('y2', '1')
    const stop1 = document.createElementNS('http://www.w3.org/2000/svg', 'stop')
    stop1.setAttribute('offset', '0%')
    stop1.setAttribute('stop-color', '#ffffff')
    const stop2 = document.createElementNS('http://www.w3.org/2000/svg', 'stop')
    stop2.setAttribute('offset', '100%')
    stop2.setAttribute('stop-color', '#ffffff')
    stop2.setAttribute('stop-opacity', '0')
    grad.appendChild(stop1)
    grad.appendChild(stop2)
    defs.appendChild(grad)
    svg.prepend(defs)
  }
}

function handleDragStart(e, node) {
  console.log('[Designer] dragstart', { executionMode: flowExecutionMode.value, nodeType: node.type })
  if (flowExecutionMode.value === 'SYNC' && node.type === 'timer') {
    ElMessage.warning('同步流程不支持定时(timer)节点')
    e.preventDefault()
    return
  }
  const properties = { 
    name: node.label, 
    code: `${node.type}_${Date.now()}` 
  }
  
  if (isPluginNode(node.type)) {
    const schema = pluginSchemas.value[node.type]
    if (schema && schema.fields) {
      schema.fields.forEach(field => {
        if (field.defaultValue !== undefined) {
          properties[field.name] = field.defaultValue
        }
      })
    }
  }
  
  e.dataTransfer.setData('application/lf-node', JSON.stringify({
    type: node.type,
    text: node.label,
    properties
  }))
}

function handleDrop(e) {
  const dataStr = e.dataTransfer.getData('application/lf-node')
  console.log('[Designer] drop', { dataStr, hasLf: !!lf, executionMode: flowExecutionMode.value })
  if (!dataStr || !lf) {
    console.warn('[Designer] drop skipped', { reason: !dataStr ? 'no dataStr' : 'no lf' })
    return
  }
  try {
    const data = JSON.parse(dataStr)
    console.log('[Designer] drop parsed', { type: data.type, text: data.text, properties: data.properties })
    if (flowExecutionMode.value === 'SYNC' && data.type === 'timer') {
      ElMessage.warning('同步流程不支持定时(timer)节点')
      return
    }
    const rect = canvasRef.value.getBoundingClientRect()
    const transform = lf.getTransform ? lf.getTransform() : { translateX: 0, translateY: 0 }
    const tx = transform?.translateX || 0
    const ty = transform?.translateY || 0
    const x = e.clientX - rect.left + tx
    const y = e.clientY - rect.top + ty

    // 循环节点成对插入
    if (data.type === 'foreach' || data.type === 'while') {
      addLoopNodePair(data.type, x, y, data.text)
      return
    }

    console.log('[Designer] addNode', { type: data.type, x, y, rect: { left: rect.left, top: rect.top }, client: { x: e.clientX, y: e.clientY }, transform: { tx, ty } })
    lf.addNode({
      type: data.type,
      x,
      y,
      text: data.text,
      properties: data.properties
    })
    console.log('[Designer] addNode success')
  } catch (err) {
    console.error('[Designer] 添加节点失败', err)
  }
}

/**
 * 成对插入循环节点
 */
function addLoopNodePair(loopType, x, y, text) {
  const startId = `${loopType}_${Date.now()}`
  const endId = `end_${loopType}_${Date.now()}`
  const endType = loopType === 'foreach' ? 'end_foreach' : 'end_while'
  const endText = loopType === 'foreach' ? '循环结束' : '条件循环结束'

  const startProperties = {
    name: text,
    code: startId,
    loopType: loopType,
    sourceExpr: loopType === 'foreach' ? '' : undefined,
    conditionExpr: loopType === 'while' ? '' : undefined,
    itemVar: 'item',
    indexVar: 'index',
    resultVar: `loopResult_${startId}`,
    maxIterations: 100,
    timeout: 30000,
    parallel: false,
    parallelLimit: 5,
    continueOnFail: false,
    emptyAction: 'skip'
  }

  const endProperties = {
    name: endText,
    code: endId,
    loopNodeId: startId,
    aggregateExpr: 'context.item',
    outputMapping: '[]'
  }

  lf.addNode({
    id: startId,
    type: loopType,
    x: x - 120,
    y,
    text,
    properties: startProperties
  })

  lf.addNode({
    id: endId,
    type: endType,
    x: x + 120,
    y,
    text: endText,
    properties: endProperties
  })

  lf.addEdge({
    type: 'bezier',
    sourceNodeId: startId,
    targetNodeId: endId,
    properties: {
      conditionType: 'default',
      isHidden: 1
    }
  })

  console.log('[Designer] addLoopNodePair success', { startId, endId })
}

/**
 * 渲染循环容器视觉框
 * 通过 lf.getTransform() 手动把逻辑坐标转成屏幕坐标，使框在滚轮缩放/平移时跟随节点。
 */
function renderLoopContainers() {
  if (!lf) return
  try {
    const graphData = lf.getGraphData()
    const svg = lf.container?.querySelector('svg')
    if (!svg) return

    // 获取画布当前的缩放与平移，用于把逻辑坐标映射到屏幕坐标
    const transform = lf.getTransform() || {}
    const scaleX = transform.SCALE_X ?? 1
    const scaleY = transform.SCALE_Y ?? 1
    const translateX = transform.TRANSLATE_X ?? 0
    const translateY = transform.TRANSLATE_Y ?? 0

    // 容器组直接挂在最外层 SVG 下，通过手动计算屏幕坐标跟随缩放
    let containerGroup = svg.querySelector('#loop-containers')
    if (!containerGroup) {
      containerGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g')
      containerGroup.setAttribute('id', 'loop-containers')
      if (svg.firstChild) {
        svg.insertBefore(containerGroup, svg.firstChild)
      } else {
        svg.appendChild(containerGroup)
      }
    }

    containerGroup.innerHTML = ''

    const pairs = findLoopPairs(graphData)
    const nodeMap = {}
    if (graphData?.nodes) {
      for (const node of graphData.nodes) {
        nodeMap[node.id] = node
      }
    }

    // 计算嵌套层级
    const levels = computeLoopLevels(pairs, graphData)

    for (const pair of pairs) {
      const [startId, endId] = pair
      const startNode = nodeMap[startId]
      if (!startNode) continue

      const bounds = computeLoopBounds(startId, endId, graphData)
      if (!bounds) continue

      // 逻辑坐标 -> 屏幕坐标
      const screenX = bounds.x * scaleX + translateX
      const screenY = bounds.y * scaleY + translateY
      const screenW = bounds.width * scaleX
      const screenH = bounds.height * scaleY

      const level = levels.get(startId) || 0
      const isForeach = startNode.type === 'foreach'
      const baseColor = isForeach ? '139, 92, 246' : '6, 182, 212'
      const labelText = isForeach ? '↻ foreach' : '⟳ while'
      const fillOpacity = Math.max(0.02, 0.06 - level * 0.015)

      const group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
      group.setAttribute('class', 'loop-container')
      group.setAttribute('data-loop-node-id', startId)

      // 容器背景矩形
      const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
      rect.setAttribute('x', screenX)
      rect.setAttribute('y', screenY)
      rect.setAttribute('width', screenW)
      rect.setAttribute('height', screenH)
      rect.setAttribute('rx', 16)
      rect.setAttribute('fill', `rgba(${baseColor}, ${fillOpacity})`)
      rect.setAttribute('stroke', `rgba(${baseColor}, 0.35)`)
      rect.setAttribute('stroke-width', 1.5)
      rect.setAttribute('stroke-dasharray', '4 4')
      group.appendChild(rect)

      // 左上角标签背景
      const name = startNode.text?.value || startNode.properties?.name || '循环'
      const labelContent = `${labelText} · ${name}`
      const textPaddingX = 10
      const textPaddingY = 5
      const fontSize = 12
      const approxWidth = labelContent.length * fontSize * 0.6 + textPaddingX * 2
      const labelHeight = fontSize + textPaddingY * 2

      const labelRect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
      labelRect.setAttribute('x', screenX + 12)
      labelRect.setAttribute('y', screenY + 8)
      labelRect.setAttribute('width', approxWidth)
      labelRect.setAttribute('height', labelHeight)
      labelRect.setAttribute('rx', 6)
      labelRect.setAttribute('fill', `rgba(${baseColor}, 0.12)`)
      labelRect.setAttribute('stroke', `rgba(${baseColor}, 0.25)`)
      labelRect.setAttribute('stroke-width', 1)
      group.appendChild(labelRect)

      // 标签文字
      const text = document.createElementNS('http://www.w3.org/2000/svg', 'text')
      text.setAttribute('x', screenX + 12 + textPaddingX)
      text.setAttribute('y', screenY + 8 + labelHeight - textPaddingY - 1)
      text.setAttribute('fill', `rgba(${baseColor}, 0.9)`)
      text.setAttribute('font-size', fontSize)
      text.setAttribute('font-weight', 600)
      text.textContent = labelContent
      group.appendChild(text)

      // 右上角删除按钮：点击删除整个循环体
      const btnSize = 16
      const btnX = screenX + screenW - btnSize - 10
      const btnY = screenY + 8
      const btnGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g')
      btnGroup.setAttribute('class', 'loop-delete-btn')
      btnGroup.style.cursor = 'pointer'
      btnGroup.addEventListener('click', (e) => {
        e.stopPropagation()
        deleteLoopContainer(startId)
      })

      const btnCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle')
      btnCircle.setAttribute('cx', btnX + btnSize / 2)
      btnCircle.setAttribute('cy', btnY + btnSize / 2)
      btnCircle.setAttribute('r', btnSize / 2)
      btnCircle.setAttribute('fill', `rgba(${baseColor}, 0.12)`)
      btnCircle.setAttribute('stroke', `rgba(${baseColor}, 0.35)`)
      btnCircle.setAttribute('stroke-width', 1)
      btnGroup.appendChild(btnCircle)

      // 垃圾桶图标（16x16 viewBox，描边风格，中间带两条镂空纹理）
      const trashColor = `rgba(${baseColor}, 0.9)`
      const iconScale = btnSize / 16
      const trashGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g')
      trashGroup.setAttribute('transform', `translate(${btnX}, ${btnY}) scale(${iconScale})`)
      trashGroup.setAttribute('fill', 'none')
      trashGroup.setAttribute('stroke', trashColor)
      trashGroup.setAttribute('stroke-width', '1.2')
      trashGroup.setAttribute('stroke-linecap', 'round')
      trashGroup.setAttribute('stroke-linejoin', 'round')

      function addTrashLine(x1, y1, x2, y2, width = 1) {
        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line')
        line.setAttribute('x1', x1)
        line.setAttribute('y1', y1)
        line.setAttribute('x2', x2)
        line.setAttribute('y2', y2)
        line.setAttribute('stroke-width', width)
        trashGroup.appendChild(line)
      }
      function addTrashRect(x, y, w, h, rx = 0) {
        const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect')
        rect.setAttribute('x', x)
        rect.setAttribute('y', y)
        rect.setAttribute('width', w)
        rect.setAttribute('height', h)
        rect.setAttribute('rx', rx)
        trashGroup.appendChild(rect)
      }

      addTrashRect(6, 2, 4, 2, 0.5)      // 提手
      addTrashLine(2.5, 5.5, 13.5, 5.5, 1.2) // 桶盖
      addTrashRect(4, 6.5, 8, 8, 1)      // 桶身外框
      addTrashLine(6.5, 9, 6.5, 12)      // 左纹理
      addTrashLine(9.5, 9, 9.5, 12)      // 右纹理

      btnGroup.appendChild(trashGroup)
      group.appendChild(btnGroup)

      containerGroup.appendChild(group)
    }
  } catch (e) {
    console.warn('渲染循环容器失败', e)
  }
}

function findLoopPairs(graphData) {
  const pairs = []
  if (!graphData?.nodes) return pairs
  const nodes = graphData.nodes
  for (const node of nodes) {
    if (node.type === 'foreach' || node.type === 'while') {
      const expectedEndType = node.type === 'foreach' ? 'end_foreach' : 'end_while'
      const endNode = nodes.find(n =>
        n.type === expectedEndType && n.properties?.loopNodeId === node.id
      )
      if (endNode) {
        pairs.push([node.id, endNode.id])
      }
    }
  }
  return pairs
}

function computeLoopBounds(startId, endId, graphData) {
  const nodes = graphData?.nodes || []
  const edges = graphData?.edges || []
  const nodeMap = {}
  for (const node of nodes) {
    nodeMap[node.id] = node
  }

  // BFS 查找循环体节点
  const bodyIds = new Set()
  const queue = [startId]
  const visited = new Set([startId])
  while (queue.length > 0) {
    const current = queue.shift()
    for (const edge of edges) {
      if (edge.sourceNodeId === current) {
        const target = edge.targetNodeId
        if (target === endId) continue
        if (!visited.has(target)) {
          visited.add(target)
          bodyIds.add(target)
          queue.push(target)
        }
      }
    }
  }
  bodyIds.add(startId)
  bodyIds.add(endId)

  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  for (const nodeId of bodyIds) {
    const node = nodeMap[nodeId]
    if (!node || node.x == null || node.y == null) continue
    const w = node.width || 148
    const h = node.height || 56
    minX = Math.min(minX, node.x - w / 2)
    minY = Math.min(minY, node.y - h / 2)
    maxX = Math.max(maxX, node.x + w / 2)
    maxY = Math.max(maxY, node.y + h / 2)
  }

  if (!isFinite(minX)) return null

  const padding = 24
  // 顶部多预留空间，避免左上角标签与循环开始节点重叠
  const topPadding = 44
  let width = maxX - minX + padding * 2
  let height = maxY - minY + topPadding + padding
  let x = minX - padding
  let y = minY - topPadding

  // 最小尺寸保护
  const minWidth = 220
  const minHeight = 100
  if (width < minWidth) {
    x -= (minWidth - width) / 2
    width = minWidth
  }
  if (height < minHeight) {
    y -= (minHeight - height) / 2
    height = minHeight
  }

  return { x, y, width, height }
}

/**
 * 计算每个循环的嵌套层级
 */
function computeLoopLevels(pairs, graphData) {
  const levels = new Map()
  const nodeMap = {}
  if (graphData?.nodes) {
    for (const node of graphData.nodes) {
      nodeMap[node.id] = node
    }
  }
  for (let i = 0; i < pairs.length; i++) {
    const [startId, endId] = pairs[i]
    const bounds = computeLoopBounds(startId, endId, graphData)
    if (!bounds) {
      levels.set(startId, 0)
      continue
    }
    let level = 0
    for (let j = 0; j < pairs.length; j++) {
      if (i === j) continue
      const [otherStartId, otherEndId] = pairs[j]
      const otherStart = nodeMap[otherStartId]
      const otherEnd = nodeMap[otherEndId]
      if (otherStart && otherEnd
          && otherStart.x >= bounds.x && otherStart.x <= bounds.x + bounds.width
          && otherStart.y >= bounds.y && otherStart.y <= bounds.y + bounds.height
          && otherEnd.x >= bounds.x && otherEnd.x <= bounds.x + bounds.width
          && otherEnd.y >= bounds.y && otherEnd.y <= bounds.y + bounds.height) {
        level++
      }
    }
    levels.set(startId, level)
  }
  return levels
}

/**
 * 查找循环体内部节点（从 start 到 end 的路径上的节点，不含 start/end）
 */
function findLoopBodyNodeIds(startId, endId, graphData) {
  const edges = graphData?.edges || []
  const bodyIds = new Set()
  const queue = [startId]
  const visited = new Set([startId])
  while (queue.length > 0) {
    const current = queue.shift()
    for (const edge of edges) {
      if (edge.sourceNodeId === current) {
        const target = edge.targetNodeId
        if (target === endId) continue
        if (!visited.has(target)) {
          visited.add(target)
          bodyIds.add(target)
          queue.push(target)
        }
      }
    }
  }
  return bodyIds
}

/**
 * 删除整个循环体（开始节点、结束节点、循环体内部节点）
 */
function deleteLoopContainer(startId) {
  if (!lf || isBatchDeletingLoop) return
  const graphData = lf.getGraphData()
  const pairs = findLoopPairs(graphData)
  const pair = pairs.find(p => p[0] === startId)
  if (!pair) return
  const endId = pair[1]
  const bodyIds = findLoopBodyNodeIds(startId, endId, graphData)
  const idsToDelete = new Set([startId, endId, ...bodyIds])

  isBatchDeletingLoop = true
  try {
    idsToDelete.forEach(id => {
      try {
        lf.deleteNode(id)
      } catch (e) {
        // 节点可能已被删除，忽略
      }
    })
  } finally {
    isBatchDeletingLoop = false
    requestAnimationFrame(renderLoopContainers)
  }
}

function updateNodeProperties() {
  if (selectedNode.value && lf) {
    // 将输入/输出映射同步回 properties（后端以JSON字符串存储）
    selectedNode.value.properties.inputMapping = JSON.stringify(inputMappings.value)
    selectedNode.value.properties.outputMapping = JSON.stringify(outputMappings.value)
    lf.setProperties(selectedNode.value.id, selectedNode.value.properties)
    // 同步节点显示文本
    const text = selectedNode.value.properties.name || selectedNode.value.text?.value || selectedNode.value.text
    if (text) {
      lf.updateText(selectedNode.value.id, text)
    }
    // 强制更新引用，触发 computed（如 availableVariables）重新计算
    selectedNode.value = { ...selectedNode.value }
  }
}

function updateEdgeProperties() {
  if (selectedEdge.value && lf) {
    lf.setProperties(selectedEdge.value.id, selectedEdge.value.properties)
  }
}

// ==================== 输入/输出映射 & SQL字段解析 ====================

async function parseSqlColumns() {
  if (!selectedNode.value?.properties?.sql) {
    ElMessage.warning('请先输入SQL语句')
    return
  }
  const sql = selectedNode.value.properties.sql.trim()
  if (!sql.toLowerCase().startsWith('select')) {
    ElMessage.warning('仅支持解析 SELECT 语句')
    return
  }
  parsingColumns.value = true
  try {
    const res = await request({
      url: '/workflow/node/parse-sql-columns',
      method: 'post',
      data: {
        dsCode: selectedNode.value.properties.dsCode || 'master',
        sql: sql
      }
    })
    parsedColumns.value = res || []
    if (parsedColumns.value.length === 0) {
      ElMessage.info('未解析到字段，请检查SQL语法')
    } else {
      ElMessage.success(`解析到 ${parsedColumns.value.length} 个字段`)
    }
  } catch (e) {
    ElMessage.error('字段解析失败: ' + (e.message || e))
  } finally {
    parsingColumns.value = false
  }
}

function addOutputMappingFromColumn(col) {
  const sourcePath = `result.data[0].${col.name}`
  const exists = outputMappings.value.some(m => m.source === sourcePath)
  if (exists) {
    ElMessage.warning('该字段已在输出映射中')
    return
  }
  outputMappings.value.push({ source: sourcePath, target: '' })
  updateNodeProperties()
  ElMessage.success(`已添加字段: ${col.name}`)
}

function addAllColumnsToOutput() {
  let added = 0
  for (const col of parsedColumns.value) {
    const sourcePath = `result.data[0].${col.name}`
    const exists = outputMappings.value.some(m => m.source === sourcePath)
    if (!exists) {
      outputMappings.value.push({ source: sourcePath, target: '' })
      added++
    }
  }
  updateNodeProperties()
  ElMessage.success(`已批量添加 ${added} 个字段至输出映射`)
}

function addInputMapping() { inputMappings.value.push({ source: '', target: '', type: 'var' }) }
function removeInputMapping(idx) { inputMappings.value.splice(idx, 1); updateNodeProperties() }
function removeCustomInputMapping(target) {
  const idx = inputMappings.value.findIndex(m => m.target === target)
  if (idx > -1) {
    inputMappings.value.splice(idx, 1)
    updateNodeProperties()
  }
}
function addOutputMapping() { outputMappings.value.push({ source: '', target: '' }) }
function removeOutputMapping(idx) { outputMappings.value.splice(idx, 1); updateNodeProperties() }

// 脚本节点：根据字段声明自动生成输出映射
function generateScriptOutputMappings() {
  if (!selectedNode.value || selectedNode.value.type !== 'script') return
  const fields = scriptOutputFields.value.split(',').map(f => f.trim()).filter(f => f)
  if (fields.length === 0) return
  let added = 0
  for (const field of fields) {
    const source = `result.${field}`
    const target = `context.${field}`
    if (!outputMappings.value.some(m => m.source === source)) {
      outputMappings.value.push({ source, target })
      added++
    }
  }
  if (added > 0) {
    updateNodeProperties()
    ElMessage.success(`已自动生成 ${added} 条输出映射`)
  }
}

// API 参数相关辅助函数
function getApiTarget(param) {
  // 支持嵌套 body 参数，如 body.params.a0188
  if (param.paramType === 'body' && param.parentId && param.parentId != 0) {
    const parts = [param.paramType]
    function buildPath(p) {
      if (p.parentId && p.parentId != 0) {
        const parent = allApiParams.value.find(ap => ap.id === p.parentId)
        if (parent) {
          buildPath(parent)
        }
      }
      parts.push(p.paramKey)
    }
    buildPath(param)
    return parts.join('.')
  }
  return `${param.paramType}.${param.paramKey}`
}

function getApiParamDisplayKey(param) {
  // 嵌套参数显示完整路径，如 params.a0188
  if (param.paramType === 'body' && param.parentId && param.parentId != 0) {
    const parent = allApiParams.value.find(ap => ap.id === param.parentId)
    if (parent) {
      return `${parent.paramKey}.${param.paramKey}`
    }
  }
  return param.paramKey
}

function syncApiInputMappings() {
  for (const param of currentApiParams.value) {
    const target = getApiTarget(param)
    const oldTarget = `${param.paramType}.${param.paramKey}`
    // 强制清理旧格式的扁平 target（如 body.a0188），无论新 target 是否已存在
    if (oldTarget !== target) {
      const oldIdx = inputMappings.value.findIndex(m => m.target === oldTarget)
      if (oldIdx >= 0) {
        // 如果新 target 还不存在，把旧 source 迁移过去
        if (!inputMappings.value.some(m => m.target === target)) {
          const oldMap = inputMappings.value[oldIdx]
          inputMappings.value.push({ source: oldMap.source, target, type: oldMap.type })
        }
        // 删除旧映射
        inputMappings.value.splice(oldIdx, 1)
      }
    }
    if (!inputMappings.value.some(m => m.target === target)) {
      inputMappings.value.push({ source: '', target })
    }
  }
}

const getApiOutputTarget = (paramKey) => {
  const map = outputMappings.value.find(m => m.source === `body.${paramKey}`)
  return map ? map.target : `context.${paramKey}`
}

const setApiOutputTarget = (paramKey, target) => {
  const source = `body.${paramKey}`
  const idx = outputMappings.value.findIndex(m => m.source === source)
  const finalTarget = target && target.trim() ? target.trim() : `context.${paramKey}`
  if (idx >= 0) {
    outputMappings.value[idx].target = finalTarget
  } else {
    outputMappings.value.push({ source, target: finalTarget })
  }
  updateNodeProperties()
}

// end 节点流程出参绑定
const getEndParamSource = (targetKey) => {
  const map = inputMappings.value.find(m => m.target === targetKey)
  return map ? map.source : `context.${targetKey}`
}

const setEndParamSource = (targetKey, source) => {
  const map = inputMappings.value.find(m => m.target === targetKey)
  if (map) {
    map.source = source && source.trim() ? source.trim() : `context.${targetKey}`
    updateNodeProperties()
  }
}

function getEndParamMappingIndex(targetKey) {
  let idx = inputMappings.value.findIndex(m => m.target === targetKey)
  if (idx === -1) {
    inputMappings.value.push({ source: `context.${targetKey}`, target: targetKey })
    idx = inputMappings.value.length - 1
    updateNodeProperties()
  }
  return idx
}

async function loadApiParamsByCode(apiCode) {
  // 如果接口目录未加载，先兜底加载（避免设计器刚打开时点击节点无数据）
  if (!apiCatalogOptions.value || apiCatalogOptions.value.length === 0) {
    try {
      await loadApiCatalogOptions()
    } catch (e) {
      console.warn('加载接口目录失败', e)
    }
  }
  const api = apiCatalogOptions.value.find(a => a.apiCode === apiCode)
  if (api?.id) {
    try {
      const params = await getApiParams(api.id)
      allApiParams.value = Array.isArray(params) ? params : []
      currentApiParams.value = allApiParams.value.filter(
        p => ['header', 'query', 'body'].includes(p.paramType) && p.dataType !== 'object'
      )
      currentResponseParams.value = allApiParams.value.filter(
        p => p.paramType === 'response' && (p.parentId == 0 || p.parentId == null)
      )
    } catch (e) {
      console.warn('加载接口参数失败', apiCode, e)
      currentApiParams.value = []
      currentResponseParams.value = []
      allApiParams.value = []
    }
  } else {
    console.warn('接口目录中未找到', apiCode)
    currentApiParams.value = []
    currentResponseParams.value = []
    allApiParams.value = []
  }
}

async function handleApiCodeChange() {
  updateNodeProperties()
  const apiCode = selectedNode.value?.properties?.apiCode
  if (apiCode) {
    await loadApiParamsByCode(apiCode)
    // 自动为接口参数创建空的输入映射项（保留已有 source）
    syncApiInputMappings()
    // 自动为接口返回参数创建输出映射项
    for (const param of currentResponseParams.value) {
      const source = `body.${param.paramKey}`
      const target = `context.${param.paramKey}`
      if (!outputMappings.value.some(m => m.source === source)) {
        outputMappings.value.push({ source, target })
      }
    }
    updateNodeProperties()
  } else {
    currentApiParams.value = []
    currentResponseParams.value = []
  }
}

function getInputType(target) {
  const mapping = inputMappings.value.find(m => m.target === target)
  return mapping ? (mapping.type || 'var') : 'var'
}

function getInputSource(target) {
  const mapping = inputMappings.value.find(m => m.target === target)
  return mapping ? mapping.source : ''
}

function setInputSource(target, source, type) {
  const idx = inputMappings.value.findIndex(m => m.target === target)
  if (idx > -1) {
    inputMappings.value[idx].source = source
    if (type) inputMappings.value[idx].type = type
  } else {
    inputMappings.value.push({ source, target, type: type || 'var' })
  }
  updateNodeProperties()
}

function toggleInputType(target) {
  const current = getInputType(target)
  const next = current === 'var' ? 'const' : 'var'
  const idx = inputMappings.value.findIndex(m => m.target === target)
  if (idx > -1) {
    inputMappings.value[idx].type = next
    if (next === 'var' && !inputMappings.value[idx].source.startsWith('context.')) {
      inputMappings.value[idx].source = ''
    }
  } else {
    inputMappings.value.push({ source: '', target, type: next })
  }
  updateNodeProperties()
}

const customInputMappings = computed(() => {
  if (selectedNode.value?.type !== 'api' || currentApiParams.value.length === 0) {
    return inputMappings.value
  }
  const apiTargets = new Set(currentApiParams.value.map(p => getApiTarget(p)))
  return inputMappings.value.filter(m => !apiTargets.has(m.target))
})

const apiParamsByType = computed(() => {
  const groups = { header: [], query: [], body: [] }
  for (const param of currentApiParams.value) {
    const type = param.paramType
    if (groups[type]) groups[type].push(param)
  }
  return groups
})

const apiParamTypeLabels = { header: 'HEADER', query: 'QUERY', body: 'BODY' }
const apiParamActiveTab = ref('body')

// 获取当前节点的所有上游节点ID（拓扑排序）
function getUpstreamNodeIds(currentId, graphData) {
  const upstream = new Set()
  const queue = [currentId]
  const visited = new Set()
  while (queue.length > 0) {
    const id = queue.shift()
    if (visited.has(id)) continue
    visited.add(id)
    for (const edge of graphData.edges) {
      if (edge.targetNodeId === id) {
        upstream.add(edge.sourceNodeId)
        queue.push(edge.sourceNodeId)
      }
    }
  }
  return upstream
}

// 变量选择器：可用变量列表（仅上游节点）
const availableVariables = computed(() => {
  const groups = []
  // 流程入参
  if (flowInputParams.value && flowInputParams.value.length > 0) {
    groups.push({
      name: '流程入参',
      items: flowInputParams.value
    })
  }
  // 系统变量
  groups.push({
    name: '系统变量',
    items: [
      { label: '_businessKey', value: 'context._businessKey', level: 0, type: '' },
      { label: '_instanceId', value: 'context._instanceId', level: 0, type: '' },
      { label: '_currentTime', value: 'context._currentTime', level: 0, type: '' }
    ]
  })
  // 循环变量
  if (lf && selectedNode.value) {
    try {
      const graphData = lf.getGraphData()
      const currentId = selectedNode.value.id
      const upstreamIds = getUpstreamNodeIds(currentId, graphData)
      const loopItems = []
      for (const node of graphData.nodes) {
        if (node.id === currentId) continue
        if (!upstreamIds.has(node.id)) continue
        // 上游 foreach/while 节点：暴露 item / index
        if (node.type === 'foreach' || node.type === 'while') {
          const itemVar = node.properties?.itemVar || 'item'
          const indexVar = node.properties?.indexVar || 'index'
          if (node.type === 'foreach') {
            const sourceExpr = node.properties?.sourceExpr || ''
            const collectionName = extractContextVarName(sourceExpr) || itemVar
            const elementLabel = `${collectionName}[${indexVar}]`
            // 当前元素整体
            loopItems.push({ label: elementLabel, value: `context.${itemVar}`, level: 0, type: 'object' })
            // 自动展开集合元素字段：orderList[index].id
            const fields = inferLoopItemFields(node, graphData, upstreamIds)
            fields.forEach(field => {
              loopItems.push({
                label: `${elementLabel}.${field}`,
                value: `context.${itemVar}.${field}`,
                level: 1,
                type: ''
              })
            })
          } else {
            loopItems.push({ label: itemVar, value: `context.${itemVar}`, level: 0, type: 'object' })
          }
          loopItems.push({ label: indexVar, value: `context.${indexVar}`, level: 0, type: 'int' })
        }
        // 上游 end_foreach/end_while 节点：暴露聚合结果
        if (node.type === 'end_foreach' || node.type === 'end_while') {
          const loopNodeId = node.properties?.loopNodeId
          const loopNode = graphData.nodes.find(n => n.id === loopNodeId)
          const resultVar = loopNode?.properties?.resultVar || `loopResult_${loopNodeId}`
          loopItems.push({ label: resultVar, value: `context.${resultVar}`, level: 0, type: '' })
        }
      }
      if (loopItems.length > 0) {
        groups.push({ name: '循环变量', items: loopItems })
      }
    } catch (e) { /* ignore */ }
  }
  // 上游节点变量
  if (lf && selectedNode.value) {
    try {
      const graphData = lf.getGraphData()
      const currentId = selectedNode.value.id
      const upstreamIds = getUpstreamNodeIds(currentId, graphData)
      for (const node of graphData.nodes) {
        if (node.id === currentId) continue
        if (!upstreamIds.has(node.id)) continue  // 只保留上游节点
        if (node.type === 'end') continue  // 结束节点无输出
        const nodeName = node.properties?.name || node.text?.value || node.id
        const items = []
        // DB 节点：resultVarName
        if (node.type === 'db' && node.properties?.resultVarName) {
          items.push({ label: node.properties.resultVarName, value: `context.${node.properties.resultVarName}` })
        }
        // Script 节点：自动解析脚本内容中的 context.set("key", ...) 声明
        if (node.type === 'script') {
          const scriptContent = node.properties?.scriptContent
          if (scriptContent) {
            const vars = parseScriptContextVars(scriptContent)
            vars.forEach(v => {
              items.push({ label: v, value: `context.${v}`, level: 0, type: '' })
            })
          }
        }
        // API 节点：尝试展开 response 参数
        if (node.type === 'api' && node.properties?.apiCode) {
          // 从当前已加载的 apiCatalogOptions 中找该 API 的 response 参数
          // 由于异步加载较复杂，这里仅展示主结果变量
        }
        // 插件节点：从 outputSchema 解析输出字段
        if (isPluginNode(node.type)) {
          const schema = pluginOutputSchemas.value[node.type]
          if (schema && schema.fields) {
            schema.fields.forEach(field => {
              items.push({
                label: field.label || field.name,
                value: `context.${field.name}`,
                level: 0,
                type: field.type
              })
            })
          }
        }
        if (items.length > 0) {
          groups.push({ name: `${nodeName}`, items })
        }
      }
    } catch (e) { /* ignore */ }
  }
  return groups
})

const filteredVars = computed(() => {
  if (!varSearch.value) return availableVariables.value
  const kw = varSearch.value.toLowerCase()
  return availableVariables.value.map(g => ({
    name: g.name,
    items: g.items.filter(v => v.label.toLowerCase().includes(kw) || v.value.toLowerCase().includes(kw))
  })).filter(g => g.items.length > 0)
})

// 数据来源分析：从输入映射中提取上游节点信息
const dataSources = computed(() => {
  if (!selectedNode.value || !lf || inputMappings.value.length === 0) return []
  try {
    const graphData = lf.getGraphData()
    const sources = []
    for (const map of inputMappings.value) {
      if (!map.source) continue
      // 匹配 nodeResult_xxx 格式的变量
      const match = map.source.match(/nodeResult_([a-zA-Z0-9_]+)/)
      if (match) {
        const nodeId = match[1]
        const node = graphData.nodes.find(n => n.id === nodeId)
        if (node) {
          sources.push({
            target: map.target,
            source: map.source,
            fromNodeName: node.properties?.name || node.text?.value || nodeId,
            fromNodeId: nodeId
          })
        }
      }
    }
    return sources
  } catch (e) {
    return []
  }
})

function openVarPicker(target) {
  currentEditingTarget.value = target
  currentEditingInputMappingIdx.value = -1
  varSearch.value = ''
  varPickerVisible.value = true
}

function openVarPickerForInputMapping(idx) {
  currentEditingTarget.value = ''
  currentEditingInputMappingIdx.value = idx
  varSearch.value = ''
  varPickerVisible.value = true
}

function selectVar(varValue) {
  if (currentEditingInputMappingIdx.value > -1) {
    const map = inputMappings.value[currentEditingInputMappingIdx.value]
    if (map) {
      map.source = varValue
      map.type = 'var'
      updateNodeProperties()
    }
    currentEditingInputMappingIdx.value = -1
  } else if (currentEditingTarget.value) {
    setInputSource(currentEditingTarget.value, varValue, 'var')
  }
  varPickerVisible.value = false
  currentEditingTarget.value = ''
}

function handleZoomIn() { lf?.zoom(true) }
function handleZoomOut() { lf?.zoom(false) }
function handleFitView() { lf?.resetZoom() }

function handleValidate() {
  if (!lf) return
  const data = lf.getGraphData()
  const hasStart = data.nodes.some(n => n.type === 'start')
  const hasEnd = data.nodes.some(n => n.type === 'end')
  if (!hasStart) { ElMessage.error('流程必须包含开始节点'); isValid.value = false; return }
  if (!hasEnd) { ElMessage.error('流程必须包含结束节点'); isValid.value = false; return }

  // 循环节点基础校验
  const loopStartTypes = ['foreach', 'while']
  const loopEndTypes = ['end_foreach', 'end_while']
  const startLoopNodes = data.nodes.filter(n => loopStartTypes.includes(n.type))
  const endLoopNodes = data.nodes.filter(n => loopEndTypes.includes(n.type))
  for (const endNode of endLoopNodes) {
    if (!endNode.properties?.loopNodeId) {
      ElMessage.error(`循环结束节点 [${endNode.text?.value || endNode.id}] 未关联循环开始节点`)
      isValid.value = false
      return
    }
    const startNode = data.nodes.find(n => n.id === endNode.properties.loopNodeId)
    if (!startNode || !loopStartTypes.includes(startNode.type)) {
      ElMessage.error(`循环结束节点 [${endNode.text?.value || endNode.id}] 关联的循环开始节点不存在`)
      isValid.value = false
      return
    }
  }
  if (startLoopNodes.length !== endLoopNodes.length) {
    ElMessage.error('循环开始节点与结束节点数量不匹配')
    isValid.value = false
    return
  }

  ElMessage.success('流程验证通过')
  isValid.value = true
}

async function handleSave() {
  if (!lf) return
  if (flowStatus.value === 1) {
    ElMessage.warning('已发布的流程不可修改，请创建新版本')
    return
  }
  debugger
  const graphData = lf.getGraphData()
  const defData = {
    id: flowId.value,
    flowCode: !flowId.value && flowName.value ? 'FLOW_' + Date.now() : undefined,
    flowName: flowName.value,
    status: 0,
    version: version.value,
    executionMode: flowExecutionMode.value,
    graphJson: JSON.stringify(graphData)
  }
  try {
    // 1. 保存流程定义
    const res = await saveFlowDefinition(defData)
    if (!flowId.value && res) {
      flowId.value = res
    }

    // 2. 保存节点和边
    if (flowId.value && graphData.nodes) {
      await saveFlowGraph(flowId.value, graphData)
    }

    ElMessage.success('流程草稿已保存')
  } catch (e) {
    ElMessage.error('保存失败: ' + e.message)
  }
}

async function handlePublish() {
  if (!flowId.value) {
    ElMessage.warning('请先保存流程')
    return
  }
  try {
    const newId = await publishFlowDefinition(flowId.value)
    flowStatus.value = 1
    // 如果返回了新的ID，说明创建了新版，更新当前ID
    if (newId && newId !== flowId.value) {
      flowId.value = newId
      // 更新URL参数
      router.replace({ path: '/workflow/designer', query: { id: newId } })
    }
    ElMessage.success('流程发布成功')
  } catch (e) {
    ElMessage.error('发布失败: ' + e.message)
  }
}

async function handleCopyAndEdit() {
  if (!flowId.value) return
  try {
    const newId = await copyFlowDefinition(flowId.value)
    ElMessage.success('已创建新版本，正在跳转...')
    router.replace({ path: '/workflow/designer', query: { id: newId } })
    // 刷新页面数据
    flowId.value = newId
    flowStatus.value = 0
    await loadFlowData()
  } catch (e) {
    ElMessage.error('创建新版本失败: ' + e.message)
  }
}

function handleTestRun() {
  testRunVisible.value = true
}

async function confirmTestRun() {
  if (!flowId.value) return
  testLoading.value = true
  try {
    const data = await startFlowInstance(flowId.value, testBusinessKey.value)
    ElMessage.success('流程实例启动成功，ID: ' + data)
    testRunVisible.value = false
    router.push('/workflow/instance')
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    testLoading.value = false
  }
}

function handleImport() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      const text = await file.text()
      const data = JSON.parse(text)
      if (!data.nodes || !Array.isArray(data.nodes)) {
        ElMessage.error('无效的流程图数据')
        return
      }
      lf.clearData()
      lf.render(data)
      ElMessage.success('导入成功')
    } catch (err) {
      ElMessage.error('导入失败: ' + err.message)
    }
  }
  input.click()
}

function handleExport() {
  if (!lf) return
  const data = lf.getGraphData()
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${flowName.value || 'flow'}.json`
  a.click()
  URL.revokeObjectURL(url)
}

async function loadFlowData() {
  try {
    const def = await getFlowDefinitionDetail(flowId.value)
    if (def) {
      flowName.value = def.flowName || '未命名流程'
      flowStatus.value = def.status || 0
      flowExecutionMode.value = def.executionMode || 'ASYNC'
      version.value = def.version || 1
      if (def.inputParams) {
        try {
          const obj = JSON.parse(def.inputParams)
          flowInputParams.value = flattenInputParams(obj)
        } catch (e) {
          flowInputParams.value = []
        }
      } else {
        flowInputParams.value = []
      }
      if (def.outputParams) {
        try {
          const obj = JSON.parse(def.outputParams)
          flowOutputParams.value = flattenInputParams(obj)
        } catch (e) {
          flowOutputParams.value = []
        }
      } else {
        flowOutputParams.value = []
      }
      console.log('[Designer] loadFlowData', { executionMode: flowExecutionMode.value, flowId: flowId.value })
      if (def.graphJson) {
        const graphData = JSON.parse(def.graphJson)
        lf.render(graphData)
        requestAnimationFrame(renderLoopContainers)
      } else {
        lf.render({ nodes: [], edges: [] })
      }
    }
  } catch (e) {
    console.error('加载流程数据失败', e)
  }
}

async function loadDatasourceOptions() {
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceOptions.value = res.list || res.records || res || []
  } catch (e) {
    datasourceOptions.value = []
  }
}

async function loadApiCatalogOptions() {
  try {
    const res = await getApiCatalogList({ page: 1, size: 999, status: 1 })
    apiCatalogOptions.value = res.list || res.records || res || []
  } catch (e) {
    apiCatalogOptions.value = []
  }
}

onMounted(() => {
  nextTick(() => initLogicFlow())
  loadDatasourceOptions()
  loadApiCatalogOptions()
})

onUnmounted(() => {
  if (lf && typeof lf.destroy === 'function') { lf.destroy() }
  lf = null
})
</script>

<style scoped lang="scss">
// ============================================================
// Workflow Designer v2 · 浅色主题
// ============================================================

$designer-bg: #f8fafc;
$panel-bg: #ffffff;
$panel-border: #e2e8f0;
$text-primary: #1e293b;
$text-secondary: #475569;
$text-muted: #94a3b8;

.designer-page {
  height: 100dvh;
  display: flex;
  flex-direction: column;
  background: $designer-bg;
  font-family: var(--font-sans);
  overflow: hidden;
  position: relative;
}

// -------- 浮动工具栏 --------
.floating-toolbar {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  height: 48px;
  background: $panel-bg;
  backdrop-filter: blur(20px) saturate(1.4);
  border: 1px solid $panel-border;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px 0 14px;
  z-index: 50;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06), inset 0 1px 0 rgba(255,255,255,0.5);
  transition: box-shadow 0.3s var(--ease-out-quart);

  &:hover {
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255,255,255,0.5);
  }
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .back-btn {
    width: 30px;
    height: 30px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: $text-secondary;
    transition: all 0.15s var(--ease-out-quart);

    &:hover {
      background: #f1f5f9;
      color: $text-primary;
    }

    &:active {
      transform: scale(0.93);
    }
  }

  .flow-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .flow-name {
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
      letter-spacing: 0.2px;
    }

    .flow-badge {
      font-size: 10px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 6px;
      text-transform: uppercase;
      letter-spacing: 0.05em;

      &.published {
        background: #d1fae5;
        color: #059669;
      }

      &.draft {
        background: #f1f5f9;
        color: $text-muted;
      }
    }
  }
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  padding: 4px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;

  .tool-group {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .tool-divider {
    width: 1px;
    height: 20px;
    background: #e2e8f0;
    margin: 0 4px;
  }

  .tool-item {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 10px;
    border-radius: 7px;
    cursor: pointer;
    color: $text-secondary;
    font-size: 12px;
    font-weight: 500;
    transition: all 0.15s var(--ease-out-quart);
    user-select: none;

    .el-icon {
      font-size: 15px;
    }

    &:hover {
      background: #f1f5f9;
      color: $text-primary;
    }

    &:active {
      transform: scale(0.95);
    }

    &.active {
      color: #059669;
      background: #d1fae5;
    }

    &.icon-only {
      padding: 5px 7px;
    }
  }
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;

  button {
    height: 32px;
    padding: 0 14px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;
    border: none;
    cursor: pointer;
    transition: all 0.15s var(--ease-out-quart);
    display: flex;
    align-items: center;
    gap: 5px;

    &:active {
      transform: scale(0.96);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  .btn-secondary {
    background: #f8fafc;
    color: $text-secondary;
    border: 1px solid #e2e8f0;

    &:hover:not(:disabled) {
      background: #f1f5f9;
      color: $text-primary;
    }
  }

  .btn-primary {
    background: linear-gradient(135deg, #2563eb, #4f46e5);
    color: #fff;
    box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);

    &:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(37, 99, 235, 0.35);
      transform: translateY(-1px);
    }
  }

  .btn-accent {
    background: linear-gradient(135deg, #059669, #10b981);
    color: #fff;
    box-shadow: 0 4px 14px rgba(16, 185, 129, 0.2);

    &:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(16, 185, 129, 0.3);
      transform: translateY(-1px);
    }
  }
}

// -------- 主体区域 --------
.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
  padding: 72px 16px 16px;
  gap: 16px;
}

// -------- 左侧面板 --------
.node-panel {
  width: 220px;
  background: $panel-bg;
  border: 1px solid $panel-border;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s var(--ease-out-expo), opacity 0.3s;

  .panel-header {
    height: 48px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 16px;
    font-weight: 600;
    font-size: 13px;
    color: $text-primary;
    border-bottom: 1px solid #f1f5f9;

    .panel-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, #dbeafe, #bfdbfe);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #2563eb;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;

    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: #e2e8f0;
      border-radius: 2px;
    }
  }

  .node-group {
    margin-bottom: 16px;

    .group-title {
      font-size: 10px;
      font-weight: 700;
      color: $text-muted;
      text-transform: uppercase;
      letter-spacing: 1px;
      margin-bottom: 8px;
      padding-left: 4px;
    }

    .group-nodes {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
  }

  .node-item {
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: move;
    background: #f8fafc;
    border: 1px solid transparent;
    overflow: hidden;
    transition: all 0.2s var(--ease-out-quart);

    .node-glow {
      position: absolute;
      top: 50%;
      left: 0;
      width: 60px;
      height: 60px;
      border-radius: 50%;
      transform: translate(-30%, -50%);
      filter: blur(20px);
      opacity: 0;
      transition: opacity 0.3s;
      pointer-events: none;
    }

    .node-accent {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 0;
      border-radius: 0 3px 3px 0;
      transition: height 0.25s var(--ease-spring);
    }

    &:hover {
      background: #f1f5f9;
      border-color: #e2e8f0;
      transform: translateX(2px);

      .node-glow {
        opacity: 1;
      }

      .node-accent {
        height: 60%;
      }
    }

    &:active {
      transform: scale(0.98);
    }

    &.disabled {
      opacity: 0.45;
      cursor: not-allowed;
      background: #f1f5f9;

      .node-label,
      .node-desc {
        color: #94a3b8;
      }
    }

    .node-icon-wrap {
      width: 30px;
      height: 30px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      background: #fff;
      border: 1px solid #f1f5f9;
      position: relative;
      z-index: 1;
    }

    .node-info {
      display: flex;
      flex-direction: column;
      gap: 1px;
      min-width: 0;
      position: relative;
      z-index: 1;

      .node-label {
        font-size: 12px;
        font-weight: 600;
        color: $text-primary;
      }

      .node-desc {
        font-size: 11px;
        color: $text-muted;
      }
    }
  }
}

// -------- 画布区域 --------
.canvas-wrapper {
  flex: 1;
  position: relative;
  background: $designer-bg;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: inset 0 0 40px rgba(0,0,0,0.02);

  // 画布角落装饰
  .canvas-corner {
    position: absolute;
    width: 20px;
    height: 20px;
    pointer-events: none;
    z-index: 5;

    &.top-left {
      top: 0;
      left: 0;
      border-top: 2px solid rgba(59, 130, 246, 0.2);
      border-left: 2px solid rgba(59, 130, 246, 0.2);
      border-radius: 16px 0 0 0;
    }
    &.top-right {
      top: 0;
      right: 0;
      border-top: 2px solid rgba(59, 130, 246, 0.2);
      border-right: 2px solid rgba(59, 130, 246, 0.2);
      border-radius: 0 16px 0 0;
    }
    &.bottom-left {
      bottom: 0;
      left: 0;
      border-bottom: 2px solid rgba(59, 130, 246, 0.2);
      border-left: 2px solid rgba(59, 130, 246, 0.2);
      border-radius: 0 0 0 16px;
    }
    &.bottom-right {
      bottom: 0;
      right: 0;
      border-bottom: 2px solid rgba(59, 130, 246, 0.2);
      border-right: 2px solid rgba(59, 130, 246, 0.2);
      border-radius: 0 0 16px 0;
    }
  }

  .logic-flow-canvas {
    width: 100%;
    height: 100%;
  }

  .canvas-watermark {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: rgba(148, 163, 184, 0.08);
    pointer-events: none;
    user-select: none;
    z-index: 0;
  }
}

// -------- 右侧面板 --------
.property-panel {
  width: 340px;
  background: $panel-bg;
  border: 1px solid $panel-border;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s var(--ease-out-expo), opacity 0.3s;

  .panel-header {
    height: 48px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 16px;
    font-weight: 600;
    font-size: 13px;
    color: $text-primary;
    border-bottom: 1px solid #f1f5f9;

    .panel-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, #fef3c7, #fde68a);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #d97706;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: #e2e8f0;
      border-radius: 2px;
    }
  }

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 11px;
    font-weight: 700;
    color: $text-secondary;
    margin: 20px 0 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f1f5f9;
    letter-spacing: 0.8px;
    text-transform: uppercase;

    &:first-child {
      margin-top: 0;
    }

    .el-button {
      text-transform: none;
      letter-spacing: 0;
      font-weight: 500;
    }
  }

  .property-form {
    :deep(.el-form-item) {
      margin-bottom: 16px;

      .el-form-item__label {
        font-size: 11px;
        font-weight: 600;
        color: $text-muted;
        padding-bottom: 6px;
        line-height: 1.4;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .el-input__wrapper,
      .el-textarea__inner,
      .el-input-number .el-input__wrapper {
        background: #fff;
        box-shadow: 0 0 0 1px #e2e8f0 inset;
        border-radius: 10px;
        color: $text-primary;
        transition: all 0.15s;

        &:hover {
          box-shadow: 0 0 0 1px #cbd5e1 inset;
        }

        &.is-focus {
          box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 3px rgba(59, 130, 246, 0.08);
        }

        input, textarea {
          color: $text-primary;
        }
      }

      .el-radio-group {
        .el-radio-button__inner {
          background: #f8fafc;
          border-color: #e2e8f0;
          color: $text-secondary;
          font-size: 12px;
          font-weight: 500;

          &:hover {
            color: $text-primary;
          }
        }

        .el-radio-button__original-radio:checked + .el-radio-button__inner {
          background: linear-gradient(135deg, #2563eb, #4f46e5);
          border-color: transparent;
          color: #fff;
          box-shadow: none;
        }
      }
    }
  }

  .form-tip {
    margin-top: 4px;
    border-radius: 10px;
    background: #eff6ff;
    border: 1px solid #dbeafe;

    :deep(.el-alert__title) {
      font-size: 12px;
      color: #2563eb;
    }

    code {
      background: #dbeafe;
      color: #2563eb;
      padding: 1px 5px;
      border-radius: 4px;
      font-family: var(--font-mono, monospace);
      font-size: 11px;
    }
  }

  .mapping-tip {
    position: relative;
    padding: 10px 12px 10px 36px;
    background: #f0f9ff;
    border: 1px solid #e0f2fe;
    border-radius: 8px;
    color: #0284c7;
    font-size: 12px;
    line-height: 1.5;
    margin-bottom: 8px;

    &::before {
      content: '';
      position: absolute;
      left: 12px;
      top: 10px;
      width: 16px;
      height: 16px;
      background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%230284c7'%3E%3Cpath d='M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10-4.477 10-10 10zm-1-11v6h2v-6h-2zm0-4v2h2V7h-2z'/%3E%3C/svg%3E") no-repeat center;
      background-size: contain;
    }
  }

  .mapping-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .mapping-card {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 12px;
      background: #f8fafc;
      border: 1px solid #f1f5f9;
      border-radius: 10px;
      transition: all 0.15s;

      &:hover {
        border-color: #e2e8f0;
        background: #f1f5f9;
      }

      .mapping-row {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 6px;
        min-width: 0;
      }

      .mapping-arrow {
        color: $text-muted;
        flex-shrink: 0;
        font-size: 14px;
      }

      .mapping-delete {
        color: #ef4444;
        cursor: pointer;
        flex-shrink: 0;
        padding: 4px;
        border-radius: 5px;
        transition: all 0.15s;

        &:hover {
          color: #dc2626;
          background: #fee2e2;
        }
      }
    }

    .mapping-empty {
      text-align: center;
      padding: 16px;
      color: $text-muted;
      font-size: 12px;
      background: #f8fafc;
      border-radius: 10px;
      border: 1px dashed #e2e8f0;
    }
  }

  // API 参数入参绑定 Tab
  .api-param-tabs {
    margin-bottom: 12px;

    :deep(.el-tabs__header) {
      margin-bottom: 8px;
    }

    :deep(.el-tabs__item) {
      font-size: 12px;
      padding: 0 12px;
      height: 32px;
      line-height: 32px;
    }

    :deep(.el-tabs__content) {
      padding: 4px 0 0;
    }

    .api-param-mapping-list {
      margin-bottom: 0;
    }
  }

  // API 参数入参绑定
  .api-param-mapping-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;

    .api-param-card {
      .api-param-row {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;

        .param-badge {
          font-size: 10px;
          font-weight: 700;
          padding: 2px 6px;
          border-radius: 4px;
          text-transform: uppercase;
          flex-shrink: 0;

          &.header {
            background: #fef3c7;
            color: #d97706;
          }

          &.query {
            background: #e0f2fe;
            color: #0284c7;
          }

          &.body {
            background: #dcfce7;
            color: #16a34a;
          }

          &.response {
            background: #f3e8ff;
            color: #9333ea;
          }
        }

        .param-info {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 4px;
          min-width: 0;
          overflow: hidden;

          .param-key {
            font-size: 12px;
            font-weight: 600;
            color: $text-primary;
            white-space: nowrap;
          }

          .param-name {
            font-size: 11px;
            color: $text-muted;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .param-required {
            color: #ef4444;
            font-size: 14px;
            font-weight: 700;
          }
        }

        .el-input {
          flex: 1.2;
        }
      }
    }
  }

  // 变量选择器触发按钮
  .var-picker-trigger {
    color: $text-muted;
    cursor: pointer;
    font-size: 12px;
    transition: color 0.15s;

    &:hover {
      color: #3b82f6;
    }
  }

  // 数据来源（数据血缘）
  .data-source-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 16px;

    .data-source-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 10px;
      background: #f0f9ff;
      border: 1px solid #bae6fd;
      border-radius: 10px;
      font-size: 12px;

      .ds-tag {
        font-weight: 600;
        color: #0369a1;
        background: #e0f2fe;
        padding: 2px 8px;
        border-radius: 6px;
        font-family: var(--font-mono, monospace);
        font-size: 11px;
      }

      .ds-arrow {
        color: #7dd3fc;
        font-size: 12px;
        flex-shrink: 0;
      }

      .ds-from {
        color: #0284c7;
        font-weight: 500;
      }
    }
  }

  // 空状态
  .empty-tip {
    height: 100%;
    min-height: 300px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    text-align: center;

    .empty-visual {
      margin-bottom: 20px;
    }

    .empty-orbit {
      position: relative;
      width: 80px;
      height: 80px;

      .empty-planet {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: linear-gradient(135deg, #3b82f6, #6366f1);
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.2);
      }

      .empty-satellite {
        position: absolute;
        top: 50%;
        left: 50%;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #60a5fa;
        margin-top: -3px;
        margin-left: -3px;

        &.s1 {
          animation: orbit1 4s linear infinite;
        }
        &.s2 {
          animation: orbit2 6s linear infinite;
          width: 4px;
          height: 4px;
          background: #cbd5e1;
        }
      }
    }

    .empty-text {
      font-size: 14px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 6px;
    }

    .empty-desc {
      font-size: 12px;
      color: $text-muted;
    }
  }
}

@keyframes orbit1 {
  from { transform: rotate(0deg) translateX(30px) rotate(0deg); }
  to { transform: rotate(360deg) translateX(30px) rotate(-360deg); }
}

@keyframes orbit2 {
  from { transform: rotate(180deg) translateX(22px) rotate(-180deg); }
  to { transform: rotate(540deg) translateX(22px) rotate(-540deg); }
}

// -------- 弹窗样式 --------
:deep(.flow-dialog) {
  .el-dialog {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 16px;
    box-shadow: 0 24px 60px rgba(0,0,0,0.1);
  }

  .el-dialog__header {
    padding: 20px 24px 12px;
    margin-right: 0;
    border-bottom: 1px solid #f1f5f9;

    .el-dialog__title {
      font-size: 15px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .el-dialog__body {
    padding: 20px 24px;
    color: $text-secondary;
  }

  .el-dialog__footer {
    padding: 12px 24px 20px;
    border-top: 1px solid #f1f5f9;
  }

  .el-input__wrapper {
    background: #fff;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    border-radius: 10px;

    input {
      color: $text-primary;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 3px rgba(59, 130, 246, 0.08);
    }
  }
}

// LogicFlow 浅色主题覆盖
:deep(.lf-graph) {
  background: $designer-bg !important;
}

:deep(.lf-mini-map) {
  background: $panel-bg;
  border: 1px solid $panel-border;
  border-radius: 10px;
}

// 变量选择器弹窗
:deep(.var-picker-dialog) {
  .el-dialog {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 16px;
    box-shadow: 0 24px 60px rgba(0,0,0,0.1);
  }

  .el-dialog__header {
    padding: 16px 20px 10px;
    margin-right: 0;
    border-bottom: 1px solid #f1f5f9;

    .el-dialog__title {
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .el-dialog__body {
    padding: 12px 20px 16px;
  }

  .var-picker-content {
    max-height: 360px;
    overflow-y: auto;

    .var-group {
      margin-bottom: 12px;

      .var-group-title {
        font-size: 11px;
        font-weight: 700;
        color: $text-muted;
        padding: 6px 0;
        letter-spacing: 0.5px;
      }

      .var-item {
        padding: 5px 16px;
        border-radius: 6px;
        cursor: pointer;
        transition: background 0.2s;
        margin-bottom: 1px;

        &:hover {
          background: #f1f5f9;
        }

        .var-line {
          display: flex;
          align-items: center;
          gap: 10px;
          position: relative;
        }

        .var-indent-mark {
          position: absolute;
          left: 0;
          top: 4px;
          bottom: 4px;
          width: 2px;
          background: #e2e8f0;
          border-radius: 1px;
        }

        .var-name {
          font-size: 14px;
          color: #334155;
          font-weight: 500;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .var-type {
          font-size: 10px;
          padding: 1px 7px;
          border-radius: 10px;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.3px;
          flex-shrink: 0;
          line-height: 1.5;

          &.type-string { color: #2563eb; background: #dbeafe; }
          &.type-int { color: #16a34a; background: #dcfce7; }
          &.type-double { color: #0891b2; background: #cffafe; }
          &.type-boolean { color: #7c3aed; background: #ede9fe; }
          &.type-object { color: #ea580c; background: #ffedd5; }
        }
      }
    }

    .var-empty {
      text-align: center;
      padding: 20px;
      color: $text-muted;
      font-size: 12px;
    }
  }

  // 脚本预览区域
  .script-preview {
    background: #1e293b;
    border-radius: 10px;
    padding: 12px;
    min-height: 120px;
    max-height: 240px;
    overflow-y: auto;
    cursor: pointer;
    border: 1px solid #334155;
    transition: all 0.15s;

    &:hover {
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.08);
    }

    pre {
      margin: 0;
      font-family: var(--font-mono, monospace);
      font-size: 12px;
      color: #e2e8f0;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .script-placeholder {
      color: #94a3b8;
      font-size: 12px;
    }
  }
}

// 脚本编辑弹窗
.script-editor-dialog {
  :deep(.el-dialog__body) {
    padding: 16px 20px;
  }
}
</style>

<style>
/* LogicFlow 右键菜单：循环边界节点隐藏“删除”项 */
.lf-menu.loop-boundary-node .lf-menu-delete-item {
  display: none !important;
}
</style>
