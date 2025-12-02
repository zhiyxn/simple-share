<template>
  <NodeViewWrapper as="span" class="math-inline-wrapper" :data-selected="selected">
    <span ref="renderTarget" class="math-inline-render" @dblclick.stop="openEditor"></span>

    <el-dialog
      v-model="editing"
      title="编辑行内公式"
      width="420px"
      append-to-body
      @close="cancelEdit"
    >
      <el-input
        v-model="draftLatex"
        type="textarea"
        :rows="3"
        placeholder="请输入 LaTeX 行内公式，例如：e^{i\\pi} + 1 = 0"
      />
      <div class="math-inline-preview-title">实时预览</div>
      <div class="math-inline-preview">
        <span ref="previewTarget" class="math-inline-preview-render"></span>
        <div v-if="previewError" class="math-inline-preview-error">
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
import { NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'
import { ElDialog, ElIcon, ElInput } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const props = defineProps<NodeViewProps>()

const renderTarget = ref<HTMLElement | null>(null)
const previewTarget = ref<HTMLElement | null>(null)
const editing = ref(false)
const latex = ref(typeof props.node.attrs.latex === 'string' ? props.node.attrs.latex : '')
const draftLatex = ref(latex.value)
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
    if (!displayMode) {
      previewError.value = ''
    }
  } catch (error: any) {
    target.innerHTML = ''
    if (!displayMode) {
      previewError.value = typeof error?.message === 'string' ? error.message : '公式渲染失败'
    }
  }
}

const syncFromNode = () => {
  const next = typeof props.node.attrs.latex === 'string' ? props.node.attrs.latex : ''
  latex.value = next
  if (!editing.value) {
    draftLatex.value = next
  }
  renderFormula(next, renderTarget.value, false)
}

onMounted(() => {
  renderFormula(latex.value, renderTarget.value, false)
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
    renderFormula(draftLatex.value, previewTarget.value, false)
  }
})

watch(draftLatex, (value) => {
  if (!editing.value) return
  renderFormula(value, previewTarget.value, false)
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
  props.updateAttributes({ latex: latex.value })
  editing.value = false
  previewError.value = ''
}
</script>

<style scoped>
.math-inline-wrapper {
  display: inline-flex;
  align-items: center;
}

.math-inline-render {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.math-inline-render:hover {
  box-shadow: 0 1px 0 rgba(37, 99, 235, 0.45);
}

[data-selected='true'].math-inline-wrapper .math-inline-render {
  background: rgba(37, 99, 235, 0.08);
  border-radius: 6px;
  padding: 2px 4px;
}

.math-inline-preview-title {
  @apply text-sm font-medium text-gray-600 mt-4 mb-2;
}

.math-inline-preview {
  @apply border border-gray-200 rounded-md px-3 py-2 bg-gray-50;
  min-height: 48px;
}

.math-inline-preview-render {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
}

.math-inline-preview-error {
  @apply mt-2 flex items-center gap-2 text-sm text-red-600;
}
</style>
