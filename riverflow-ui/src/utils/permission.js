import { useUserStore } from '@/store/modules/user'

/**
 * 判断是否拥有指定权限
 * @param {string|string[]} value
 * @returns {boolean}
 */
export function hasPermission(value) {
  const userStore = useUserStore()
  return userStore.hasPermission(value)
}

/**
 * 判断是否拥有任意一个权限
 * @param {string|string[]} value
 * @returns {boolean}
 */
export function hasAnyPermission(value) {
  const userStore = useUserStore()
  return userStore.hasAnyPermission(value)
}

/**
 * 判断是否拥有指定角色
 * @param {string|string[]} value
 * @returns {boolean}
 */
export function hasRole(value) {
  const userStore = useUserStore()
  return userStore.hasRole(value)
}
