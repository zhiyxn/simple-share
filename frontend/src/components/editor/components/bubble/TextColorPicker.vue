<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom"
    trigger="click"
    :width="260"
    popper-class="color-picker-popover"
  >
    <div class="color-picker">
      <div class="color-picker__section">
        <div class="color-picker__label">快速选择</div>
        <div class="color-picker__grid">
          <button
            v-for="colorOption in colorOptions"
            :key="colorOption"
            type="button"
            class="color-picker__swatch"
            :class="{ 'is-active': isActiveColor(colorOption) }"
            :style="{ backgroundColor: colorOption }"
            @click="handleSelect(colorOption)"
          />
        </div>
      </div>
      <el-divider />
      <div class="color-picker__section">
        <div class="color-picker__label">自定义颜色</div>
        <div class="color-picker__custom">
          <el-color-picker
            v-model="customColor"
            :predefine="colorOptions"
            :teleported="false"
            @change="handleCustomColorChange"
          />
          <el-input
            v-model="customColorInput"
            size="small"
            placeholder="#000000"
            @keyup.enter.prevent="handleApplyCustomColor"
          />
          <el-button
            size="small"
            type="primary"
            :disabled="!isCustomColorValid"
            @click="handleApplyCustomColor"
          >
            应用
          </el-button>
        </div>
      </div>
      <div class="color-picker__actions">
        <el-button
          size="small"
          type="info"
          plain
          @click="handleClear"
        >
          清除{{ isHighlightMode ? '背景' : '颜色' }}
        </el-button>
      </div>
    </div>
    <template #reference>
      <button
        type="button"
        class="bubble-button"
        :class="{ 'is-active': hasActiveColor, 'is-highlight': isHighlightMode }"
        :style="previewButtonStyle"
      >
        <span
          v-if="previewColor"
          class="color-preview"
          :class="{ 'is-highlight': isHighlightMode }"
          :style="{ backgroundColor: previewColor }"
        />
        <el-icon>
          <component :is="icon" />
        </el-icon>
      </button>
    </template>
  </el-popover>
</template>

<script lang="ts">
const DEFAULT_COLORS = [
  '#1f2937',
  '#111827',
  '#ef4444',
  '#f97316',
  '#f59e0b',
  '#10b981',
  '#3b82f6',
  '#6366f1',
  '#8b5cf6',
  '#ec4899',
  '#fde68a',
  '#fef3c7',
  '#d1d5db',
  '#9ca3af',
  '#6b7280',
]
</script>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import { ElPopover, ElButton, ElIcon, ElDivider, ElColorPicker, ElInput } from 'element-plus'
import MdiFormatColorText from '~icons/mdi/format-color-text'
import MdiFormatColorFill from '~icons/mdi/format-color-fill'

interface Props {
  editor: Editor
  mode?: 'color' | 'highlight'
  colors?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'color',
  colors: () => DEFAULT_COLORS,
})

const visible = ref(false)

const colorOptions = computed(() => props.colors)

const isHighlightMode = computed(() => props.mode === 'highlight')

const defaultCustomColor = computed(() =>
  isHighlightMode.value ? '#fde68a' : '#111827'
)

const icon = computed(() =>
  isHighlightMode.value ? MdiFormatColorFill : MdiFormatColorText
)

const activeColor = computed(() => {
  if (isHighlightMode.value) {
    return props.editor.getAttributes('highlight')?.color || ''
  }
  return props.editor.getAttributes('textStyle')?.color || ''
})

const hasActiveColor = computed(() => Boolean(activeColor.value))

const previewColor = computed(() => {
  if (!activeColor.value) {
    return ''
  }
  return normalizeHexColor(activeColor.value)
})

const getReadableTextColor = (hexColor: string) => {
  const hex = normalizeHexColor(hexColor).replace('#', '')
  if (hex.length !== 6) {
    return '#111827'
  }
  const r = parseInt(hex.slice(0, 2), 16)
  const g = parseInt(hex.slice(2, 4), 16)
  const b = parseInt(hex.slice(4, 6), 16)
  const luminance = 0.299 * r + 0.587 * g + 0.114 * b
  return luminance > 186 ? '#111827' : '#ffffff'
}

