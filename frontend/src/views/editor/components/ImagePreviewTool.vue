<template>
  <section class="image-preview-tool">
    <header class="image-preview-tool__header">
      <div class="image-preview-tool__title">
        <h4>图片展示</h4>
        <p>输入图片地址即可预览效果</p>
      </div>
      <el-tag size="small" type="info">实时</el-tag>
    </header>
    <div class="image-preview-tool__body">
      <el-input
        v-model="inputValue"
        placeholder="输入图片链接，例如 https://example.com/banner.png"
        clearable
        :disabled="pasting"
        @keyup.enter="applyImmediately"
        @change="applyImmediately"
      >
        <template #append>
          <el-button text type="primary" @click="applyImmediately">预览</el-button>
        </template>
      </el-input>
      <p class="image-preview-tool__helper">
        支持 http(s)、OSS、七牛、COS 等可直接访问的图片地址
      </p>
      <div
        class="image-preview-tool__preview"
        :class="{ 'image-preview-tool__preview--empty': !previewUrl }"
      >
        <template v-if="previewUrl">
          <img
            :key="renderKey"
            :src="previewUrl"
            alt="图片预览"
            draggable="false"
            @load="handleLoad"
            @error="handleError"
            :class="{
              'image-preview-tool__img--loading': loading,
              'image-preview-tool__img--hidden': !!error
            }"
          />
          <div
            v-if="loading"
            class="image-preview-tool__status image-preview-tool__status--loading"
          >
            <span class="dot dot--one"></span>
            <span class="dot dot--two"></span>
            <span class="dot dot--three"></span>
            <span>加载中...</span>
          </div>
          <div
            v-else-if="error"
            class="image-preview-tool__status image-preview-tool__status--error"
          >
            {{ error }}
          </div>
        </template>
        <div v-else class="image-preview-tool__placeholder">
          输入图片地址即可实时查看显示效果
        </div>
      </div>

      <div class="image-preview-tool__actions">
        <el-button text size="small" @click="clearInput" :disabled="!inputValue">
          清空
        </el-button>
        <el-button
          text
          size="small"
          @click="pasteFromClipboard"
          :loading="pasting"
          :disabled="!clipboardSupported"
        >
          粘贴链接
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const inputValue = ref('')
const previewUrl = ref('')
const loading = ref(false)
const error = ref('')
const pasting = ref(false)
const renderKey = ref(0)

const clipboardSupported =
  typeof navigator !== 'undefined' &&
  !!navigator.clipboard &&
  typeof navigator.clipboard.readText === 'function'

let previewTimer: ReturnType<typeof setTimeout> | undefined

const clearPreviewTimer = () => {
  if (previewTimer) {
    clearTimeout(previewTimer)
    previewTimer = undefined
  }
}

const triggerPreview = (raw: string, forceReload = false) => {
  const normalized = raw.trim()
  if (!normalized) {
    previewUrl.value = ''
    loading.value = false
    error.value = ''
    renderKey.value += 1
    return
  }
  const needsReload = forceReload || normalized !== previewUrl.value
  if (!needsReload) {
    return
  }
  previewUrl.value = normalized
  renderKey.value += 1
  loading.value = true
  error.value = ''
}

watch(inputValue, value => {
  clearPreviewTimer()
  if (!value || !value.trim()) {
    triggerPreview('')
    return
  }
  previewTimer = setTimeout(() => {
    triggerPreview(value)
  }, 450)
})

const applyImmediately = () => {
  clearPreviewTimer()
  triggerPreview(inputValue.value, true)
}

const handleLoad = () => {
  loading.value = false
  error.value = ''
}

const handleError = () => {
  loading.value = false
  error.value = '图片加载失败，请检查链接是否正确、支持跨域访问'
}

const clearInput = () => {
  inputValue.value = ''
  triggerPreview('')
}

const pasteFromClipboard = async () => {
  if (!clipboardSupported) {
    ElMessage.warning('当前浏览器不支持快捷粘贴')
    return
  }
  try {
    pasting.value = true
    const text = await navigator.clipboard.readText()
    if (!text.trim()) {
      ElMessage.info('剪贴板为空')
      return
    }
    inputValue.value = text.trim()
    applyImmediately()
  } catch (err) {
    console.warn('read clipboard failed', err)
    ElMessage.error('无法读取剪贴板，请手动粘贴')
  } finally {
    pasting.value = false
  }
}

onBeforeUnmount(() => {
  clearPreviewTimer()
})
</script>

<style scoped>
.image-preview-tool {
  @apply rounded-2xl border border-gray-200 bg-white shadow-sm;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.image-preview-tool__header {
  @apply flex items-center justify-between;
}

.image-preview-tool__title h4 {
  @apply text-base font-semibold text-gray-900 mb-1;
}

.image-preview-tool__title p {
  @apply text-xs text-gray-500 m-0;
}

.image-preview-tool__body {
  @apply space-y-4;
}

.image-preview-tool__helper {
  @apply text-xs text-gray-500 m-0;
}

.image-preview-tool__preview {
  @apply relative rounded-xl border border-dashed border-gray-200 bg-gray-50 overflow-hidden;
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.image-preview-tool__preview img {
  max-height: 320px;
  width: 100%;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.15);
}

.image-preview-tool__preview--empty {
  border-style: solid;
  border-color: #e2e8f0;
}

.image-preview-tool__placeholder {
  @apply text-sm text-gray-500 text-center px-4;
}

.image-preview-tool__status {
  @apply absolute inset-0 flex flex-col items-center justify-center gap-2 text-sm;
  background: rgba(248, 250, 252, 0.95);
  font-weight: 500;
}

.image-preview-tool__status--loading {
  color: #2563eb;
}

.image-preview-tool__status--error {
  color: #dc2626;
  background: rgba(254, 226, 226, 0.95);
}

.image-preview-tool__img--loading {
  filter: blur(1px);
  opacity: 0.7;
}

.image-preview-tool__img--hidden {
  opacity: 0;
  visibility: hidden;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 1s infinite ease-in-out;
}

.dot--two {
  animation-delay: 0.15s;
}

.dot--three {
  animation-delay: 0.3s;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.2;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

.image-preview-tool__actions {
  @apply flex justify-end gap-2;
}

@media (max-width: 768px) {
  .image-preview-tool {
    @apply rounded-xl;
  }
}
</style>
