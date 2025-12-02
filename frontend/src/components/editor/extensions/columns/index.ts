import { Node, mergeAttributes, findParentNode } from '@tiptap/core'
import { EditorState, TextSelection } from '@tiptap/pm/state'
import { Node as PMNode } from '@tiptap/pm/model'
import { markRaw } from 'vue'

// 单列扩展
const Column = Node.create({
  name: 'column',
  content: 'block+',
  isolating: true,

  addOptions() {
    return {
      HTMLAttributes: {
        class: 'column',
      },
    }
  },

  addAttributes() {
    return {
      index: {
        default: 0,
        parseHTML: element => element.getAttribute('data-index'),
        renderHTML: attributes => {
          return { 'data-index': attributes.index }
        },
      },
      style: {
        default: 'min-width: 0; flex: 1 1; box-sizing: border-box;',
        parseHTML: element => element.getAttribute('style'),
        renderHTML: attributes => {
          return { style: attributes.style }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[class=column]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'div',
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ]
  },
})

// 分栏容器扩展
const Columns = Node.create({
  name: 'columns',
  group: 'block',
  priority: 10,
  defining: true,
  isolating: true,
  allowGapCursor: true,
  content: 'column{1,}',

  addOptions() {
    return {
      HTMLAttributes: {
        class: 'columns',
      },
    }
  },

  addAttributes() {
    return {
      cols: {
        default: 2,
        parseHTML: element => {
          const cols = element.getAttribute('data-cols')
          return cols ? parseInt(cols, 10) : 2
        },
        renderHTML: attributes => {
          return { 'data-cols': attributes.cols }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[class=columns]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'div',
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ]
  },

  addCommands() {
    return {
      insertColumns: (attrs = { cols: 2 }) => ({ commands, state }) => {
        const { cols } = attrs
        const schema = state.schema
        const columnType = schema.nodes.column
        const columnsType = schema.nodes.columns

        const columns = []
        for (let i = 0; i < cols; i++) {
          const column = columnType.createAndFill({ index: i })
          if (column) {
            columns.push(column)
          }
        }

        const columnsNode = columnsType.create({ cols }, columns)
        return commands.insertContent(columnsNode.toJSON())
      },

      addColBefore: () => ({ state, dispatch }) => {
        return this.addOrDeleteCol(dispatch, state, 'addBefore')
      },

      addColAfter: () => ({ state, dispatch }) => {
        return this.addOrDeleteCol(dispatch, state, 'addAfter')
      },

      deleteCol: () => ({ state, dispatch }) => {
        return this.addOrDeleteCol(dispatch, state, 'delete')
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-c': () => this.editor.commands.insertColumns({ cols: 2 }),
    }
  },

  addOrDeleteCol(dispatch: any, state: EditorState, type: 'addBefore' | 'addAfter' | 'delete') {
    const maybeColumns = findParentNode(node => node.type.name === 'columns')(state.selection)
    const maybeColumn = findParentNode(node => node.type.name === 'column')(state.selection)

    if (dispatch && maybeColumns && maybeColumn) {
      const cols = maybeColumns.node
      const colIndex = maybeColumn.node.attrs.index
      const colsJSON = cols.toJSON()

      let nextIndex = colIndex

      if (type === 'delete') {
        nextIndex = colIndex - 1
        colsJSON.content.splice(colIndex, 1)
      } else {
        nextIndex = type === 'addBefore' ? colIndex : colIndex + 1
        colsJSON.content.splice(nextIndex, 0, {
          type: 'column',
          attrs: { index: colIndex },
          content: [{ type: 'paragraph' }],
        })
      }

      colsJSON.attrs.cols = colsJSON.content.length

      colsJSON.content.forEach((colJSON: any, index: number) => {
        colJSON.attrs.index = index
      })

      const nextCols = PMNode.fromJSON(state.schema, colsJSON)

      let nextSelectPos = maybeColumns.pos
      nextCols.content.forEach((col, _pos, index) => {
        if (index < nextIndex) {
          nextSelectPos += col.nodeSize
        }
      })

      const tr = state.tr.setTime(Date.now())
      tr.replaceWith(
        maybeColumns.pos,
        maybeColumns.pos + maybeColumns.node.nodeSize,
        nextCols
      ).setSelection(TextSelection.near(tr.doc.resolve(nextSelectPos)))

      dispatch(tr)
    }
    return true
  },
})

export { Columns, Column }
export default Columns