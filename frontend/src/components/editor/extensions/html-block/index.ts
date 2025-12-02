import { Extension } from '@tiptap/core'
import type { ExtensionOptions } from '../../types'
import { markRaw } from 'vue'
import { Document } from '@element-plus/icons-vue'

const HtmlCodeBlock = Extension.create<ExtensionOptions>({
  name: 'htmlCodeBlockShortcut',

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 81,
          icon: markRaw(Document),
          title: 'HTML 代码',
          keywords: ['html', 'code', 'daima'],
          command: ({ editor, range }) => {
            editor.chain().focus().deleteRange(range).setCodeBlock({ language: 'html' }).run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 26,
          icon: markRaw(Document),
          title: 'HTML 代码',
          description: '插入 HTML 代码块',
          action: () => {
            editor.chain().focus().setCodeBlock({ language: 'html' }).run()
          },
        }
      },
    }
  },

  addCommands() {
    return {
      setHtmlCodeBlock:
        () =>
        ({ commands }) =>
          commands.setCodeBlock({ language: 'html' }),
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-h': () => this.editor.commands.setCodeBlock({ language: 'html' }),
    }
  },
})

export default HtmlCodeBlock
