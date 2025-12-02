<template>
  <div class="toolbox-menu">
    <div class="flex items-center gap-2 p-2 border-b border-gray-200">
      <h3 class="text-sm font-medium text-gray-700">工具箱</h3>
    </div>
    
    <div class="p-2 space-y-2">
      <!-- 文档结构 -->
      <div class="space-y-1">
        <h4 class="text-xs font-medium text-gray-500 uppercase tracking-wide">文档结构</h4>
        <button
          @click="insertHeading(1)"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <span class="text-lg font-bold">H1</span>
          <span class="text-sm text-gray-600">标题 1</span>
        </button>
        <button
          @click="insertHeading(2)"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <span class="text-base font-bold">H2</span>
          <span class="text-sm text-gray-600">标题 2</span>
        </button>
        <button
          @click="insertHeading(3)"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <span class="text-sm font-bold">H3</span>
          <span class="text-sm text-gray-600">标题 3</span>
        </button>
      </div>

      <!-- 列表 -->
      <div class="space-y-1">
        <h4 class="text-xs font-medium text-gray-500 uppercase tracking-wide">列表</h4>
        <button
          @click="insertBulletList"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <List class="h-4 w-4" />
          <span class="text-sm text-gray-600">无序列表</span>
        </button>
        <button
          @click="insertOrderedList"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <ListOrdered class="h-4 w-4" />
          <span class="text-sm text-gray-600">有序列表</span>
        </button>
        <button
          @click="insertTaskList"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <Check class="h-4 w-4" />
          <span class="text-sm text-gray-600">任务列表</span>
        </button>
      </div>

      <!-- 媒体 -->
      <div class="space-y-1">
        <h4 class="text-xs font-medium text-gray-500 uppercase tracking-wide">媒体</h4>
        <button
          @click="insertImage"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <Picture class="h-4 w-4" />
          <span class="text-sm text-gray-600">图片</span>
        </button>
        <button
          @click="insertTable"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <Grid class="h-4 w-4" />
          <span class="text-sm text-gray-600">表格</span>
        </button>
        <button
          @click="insertCodeBlock"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <Code class="h-4 w-4" />
          <span class="text-sm text-gray-600">代码块</span>
        </button>
      </div>

      <!-- 其他 -->
      <div class="space-y-1">
        <h4 class="text-xs font-medium text-gray-500 uppercase tracking-wide">其他</h4>
        <button
          @click="insertHorizontalRule"
          class="w-full flex items-center gap-2 p-2 text-left rounded hover:bg-gray-100"
        >
          <Minus class="h-4 w-4" />
          <span class="text-sm text-gray-600">分割线</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Editor } from '@tiptap/vue-3'
import { 
  List, 
  Document as ListOrdered, 
  Check, 
  Picture, 
  Grid, 
  Document as Code, 
  Minus 
} from '@element-plus/icons-vue'

const props = defineProps<{
  editor: Editor
}>()

const insertHeading = (level: number) => {
  props.editor.chain().focus().toggleHeading({ level }).run()
}

const insertBulletList = () => {
  props.editor.chain().focus().toggleBulletList().run()
}

const insertOrderedList = () => {
  props.editor.chain().focus().toggleOrderedList().run()
}

const insertTaskList = () => {
  props.editor.chain().focus().toggleTaskList().run()
}

const insertImage = () => {
  const url = window.prompt('请输入图片URL:')
  if (url) {
    props.editor.chain().focus().setImage({ src: url }).run()
  }
}

const insertTable = () => {
  props.editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
}

const insertCodeBlock = () => {
  props.editor.chain().focus().toggleCodeBlock().run()
}

const insertHorizontalRule = () => {
  props.editor.chain().focus().setHorizontalRule().run()
}
</script>

<style scoped>
.toolbox-menu {
  @apply bg-white border border-gray-200 rounded-lg shadow-sm;
  width: 200px;
}
</style>