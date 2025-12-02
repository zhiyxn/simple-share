<template>
  <div
    class="toolbox-menu"
    :style="{ left: `${position.x}px`, top: `${position.y}px` }"
    @click.stop
  >
    <div class="toolbox-search">
      <el-input
        v-model="searchQuery"
        size="small"
        placeholder="搜索或输入命令..."
        prefix-icon="Search"
        @input="handleSearch"
        @keydown.enter="handleSelectItem(filteredItems[selectedIndex])"
        @keydown.up.prevent="handleKeyUp"
        @keydown.down.prevent="handleKeyDown"
        @keydown.esc="$emit('close')"
      />
    </div>

    <div class="toolbox-items">
      <div
        v-for="(item, index) in filteredItems"
        :key="item.key"
        :class="[
          'toolbox-item',
          { 'selected': index === selectedIndex }
        ]"
        @click="handleSelectItem(item)"
        @mouseenter="selectedIndex = index"
      >
        <div class="item-icon">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="item-content">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-description">{{ item.description }}</div>
        </div>
        <div v-if="item.shortcut" class="item-shortcut">
          {{ item.shortcut }}
        </div>
      </div>
    </div>

    <div v-if="filteredItems.length === 0" class="toolbox-empty">
      <div class="empty-icon">
        <el-icon><Search /></el-icon>
      </div>
      <div class="empty-text">未找到匹配的命令</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import type { Editor } from '@tiptap/vue-3'
import { ElInput, ElIcon, ElMessageBox } from 'element-plus'
import {
  Document,
  Edit,
  Picture,
  VideoCamera,
  Microphone,
  Grid,
  List,
  Sort,
  ChatRound,
  Document as DocumentCode,
  Link,
  Monitor,
  Folder,
  Brush,
  Search,
} from '@element-plus/icons-vue'

interface ToolboxItem {
  key: string
  title: string
  description: string
  icon: any
  shortcut?: string
  keywords: string[]
  action: () => void
}

interface Props {
  editor: Editor | null
  position: { x: number; y: number }
}

interface Emits {
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const searchQuery = ref('')
const selectedIndex = ref(0)

const isValidImageUrl = (url: string) => {
  if (!url) {
    return false
  }

  const trimmed = url.trim()
  if (!trimmed) {
    return false
  }

  if (/^(https?:)?\/\//i.test(trimmed)) {
    return true
  }

  if (trimmed.startsWith('/') || /^data:image\//i.test(trimmed) || /^blob:/i.test(trimmed)) {
    return true
  }

  try {
    new URL(trimmed, window.location.origin)
    return true
  } catch (error) {
    return false
  }
}

const insertImageByUrl = (url: string, alt?: string) => {
  const targetUrl = url.trim()
  if (!targetUrl) {
    return
  }

  props.editor?.chain().focus().setImage({ src: targetUrl, alt: alt?.trim() }).run()
}

const openLocalImagePicker = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      const url = URL.createObjectURL(file)
      insertImageByUrl(url, file.name)
    }
  }
  input.click()
}

const handleImageCommand = () => {
  ElMessageBox.prompt('请输入图片链接，或点击“上传图片”选择本地文件。', '插入图片', {
    confirmButtonText: '插入链接',
    cancelButtonText: '上传图片',
    showCancelButton: true,
    distinguishCancelAndClose: true,
    inputPlaceholder: 'https://example.com/image.png',
    inputValidator: (value) => {
      if (!value?.trim()) {
        return '请输入图片链接'
      }

      return isValidImageUrl(value.trim()) ? true : '请输入有效的图片链接'
    },
  })
    .then(({ value }) => {
      insertImageByUrl(value)
    })
    .catch((action: string) => {
      if (action === 'cancel') {
        openLocalImagePicker()
      }
    })
}

