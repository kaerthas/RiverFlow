<template>
  <div class="designer-page">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="left">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <div class="flow-info">
          <span class="flow-name">{{ flowName || '未命名流程' }}</span>
          <el-tag v-if="flowStatus === 1" type="success" size="small">已发布</el-tag>
          <el-tag v-else size="small">草稿</el-tag>
        </div>
      </div>
      <div class="center">
        <el-button :type="isValid ? 'success' : ''" text @click="handleValidate">
          <el-icon><CircleCheck /></el-icon> 验证
        </el-button>
        <el-button text @click="handleImport">
          <el-icon><Upload /></el-icon> 导入
        </el-button>
        <el-button text @click="handleExport">
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </div>
      <div class="right">
        <el-button @click="handleSave">保存草稿</el-button>
        <el-button type="primary" @click="handlePublish">发布流程</el-button>
        <el-button type="success" @click="handleTestRun" :disabled="!flowId">
          <el-icon><VideoPlay /></el-icon> 测试运行
        </el-button>
      </div>
    </div>

    <div class="designer-body">
      <!-- 左侧节点面板 -->
      <div class="node-panel">
        <div class="panel-title">节点组件</div>
        <div class="node-list">
          <div
            v-for="node in nodeTypes"
            :key="node.type"
            class="node-item"
            draggable="true"
            @dragstart="handleDragStart($event, node)"
          >
            <div class="node-icon" :class="node.type">
              <el-icon :size="20"><component :is="node.icon" /></el-icon>
            </div>
            <span class="node-label">{{ node.label }}</span>
          </div>
        </div>
      </div>

      <!-- 中间画布 -->
      <div class="canvas-wrapper" @drop="handleDrop" @dragover.prevent>
        <div ref="canvasRef" class="logic-flow-canvas"></div>
        <div class="canvas-tools">
          <el-button circle size="small" @click="handleZoomIn"><el-icon><ZoomIn /></el-icon></el-button>
          <el-button circle size="small" @click="handleZoomOut"><el-icon><ZoomOut /></el-icon></el-button>
          <el-button circle size="small" @click="handleFitView"><el-icon><FullScreen /></el-icon></el-button>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <div class="panel-title">属性配置</div>
        <div v-if="selectedNode" class="property-form">
          <el-form label-position="top" size="small">
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.properties.name" placeholder="请输入节点名称" @change="updateNodeText" />
            </el-form-item>
            <el-form-item label="节点编码">
              <el-input v-model="selectedNode.properties.code" placeholder="请输入节点编码" disabled />
            </el-form-item>

            <!-- API节点配置 -->
            <template v-if="selectedNode.type === 'api'">
              <el-form-item label="绑定接口">
                <el-select v-model="selectedNode.properties.apiCode" placeholder="选择已注册的接口" style="width: 100%">
                  <el-option label="省里统一认证平台" value="API_001" />
                  <el-option label="协同调度中心" value="API_002" />
                  <el-option label="中残申请接口" value="API_003" />
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
            </template>

            <!-- DB节点配置 -->
            <template v-if="selectedNode.type === 'db'">
              <el-form-item label="数据源">
                <el-select v-model="selectedNode.properties.dsCode" placeholder="选择数据源" style="width: 100%">
                  <el-option label="主库(mysql)" value="master" />
                  <el-option label="Oracle业务库" value="biz_oracle" />
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
            </template>

            <!-- 条件节点配置 -->
            <template v-if="selectedNode.type === 'condition'">
              <el-form-item label="条件表达式(SpEL)">
                <el-input v-model="selectedNode.properties.expression" type="textarea" :rows="3" placeholder="如: #{context.resultCode == 200}" />
              </el-form-item>
              <el-alert type="info" :closable="false" show-icon>
                <template #title>支持使用 <code>context.xxx</code> 访问上下文变量</template>
              </el-alert>
            </template>

            <!-- 定时节点配置 -->
            <template v-if="selectedNode.type === 'timer'">
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
            </template>

            <!-- 脚本节点配置 -->
            <template v-if="selectedNode.type === 'script'">
              <el-form-item label="脚本内容 (Groovy)">
                <MonacoEditor
                  v-model="selectedNode.properties.scriptContent"
                  language="java"
                  theme="vs"
                  height="240px"
                />
              </el-form-item>
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  支持 Groovy 语法，可访问 <code>args</code> 上下文变量
                </template>
              </el-alert>
            </template>

            <!-- 输入映射 -->
            <el-divider content-position="left">输入映射</el-divider>
            <div class="mapping-list">
              <div v-for="(map, idx) in inputMappings" :key="idx" class="mapping-item">
                <el-input v-model="map.source" placeholder="来源(上下文)" size="small" />
                <el-icon class="mapping-arrow"><Right /></el-icon>
                <el-input v-model="map.target" placeholder="目标(节点入参)" size="small" />
                <el-icon class="mapping-delete" @click="removeInputMapping(idx)"><Close /></el-icon>
              </div>
              <el-button link type="primary" size="small" @click="addInputMapping"><el-icon><Plus /></el-icon> 添加映射</el-button>
            </div>

            <!-- 输出映射 -->
            <el-divider content-position="left">输出映射</el-divider>
            <div class="mapping-list">
              <div v-for="(map, idx) in outputMappings" :key="idx" class="mapping-item">
                <el-input v-model="map.source" placeholder="来源(节点返回)" size="small" />
                <el-icon class="mapping-arrow"><Right /></el-icon>
                <el-input v-model="map.target" placeholder="目标(上下文)" size="small" />
                <el-icon class="mapping-delete" @click="removeOutputMapping(idx)"><Close /></el-icon>
              </div>
              <el-button link type="primary" size="small" @click="addOutputMapping"><el-icon><Plus /></el-icon> 添加映射</el-button>
            </div>
          </el-form>
        </div>
        <div v-else-if="selectedEdge" class="property-form">
          <el-form label-position="top" size="small">
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
          <el-empty description="请在画布上选择一个节点或连线" />
        </div>
      </div>
    </div>

    <!-- 测试运行弹窗 -->
    <el-dialog v-model="testRunVisible" title="测试运行流程" width="500px">
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
  CircleNode, CircleNodeModel,
  DiamondNode, DiamondNodeModel,
  h
} from '@logicflow/core'
import {
  Control,
  Menu,
  DndPanel,
  SelectionSelect
} from '@logicflow/extension'
import '@logicflow/core/dist/style/index.css'
import '@logicflow/extension/lib/style/index.css'
import { saveFlowDefinition, getFlowDefinitionDetail, publishFlowDefinition } from '@/api/workflow'
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

