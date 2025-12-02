<template>
  <NodeViewWrapper class="iframe-wrapper">
    <div class="iframe-header">
      <span class="iframe-title">嵌入网页</span>
      <div class="iframe-actions">
        <el-button text size="small" @click="toggleEdit">
          <el-icon>
            <Edit v-if="!isEditing" />
            <Check v-else />
          </el-icon>
          {{ isEditing ? '完成' : '编辑' }}
        </el-button>
        <el-button text size="small" @click="openInNewTab" :disabled="!iframeSrc">
          <el-icon><Link /></el-icon>
          新窗口打开
        </el-button>
      </div>
    </div>

    <div v-if="isEditing" class="iframe-editor">
      <div class="form-group">
        <label>网页地址 (URL):</label>
        <el-input
          v-model="iframeSrc"
          placeholder="请输入网页地址，如: https://example.com"
          @blur="updateAttributes"
        />
      </div>
      
      <div class="form-row">
        <div class="form-group">
          <label>宽度:</label>
          <el-input
            v-model="iframeWidth"
            placeholder="100%"
            @blur="updateAttributes"
          />
        </div>
        
        <div class="form-group">
          <label>高度:</label>
          <el-input
            v-model="iframeHeight"
            placeholder="400px"
            @blur="updateAttributes"
          />
        </div>
      </div>
      
      <div class="form-group">
        <label>标题 (可选):</label>
        <el-input
          v-model="iframeTitle"
          placeholder="网页标题"
          @blur="updateAttributes"
        />
      </div>
    </div>

    <div v-else class="iframe-preview">
      <div v-if="iframeSrc" class="iframe-container">
        <iframe
          :src="iframeSrc"
          :width="iframeWidth"
          :height="iframeHeight"
          :title="iframeTitle"
          frameborder="0"
          allowfullscreen
          class="iframe-content"
        ></iframe>
      </div>
      <div v-else class="iframe-placeholder">
        <el-icon class="placeholder-icon"><Link /></el-icon>
        <p>点击编辑按钮添加网页地址</p>
      </div>
    </div>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElButton, ElIcon, ElInput, ElMessage } from 'element-plus'
import { Edit, Check, Link } from '@element-plus/icons-vue'
import { NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'

const props = defineProps<NodeViewProps>()

const isEditing = ref(false)
const iframeSrc = ref(props.node?.attrs.src || '')
const iframeWidth = ref(props.node?.attrs.width || '100%')
const iframeHeight = ref(props.node?.attrs.height || '400px')
const iframeTitle = ref(props.node?.attrs.title || '')

const toggleEdit = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value) {
    updateAttributes()
  }
}

const updateAttributes = () => {
  props.updateAttributes({
    src: iframeSrc.value,
    width: iframeWidth.value,
    height: iframeHeight.value,
    title: iframeTitle.value,
  })
}

const openInNewTab = () => {
  if (iframeSrc.value) {
    window.open(iframeSrc.value, '_blank')
  }
}
</script>

<style scoped>
.iframe-wrapper {
  @apply border border-gray-200 rounded-lg my-4 overflow-hidden;
}

.iframe-header {
  @apply flex items-center justify-between p-3 bg-gray-50 border-b border-gray-200;
}

.iframe-title {
  @apply font-medium text-gray-900;
}

.iframe-actions {
  @apply flex items-center space-x-2;
}

.iframe-editor {
  @apply p-4 space-y-4;
}

.form-group {
  @apply space-y-2;
}

.form-row {
  @apply grid grid-cols-2 gap-4;
}

.form-group label {
  @apply block text-sm font-medium text-gray-700;
}

.iframe-preview {
  @apply min-h-[200px];
}

.iframe-container {
  @apply w-full;
}

.iframe-content {
  @apply w-full border-0;
}

.iframe-placeholder {
  @apply flex flex-col items-center justify-center py-16 text-gray-500;
}

.placeholder-icon {
  @apply text-4xl mb-4;
}
</style>