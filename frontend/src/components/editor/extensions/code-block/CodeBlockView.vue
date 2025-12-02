<template>
  <NodeViewWrapper class="code-block-wrapper">
    <div class="code-block-header" contenteditable="false">
      <div class="header-left">
        <el-button
          size="small"
          text
          @click="collapsed = !collapsed"
          class="collapse-btn"
        >
          <el-icon>
            <ArrowRight v-if="collapsed" />
            <ArrowDown v-else />
          </el-icon>
        </el-button>
        
        <el-select
          v-model="selectedLanguage"
          size="small"
          class="language-select"
          @change="handleLanguageChange"
        >
          <el-option
            v-for="lang in languageOptions"
            :key="lang.value"
            :label="lang.label"
            :value="lang.value"
          />
        </el-select>
      </div>
      
      <div class="header-right">
        <el-button
          size="small"
          text
          @click="handleCopyCode"
          :disabled="!copyReady"
          class="copy-btn"
        >
          <el-icon>
            <Check v-if="!copyReady" class="text-green-500" />
            <DocumentCopy v-else />
          </el-icon>
        </el-button>
      </div>
    </div>
    
    <div
      v-show="!collapsed"
      class="code-content"
      ref="codeContentContainer"
      @keydown.capture="handleKeyDown"
    >
      <NodeViewContent
        as="pre"
        class="code-pre"
        :contenteditable="true"
        spellcheck="false"
        :data-language="selectedLanguage"
        ref="codePreRef"
      />
    </div>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { ElButton, ElSelect, ElOption, ElIcon } from 'element-plus'
