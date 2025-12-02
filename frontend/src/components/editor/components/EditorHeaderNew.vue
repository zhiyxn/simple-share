<template>
  <div class="editor-header-new">
    <div class="toolbar-section">
      <!-- 工具箱按钮 -->
      <el-dropdown
        trigger="click"
        placement="bottom-start"
        :hide-on-click="false"
        @visible-change="handleToolboxVisibleChange"
      >
        <el-button
          class="toolbox-trigger"
          :class="{ 'is-active': toolboxVisible }"
          size="small"
        >
          <el-icon class="plus-icon">
            <Plus />
          </el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu class="toolbox-dropdown">
            <div class="toolbox-content">
              <component
                v-for="(toolboxItem, index) in getToolboxItemsFromExtensions()"
                :key="index"
                :is="toolboxItem.component"
                v-bind="toolboxItem.props"
                @click="handleToolboxItemClick"
              />
            </div>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 分隔符 -->
      <div class="toolbar-divider" />

      <!-- 动态工具栏项目 -->
      <div class="toolbar-items">
        <template
          v-for="(item, index) in getToolbarItemsFromExtensions()"
          :key="index"
        >
          <!-- 没有子项的工具栏项 -->
          <component
            v-if="!item.children?.length"
            :is="item.component"
            v-bind="item.props"
          />
          
          <!-- 有子项的工具栏项（下拉菜单） -->
          <el-dropdown
            v-else
            trigger="click"
            placement="bottom-start"
          >
            <component
              :is="item.component"
              v-bind="item.props"
              :children="item.children"
            />
            <template #dropdown>
              <el-dropdown-menu>
                <component
                  v-for="(child, childIndex) in item.children"
                  :key="childIndex"
                  :is="child.component"
                  v-bind="child.props"
                />
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Editor, type AnyExtension } from '@tiptap/vue-3'
import type { ToolbarItemType, ToolboxItemType } from '../types'
import { ElButton, ElIcon, ElDropdown, ElDropdownMenu } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

interface Props {
  editor?: Editor | null
}

const props = defineProps<Props>()

const toolboxVisible = ref(false)

// 从扩展中获取工具栏项目
function getToolbarItemsFromExtensions(): ToolbarItemType[] {
  if (!props.editor) return []
  
  const extensionManager = props.editor.extensionManager
  return extensionManager.extensions
    .reduce((acc: ToolbarItemType[], extension: AnyExtension) => {
      const { getToolbarItems } = extension.options

      if (!getToolbarItems) {
        return acc
      }

      const items = getToolbarItems({
        editor: props.editor!,
      })

      if (Array.isArray(items)) {
        return [...acc, ...items]
      }

      return [...acc, items]
    }, [])
    .sort((a, b) => a.priority - b.priority)
}

// 从扩展中获取工具箱项目
function getToolboxItemsFromExtensions(): ToolboxItemType[] {
  if (!props.editor) return []
  
  const extensionManager = props.editor.extensionManager
  return extensionManager.extensions
    .reduce((acc: ToolboxItemType[], extension: AnyExtension) => {
      const { getToolboxItems } = extension.options

      if (!getToolboxItems) {
        return acc
      }

      const items = getToolboxItems({
        editor: props.editor!,
      })

      if (Array.isArray(items)) {
        return [...acc, ...items]
      }

      return [...acc, items]
    }, [])
    .sort((a, b) => a.priority - b.priority)
}

const handleToolboxVisibleChange = (visible: boolean) => {
  toolboxVisible.value = visible
}

const handleToolboxItemClick = () => {
  toolboxVisible.value = false
}
</script>

<style scoped>
.editor-header-new {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid var(--el-border-color-light);
  background-color: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.toolbar-section {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.toolbox-trigger {
  border-radius: 6px;
  transition: all 0.2s;
}

.toolbox-trigger.is-active {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.plus-icon {
  color: var(--el-color-success);
  font-size: 16px;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background-color: var(--el-border-color-light);
  margin: 0 4px;
}

.toolbar-items {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.toolbox-dropdown {
  max-width: 280px;
  max-height: 400px;
  overflow-y: auto;
}

.toolbox-content {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>