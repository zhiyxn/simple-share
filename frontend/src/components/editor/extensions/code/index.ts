import TiptapCode from '@tiptap/extension-code'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import MdiCodeTags from '~icons/mdi/code-tags'

const Code = TiptapCode.extend({
  renderHTML({ HTMLAttributes }) {
    return ['code', HTMLAttributes, 0]
  },

  addKeyboardShortcuts() {
    return {
      'Mod-e': () => this.editor.commands.toggleCode(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return [{
          priority: 85,
          icon: markRaw(MdiCodeTags),
          title: '行内代码',
          keywords: ['code', 'inline', '代码', '行内'],
          command: ({ editor, range }) => {
            editor.chain().focus().deleteRange(range).toggleCode().run()
          },
        }]
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 65,
            props: {
              isActive: () => editor.isActive('code'),
              icon: markRaw(MdiCodeTags),
              title: '行内代码 (Ctrl+E)',
              action: () => editor.chain().focus().toggleCode().run(),
            },
          },
        ]
      },
    }
  },
})

export default Code
