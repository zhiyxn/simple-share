import TiptapUnderline from '@tiptap/extension-underline'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import MdiFormatUnderline from '~icons/mdi/format-underline'

const Underline = TiptapUnderline.extend({
  addKeyboardShortcuts() {
    return {
      'Mod-u': () => this.editor.commands.toggleUnderline(),
      'Mod-U': () => this.editor.commands.toggleUnderline(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 75,
            props: {
              isActive: () => editor.isActive('underline'),
              icon: markRaw(MdiFormatUnderline),
              title: '下划线 (Ctrl+U)',
              action: () => editor.chain().focus().toggleUnderline().run(),
            },
          },
        ]
      },
    }
  },
})

export default Underline