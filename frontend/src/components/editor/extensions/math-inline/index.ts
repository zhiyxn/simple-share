import { Node, mergeAttributes } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import MathInlineView from './MathInlineView.vue'

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
  if (attr) return attr
  const raw = element.textContent ?? ''
  return raw.replace(/^\s*\$\s*/, '').replace(/\s*\$\s*$/, '').trim()
}

const MathInline = Node.create({
  name: 'mathInline',
  group: 'inline',
  inline: true,
  atom: true,
  selectable: true,
  draggable: false,

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
        tag: 'span.md-math-inline',
      },
    ]
  },

  renderHTML({ node, HTMLAttributes }) {
    const attrs = { ...HTMLAttributes }
    delete attrs.latex

    const latex: string = node.attrs.latex ?? ''
    const encodedLatex = encodeAttribute(latex)
    const className = attrs.class ? `${attrs.class} md-math-inline` : 'md-math-inline'

    return [
      'span',
      mergeAttributes(attrs, {
        class: className,
        'data-latex': encodedLatex,
      }),
      `$${escapeContent(latex)}$`,
    ]
  },

  addNodeView() {
    return VueNodeViewRenderer(MathInlineView)
  },
})

export default MathInline
