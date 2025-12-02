<template>
  <div class="editor-toolbar">
    <!-- 左侧工具组 -->
    <div class="toolbar-group">
      <!-- 撤销重做 -->
      <el-button-group>
        <el-button
          size="small"
          :disabled="!editor?.can().undo()"
          @click="editor?.chain().focus().undo().run()"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-button
          size="small"
          :disabled="!editor?.can().redo()"
          @click="editor?.chain().focus().redo().run()"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 标题级别 -->
      <el-select
        v-model="currentHeadingLevel"
        size="small"
        style="width: 120px"
        @change="handleHeadingChange"
      >
        <el-option label="正文" :value="0" />
        <el-option label="标题 1" :value="1" />
        <el-option label="标题 2" :value="2" />
        <el-option label="标题 3" :value="3" />
        <el-option label="标题 4" :value="4" />
        <el-option label="标题 5" :value="5" />
        <el-option label="标题 6" :value="6" />
      </el-select>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 文本格式 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive('bold') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleBold().run()"
        >
          <el-icon><Edit /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('italic') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleItalic().run()"
        >
          <el-icon><EditPen /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('underline') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleUnderline().run()"
        >
          <el-icon><Document /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('strike') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleStrike().run()"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 上标下标 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive('superscript') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleSuperscript().run()"
        >
          X²
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('subscript') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleSubscript().run()"
        >
          X₂
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 高亮和代码 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive('highlight') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleHighlight().run()"
        >
          <el-icon><Brush /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('code') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleCode().run()"
        >
          <el-icon><Code /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 对齐方式 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive({ textAlign: 'left' }) ? 'primary' : 'default'"
          @click="editor?.chain().focus().setTextAlign('left').run()"
        >
          <el-icon><Operation /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive({ textAlign: 'center' }) ? 'primary' : 'default'"
          @click="editor?.chain().focus().setTextAlign('center').run()"
        >
          <el-icon><Menu /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive({ textAlign: 'right' }) ? 'primary' : 'default'"
          @click="editor?.chain().focus().setTextAlign('right').run()"
        >
          <el-icon><Setting /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive({ textAlign: 'justify' }) ? 'primary' : 'default'"
          @click="editor?.chain().focus().setTextAlign('justify').run()"
        >
          <el-icon><More /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 列表 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive('bulletList') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleBulletList().run()"
        >
          <el-icon><List /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('orderedList') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleOrderedList().run()"
        >
          <el-icon><Sort /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 引用和代码块 -->
      <el-button-group>
        <el-button
          size="small"
          :type="editor?.isActive('blockquote') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleBlockquote().run()"
        >
          <el-icon><ChatRound /></el-icon>
        </el-button>
        <el-button
          size="small"
          :type="editor?.isActive('codeBlock') ? 'primary' : 'default'"
          @click="editor?.chain().focus().toggleCodeBlock().run()"
        >
          <el-icon><Document /></el-icon>
        </el-button>
      </el-button-group>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 插入元素 -->
      <el-dropdown @command="handleInsertCommand">
        <el-button size="small">
          插入
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="table">
              <el-icon><Grid /></el-icon>
              表格
            </el-dropdown-item>
            <el-dropdown-item command="image">
              <el-icon><Picture /></el-icon>
              图片
            </el-dropdown-item>
            <el-dropdown-item command="video">
              <el-icon><VideoCamera /></el-icon>
              视频
            </el-dropdown-item>
            <el-dropdown-item command="audio">
              <el-icon><Microphone /></el-icon>
              音频
            </el-dropdown-item>
            <el-dropdown-item command="link">
              <el-icon><Link /></el-icon>
              链接
            </el-dropdown-item>
            <el-dropdown-item command="iframe">
              <el-icon><Monitor /></el-icon>
              嵌入
            </el-dropdown-item>
            <el-dropdown-item command="details">
              <el-icon><Folder /></el-icon>
              折叠块
            </el-dropdown-item>
            <el-dropdown-item command="columns">
              <el-icon><Grid /></el-icon>
              分栏
            </el-dropdown-item>
            <el-dropdown-item command="html">
              <el-icon><Document /></el-icon>
              HTML
            </el-dropdown-item>
            <el-dropdown-item command="markdown">
              <el-icon><Document /></el-icon>
              Markdown
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 右侧工具组 -->
    <div class="toolbar-group">
      <!-- 全屏切换 -->
      <el-button
        size="small"
        @click="$emit('toggle-fullscreen')"
      >
        <el-icon><FullScreen /></el-icon>
      </el-button>

      <!-- 预览 -->
      <el-button
        size="small"
        @click="$emit('preview')"
      >
        <el-icon><View /></el-icon>
        预览
      </el-button>

      <!-- 保存 -->
      <el-button
        size="small"
        type="primary"
        @click="$emit('save')"
      >
        <el-icon><Check /></el-icon>
        保存
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import {
  ElButton,
  ElButtonGroup,
  ElSelect,
  ElOption,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElIcon,
} from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Edit,
  EditPen,
  Document,
  Delete,
  Brush,
  Document as Code,
  Operation,
  Menu,
  Setting,
  More,
  List,
  Sort,
  ChatRound,
  ArrowDown,
  Grid,
  Picture,
  VideoCamera,
  Microphone,
  Link,
  Monitor,
  Folder,
  FullScreen,
  View,
  Check,
} from '@element-plus/icons-vue'

