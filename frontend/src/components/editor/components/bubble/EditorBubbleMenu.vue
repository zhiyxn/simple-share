<script lang="ts" setup>
import type { Editor } from '@tiptap/vue-3'
import { NodeSelection } from '@tiptap/pm/state'
import type { PropType } from 'vue'
import { computed } from 'vue'
import BubbleMenu from './BubbleMenu.vue'
import BubbleItem from './BubbleItem.vue'
import type { NodeBubbleMenuType, BubbleMenuItemType } from '../../types'

const props = defineProps({
  editor: {
    type: Object as PropType<Editor>,
    required: true,
  },
})

const getBubbleMenuFromExtensions = () => {
  const extensionManager = props.editor?.extensionManager
  const bubbleMenus: NodeBubbleMenuType[] = []
  
  // 收集所有文本格式化菜单项
  const textBubbleMenuItems: BubbleMenuItemType[] = []
  
  // 用于去重的Set，基于title和action来判断是否重复
  const seenItems = new Set<string>()
  
  extensionManager.extensions.forEach((extension: any) => {
    const { getBubbleMenu } = extension.options || {}

    if (!getBubbleMenu) {
      return
    }

    const bubbleMenuResult = getBubbleMenu({
      editor: props.editor,
    })

    // 如果返回的是数组（文本扩展），收集到文本菜单项中
    if (Array.isArray(bubbleMenuResult)) {
      bubbleMenuResult.forEach((item: BubbleMenuItemType) => {
        // 创建唯一标识符
        let itemKey = ''
        if (item.component) {
          // 对于组件类型，使用组件名和优先级作为标识符
          itemKey = `${item.component.name || 'component'}_${item.priority || 0}`
        } else {
          // 对于普通菜单项，基于title和action的字符串表示
          itemKey = `${item.props?.title || ''}_${item.props?.action?.toString() || ''}`
        }
        
        // 如果没有见过这个项目，则添加
        if (!seenItems.has(itemKey)) {
          seenItems.add(itemKey)
          textBubbleMenuItems.push(item)
        }
      })
    } 
    // 如果返回的是对象（表格等节点扩展），直接添加
    else if (bubbleMenuResult && typeof bubbleMenuResult === 'object') {
      const nodeBubbleMenu = bubbleMenuResult as NodeBubbleMenuType
      if (nodeBubbleMenu.items) {
        // 对节点菜单项也进行去重
        const uniqueItems: BubbleMenuItemType[] = []
        const nodeSeenItems = new Set<string>()
        
        nodeBubbleMenu.items.forEach((item: BubbleMenuItemType) => {
          const itemKey = `${item.props?.title || ''}_${item.props?.action?.toString() || ''}`
          if (!nodeSeenItems.has(itemKey)) {
            nodeSeenItems.add(itemKey)
            uniqueItems.push(item)
          }
        })
        
        nodeBubbleMenu.items = uniqueItems.sort(
          (a, b) => a.priority - b.priority
        )
      }
      bubbleMenus.push(nodeBubbleMenu)
    }
  })
  
  // 如果有文本菜单项，创建一个文本气泡菜单
  if (textBubbleMenuItems.length > 0) {
    const textBubbleMenu: NodeBubbleMenuType = {
      pluginKey: 'textBubbleMenu',
      shouldShow: ({ state, editor }) => {
        const { selection } = state
        const { empty } = selection
        
        // 只有选中文本时才显示文本格式化菜单
        if (empty) {
          return false
        }
        if (selection instanceof NodeSelection) {
          return false
        }
        
        return true
      },
      items: textBubbleMenuItems.sort((a, b) => (b.priority || 0) - (a.priority || 0))
    }
    bubbleMenus.unshift(textBubbleMenu) // 文本菜单优先级最高
  }
  
  return bubbleMenus
}

const shouldShow = (
  props: {
    editor: Editor
    state: any
    node?: HTMLElement
    view?: any
    oldState?: any
    from?: number
    to?: number
  },
  bubbleMenu: NodeBubbleMenuType
) => {
  if (!props.editor.isEditable) {
    return false
  }
  
  return bubbleMenu.shouldShow?.(props) || false
}
</script>

<template>
  <BubbleMenu
    v-for="(bubbleMenu, index) in getBubbleMenuFromExtensions()"
    :key="index"
    :plugin-key="bubbleMenu?.pluginKey"
    :should-show="(prop) => shouldShow(prop, bubbleMenu)"
    :editor="editor"
    :tippy-options="{
      maxWidth: '100%',
      duration: 100,
      ...bubbleMenu.tippyOptions,
    }"
    :get-render-container="bubbleMenu.getRenderContainer"
    :default-animation="bubbleMenu.defaultAnimation"
  >
    <div class="bubble-menu">
      <template v-if="bubbleMenu.items">
        <template
          v-for="(item, itemIndex) in bubbleMenu.items"
          :key="itemIndex"
        >
          <template v-if="item.component">
            <component
              :is="item.component"
              v-bind="item.componentProps"
              :editor="editor"
            />
          </template>
          <BubbleItem v-else :editor="editor" v-bind="item.props" />
        </template>
      </template>
      <template v-else-if="bubbleMenu.component">
        <component :is="bubbleMenu?.component" :editor="editor" />
      </template>
    </div>
  </BubbleMenu>
</template>

<style scoped>
.bubble-menu {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: white;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: calc(100vw - 30px);
  overflow-x: auto;
  backdrop-filter: blur(8px);
  z-index: 1000;
}

.bubble-menu::-webkit-scrollbar {
  height: 4px;
}

.bubble-menu::-webkit-scrollbar-track {
  background: transparent;
}

.bubble-menu::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 2px;
}

.bubble-menu::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-dark);
}

/* 确保菜单项之间有适当的间距 */
.bubble-menu > * {
  flex-shrink: 0;
}

/* 分隔线样式 */
.bubble-menu::after {
  content: '';
  width: 1px;
  height: 20px;
  background: var(--el-border-color-light);
  margin: 0 4px;
  display: none;
}

/* 当有多个菜单项时显示分隔线 */
.bubble-menu:has(> *:nth-child(2))::after {
  display: block;
}
</style>