// 工具箱项目
const toolboxItems = computed<ToolboxItem[]>(() => [
  {
    key: 'heading1',
    title: '标题 1',
    description: '大标题',
    icon: Document,
    shortcut: 'Ctrl+Alt+1',
    keywords: ['标题', '大标题', 'h1', 'heading'],
    action: () => props.editor?.chain().focus().toggleHeading({ level: 1 }).run(),
  },
  {
    key: 'heading2',
    title: '标题 2',
    description: '中标题',
    icon: Document,
    shortcut: 'Ctrl+Alt+2',
    keywords: ['标题', '中标题', 'h2', 'heading'],
    action: () => props.editor?.chain().focus().toggleHeading({ level: 2 }).run(),
  },
  {
    key: 'heading3',
    title: '标题 3',
    description: '小标题',
    icon: Document,
    shortcut: 'Ctrl+Alt+3',
    keywords: ['标题', '小标题', 'h3', 'heading'],
    action: () => props.editor?.chain().focus().toggleHeading({ level: 3 }).run(),
  },
  {
    key: 'paragraph',
    title: '正文',
    description: '普通文本段落',
    icon: Edit,
    keywords: ['正文', '段落', 'paragraph', 'text'],
    action: () => props.editor?.chain().focus().setParagraph().run(),
  },
  {
    key: 'bulletList',
    title: '无序列表',
    description: '创建无序列表',
    icon: List,
    shortcut: 'Ctrl+Shift+8',
    keywords: ['列表', '无序列表', 'list', 'bullet'],
    action: () => props.editor?.chain().focus().toggleBulletList().run(),
  },
  {
    key: 'orderedList',
    title: '有序列表',
    description: '创建有序列表',
    icon: Sort,
    shortcut: 'Ctrl+Shift+7',
    keywords: ['列表', '有序列表', 'list', 'ordered', 'number'],
    action: () => props.editor?.chain().focus().toggleOrderedList().run(),
  },
  {
    key: 'blockquote',
    title: '引用',
    description: '创建引用块',
    icon: ChatRound,
    shortcut: 'Ctrl+Shift+B',
    keywords: ['引用', 'quote', 'blockquote'],
    action: () => props.editor?.chain().focus().toggleBlockquote().run(),
  },
  {
    key: 'codeBlock',
    title: '代码块',
    description: '插入代码块',
    icon: DocumentCode,
    shortcut: 'Ctrl+Alt+C',
    keywords: ['代码', '代码块', 'code', 'codeblock'],
    action: () => props.editor?.chain().focus().toggleCodeBlock().run(),
  },
  {
    key: 'table',
    title: '表格',
    description: '插入表格',
    icon: Grid,
    keywords: ['表格', 'table'],
    action: () => props.editor?.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run(),
  },
  {
    key: 'image',
    title: '图片',
    description: '上传或插入图片',
    icon: Picture,
    keywords: ['图片', '图像', 'image', 'picture'],
    action: () => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'image/*'
      input.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setImage({ src: url }).run()
        }
      }
      input.click()
    },
  },
  {
    key: 'video',
    title: '视频',
    description: '上传或插入视频',
    icon: VideoCamera,
    keywords: ['视频', 'video'],
    action: () => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'video/*'
      input.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setVideo({ src: url }).run()
        }
      }
      input.click()
    },
  },
  {
    key: 'audio',
    title: '音频',
    description: '上传或插入音频',
    icon: Microphone,
    keywords: ['音频', 'audio'],
    action: () => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = 'audio/*'
      input.onchange = (e) => {
        const file = (e.target as HTMLInputElement).files?.[0]
        if (file) {
          const url = URL.createObjectURL(file)
          props.editor?.chain().focus().setAudio({ src: url }).run()
        }
      }
      input.click()
    },
  },
  {
    key: 'link',
    title: '链接',
    description: '插入链接',
    icon: Link,
    shortcut: 'Ctrl+K',
    keywords: ['链接', 'link', 'url'],
    action: () => {
      const url = prompt('请输入链接地址:')
      if (url) {
        props.editor?.chain().focus().setLink({ href: url }).run()
      }
    },
  },
  {
    key: 'iframe',
    title: '嵌入',
    description: '嵌入外部内容',
    icon: Monitor,
    keywords: ['嵌入', 'iframe', 'embed'],
    action: () => {
      const url = prompt('请输入嵌入地址:')
      if (url) {
        props.editor?.chain().focus().setIframe({ src: url }).run()
      }
    },
  },
  {
    key: 'details',
    title: '折叠块',
    description: '创建可折叠的内容块',
    icon: Folder,
    keywords: ['折叠', '折叠块', 'details', 'collapse'],
    action: () => props.editor?.chain().focus().setDetails().run(),
  },
  {
    key: 'columns',
    title: '分栏',
    description: '创建多栏布局',
    icon: Grid,
    keywords: ['分栏', '多栏', 'columns', 'layout'],
    action: () => props.editor?.chain().focus().setColumns().run(),
  },
  {
    key: 'html',
    title: 'HTML',
    description: '插入HTML代码块',
    icon: DocumentCode,
    keywords: ['html', 'HTML', '代码'],
    action: () => props.editor?.chain().focus().setHtmlBlock().run(),
  },
  {
    key: 'markdown',
    title: 'Markdown',
    description: '插入Markdown代码块',
    icon: Document,
    keywords: ['markdown', 'Markdown', 'md'],
    action: () => props.editor?.chain().focus().setMarkdownBlock().run(),
  },
  {
    key: 'highlight',
    title: '高亮',
    description: '高亮选中文本',
    icon: Brush,
    shortcut: 'Ctrl+Shift+H',
    keywords: ['高亮', 'highlight', '标记'],
    action: () => props.editor?.chain().focus().toggleHighlight().run(),
  },
])

