<template>
  <div class="heading-selector">
    <el-dropdown 
      trigger="click" 
      placement="bottom-start"
      @command="handleCommand"
    >
      <el-button 
        size="small" 
        :class="{ 'is-active': isHeadingActive }"
        class="heading-button"
      >
        <el-icon><component :is="currentHeadingIcon" /></el-icon>
        <span class="heading-text">{{ currentHeadingText }}</span>
        <el-icon class="arrow-icon"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item 
            command="paragraph"
            :class="{ 'is-active': !isHeadingActive }"
          >
            <el-icon><Document /></el-icon>
            <span>正文</span>
          </el-dropdown-item>
          <el-dropdown-item 
            v-for="level in 6" 
            :key="level"
            :command="`heading-${level}`"
            :class="{ 'is-active': editor.isActive('heading', { level }) }"
          >
            <el-icon><component :is="getHeadingIcon(level)" /></el-icon>
            <span>标题 {{ level }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElButton, ElIcon } from 'element-plus'
import { 
  Document, 
  ArrowDown,
  Rank,
  Grid,
  List,
  More,
  Menu,
  Operation
} from '@element-plus/icons-vue'

interface Props {
  editor: Editor
}

const props = defineProps<Props>()

// 计算当前标题状态
const isHeadingActive = computed(() => {
  return props.editor.isActive('heading')
})

const currentHeadingLevel = computed(() => {
  if (!isHeadingActive.value) return 0
  for (let level = 1; level <= 6; level++) {
    if (props.editor.isActive('heading', { level })) {
      return level
    }
  }
  return 0
})

const currentHeadingText = computed(() => {
  if (!isHeadingActive.value) return '正文'
  return `H${currentHeadingLevel.value}`
})

const currentHeadingIcon = computed(() => {
  if (!isHeadingActive.value) return Document
  return getHeadingIcon(currentHeadingLevel.value)
})

// 获取标题图标
const getHeadingIcon = (level: number) => {
  switch (level) {
    case 1:
      return Rank
    case 2:
      return Grid
    case 3:
      return List
    case 4:
      return Menu
    case 5:
      return Operation
    case 6:
      return More
    default:
      return Document
  }
}

// 处理命令
const handleCommand = (command: string) => {
  if (command === 'paragraph') {
    props.editor.chain().focus().setParagraph().run()
  } else if (command.startsWith('heading-')) {
    const level = parseInt(command.split('-')[1])
    props.editor.chain().focus().toggleHeading({ level }).run()
  }
}
</script>

<style scoped>
.heading-selector {
  display: inline-block;
}

.heading-button {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 80px;
  justify-content: space-between;
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  transition: all 0.2s;
}

.heading-button:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.heading-button.is-active {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
}

.heading-text {
  font-size: 12px;
  font-weight: 500;
}

.arrow-icon {
  font-size: 12px;
  opacity: 0.7;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

:deep(.el-dropdown-menu__item.is-active) {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
</style>