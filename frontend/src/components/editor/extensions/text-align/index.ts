import TiptapTextAlign from '@tiptap/extension-text-align'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import MdiFormatAlignLeft from '~icons/mdi/format-align-left'
import MdiFormatAlignCenter from '~icons/mdi/format-align-center'
import MdiFormatAlignRight from '~icons/mdi/format-align-right'

const TextAlign = TiptapTextAlign.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      types: ['heading', 'paragraph'],
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 60,
            props: {
              isActive: () => editor.isActive({ textAlign: 'left' }) || !editor.getAttributes('paragraph').textAlign,
              icon: markRaw(MdiFormatAlignLeft),
              title: '左对齐 (Ctrl+Shift+L)',
              action: () => editor.chain().focus().setTextAlign('left').run(),
            },
          },
          {
            priority: 59,
            props: {
              isActive: () => editor.isActive({ textAlign: 'center' }),
              icon: markRaw(MdiFormatAlignCenter),
              title: '居中对齐 (Ctrl+Shift+E)',
              action: () => editor.chain().focus().setTextAlign('center').run(),
            },
          },
          {
            priority: 58,
            props: {
              isActive: () => editor.isActive({ textAlign: 'right' }),
              icon: markRaw(MdiFormatAlignRight),
              title: '右对齐 (Ctrl+Shift+R)',
              action: () => editor.chain().focus().setTextAlign('right').run(),
            },
          },
        ]
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-l': () => this.editor.commands.setTextAlign('left'),
      'Mod-Shift-e': () => this.editor.commands.setTextAlign('center'),
      'Mod-Shift-r': () => this.editor.commands.setTextAlign('right'),
    }
  },
})

export default TextAlign
