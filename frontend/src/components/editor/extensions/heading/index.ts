import { Heading as TiptapHeading } from '@tiptap/extension-heading'
import { markRaw } from 'vue'
import { mergeAttributes } from '@tiptap/core'
import type { ExtensionOptions, BubbleMenuItemType } from '../../types'
import type { Editor } from '@tiptap/vue-3'
import HeadingSelector from '../../components/bubble/HeadingSelector.vue'

// 图标组件 - 使用不同的图标来区分标题级别
import { Document, Grid, List, More, Menu, Operation, Setting } from '@element-plus/icons-vue'

export default TiptapHeading.extend<ExtensionOptions>({
  renderHTML({ node, HTMLAttributes }) {
    const hasLevel = this.options.levels.includes(node.attrs.level)
    const level = hasLevel ? node.attrs.level : this.options.levels[0]
    return [
      `h${level}`,
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      levels: [1, 2, 3, 4, 5, 6],
      getCommandMenuItems() {
        return [
          {
            priority: 10,
            icon: markRaw(Document),
            title: '段落',
            keywords: ['paragraph', 'text', 'putongwenben'],
            command: ({ editor, range }) => {
              editor.chain().focus().deleteRange(range).setParagraph().run()
            },
          },
          {
            priority: 20,
            icon: markRaw(Grid),
            title: '一级标题',
            keywords: ['h1', 'header1', '1', 'yijibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 1 })
                .run()
            },
          },
          {
            priority: 30,
            icon: markRaw(List),
            title: '二级标题',
            keywords: ['h2', 'header2', '2', 'erjibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 2 })
                .run()
            },
          },
          {
            priority: 40,
            icon: markRaw(Menu),
            title: '三级标题',
            keywords: ['h3', 'header3', '3', 'sanjibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 3 })
                .run()
            },
          },
          {
            priority: 50,
            icon: markRaw(Operation),
            title: '四级标题',
            keywords: ['h4', 'header4', '4', 'sijibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 4 })
                .run()
            },
          },
          {
            priority: 60,
            icon: markRaw(Setting),
            title: '五级标题',
            keywords: ['h5', 'header5', '5', 'wujibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 5 })
                .run()
            },
          },
          {
            priority: 70,
            icon: markRaw(More),
            title: '六级标题',
            keywords: ['h6', 'header6', '6', 'liujibiaoti'],
            command: ({ editor, range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 6 })
                .run()
            },
          },
        ]
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 110,
            component: markRaw(HeadingSelector),
            componentProps: {
              editor,
            },
          },
        ]
      },
    }
  },
})
