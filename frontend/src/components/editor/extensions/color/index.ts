import TiptapColor from '@tiptap/extension-color'
import { TextStyle as TiptapTextStyle } from '@tiptap/extension-text-style'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import TextColorPicker from '../../components/bubble/TextColorPicker.vue'

const Color = TiptapColor.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      types: ['textStyle'],
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 55,
            component: markRaw(TextColorPicker),
            componentProps: {
              mode: 'color',
            },
          },
        ]
      },
    }
  },
})

export { TiptapTextStyle as TextStyle }
export default Color
