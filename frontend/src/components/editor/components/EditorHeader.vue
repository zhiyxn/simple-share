<template>
  <div class="editor-header">
    <div class="header-left">
      <el-button 
        text 
        @click="$emit('back')"
        :icon="ArrowLeft"
        class="back-btn"
      >
        返回
      </el-button>
      
      <div class="title-input">
        <el-input
          v-model="localTitle"
          placeholder="请输入文章标题..."
          size="large"
          class="title-field"
          @input="handleTitleChange"
          @blur="handleTitleBlur"
        />
      </div>
    </div>

    <div class="header-center">
      <div class="save-status">
        <el-icon v-if="saveStatus === 'saving'" class="is-loading">
          <Loading />
        </el-icon>
        <el-icon v-else-if="saveStatus === 'saved'" class="saved-icon">
          <Check />
        </el-icon>
        <el-icon v-else-if="saveStatus === 'error'" class="error-icon">
          <Close />
        </el-icon>
        <span class="status-text">{{ saveStatusText }}</span>
      </div>
    </div>

    <div class="header-right">
      <el-button-group>
        <el-button 
          :type="viewMode === 'edit' ? 'primary' : ''"
          @click="$emit('changeViewMode', 'edit')"
          size="small"
        >
          编辑
        </el-button>
        <el-button 
          :type="viewMode === 'preview' ? 'primary' : ''"
          @click="$emit('changeViewMode', 'preview')"
          size="small"
        >
          预览
        </el-button>
        <el-button 
          :type="viewMode === 'split' ? 'primary' : ''"
          @click="$emit('changeViewMode', 'split')"
          size="small"
        >
          分屏
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button 
        @click="$emit('save')"
        :loading="saveStatus === 'saving'"
        :disabled="!hasChanges"
        size="small"
      >
        保存
      </el-button>

      <el-dropdown @command="handleCommand">
        <el-button type="primary" size="small">
          发布
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="publish">立即发布</el-dropdown-item>
            <el-dropdown-item command="schedule">定时发布</el-dropdown-item>
            <el-dropdown-item command="draft">保存为草稿</el-dropdown-item>
            <el-dropdown-item divided command="settings">发布设置</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-button 
        text 
        @click="$emit('toggleSidebar')"
        :icon="sidebarVisible ? 'ep-arrow-right' : 'ep-arrow-left'"
        size="small"
        class="sidebar-toggle"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowLeft, ArrowDown, Loading, Check, Close } from '@element-plus/icons-vue'

interface Props {
  title: string
  saveStatus: 'idle' | 'saving' | 'saved' | 'error'
  hasChanges: boolean
  viewMode: 'edit' | 'preview' | 'split'
  sidebarVisible: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  back: []
  titleChange: [title: string]
  save: []
  publish: [type: 'publish' | 'schedule' | 'draft']
  settings: []
  changeViewMode: [mode: 'edit' | 'preview' | 'split']
  toggleSidebar: []
}>()

const localTitle = ref(props.title)

// 计算属性
const saveStatusText = computed(() => {
  switch (props.saveStatus) {
    case 'saving':
      return '保存中...'
    case 'saved':
      return '已保存'
    case 'error':
      return '保存失败'
    default:
      return props.hasChanges ? '有未保存的更改' : '已保存'
  }
})

// 监听标题变化
watch(() => props.title, (newTitle) => {
  localTitle.value = newTitle
})

// 方法
const handleTitleChange = () => {
  emit('titleChange', localTitle.value)
}

const handleTitleBlur = () => {
  if (!localTitle.value.trim()) {
    localTitle.value = '无标题文档'
    emit('titleChange', localTitle.value)
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'publish':
    case 'schedule':
    case 'draft':
      emit('publish', command as 'publish' | 'schedule' | 'draft')
      break
    case 'settings':
      emit('settings')
      break
  }
}
</script>

<style scoped>
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.back-btn {
  margin-right: 12px;
  flex-shrink: 0;
}

.title-input {
  flex: 1;
  max-width: 400px;
}

.title-field {
  --el-input-border-color: transparent;
  --el-input-hover-border-color: var(--el-border-color);
  --el-input-focus-border-color: var(--el-color-primary);
}

.title-field :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  padding: 8px 12px;
}

.title-field :deep(.el-input__inner) {
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.header-center {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.save-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.saved-icon {
  color: var(--el-color-success);
}

.error-icon {
  color: var(--el-color-danger);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: flex-end;
}

.sidebar-toggle {
  margin-left: 8px;
}

.el-divider--vertical {
  height: 20px;
  margin: 0 8px;
}
</style>