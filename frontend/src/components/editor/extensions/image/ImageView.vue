<template>
  <InlineBlockBox>
    <div
      class="relative block h-full max-w-full overflow-visible rounded-md transition-all"
      :class="{
        'rounded ring-2 ring-blue-500 image-selected': selected,
      }"
      :style="containerStyle"
    >
      <!-- 初始化状态 -->
      <div v-if="initialization" class="image-placeholder">
        <div class="placeholder-content">
          <el-icon class="placeholder-icon"><Picture /></el-icon>
          <p>点击添加图片</p>
          <el-button type="primary" @click="handleAddImage">
            添加图片
          </el-button>
        </div>
      </div>

      <!-- 图片显示 -->
      <div v-else-if="src && !uploading" class="group relative">
        <div
          v-if="selected"
          class="resize-outline"
        />
        <img
          ref="resizeRef"
          :src="src"
          :alt="alt"
          class="m-0 max-w-full h-auto"
          :style="imageInlineStyle"
          @load="onImageLoaded"
        />

        <transition name="image-toolbar-fade">
          <div
            v-if="showSettingsBubble"
            class="image-floating-toolbar"
          >
            <div class="toolbar-section slider-section">
              <span class="toolbar-label">大小</span>
              <div class="slider-controls">
                <el-slider
                  v-model="sizePercent"
                  :min="SIZE_MIN"
                  :max="SIZE_MAX"
                  :step="1"
                  :show-tooltip="false"
                  size="small"
                  class="image-size-slider"
                  @input="handleSliderInput"
                />
                <el-input-number
                  v-model="sizePercent"
                  :min="SIZE_MIN"
                  :max="SIZE_MAX"
                  :step="1"
                  size="small"
                  controls-position="right"
                  class="size-input"
                  @change="handleInputNumberChange"
                />
              </div>
              <div class="preset-buttons">
                <span class="toolbar-label toolbar-label--secondary">快速值</span>
                <div class="preset-list">
                  <el-button
                    v-for="preset in sizePresets"
                    :key="preset"
                    size="small"
                    :type="preset === sizePercent ? 'primary' : 'default'"
                    @click="applySizePreset(preset)"
                  >
                    {{ preset }}%
                  </el-button>
                </div>
              </div>
            </div>
            <div class="toolbar-section">
              <span class="toolbar-label">对齐</span>
              <div class="alignment-buttons">
                <el-button
                  size="small"
                  :type="alignmentValue === 'left' ? 'primary' : 'default'"
                  @click="setAlignment('left')"
                >
                  左
                </el-button>
                <el-button
                  size="small"
                  :type="alignmentValue === 'center' ? 'primary' : 'default'"
                  @click="setAlignment('center')"
                >
                  中
                </el-button>
                <el-button
                  size="small"
                  :type="alignmentValue === 'right' ? 'primary' : 'default'"
                  @click="setAlignment('right')"
                >
                  右
                </el-button>
              </div>
            </div>
            <div class="toolbar-section">
              <span class="toolbar-label">边框</span>
              <el-select
                v-model="borderStyleValue"
                size="small"
                class="border-style-select"
                popper-class="image-border-select"
              >
                <el-option
                  v-for="option in borderStyleOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </div>
          </div>
        </transition>
        
        <!-- 悬停工具栏 -->
        <div
          v-if="src"
          class="absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <div class="flex space-x-2">
            <el-button size="small" @click="handleResetInit">
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button size="small" @click="deleteNode">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 调整大小手柄 -->
        <div
          class="resize-handle resize-handle--left"
          @mousedown="startResize($event, 'left')"
        />
        <div
          class="resize-handle resize-handle--right"
          @mousedown="startResize($event, 'right')"
        />
        <div
          class="resize-handle resize-handle--corner"
          @mousedown="startResize($event, 'right')"
        />
      </div>

      <!-- 上传状态 -->
      <div v-else-if="uploading || fileBase64" class="upload-status">
        <div v-if="fileBase64" class="preview-image">
          <img :src="fileBase64" alt="预览" class="max-w-full h-auto rounded-md" />
        </div>
        
        <div class="upload-progress">
          <el-progress :percentage="uploadProgress" />
          <div class="progress-actions">
            <el-button size="small" @click="handleUploadAbort">取消</el-button>
            <el-button v-if="retryFlag" size="small" type="primary" @click="handleUploadRetry">
              重试
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 链接获取组件 -->
    <EditorLinkObtain
      ref="editorLinkObtain"
      v-model:visible="showLinkDialog"
      show-upload
      @link-submit="handleSetExternalLink"
      @file-upload="handleFileUpload"
      @upload-success="handleUploadSuccess"
      @upload-error="handleUploadError"
      @upload-progress="handleUploadProgress"
    />
  </InlineBlockBox>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElIcon, ElButton, ElProgress, ElSelect, ElOption, ElSlider, ElInputNumber } from 'element-plus'
