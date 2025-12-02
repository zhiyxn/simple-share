import type { Extensions } from '@tiptap/core'

/**
 * 过滤重复的扩展
 */
export function filterDuplicateExtensions(extensions: Extensions): Extensions {
  if (!extensions) {
    return []
  }

  const resolvedExtensions = sortExtensions(
    extensions.filter((item) => Boolean(item?.name)) as Extensions
  )
  const map = new Map<string, any>()

  resolvedExtensions.forEach((extension) => {
    if (!extension?.name) {
      return
    }

    const key = `${extension.type}-${extension.name}`
    map.set(key, extension)
  })

  return Array.from(map.values())
}

/**
 * 按优先级排序扩展
 */
function sortExtensions(extensions: Extensions): Extensions {
  const defaultPriority = 100

  return extensions.sort((a, b) => {
    const priorityA = a.priority || defaultPriority
    const priorityB = b.priority || defaultPriority

    if (priorityA > priorityB) {
      return -1
    }

    if (priorityA < priorityB) {
      return 1
    }

    return 0
  })
}
