/**
 * 调试工具函数
 */

// 日志级别
export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3
}

// 当前日志级别
const currentLogLevel = import.meta.env.VITE_LOG_LEVEL === 'debug' ? LogLevel.DEBUG : LogLevel.INFO

/**
 * 格式化日志输出
 */
class Logger {
  private static formatMessage(level: string, message: string, ...args: any[]) {
    const timestamp = new Date().toLocaleTimeString()
    const prefix = `[${timestamp}] [${level}]`
    return [prefix, message, ...args]
  }

  static debug(message: string, ...args: any[]) {
    if (currentLogLevel <= LogLevel.DEBUG) {
      console.log(...this.formatMessage('DEBUG', message), ...args)
    }
  }

  static info(message: string, ...args: any[]) {
    if (currentLogLevel <= LogLevel.INFO) {
      console.info(...this.formatMessage('INFO', message), ...args)
    }
  }

  static warn(message: string, ...args: any[]) {
    if (currentLogLevel <= LogLevel.WARN) {
      console.warn(...this.formatMessage('WARN', message), ...args)
    }
  }

  static error(message: string, ...args: any[]) {
    if (currentLogLevel <= LogLevel.ERROR) {
      console.error(...this.formatMessage('ERROR', message), ...args)
    }
  }

  static group(title: string) {
    console.group(`🔍 ${title}`)
  }

  static groupEnd() {
    console.groupEnd()
  }
}

/**
 * 性能监控工具
 */
export class PerformanceMonitor {
  private static marks: Map<string, number> = new Map()

  static mark(name: string) {
    this.marks.set(name, performance.now())
    Logger.debug(`Performance mark: ${name}`)
  }

  static measure(name: string, startMark?: string) {
    const endTime = performance.now()
    const startTime = startMark ? this.marks.get(startMark) : this.marks.get(name)
    
    if (startTime) {
      const duration = endTime - startTime
      Logger.info(`Performance measure: ${name} took ${duration.toFixed(2)}ms`)
      return duration
    }
    
    Logger.warn(`Performance measure: Start mark "${startMark || name}" not found`)
    return 0
  }

  static clearMarks() {
    this.marks.clear()
    Logger.debug('Performance marks cleared')
  }

  static getMemoryInfo() {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      return {
        usedJSHeapSize: Math.round(memory.usedJSHeapSize / 1024 / 1024),
        totalJSHeapSize: Math.round(memory.totalJSHeapSize / 1024 / 1024),
        jsHeapSizeLimit: Math.round(memory.jsHeapSizeLimit / 1024 / 1024)
      }
    }
    return null
  }
}

/**
 * 网络请求监控
 */
export class NetworkMonitor {
  private static requests: Array<{
    url: string
    method: string
    status: number
    duration: number
    timestamp: Date
    size?: number
  }> = []

  static addRequest(request: {
    url: string
    method: string
    status: number
    duration: number
    size?: number
  }) {
    this.requests.unshift({
      ...request,
      timestamp: new Date()
    })

    // 只保留最近100个请求
    if (this.requests.length > 100) {
      this.requests = this.requests.slice(0, 100)
    }

    Logger.debug(`Network request: ${request.method} ${request.url} - ${request.status} (${request.duration}ms)`)
  }

  static getRequests() {
    return [...this.requests]
  }

  static clearRequests() {
    this.requests = []
    Logger.debug('Network requests cleared')
  }

  static getStats() {
    const total = this.requests.length
    const successful = this.requests.filter(r => r.status >= 200 && r.status < 300).length
    const failed = this.requests.filter(r => r.status >= 400).length
    const avgDuration = total > 0 ? this.requests.reduce((sum, r) => sum + r.duration, 0) / total : 0

    return {
      total,
      successful,
      failed,
      successRate: total > 0 ? (successful / total * 100).toFixed(1) : '0',
      avgDuration: avgDuration.toFixed(2)
    }
  }
}

/**
 * 错误监控
 */
export class ErrorMonitor {
  private static errors: Array<{
    message: string
    stack?: string
    timestamp: Date
    type: 'javascript' | 'promise' | 'resource'
    url?: string
  }> = []

  static addError(error: {
    message: string
    stack?: string
    type: 'javascript' | 'promise' | 'resource'
    url?: string
  }) {
    this.errors.unshift({
      ...error,
      timestamp: new Date()
    })

    // 只保留最近50个错误
    if (this.errors.length > 50) {
      this.errors = this.errors.slice(0, 50)
    }

    Logger.error(`${error.type} error: ${error.message}`, error.stack)
  }

