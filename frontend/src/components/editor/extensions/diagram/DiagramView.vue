<template>
  <InlineBlockBox>
    <div
      class="diagram-wrapper"
      :class="{
        'ring-2 ring-blue-500': selected,
      }"
    >
      <!-- 头部工具栏 -->
      <div class="diagram-header">
        <div class="header-left">
          <el-icon class="diagram-icon"><TrendCharts /></el-icon>
          <span class="diagram-title">文本绘图 (Mermaid)</span>
        </div>
        <div class="header-right">
          <el-button
            text
            size="small"
            class="action-btn"
            @click="toggleEdit"
          >
            <el-icon><Edit v-if="!isEditing" /><View v-else /></el-icon>
          </el-button>
          <el-button
            text
            size="small"
            class="action-btn"
            @click="copyCode"
          >
            <el-icon><CopyDocument /></el-icon>
          </el-button>
          <el-button
            text
            size="small"
            class="action-btn"
            @click="deleteNode"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 编辑模式 -->
      <div v-if="isEditing" class="diagram-editor">
        <el-input
          v-model="diagramContent"
          type="textarea"
          :rows="8"
          placeholder="请输入Mermaid图表代码..."
          @blur="updateContent"
        />
        <div class="editor-help">
          <p class="help-text">支持的图表类型：</p>
          <ul class="help-list">
            <li>流程图: graph TD</li>
            <li>时序图: sequenceDiagram</li>
            <li>甘特图: gantt</li>
            <li>饼图: pie</li>
          </ul>
        </div>
      </div>

      <!-- 预览模式 -->
      <div v-else class="diagram-preview">
        <div v-if="diagramContent" ref="diagramContainer" class="diagram-content"></div>
        <div v-else class="diagram-placeholder">
          <el-icon class="placeholder-icon"><TrendCharts /></el-icon>
          <p>点击编辑按钮添加图表代码</p>
        </div>
      </div>
    </div>
  </InlineBlockBox>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, onMounted, watch } from 'vue'
import { ElIcon, ElButton, ElInput, ElMessage } from 'element-plus'
import { TrendCharts, Edit, View, CopyDocument, Delete } from '@element-plus/icons-vue'
import type { NodeViewProps } from '@tiptap/vue-3'
import InlineBlockBox from '../../components/InlineBlockBox.vue'

const props = defineProps<NodeViewProps>()

const isEditing = ref(false)
const diagramContainer = ref<HTMLElement>()
const diagramContent = ref(props.node?.attrs.content || '')

// 简单的Mermaid渲染（实际项目中应该使用mermaid库）
const renderDiagram = async () => {
  if (!diagramContainer.value || !diagramContent.value) return
  
  try {
    // 这里应该使用真正的mermaid库来渲染
    // 为了演示，我们创建一个简单的SVG占位符
    const svg = createSimpleDiagram(diagramContent.value)
    diagramContainer.value.innerHTML = svg
  } catch (error) {
    console.error('渲染图表失败:', error)
    diagramContainer.value.innerHTML = '<div class="error-message">图表渲染失败，请检查语法</div>'
  }
}

