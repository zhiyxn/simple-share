<template>
  <div class="editor-container" :class="{ 'sidebar-visible': showSidebar }">
    <!-- 编辑区域 -->
    <div class="editor-main">
      <!-- 标题输入区域 -->
      <div class="title-section">
        <el-input
          :model-value="title"
          placeholder="请输入文章标题..."
          class="title-input"
          @input="handleTitleChange"
        />
        <!-- 标题下方分割线 -->
        <div class="title-divider"></div>
      </div>

      <!-- 内容编辑器 -->
      <div class="content-section">
        <RichTextEditorNew
          :model-value="content"
          :title="title"
          :show-sidebar="false"
          :upload-image="uploadImage"
          @update:model-value="handleContentChange"
          @update:title="handleTitleChange"
        />
      </div>
    </div>

    <!-- 右侧操作面板 -->
    <div class="right-panel" :class="{ 'panel-collapsed': !showSidebar }">
      <!-- 面板头部 -->
      <div class="panel-header" v-show="showSidebar">
        <div class="flex items-center justify-between">
          <h3 class="panel-title">文章操作</h3>
          <el-button
            @click="toggleSidebar"
            class="panel-toggle"
            size="small"
            :text="true"
            :circle="true"
          >
            <el-icon>
              <DArrowRight />
            </el-icon>
          </el-button>
        </div>
      </div>

      <!-- 面板内容 -->
      <div class="panel-content" v-show="showSidebar">
        <slot name="sidebar-content">
          <!-- 默认侧边栏内容 -->
          <section class="panel-card">
            <header class="panel-card__header">
              <h4>文章统计</h4>
            </header>
            <div class="panel-card__body">
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="stat-value">{{ wordCount }}</span>
                  <span class="stat-label">字数统计</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ characterCount }}</span>
                  <span class="stat-label">字符数量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ readingTime }}</span>
                  <span class="stat-label">预计阅读</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ headingCount }}</span>
                  <span class="stat-label">标题数量</span>
                </div>
              </div>
            </div>
          </section>

          <section class="panel-card" v-if="outlineHeadings.length > 0">
            <header class="panel-card__header">
              <h4>文章大纲</h4>
            </header>
            <div class="panel-card__body outline-list">
              <button
                v-for="(heading, index) in outlineHeadings"
                :key="index"
                type="button"
                class="outline-item"
                :style="{ paddingLeft: `${(heading.level - 1) * 12}px` }"
                @click="scrollToOutline(index)"
              >
                {{ heading.text }}
              </button>
            </div>
          </section>
        </slot>
      </div>
    </div>

    <div class="sidebar-toggle" v-if="!showSidebar">
      <el-button
        @click="toggleSidebar"
        class="toggle-btn"
        size="small"
        plain
        circle
      >
        <el-icon>
          <DArrowLeft />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import RichTextEditorNew from './RichTextEditorNew.vue'

// Props
interface Props {
  title?: string
  content?: string
  showSidebar?: boolean
  uploadImage?: (file: File) => Promise<{ url: string }>
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  content: '',
  showSidebar: false,
  uploadImage: undefined
})

// Emits
const emit = defineEmits<{
  'update:title': [value: string]
  'update:content': [value: string]
  'update:showSidebar': [value: boolean]
}>()

// 响应式数据
const showSidebar = ref(props.showSidebar)

// 计算属性
const wordCount = computed(() => {
  if (!props.content) return 0
  // 移除HTML标签后计算字数
  const text = props.content.replace(/<[^>]*>/g, '')
  return text.length
})

const characterCount = computed(() => {
  if (!props.content) return 0
  return props.content.length
})

const readingTime = computed(() => {
  const wordsPerMinute = 200
  const minutes = Math.ceil(wordCount.value / wordsPerMinute)
  return `${minutes}分钟`
})

const headingCount = computed(() => {
  if (!props.content) return 0
  const headingMatches = props.content.match(/<h[1-6][^>]*>/g)
  return headingMatches ? headingMatches.length : 0
})

