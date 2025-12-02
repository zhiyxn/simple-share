<template>
  <NodeViewWrapper class="inline-block-box">
    <slot />
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { NodeViewWrapper } from '@tiptap/vue-3'
// 简单的内联块容器组件
</script>

<style scoped>
.inline-block-box {
  display: inline-flex;
  max-width: 100%;
  margin: 4px 6px;
  vertical-align: top;
  box-sizing: border-box;
}
</style>
