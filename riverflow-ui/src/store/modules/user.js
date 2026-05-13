import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  const token = ref(Cookies.get('riverflow-token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(val) {
    token.value = val
    Cookies.set('riverflow-token', val, { expires: 1 })
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    Cookies.remove('riverflow-token')
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    clearToken,
    setUserInfo
  }
})