import { Picture, Refresh, Delete } from '@element-plus/icons-vue'
import type { NodeViewProps } from '@tiptap/vue-3'
import EditorLinkObtain from '../../components/EditorLinkObtain.vue'
import InlineBlockBox from '../../components/InlineBlockBox.vue'
import type { AttachmentAttr } from '../../types'
import { fileToBase64 } from '../../utils/upload'
import Image from './index'
import { getImageBorderStyleConfig, imageBorderStyleOptions, type ImageBorderStyle } from './presets'

const props = defineProps<NodeViewProps>()

const src = computed({
  get: () => props.node?.attrs.src,
  set: (src: string) => {
    props.updateAttributes({ src })
  },
})

const alt = computed({
  get: () => props.node?.attrs.alt,
  set: (alt: string) => {
    props.updateAttributes({ alt })
  },
})

const fileBase64 = ref<string>()
const uploadProgress = ref<number>(0)
const retryFlag = ref<boolean>(false)
const uploading = ref<boolean>(false)
const showLinkDialog = ref<boolean>(false)
const editorLinkObtain = ref()
const borderStyleOptions = imageBorderStyleOptions
const SIZE_MIN = 10
const SIZE_MAX = 200
const sizePresets = [50, 75, 100, 125, 150]
const sizePercent = ref(100)

const initialization = computed(() => {
  return !src.value && !fileBase64.value
})

type AlignmentStyle = {
  marginLeft: string
  marginRight: string
  textAlign: string
}

const createAlignmentStyle = (alignment?: string): AlignmentStyle => {
  switch (alignment) {
    case 'center':
      return {
        marginLeft: 'auto',
        marginRight: 'auto',
        textAlign: 'center',
      }
    case 'right':
      return {
        marginLeft: 'auto',
        marginRight: '0',
        textAlign: 'right',
      }
    default:
      return {
        marginLeft: '0',
        marginRight: 'auto',
        textAlign: 'left',
      }
  }
}

const alignmentValue = computed<'left' | 'center' | 'right'>(() => props.node?.attrs.textAlign ?? 'left')

const alignmentStyle = computed<AlignmentStyle>(() =>
  createAlignmentStyle(alignmentValue.value)
)

const borderStyleValue = computed<ImageBorderStyle>({
  get: () => props.node?.attrs.borderStyle ?? 'none',
  set: (value: ImageBorderStyle) => {
    props.updateAttributes({ borderStyle: value })
  },
})

const borderStyleConfig = computed(() => getImageBorderStyleConfig(borderStyleValue.value))

const containerStyle = computed(() => {
  const style: Record<string, string> = {
    ...alignmentStyle.value,
  }
  const width = initialization.value ? '100%' : props.node?.attrs.width ?? '100%'
  if (width) {
    style.width = width
  }
  Object.assign(style, borderStyleConfig.value.wrapper)
  return style
})

const imageInlineStyle = computed(() => {
  const style: Record<string, string> = {
    width: props.node?.attrs.width ?? '100%',
  }
  if (props.node?.attrs.height) {
    style.height = props.node.attrs.height
  }
  Object.assign(style, borderStyleConfig.value.image)
  return style
})

const showSettingsBubble = computed(() =>
  Boolean(props.selected && !initialization.value && src.value && !uploading.value)
)

const clampPercent = (value: number) => Math.min(SIZE_MAX, Math.max(SIZE_MIN, Math.round(value)))

