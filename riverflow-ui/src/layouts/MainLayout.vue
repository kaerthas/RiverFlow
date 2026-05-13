<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img src="/logo.svg" alt="logo" v-if="false" />
        <el-icon :size="28" color="#1677FF"><ElementPlus /></el-icon>
        <span v-show="!isCollapse" class="title">RiverFlow</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#001529"
          text-color="#a6adb4"
          active-text-color="#FFFFFF"
        >
          <SidebarItem
            v-for="route in menuRoutes"
            :key="route.path"
            :item="route"
            :base-path="route.path"
          />
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <Breadcrumb />
        </div>
        <div class="right">
          <el-tooltip content="文档">
            <el-icon :size="18" class="action-icon"><Document /></el-icon>
          </el-tooltip>
          <el-tooltip content="全屏">
            <el-icon :size="18" class="action-icon"><FullScreen /></el-icon>
          </el-tooltip>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="28" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="name">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

// 从路由生成菜单
const menuRoutes = computed(() => {
  const layoutRoute = router.getRoutes().find(r => r.path === '/')
  return layoutRoute?.children?.filter(r => !r.meta?.hidden) || []
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleLogout() {
  userStore.clearToken()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;

  .sidebar {
    background: #001529;
    transition: width 0.3s;
    box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);

    .logo {
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 16px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);

      .title {
        margin-left: 10px;
        font-size: 18px;
        font-weight: 600;
        color: #FFFFFF;
        white-space: nowrap;
      }
    }

    :deep(.el-menu) {
      border-right: none;
    }
  }

  .header {
    height: 56px;
    background: #FFFFFF;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    z-index: 100;

    .left {
      display: flex;
      align-items: center;
      gap: 16px;

      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        color: #595959;
        &:hover { color: #1677FF; }
      }
    }

    .right {
      display: flex;
      align-items: center;
      gap: 20px;

      .action-icon {
        cursor: pointer;
        color: #595959;
        &:hover { color: #1677FF; }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;

        .name {
          font-size: 14px;
          color: #262626;
        }
      }
    }
  }

  .main-content {
    padding: 0;
    background: #F0F2F5;
    overflow: auto;
  }
}
</style>
