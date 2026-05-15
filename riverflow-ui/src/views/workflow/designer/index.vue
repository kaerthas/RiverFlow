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
        <button class="btn-secondary" @click="handleSave">保存草稿</button>
        <button class="btn-primary" @click="handlePublish">发布流程</button>
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
                <el-input v-model="selectedNode.properties.name" placeholder="请输入节点名称" @change="updateNodeText" />
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
                  <el-select v-model="selectedNode.properties.apiCode" placeholder="选择已注册的接口" clearable style="width: 100%">
                    <el-option v-for="api in apiCatalogOptions" :key="api.id" :label="api.apiName" :value="api.apiCode" />
                  </el-select>
                </el-form-item>
                <el-form-item label="超时时间(ms)">
                  <el-input-number v-model="selectedNode.properties.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" />
                </el-form-item>
                <el-form-item label="失败策略">
                  <el-radio-group v-model="selectedNode.properties.failStrategy">
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
                  <el-select v-model="selectedNode.properties.dsCode" placeholder="选择数据源" clearable style="width: 100%">
                    <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.dsName" :value="ds.dsCode" />
                  </el-select>
                </el-form-item>
                <el-form-item label="操作类型">
                  <el-radio-group v-model="selectedNode.properties.operation">
                    <el-radio-button label="select">查询</el-radio-button>
                    <el-radio-button label="insert">插入</el-radio-button>
                    <el-radio-button label="update">更新</el-radio-button>
                    <el-radio-button label="delete">删除</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="SQL语句">
                  <el-input v-model="selectedNode.properties.sql" type="textarea" :rows="4" placeholder="支持SpEL占位符" />
                </el-form-item>
              </el-form>
            </template>

            <!-- 条件节点配置 -->
            <template v-if="selectedNode.type === 'condition'">
              <div class="section-title">条件配置</div>
              <el-form label-position="top" size="default">
                <el-form-item label="条件表达式(SpEL)">
                  <el-input v-model="selectedNode.properties.expression" type="textarea" :rows="3" placeholder="如: #{context.resultCode == 200}" />
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
                  <el-radio-group v-model="selectedNode.properties.timerType">
                    <el-radio-button label="delay">延迟</el-radio-button>
                    <el-radio-button label="fixed">指定时间</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="selectedNode.properties.timerType === 'delay'" label="延迟秒数">
                  <el-input-number v-model="selectedNode.properties.delaySeconds" :min="1" style="width: 100%" />
                </el-form-item>
                <el-form-item v-else label="指定时间">
                  <el-date-picker v-model="selectedNode.properties.fixedTime" type="datetime" placeholder="选择日期时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
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
                  />
                </el-form-item>
              </el-form>
              <el-alert class="form-tip" type="info" :closable="false" show-icon>
                <template #title>支持 Groovy 语法，可访问 <code>args</code> 上下文变量</template>
              </el-alert>
            </template>

            <!-- 输入映射 -->
            <div class="section-title">
              <span>输入映射</span>
              <el-button link type="primary" size="small" @click="addInputMapping">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
            <div class="mapping-list">
              <div v-for="(map, idx) in inputMappings" :key="idx" class="mapping-card">
                <div class="mapping-row">
                  <el-input v-model="map.source" placeholder="来源(上下文)" size="small" />
                  <el-icon class="mapping-arrow"><Right /></el-icon>
                  <el-input v-model="map.target" placeholder="目标(节点入参)" size="small" />
                </div>
                <el-icon class="mapping-delete" @click="removeInputMapping(idx)"><Close /></el-icon>
              </div>
              <div v-if="inputMappings.length === 0" class="mapping-empty">暂无输入映射</div>
            </div>

            <!-- 输出映射 -->
            <div class="section-title">
              <span>输出映射</span>
              <el-button link type="primary" size="small" @click="addOutputMapping">
                <el-icon><Plus /></el-icon> 添加
              </el-button>
            </div>
            <div class="mapping-list">
              <div v-for="(map, idx) in outputMappings" :key="idx" class="mapping-card">
                <div class="mapping-row">
                  <el-input v-model="map.source" placeholder="来源(节点返回)" size="small" />
                  <el-icon class="mapping-arrow"><Right /></el-icon>
                  <el-input v-model="map.target" placeholder="目标(上下文)" size="small" />
                </div>
                <el-icon class="mapping-delete" @click="removeOutputMapping(idx)"><Close /></el-icon>
              </div>
              <div v-if="outputMappings.length === 0" class="mapping-empty">暂无输出映射</div>
            </div>
          </div>

          <div v-else-if="selectedEdge" class="property-form">
            <div class="section-title">连线配置</div>
            <el-form label-position="top" size="default">
              <el-form-item label="条件类型">
                <el-radio-group v-model="selectedEdge.properties.conditionType">
                  <el-radio-button label="default">默认</el-radio-button>
                  <el-radio-button label="success">成功</el-radio-button>
                  <el-radio-button label="fail">失败</el-radio-button>
                  <el-radio-button label="custom">自定义</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="selectedEdge.properties.conditionType === 'custom'" label="SpEL表达式">
                <el-input v-model="selectedEdge.properties.conditionExpression" type="textarea" :rows="2" placeholder="#{context.resultCode == 200}" />
              </el-form-item>
              <el-form-item label="优先级">
                <el-input-number v-model="selectedEdge.properties.priority" :min="0" :max="100" style="width: 100%" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import LogicFlow, {
  RectNode, RectNodeModel,
  h
} from '@logicflow/core'
import {
  Menu,
  SelectionSelect
} from '@logicflow/extension'
import '@logicflow/core/dist/style/index.css'
import '@logicflow/extension/lib/style/index.css'
import { saveFlowDefinition, saveFlowGraph, getFlowDefinitionDetail, publishFlowDefinition, startFlowInstance } from '@/api/workflow'
import { getDatasourceList } from '@/api/datasource'
import { getApiCatalogList } from '@/api/apiMgr'
import MonacoEditor from '@/components/MonacoEditor/index.vue'

