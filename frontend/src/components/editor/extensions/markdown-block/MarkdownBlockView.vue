<template>
  <NodeViewWrapper class="markdown-block-wrapper">
    <div class="markdown-block-header">
      <span class="markdown-block-title">Markdown 代码块</span>
      <div class="markdown-block-actions">
        <el-button text size="small" @click="toggleEdit">
          <el-icon>
            <Edit v-if="!isEditing" />
            <Check v-else />
          </el-icon>
          {{ isEditing ? '完成' : '编辑' }}
        </el-button>
        <el-button text size="small" @click="copyCode">
          <el-icon><DocumentCopy /></el-icon>
          复制
        </el-button>
      </div>
    </div>

    <div v-if="isEditing" class="markdown-block-editor">
      <el-input
        v-model="markdownContent"
        type="textarea"
        :rows="10"
        placeholder="请输入 Markdown 代码..."
        @blur="updateContent"
      />
    </div>

    <div v-else class="markdown-block-preview">
      <div v-if="markdownContent" v-html="renderedMarkdown" class="markdown-content"></div>
      <div v-else class="markdown-placeholder">
        点击编辑按钮添加 Markdown 代码
      </div>
    </div>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { TextSelection } from '@tiptap/pm/state'
import { ElButton, ElIcon, ElInput, ElMessage } from 'element-plus'
import { Edit, Check, DocumentCopy } from '@element-plus/icons-vue'
import { NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'

import { createMarkdownRenderer } from '@/composables/useMarkdown'

const props = defineProps<NodeViewProps>()

const isEditing = ref(false)
const markdownContent = ref(props.node?.attrs.content || '')

// 增强的 Markdown 渲染功能
const { parse, ensureHighlightLanguages } = createMarkdownRenderer()
ensureHighlightLanguages()

const renderedMarkdown = computed(() => {
  if (!markdownContent.value) return ''
  try {
    return parse(markdownContent.value)
  } catch (error) {
    console.warn('Failed to parse markdown block', error)
    return markdownContent.value
  }
})

const ensureCursorAfterNode = () => {
  if (!props.editor || typeof props.getPos !== 'function') {
    return
  }

  const nodePos = props.getPos()
  if (typeof nodePos !== 'number') {
    return
  }

  nextTick(() => {
    props.editor
      ?.chain()
      .focus()
      .command(({ state, tr, dispatch }) => {
        const node = state.doc.nodeAt(nodePos)
        if (!node) {
          return false
        }

        const posAfter = nodePos + node.nodeSize
        const paragraphType = state.schema.nodes.paragraph
        const $posAfter = tr.doc.resolve(posAfter)
        const nextNode = $posAfter.nodeAfter
        let transaction = tr
        let targetPos = posAfter

        const needsNewParagraph =
          !nextNode ||
          !nextNode.isTextblock ||
          (nextNode.isTextblock && nextNode.content.size > 0)

        if (needsNewParagraph) {
          if (!paragraphType) {
            return false
          }
          transaction = transaction.insert(posAfter, paragraphType.create())
          targetPos = posAfter + 1
        } else {
          targetPos = posAfter + 1
        }

        const selection = TextSelection.create(transaction.doc, targetPos)
        transaction = transaction.setSelection(selection).scrollIntoView()
        if (dispatch) {
          dispatch(transaction)
        }

        return true
      })
      .run()
  })
}

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value) {
    updateContent()
    ensureCursorAfterNode()
  }
}

const updateContent = () => {
  props.updateAttributes({ content: markdownContent.value })
}

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(markdownContent.value)
    ElMessage.success('Markdown 代码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.markdown-block-wrapper {
  @apply border border-gray-200 rounded-lg my-4 overflow-hidden;
}

.markdown-block-header {
  @apply flex items-center justify-between p-3 bg-gray-50 border-b border-gray-200;
}

.markdown-block-title {
  @apply font-medium text-gray-900;
}

.markdown-block-actions {
  @apply flex items-center space-x-2;
}

.markdown-block-editor {
  @apply p-4;
}

.markdown-block-preview {
  @apply p-4 min-h-[100px];
}

.markdown-content {
  font-size: 16.5px;
  line-height: 1.75;
  color: #0f172a;
  letter-spacing: 0.01em;
  word-break: break-word;
}

.markdown-content > :first-child {
  margin-top: 0 !important;
}

.markdown-content > :last-child {
  margin-bottom: 0 !important;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4) {
  color: #0f172a;
  font-weight: 700;
  margin: 1.5em 0 0.65em;
  line-height: 1.3;
}

.markdown-content :deep(h1) {
  font-size: 2.1rem;
}

.markdown-content :deep(h2) {
  font-size: 1.6rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.35em;
}

.markdown-content :deep(h3) {
  font-size: 1.35rem;
}

.markdown-content :deep(h4) {
  font-size: 1.2rem;
}

.markdown-content :deep(h5) {
  font-size: 1.05rem;
  font-weight: 600;
  margin: 1.2em 0 0.5em;
}

.markdown-content :deep(h6) {
  font-size: 0.95rem;
  font-weight: 600;
  margin: 1.1em 0 0.45em;
}

.markdown-content :deep(p) {
  margin: 0.75em 0;
}

.markdown-content :deep(p:empty) {
  display: none;
  margin: 0;
}

@supports selector(:has(*)) {
  .markdown-content :deep(p:has(> br:only-child)) {
    display: none;
  }
}

.markdown-content :deep(strong) {
  font-weight: 700;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(del) {
  opacity: 0.75;
}

.markdown-content :deep(code) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 0.9em;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
}

.markdown-content :deep(pre) {
  margin: 1.4em 0;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 16px 16px;
  overflow-x: auto;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  font-size: 0.92em;
  color: #0f172a;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  background: #eff6ff;
  padding: 14px 18px;
  margin: 1.2em 0;
  border-radius: 0 14px 14px 0;
  color: #1e3a8a;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.75em 0 0.75em 1.6em;
  padding-left: 0;
}

.markdown-content :deep(li) {
  margin: 0.35em 0;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px dashed #e2e8f0;
  margin: 2em 0;
}

.markdown-content :deep(a) {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px dashed rgba(37, 99, 235, 0.35);
}

.markdown-content :deep(a:hover) {
  border-bottom-color: rgba(37, 99, 235, 0.6);
}

.markdown-content :deep(img) {
  display: block;
  max-width: 100%;
  border-radius: 16px;
  margin: 1.6em 0;
  box-shadow: 0 14px 40px -30px rgba(15, 23, 42, 0.55);
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.4em 0;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  text-align: left;
}

.markdown-content :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.markdown-content :deep(tr:nth-child(even)) {
  background: #f9fafb;
}

.markdown-placeholder {
  @apply text-gray-500 text-center py-8;
}
</style>
