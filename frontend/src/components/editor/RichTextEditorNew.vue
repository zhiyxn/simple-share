<template>
  <div class="rich-text-editor-new">
    <!-- 编辑器主体 -->
    <div class="editor-body">
      <div class="editor-content markdown-surface">
        <EditorContent :editor="editor" />
        <!-- 气泡菜单 -->
        <EditorBubbleMenu v-if="editor" :editor="editor" />
        <TableFloatingToolbar v-if="editor" :editor="editor" />
      </div>

      <!-- 侧边栏 -->
      <div v-if="showSidebar" class="editor-sidebar">
        <el-tabs v-model="activeSidebarTab" type="border-card">
          <!-- 大纲标签页 -->
          <el-tab-pane label="大纲" name="toc">
            <div class="toc-content">
              <div v-if="headingNodes.length === 0" class="empty-state">
                暂无大纲
              </div>
              <ul v-else class="toc-list">
                <li
                  v-for="(node, index) in headingNodes"
                  :key="index"
                  :class="[
                    'toc-item',
                    { 'active': node.id === selectedHeadingNode?.id }
                  ]"
                  :style="{ paddingLeft: `${(node.level - 1) * 16}px` }"
                  @click="handleSelectHeadingNode(node)"
                >
                  <el-icon class="heading-icon">
                    <component :is="getHeadingIcon(node.level)" />
                  </el-icon>
                  <span class="heading-text">{{ node.text }}</span>
                </li>
              </ul>
            </div>
          </el-tab-pane>

          <!-- 详情标签页 -->
          <el-tab-pane label="详情" name="detail">
            <div class="detail-content">
              <!-- 字符统计 -->
              <div class="detail-item">
                <div class="detail-label">字符数</div>
                <div class="detail-value">{{ characterCount }}</div>
              </div>

              <!-- 词数统计 -->
              <div class="detail-item">
                <div class="detail-label">词数</div>
                <div class="detail-value">{{ wordCount }}</div>
              </div>

              <!-- 发布时间 -->
              <div v-if="publishTime" class="detail-item">
                <div class="detail-label">发布时间</div>
                <div class="detail-value">{{ formatDate(publishTime) }}</div>
              </div>

              <!-- 创建者 -->
              <div v-if="owner" class="detail-item">
                <div class="detail-label">创建者</div>
                <div class="detail-value">{{ owner }}</div>
              </div>

              <!-- 访问链接 -->
              <div v-if="permalink" class="detail-item">
                <div class="detail-label">访问链接</div>
                <div class="detail-value">
                  <a :href="permalink" target="_blank" class="permalink-link">
                    {{ permalink }}
                  </a>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import { ElTabs, ElTabPane, ElIcon } from 'element-plus'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import CharacterCount from '@tiptap/extension-character-count'
import Subscript from '@tiptap/extension-subscript'
import Superscript from '@tiptap/extension-superscript'
import Highlight from './extensions/highlight'

// 导入自定义扩展（包含气泡菜单功能）
import Bold from './extensions/bold'
import Italic from './extensions/italic'
import Underline from './extensions/underline'
import Link from './extensions/link'
import Heading from './extensions/heading'
import CommandsMenu from './extensions/commands-menu'
import BulletList from './extensions/bullet-list'
import OrderedList, { ListItem } from './extensions/ordered-list'
import CodeBlock from './extensions/code-block'
import Code from './extensions/code'
import Strike from './extensions/strike'
import Blockquote from './extensions/blockquote'
import Text from './extensions/text'
import Image from './extensions/image'
import Video from './extensions/video'
import Audio from './extensions/audio'
import Upload from './extensions/upload'
import tableExtensions from './extensions/table'
import TextAlign from './extensions/text-align'
import Color, { TextStyle } from './extensions/color'
import { Details, DetailsSummary, DetailsContent } from './extensions/details'
import HtmlBlock from './extensions/html-block'
import MarkdownBlock from './extensions/markdown-block'
import Diagram from './extensions/diagram'
import Iframe from './extensions/iframe'
import TaskList from './extensions/task-list'
import TaskItem from './extensions/task-item'
import SecureResource from './extensions/secure-resource'
import MemberContent from './extensions/member-content'
import TableFloatingToolbar from './components/TableFloatingToolbar.vue'
import MathBlock from './extensions/math-block'
import MathInline from './extensions/math-inline'

