// 组件导出
export { default as RichTextEditor } from './RichTextEditor.vue'
export { default as EditorToolbar } from './components/EditorToolbar.vue'
export { default as EditorHeader } from './components/EditorHeader.vue'
export { default as EditorSidebar } from './components/EditorSidebar.vue'
export { default as ToolboxMenu } from './components/ToolboxMenu.vue'
export { default as EditorToolbox } from './components/EditorToolbox.vue'
export { default as EditorLinkObtain } from './components/EditorLinkObtain.vue'
export { default as InlineBlockBox } from './components/InlineBlockBox.vue'

// 导出扩展组件
export { default as ImageExtension } from './extensions/image/ImageExtension.vue'
export { default as VideoExtension } from './extensions/video/VideoExtension.vue'
export { default as AudioExtension } from './extensions/audio/AudioExtension.vue'
export { default as UploadExtension } from './extensions/upload/UploadExtension.vue'

// 导出工具函数
export { useAttachment } from './composables/use-attachment'
export { useExtension } from './composables/use-extension'
export * from './utils/attachment'
export * from './utils/upload'

// 导出类型
export type * from './types'