import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import IframeView from './IframeView.vue'

const Iframe = Node.create({
  name: 'iframe',
  group: 'block',
  atom: true,
  draggable: true,

  addAttributes() {
    return {
      src: {
        default: '',
        parseHTML: element => element.getAttribute('src') || '',
        renderHTML: attributes => {
          return { src: attributes.src }
        },
      },
      width: {
        default: '100%',
        parseHTML: element => element.getAttribute('width') || '100%',
        renderHTML: attributes => {
          return { width: attributes.width }
        },
      },
      height: {
        default: '400px',
        parseHTML: element => element.getAttribute('height') || '400px',
        renderHTML: attributes => {
          return { height: attributes.height }
        },
      },
      title: {
        default: '',
        parseHTML: element => element.getAttribute('title') || '',
        renderHTML: attributes => {
          return { title: attributes.title }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'iframe',
      },
      {
        tag: 'div[data-type="iframe"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['iframe', HTMLAttributes]
  },

  addNodeView() {
    return VueNodeViewRenderer(IframeView)
  },

  addCommands() {
    return {
      setIframe: (attrs = {}) => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs,
        })
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-i': () => this.editor.commands.setIframe(),
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 90,
          icon: 'link',
          title: '嵌入网页',
          keywords: ['iframe', 'web', 'qianru', 'wangye'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setIframe()
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 40,
          icon: 'link',
          title: '嵌入网页',
          description: '插入外部网页或应用',
          action: () => {
            editor.chain().focus().setIframe().run()
          },
        }
      },
    }
  },
})

export default Iframe