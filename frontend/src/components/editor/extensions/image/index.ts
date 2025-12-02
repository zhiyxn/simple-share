import { mergeAttributes } from '@tiptap/core'
import { Image as TiptapImage } from '@tiptap/extension-image'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import type { ExtensionOptions } from '../../types'
import ImageView from './ImageView.vue'
import { getImageBorderStyleConfig, type ImageBorderStyle } from './presets'

// 图标组件
import { Picture } from '@element-plus/icons-vue'

const DEFAULT_WIDTH = '70%'
const DEFAULT_ALIGNMENT: 'left' | 'center' | 'right' = 'center'
const DEFAULT_BORDER_STYLE: ImageBorderStyle = 'none'

const alignmentToStyles = (alignment: string | undefined) => {
  const baseInlineStyles = [
    'display: inline-flex',
    'flex-direction: column',
    'vertical-align: top',
    'gap: 8px',
    'margin: 4px 6px'
  ]

  switch (alignment) {
    case 'center':
      return [
        'display: flex',
        'flex-direction: column',
        'align-items: center',
        'justify-content: center',
        'width: 100%',
        'margin: 12px auto'
      ]
    case 'right':
      return [...baseInlineStyles, 'align-items: flex-end', 'justify-content: center']
    default:
      return [...baseInlineStyles, 'align-items: flex-start', 'justify-content: center']
  }
}

export default TiptapImage.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 90,
          icon: markRaw(Picture),
          title: '图片',
          keywords: ['image', 'tupian'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent([{ type: 'image', attrs: { src: '' } }])
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 10,
          icon: markRaw(Picture),
          title: '图片',
          description: '插入图片',
          action: () => {
            editor.chain().focus().insertContent([{ type: 'image', attrs: { src: '' } }]).run()
          },
        }
      },
    }
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: DEFAULT_WIDTH,
        parseHTML: element => element.getAttribute('data-image-width') ?? element.getAttribute('width') ?? DEFAULT_WIDTH,
        renderHTML: attributes => ({
          'data-image-width': attributes.width ?? DEFAULT_WIDTH,
        }),
      },
      height: {
        default: null,
        parseHTML: element => element.getAttribute('data-image-height') ?? element.getAttribute('height'),
        renderHTML: attributes => ({
          'data-image-height': attributes.height ?? undefined,
        }),
      },
      file: {
        default: null,
        renderHTML() {
          return {}
        },
        parseHTML() {
          return null
        },
      },
      textAlign: {
        default: DEFAULT_ALIGNMENT,
        renderHTML: attributes => ({
          'data-image-align': attributes.textAlign ?? DEFAULT_ALIGNMENT,
        }),
      },
      borderStyle: {
        default: DEFAULT_BORDER_STYLE,
        renderHTML: attributes => ({
          'data-image-border': attributes.borderStyle ?? DEFAULT_BORDER_STYLE,
        }),
      },
    }
  },

  renderHTML({ HTMLAttributes }) {
    const {
      width,
      height,
      textAlign,
      borderStyle,
      style,
      ...rest
    } = HTMLAttributes

    const resolvedWidth = width ?? DEFAULT_WIDTH
    const resolvedAlignment = (textAlign ?? DEFAULT_ALIGNMENT) as 'left' | 'center' | 'right'
    const resolvedBorderStyle = (borderStyle ?? DEFAULT_BORDER_STYLE) as ImageBorderStyle

    const styleParts: string[] = []
    if (style) {
      styleParts.push(style)
    }
    if (resolvedWidth) {
      styleParts.push(`width: ${resolvedWidth}`)
    }
    if (height) {
      styleParts.push(`height: ${height}`)
    }

    const alignmentStyles = alignmentToStyles(resolvedAlignment)
    styleParts.push(...alignmentStyles)

    const borderConfig = getImageBorderStyleConfig(resolvedBorderStyle)
    Object.entries(borderConfig.image).forEach(([key, value]) => {
      if (value) {
        styleParts.push(`${key}: ${value}`)
      }
    })

    const mergedAttributes = mergeAttributes(
      this.options.HTMLAttributes,
      rest,
      {
        style: styleParts.join('; '),
      },
    )

    return ['img', mergedAttributes]
  },

  addNodeView() {
    return VueNodeViewRenderer(ImageView)
  },
})