  static getErrors() {
    return [...this.errors]
  }

  static clearErrors() {
    this.errors = []
    Logger.debug('Errors cleared')
  }

  static getStats() {
    const total = this.errors.length
    const byType = this.errors.reduce((acc, error) => {
      acc[error.type] = (acc[error.type] || 0) + 1
      return acc
    }, {} as Record<string, number>)

    return {
      total,
      byType
    }
  }
}

/**
 * 存储调试工具
 */
export class StorageDebugger {
  static inspectLocalStorage() {
    Logger.group('LocalStorage Contents')
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key) {
        const value = localStorage.getItem(key)
        try {
          const parsed = JSON.parse(value || '')
          Logger.info(`${key}:`, parsed)
        } catch {
          Logger.info(`${key}:`, value)
        }
      }
    }
    Logger.groupEnd()
  }

  static inspectSessionStorage() {
    Logger.group('SessionStorage Contents')
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i)
      if (key) {
        const value = sessionStorage.getItem(key)
        try {
          const parsed = JSON.parse(value || '')
          Logger.info(`${key}:`, parsed)
        } catch {
          Logger.info(`${key}:`, value)
        }
      }
    }
    Logger.groupEnd()
  }

  static getStorageSize() {
    const localStorageSize = new Blob(Object.values(localStorage)).size
    const sessionStorageSize = new Blob(Object.values(sessionStorage)).size
    
    return {
      localStorage: Math.round(localStorageSize / 1024),
      sessionStorage: Math.round(sessionStorageSize / 1024)
    }
  }
}

/**
 * 组件调试工具
 */
export class ComponentDebugger {
  static logComponentMount(componentName: string, props?: any) {
    Logger.debug(`Component mounted: ${componentName}`, props)
  }

  static logComponentUnmount(componentName: string) {
    Logger.debug(`Component unmounted: ${componentName}`)
  }

  static logComponentUpdate(componentName: string, oldProps?: any, newProps?: any) {
    Logger.debug(`Component updated: ${componentName}`, { oldProps, newProps })
  }

  static logComponentError(componentName: string, error: Error) {
    Logger.error(`Component error in ${componentName}:`, error)
  }
}

/**
 * 路由调试工具
 */
export class RouteDebugger {
  static logRouteChange(from: string, to: string) {
    Logger.info(`Route change: ${from} → ${to}`)
  }

  static logRouteError(route: string, error: Error) {
    Logger.error(`Route error for ${route}:`, error)
  }
}

/**
 * 全局调试工具
 */
export const debugTools = {
  logger: Logger,
  performance: PerformanceMonitor,
  network: NetworkMonitor,
  errorMonitor: ErrorMonitor,
  storage: StorageDebugger,
  component: ComponentDebugger,
  route: RouteDebugger,

  // 便捷方法
  log: Logger.info,
  debug: Logger.debug,
  warn: Logger.warn,
  error: Logger.error,

  // 环境信息
  getEnvironmentInfo() {
    return {
      mode: import.meta.env.MODE,
      dev: import.meta.env.DEV,
      prod: import.meta.env.PROD,
      apiBaseUrl: import.meta.env.VITE_API_BASE_URL,
      debugMode: import.meta.env.VITE_DEBUG_MODE,
      userAgent: navigator.userAgent,
      platform: navigator.platform,
      language: navigator.language,
      cookieEnabled: navigator.cookieEnabled,
      onLine: navigator.onLine,
      screenResolution: `${screen.width}x${screen.height}`,
      viewportSize: `${window.innerWidth}x${window.innerHeight}`,
      colorDepth: screen.colorDepth,
      pixelRatio: window.devicePixelRatio
    }
  },

  // 初始化调试工具
  init() {
    if (import.meta.env.DEV || import.meta.env.VITE_DEBUG_MODE === 'true') {
      // 将调试工具挂载到全局
      (window as any).debugTools = this

      // 设置全局错误监听
      window.addEventListener('error', (event) => {
        ErrorMonitor.addError({
          message: event.message,
          stack: event.error?.stack,
          type: 'javascript',
          url: event.filename
        })
      })

      window.addEventListener('unhandledrejection', (event) => {
        ErrorMonitor.addError({
          message: event.reason?.message || String(event.reason),
          stack: event.reason?.stack,
          type: 'promise'
        })
      })

      Logger.info('Debug tools initialized')
      Logger.info('Environment info:', this.getEnvironmentInfo())
    }
  }
}

// 自动初始化
debugTools.init()

export default debugTools