const widthToPercent = (width?: string | number | null): number => {
  if (!width) {
    return 100
  }
  const raw = String(width).trim()
  if (!raw) {
    return 100
  }
  if (raw.endsWith('%')) {
    return clampPercent(Number(raw.slice(0, -1)))
  }
  if (raw.endsWith('px') && resizeRef.value?.parentElement) {
    const containerWidth = resizeRef.value.parentElement.clientWidth || 1
    const px = Number(raw.slice(0, -2))
    if (!Number.isNaN(px) && containerWidth > 0) {
      return clampPercent(Math.round((px / containerWidth) * 100))
    }
  }
  return 100
}

watch(
  () => props.node?.attrs.width,
  (width) => {
    const next = widthToPercent(width)
    if (Math.abs(next - sizePercent.value) > 0.5) {
      sizePercent.value = next
    }
  },
  { immediate: true }
)

const setAlignment = (value: 'left' | 'center' | 'right') => {
  if (value === alignmentValue.value) {
    return
  }
  props.updateAttributes({ textAlign: value })
}

const handleAddImage = () => {
  showLinkDialog.value = true
}

const handleUploadAbort = () => {
  editorLinkObtain.value?.abort()
  uploading.value = false
  fileBase64.value = undefined
}

const handleUploadReady = async (file: File) => {
  fileBase64.value = await fileToBase64(file)
  retryFlag.value = false
}

const handleSetExternalLink = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
    alt: attachment.name,
  })
  showLinkDialog.value = false
}

const handleFileUpload = async (file: File) => {
  if (!file) {
    return
  }
  uploading.value = true
  retryFlag.value = false
  uploadProgress.value = 0
  await handleUploadReady(file)

  editorLinkObtain.value?.startUploadFromParent?.(file)
}

const handleUploadSuccess = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
    alt: attachment.name,
  })
  uploading.value = false
  fileBase64.value = undefined
  showLinkDialog.value = false
}

const handleUploadError = () => {
  retryFlag.value = true
  uploading.value = false
}

const handleUploadProgress = (progress: number) => {
  uploadProgress.value = progress
}

const handleUploadRetry = () => {
  editorLinkObtain.value?.retry()
}

const resetUpload = () => {
  fileBase64.value = undefined
  uploadProgress.value = 0
  uploading.value = false

  const { file } = props.node.attrs
  if (file) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
      file: undefined,
    })
  }
}

const handleResetInit = () => {
  editorLinkObtain.value?.reset()
  props.updateAttributes({
    src: '',
    file: undefined,
  })
  resetUpload()
  showLinkDialog.value = true
}

const aspectRatio = ref<number>(0)
const resizeRef = ref<HTMLImageElement>()
const MIN_RESIZE_WIDTH = 120

function onImageLoaded() {
  if (!resizeRef.value) return
  aspectRatio.value = resizeRef.value.clientWidth / resizeRef.value.clientHeight
  const synced = widthToPercent(props.node?.attrs.width)
  if (Math.abs(synced - sizePercent.value) > 0.5) {
    sizePercent.value = synced
  }
}

function startResize(e: MouseEvent, direction: 'left' | 'right' = 'right') {
  if (!resizeRef.value) return

  e.preventDefault()
  const startX = e.clientX
  const startWidth = resizeRef.value.clientWidth
  const maxWidth = resizeRef.value.parentElement?.clientWidth || startWidth

  const updateEditorSelection = () => {
    props.editor
      .chain()
      .setNodeSelection(props.getPos())
      .focus()
      .run()
  }

  updateEditorSelection()

  function doDrag(event: MouseEvent) {
    if (!resizeRef.value) return

    const delta = direction === 'right'
      ? event.clientX - startX
      : startX - event.clientX

    const nextWidth = Math.min(Math.max(startWidth + delta, MIN_RESIZE_WIDTH), maxWidth)
    const containerWidth = resizeRef.value?.parentElement?.clientWidth || maxWidth
    const percent = containerWidth
      ? clampPercent((nextWidth / containerWidth) * 100)
      : undefined
    const attrs: { width: string; height?: string } = percent
      ? { width: `${percent}%`, height: undefined }
      : { width: `${nextWidth.toFixed(0)}px` }

    props.editor
      .chain()
      .setNodeSelection(props.getPos())
      .updateAttributes(Image.name, attrs)
      .focus()
      .run()

    if (percent !== undefined) {
      sizePercent.value = percent
    }
  }

  function stopDrag() {
    document.removeEventListener('mousemove', doDrag)
    document.removeEventListener('mouseup', stopDrag)
  }

  document.addEventListener('mousemove', doDrag)
  document.addEventListener('mouseup', stopDrag)
}

