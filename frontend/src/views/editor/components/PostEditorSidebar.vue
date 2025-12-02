<template>
  <div class="right-panel" :class="{ 'panel-collapsed': !showSidebar }">
    <div class="panel-header" v-show="showSidebar">
      <div class="flex items-center justify-between">
        <h3 class="panel-title">文章操作</h3>
        <el-button
          class="panel-toggle"
          size="small"
          :text="true"
          :circle="true"
          @click="emit('toggle-sidebar')"
        >
          <el-icon>
            <DArrowRight />
          </el-icon>
        </el-button>
      </div>
    </div>

    <div class="collapsed-toggle" v-show="!showSidebar">
      <el-button
        class="expand-btn"
        size="small"
        :text="true"
        :circle="true"
        @click="emit('toggle-sidebar')"
      >
        <el-icon>
          <DArrowLeft />
        </el-icon>
      </el-button>
    </div>

    <div class="panel-content" v-show="showSidebar">
      <section class="panel-card">
        <header class="panel-card__header">
          <h4>写作信息</h4>
        </header>
        <div class="panel-card__body">
          <div class="panel-stats">
            <div class="panel-stats__item">
              <strong>{{ wordCount }}</strong>
              <span>正文字数</span>
            </div>
            <div class="panel-stats__item">
              <strong>{{ previewLength }}</strong>
              <span>预览字数</span>
            </div>
            <div class="panel-stats__item">
              <strong>{{ outlineHeadings.length }}</strong>
              <span>标题数量</span>
            </div>
          </div>
        </div>
      </section>

      <section v-if="outlineHeadings.length > 0" class="panel-card">
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
            @click="emit('scroll-to-outline', index)"
          >
            {{ heading.text }}
          </button>
        </div>
      </section>
      <ImagePreviewTool />
    </div>
  </div>
</template>

<script setup lang="ts">
import { DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import type { OutlineHeading } from '../types'
import ImagePreviewTool from './ImagePreviewTool.vue'

defineProps<{
  showSidebar: boolean
  wordCount: number
  previewLength: number
  outlineHeadings: OutlineHeading[]
}>()

const emit = defineEmits<{
  (e: 'toggle-sidebar'): void
  (e: 'scroll-to-outline', index: number): void
}>()
</script>

<style scoped>
.right-panel {
  @apply w-80 flex flex-col;
  flex-shrink: 0;
  background: transparent;
  transition: all 0.3s ease;
  border-left: 1px solid var(--el-border-color-light);
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.right-panel.panel-collapsed {
  @apply w-12 overflow-hidden;
}

.panel-header {
  @apply p-4;
  background: transparent;
}

.panel-title {
  @apply text-lg font-semibold m-0;
  color: var(--el-text-color-primary);
}

.panel-toggle {
  @apply text-gray-500 hover:text-gray-700;
}

.collapsed-toggle {
  @apply p-4 flex justify-center;
}

.expand-btn {
  @apply text-gray-500 hover:text-gray-700;
}

.panel-content {
  @apply flex-1 p-4 space-y-4;
}

.panel-card {
  @apply bg-white rounded-xl border mb-4 shadow-sm;
  border-color: var(--el-border-color-lighter);
  background: var(--el-bg-color);
  overflow: hidden;
}

.panel-card__header {
  @apply px-4 py-3 border-b;
  border-color: var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
}

.panel-card__header h4 {
  @apply text-sm font-semibold m-0;
  color: var(--el-text-color-primary);
}

.panel-card__body {
  @apply p-4 space-y-4;
}

.panel-stats {
  @apply grid gap-3;
  grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
}

.panel-stats__item {
  @apply flex flex-col items-center justify-center gap-1 rounded-xl px-3 py-4;
  background: var(--el-bg-color-page);
}

.panel-stats__item strong {
  @apply text-xl font-semibold;
  color: var(--el-color-primary);
}

.panel-stats__item span {
  @apply text-xs;
  color: var(--el-text-color-secondary);
}

.outline-list {
  @apply space-y-1 max-h-56 overflow-y-auto;
}

.outline-item {
  @apply w-full text-left text-sm px-3 py-2 rounded-md transition-colors;
  color: var(--el-text-color-primary);
  background: transparent;
}

.outline-item:hover {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

@media (max-width: 1024px) {
  .right-panel {
    @apply w-72;
  }
}

@media (max-width: 768px) {
  .right-panel {
    @apply w-full h-auto border-l-0 border-t;
    border-color: var(--el-border-color-light);
  }
}
</style>
