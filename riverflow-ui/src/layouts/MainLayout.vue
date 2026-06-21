<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <rect width="40" height="40" rx="10" fill="#f0f9ff" />
            <!-- 耳朵 -->
            <circle cx="12" cy="13" r="4.5" fill="#d97706" />
            <circle cx="28" cy="13" r="4.5" fill="#d97706" />
            <circle cx="12" cy="13" r="2.5" fill="#fcd34d" />
            <circle cx="28" cy="13" r="2.5" fill="#fcd34d" />
            <!-- 脸 -->
            <ellipse cx="20" cy="23" rx="11" ry="9" fill="#d97706" />
            <!-- 嘴部浅色区域 -->
            <ellipse cx="20" cy="25.5" rx="7" ry="5" fill="#fcd34d" />
            <!-- 眼睛 -->
            <circle cx="15.5" cy="21" r="1.8" fill="#1f2937" />
            <circle cx="24.5" cy="21" r="1.8" fill="#1f2937" />
            <circle cx="16.2" cy="20.3" r="0.6" fill="#ffffff" />
            <circle cx="25.2" cy="20.3" r="0.6" fill="#ffffff" />
            <!-- 大门牙 -->
            <rect x="18" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#ffffff" />
            <rect x="20.2" y="26.5" width="1.8" height="2.8" rx="0.5" fill="#ffffff" />
            <!-- 鼻子 -->
            <ellipse cx="20" cy="24.5" rx="2.2" ry="1.6" fill="#92400e" />
          </svg>
        </div>
        <span v-show="!isCollapse" class="title">RiverFlow</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="transparent"
          text-color="#9ca3af"
          active-text-color="#ffffff"
        >
          <SidebarItem
            v-for="route in menuRoutes"
            :key="route.path"
            :item="route"
            :base-path="route.path"
          />
        </el-menu>
      </el-scrollbar>

      <!-- 底部装饰线 -->
      <div v-show="!isCollapse" class="sidebar-footer">
        <div class="footer-line"></div>
        <span class="footer-text">v1.0.0</span>
      </div>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="left">
          <div class="collapse-btn" @click="toggleCollapse">
            <el-icon :size="18"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </div>
          <Breadcrumb />
        </div>
        <div class="right">
          <div class="action-icon">
            <el-icon :size="18"><Document /></el-icon>
          </div>
          <div class="action-icon">
            <el-icon :size="18"><FullScreen /></el-icon>
          </div>
          <el-dropdown @command="handleLocaleChange">
            <div class="action-icon">
              <el-icon :size="18"><Switch /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
                <el-dropdown-item command="en-US">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown>
            <div class="user-info">
              <div class="avatar-ring">
                <el-avatar :size="28" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              </div>
              <span class="name">{{ userName }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>{{ $t('layoutMainLayout.个人中心_409120b5') }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">{{ $t('layoutMainLayout.退出登录_44efd179') }}</el-dropdown-item>
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
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import SidebarItem from './components/SidebarItem.vue'
import Breadcrumb from './components/Breadcrumb.vue'
import { logout } from '@/api/auth'
import { ElMessageBox } from 'element-plus'
import { setLocale } from '@/i18n'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || t('layoutMainLayout.管理员_b1dae9bc'))

const menuRoutes = computed(() => {
  const layoutRoute = router.getRoutes().find(r => r.path === '/')
  return layoutRoute?.children?.filter(r => !r.meta?.hidden) || []
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleLocaleChange(locale) {
  setLocale(locale)
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm(t('layoutMainLayout.确定要退出登_13bc0ac0'), t('layoutMainLayout.提示_02d9819d'), { type: 'warning' })
    await logout().catch(() => {})
    userStore.clearToken()
    router.push('/login')
  } catch (e) {
    // 取消退出
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100dvh;

  .sidebar {
    background: linear-gradient(180deg, #0f1115 0%, #161922 100%);
    transition: width 0.35s var(--ease-out-expo);
    position: relative;
    display: flex;
    flex-direction: column;

    // 微妙的顶部光晕
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 180px;
      background: radial-gradient(
        ellipse 80% 60% at 50% 0%,
        rgba(37, 99, 235, 0.08) 0%,
        transparent 70%
      );
      pointer-events: none;
    }

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 0 20px;
      position: relative;
      z-index: 1;

      .logo-icon {
        width: 32px;
        height: 32px;
        border-radius: 10px;
        background: linear-gradient(135deg, #2563eb 0%, #4f46e5 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        flex-shrink: 0;
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
      }

      .title {
        font-size: 18px;
        font-weight: 700;
        color: #ffffff;
        white-space: nowrap;
        letter-spacing: -0.02em;
      }
    }

    :deep(.el-menu) {
      border-right: none;
      background: transparent;
      padding: 0 10px;

      .el-menu-item,
      .el-sub-menu__title {
        height: 44px;
        line-height: 44px;
        border-radius: 10px;
        margin-bottom: 4px;
        transition: all 0.2s var(--ease-out-quart);
        font-size: 13px;
        font-weight: 500;

        &:hover {
          background: rgba(255, 255, 255, 0.06);
        }

        &.is-active {
          background: linear-gradient(90deg, rgba(37, 99, 235, 0.2) 0%, rgba(37, 99, 235, 0.05) 100%);
          color: #ffffff;
          font-weight: 600;

          &::before {
            content: '';
            position: absolute;
            left: -10px;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            background: #2563eb;
            border-radius: 0 3px 3px 0;
          }
        }
      }

      .el-sub-menu {
        .el-menu-item {
          padding-left: 44px !important;
        }
      }
    }

    .sidebar-footer {
      margin-top: auto;
      padding: 16px 20px;
      position: relative;
      z-index: 1;

      .footer-line {
        height: 1px;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
        margin-bottom: 10px;
      }

      .footer-text {
        font-size: 11px;
        color: #4b5563;
        font-family: var(--font-mono, monospace);
      }
    }
  }

  .header {
    height: var(--rf-header-height);
    background: rgba(255, 255, 255, 0.82);
    backdrop-filter: blur(12px) saturate(1.2);
    -webkit-backdrop-filter: blur(12px) saturate(1.2);
    border-bottom: 1px solid rgba(229, 231, 235, 0.6);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    z-index: 100;
    position: sticky;
    top: 0;

    .left {
      display: flex;
      align-items: center;
      gap: 16px;

      .collapse-btn {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: var(--rf-text-secondary);
        transition: all 0.2s var(--ease-out-quart);

        &:hover {
          background: var(--rf-neutral-100);
          color: var(--rf-primary);
        }

        &:active {
          transform: scale(0.94);
        }
      }
    }

    .right {
      display: flex;
      align-items: center;
      gap: 8px;

      .action-icon {
        width: 34px;
        height: 34px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: var(--rf-text-secondary);
        transition: all 0.2s var(--ease-out-quart);

        &:hover {
          background: var(--rf-neutral-100);
          color: var(--rf-primary);
        }

        &:active {
          transform: scale(0.94);
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        padding: 4px 10px 4px 6px;
        border-radius: 10px;
        transition: background 0.2s var(--ease-out-quart);
        margin-left: 4px;

        &:hover {
          background: var(--rf-neutral-100);
        }

        .avatar-ring {
          position: relative;

          &::after {
            content: '';
            position: absolute;
            inset: -2px;
            border-radius: 50%;
            border: 2px solid transparent;
            background: linear-gradient(135deg, #2563eb, #4f46e5) border-box;
            -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
            mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
            -webkit-mask-composite: xor;
            mask-composite: exclude;
          }
        }

        .name {
          font-size: 13px;
          font-weight: 500;
          color: var(--rf-text-main);
        }
      }
    }
  }

  .main-content {
    padding: 0;
    background: var(--rf-bg-page);
    overflow: auto;
  }
}
</style>
