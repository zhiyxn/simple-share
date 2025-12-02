import type { Directive, DirectiveBinding } from 'vue'
import { SITE_WATERMARK_TEXT } from '@/config/site'

// 水印配置接口
interface WatermarkOptions {
  text?: string
  fontSize?: number
  fontFamily?: string
  color?: string
  opacity?: number
  angle?: number
  width?: number
  height?: number
  zIndex?: number
  gap?: [number, number]
  offset?: [number, number]
  image?: string
  imageWidth?: number
  imageHeight?: number
  repeat?: boolean
  pointer?: boolean
}

// 默认配置
const defaultOptions: Required<WatermarkOptions> = {
  text: SITE_WATERMARK_TEXT,
  fontSize: 14,
  fontFamily: 'Arial, sans-serif',
  color: '#000000',
  opacity: 0.1,
  angle: -20,
  width: 200,
  height: 100,
  zIndex: 1000,
  gap: [100, 100],
  offset: [0, 0],
  image: '',
  imageWidth: 100,
  imageHeight: 100,
  repeat: true,
  pointer: false
}

// 存储水印元素和配置
const watermarkData = new WeakMap<HTMLElement, {
  watermarkEl: HTMLElement
  options: Required<WatermarkOptions>
  observer?: MutationObserver
}>()

// 生成水印背景图片
const generateWatermarkImage = (options: Required<WatermarkOptions>): string => {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')!
  
  const { width, height, text, fontSize, fontFamily, color, opacity, angle, image, imageWidth, imageHeight } = options
  
  canvas.width = width
  canvas.height = height
  
  ctx.clearRect(0, 0, width, height)
  
  // 设置透明度
  ctx.globalAlpha = opacity
  
  // 如果有图片水印
  if (image) {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      ctx.save()
      ctx.translate(width / 2, height / 2)
      ctx.rotate((angle * Math.PI) / 180)
      ctx.drawImage(img, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight)
      ctx.restore()
    }
    img.src = image
  }
  
  // 文字水印
  if (text) {
    ctx.save()
    ctx.translate(width / 2, height / 2)
    ctx.rotate((angle * Math.PI) / 180)
    
    ctx.font = `${fontSize}px ${fontFamily}`
    ctx.fillStyle = color
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    
    // 支持多行文本
    const lines = text.split('\n')
    const lineHeight = fontSize * 1.2
    const totalHeight = lines.length * lineHeight
    
    lines.forEach((line, index) => {
      const y = (index - (lines.length - 1) / 2) * lineHeight
      ctx.fillText(line, 0, y)
    })
    
    ctx.restore()
  }
  
  return canvas.toDataURL()
}

// 创建水印元素
const createWatermarkElement = (options: Required<WatermarkOptions>): HTMLElement => {
  const watermarkEl = document.createElement('div')
  const dataUrl = generateWatermarkImage(options)
  
  watermarkEl.style.cssText = `
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: ${options.pointer ? 'auto' : 'none'};
    z-index: ${options.zIndex};
    background-image: url(${dataUrl});
    background-repeat: ${options.repeat ? 'repeat' : 'no-repeat'};
    background-position: ${options.offset[0]}px ${options.offset[1]}px;
    background-size: ${options.width + options.gap[0]}px ${options.height + options.gap[1]}px;
  `
  
  // 添加水印标识
  watermarkEl.setAttribute('data-watermark', 'true')
  
  return watermarkEl
}

// 更新水印
const updateWatermark = (el: HTMLElement, options: Required<WatermarkOptions>) => {
  const data = watermarkData.get(el)
  if (!data) return
  
  // 移除旧水印
  if (data.watermarkEl.parentNode) {
    data.watermarkEl.parentNode.removeChild(data.watermarkEl)
  }
  
  // 创建新水印
  const newWatermarkEl = createWatermarkElement(options)
  
  // 确保容器是相对定位
  if (getComputedStyle(el).position === 'static') {
    el.style.position = 'relative'
  }
  
  el.appendChild(newWatermarkEl)
  
  // 更新数据
  data.watermarkEl = newWatermarkEl
  data.options = options
}

