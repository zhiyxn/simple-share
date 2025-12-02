import { Blockquote as TiptapBlockquote } from '@tiptap/extension-blockquote'
import { mergeAttributes } from '@tiptap/core'
import { markRaw } from 'vue'
import type { ExtensionOptions } from '../../types'
import MdiFormatQuoteClose from '~icons/mdi/format-quote-close'

export default TiptapBlockquote.extend<ExtensionOptions>({
  renderHTML({ HTMLAttributes }) {
    return ['blockquote', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return [{
          priority: 95,
          icon: markRaw(MdiFormatQuoteClose),
          title: '引用块',
          keywords: ['blockquote', 'quote', '引用', '引用块'],
          command: ({ editor, range }) => {
            editor.chain().focus().deleteRange(range).setBlockquote().run()
          },
        }]
      },
      getToolboxItems({ editor }) {
        return {
          priority: 40,
          icon: markRaw(MdiFormatQuoteClose),
          title: '引用块',
          description: '插入引用块',
          action: () => {
            editor.chain().focus().setBlockquote().run()
          },
        }
      },
    }
  },
})
