import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { markRaw } from 'vue'
import type { ExtensionOptions } from '../../types'
import { Grid } from '@element-plus/icons-vue'
import { TableCellWithAlignment, TableHeaderWithAlignment } from './cellExtensions'

const ExtendedTable = Table.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      resizable: true,
      allowTableNodeSelection: true,
      getCommandMenuItems() {
        return {
          priority: 120,
          icon: markRaw(Grid),
          title: '表格',
          keywords: ['table', 'biaoge', '表格'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 50,
          component: markRaw(Grid),
          props: {
            editor,
            icon: markRaw(Grid),
            title: '表格',
            description: '插入表格',
            action: () => {
              editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
            },
          },
        }
      },
    }
  },
})

export { Table, TableRow, TableHeaderWithAlignment as TableHeader, TableCellWithAlignment as TableCell }

export default [
  ExtendedTable,
  TableRow,
  TableHeaderWithAlignment,
  TableCellWithAlignment,
]
