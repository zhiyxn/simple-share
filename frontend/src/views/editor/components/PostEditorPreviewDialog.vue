<template>
  <el-dialog
    :model-value="modelValue"
    title="文章预览"
    width="80%"
    :fullscreen="fullscreen"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template #header="{ titleId, titleClass }">
      <div class="preview-header">
        <h4 :id="titleId" :class="titleClass">文章预览</h4>
        <div class="preview-actions">
          <el-button size="small" @click="emit('update:fullscreen', !fullscreen)">
            <el-icon>
              <FullScreen />
            </el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <div class="preview-content">
      <h1 class="preview-title">{{ post.title || '无标题' }}</h1>
      <p v-if="post.summary" class="preview-summary">{{ post.summary }}</p>
      <div class="preview-meta">
        <span>作者：{{ previewAuthor }}</span>
        <span>创建时间：{{ post.createdAt ? formatDate(post.createdAt) : '暂未保存' }}</span>
        <span v-if="post.updatedAt">更新时间：{{ formatDate(post.updatedAt) }}</span>
      </div>
      <div class="preview-body">
        <article
          v-if="renderedHtml"
          ref="previewContainer"
          class="markdown-surface"
          v-html="renderedHtml"
        ></article>
        <div v-else class="preview-empty">暂无可预览的内容</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, toRefs, watch } from 'vue'
import { FullScreen } from '@element-plus/icons-vue'
import { useArticlePreviewRenderer } from '../composables/useArticlePreviewRenderer'
import type { EditorPost, EditorUser } from '../types'
import '@/styles/markdown.css'

const props = defineProps<{
  modelValue: boolean
  fullscreen: boolean
  post: EditorPost
  currentUser?: EditorUser | null
  formatDate: (date?: Date) => string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'update:fullscreen', value: boolean): void
}>()

const { modelValue, fullscreen, post, currentUser, formatDate } = toRefs(props)

const previewAuthor = computed(() => currentUser.value?.name || '匿名')

const {
  renderedHtml,
  containerRef: previewContainer,
  enhance,
  refresh,
  cleanupSecureResource,
  protectCopyComputed,
} = useArticlePreviewRenderer({
  source: () => post.value.content ?? '',
  protectCopy: () => post.value.allowCopy === 0,
  autoEnhance: false,
})

watch(renderedHtml, async (value) => {
  if (!modelValue.value || !value) return
  await nextTick()
  await enhance()
})

watch(modelValue, async (visible) => {
  if (visible) {
    await refresh()
  } else {
    cleanupSecureResource()
  }
})

watch(protectCopyComputed, async () => {
  if (!modelValue.value || !renderedHtml.value) return
  await nextTick()
  await enhance()
})
</script>

<style scoped>
.preview-header {
  @apply flex items-center justify-between;
}

.preview-content {
  @apply max-w-4xl mx-auto flex flex-col gap-6;
  padding: 8px 0 32px;
}

.preview-title {
  @apply text-3xl font-bold;
  color: var(--el-text-color-primary);
  margin: 0;
}

.preview-summary {
  @apply text-base leading-relaxed;
  margin: 0;
  color: var(--el-text-color-secondary);
}

.preview-meta {
  @apply flex flex-wrap items-center gap-4 text-sm;
  color: var(--el-text-color-secondary);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 16px;
}

.preview-body {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.08);
  display: flex;
  justify-content: center;
}

.preview-body .markdown-surface {
  width: 100%;
  max-width: 900px;
  display: block;
}

.preview-empty {
  @apply text-center text-gray-500;
  padding: 48px 0;
}

@media (max-width: 768px) {
  .preview-content {
    padding: 0;
  }

  .preview-body {
    padding: 18px;
    border-radius: 14px;
  }
}
</style>
