<template>
  <div class="editor-toolbar border-b border-gray-200 bg-white p-2">
    <div class="flex flex-wrap items-center gap-1">
      <!-- 文本格式 -->
      <div class="flex items-center gap-1 border-r border-gray-200 pr-2">
        <button
          @click="editor.chain().focus().toggleBold().run()"
          :class="{ 'bg-gray-100': editor.isActive('bold') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="粗体 (Ctrl+B)"
        >
          <Edit class="h-4 w-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleItalic().run()"
          :class="{ 'bg-gray-100': editor.isActive('italic') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="斜体 (Ctrl+I)"
        >
          <EditPen class="h-4 w-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleStrike().run()"
          :class="{ 'bg-gray-100': editor.isActive('strike') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="删除线"
        >
          <Strikethrough class="h-4 w-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleUnderline().run()"
          :class="{ 'bg-gray-100': editor.isActive('underline') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="下划线 (Ctrl+U)"
        >
          <Underline class="h-4 w-4" />
        </button>
      </div>

      <!-- 标题 -->
      <div class="flex items-center gap-1 border-r border-gray-200 pr-2">
        <button
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
          :class="{ 'bg-gray-100': editor.isActive('heading', { level: 1 }) }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="标题 1"
        >
          H1
        </button>
        <button
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
          :class="{ 'bg-gray-100': editor.isActive('heading', { level: 2 }) }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="标题 2"
        >
          H2
        </button>
        <button
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
          :class="{ 'bg-gray-100': editor.isActive('heading', { level: 3 }) }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="标题 3"
        >
          H3
        </button>
      </div>

      <!-- 列表 -->
      <div class="flex items-center gap-1 border-r border-gray-200 pr-2">
        <button
          @click="editor.chain().focus().toggleBulletList().run()"
          :class="{ 'bg-gray-100': editor.isActive('bulletList') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="无序列表"
        >
          <List class="h-4 w-4" />
        </button>
        <button
          @click="editor.chain().focus().toggleOrderedList().run()"
          :class="{ 'bg-gray-100': editor.isActive('orderedList') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="有序列表"
        >
          <ListOrdered class="h-4 w-4" />
        </button>
      </div>

      <!-- 其他功能 -->
      <div class="flex items-center gap-1">
        <button
          @click="editor.chain().focus().toggleCodeBlock().run()"
          :class="{ 'bg-gray-100': editor.isActive('codeBlock') }"
          class="rounded p-1.5 hover:bg-gray-100"
          title="代码块"
        >
          <Code class="h-4 w-4" />
        </button>
        <button
          @click="editor.chain().focus().setHorizontalRule().run()"
          class="rounded p-1.5 hover:bg-gray-100"
          title="分割线"
        >
          <Minus class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Editor } from '@tiptap/vue-3'
import { 
  Edit, 
  EditPen, 
  Minus as Strikethrough, 
  Link as Underline,
  List,
  Grid as ListOrdered,
  Document as Code,
  Minus
} from '@element-plus/icons-vue'

defineProps<{
  editor: Editor
}>()
</script>