// 类型定义统一导出

// API相关类型
export * from './api'

// 用户相关类型
export * from './user'

// 文章相关类型
export * from './article'

// 分类相关类型
export * from './category'

// AiTech 模块
export * from './aitech'

// 租户相关类型
export * from './tenant'

// 操作日志类型
export * from './operation-log'

// 通用类型定义
export interface BaseEntity {
  id: string
  createdAt: string
  updatedAt: string
}

// 分页参数
export interface PaginationParams {
  page?: number
  limit?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

// 搜索参数
export interface SearchParams {
  keyword?: string
  filters?: Record<string, any>
}

// 时间范围参数
export interface DateRangeParams {
  startDate?: string
  endDate?: string
}

// 选项接口
export interface Option {
  label: string
  value: string | number
  disabled?: boolean
  children?: Option[]
}

// 树形节点接口
export interface TreeNode {
  id: string
  label: string
  children?: TreeNode[]
  disabled?: boolean
  expanded?: boolean
  selected?: boolean
}

// 表单验证规则
export interface ValidationRule {
  required?: boolean
  min?: number
  max?: number
  pattern?: RegExp
  validator?: (value: any) => boolean | string
  message?: string
}

// 表单字段配置
export interface FormField {
  name: string
  label: string
  type: 'text' | 'email' | 'password' | 'number' | 'textarea' | 'select' | 'checkbox' | 'radio' | 'date' | 'file'
  placeholder?: string
  defaultValue?: any
  options?: Option[]
  rules?: ValidationRule[]
  disabled?: boolean
  hidden?: boolean
  span?: number
}

// 表格列配置
export interface TableColumn {
  key: string
  title: string
  dataIndex?: string
  width?: number | string
  align?: 'left' | 'center' | 'right'
  sortable?: boolean
  filterable?: boolean
  fixed?: 'left' | 'right'
  render?: (value: any, record: any, index: number) => any
}

// 菜单项接口
export interface MenuItem {
  id: string
  title: string
  icon?: string
  path?: string
  component?: string
  redirect?: string
  children?: MenuItem[]
  meta?: {
    requiresAuth?: boolean
    roles?: string[]
    permissions?: string[]
    hidden?: boolean
    keepAlive?: boolean
    title?: string
    icon?: string
  }
}

// 路由元信息
export interface RouteMeta {
  title?: string
  icon?: string
  requiresAuth?: boolean
  roles?: string[]
  permissions?: string[]
  hidden?: boolean
  keepAlive?: boolean
  breadcrumb?: boolean
  activeMenu?: string
}

// 面包屑项
export interface BreadcrumbItem {
  title: string
  path?: string
  icon?: string
}

// 通知项
export interface NotificationItem {
  id: string
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  message: string
  duration?: number
  showClose?: boolean
  onClick?: () => void
  onClose?: () => void
}

// 主题配置
export interface ThemeConfig {
  mode: 'light' | 'dark' | 'auto'
  primaryColor: string
  borderRadius: number
  fontSize: number
  fontFamily: string
}

// 布局配置
export interface LayoutConfig {
  sidebarCollapsed: boolean
  sidebarWidth: number
  headerHeight: number
  footerHeight: number
  showBreadcrumb: boolean
  showTabs: boolean
  showFooter: boolean
}

// 应用配置
export interface AppConfig {
  name: string
  version: string
  description: string
  author: string
  homepage: string
  repository: string
  license: string
  theme: ThemeConfig
  layout: LayoutConfig
}

// 环境变量
export interface EnvConfig {
  NODE_ENV: 'development' | 'production' | 'test'
  VITE_API_BASE_URL: string
  VITE_APP_TITLE: string
  VITE_APP_VERSION: string
  VITE_ENABLE_MOCK: boolean
  VITE_ENABLE_DEVTOOLS: boolean
}

// 错误信息
export interface ErrorInfo {
  code: string | number
  message: string
  stack?: string
  timestamp: number
  url?: string
  userAgent?: string
}

// 加载状态
export interface LoadingState {
  loading: boolean
  error?: ErrorInfo
  lastUpdated?: number
}

// 操作结果
export interface OperationResult<T = any> {
  success: boolean
  data?: T
  error?: ErrorInfo
  message?: string
}

// 文件上传状态
export interface UploadStatus {
  status: 'pending' | 'uploading' | 'success' | 'error'
  progress: number
  file: File
  response?: any
  error?: string
}

// 键值对
export interface KeyValue<T = any> {
  key: string
  value: T
}

// 坐标点
export interface Point {
  x: number
  y: number
}

// 尺寸
export interface Size {
  width: number
  height: number
}

// 矩形区域
export interface Rect extends Point, Size {}

// 颜色值
export type Color = string

// 事件处理器
export type EventHandler<T = any> = (event: T) => void

// 异步函数
export type AsyncFunction<T = any, R = any> = (...args: T[]) => Promise<R>

// 可选的异步函数
export type MaybeAsyncFunction<T = any, R = any> = (...args: T[]) => R | Promise<R>
