import { watchEffect } from 'vue'
import { hasPermission, hasAnyPermission, hasRole } from '@/utils/permission'

/**
 * 权限指令 v-permission="'system:user:add'"
 * 权限指令 v-permission="['system:user:add', 'system:user:edit']"
 *
 * 修饰符 .any 表示拥有任意一个权限即可
 * 修饰符 .role 表示按角色校验
 *
 * 采用 display:none 控制显隐，支持用户权限变化后自动重新评估
 */
export const permission = {
  mounted(el, binding) {
    const { value, modifiers } = binding
    if (!value) return

    // 记录元素原始 display，便于有权限时恢复
    el._permissionOriginalDisplay = el.style.display

    el._permissionStop = watchEffect(() => {
      let hasAuth = false
      if (modifiers.role) {
        hasAuth = hasRole(value)
      } else if (modifiers.any) {
        hasAuth = hasAnyPermission(value)
      } else {
        hasAuth = hasPermission(value)
      }
      el.style.display = hasAuth ? (el._permissionOriginalDisplay || '') : 'none'
    })
  },
  unmounted(el) {
    if (el._permissionStop) {
      el._permissionStop()
    }
  }
}

export default {
  install(app) {
    app.directive('permission', permission)
  }
}
