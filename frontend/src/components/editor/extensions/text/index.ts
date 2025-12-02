import { Text as TiptapText } from '@tiptap/extension-text'
import type { Editor } from '@tiptap/vue-3'
import type { ExtensionOptions, BubbleMenuItemType } from '../../types'

const Text = TiptapText.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        // 从所有扩展中收集气泡菜单项，但排除text扩展自身以避免无限递归
        const items: BubbleMenuItemType[] = []
        
        editor.extensionManager.extensions.forEach((extension) => {
          // 跳过text扩展自身，避免无限递归
          if (extension.name === 'text') {
            return
          }
          
          const bubbleMenuOptions = extension.options?.getBubbleMenu?.({ editor })
          if (bubbleMenuOptions && Array.isArray(bubbleMenuOptions)) {
            items.push(...bubbleMenuOptions)
          }
        })
        
        // 按优先级排序
        return items.sort((a, b) => (b.priority || 0) - (a.priority || 0))
      },
    }
  },
})

export default Text