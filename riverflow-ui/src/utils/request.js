import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg || error.message || '网络请求异常'

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      handleUnauthorized()
    } else {
      ElMessage.error(msg)
    }

    return Promise.reject(error)
  }
)

/**
 * 统一处理未授权：清除 Token 并跳转登录页
 */
function handleUnauthorized() {
  const userStore = useUserStore()
  userStore.clearToken()
  // 使用 window.location 进行全页跳转，避免路由守卫拦截
  window.location.href = '/login'
}

export default service
