import ToolbarItem from '../../components/ToolbarItem.vue'
import type { Editor } from '@tiptap/vue-3'
import type { ExtensionOptions } from '../../types'
import type { BoldOptions } from '@tiptap/extension-bold'
import { Bold as TiptapBold } from '@tiptap/extension-bold'
import { markRaw } from 'vue'
import { Edit } from '@element-plus/icons-vue'
import type { BubbleMenuItemType } from '../../types'
import MdiFormatBold from '~icons/mdi/format-bold'

const Bold = TiptapBold.extend<ExtensionOptions & BoldOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 40,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive('bold'),
            icon: markRaw(Edit),
            title: '加粗',
            action: () => {
              editor.chain().focus().toggleBold().run()
            },
          },
        }
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 100,
            props: {
              isActive: () => editor.isActive('bold'),
              icon: markRaw(MdiFormatBold),
              title: '粗体 (Ctrl+B)',
              action: () => editor.chain().focus().toggleBold().run(),
            },
          },
        ]
      },
    }
  },
})

export default Bold