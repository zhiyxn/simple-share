import TiptapStrike from '@tiptap/extension-strike'
import { mergeAttributes } from '@tiptap/core'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import MdiFormatStrikethrough from '~icons/mdi/format-strikethrough'

const Strike = TiptapStrike.extend({
  renderHTML({ HTMLAttributes }) {
    return ['s', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-s': () => this.editor.commands.toggleStrike(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return [{
          priority: 90,
          icon: markRaw(MdiFormatStrikethrough),
          title: '删除线',
          keywords: ['strike', 'strikethrough', '删除线', '划线'],
          command: ({ editor, range }) => {
            editor.chain().focus().deleteRange(range).toggleStrike().run()
          },
        }]
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 80,
            props: {
              isActive: () => editor.isActive('strike'),
              icon: markRaw(MdiFormatStrikethrough),
              title: '删除线 (Ctrl+Shift+S)',
              action: () => editor.chain().focus().toggleStrike().run(),
            },
          },
        ]
      },
    }
  },
})

export default Strike
