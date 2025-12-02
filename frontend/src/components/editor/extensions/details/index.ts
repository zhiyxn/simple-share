import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import DetailsView from './DetailsView.vue'
import { ArrowDown } from '@element-plus/icons-vue'

const DetailsSummary = Node.create({
  name: 'detailsSummary',
  content: 'inline*',
  defining: true,
  parseHTML() {
    return [{ tag: 'summary' }]
  },
  renderHTML({ HTMLAttributes }) {
    return ['summary', HTMLAttributes, 0]
  },
})

const DetailsContent = Node.create({
  name: 'detailsContent',
  content: 'block+',
  defining: true,
  parseHTML() {
    return [{ tag: 'div[data-type="detailsContent"]' }]
  },
  renderHTML({ HTMLAttributes }) {
    return ['div', { ...HTMLAttributes, 'data-type': 'detailsContent' }, 0]
  },
})

const Details = Node.create({
  name: 'details',
  group: 'block',
  content: 'detailsSummary detailsContent',
  defining: true,
  isolating: true,

  addOptions() {
    return {
      getCommandMenuItems() {
        return {
          priority: 110,
          icon: markRaw(ArrowDown),
          title: '折叠内容',
          keywords: ['details', 'zheshe', 'collapse'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setDetails()
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 60,
          icon: markRaw(ArrowDown),
          title: '折叠内容',
          description: '插入可折叠的内容块',
          action: () => {
            editor.chain().focus().setDetails().run()
          },
        }
      },
    }
  },

  addAttributes() {
    return {
      open: {
        default: false,
        parseHTML: element => element.hasAttribute('open'),
        renderHTML: attributes => {
          if (!attributes.open) {
            return {}
          }
          return { open: '' }
        },
      },
    }
  },

  parseHTML() {
    return [{ tag: 'details' }]
  },

  renderHTML({ HTMLAttributes }) {
    return ['details', HTMLAttributes, 0]
  },

  addNodeView() {
    return VueNodeViewRenderer(DetailsView)
  },

  addCommands() {
    return {
      setDetails: () => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: { open: true },
          content: [
            {
              type: 'detailsSummary',
              content: [{ type: 'text', text: '点击展开/收起' }],
            },
            {
              type: 'detailsContent',
              content: [{ type: 'paragraph' }],
            },
          ],
        })
      },
      unsetDetails: () => ({ commands }) => {
        return commands.lift('details')
      },
      toggleDetails: () => ({ commands }) => {
        return commands.updateAttributes('details', { open: !this.editor.getAttributes('details').open })
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-d': () => this.editor.commands.setDetails(),
    }
  },
})

export { Details, DetailsSummary, DetailsContent }
export default Details