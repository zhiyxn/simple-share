import type { Extensions, Extension } from '@tiptap/vue-3'

export function useExtension() {
  const filterDuplicateExtensions = (extensions: Extensions | undefined) => {
    if (!extensions) {
      return
    }
    const resolvedExtensions = sort(extensions)
    const map = new Map<string, Extension>()
    
    resolvedExtensions.forEach((extension) => {
      if (!extension.name) {
        console.warn(
          `Extension name is missing for Extension, type: ${extension.type}.`
        )
        const key = crypto.randomUUID()
        map.set(key, extension)
        return
      }
      const key = `${extension.type}-${extension.name}`
      if (map.has(key)) {
        console.warn(
          `Duplicate found for Extension, type: ${extension.type}, name: ${extension.name}. Keeping the later one.`
        )
      }
      map.set(key, extension)
    })
    return Array.from(map.values())
  }

  /**
   * Sort extensions by priority.
   * @param extensions An array of Tiptap extensions
   * @returns A sorted array of Tiptap extensions by priority
   */
  const sort = (extensions: Extensions): Extensions => {
    const defaultPriority = 100

    return extensions.sort((a, b) => {
      const priorityA = (a as any).priority || defaultPriority
      const priorityB = (b as any).priority || defaultPriority

      if (priorityA > priorityB) {
        return -1
      }

      if (priorityA < priorityB) {
        return 1
      }

      return 0
    })
  }

  return {
    filterDuplicateExtensions,
  }
}