// 导入扩展工具
import { filterDuplicateExtensions } from './utils/extension-utils'

// 导入组件
import EditorBubbleMenu from './components/bubble/EditorBubbleMenu.vue'

// 图标组件
import { Document, List, Grid, More } from '@element-plus/icons-vue'
import { DOMParser as ProseMirrorDOMParser } from 'prosemirror-model'
import { NodeSelection } from 'prosemirror-state'
import type { ContentFormat } from './utils/markdown'
import {
  detectContentFormat,
  markdownToHtml,
  htmlToMarkdown,
  isMarkdownContent,
  createMarkdownContainer,
} from './utils/markdown'
import '@/styles/markdown.css'

interface HeadingNode {
  id: string
  level: number
  text: string
  pos: number
}

const FALLBACK_PLACEHOLDER =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300"><rect width="400" height="300" fill="%23f3f4f6"/><text x="50%" y="50%" fill="%239ca3af" font-size="20" font-family="Arial, sans-serif" dominant-baseline="middle" text-anchor="middle">图片预览不可用</text></svg>'

const extractPlainTextFromHtml = (html: string): string => {
  if (!html || typeof DOMParser === 'undefined') {
    return ''
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')
    return doc.body?.textContent?.trim() ?? ''
  } catch (error) {
    console.warn('Failed to parse clipboard HTML', error)
    return ''
  }
}

const sanitizePastedHTML = (html: string): string => {
  if (!html || typeof DOMParser === 'undefined') {
    return html
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')

    doc.querySelectorAll('img').forEach((img) => {
      const src = img.getAttribute('src') ?? ''

      if (!src || src.length > 1024 * 64) {
        img.setAttribute('src', FALLBACK_PLACEHOLDER)
      } else if (/^https?:\/\//i.test(src)) {
        if (/via\.placeholder\.com/i.test(src) || /placeholder/i.test(src)) {
          img.setAttribute('src', FALLBACK_PLACEHOLDER)
        }
        img.setAttribute('loading', 'lazy')
        img.setAttribute('referrerpolicy', 'no-referrer')
        img.style.maxWidth = '100%'
        img.style.height = 'auto'
      } else if (!src.startsWith('data:') && !src.startsWith('/')) {
        img.setAttribute('src', FALLBACK_PLACEHOLDER)
      }
    })

    doc.querySelectorAll('table').forEach((table) => {
      table.setAttribute('data-responsive-table', 'true')
    })

    return doc.body.innerHTML
  } catch (error) {
    console.warn('Failed to sanitize pasted HTML', error)
    return html
  }
}

interface Props {
  modelValue?: string
  title?: string
  placeholder?: string
  showSidebar?: boolean
  publishTime?: string
  owner?: string
  permalink?: string
  uploadImage?: (file: File) => Promise<{ url: string }>
  readonly?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'update:title', value: string): void
  (e: 'update', value: {
    content: string
    markdown: string
    format: ContentFormat
    title: string
  }): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  title: '',
  placeholder: '输入 / 以选择输入类型',
  showSidebar: true,
  readonly: false,
})

const emit = defineEmits<Emits>()

const initialFormat: ContentFormat = detectContentFormat(props.modelValue)
const contentFormat = ref<ContentFormat>(initialFormat)
const UPDATE_EMIT_DELAY = 200
let emitTimer: ReturnType<typeof setTimeout> | null = null
const pendingUpdate = ref<{ html: string; title: string } | null>(null)

const initialHtmlContent = computed(() => {
  if (!props.modelValue) {
    return ''
  }

  const html = contentFormat.value === 'markdown'
    ? markdownToHtml(props.modelValue)
    : props.modelValue

  return sanitizePastedHTML(html)
})

const lastSyncedHtml = ref(initialHtmlContent.value)
const lastEmittedModelValue = ref(props.modelValue ?? '')

// 响应式数据
const activeSidebarTab = ref('toc')
const selectedHeadingNode = ref<HeadingNode | null>(null)

