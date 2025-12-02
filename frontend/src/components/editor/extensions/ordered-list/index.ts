import TiptapOrderedList from '@tiptap/extension-ordered-list'
import TiptapListItem from '@tiptap/extension-list-item'
import { mergeAttributes } from '@tiptap/core'
import { markRaw } from 'vue'
import type { CommandMenuItemType } from '../../types'
import MdiFormatListNumbered from '~icons/mdi/format-list-numbered'

const OrderedList = TiptapOrderedList.extend({
  renderHTML({ HTMLAttributes }) {
    return ['ol', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems(): CommandMenuItemType[] {
        return [
          {
            priority: 75,
            icon: markRaw(MdiFormatListNumbered),
            title: '有序列表',
            keywords: ['ordered', 'list', '有序', '列表', '数字'],
            command: ({ editor, range }) => {
              editor.chain().focus().deleteRange(range).toggleOrderedList().run()
            },
          },
        ]
      },
      getToolboxItems({ editor }) {
        return {
          priority: 55,
          icon: markRaw(MdiFormatListNumbered),
          title: '有序列表',
          description: '插入有序列表',
          action: () => {
            editor.chain().focus().toggleOrderedList().run()
          },
        }
      },
    }
  },
})

const ListItem = TiptapListItem.extend({
  renderHTML({ HTMLAttributes }) {
    return ['li', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {
        class: 'list-item',
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      ...this.parent?.(),
      Enter: () => this.editor.commands.splitListItem(this.name),
      'Shift-Tab': () => this.editor.commands.liftListItem(this.name),
      Tab: () => this.editor.commands.sinkListItem(this.name),
    }
  },
})

export default OrderedList
export { ListItem }
