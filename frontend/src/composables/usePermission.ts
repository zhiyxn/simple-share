import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

type PermissionValue = string | string[] | undefined

const toArray = (value: PermissionValue): string[] => {
  if (!value) {
    return []
  }
  if (Array.isArray(value)) {
    return value
  }
  return [value]
}

export const usePermission = () => {
  const userStore = useUserStore()

  const hasPermission = (value: PermissionValue): boolean => {
    const requiredPerms = toArray(value)
    if (requiredPerms.length === 0) {
      return true
    }
    if (userStore.isAdmin) {
      return true
    }
    const userPerms = userStore.userInfo?.permissions || []
    if (!userPerms.length) {
      return false
    }
    return requiredPerms.some((perm) => userPerms.includes(perm))
  }

  const permissionMap = computed(() => new Set(userStore.userInfo?.permissions || []))

  const ensurePermission = (value: PermissionValue) => hasPermission(value)

  return {
    hasPermission,
    ensurePermission,
    permissionMap
  }
}

export default usePermission