// 编辑器实例
const editor = useEditor({
  content: initialHtmlContent.value,
  editable: !props.readonly,
  extensions: filterDuplicateExtensions([
    StarterKit.configure({
      // 禁用默认的扩展，使用自定义的
      bold: false,
      italic: false,
      strike: false,
      code: false,
      heading: false,
      bulletList: false,
      orderedList: false,
      listItem: false,
      codeBlock: false,
      blockquote: false,
      taskList: false,
      taskItem: false,
      // 禁用默认的文本和链接扩展，使用自定义的
      text: false,
      link: false,
    }),
    Placeholder.configure({
      placeholder: props.placeholder,
    }),
    CharacterCount,
    TextAlign.configure({
      types: ['paragraph', 'heading', 'image', 'video', 'audio'],
    }),
    Subscript,
    Superscript,
    TextStyle,
    Color,
    Highlight,
    // 自定义扩展（包含气泡菜单功能）
    Text,
    Bold,
    Italic,
    Underline,
    Strike,
    Code,
    Link,
    BulletList,
    OrderedList,
    ListItem,
    CodeBlock,
    Blockquote,
    ...tableExtensions,
    Heading,
    CommandsMenu,
    Image,
    Video,
    Audio,
    Upload,
    Details,
    DetailsSummary,
    DetailsContent,
    HtmlBlock,
    MarkdownBlock,
    MathBlock,
    MathInline,
    Diagram,
    Iframe,
    TaskList,
    TaskItem,
    SecureResource,
    MemberContent,
  ]),
  transformPastedHTML: (html) => sanitizePastedHTML(html),
  editorProps: {
    handlePaste(view, event) {
      const clipboard = event.clipboardData
      if (!clipboard) {
        return false
      }

      const { selection } = view.state
      const { $from, $to } = selection
      const fromIsCode = $from?.parent?.type?.spec?.code === true
      const toIsCode = $to?.parent?.type?.spec?.code === true
      const nodeIsCode =
        selection instanceof NodeSelection && selection.node?.type?.spec?.code === true

      if (fromIsCode || toIsCode || nodeIsCode) {
        return false
      }

      const textData = clipboard.getData('text/plain')
      if (!textData || textData.trim().length === 0) {
        return false
      }

      const htmlData = clipboard.getData('text/html')
      if (htmlData && htmlData.trim().length > 0) {
        const htmlText = extractPlainTextFromHtml(htmlData)
        if (htmlText && htmlText !== textData.trim()) {
          return false
        }
      }

      if (!isMarkdownContent(textData)) {
        return false
      }

      try {
        const parsedHtml = markdownToHtml(textData)
        if (!parsedHtml || parsedHtml.trim().length === 0) {
          return false
        }

        event.preventDefault()

        const container = createMarkdownContainer(parsedHtml.trim())
        if (!container) {
          return false
        }

        const parser = ProseMirrorDOMParser.fromSchema(view.state.schema)
        const slice = parser.parseSlice(container, { preserveWhitespace: 'full' })

        const tr = view.state.tr.replaceSelection(slice)
        view.dispatch(tr.scrollIntoView())
        return true
      } catch (error) {
        console.warn('Failed to transform markdown paste', error)
        return false
      }
    },
  },
  onUpdate: ({ editor }) => {
    const htmlContent = editor.getHTML()
    lastSyncedHtml.value = htmlContent

    const nextModelValue = htmlContent

    if (nextModelValue !== lastEmittedModelValue.value) {
      lastEmittedModelValue.value = nextModelValue
      emit('update:modelValue', nextModelValue)
    }

    pendingUpdate.value = { html: htmlContent, title: props.title }

    if (emitTimer) {
      return
    }

    emitTimer = setTimeout(() => {
      emitTimer = null
      const payload = pendingUpdate.value
      if (!payload) return
      const markdownContent = htmlToMarkdown(payload.html)
      emit('update', {
        content: payload.html,
        markdown: markdownContent,
        format: contentFormat.value,
        title: payload.title,
      })
      pendingUpdate.value = null
    }, UPDATE_EMIT_DELAY)
  },
})

// 计算属性
const characterCount = computed(() => {
  return editor.value?.storage.characterCount.characters() || 0
})

const wordCount = computed(() => {
  return editor.value?.storage.characterCount.words() || 0
})

