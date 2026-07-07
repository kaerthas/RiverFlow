import i18n from '@/i18n'
const { t } = i18n.global
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: t('routeRouter.登录_402d19e5'), hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: t('routeRouter.数据大盘_8a3adf76'), icon: 'Odometer' }
      },
      {
        path: 'item',
        name: 'Item',
        component: () => import('@/views/item/index.vue'),
        meta: { title: t('routeRouter.事项管理_b3d49744'), icon: 'Document' }
      },
      {
        path: 'datasource',
        name: 'Datasource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: t('routeRouter.数据源管理_4feab28d'), icon: 'DataAnalysis' }
      },
      {
        path: 'dynamic-table',
        name: 'DynamicTable',
        component: () => import('@/views/dynamicTable/index.vue'),
        meta: { title: t('routeRouter.动态表设计_b33c7316'), icon: 'Grid' }
      },
      {
        path: 'api-mgr',
        name: 'ApiMgr',
        component: () => import('@/views/apiMgr/index.vue'),
        meta: { title: t('routeRouter.接口注册_5c992ccb'), icon: 'Link' }
      },
      {
        path: 'workflow',
        name: 'Workflow',
        redirect: '/workflow/definition',
        meta: { title: t('routeRouter.工作流_228526dc'), icon: 'Share' },
        children: [
          {
            path: 'definition',
            name: 'WorkflowDefinition',
            component: () => import('@/views/workflow/definition/index.vue'),
            meta: { title: t('routeRouter.流程定义_300d6075') }
          },
          {
            path: 'designer',
            name: 'WorkflowDesigner',
            component: () => import('@/views/workflow/designer/index.vue'),
            meta: { title: t('routeRouter.流程设计器_98d0b1b0'), hidden: true }
          },
          {
            path: 'instance',
            name: 'WorkflowInstance',
            component: () => import('@/views/workflow/instance/index.vue'),
            meta: { title: t('routeRouter.实例监控_a92a32fa') }
          }
        ]
      },
      {
        path: 'ai',
        name: 'Ai',
        component: () => import('@/views/ai/index.vue'),
        meta: { title: t('routeRouter.AI助手_1306956f'), icon: 'MagicStick' }
      },
      {
        path: 'ai-model',
        name: 'AiModel',
        component: () => import('@/views/ai/model/index.vue'),
        meta: { title: t('routeRouter.AI模型管理_a1b2c3d4'), icon: 'Cpu' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/index.vue'),
        meta: { title: t('routeRouter.运行监控_8bf81f31'), icon: 'Monitor' }
      },
      {
        path: 'plugin',
        name: 'Plugin',
        component: () => import('@/views/plugin/PluginManager.vue'),
        meta: { title: t('routeRouter.插件管理_f20cdea7'), icon: 'Box' }
      },
      {
        path: 'script-mgr',
        name: 'ScriptMgr',
        component: () => import('@/views/scriptMgr/index.vue'),
        meta: { title: t('routeRouter.脚本管理_a1fb7f16'), icon: 'DocumentCopy' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - RiverFlow` : t('routeRouter.河狸流程编排_a118db9e')

  const userStore = useUserStore()
  if (to.path === '/login') {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
