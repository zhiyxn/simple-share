import ToolbarItem from '../../components/ToolbarItem.vue'
import type { Editor } from '@tiptap/vue-3'
import type { ExtensionOptions } from '../../types'
import type { ItalicOptions } from '@tiptap/extension-italic'
import { Italic as TiptapItalic } from '@tiptap/extension-italic'
import { markRaw } from 'vue'
import { EditPen } from '@element-plus/icons-vue'
import type { BubbleMenuItemType } from '../../types'
import MdiFormatItalic from '~icons/mdi/format-italic'

const Italic = TiptapItalic.extend<ExtensionOptions & ItalicOptions>({
  addKeyboardShortcuts() {
    return {
      'Mod-i': () => this.editor.commands.toggleItalic(),
      'Mod-I': () => this.editor.commands.toggleItalic(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 50,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive('italic'),
            icon: markRaw(EditPen),
            title: '斜体',
            action: () => {
              editor.chain().focus().toggleItalic().run()
            },
          },
        }
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 90,
            props: {
              isActive: () => editor.isActive('italic'),
              icon: markRaw(MdiFormatItalic),
              title: '斜体 (Ctrl+I)',
              action: () => editor.chain().focus().toggleItalic().run(),
            },
          },
        ]
      },
    }
  },
})

export default Italic