const previewButtonStyle = computed(() => {
  if (!previewColor.value) {
    return {}
  }

  if (isHighlightMode.value) {
    return {
      backgroundColor: previewColor.value,
      color: getReadableTextColor(previewColor.value),
    }
  }

  return {
    backgroundColor: previewColor.value,
    color: getReadableTextColor(previewColor.value),
  }
})

const isActiveColor = (color: string) => {
  return activeColor.value?.toLowerCase() === color.toLowerCase()
}

const customColor = ref(defaultCustomColor.value)
const customColorInput = ref(defaultCustomColor.value)

const syncCustomColor = (color?: string) => {
  const nextColor = color || defaultCustomColor.value
  customColor.value = nextColor
  customColorInput.value = nextColor
}

watch(
  () => activeColor.value,
  (color) => {
    syncCustomColor(color)
  },
  { immediate: true }
)

const HEX_COLOR_REGEX = /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/

const isCustomColorValid = computed(() => {
  const value = customColorInput.value?.trim() || ''
  const prefixed = value.startsWith('#') ? value : `#${value}`
  return HEX_COLOR_REGEX.test(prefixed)
})

const normalizeHexColor = (value: string) => {
  if (!value) {
    return ''
  }
  const trimmed = value.trim()
  const prefixed = trimmed.startsWith('#') ? trimmed : `#${trimmed}`
  if (/^#([0-9a-fA-F]{3})$/.test(prefixed)) {
    const chars = prefixed.slice(1)
    return `#${chars
      .split('')
      .map((char) => `${char}${char}`)
      .join('')}`.toLowerCase()
  }
  return prefixed.toLowerCase()
}

const applyColor = (color: string) => {
  if (isHighlightMode.value) {
    props.editor.chain().focus().setHighlight({ color }).run()
  } else {
    props.editor.chain().focus().setColor(color).run()
  }
}

const clearColor = () => {
  if (isHighlightMode.value) {
    props.editor.chain().focus().unsetHighlight().run()
  } else {
    props.editor.chain().focus().unsetColor().run()
  }
}

const handleSelect = (color: string) => {
  applyColor(color)
  visible.value = false
}

const handleClear = () => {
  clearColor()
  visible.value = false
}

const handleCustomColorChange = (color: string | null) => {
  if (!color) {
    return
  }
  customColor.value = color
  customColorInput.value = color
}

const handleApplyCustomColor = () => {
  if (!isCustomColorValid.value) {
    return
  }
  const normalized = normalizeHexColor(customColorInput.value)
  syncCustomColor(normalized)
  applyColor(normalized)
  visible.value = false
}
</script>

<style scoped>
.bubble-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 1px solid transparent;
  background: transparent;
  color: #4b5563;
  transition: all 0.2s ease;
  position: relative;
}

.bubble-button .color-preview {
  position: absolute;
  top: 6px;
  left: 6px;
  width: 8px;
  height: 20px;
  border-radius: 2px;
  opacity: 0.9;
  pointer-events: none;
}

.bubble-button .color-preview.is-highlight {
  width: 20px;
  height: 4px;
  top: 22px;
  left: 6px;
  border-radius: 999px;
}

.bubble-button:hover {
  background: #f3f4f6;
  color: #111827;
}

.bubble-button.is-active {
  background: #111827;
  color: #ffffff;
}

.color-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-picker__section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-picker__label {
  font-size: 12px;
  color: #6b7280;
}

.color-picker__grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.color-picker__swatch {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: transform 0.15s ease, border-color 0.15s ease;
}

.color-picker__swatch:hover {
  transform: translateY(-1px);
}

.color-picker__swatch.is-active {
  border-color: var(--el-color-primary);
}

.color-picker__custom {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-picker__custom :deep(.el-color-picker) {
  --el-color-picker-trigger-size: 32px;
}

.color-picker__custom :deep(.el-input) {
  flex: 1;
}

.color-picker__actions {
  display: flex;
  justify-content: center;
}

:deep(.color-picker-popover) {
  padding: 12px 16px;
}
</style>
