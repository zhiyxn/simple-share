import TiptapLink from '@tiptap/extension-link'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import MdiLink from '~icons/mdi/link'
import MdiLinkOff from '~icons/mdi/link-off'

const Link = TiptapLink.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      openOnClick: false,
      HTMLAttributes: {
        class: 'editor-link',
      },
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 70,
            props: {
              isActive: () => editor.isActive('link'),
              icon: markRaw(MdiLink),
              title: editor.isActive('link') ? '取消链接' : '添加链接',
              action: () => {
                if (editor.isActive('link')) {
                  editor.chain().focus().unsetLink().run()
                } else {
                  const url = window.prompt('请输入链接地址:')
                  if (url) {
                    editor.chain().focus().setLink({ href: url }).run()
                  }
                }
              },
            },
          },
        ]
      },
    }
  },
})

export default Link