<template>
  <div class="editor-main" :class="{ 'sidebar-hidden': !showSidebar }">
    <div class="title-section">
      <el-input
        v-model="titleValue"
        :placeholder="titlePlaceholder"
        class="title-input"
        @input="handleTitleInput"
      />
      <div class="title-divider"></div>
    </div>

    <div class="content-section">
      <RichTextEditorNew
        v-model="contentValue"
        :title="titleValue"
        :show-sidebar="false"
        :upload-image="uploadImage"
        @update:title="handleTitleUpdate"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
import RichTextEditorNew from '@/components/editor/RichTextEditorNew.vue'

const props = withDefaults(
  defineProps<{
    title: string
    content: string
    showSidebar: boolean
    uploadImage: (file: File) => Promise<{ url: string }>
    titlePlaceholder?: string
  }>(),
  {
    titlePlaceholder: '请输入文章标题...',
  }
)

const emit = defineEmits<{
  (e: 'update:title', value: string): void
  (e: 'update:content', value: string): void
  (e: 'title-change', value: string): void
}>()

const titleValue = computed({
  get: () => props.title,
  set: (value: string) => {
    emit('update:title', value)
  }
})

const contentValue = computed({
  get: () => props.content,
  set: (value: string) => {
    emit('update:content', value)
  }
})

const { showSidebar, uploadImage, titlePlaceholder } = toRefs(props)

const handleTitleInput = (value: string) => {
  emit('title-change', value)
}

const handleTitleUpdate = (value: string) => {
  emit('update:title', value)
  emit('title-change', value)
}
</script>

<style scoped>
.editor-main {
  @apply flex-1 flex flex-col;
  transition: all 0.3s ease;
  max-width: calc(100% - 320px);
  overflow: visible;
  min-height: 0;
  margin: 0;
  padding: 0;
}

.editor-main.sidebar-hidden {
  max-width: 100%;
}

.title-divider {
  @apply w-full h-px bg-gray-200 mt-4 mb-2;
  border-color: var(--el-border-color-lighter);
}

.title-section {
  @apply w-full pt-4 pb-0;
  text-align: left;
  margin: 0;
  padding-left: 0;
  padding-right: 0;
}

.title-input {
  @apply w-full;
  text-align: left;
}

:deep(.title-input .el-input__wrapper) {
  @apply border-none shadow-none bg-transparent px-0;
  min-height: 48px;
  text-align: left;
}

:deep(.title-input .el-input__inner) {
  @apply text-3xl font-bold placeholder-gray-400;
  line-height: 1.2;
  color: var(--el-text-color-primary);
  min-height: 48px;
  padding: 6px 0;
  text-align: left;
}

.content-section {
  @apply flex-1 w-full pb-8;
  text-align: left;
  overflow: visible;
  min-height: 0;
  margin: 0;
  padding-left: 0;
  padding-right: 0;
}

:deep(.content-section .rich-text-editor) {
  @apply border-none shadow-none bg-transparent;
  text-align: left;
}

:deep(.content-section .ProseMirror) {
  @apply border-none outline-none bg-transparent;
  font-size: 16px;
  line-height: 1.7;
  color: var(--el-text-color-primary);
  min-height: 400px;
  border-bottom: none !important;
  text-align: left;
  padding-top: 0.25rem;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
  max-width: 900px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.content-section .ProseMirror > *) {
  width: 100%;
}

@media (max-width: 1024px) {
  .editor-main {
    max-width: calc(100% - 288px);
  }
}

@media (max-width: 768px) {
  .editor-main {
    @apply max-w-none;
  }

  .title-section,
  .content-section {
    @apply px-4;
  }
}

:deep(.ProseMirror) {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

:deep(.ProseMirror h1) {
  @apply text-2xl font-bold mt-6 mb-4 text-gray-900;
}

:deep(.ProseMirror h2) {
  @apply text-xl font-bold mt-5 mb-3 text-gray-800;
}

:deep(.ProseMirror h3) {
  @apply text-lg font-bold mt-4 mb-2 text-gray-700;
}

:deep(.ProseMirror p) {
  @apply mb-4 leading-relaxed text-gray-700;
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  @apply mb-4 pl-6;
}

:deep(.ProseMirror li) {
  @apply mb-1 text-gray-700;
}

:deep(.ProseMirror blockquote) {
  @apply border-l-4 border-blue-200 pl-4 italic my-4 bg-blue-50 py-2 text-gray-600;
}

:deep(.ProseMirror pre:not(.code-pre)) {
  @apply rounded-lg p-4 my-4 overflow-x-auto bg-gray-100 border border-gray-200;
}

:deep(.ProseMirror code) {
  @apply px-1 py-0.5 rounded text-sm font-mono bg-gray-100 text-gray-800;
}

:deep(.ProseMirror table) {
  @apply border-collapse my-4 w-full border border-gray-200;
}

:deep(.ProseMirror th),
:deep(.ProseMirror td) {
  @apply px-3 py-2 border border-gray-200;
}

:deep(.ProseMirror th) {
  @apply bg-gray-100 font-semibold text-gray-800;
}

:deep(.ProseMirror a) {
  @apply text-blue-600 hover:text-blue-800 underline;
}

:deep(.ProseMirror img) {
  @apply rounded-lg max-w-full h-auto my-4;
}

:deep(.ProseMirror .tableWrapper) {
  @apply overflow-x-auto my-4;
}

:deep(.ProseMirror .selectedCell:after) {
  background: rgba(200, 200, 255, 0.4);
}

:deep(.ProseMirror) {
  border-bottom: none !important;
}

:deep(.ProseMirror-focused) {
  outline: none !important;
  border-bottom: none !important;
}
</style>
