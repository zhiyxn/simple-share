import TiptapBulletList from '@tiptap/extension-bullet-list'
import { mergeAttributes } from '@tiptap/core'
import { markRaw } from 'vue'
import type { CommandMenuItemType } from '../../types'
import MdiFormatListBulleted from '~icons/mdi/format-list-bulleted'

const BulletList = TiptapBulletList.extend({
  renderHTML({ HTMLAttributes }) {
    return ['ul', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems(): CommandMenuItemType[] {
        return [
          {
            priority: 80,
            icon: markRaw(MdiFormatListBulleted),
            title: '无序列表',
            keywords: ['bullet', 'list', '无序', '列表', '项目'],
            command: ({ editor, range }) => {
              editor.chain().focus().deleteRange(range).toggleBulletList().run()
            },
          },
        ]
      },
      getToolboxItems({ editor }) {
        return {
          priority: 50,
          icon: markRaw(MdiFormatListBulleted),
          title: '无序列表',
          description: '插入无序列表',
          action: () => {
            editor.chain().focus().toggleBulletList().run()
          },
        }
      },
    }
  },
})

export default BulletList
