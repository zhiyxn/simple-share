<template>
  <NodeViewWrapper class="details-wrapper" :class="{ 'is-open': isOpen }">
    <div class="details-header" @click="toggleOpen">
      <el-button text size="small" class="toggle-btn">
        <el-icon>
          <ArrowRight v-if="!isOpen" />
          <ArrowDown v-else />
        </el-icon>
      </el-button>
      <div class="summary-content">
        <NodeViewContent as="div" class="summary-text" />
      </div>
    </div>
    
    <div v-show="isOpen" class="details-content">
      <NodeViewContent as="div" class="content-area" />
    </div>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElButton, ElIcon } from 'element-plus'
import { ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { NodeViewContent, NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'

const props = defineProps<NodeViewProps>()

const isOpen = computed({
  get: () => props.node?.attrs.open || false,
  set: (open: boolean) => {
    props.updateAttributes({ open })
  },
})

const toggleOpen = () => {
  isOpen.value = !isOpen.value
}
</script>

<style scoped>
.details-wrapper {
  @apply border border-gray-200 rounded-lg my-4 overflow-hidden;
}

.details-header {
  @apply flex items-center p-3 bg-gray-50 cursor-pointer hover:bg-gray-100 transition-colors;
}

.toggle-btn {
  @apply !p-1 mr-2;
}

.summary-content {
  @apply flex-1;
}

.summary-text {
  @apply font-medium text-gray-900;
}

.details-content {
  @apply p-4 border-t border-gray-200;
}

.content-area {
  @apply space-y-2;
}

.is-open .details-header {
  @apply border-b border-gray-200;
}
</style>