const nodeTypes = [
  { type: 'start', label: '开始', icon: 'VideoPlay', color: '#52C41A' },
  { type: 'api', label: '接口调用', icon: 'Upload', color: '#1677FF' },
  { type: 'db', label: '数据库', icon: 'Coin', color: '#722ED1' },
  { type: 'script', label: '脚本处理', icon: 'DocumentCopy', color: '#EB2F96' },
  { type: 'condition', label: '条件判断', icon: 'Share', color: '#FA8C16' },
  { type: 'timer', label: '定时等待', icon: 'Timer', color: '#13C2C2' },
  { type: 'end', label: '结束', icon: 'CircleCheck', color: '#F5222D' }
]

// 注册 LogicFlow 扩展
LogicFlow.use(Control)
LogicFlow.use(Menu)
LogicFlow.use(DndPanel)
LogicFlow.use(SelectionSelect)

function initLogicFlow() {
  if (!canvasRef.value) return

  lf = new LogicFlow({
    container: canvasRef.value,
    grid: {
      type: 'dot',
      size: 10,
      config: { color: '#E8E8E8' }
    },
    keyboard: { enabled: true },
    snapline: true,
    stopScrollGraph: true,
    stopZoomGraph: false,
    metaKeyMultipleSelected: true,
    style: {
      rect: { rx: 6, ry: 6 },
      circle: { r: 25 }
    }
  })

  // 注册自定义节点
  registerCustomNodes()

  // 事件监听
  lf.on('element:click', ({ data }) => {
    if (data.type === 'polyline' || data.type === 'line') {
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
        { id: 'start_1', type: 'start', x: 200, y: 300, text: '开始', properties: { name: '开始', code: 'start_1' } },
        { id: 'end_1', type: 'end', x: 600, y: 300, text: '结束', properties: { name: '结束', code: 'end_1' } }
      ],
      edges: []
    })
  }
}

