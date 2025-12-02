// API相关类型定义

// 通用API响应结构
export interface ApiResponse<T = any> {
  code: number
  message?: string
  msg?: string
  success?: boolean
  data?: T
  rows?: T[]
  list?: T[]
  records?: T[]
  total?: number
  pageNum?: number
  pageSize?: number
  timestamp?: number
  traceId?: string
  [key: string]: unknown
}

// 分页响应结构
export interface PaginatedResponse<T = any> {
  items: T[]
  total: number
  page: number
  limit: number
  totalPages: number
  hasNext: boolean
  hasPrev: boolean
  rows?: T[]
  list?: T[]
  records?: T[]
  pageNum?: number
  pageSize?: number
}

// 错误响应结构
export interface ApiError {
  code: number
  message: string
  details?: Record<string, any>
  field?: string
  timestamp: number
  traceId?: string
}

// 文件上传响应
export interface UploadResponse {
  url: string
  filename: string
  originalName: string
  size: number
  mimeType: string
  hash?: string
}

// 批量操作响应
export interface BatchOperationResponse {
  successCount: number
  failureCount: number
  totalCount: number
  failures?: Array<{
    id: string
    error: string
  }>
}

// 验证码响应
export interface CaptchaResponse {
  captchaId: string
  captchaImage: string // base64
  expiresIn: number // seconds
}

// 统计数据响应
export interface StatsResponse {
  period: 'today' | 'week' | 'month' | 'year'
  data: Record<string, number>
  trend?: {
    direction: 'up' | 'down' | 'stable'
    percentage: number
  }
}

// 搜索响应
export interface SearchResponse<T = any> {
  results: T[]
  total: number
  took: number // ms
  query: string
  suggestions?: string[]
  facets?: Record<string, Array<{
    value: string
    count: number
  }>>
}

// 健康检查响应
export interface HealthCheckResponse {
  status: 'healthy' | 'unhealthy' | 'degraded'
  version: string
  uptime: number
  timestamp: number
  services: Record<string, {
    status: 'up' | 'down'
    responseTime?: number
    lastCheck: number
  }>
}

// 配置响应
export interface ConfigResponse {
  [key: string]: any
}

// 导出任务响应
export interface ExportTaskResponse {
  taskId: string
  status: 'pending' | 'processing' | 'completed' | 'failed'
  progress: number // 0-100
  downloadUrl?: string
  expiresAt?: string
  createdAt: string
}

// 导入任务响应
export interface ImportTaskResponse {
  taskId: string
  status: 'pending' | 'processing' | 'completed' | 'failed'
  progress: number // 0-100
  processedCount: number
  totalCount: number
  errors?: string[]
  createdAt: string
}

// 通知响应
export interface NotificationResponse {
  id: string
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  message: string
  data?: Record<string, any>
  isRead: boolean
  createdAt: string
}

// 系统信息响应
export interface SystemInfoResponse {
  version: string
  buildTime: string
  environment: 'development' | 'staging' | 'production'
  features: string[]
  limits: {
    maxFileSize: number
    maxUsers: number
    maxArticles: number
  }
}

// 请求配置
export interface RequestConfig {
  timeout?: number
  retries?: number
  retryDelay?: number
  showLoading?: boolean
  showError?: boolean
  showSuccess?: boolean
  successMessage?: string
  errorMessage?: string
  useCache?: boolean
  dedupe?: boolean
  headers?: Record<string, string>
}

// WebSocket消息类型
export interface WebSocketMessage<T = any> {
  type: string
  data: T
  timestamp: number
  id?: string
}

// 实时通知消息
export interface RealtimeNotification {
  type: 'notification' | 'message' | 'update'
  userId?: string
  tenantId?: string
  data: any
  timestamp: number
}

// 文件信息
export interface FileInfo {
  id: string
  filename: string
  originalName: string
  size: number
  mimeType: string
  url: string
  thumbnailUrl?: string
  hash: string
  uploadedBy: {
    id: string
    username: string
  }
  createdAt: string
}

// 缓存信息
export interface CacheInfo {
  key: string
  value: any
  ttl: number // seconds
  createdAt: number
  accessCount: number
  lastAccess: number
}

// 日志条目
export interface LogEntry {
  id: string
  level: 'debug' | 'info' | 'warn' | 'error'
  message: string
  data?: Record<string, any>
  source: string
  userId?: string
  tenantId?: string
  timestamp: number
}

// 性能指标
export interface PerformanceMetrics {
  responseTime: number
  throughput: number
  errorRate: number
  cpuUsage: number
  memoryUsage: number
  diskUsage: number
  activeConnections: number
  timestamp: number
}

// 备份信息
export interface BackupInfo {
  id: string
  filename: string
  size: number
  type: 'full' | 'incremental'
  status: 'pending' | 'processing' | 'completed' | 'failed'
  progress: number
  createdAt: string
  completedAt?: string
  downloadUrl?: string
  expiresAt?: string
}

// 任务队列信息
export interface QueueInfo {
  name: string
  waiting: number
  active: number
  completed: number
  failed: number
  delayed: number
  paused: boolean
}

// 数据库连接信息
export interface DatabaseInfo {
  type: string
  host: string
  database: string
  connected: boolean
  connectionCount: number
  maxConnections: number
  queryCount: number
  averageQueryTime: number
}
