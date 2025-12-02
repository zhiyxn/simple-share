import type { Directive, DirectiveBinding } from 'vue'

// 防复制配置接口
interface CopyProtectionOptions {
  disableSelection?: boolean
  disableRightClick?: boolean
  disableCopy?: boolean
  disablePrint?: boolean
  disableDevTools?: boolean
  showWarning?: boolean
  warningMessage?: string
}

// 默认配置
const defaultOptions: CopyProtectionOptions = {
  disableSelection: true,
  disableRightClick: true,
  disableCopy: true,
  disablePrint: false,
  disableDevTools: false,
  showWarning: true,
  warningMessage: '内容受版权保护，禁止复制！'
}

// 存储元素的原始样式和事件处理器
const elementData = new WeakMap<HTMLElement, {
  originalStyles: Partial<CSSStyleDeclaration>
  eventHandlers: { [key: string]: EventListener }
  options: CopyProtectionOptions
}>()

// 显示警告消息
const showWarning = (message: string) => {
  // 创建警告提示
  const warning = document.createElement('div')
  warning.textContent = message
  warning.style.cssText = `
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 12px 24px;
    border-radius: 6px;
    font-size: 14px;
    z-index: 10000;
    pointer-events: none;
    animation: fadeInOut 2s ease-in-out;
  `
  
  // 添加动画样式
  if (!document.getElementById('copy-protection-styles')) {
    const style = document.createElement('style')
    style.id = 'copy-protection-styles'
    style.textContent = `
      @keyframes fadeInOut {
        0% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
        20% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
        80% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
        100% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
      }
    `
    document.head.appendChild(style)
  }
  
  document.body.appendChild(warning)
  
  // 2秒后移除警告
  setTimeout(() => {
    if (warning.parentNode) {
      warning.parentNode.removeChild(warning)
    }
  }, 2000)
}

// 阻止选择文本
const preventSelection = (e: Event) => {
  e.preventDefault()
  return false
}

// 阻止右键菜单
const preventContextMenu = (e: Event) => {
  e.preventDefault()
  return false
}

// 阻止复制相关快捷键
const preventCopyKeys = (e: KeyboardEvent) => {
  const { ctrlKey, metaKey, key, keyCode } = e
  
  // 阻止 Ctrl+C, Ctrl+A, Ctrl+S, Ctrl+P, F12 等
  if (
    (ctrlKey || metaKey) && (
      key === 'c' || key === 'C' ||
      key === 'a' || key === 'A' ||
      key === 's' || key === 'S' ||
      key === 'p' || key === 'P' ||
      key === 'u' || key === 'U' ||
      key === 'i' || key === 'I'
    ) ||
    keyCode === 123 || // F12
    keyCode === 67 ||  // Ctrl+C
    keyCode === 65 ||  // Ctrl+A
    keyCode === 83 ||  // Ctrl+S
    keyCode === 80 ||  // Ctrl+P
    keyCode === 85 ||  // Ctrl+U
    keyCode === 73     // Ctrl+I
  ) {
    e.preventDefault()
    
    const data = elementData.get(e.currentTarget as HTMLElement)
    if (data?.options.showWarning) {
      showWarning(data.options.warningMessage || defaultOptions.warningMessage!)
    }
    
    return false
  }
}

// 阻止拖拽
const preventDrag = (e: Event) => {
  e.preventDefault()
  return false
}

// 阻止打印
const preventPrint = () => {
  window.print = () => {
    console.warn('打印功能已被禁用')
  }
}

// 禁用开发者工具检测
const disableDevTools = () => {
  // 检测开发者工具
  const devtools = {
    open: false,
    orientation: null as string | null
  }
  
  const threshold = 160
  
  setInterval(() => {
    if (
      window.outerHeight - window.innerHeight > threshold ||
      window.outerWidth - window.innerWidth > threshold
    ) {
      if (!devtools.open) {
        devtools.open = true
        console.clear()
        console.warn('检测到开发者工具，请关闭后继续浏览')
        // 可以在这里添加更严格的措施，如跳转到警告页面
      }
    } else {
      devtools.open = false
    }
  }, 500)
  
  // 禁用常见的调试方法
  ;(window as any).console.log = () => {}
  ;(window as any).console.warn = () => {}
  ;(window as any).console.error = () => {}
  ;(window as any).console.info = () => {}
  ;(window as any).console.debug = () => {}
}

