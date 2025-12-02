<script lang="ts" setup>
import type { Editor } from '@tiptap/vue-3'
import type { PropType } from 'vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { BubbleMenuPlugin } from './BubbleMenuPlugin'

const props = defineProps({
  pluginKey: {
    type: [String, Object] as PropType<string | any>,
    default: 'bubbleMenu',
  },
  editor: {
    type: Object as PropType<Editor>,
    required: true,
  },
  shouldShow: {
    type: Function as PropType<(props: any) => boolean>,
    default: null,
  },
  tippyOptions: {
    type: Object as PropType<Record<string, any>>,
    default: () => ({}),
  },
  getRenderContainer: {
    type: Function as PropType<(node: HTMLElement) => HTMLElement>,
    default: undefined,
  },
  defaultAnimation: {
    type: Boolean,
    default: true,
  },
})

const root = ref<HTMLElement>()

onMounted(() => {
  const { pluginKey, editor, shouldShow, tippyOptions, getRenderContainer, defaultAnimation } = props

  editor.registerPlugin(
    BubbleMenuPlugin({
      pluginKey,
      editor,
      element: root.value!,
      shouldShow,
      tippyOptions,
      getRenderContainer,
      defaultAnimation,
    })
  )
})

onBeforeUnmount(() => {
  const { pluginKey, editor } = props
  editor.unregisterPlugin(pluginKey)
})
</script>

<template>
  <div ref="root" style="visibility: hidden">
    <slot />
  </div>
</template>