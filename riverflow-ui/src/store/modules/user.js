import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  const token = ref(Cookies.get('riverflow-token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  // 用户权限标识集合
  const permissions = computed(() => {
    return userInfo.value?.permissions || []
  })

  // 用户角色编码集合
  const roles = computed(() => {
    return userInfo.value?.roles || []
  })

  // 用户菜单树
  const menus = computed(() => {
    return userInfo.value?.menus || []
  })

  // 是否为超级管理员
  const isAdmin = computed(() => {
    return userInfo.value?.admin === true || roles.value.includes('admin')
  })

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

  /**
   * 判断是否拥有指定权限
   * @param {string|string[]} perm 权限标识或权限标识数组
   */
  function hasPermission(perm) {
    if (isAdmin.value) return true
    if (!perm) return false
    const perms = Array.isArray(perm) ? perm : [perm]
    return perms.every(p => permissions.value.includes(p))
  }

  /**
   * 判断是否拥有任意一个权限
   * @param {string|string[]} perm
   */
  function hasAnyPermission(perm) {
    if (isAdmin.value) return true
    if (!perm) return false
    const perms = Array.isArray(perm) ? perm : [perm]
    return perms.some(p => permissions.value.includes(p))
  }

  /**
   * 判断是否拥有指定角色
   * @param {string|string[]} role
   */
  function hasRole(role) {
    if (isAdmin.value) return true
    if (!role) return false
    const rolesArr = Array.isArray(role) ? role : [role]
    return rolesArr.every(r => roles.value.includes(r))
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    menus,
    isAdmin,
    isLoggedIn,
    setToken,
    clearToken,
    setUserInfo,
    hasPermission,
    hasAnyPermission,
    hasRole
  }
})
