<template>
  <div ref="containerRef" class="workflow-designer">
    <!-- LogicFlow 画布容器 -->
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const containerRef = ref(null)

onMounted(() => {
  // TODO: 初始化 LogicFlow 画布
  // 预留 LogicFlow 集成入口
})
</script>

<style scoped>
.workflow-designer {
  width: 100%;
  height: 100%;
}
</style>
