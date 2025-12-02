import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

type PermissionValue = string | string[] | undefined

const resolveRequiredPermissions = (value: PermissionValue): string[] => {
  if (!value) {
    return []
  }
  if (Array.isArray(value)) {
    return value
  }
  return [value]
}

const hasPermission = (value: PermissionValue): boolean => {
  const requiredPerms = resolveRequiredPermissions(value)
  if (requiredPerms.length === 0) {
    return true
  }

  const userStore = useUserStore()
  if (userStore.isAdmin) {
    return true
  }

  const userPerms = userStore.userInfo?.permissions || []
  if (!userPerms.length) {
    return false
  }

  return requiredPerms.some((perm) => userPerms.includes(perm))
}

const resolveTargetElement = (node: Node): HTMLElement | null => {
  if (node instanceof HTMLElement) {
    return node
  }

  let current: Node | null = node.nextSibling
  while (current) {
    if (current instanceof HTMLElement) {
      return current
    }
    current = current.nextSibling
  }

  return null
}

const cleanup = (el: Node) => {
  const target = resolveTargetElement(el)

  if (target && target.parentNode) {
    target.parentNode.removeChild(target)
  }

  if (!(el instanceof HTMLElement) && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

const checkAndToggle = (el: Node, binding: DirectiveBinding<PermissionValue>) => {
  if (!hasPermission(binding.value)) {
    cleanup(el)
  }
}

export const hasPermi: Directive<any, PermissionValue> = {
  mounted(el, binding) {
    checkAndToggle(el, binding)
  },
  updated(el, binding) {
    checkAndToggle(el, binding)
  }
}

export default hasPermi