import { ArrowRight, ArrowDown, Check, DocumentCopy } from '@element-plus/icons-vue'
import { NodeViewContent, NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'

const props = defineProps<NodeViewProps>()

const copyReady = ref(true)
const codeContentContainer = ref<HTMLElement | null>(null)
const codePreRef = ref<HTMLElement | null>(null)

const languageOptions = computed(() => {
  return props.extension.options.languages || []
})

const selectedLanguage = computed({
  get: () => {
    const language = props.node?.attrs.language
    return language === undefined || language === null ? 'javascript' : language
  },
  set: (language: string) => {
    props.updateAttributes({ language })
  },
})

const collapsed = computed({
  get: () => props.node?.attrs.collapsed || false,
  set: (collapsed: boolean) => {
    props.updateAttributes({ collapsed })
  },
})

const getSelectionRangeWithinNode = (): { from: number; to: number } | null => {
  if (typeof props.getPos !== 'function') {
    return null
  }

  const nodePos = props.getPos()
  if (typeof nodePos !== 'number') {
    return null
  }

  const start = nodePos + 1
  const end = nodePos + props.node.nodeSize - 1
  const { selection } = props.editor.state

  if (selection.from < start || selection.to > end) {
    return null
  }

  return {
    from: selection.from,
    to: selection.to,
  }
}

const handleLanguageChange = (language: string) => {
  const selectionRange = getSelectionRangeWithinNode()
  selectedLanguage.value = language

  nextTick(() => {
    if (selectionRange) {
      props.editor.chain().setTextSelection(selectionRange).run()
    } else {
      props.editor.view?.focus()
    }
  })
}

const handleCopyCode = async () => {
  if (!copyReady.value) return
  
  const code = props.node.textContent
  try {
    await navigator.clipboard.writeText(code)
    copyReady.value = false
    setTimeout(() => {
      copyReady.value = true
    }, 2000)
  } catch (error) {
    console.error('Failed to copy code:', error)
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (!(event.metaKey || event.ctrlKey)) {
    return
  }

  if (event.key.toLowerCase() !== 'a') {
    return
  }

  if (!codeContentContainer.value?.contains(event.target as Node)) {
    return
  }

  const preEl = codePreRef.value
  if (!preEl) {
    return
  }

  const selection = window.getSelection()
  if (!selection) {
    return
  }

  event.preventDefault()
  event.stopPropagation()

  const range = document.createRange()
  range.selectNodeContents(preEl)
  selection.removeAllRanges()
  selection.addRange(range)
}
</script>

<style scoped>
.code-block-wrapper {
  @apply mt-4 rounded-2xl border border-slate-800 shadow-xl;
  background: linear-gradient(150deg, #050b17 0%, #0d1117 55%, #05070d 100%);
  color: #e6edf3;
  overflow: hidden;
  max-width: 100%;
  width: 100%;
  margin-bottom: 18px;
  contain: content;
}

.code-block-header {
  @apply flex items-center justify-between border-b border-slate-900 px-4 py-2.5;
  background: linear-gradient(120deg, rgba(9, 13, 23, 0.95), rgba(14, 20, 35, 0.9));
}

.header-left {
  @apply flex items-center space-x-3;
}

.header-right {
  @apply flex items-center space-x-2;
}

.collapse-btn,
.copy-btn {
  @apply !p-1.5 rounded transition-colors text-slate-200;
}

.collapse-btn:hover,
.copy-btn:hover {
  background-color: rgba(56, 189, 248, 0.15);
}

.language-select {
  @apply w-40 text-sm;
}

.language-select :deep(.el-select__wrapper) {
  @apply bg-transparent border border-slate-700 text-slate-200;
  box-shadow: none;
}

.language-select :deep(.el-select__placeholder),
.language-select :deep(.el-select__selected-item) {
  @apply text-slate-200;
}

.code-content {
  @apply relative;
  background: #0b1120;
  max-height: 420px;
  overflow: auto;
}

.code-pre {
  @apply m-0 text-[13px] font-mono;
  padding: 1.25rem 1.5rem;
  white-space: pre-wrap !important;
  word-break: break-word;
  overflow-wrap: anywhere;
  tab-size: 2;
  line-height: 1.65;
  min-height: 140px;
  color: #e6edf3;
  background: transparent !important;
  border-left: 3px solid var(--code-accent-color, #3b82f6);
  overflow: auto;
}

.code-pre :deep(.hljs) {
  color: #e6edf3 !important;
  background: transparent !important;
  text-shadow: 0 0.5px 0 rgba(0, 0, 0, 0.2);
}

.code-pre :deep(.hljs-comment),
.code-pre :deep(.hljs-quote) {
  color: #8b949e !important;
  font-style: italic;
}

.code-pre :deep(.hljs-keyword),
.code-pre :deep(.hljs-selector-tag),
.code-pre :deep(.hljs-literal),
.code-pre :deep(.hljs-symbol) {
  color: #ff7b72 !important;
}

.code-pre :deep(.hljs-built_in),
.code-pre :deep(.hljs-type),
.code-pre :deep(.hljs-class .hljs-title),
.code-pre :deep(.hljs-attr),
.code-pre :deep(.hljs-attribute),
.code-pre :deep(.hljs-name),
.code-pre :deep(.hljs-variable),
.code-pre :deep(.hljs-template-variable) {
  color: #79c0ff !important;
}

.code-pre :deep(.hljs-string),
.code-pre :deep(.hljs-doctag),
.code-pre :deep(.hljs-meta .hljs-string),
.code-pre :deep(.hljs-regexp) {
  color: #9be9a8 !important;
}

.code-pre :deep(.hljs-number),
.code-pre :deep(.hljs-meta),
.code-pre :deep(.hljs-title),
.code-pre :deep(.hljs-section) {
  color: #f4d3ff !important;
}

.code-pre :deep(.hljs-function),
.code-pre :deep(.hljs-title .hljs-title),
.code-pre :deep(.hljs-params) {
  color: #d2a8ff !important;
}

.code-pre :deep(.hljs-strong) {
  font-weight: 600;
  color: #ffd580 !important;
}

.code-pre :deep(.hljs-emphasis) {
  font-style: italic;
  color: #7ee787 !important;
}

.code-pre :deep(.hljs-addition) {
  color: #7ee787 !important;
}

.code-pre :deep(.hljs-deletion) {
  color: #ffa198 !important;
}

/* 语言高亮 - 使用强调色而不是背景色 */
.code-pre[data-language='javascript'] {
  --code-accent-color: #facc15;
}

.code-pre[data-language='typescript'] {
  --code-accent-color: #38bdf8;
}

.code-pre[data-language='python'] {
  --code-accent-color: #34d399;
}

.code-pre[data-language='java'] {
  --code-accent-color: #fb7185;
}

.code-pre[data-language='css'] {
  --code-accent-color: #c084fc;
}

.code-pre[data-language='html'] {
  --code-accent-color: #f87171;
}

.code-pre[data-language='json'] {
  --code-accent-color: #818cf8;
}

.code-pre[data-language='bash'] {
  --code-accent-color: #94a3b8;
}

.code-pre :deep(code) {
  @apply bg-transparent p-0 text-inherit;
  font-family: 'Fira Code', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.code-pre ::selection {
  background: rgba(56, 189, 248, 0.35);
}

/* 滚动条样式 */
.code-content::-webkit-scrollbar,
.code-pre::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.code-content::-webkit-scrollbar-track,
.code-pre::-webkit-scrollbar-track {
  background: rgba(148, 163, 184, 0.15);
  border-radius: 999px;
}

.code-content::-webkit-scrollbar-thumb,
.code-pre::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.4);
  border-radius: 999px;
}

.code-content::-webkit-scrollbar-thumb:hover,
.code-pre::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.65);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .code-block-wrapper {
    @apply mx-2;
  }
  
  .language-select {
    @apply w-28 text-xs;
  }
  
  .code-pre {
    @apply text-xs p-4;
  }
}
</style>
