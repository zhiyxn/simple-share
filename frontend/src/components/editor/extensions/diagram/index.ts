import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import DiagramView from './DiagramView.vue'
import { TrendCharts } from '@element-plus/icons-vue'

const Diagram = Node.create({
  name: 'diagram',
  group: 'block',
  atom: true,
  draggable: true,

  addOptions() {
    return {
      getCommandMenuItems() {
        return {
          priority: 130,
          icon: markRaw(TrendCharts),
          title: '文本绘图',
          keywords: ['diagram', 'mermaid', 'chart', 'tubiao', 'liuchengtu'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setDiagram({ content: 'graph TD\n    A[开始] --> B[处理]\n    B --> C[结束]' })
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 70,
          icon: markRaw(TrendCharts),
          title: '文本绘图',
          description: '使用Mermaid语法创建流程图、时序图等',
          action: () => {
            editor.chain().focus().setDiagram({ content: 'graph TD\n    A[开始] --> B[处理]\n    B --> C[结束]' }).run()
          },
        }
      },
    }
  },

  addAttributes() {
    return {
      content: {
        default: '',
        parseHTML: element => element.getAttribute('data-content') || '',
        renderHTML: attributes => {
          return { 'data-content': attributes.content }
        },
      },
      type: {
        default: 'mermaid',
        parseHTML: element => element.getAttribute('data-type') || 'mermaid',
        renderHTML: attributes => {
          return { 'data-type': attributes.type }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="diagram"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', { ...HTMLAttributes, 'data-type': 'diagram' }]
  },

  addNodeView() {
    return VueNodeViewRenderer(DiagramView)
  },

  addCommands() {
    return {
      setDiagram: (options = { content: '', type: 'mermaid' }) => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: options,
        })
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-m': () => this.editor.commands.setDiagram({ content: 'graph TD\n    A[开始] --> B[处理]\n    B --> C[结束]' }),
    }
  },
})

export default Diagram