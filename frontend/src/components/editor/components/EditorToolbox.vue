<template>
  <div
    v-if="visible"
    ref="toolboxRef"
    class="editor-toolbox"
    :style="toolboxStyle"
  >
    <div class="toolbox-content">
      <div class="toolbox-search">
        <el-input
          v-model="searchText"
          placeholder="搜索工具..."
          size="small"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <div class="toolbox-items">
        <div
          v-for="(item, index) in filteredItems"
          :key="item.name"
          class="toolbox-item"
          :class="{ active: selectedIndex === index }"
          @click="handleItemClick(item)"
          @mouseenter="selectedIndex = index"
        >
          <div class="item-icon">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-description">{{ item.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElInput, ElIcon } from 'element-plus'
import { 
  Search, 
  Picture, 
  VideoCamera, 
  Headphone,
  Document,
  Link,
  List,
  Grid,
  Promotion,
  ChatLineRound
} from '@element-plus/icons-vue'
import type { ToolboxItem } from '../types'

interface Props {
  visible: boolean
  position: { x: number; y: number }
  items: ToolboxItem[]
}

interface Emits {
  (e: 'item-select', item: ToolboxItem): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  position: () => ({ x: 0, y: 0 }),
  items: () => [],
})

const emit = defineEmits<Emits>()

const toolboxRef = ref<HTMLElement>()
const searchText = ref('')
const selectedIndex = ref(0)

// 默认工具箱项目
const defaultItems: ToolboxItem[] = [
  {
    name: 'heading1',
    title: '标题 1',
    description: '大标题',
    icon: 'H1',
    action: 'heading',
    attrs: { level: 1 },
  },
  {
    name: 'heading2',
    title: '标题 2',
    description: '中标题',
    icon: 'H2',
    action: 'heading',
    attrs: { level: 2 },
  },
  {
    name: 'heading3',
    title: '标题 3',
    description: '小标题',
    icon: 'H3',
    action: 'heading',
    attrs: { level: 3 },
  },
  {
    name: 'paragraph',
    title: '正文',
    description: '普通文本段落',
    icon: Document,
    action: 'paragraph',
  },
  {
    name: 'bulletList',
    title: '无序列表',
    description: '创建无序列表',
    icon: List,
    action: 'bulletList',
  },
  {
    name: 'orderedList',
    title: '有序列表',
    description: '创建有序列表',
    icon: Grid,
    action: 'orderedList',
  },
  {
    name: 'blockquote',
    title: '引用',
    description: '创建引用块',
    icon: ChatLineRound,
    action: 'blockquote',
  },
  {
    name: 'image',
    title: '图片',
    description: '插入图片',
    icon: Picture,
    action: 'image',
  },
  {
    name: 'video',
    title: '视频',
    description: '插入视频',
    icon: VideoCamera,
    action: 'video',
  },
  {
    name: 'audio',
    title: '音频',
    description: '插入音频',
    icon: Headphone,
    action: 'audio',
  },
  {
    name: 'link',
    title: '链接',
    description: '插入链接',
    icon: Link,
    action: 'link',
  },
  {
    name: 'divider',
    title: '分割线',
    description: '插入分割线',
    icon: Promotion,
    action: 'horizontalRule',
  },
]

// 合并默认项目和传入的项目
const allItems = computed(() => {
  return [...defaultItems, ...props.items]
})

// 过滤后的项目
const filteredItems = computed(() => {
  if (!searchText.value) {
    return allItems.value
  }
  return allItems.value.filter(item =>
    item.title.toLowerCase().includes(searchText.value.toLowerCase()) ||
    item.description.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 工具箱样式
const toolboxStyle = computed(() => {
  return {
    position: 'fixed',
    left: `${props.position.x}px`,
    top: `${props.position.y}px`,
    zIndex: 1000,
  }
})

// 搜索处理
const handleSearch = () => {
  selectedIndex.value = 0
}

// 项目点击处理
const handleItemClick = (item: ToolboxItem) => {
  emit('item-select', item)
}

// 键盘事件处理
const handleKeydown = (event: KeyboardEvent) => {
  if (!props.visible) return

  switch (event.key) {
    case 'ArrowUp':
      event.preventDefault()
      selectedIndex.value = Math.max(0, selectedIndex.value - 1)
      break
    case 'ArrowDown':
      event.preventDefault()
      selectedIndex.value = Math.min(filteredItems.value.length - 1, selectedIndex.value + 1)
      break
    case 'Enter':
      event.preventDefault()
      if (filteredItems.value[selectedIndex.value]) {
        handleItemClick(filteredItems.value[selectedIndex.value])
      }
      break
    case 'Escape':
      event.preventDefault()
      emit('close')
      break
  }
}

// 点击外部关闭
const handleClickOutside = (event: MouseEvent) => {
  if (props.visible && toolboxRef.value && !toolboxRef.value.contains(event.target as Node)) {
    emit('close')
  }
}

// 监听可见性变化
watch(() => props.visible, (visible) => {
  if (visible) {
    selectedIndex.value = 0
    searchText.value = ''
    nextTick(() => {
      // 聚焦搜索框
      const searchInput = toolboxRef.value?.querySelector('input')
      searchInput?.focus()
    })
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.editor-toolbox {
  @apply bg-white border border-gray-200 rounded-lg shadow-lg;
  min-width: 280px;
  max-width: 320px;
  max-height: 400px;
  overflow: hidden;
}

.toolbox-content {
  @apply p-2;
}

.toolbox-search {
  @apply mb-2;
}

.toolbox-items {
  @apply max-h-80 overflow-y-auto;
}

.toolbox-item {
  @apply flex items-center p-2 rounded cursor-pointer transition-colors;
}

.toolbox-item:hover,
.toolbox-item.active {
  @apply bg-blue-50;
}

.item-icon {
  @apply flex-shrink-0 w-8 h-8 flex items-center justify-center mr-3 text-gray-600;
}

.item-content {
  @apply flex-1 min-w-0;
}

.item-title {
  @apply font-medium text-gray-900 text-sm;
}

.item-description {
  @apply text-xs text-gray-500 truncate;
}
</style>