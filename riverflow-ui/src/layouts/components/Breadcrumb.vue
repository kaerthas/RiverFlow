<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
      <span v-if="index === breadcrumbs.length - 1" class="no-redirect">{{ item.title }}</span>
      <a v-else @click.prevent="handleLink(item)">{{ item.title }}</a>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const breadcrumbs = ref([])

function getBreadcrumb() {
  let matched = route.matched.filter(item => item.meta?.title)
  breadcrumbs.value = matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
}

function handleLink(item) {
  router.push(item.path)
}

watch(() => route.path, getBreadcrumb, { immediate: true })
</script>

<style scoped>
.no-redirect {
  color: #8C8C8C;
  cursor: text;
}
</style>