const outlineHeadings = computed(() => {
  if (!props.content) return []
  
  const headingRegex = /<h([1-6])[^>]*>(.*?)<\/h[1-6]>/g
  const headings: Array<{ level: number; text: string }> = []
  let match
  
  while ((match = headingRegex.exec(props.content)) !== null) {
    headings.push({
      level: parseInt(match[1]),
      text: match[2].replace(/<[^>]*>/g, '').trim()
    })
  }
  
  return headings
})

// 方法
const handleTitleChange = (value: string) => {
  emit('update:title', value)
}

const handleContentChange = (value: string) => {
  emit('update:content', value)
}

const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
  emit('update:showSidebar', showSidebar.value)
}

const scrollToOutline = (index: number) => {
  // 滚动到对应标题的逻辑
  console.log('Scroll to outline:', index)
}

// 监听props变化
watch(() => props.showSidebar, (newValue) => {
  showSidebar.value = newValue
})
</script>

<style scoped>
.editor-container {
  position: relative;
  display: flex;
  flex: 1;
  min-height: 0;
  height: calc(100vh - 200px);
  padding: 0 32px;
  background: transparent;
  --sidebar-width: 320px;
  --sidebar-gap: 32px;
}

.editor-main {
  @apply flex flex-col;
  min-width: 0;
  width: 100%;
  margin-right: 0;
  transition: margin-right 0.3s ease;
}

.editor-container.sidebar-visible .editor-main {
  margin-right: calc(var(--sidebar-width) + var(--sidebar-gap));
}

.title-section {
  @apply border-b border-gray-100;
}

.title-input {
  @apply border-0 text-2xl font-bold;
}

.title-input :deep(.el-input__wrapper) {
  @apply shadow-none border-0 bg-transparent;
  padding: 24px 16px 16px;
}

.title-input :deep(.el-input__inner) {
  @apply text-2xl font-bold text-gray-900;
  line-height: 1.2;
}

.title-input :deep(.el-input__inner::placeholder) {
  @apply text-gray-400;
}

.title-divider {
  @apply h-px bg-gray-100 mx-4;
}

.content-section {
  @apply flex-1 overflow-hidden;
}


.right-panel {
  @apply border border-gray-200 bg-gray-50 flex flex-col overflow-hidden;
  position: absolute;
  top: 16px;
  right: 32px;
  bottom: 16px;
  width: var(--sidebar-width);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  transform: translateX(0);
  transition: transform 0.3s ease, opacity 0.3s ease;
  z-index: 20;
}

.right-panel.panel-collapsed {
  transform: translateX(calc(100% + var(--sidebar-gap)));
  opacity: 0;
  pointer-events: none;
}

.panel-header {
  @apply p-4 border-b border-gray-200 bg-white;
}

.panel-title {
  @apply text-lg font-semibold text-gray-900 m-0;
}

.panel-toggle {
  @apply text-gray-500 hover:text-gray-700;
}


.sidebar-toggle {
  position: absolute;
  top: 16px;
  right: 32px;
  z-index: 25;
}

.toggle-btn {
  @apply text-gray-500 hover:text-gray-700;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.15);
}

.panel-content {
  @apply flex-1 overflow-y-auto p-4 space-y-4;
}

.panel-card {
  @apply bg-white rounded-lg border border-gray-200 overflow-hidden;
}

.panel-card__header {
  @apply px-4 py-3 border-b border-gray-200 bg-gray-50;
}

.panel-card__header h4 {
  @apply text-sm font-semibold text-gray-900 m-0;
}

.panel-card__body {
  @apply p-4;
}

.stats-grid {
  @apply grid grid-cols-2 gap-4;
}

.stat-item {
  @apply text-center;
}

.stat-value {
  @apply block text-lg font-bold text-gray-900;
}

.stat-label {
  @apply block text-xs text-gray-500 mt-1;
}

.outline-list {
  @apply space-y-1;
}

.outline-item {
  @apply w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded transition-colors;
  border: none;
  background: none;
}

.outline-item:hover {
  @apply bg-gray-100;
}
</style>
