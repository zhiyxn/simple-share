<template>
  <teleport to="body">
    <transition name="table-toolbar-fade">
      <div
        v-show="visible"
        class="table-floating-toolbar"
        :style="toolbarStyle"
      >
        <div class="table-floating-toolbar__section">
          <span class="table-floating-toolbar__label">列</span>
          <el-button-group>
            <el-tooltip content="在左侧插入列" placement="top">
              <el-button size="small" @click="addColumnBefore">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="在右侧插入列" placement="top">
              <el-button size="small" @click="addColumnAfter">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除当前列" placement="top">
              <el-button
                size="small"
                type="danger"
                :disabled="!capabilities.canDeleteColumn"
                @click="deleteColumn"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </div>

        <div class="table-floating-toolbar__divider" />

        <div class="table-floating-toolbar__section">
          <span class="table-floating-toolbar__label">行</span>
          <el-button-group>
            <el-tooltip content="在上方插入行" placement="top">
              <el-button size="small" @click="addRowBefore">
                <el-icon><ArrowUp /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="在下方插入行" placement="top">
              <el-button size="small" @click="addRowAfter">
                <el-icon><ArrowDown /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除当前行" placement="top">
              <el-button
                size="small"
                type="danger"
                :disabled="!capabilities.canDeleteRow"
                @click="deleteRow"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </div>

        <div class="table-floating-toolbar__divider" />

        <div class="table-floating-toolbar__section">
          <span class="table-floating-toolbar__label">结构</span>
          <el-button-group>
            <el-tooltip content="切换表头行" placement="top">
              <el-button
                size="small"
                :type="hasHeaderRow ? 'primary' : 'default'"
                @click="toggleHeaderRow"
              >
                <el-icon><Minus /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="切换表头列" placement="top">
              <el-button
                size="small"
                :type="hasHeaderColumn ? 'primary' : 'default'"
                @click="toggleHeaderColumn"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="合并单元格" placement="top">
              <el-button
                size="small"
                :disabled="!capabilities.canMergeCells"
                @click="mergeCells"
              >
                <el-icon><Connection /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="拆分单元格" placement="top">
              <el-button
                size="small"
                :disabled="!capabilities.canSplitCell"
                @click="splitCell"
              >
                <el-icon><Crop /></el-icon>
              </el-button>
            </el-tooltip>
          </el-button-group>
        </div>

        <div class="table-floating-toolbar__divider" />

        <div class="table-floating-toolbar__section">
          <span class="table-floating-toolbar__label">对齐</span>
          <el-button-group>
            <el-tooltip content="左对齐" placement="top">
              <el-button
                size="small"
                :type="alignment === 'left' ? 'primary' : 'default'"
                :disabled="!capabilities.canAlign"
                @click="setAlignment('left')"
              >
                左
              </el-button>
            </el-tooltip>
            <el-tooltip content="居中对齐" placement="top">
              <el-button
                size="small"
                :type="alignment === 'center' ? 'primary' : 'default'"
                :disabled="!capabilities.canAlign"
                @click="setAlignment('center')"
              >
                中
              </el-button>
            </el-tooltip>
            <el-tooltip content="右对齐" placement="top">
              <el-button
                size="small"
                :type="alignment === 'right' ? 'primary' : 'default'"
                :disabled="!capabilities.canAlign"
                @click="setAlignment('right')"
              >
                右
              </el-button>
            </el-tooltip>
          </el-button-group>
        </div>

        <div class="table-floating-toolbar__divider" />

        <div class="table-floating-toolbar__section">
          <el-popconfirm
            title="确认删除整张表格？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            confirm-button-type="danger"
            @confirm="deleteTable"
          >
            <template #reference>
              <el-button size="small" type="danger">
                <el-icon><Delete /></el-icon>
                删除表格
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ElButton, ElButtonGroup, ElTooltip, ElPopconfirm, ElIcon } from 'element-plus'
import { ArrowLeft, ArrowRight, ArrowUp, ArrowDown, Delete, Connection, Crop, Minus, Plus } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import { NodeSelection } from '@tiptap/pm/state'

type Alignment = 'left' | 'center' | 'right'

interface Props {
  editor: Editor | null
}

const props = defineProps<Props>()

const visible = ref(false)
const alignment = ref<Alignment>('left')
const capabilities = reactive({
  canDeleteColumn: false,
  canDeleteRow: false,
  canMergeCells: false,
  canSplitCell: false,
  canAlign: false,
})

const position = reactive({ top: 0, left: 0, width: 0 })
const toolbarStyle = computed(() => ({
  top: `${position.top}px`,
  left: `${position.left}px`,
  width: position.width ? `${position.width}px` : 'auto',
}))

const TABLE_OFFSET = 12
let editorCleanup: Array<() => void> = []

const detachEditorListeners = () => {
  editorCleanup.forEach((dispose) => {
    if (typeof dispose === 'function') {
      dispose()
    }
  })
  editorCleanup = []
}

const getActiveTableElement = (): HTMLElement | null => {
  const editor = props.editor
  if (!editor) {
    return null
  }

  const { state, view } = editor
  const { selection } = state

  if (selection instanceof NodeSelection && selection.node.type.name === 'table') {
    const dom = view.nodeDOM(selection.from)
    if (dom instanceof HTMLElement) {
      return dom
    }
  }

  const $pos = selection.$from
  const node = view.domAtPos($pos.pos)?.node as Node | null
  if (!node) {
    return null
  }

  if (node.nodeType === Node.ELEMENT_NODE) {
    return (node as HTMLElement).closest('table')
  }

  if (node.parentElement) {
    return node.parentElement.closest('table')
  }

  return null
}

