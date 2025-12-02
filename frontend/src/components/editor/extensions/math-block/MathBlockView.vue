<template>
  <NodeViewWrapper class="math-block-wrapper" :data-selected="selected">
    <div class="math-block-header">
      <div class="math-block-title">
        <el-icon><Collection /></el-icon>
        <span>数学公式</span>
      </div>
      <div class="math-block-actions">
        <el-button text size="small" @click="openEditor">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button text size="small" @click="copyLatex">
          <el-icon><DocumentCopy /></el-icon>
          复制 LaTeX
        </el-button>
      </div>
    </div>

    <div class="math-block-body">
      <div ref="renderTarget" class="math-block-render"></div>
      <div v-if="errorMessage" class="math-block-error">
        <el-icon><Warning /></el-icon>
        <span>{{ errorMessage }}</span>
      </div>
    </div>

    <el-dialog
      v-model="editing"
      title="编辑数学公式"
      width="480px"
      append-to-body
      @close="cancelEdit"
    >
      <el-input
        v-model="draftLatex"
        type="textarea"
        :rows="6"
        placeholder="请输入 LaTeX 表达式，例如：E = mc^2"
      />
      <div class="math-block-preview-title">实时预览</div>
      <div class="math-block-preview">
        <div ref="previewTarget" class="math-block-preview-render"></div>
        <div v-if="previewError" class="math-block-preview-error">
          <el-icon><Warning /></el-icon>
          <span>{{ previewError }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" @click="applyLatex">应用</el-button>
      </template>
    </el-dialog>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElButton, ElDialog, ElIcon, ElInput, ElMessage } from 'element-plus'
import { Collection, DocumentCopy, Edit, Warning } from '@element-plus/icons-vue'
import { NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const props = defineProps<NodeViewProps>()

const renderTarget = ref<HTMLElement | null>(null)
const previewTarget = ref<HTMLElement | null>(null)
const editing = ref(false)
const latex = ref(typeof props.node.attrs.latex === 'string' ? props.node.attrs.latex : '')
const draftLatex = ref(latex.value)
const errorMessage = ref('')
const previewError = ref('')

const selected = computed(() => props.selected)

const renderFormula = (source: string, target: HTMLElement | null, displayMode: boolean) => {
  if (!target) return
  try {
    katex.render(source || '', target, {
      displayMode,
      throwOnError: false,
      errorColor: '#dc2626',
      strict: 'ignore',
    })
    if (displayMode) {
      errorMessage.value = ''
    } else {
      previewError.value = ''
    }
  } catch (error: any) {
    target.innerHTML = ''
    const message = typeof error?.message === 'string' ? error.message : '公式渲染失败'
    if (displayMode) {
      errorMessage.value = message
    } else {
      previewError.value = message
    }
  }
}

const syncFromNode = () => {
  const next = typeof props.node.attrs.latex === 'string' ? props.node.attrs.latex : ''
  latex.value = next
  if (!editing.value) {
    draftLatex.value = next
  }
  renderFormula(next, renderTarget.value, true)
}

onMounted(() => {
  renderFormula(latex.value, renderTarget.value, true)
})

watch(
  () => props.node.attrs.latex,
  () => {
    syncFromNode()
  }
)

watch(editing, async (value) => {
  if (value) {
    draftLatex.value = latex.value
    await nextTick()
    renderFormula(draftLatex.value, previewTarget.value, true)
  }
})

watch(draftLatex, (value) => {
  if (!editing.value) return
  renderFormula(value, previewTarget.value, true)
})

const openEditor = () => {
  editing.value = true
}

const cancelEdit = () => {
  editing.value = false
  draftLatex.value = latex.value
  previewError.value = ''
}

const applyLatex = () => {
  latex.value = draftLatex.value
  props.updateAttributes({
    latex: latex.value,
  })
  editing.value = false
  previewError.value = ''
}

const copyLatex = async () => {
  try {
    await navigator.clipboard.writeText(latex.value || '')
    ElMessage.success('LaTeX 已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请稍后再试')
  }
}
</script>

<style scoped>
.math-block-wrapper {
  @apply border border-gray-200 rounded-lg overflow-hidden my-4;
  background: #ffffff;
}

[data-selected='true'].math-block-wrapper {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.math-block-header {
  @apply flex items-center justify-between px-4 py-2 border-b border-gray-200 bg-gray-50;
}

.math-block-title {
  @apply flex items-center gap-2 font-medium text-gray-800;
}

.math-block-actions {
  @apply flex items-center gap-2;
}

.math-block-body {
  @apply px-4 py-3;
}

.math-block-render {
  @apply overflow-x-auto py-3;
  font-size: 1.05rem;
}

.math-block-error {
  @apply mt-2 flex items-center gap-2 text-sm text-red-600;
}

.math-block-preview-title {
  @apply text-sm font-medium text-gray-600 mt-4 mb-2;
}

.math-block-preview {
  @apply border border-gray-200 rounded-md px-3 py-2 bg-gray-50;
  min-height: 80px;
}

.math-block-preview-render {
  @apply overflow-x-auto;
}

.math-block-preview-error {
  @apply mt-2 flex items-center gap-2 text-sm text-red-600;
}

.katex-display {
  margin: 0 !important;
}
</style>