const headingNodes = computed(() => {
  if (!editor.value) return []
  
  const nodes: HeadingNode[] = []
  const doc = editor.value.state.doc
  
  doc.descendants((node, pos) => {
    if (node.type.name === 'heading') {
      const id = node.attrs.id || `heading-${pos}`
      nodes.push({
        id,
        level: node.attrs.level,
        text: node.textContent,
        pos,
      })
    }
  })
  
  return nodes
})

// 方法
const getHeadingIcon = (level: number) => {
  switch (level) {
    case 1:
      return Document
    case 2:
      return List
    case 3:
      return Grid
    default:
      return More
  }
}

function handleSelectHeadingNode(node: HeadingNode) {
  if (!editor.value) return
  editor.value.chain().focus().setTextSelection(node.pos).run()
  selectedHeadingNode.value = node
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

// Sync editor instance once available
watch(
  () => editor.value,
  (instance) => {
    if (instance) {
      lastSyncedHtml.value = instance.getHTML()
    }
  },
  { immediate: true }
)

// Sync modelValue into editor content
watch(
  () => props.modelValue,
  (newValue) => {
    if (!editor.value) {
      return
    }

    const incoming = newValue ?? ''
    lastEmittedModelValue.value = incoming

    const trimmed = incoming.trim()

    if (!trimmed) {
      if (lastSyncedHtml.value !== '') {
        editor.value.commands.setContent('', false)
        lastSyncedHtml.value = ''
      }
      return
    }

    const detectedFormat = detectContentFormat(incoming)
    contentFormat.value = detectedFormat

    const htmlContent =
      detectedFormat === 'markdown' ? markdownToHtml(incoming) : incoming

    if (htmlContent === lastSyncedHtml.value) {
      return
    }

    const sanitized = sanitizePastedHTML(htmlContent)
    editor.value.commands.setContent(sanitized, false)
    lastSyncedHtml.value = sanitized
  }
)

watch(
  () => props.readonly,
  (newValue) => {
    if (editor.value) {
      editor.value.setEditable(!newValue)
    }
  }
)

onMounted(() => {
  // 这里可以做一些初始化，例如渲染大纲
})

onBeforeUnmount(() => {
  editor.value?.destroy()
  if (emitTimer) {
    clearTimeout(emitTimer)
    emitTimer = null
  }
})
</script>

<style scoped>
.rich-text-editor-new {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.editor-body {
  display: flex;
  min-height: 0;
  background: transparent;
}

.editor-content {
  flex: 1;
  padding: 0;
  min-height: 0;
}

.editor-sidebar {
  width: 280px;
  border-left: 1px solid #e5e7eb;
  background: #fff;
}

.toc-content {
  padding: 12px;
}

.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.toc-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.toc-item:hover {
  background: #f3f4f6;
}

.toc-item.active {
  background: #e5efff;
}

.heading-icon {
  color: #6b7280;
}

.heading-text {
  color: #374151;
  font-size: 12px;
}

.detail-content {
  padding: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
  padding: 6px 0;
}

.detail-label {
  color: #6b7280;
}

.detail-value {
  color: #374151;
}

.permalink-link {
  color: #3b82f6;
}

/* 编辑器内容样式 */
:deep(.ProseMirror) {
  outline: none;
  padding: 24px 0;
  max-width: 100%;
  margin: 0;
  min-height: 400px;
  line-height: 1.7;
  font-size: 16px;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

:deep(.ProseMirror p.is-empty::before) {
  content: attr(data-placeholder);
  float: left;
  color: #9ca3af;
  pointer-events: none;
  height: 0;
}

:deep(.ProseMirror h1) {
  font-size: 1.875rem;
  font-weight: bold;
  margin-top: 2rem;
  margin-bottom: 1rem;
  line-height: 1.2;
}

:deep(.ProseMirror h2) {
  font-size: 1.5rem;
  font-weight: bold;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  line-height: 1.3;
}

:deep(.ProseMirror h3) {
  font-size: 1.25rem;
  font-weight: bold;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

:deep(.ProseMirror h4) {
  font-size: 1.125rem;
  font-weight: bold;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

:deep(.ProseMirror h5) {
  font-size: 1rem;
  font-weight: bold;
  margin-top: 0.75rem;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

:deep(.ProseMirror h6) {
  font-size: 0.875rem;
  font-weight: bold;
  margin-top: 0.75rem;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

:deep(.ProseMirror p) {
  margin-top: 0.75rem;
  margin-bottom: 0;
  line-height: 1.6;
}

:deep(.ProseMirror p:empty) {
  display: block;
  margin: 0;
  min-height: 0.6em;
}

@supports selector(:has(*)) {
  :deep(.ProseMirror p:has(> br:only-child)) {
    display: block;
    margin: 0;
    min-height: 0.6em;
  }
}

:deep(.ProseMirror li > p) {
  margin: 0;
}

:deep(.ProseMirror li > p + p) {
  margin-top: 0.4rem;
}

:deep(.ProseMirror ul) {
  list-style-type: disc;
  list-style-position: outside;
  padding-left: 1.5rem;
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

:deep(.ProseMirror ul ul) {
  list-style-type: circle;
}

:deep(.ProseMirror ul ul ul) {
  list-style-type: square;
}

:deep(.ProseMirror ol) {
  list-style-type: decimal;
  list-style-position: outside;
  padding-left: 1.5rem;
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

:deep(.ProseMirror ol ol) {
  list-style-type: lower-alpha;
}

:deep(.ProseMirror ol ol ol) {
  list-style-type: lower-roman;
}

:deep(.ProseMirror li) {
  display: list-item;
  margin-top: 0.25rem;
  margin-bottom: 0.25rem;
}

:deep(.ProseMirror ul[data-type='taskList']) {
  list-style: none;
  padding-left: 0;
  margin: 0.5rem 0;
}

:deep(.ProseMirror ul[data-type='taskList'] li) {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin: 0.25rem 0;
}

:deep(.ProseMirror ul[data-type='taskList'] li > label) {
  margin-top: 0.3rem;
}

:deep(.ProseMirror ul[data-type='taskList'] li > div) {
  flex: 1 1 auto;
}

:deep(.ProseMirror ul[data-type='taskList'] li ul) {
  padding-left: 1.5rem;
  margin: 0.45rem 0 0;
}

:deep(.ProseMirror ul[data-type='taskList'] li > div > p) {
  margin: 0;
}

:deep(.ProseMirror blockquote) {
  border-left: 4px solid #d1d5db;
  padding-left: 1rem;
  font-style: italic;
  color: #6b7280;
  margin: 1rem 0;
}

:deep(.ProseMirror pre:not(.code-pre)) {
  background: #f3f4f6;
  border-radius: 0.5rem;
  padding: 1rem;
  margin: 1rem 0;
  overflow-x: auto;
}

:deep(.ProseMirror .code-block-wrapper) {
  margin-top: 1rem;
  margin-bottom: 1.25rem;
}

:deep(.ProseMirror code) {
  background: #f3f4f6;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  font-family: monospace;
}

:deep(.ProseMirror table) {
  border-collapse: collapse;
  border: 1px solid #d1d5db;
  margin: 1rem 0;
}

:deep(.ProseMirror ::selection) {
  background: rgba(59, 130, 246, 0.25);
  color: inherit;
}

:deep(.ProseMirror ::-moz-selection) {
  background: rgba(59, 130, 246, 0.25);
  color: inherit;
}

:deep(.ProseMirror th),
:deep(.ProseMirror td) {
  border: 1px solid #d1d5db;
  padding: 0.75rem;
}

:deep(.ProseMirror th) {
  background: #f9fafb;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .editor-body {
    flex-direction: column;
  }
  
  .editor-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e5e7eb;
    max-height: 300px;
    overflow-y: auto;
  }
  
  :deep(.ProseMirror) {
    max-width: 100%;
    padding: 20px 24px;
  }
}

@media (max-width: 768px) {
  .editor-content {
    padding: 0;
  }
  
  :deep(.ProseMirror) {
    padding: 16px 20px;
    font-size: 15px;
    min-height: 300px;
  }
}

@media (max-width: 480px) {
  .editor-content {
    padding: 0;
  }
  
  :deep(.ProseMirror) {
    padding: 12px 16px;
    font-size: 14px;
  }
}
</style>
