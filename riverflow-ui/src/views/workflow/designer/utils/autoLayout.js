/**
 * 流程图自动布局工具
 *
 * 基于拓扑排序的分层布局算法，把节点按层级排列，使流程图更清晰。
 */

const NODE_WIDTH = 160
const NODE_HEIGHT = 60
const LEVEL_GAP_X = 220
const LEVEL_GAP_Y = 100
const START_X = 120
const START_Y = 240

/**
 * 对 graphJson 的节点进行自动布局
 * @param {Object} graphJson LogicFlow 的 graphJson
 * @returns {Object} 布局后的 graphJson
 */
export function layoutGraph(graphJson) {
  if (!graphJson || !graphJson.nodes || graphJson.nodes.length === 0) {
    return graphJson
  }

  const nodes = graphJson.nodes.map(n => ({ ...n }))
  const edges = (graphJson.edges || []).map(e => ({ ...e }))

  // 构建邻接表和入度
  const nodeMap = new Map()
  const adj = new Map()
  const inDegree = new Map()
  const reverseAdj = new Map()

  nodes.forEach(n => {
    nodeMap.set(n.id, n)
    adj.set(n.id, [])
    reverseAdj.set(n.id, [])
    inDegree.set(n.id, 0)
  })

  edges.forEach(e => {
    const source = e.sourceNodeId
    const target = e.targetNodeId
    if (adj.has(source) && adj.has(target)) {
      adj.get(source).push(target)
      reverseAdj.get(target).push(source)
      inDegree.set(target, inDegree.get(target) + 1)
    }
  })

  // 找到开始节点（入度为 0 或 type 为 start）
  const startNodes = nodes.filter(n => n.type === 'start' || inDegree.get(n.id) === 0)
  const queue = [...startNodes.map(n => n.id)]
  const levels = new Map()
  startNodes.forEach(n => levels.set(n.id, 0))

  // 拓扑排序并计算层级
  const visited = new Set()
  while (queue.length > 0) {
    const id = queue.shift()
    if (visited.has(id)) continue
    visited.add(id)

    const currentLevel = levels.get(id) || 0
    const neighbors = adj.get(id) || []
    neighbors.forEach(nextId => {
      const nextLevel = Math.max(levels.get(nextId) || 0, currentLevel + 1)
      levels.set(nextId, nextLevel)
      queue.push(nextId)
    })
  }

  // 按层级分组
  const levelGroups = new Map()
  levels.forEach((level, id) => {
    if (!levelGroups.has(level)) {
      levelGroups.set(level, [])
    }
    levelGroups.get(level).push(id)
  })

  // 计算每个节点的坐标
  levelGroups.forEach((nodeIds, level) => {
    const count = nodeIds.length
    const totalHeight = (count - 1) * LEVEL_GAP_Y
    const startY = START_Y - totalHeight / 2

    nodeIds.forEach((id, index) => {
      const node = nodeMap.get(id)
      node.x = START_X + level * LEVEL_GAP_X
      node.y = startY + index * LEVEL_GAP_Y
    })
  })

  return {
    ...graphJson,
    nodes
  }
}

/**
 * 重新排列边，确保边的路径清晰
 */
export function layoutEdges(graphJson) {
  if (!graphJson || !graphJson.edges) return graphJson
  const edges = graphJson.edges.map(e => ({
    ...e,
    type: e.type || 'polyline'
  }))
  return {
    ...graphJson,
    edges
  }
}