// 创建简单的图表占位符
const createSimpleDiagram = (content: string): string => {
  const lines = content.split('\n').filter(line => line.trim())
  const isFlowchart = content.includes('graph') || content.includes('flowchart')
  const isSequence = content.includes('sequenceDiagram')
  const isPie = content.includes('pie')
  
  if (isFlowchart) {
    return `
      <svg width="300" height="200" viewBox="0 0 300 200" xmlns="http://www.w3.org/2000/svg">
        <rect x="50" y="30" width="80" height="40" rx="5" fill="#e3f2fd" stroke="#1976d2" stroke-width="2"/>
        <text x="90" y="55" text-anchor="middle" font-size="12" fill="#1976d2">开始</text>
        
        <rect x="50" y="100" width="80" height="40" rx="5" fill="#f3e5f5" stroke="#7b1fa2" stroke-width="2"/>
        <text x="90" y="125" text-anchor="middle" font-size="12" fill="#7b1fa2">处理</text>
        
        <rect x="170" y="100" width="80" height="40" rx="5" fill="#e8f5e8" stroke="#388e3c" stroke-width="2"/>
        <text x="210" y="125" text-anchor="middle" font-size="12" fill="#388e3c">结束</text>
        
        <path d="M90 70 L90 100" stroke="#666" stroke-width="2" marker-end="url(#arrowhead)"/>
        <path d="M130 120 L170 120" stroke="#666" stroke-width="2" marker-end="url(#arrowhead)"/>
        
        <defs>
          <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill="#666"/>
          </marker>
        </defs>
      </svg>
    `
  } else if (isSequence) {
    return `
      <svg width="300" height="200" viewBox="0 0 300 200" xmlns="http://www.w3.org/2000/svg">
        <line x1="60" y1="30" x2="60" y2="170" stroke="#666" stroke-width="2"/>
        <line x1="240" y1="30" x2="240" y2="170" stroke="#666" stroke-width="2"/>
        
        <rect x="20" y="10" width="80" height="30" fill="#e3f2fd" stroke="#1976d2"/>
        <text x="60" y="30" text-anchor="middle" font-size="12" fill="#1976d2">用户</text>
        
        <rect x="200" y="10" width="80" height="30" fill="#f3e5f5" stroke="#7b1fa2"/>
        <text x="240" y="30" text-anchor="middle" font-size="12" fill="#7b1fa2">系统</text>
        
        <path d="M60 60 L240 60" stroke="#666" stroke-width="2" marker-end="url(#arrowhead)"/>
        <text x="150" y="55" text-anchor="middle" font-size="10" fill="#666">请求</text>
        
        <path d="M240 90 L60 90" stroke="#666" stroke-width="2" marker-end="url(#arrowhead)"/>
        <text x="150" y="85" text-anchor="middle" font-size="10" fill="#666">响应</text>
        
        <defs>
          <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill="#666"/>
          </marker>
        </defs>
      </svg>
    `
  } else if (isPie) {
    return `
      <svg width="300" height="200" viewBox="0 0 300 200" xmlns="http://www.w3.org/2000/svg">
        <circle cx="150" cy="100" r="80" fill="#e3f2fd" stroke="#1976d2" stroke-width="2"/>
        <path d="M150 100 L150 20 A80 80 0 0 1 230 100 Z" fill="#f3e5f5" stroke="#7b1fa2" stroke-width="2"/>
        <path d="M150 100 L230 100 A80 80 0 0 1 150 180 Z" fill="#e8f5e8" stroke="#388e3c" stroke-width="2"/>
        <text x="150" y="105" text-anchor="middle" font-size="12" fill="#333">饼图示例</text>
      </svg>
    `
  }
  
  return `
    <svg width="300" height="200" viewBox="0 0 300 200" xmlns="http://www.w3.org/2000/svg">
      <rect x="10" y="10" width="280" height="180" fill="#f5f5f5" stroke="#ddd" stroke-width="2" rx="5"/>
      <text x="150" y="100" text-anchor="middle" font-size="14" fill="#666">图表预览</text>
      <text x="150" y="120" text-anchor="middle" font-size="12" fill="#999">请输入有效的Mermaid代码</text>
    </svg>
  `
}

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value) {
    updateContent()
  }
}

const updateContent = () => {
  props.updateAttributes({ content: diagramContent.value })
}

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(diagramContent.value)
    ElMessage.success('图表代码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 监听内容变化，重新渲染图表
watch(() => props.node?.attrs.content, (newContent) => {
  diagramContent.value = newContent || ''
  if (!isEditing.value) {
    nextTick(() => {
      renderDiagram()
    })
  }
}, { immediate: true })

// 监听编辑状态变化
watch(isEditing, (editing) => {
  if (!editing) {
    nextTick(() => {
      renderDiagram()
    })
  }
})

onMounted(() => {
  if (!isEditing.value) {
    renderDiagram()
  }
})
</script>

<style scoped>
.diagram-wrapper {
  @apply border border-gray-200 rounded-lg my-4 overflow-hidden;
}

.diagram-header {
  @apply flex items-center justify-between p-3 bg-gray-50 border-b border-gray-200;
}

.header-left {
  @apply flex items-center space-x-2;
}

.diagram-icon {
  @apply text-blue-600;
}

.diagram-title {
  @apply font-medium text-gray-900;
}

.header-right {
  @apply flex items-center space-x-1;
}

.action-btn {
  @apply !p-1.5 hover:bg-gray-200 rounded transition-colors;
}

.diagram-editor {
  @apply p-4 space-y-3;
}

.editor-help {
  @apply mt-3 p-3 bg-blue-50 rounded-lg;
}

.help-text {
  @apply text-sm font-medium text-blue-800 mb-2;
}

.help-list {
  @apply text-xs text-blue-600 space-y-1;
}

.help-list li {
  @apply list-disc list-inside;
}

.diagram-preview {
  @apply p-4 min-h-[200px] flex items-center justify-center;
}

.diagram-content {
  @apply w-full flex justify-center;
}

.diagram-content :deep(svg) {
  @apply max-w-full h-auto;
}

.diagram-placeholder {
  @apply text-gray-500 text-center;
}

.placeholder-icon {
  @apply text-4xl mb-2 text-gray-400;
}

.error-message {
  @apply text-red-600 text-center p-4 bg-red-50 rounded border border-red-200;
}
</style>