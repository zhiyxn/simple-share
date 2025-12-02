import { Node, mergeAttributes } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import MathBlockView from './MathBlockView.vue'

const decodeAttribute = (value: string | null | undefined) => {
  if (!value) return ''
  return value
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
}

const encodeAttribute = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

const escapeContent = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

const extractLatex = (element: Element) => {
  const attr = decodeAttribute(element.getAttribute('data-latex'))
  if (attr) {
    return attr
  }
  const raw = element.textContent ?? ''
  return raw
    .replace(/^\s*\$\$\s*/, '')
    .replace(/\s*\$\$\s*$/, '')
    .trim()
}

const MathBlock = Node.create({
  name: 'mathBlock',
  group: 'block',
  atom: true,
  draggable: true,
  selectable: true,

  addAttributes() {
    return {
      latex: {
        default: '',
        parseHTML: element => extractLatex(element),
        renderHTML: attributes => ({
          'data-latex': encodeAttribute(attributes.latex ?? ''),
        }),
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div.md-math-block',
      },
    ]
  },

  renderHTML({ node, HTMLAttributes }) {
    const attrs = { ...HTMLAttributes }
    delete attrs.latex

    const latex: string = node.attrs.latex ?? ''
    const encodedLatex = encodeAttribute(latex)
    const className = attrs.class ? `${attrs.class} md-math-block` : 'md-math-block'

    return [
      'div',
      mergeAttributes(attrs, {
        class: className,
        'data-latex': encodedLatex,
      }),
      `$$\n${escapeContent(latex)}\n$$`,
    ]
  },

  addNodeView() {
    return VueNodeViewRenderer(MathBlockView)
  },

  addOptions() {
    const extensionName = this.name
    const defaultLatex = '\\int_{a}^{b} f(x) \\, dx'

    const insertMathBlock = (editorInstance: any, options?: { range?: { from: number; to: number } }) => {
      if (!editorInstance || !extensionName) {
        return
      }

      let chain = editorInstance.chain().focus()

      const range = options?.range
      if (range && typeof range.from === 'number' && typeof range.to === 'number') {
        chain = chain.deleteRange(range)
      }

      chain
        .insertContent({
          type: extensionName,
          attrs: { latex: defaultLatex },
        })
        .run()
    }

    return {
      getCommandMenuItems() {
        return {
          priority: 90,
          icon: 'formula',
          title: '数学公式块',
          keywords: ['math', 'latex', 'gongshi'],
          command: ({ editor, range }) => insertMathBlock(editor, { range }),
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 45,
          icon: 'formula',
          title: '数学公式',
          description: '插入 KaTeX 数学公式块',
          action: () => insertMathBlock(editor),
        }
      },
    }
  },
})

export default MathBlock
