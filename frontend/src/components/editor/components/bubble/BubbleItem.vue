<template>
  <button
    v-if="visible({ editor })"
    :class="{ 'bg-gray-200 !text-black': isActive({ editor }) }"
    @click="handleClick"
    class="rounded-md p-2 text-lg text-gray-600 hover:bg-gray-100"
    :title="title"
  >
    <component :is="icon" class="h-5 w-5" />
  </button>
</template>

<script lang="ts" setup>
import type { Component } from 'vue'
import type { Editor } from '@tiptap/vue-3'

interface Props {
  editor: Editor
  isActive?: ({ editor }: { editor: Editor }) => boolean
  visible?: ({ editor }: { editor: Editor }) => boolean
  icon?: Component
  title?: string
  action?: ({ editor }: { editor: Editor }) => void
}

const props = withDefaults(defineProps<Props>(), {
  isActive: () => false,
  visible: () => true,
  title: '',
  action: undefined,
})

const handleClick = () => {
  if (props.action) {
    props.action({ editor: props.editor })
  }
}
</script>