// 应用防复制样式
const applyProtectionStyles = (el: HTMLElement, options: CopyProtectionOptions) => {
  const styles: Partial<CSSStyleDeclaration> = {}
  
  if (options.disableSelection) {
    styles.userSelect = 'none'
    styles.webkitUserSelect = 'none'
    styles.mozUserSelect = 'none'
    styles.msUserSelect = 'none'
    styles.webkitTouchCallout = 'none'
    styles.webkitUserDrag = 'none'
    styles.khtmlUserSelect = 'none'
  }
  
  // 保存原始样式
  const originalStyles: Partial<CSSStyleDeclaration> = {}
  Object.keys(styles).forEach(key => {
    const styleKey = key as keyof CSSStyleDeclaration
    originalStyles[styleKey] = el.style[styleKey] as any
    ;(el.style as any)[styleKey] = (styles as any)[styleKey]
  })
  
  return originalStyles
}

// 绑定事件处理器
const bindEventHandlers = (el: HTMLElement, options: CopyProtectionOptions) => {
  const handlers: { [key: string]: EventListener } = {}
  
  if (options.disableSelection) {
    handlers.selectstart = preventSelection
    handlers.dragstart = preventDrag
    el.addEventListener('selectstart', handlers.selectstart)
    el.addEventListener('dragstart', handlers.dragstart)
  }
  
  if (options.disableRightClick) {
    handlers.contextmenu = preventContextMenu
    el.addEventListener('contextmenu', handlers.contextmenu)
  }
  
  if (options.disableCopy) {
    handlers.keydown = preventCopyKeys
    el.addEventListener('keydown', handlers.keydown)
  }
  
  return handlers
}

// 移除事件处理器
const removeEventHandlers = (el: HTMLElement, handlers: { [key: string]: EventListener }) => {
  Object.keys(handlers).forEach(event => {
    el.removeEventListener(event, handlers[event])
  })
}

// 恢复原始样式
const restoreOriginalStyles = (el: HTMLElement, originalStyles: Partial<CSSStyleDeclaration>) => {
  Object.keys(originalStyles).forEach(key => {
    const styleKey = key as keyof CSSStyleDeclaration
    ;(el.style as any)[styleKey] = originalStyles[styleKey] || ''
  })
}

// 防复制指令
const copyProtection: Directive<HTMLElement, CopyProtectionOptions | boolean> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<CopyProtectionOptions | boolean>) {
    let options: CopyProtectionOptions
    
    if (typeof binding.value === 'boolean') {
      options = binding.value ? defaultOptions : {}
    } else {
      options = { ...defaultOptions, ...binding.value }
    }
    
    // 如果所有保护选项都是 false，则不应用任何保护
    if (!Object.values(options).some(value => value === true)) {
      return
    }
    
    // 应用样式
    const originalStyles = applyProtectionStyles(el, options)
    
    // 绑定事件
    const eventHandlers = bindEventHandlers(el, options)
    
    // 全局设置
    if (options.disablePrint) {
      preventPrint()
    }
    
    if (options.disableDevTools) {
      disableDevTools()
    }
    
    // 存储数据
    elementData.set(el, {
      originalStyles,
      eventHandlers,
      options
    })
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding<CopyProtectionOptions | boolean>) {
    // 先清理旧的设置
    const data = elementData.get(el)
    if (data) {
      removeEventHandlers(el, data.eventHandlers)
      restoreOriginalStyles(el, data.originalStyles)
    }
    
    // 重新应用新的设置
    let options: CopyProtectionOptions
    
    if (typeof binding.value === 'boolean') {
      options = binding.value ? defaultOptions : {}
    } else {
      options = { ...defaultOptions, ...binding.value }
    }
    
    if (!Object.values(options).some(value => value === true)) {
      elementData.delete(el)
      return
    }
    
    const originalStyles = applyProtectionStyles(el, options)
    const eventHandlers = bindEventHandlers(el, options)
    
    elementData.set(el, {
      originalStyles,
      eventHandlers,
      options
    })
  },
  
  unmounted(el: HTMLElement) {
    const data = elementData.get(el)
    if (data) {
      removeEventHandlers(el, data.eventHandlers)
      restoreOriginalStyles(el, data.originalStyles)
      elementData.delete(el)
    }
  }
}

export default copyProtection
export type { CopyProtectionOptions }