// 创建防篡改观察器
const createMutationObserver = (el: HTMLElement): MutationObserver => {
  const observer = new MutationObserver((mutations) => {
    const data = watermarkData.get(el)
    if (!data) return
    
    let needsUpdate = false
    
    mutations.forEach((mutation) => {
      // 检查是否有水印元素被移除
      if (mutation.type === 'childList') {
        mutation.removedNodes.forEach((node) => {
          if (node === data.watermarkEl || 
              (node as Element)?.getAttribute?.('data-watermark') === 'true') {
            needsUpdate = true
          }
        })
      }
      
      // 检查水印元素的样式是否被修改
      if (mutation.type === 'attributes' && 
          mutation.target === data.watermarkEl && 
          mutation.attributeName === 'style') {
        needsUpdate = true
      }
    })
    
    if (needsUpdate) {
      // 延迟重新创建水印，避免无限循环
      setTimeout(() => {
        updateWatermark(el, data.options)
      }, 10)
    }
  })
  
  observer.observe(el, {
    childList: true,
    attributes: true,
    attributeFilter: ['style'],
    subtree: true
  })
  
  return observer
}

// 水印指令
const watermark: Directive<HTMLElement, WatermarkOptions | string> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<WatermarkOptions | string>) {
    let options: Required<WatermarkOptions>
    
    if (typeof binding.value === 'string') {
      options = { ...defaultOptions, text: binding.value }
    } else {
      options = { ...defaultOptions, ...binding.value }
    }
    
    // 创建水印元素
    const watermarkEl = createWatermarkElement(options)
    
    // 确保容器是相对定位
    if (getComputedStyle(el).position === 'static') {
      el.style.position = 'relative'
    }
    
    el.appendChild(watermarkEl)
    
    // 创建防篡改观察器
    const observer = createMutationObserver(el)
    
    // 存储数据
    watermarkData.set(el, {
      watermarkEl,
      options,
      observer
    })
  },
  
  updated(el: HTMLElement, binding: DirectiveBinding<WatermarkOptions | string>) {
    let options: Required<WatermarkOptions>
    
    if (typeof binding.value === 'string') {
      options = { ...defaultOptions, text: binding.value }
    } else {
      options = { ...defaultOptions, ...binding.value }
    }
    
    updateWatermark(el, options)
  },
  
  unmounted(el: HTMLElement) {
    const data = watermarkData.get(el)
    if (data) {
      // 停止观察
      if (data.observer) {
        data.observer.disconnect()
      }
      
      // 移除水印元素
      if (data.watermarkEl.parentNode) {
        data.watermarkEl.parentNode.removeChild(data.watermarkEl)
      }
      
      watermarkData.delete(el)
    }
  }
}

// 全局水印函数
export const createGlobalWatermark = (options: WatermarkOptions = {}) => {
  const finalOptions: Required<WatermarkOptions> = { ...defaultOptions, ...options }
  
  // 移除已存在的全局水印
  const existingWatermark = document.querySelector('[data-global-watermark="true"]')
  if (existingWatermark) {
    existingWatermark.remove()
  }
  
  // 创建全局水印容器
  const watermarkContainer = document.createElement('div')
  watermarkContainer.setAttribute('data-global-watermark', 'true')
  watermarkContainer.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    pointer-events: none;
    z-index: ${finalOptions.zIndex};
  `
  
  // 创建水印元素
  const watermarkEl = createWatermarkElement(finalOptions)
  watermarkContainer.appendChild(watermarkEl)
  
  document.body.appendChild(watermarkContainer)
  
  // 防篡改保护
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.type === 'childList') {
        mutation.removedNodes.forEach((node) => {
          if (node === watermarkContainer) {
            // 水印被移除，重新创建
            setTimeout(() => {
              createGlobalWatermark(options)
            }, 10)
          }
        })
      }
    })
  })
  
  observer.observe(document.body, {
    childList: true
  })
  
  return {
    destroy: () => {
      observer.disconnect()
      if (watermarkContainer.parentNode) {
        watermarkContainer.parentNode.removeChild(watermarkContainer)
      }
    }
  }
}

// 移除全局水印
export const removeGlobalWatermark = () => {
  const watermark = document.querySelector('[data-global-watermark="true"]')
  if (watermark) {
    watermark.remove()
  }
}

export default watermark
export type { WatermarkOptions }