// 过滤后的项目
const filteredItems = computed(() => {
  if (!searchQuery.value) {
    return toolboxItems.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return toolboxItems.value.filter(item => {
    return item.title.toLowerCase().includes(query) ||
           item.description.toLowerCase().includes(query) ||
           item.keywords.some(keyword => keyword.toLowerCase().includes(query))
  })
})

// 处理搜索
const handleSearch = () => {
  selectedIndex.value = 0
}

// 处理键盘导航
const handleKeyUp = () => {
  if (selectedIndex.value > 0) {
    selectedIndex.value--
  }
}

const handleKeyDown = () => {
  if (selectedIndex.value < filteredItems.value.length - 1) {
    selectedIndex.value++
  }
}

// 处理选择项目
const handleSelectItem = (item: ToolboxItem) => {
  if (!item) {
    return
  }

  if (item.key === 'image') {
    handleImageCommand()
  } else {
    item.action()
  }

  emit('close')
}

// 处理点击外部关闭
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as Element
  if (!target.closest('.toolbox-menu')) {
    emit('close')
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.toolbox-menu {
  @apply fixed z-50 bg-white rounded-lg shadow-lg border border-gray-200 w-80 max-h-96 overflow-hidden;
}

.toolbox-search {
  @apply p-3 border-b border-gray-100;
}

.toolbox-items {
  @apply max-h-80 overflow-y-auto;
}

.toolbox-item {
  @apply flex items-center gap-3 px-3 py-2 cursor-pointer hover:bg-gray-50 transition-colors;
}

.toolbox-item.selected {
  @apply bg-blue-50 text-blue-600;
}

.item-icon {
  @apply w-8 h-8 flex items-center justify-center bg-gray-100 rounded;
}

.toolbox-item.selected .item-icon {
  @apply bg-blue-100;
}

.item-content {
  @apply flex-1 min-w-0;
}

.item-title {
  @apply font-medium text-sm;
}

.item-description {
  @apply text-xs text-gray-500 truncate;
}

.item-shortcut {
  @apply text-xs text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded;
}

.toolbox-item.selected .item-shortcut {
  @apply bg-blue-100 text-blue-500;
}

.toolbox-empty {
  @apply flex flex-col items-center justify-center py-8 text-gray-500;
}

.empty-icon {
  @apply w-12 h-12 flex items-center justify-center bg-gray-100 rounded-full mb-2;
}

.empty-text {
  @apply text-sm;
}
</style>