function registerCustomNodes() {
  const nodeConfig = {
    start: { Model: CircleNodeModel, View: CircleNode, shape: 'circle', color: '#52C41A', label: '开始', r: 30 },
    end: { Model: CircleNodeModel, View: CircleNode, shape: 'circle', color: '#F5222D', label: '结束', r: 30 },
    api: { Model: RectNodeModel, View: RectNode, shape: 'rect', color: '#1677FF', label: '接口', w: 120, h: 50 },
    db: { Model: RectNodeModel, View: RectNode, shape: 'rect', color: '#722ED1', label: '数据库', w: 120, h: 50 },
    script: { Model: RectNodeModel, View: RectNode, shape: 'rect', color: '#EB2F96', label: '脚本', w: 120, h: 50 },
    condition: { Model: DiamondNodeModel, View: DiamondNode, shape: 'diamond', color: '#FA8C16', label: '条件', w: 100, h: 80 },
    timer: { Model: RectNodeModel, View: RectNode, shape: 'rect', color: '#13C2C2', label: '定时', w: 120, h: 50 }
  }

  Object.entries(nodeConfig).forEach(([type, cfg]) => {
    const NodeModel = class extends cfg.Model {
      initNodeData(data) {
        super.initNodeData(data)
        if (cfg.shape === 'circle') this.r = cfg.r
        else if (cfg.shape === 'rect') { this.width = cfg.w; this.height = cfg.h }
        else if (cfg.shape === 'diamond') { this.width = cfg.w; this.height = cfg.h }
      }
      getNodeStyle() {
        const style = super.getNodeStyle()
        style.fill = this.properties.color || cfg.color
        style.stroke = '#fff'
        style.strokeWidth = 2
        return style
      }
      getTextStyle() {
        const style = super.getTextStyle()
        style.color = '#fff'
        style.fontSize = 12
        return style
      }
    }

    const NodeView = class extends cfg.View {
      getShape() {
        const shape = super.getShape()
        return shape
      }
    }

    lf.register({
      type,
      model: NodeModel,
      view: NodeView
    })
  })
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
    lf.changeNodeId(selectedNode.value.id, selectedNode.value.id) // 触发更新
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
    const res = await saveFlowDefinition(defData)
    if (!flowId.value && res) {
      flowId.value = res
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
    const res = await fetch('/api/workflow/instance/' + flowId.value + '/start?businessKey=' + encodeURIComponent(testBusinessKey.value), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('流程实例启动成功，ID: ' + data.data)
      testRunVisible.value = false
      // 跳转到实例监控
      router.push('/workflow/instance')
    } else {
      ElMessage.error(data.msg || '启动失败')
    }
  } catch (e) {
    ElMessage.error('启动失败: ' + e.message)
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

onMounted(() => {
  nextTick(() => initLogicFlow())
})

onUnmounted(() => {
  if (lf) { lf.destroy(); lf = null }
})
</script>

<style scoped lang="scss">
.designer-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F0F2F5;
}

.designer-header {
  height: 56px;
  background: #FFFFFF;
  border-bottom: 1px solid #E8E8E8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;

  .left {
    display: flex;
    align-items: center;
    gap: 16px;

    .flow-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .flow-name {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }
  }

  .center {
    display: flex;
    gap: 8px;
  }

  .right {
    display: flex;
    gap: 12px;
  }
}

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;

  .node-panel {
    width: 200px;
    background: #FFFFFF;
    border-right: 1px solid #E8E8E8;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;

    .panel-title {
      height: 40px;
      line-height: 40px;
      padding: 0 16px;
      font-weight: 600;
      font-size: 14px;
      color: #262626;
      border-bottom: 1px solid #F0F0F0;
    }

    .node-list {
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .node-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 6px;
        cursor: move;
        transition: all 0.2s;
        border: 1px solid #F0F0F0;

        &:hover {
          background: #F5F5F5;
          border-color: #D9D9D9;
        }

        .node-icon {
          width: 32px;
          height: 32px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #FFFFFF;

          &.start { background: #52C41A; }
          &.api { background: #1677FF; }
          &.db { background: #722ED1; }
          &.script { background: #EB2F96; }
          &.condition { background: #FA8C16; }
          &.timer { background: #13C2C2; }
          &.end { background: #F5222D; }
        }

        .node-label {
          font-size: 13px;
          color: #262626;
        }
      }
    }
  }

  .canvas-wrapper {
    flex: 1;
    position: relative;
    background: #FAFAFA;

    .logic-flow-canvas {
      width: 100%;
      height: 100%;
    }

    .canvas-tools {
      position: absolute;
      bottom: 20px;
      right: 20px;
      display: flex;
      gap: 8px;
      background: #FFFFFF;
      padding: 8px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
  }

  .property-panel {
    width: 320px;
    background: #FFFFFF;
    border-left: 1px solid #E8E8E8;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;

    .panel-title {
      height: 40px;
      line-height: 40px;
      padding: 0 16px;
      font-weight: 600;
      font-size: 14px;
      color: #262626;
      border-bottom: 1px solid #F0F0F0;
    }

    .property-form {
      flex: 1;
      padding: 16px;
      overflow-y: auto;

      .mapping-list {
        .mapping-item {
          display: flex;
          align-items: center;
          gap: 6px;
          margin-bottom: 8px;

          .mapping-arrow {
            color: #8C8C8C;
            flex-shrink: 0;
          }

          .mapping-delete {
            color: #F5222D;
            cursor: pointer;
            flex-shrink: 0;
            &:hover { color: #ff4d4f; }
          }
        }
      }
    }

    .empty-tip {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>
