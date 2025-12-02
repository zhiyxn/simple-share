import type { Editor, Range } from '@tiptap/vue-3'
import type { Component } from 'vue'

// 附件相关类型
export interface AttachmentLike {
  url?: string
  name?: string
  type?: string
  size?: number
}

export interface AttachmentAttr {
  url?: string
  name?: string
}

export interface AttachmentResult {
  updateAttachment: (attachments: AttachmentLike[]) => void
}

// 上传相关类型
export interface FileProps {
  file: File
  editor: Editor
}

export interface UploadResponse {
  controller: AbortController
  onUploadProgress: (progress: number) => void
  onFinish: (attachment?: AttachmentLike) => void
  onError: (error: Error) => void
}

// 编辑器配置类型
export interface EditorOptions {
  placeholder?: string
  content?: string
  editable?: boolean
  autofocus?: boolean
  uploadImage?: (file: File) => Promise<AttachmentLike>
  uploadVideo?: (file: File) => Promise<AttachmentLike>
  uploadAudio?: (file: File) => Promise<AttachmentLike>
}

// 工具箱相关类型（旧版本，保持兼容性）
export interface ToolboxItem {
  name: string
  title: string
  icon: string
  command: (editor: Editor) => void
  isActive?: (editor: Editor) => boolean
}

export interface ToolboxGroup {
  name: string
  title: string
  items: ToolboxItem[]
}

// 工具栏项目类型
export interface ToolbarItemType {
  priority: number
  component: Component
  props: {
    editor: Editor
    isActive: boolean
    disabled?: boolean
    icon?: Component
    title?: string
    action?: () => void
  }
  children?: ToolbarItemType[]
}

// 工具箱项目类型（新版本）
export interface ToolboxItemType {
  priority: number
  component: Component
  props: {
    editor: Editor
    icon?: Component
    title?: string
    description?: string
    action?: () => void
  }
}

// 命令菜单项目类型
export interface CommandMenuItemType {
  priority: number
  icon: Component
  title: string
  keywords: string[]
  command: ({ editor, range }: { editor: Editor; range: Range }) => void
}

// 气泡菜单相关类型
export interface BubbleMenuItemType {
  priority: number
  component?: Component
  props?: {
    isActive?: (props: { editor: Editor }) => boolean
    icon?: Component
    title?: string
    action?: (props: { editor: Editor }) => void
    [key: string]: any
  }
}

export interface NodeBubbleMenuType {
  pluginKey: string
  shouldShow?: (props: {
    editor: Editor
    state: any
    node?: HTMLElement
    view?: any
    oldState?: any
    from?: number
    to?: number
  }) => boolean
  tippyOptions?: Record<string, any>
  defaultAnimation?: boolean
  getRenderContainer?: (node: HTMLElement) => HTMLElement
  items?: BubbleMenuItemType[]
  component?: Component
}

// 拖拽项目类型
export interface DraggableItemType {
  component: Component
  props: {
    editor: Editor
    node?: any
  }
}

// 扩展选项接口
export interface ExtensionOptions {
  // 顶部工具栏扩展
  getToolbarItems?: ({
    editor,
  }: {
    editor: Editor
  }) => ToolbarItemType | ToolbarItemType[]

  // Slash Command 扩展
  getCommandMenuItems?: () => CommandMenuItemType | CommandMenuItemType[]

  // 悬浮菜单扩展
  getBubbleMenu?: ({ editor }: { editor: Editor }) => NodeBubbleMenuType

  // 工具箱扩展
  getToolboxItems?: ({
    editor,
  }: {
    editor: Editor
  }) => ToolboxItemType | ToolboxItemType[]

  // 拖拽扩展
  getDraggable?: ({ editor }: { editor: Editor }) => DraggableItemType | boolean
}

// 编辑器扩展基础类型
export interface EditorExtension {
  name: string
  options?: ExtensionOptions
}