const updateCapabilities = () => {
  const editor = props.editor
  if (!editor) {
    capabilities.canDeleteColumn = false
    capabilities.canDeleteRow = false
    capabilities.canMergeCells = false
    capabilities.canSplitCell = false
    capabilities.canAlign = false
    return
  }

  const can = editor.can()
  capabilities.canDeleteColumn = can.deleteColumn?.() ?? false
  capabilities.canDeleteRow = can.deleteRow?.() ?? false
  capabilities.canMergeCells = can.mergeCells?.() ?? false
  capabilities.canSplitCell = can.splitCell?.() ?? false
  capabilities.canAlign = can.setCellAttribute?.('textAlign', 'left') ?? false
}

const updateAlignment = () => {
  const editor = props.editor
  if (!editor) {
    alignment.value = 'left'
    return
  }

  const cellAlign = editor.getAttributes('tableCell')?.textAlign as Alignment | undefined
  const headerAlign = editor.getAttributes('tableHeader')?.textAlign as Alignment | undefined
  alignment.value = cellAlign || headerAlign || 'left'
}

const updateToolbarState = () => {
  const editor = props.editor
  if (!editor) {
    visible.value = false
    return
  }

  if (!editor.isActive('table')) {
    visible.value = false
    return
  }

  const table = getActiveTableElement()
  if (!table) {
    visible.value = false
    return
  }

  const rect = table.getBoundingClientRect()
  position.top = rect.bottom + window.scrollY + TABLE_OFFSET
  position.left = rect.left + window.scrollX
  position.width = rect.width

  updateCapabilities()
  updateAlignment()
  visible.value = true
}

const handleEditorBlur = () => {
  visible.value = false
}

const attachEditorListeners = (editor: Editor | null) => {
  detachEditorListeners()
  if (!editor) {
    return
  }

  const bind = <T extends Parameters<Editor['on']>[0]>(
    event: T,
    handler: Parameters<Editor['on']>[1]
  ) => {
    editor.on(event, handler)
    editorCleanup.push(() => editor.off(event, handler))
  }

  bind('selectionUpdate', updateToolbarState)
  bind('transaction', updateToolbarState)
  bind('focus', updateToolbarState)
  bind('blur', handleEditorBlur)

  updateToolbarState()
}

const handleWindowChange = () => {
  if (visible.value) {
    updateToolbarState()
  }
}

watch(
  () => props.editor,
  (next) => {
    attachEditorListeners(next)
  },
  { immediate: true }
)

onMounted(() => {
  window.addEventListener('scroll', handleWindowChange, true)
  window.addEventListener('resize', handleWindowChange)
})

onBeforeUnmount(() => {
  detachEditorListeners()
  window.removeEventListener('scroll', handleWindowChange, true)
  window.removeEventListener('resize', handleWindowChange)
})

const editorChain = () => props.editor?.chain().focus()

const addColumnBefore = () => editorChain()?.addColumnBefore().run()
const addColumnAfter = () => editorChain()?.addColumnAfter().run()
const deleteColumn = () => editorChain()?.deleteColumn().run()

const addRowBefore = () => editorChain()?.addRowBefore().run()
const addRowAfter = () => editorChain()?.addRowAfter().run()
const deleteRow = () => editorChain()?.deleteRow().run()

const mergeCells = () => editorChain()?.mergeCells().run()
const splitCell = () => editorChain()?.splitCell().run()
const deleteTable = () => editorChain()?.deleteTable().run()

const toggleHeaderRow = () => editorChain()?.toggleHeaderRow().run()
const toggleHeaderColumn = () => editorChain()?.toggleHeaderColumn().run()

const hasHeaderRow = computed(() => {
  if (!props.editor) return false
  if (!props.editor.isActive('table')) return false
  return Boolean(props.editor.getAttributes('table')?.hasHeaderRow)
})

const hasHeaderColumn = computed(() => {
  if (!props.editor) return false
  if (!props.editor.isActive('table')) return false
  return Boolean(props.editor.getAttributes('table')?.hasHeaderColumn)
})

const setAlignment = (value: Alignment) => {
  editorChain()?.setCellAttribute('textAlign', value).run()
  alignment.value = value
  updateToolbarState()
}
</script>

<style scoped>
.table-toolbar-fade-enter-active,
.table-toolbar-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.table-toolbar-fade-enter-from,
.table-toolbar-fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.table-floating-toolbar {
  position: absolute;
  z-index: 1200;
  display: flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.92);
  box-shadow: 0 18px 35px -20px rgba(15, 23, 42, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.35);
  color: #e2e8f0;
  backdrop-filter: blur(12px);
  gap: 12px;
}

.table-floating-toolbar__section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-floating-toolbar__label {
  font-size: 12px;
  color: rgba(248, 250, 252, 0.7);
  letter-spacing: 0.2px;
}

.table-floating-toolbar__divider {
  width: 1px;
  height: 28px;
  background: rgba(226, 232, 240, 0.2);
}

:deep(.el-button-group) > .el-button {
  padding: 0 10px;
}

:deep(.el-button-group) > .el-button:not(.is-disabled) {
  background: transparent;
  color: #cbd5f5;
}

:deep(.el-button-group) > .el-button.is-primary {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.35);
  color: #93c5fd;
}

:deep(.el-button-group) > .el-button.is-danger {
  color: #f87171;
}

:deep(.el-popconfirm__reference) {
  width: auto;
}
</style>

