import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import MarkdownBlockView from './MarkdownBlockView.vue'

const MarkdownBlock = Node.create({
  name: 'markdownBlock',
  group: 'block',
  atom: true,
  draggable: true,

  addAttributes() {
    return {
      content: {
        default: '',
        parseHTML: element => element.getAttribute('data-content') || '',
        renderHTML: attributes => {
          return { 'data-content': attributes.content }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="markdown-block"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', { ...HTMLAttributes, 'data-type': 'markdown-block' }]
  },

  addNodeView() {
    return VueNodeViewRenderer(MarkdownBlockView)
  },

  addCommands() {
    return {
      setMarkdownBlock: (content = '') => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: { content },
        })
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-m': () => this.editor.commands.setMarkdownBlock(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 85,
          icon: 'markdown',
          title: 'Markdown 代码块',
          keywords: ['markdown', 'md', 'daima'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setMarkdownBlock()
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 35,
          icon: 'markdown',
          title: 'Markdown 代码块',
          description: '插入可编辑的 Markdown 代码块',
          action: () => {
            editor.chain().focus().setMarkdownBlock().run()
          },
        }
      },
    }
  },
})

export default MarkdownBlock