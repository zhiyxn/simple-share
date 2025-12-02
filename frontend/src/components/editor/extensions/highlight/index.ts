import TiptapHighlight from '@tiptap/extension-highlight'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import type { BubbleMenuItemType } from '../../types'
import TextColorPicker from '../../components/bubble/TextColorPicker.vue'

const Highlight = TiptapHighlight.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      multicolor: true,
      getBubbleMenu({ editor }: { editor: Editor }): BubbleMenuItemType[] {
        return [
          {
            priority: 54,
            component: markRaw(TextColorPicker),
            componentProps: {
              mode: 'highlight',
            },
          },
        ]
      },
    }
  },
})

export default Highlight
