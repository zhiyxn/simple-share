import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'

type AlignValue = 'left' | 'center' | 'right'

const createAlignableCell = <T extends typeof TableCell | typeof TableHeader>(Base: T) =>
  Base.extend({
    addAttributes() {
      const parentAttributes = this.parent?.() ?? {}

      return {
        ...parentAttributes,
        textAlign: {
          default: 'left' satisfies AlignValue,
          parseHTML: element =>
            (element.getAttribute('data-align') as AlignValue | null) ||
            ((element.style?.textAlign as AlignValue | undefined) ?? 'left'),
          renderHTML: attributes => {
            const alignment = (attributes.textAlign as AlignValue | undefined) || 'left'

            if (!alignment || alignment === 'left') {
              return {}
            }

            return {
              'data-align': alignment,
              style: `text-align: ${alignment};`,
            }
          },
        },
      }
    },
  })

export const TableCellWithAlignment = createAlignableCell(TableCell)
export const TableHeaderWithAlignment = createAlignableCell(TableHeader)
