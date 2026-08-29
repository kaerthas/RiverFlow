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
    meta: { title: '登录', hidden: true }
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
        meta: { title: '数据大盘', icon: 'Odometer' }
      },
      {
        path: 'item',
        name: 'Item',
        component: () => import('@/views/item/index.vue'),
        meta: { title: '事项管理', icon: 'Document' }
      },
      {
        path: 'datasource',
        name: 'Datasource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理', icon: 'DataAnalysis' }
      },
      {
        path: 'dynamic-table',
        name: 'DynamicTable',
        component: () => import('@/views/dynamicTable/index.vue'),
        meta: { title: '动态表设计', icon: 'Grid' }
      },
      {
        path: 'app-mgr',
        name: 'AppMgr',
        component: () => import('@/views/appMgr/index.vue'),
        meta: { title: '应用管理', icon: 'Platform' }
      },
      {
        path: 'api-mgr',
        name: 'ApiMgr',
        component: () => import('@/views/apiMgr/index.vue'),
        meta: { title: '接口注册', icon: 'Link', hidden: true }
      },
      {
        path: 'api-call-log',
        name: 'ApiCallLog',
        component: () => import('@/views/apiCallLog/index.vue'),
        meta: { title: '调用日志', icon: 'Tickets' }
      },
      {
        path: 'workflow',
        name: 'Workflow',
        redirect: '/workflow/definition',
        meta: { title: '工作流', icon: 'Share' },
        children: [
          {
            path: 'definition',
            name: 'WorkflowDefinition',
            component: () => import('@/views/workflow/definition/index.vue'),
            meta: { title: '流程定义' }
          },
          {
            path: 'designer',
            name: 'WorkflowDesigner',
            component: () => import('@/views/workflow/designer/index.vue'),
            meta: { title: '流程设计器', hidden: true }
          },
          {
            path: 'instance',
            name: 'WorkflowInstance',
            component: () => import('@/views/workflow/instance/index.vue'),
            meta: { title: '实例监控' }
          }
        ]
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/index.vue'),
        meta: { title: '运行监控', icon: 'Monitor' }
      },
      {
        path: 'plugin',
        name: 'Plugin',
        component: () => import('@/views/plugin/PluginManager.vue'),
        meta: { title: '插件管理', icon: 'Box' }
      },
      {
        path: 'script-mgr',
        name: 'ScriptMgr',
        component: () => import('@/views/scriptMgr/index.vue'),
        meta: { title: '脚本管理', icon: 'DocumentCopy' }
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
  document.title = to.meta.title ? `${to.meta.title} - RiverFlow` : 'RiverFlow · 河狸流程编排平台'

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