const updateImageWidthPercent = (value: number) => {
  const clamped = clampPercent(value)
  sizePercent.value = clamped
  props.editor
    .chain()
    .setNodeSelection(props.getPos())
    .updateAttributes(Image.name, { width: `${clamped}%`, height: undefined })
    .focus()
    .run()
}

const handleSliderInput = (value: number) => {
  if (!Number.isFinite(value)) {
    return
  }
  updateImageWidthPercent(value)
}

const handleInputNumberChange = (value: number | undefined) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return
  }
  updateImageWidthPercent(value)
}

const applySizePreset = (preset: number) => {
  updateImageWidthPercent(preset)
}

onMounted(() => {
  // 如果节点包含文件属性，初始化时直接触发上传
  const { file } = props.node.attrs
  if (file) {
    handleFileUpload(file)
  }
})
</script>

<style scoped>
.image-placeholder {
  @apply border-2 border-dashed border-gray-300 rounded-lg p-6 text-center;
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  @apply text-gray-500;
}

.placeholder-icon {
  @apply text-4xl mb-4;
}

.upload-status {
  @apply p-4;
}

.preview-image {
  @apply mb-4;
}

.upload-progress {
  @apply space-y-2;
}

.progress-actions {
  @apply flex justify-center space-x-2;
}

.image-floating-toolbar {
  @apply absolute left-1/2 -translate-x-1/2 -top-3 flex items-center gap-4 rounded-full border border-slate-200 bg-white/95 px-4 py-2 shadow-lg backdrop-blur transition-opacity;
  z-index: 20;
  min-width: 320px;
}

.image-floating-toolbar ::-webkit-scrollbar {
  height: 4px;
}

.toolbar-section {
  @apply flex items-center gap-2;
}

.slider-section {
  @apply w-72 flex flex-col gap-2;
}

.slider-controls {
  @apply flex items-center gap-3;
}

.size-input {
  width: 90px;
}

.size-input :deep(.el-input__wrapper) {
  @apply px-2;
}

.preset-buttons {
  @apply flex items-center gap-2;
}

.preset-list {
  @apply flex flex-wrap gap-1;
}

.image-size-slider :deep(.el-slider__runway) {
  @apply bg-slate-200/70;
}

.image-size-slider :deep(.el-slider__bar) {
  @apply bg-blue-500/80;
}

.toolbar-label {
  @apply text-xs font-semibold text-slate-500;
}

.toolbar-label--secondary {
  @apply text-[11px] font-medium text-slate-400;
}

.alignment-buttons {
  @apply flex gap-1;
}

.border-style-select {
  width: 120px;
}

.border-style-select :deep(.el-input__wrapper) {
  @apply shadow-none border-slate-200;
}

.image-floating-toolbar :deep(.el-button.is-plain) {
  @apply border-slate-200;
}

.resize-handle {
  @apply absolute opacity-0 group-hover:opacity-100 transition-opacity;
}

.image-selected .resize-handle {
  @apply opacity-100;
}

.resize-outline {
  @apply absolute inset-0 rounded-2xl border-2 border-blue-400/80 pointer-events-none;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  z-index: 10;
}

.resize-handle--left,
.resize-handle--right {
  @apply top-1/2 h-10 w-2 -translate-y-1/2 bg-blue-500/30 cursor-ew-resize rounded;
}

.resize-handle--left {
  @apply -left-3;
}

.resize-handle--right {
  @apply -right-3;
}

.resize-handle--corner {
  @apply bottom-2 right-2 h-4 w-4 rounded-full bg-blue-500 cursor-se-resize;
}

.image-toolbar-fade-enter-from,
.image-toolbar-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -6px);
}

.image-toolbar-fade-enter-active,
.image-toolbar-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
</style>
