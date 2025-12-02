import type { App } from 'vue'
import copyProtection from './copy-protection'
import watermark from './watermark'
import { vContentProtect as contentProtect } from './content-protect'
import hasPermi from './permission'

// 指令集合
const directives = {
  copyProtection,
  watermark,
  'content-protect': contentProtect,
  'has-permi': hasPermi,
  hasPermi: hasPermi
} as const

// 安装所有指令的函数
export const installDirectives = (app: App) => {
  Object.entries(directives).forEach(([name, directive]) => {
    app.directive(name, directive)
  })
}

// 单独导出指令
export { copyProtection, watermark, contentProtect }

// 导出类型
export type { CopyProtectionOptions } from './copy-protection'
export type { WatermarkOptions } from './watermark'
export { createGlobalWatermark, removeGlobalWatermark } from './watermark'

// 默认导出
export default {
  install: installDirectives
}
