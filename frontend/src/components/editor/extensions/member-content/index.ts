import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import { StarFilled } from '@element-plus/icons-vue'
import MemberContentView from './MemberContentView.vue'

const DEFAULT_PLACEHOLDER = '这里是会员专属内容，请在此输入正文...'

const MemberContent = Node.create({
  name: 'memberContent',
  group: 'block',
  content: 'block+',
  isolating: true,
  defining: true,

  addOptions() {
    return {
      getCommandMenuItems: () => ({
        priority: 170,
        icon: markRaw(StarFilled),
        title: '会员内容',
        keywords: ['vip', 'member', 'huiyuan', '专属', '付费'],
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setMemberContent()
            .run()
        },
      }),
    }
  },

  addAttributes() {
    return {
      audience: {
        default: 'vip',
        parseHTML: (element) => element.getAttribute('data-audience') || 'vip',
        renderHTML: (attributes) => {
          if (!attributes.audience) {
            return {}
          }
          return {
            'data-audience': attributes.audience,
          }
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'section[data-type="member-content"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'section',
      { ...HTMLAttributes, 'data-type': 'member-content' },
      0,
    ]
  },

  addCommands() {
    return {
      setMemberContent:
        () =>
        ({ commands }) => {
          return commands.insertContent({
            type: this.name,
            attrs: {
              audience: 'vip',
            },
            content: [
              {
                type: 'paragraph',
                content: [
                  {
                    type: 'text',
                    text: DEFAULT_PLACEHOLDER,
                  },
                ],
              },
            ],
          })
        },
    }
  },

  addNodeView() {
    return VueNodeViewRenderer(MemberContentView)
  },
})

export default MemberContent
