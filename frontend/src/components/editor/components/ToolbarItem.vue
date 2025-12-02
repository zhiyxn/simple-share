<template>
  <el-tooltip :content="title" placement="bottom" :disabled="!title">
    <el-button
      :class="[
        { 'is-active': isActive },
        { 'is-disabled': disabled },
      ]"
      class="toolbar-item"
      :disabled="disabled"
      size="small"
      :type="isActive ? 'primary' : 'default'"
      @click="handleClick"
    >
      <el-icon v-if="icon">
        <component :is="icon" />
      </el-icon>
      <el-icon v-if="children?.length" class="dropdown-icon">
        <ArrowDown />
      </el-icon>
    </el-button>
  </el-tooltip>
</template>

<script setup lang="ts">
import type { ToolbarItemType } from '../types'
import type { Component } from 'vue'
import { ElButton, ElIcon, ElTooltip } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

interface Props {
  isActive?: boolean
  disabled?: boolean
  title?: string
  action?: () => void
  icon?: Component
  children?: ToolbarItemType[]
}

const props = withDefaults(defineProps<Props>(), {
  isActive: false,
  disabled: false,
  title: undefined,
  action: undefined,
  icon: undefined,
  children: undefined,
})

const handleClick = () => {
  if (!props.disabled && props.action) {
    props.action()
  }
}
</script>

<style scoped>
.toolbar-item {
  border-radius: 4px;
  transition: all 0.2s;
}

.toolbar-item.is-active {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
}

.toolbar-item.is-disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.toolbar-item:hover:not(.is-disabled) {
  background-color: var(--el-color-primary-light-9);
}

.dropdown-icon {
  margin-left: 4px;
  font-size: 12px;
}
</style>