interface Props {
  editor: Editor | null
}

interface Emits {
  (e: 'toggle-fullscreen'): void
  (e: 'preview'): void
  (e: 'save'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 当前标题级别
const currentHeadingLevel = ref(0)

// 监听编辑器状态变化
watch(
  () => props.editor?.state,
  () => {
    if (!props.editor) return
    
    // 更新当前标题级别
    for (let level = 1; level <= 6; level++) {
      if (props.editor.isActive('heading', { level })) {
        currentHeadingLevel.value = level
        return
      }
    }
    currentHeadingLevel.value = 0
  },
  { deep: true }
)

// 处理标题级别变化
const handleHeadingChange = (level: number) => {
  if (!props.editor) return
  
  if (level === 0) {
    props.editor.chain().focus().setParagraph().run()
  } else {
    props.editor.chain().focus().toggleHeading({ level }).run()
  }
}

// 处理插入命令
const handleInsertCommand = (command: string) => {
  if (!props.editor) return
  
  switch (command) {
    case 'table':
      props.editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
      break
    case 'image':
      // 触发图片上传
      const imageInput = document.createElement('input')
      imageInput.type = 'file'
      imageInput.accept = 'image/*'
      imageInput.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          // 这里应该调用上传函数，暂时使用本地URL
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setImage({ src: url }).run()
        }
      }
      imageInput.click()
      break
    case 'video':
      // 触发视频上传
      const videoInput = document.createElement('input')
      videoInput.type = 'file'
      videoInput.accept = 'video/*'
      videoInput.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setVideo({ src: url }).run()
        }
      }
      videoInput.click()
      break
    case 'audio':
      // 触发音频上传
      const audioInput = document.createElement('input')
      audioInput.type = 'file'
      audioInput.accept = 'audio/*'
      audioInput.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setAudio({ src: url }).run()
        }
      }
      audioInput.click()
      break
    case 'link':
      const url = prompt('请输入链接地址:')
      if (url) {
        props.editor.chain().focus().setLink({ href: url }).run()
      }
      break
    case 'iframe':
      const iframeUrl = prompt('请输入嵌入地址:')
      if (iframeUrl) {
        props.editor.chain().focus().setIframe({ src: iframeUrl }).run()
      }
      break
    case 'details':
      props.editor.chain().focus().setDetails().run()
      break
    case 'columns':
      props.editor.chain().focus().setColumns().run()
      break
    case 'html':
      props.editor.chain().focus().setHtmlBlock().run()
      break
    case 'markdown':
      props.editor.chain().focus().setMarkdownBlock().run()
      break
  }
}
</script>

<style scoped>
.editor-toolbar {
  @apply flex items-center justify-between px-4 py-2 bg-white border-b border-gray-200;
}

.toolbar-group {
  @apply flex items-center gap-2;
}

.toolbar-divider {
  @apply w-px h-6 bg-gray-300;
}

:deep(.el-button-group .el-button) {
  @apply border-gray-300;
}

:deep(.el-button-group .el-button:hover) {
  @apply border-blue-400;
}

:deep(.el-button-group .el-button.el-button--primary) {
  @apply bg-blue-500 border-blue-500;
}
</style>