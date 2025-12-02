<template>
  <div
    class="toolbox-item-new"
    @click="handleClick"
  >
    <div class="item-icon">
      <component
        v-if="icon"
        :is="icon"
        class="icon"
      />
    </div>
    <div class="item-content">
      <div class="item-title">{{ title }}</div>
      <div v-if="description" class="item-description">{{ description }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Editor } from '@tiptap/vue-3'
import type { Component } from 'vue'

interface Props {
  editor?: Editor
  title?: string
  description?: string
  action?: () => void
  icon?: Component
}

const props = withDefaults(defineProps<Props>(), {
  editor: undefined,
  title: undefined,
  description: undefined,
  action: undefined,
  icon: undefined,
})

const handleClick = () => {
  if (props.action) {
    props.action()
  }
}
</script>

<style scoped>
.toolbox-item-new {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 12px;
}

.toolbox-item-new:hover {
  background-color: var(--el-color-primary-light-9);
}

.item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  transition: all 0.2s;
}

.toolbox-item-new:hover .item-icon {
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.icon {
  font-size: 16px;
  color: var(--el-text-color-regular);
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  margin-bottom: 2px;
}

.item-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbox-item-new:hover .item-title {
  color: var(--el-color-primary);
  font-weight: 600;
}
</style>