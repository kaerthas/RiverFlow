<template>
  <div v-if="!item.meta?.hidden">
    <template v-if="!item.children || item.children.length === 0">
      <el-menu-item :index="resolvePath(item.path)">
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <template #title>{{ item.meta?.title }}</template>
      </el-menu-item>
    </template>

    <el-sub-menu v-else :index="resolvePath(item.path || '')">
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <SidebarItem
        v-for="child in item.children"
        :key="resolvePath(child.path || child.id || child.meta?.title)"
        :item="child"
        :base-path="resolvePath(child.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
function resolve(basePath, routePath) {
  if (!routePath) {
    return basePath && basePath.startsWith('/') ? basePath : '/' + (basePath || '')
  }
  if (routePath.startsWith('/')) return routePath
  if (!basePath) return '/' + routePath.replace(/^\//, '')
  basePath = basePath.startsWith('/') ? basePath : '/' + basePath
  return basePath.replace(/\/$/, '') + '/' + routePath.replace(/^\//, '')
}

const props = defineProps({
  item: { type: Object, required: true },
  basePath: { type: String, default: '' }
})

function resolvePath(routePath) {
  return resolve(props.basePath, routePath)
}
</script>