const route = useRoute()
const router = useRouter()
const canvasRef = ref(null)
let lf = null

const flowId = ref(route.query.id ? parseInt(route.query.id) : null)
const flowName = ref('未命名流程')
const flowStatus = ref(0)
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

const nodeGroups = [
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
      { type: 'timer', label: '定时等待', desc: '延迟或定时', icon: 'Timer', color: '#06b6d4' }
    ]
  }
]

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
  lf.on('element:click', ({ data }) => {
    const edgeTypes = ['polyline', 'line', 'bezier']
    if (edgeTypes.includes(data.type)) {
      selectedNode.value = null
      selectedEdge.value = data
    } else {
      selectedEdge.value = null
      selectedNode.value = data
      inputMappings.value = data.properties?.inputMapping || []
      outputMappings.value = data.properties?.outputMapping || []
    }
  })

  lf.on('blank:click', () => {
    selectedNode.value = null
    selectedEdge.value = null
  })

  lf.on('connection:not-allowed', (data) => {
    ElMessage.warning(data.msg || '不允许的连线')
  })

  // 如果有流程ID，加载已有数据
  if (flowId.value) {
    loadFlowData()
  } else {
    // 默认添加开始和结束节点
    lf.render({
      nodes: [
        { id: 'start_1', type: 'start', x: 240, y: 320, text: '开始', properties: { name: '开始', code: 'start_1' } },
        { id: 'end_1', type: 'end', x: 640, y: 320, text: '结束', properties: { name: '结束', code: 'end_1' } }
      ],
      edges: []
    })
  }
}

function registerCustomNodes() {
  const nodeConfig = {
    start:  { color: '#22c55e', label: '开始',  w: 140, h: 52 },
    end:    { color: '#ef4444', label: '结束',  w: 140, h: 52 },
    api:    { color: '#3b82f6', label: '接口',  w: 148, h: 56 },
    db:     { color: '#8b5cf6', label: '数据库', w: 148, h: 56 },
    script: { color: '#ec4899', label: '脚本',  w: 148, h: 56 },
    condition: { color: '#f59e0b', label: '条件', w: 148, h: 56 },
    timer:  { color: '#06b6d4', label: '定时',  w: 148, h: 56 }
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
  e.dataTransfer.setData('application/lf-node', JSON.stringify({
    type: node.type,
    text: node.label,
    properties: { name: node.label, code: `${node.type}_${Date.now()}` }
  }))
}

function handleDrop(e) {
  const dataStr = e.dataTransfer.getData('application/lf-node')
  if (!dataStr || !lf) return
  try {
    const data = JSON.parse(dataStr)
    const rect = canvasRef.value.getBoundingClientRect()
    const x = e.clientX - rect.left + lf.getTransform().translateX
    const y = e.clientY - rect.top + lf.getTransform().translateY
    lf.addNode({
      type: data.type,
      x,
      y,
      text: data.text,
      properties: data.properties
    })
  } catch (err) {
    console.error('添加节点失败', err)
  }
}

function updateNodeText() {
  if (selectedNode.value && lf) {
    lf.setProperties(selectedNode.value.id, { name: selectedNode.value.properties.name })
    lf.changeNodeId(selectedNode.value.id, selectedNode.value.id)
  }
}

function addInputMapping() { inputMappings.value.push({ source: '', target: '' }) }
function removeInputMapping(idx) { inputMappings.value.splice(idx, 1) }
function addOutputMapping() { outputMappings.value.push({ source: '', target: '' }) }
function removeOutputMapping(idx) { outputMappings.value.splice(idx, 1) }

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
  ElMessage.success('流程验证通过')
  isValid.value = true
}

async function handleSave() {
  if (!lf) return
  const graphData = lf.getGraphData()
  const defData = {
    id: flowId.value,
    flowCode: flowName.value ? 'FLOW_' + Date.now() : undefined,
    flowName: flowName.value,
    status: 0,
    graphJson: JSON.stringify(graphData)
  }
  try {
    // 1. 保存流程定义
    const res = await saveFlowDefinition(defData)
    if (!flowId.value && res) {
      flowId.value = res
    }

    // 2. 保存节点和边（核心修复：之前只保存了 graphJson 字符串，节点和边没拆表存储）
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
    await publishFlowDefinition(flowId.value)
    flowStatus.value = 1
    ElMessage.success('流程发布成功')
  } catch (e) {
    ElMessage.error('发布失败: ' + e.message)
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
      if (def.graphJson) {
        const graphData = JSON.parse(def.graphJson)
        lf.render(graphData)
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
</style>
