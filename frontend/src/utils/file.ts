/**
 * 文件相关工具函数
 */

import { service } from '@/api/request'
import { fileConfigApi } from '@/api/infra'
import { ensureAbsoluteUrl } from '@/utils/url'
import type { AxiosProgressEvent } from 'axios'

// 缓存文件配置信息
let cachedFileConfig: any = null
let configPromise: Promise<any> | null = null

/**
 * 获取主文件配置
 */
export async function getMasterFileConfig() {
  if (cachedFileConfig) {
    return cachedFileConfig
  }

  if (configPromise) {
    return configPromise
  }

  configPromise = (async () => {
    try {
      const response = await fileConfigApi.getMasterConfig()

      const hasWrappedResponse =
        response && typeof response === 'object' && 'code' in response && 'data' in response
      const configData = hasWrappedResponse ? (response as any).data : response

      if (configData) {
        // 创建配置副本以避免修改原始数据
        const configCopy = { ...configData }

        // 记录从后端获取的 domain 值（用于调试）
        console.log('从后端获取的完整配置:', configCopy)

        // 过滤敏感信息 - 只处理 config 字段中的内容
        if (configCopy.config) {
          try {
            const parsedConfig =
              typeof configCopy.config === 'string'
                ? JSON.parse(configCopy.config)
                : configCopy.config

            const filteredConfig = {
              domain: parsedConfig?.domain,
              endpoint: parsedConfig?.endpoint,
              bucket: parsedConfig?.bucket
            }

            // 明确不包含 accessKey、secretKey 等敏感信息
            configCopy.config = JSON.stringify(filteredConfig)
          } catch (error) {
            console.error('解析并过滤配置失败', error)
          }
        }

        cachedFileConfig = configCopy
        return cachedFileConfig
      }
    } catch (error) {
      console.error('获取主文件配置失败', error)
    }
    return null
  })()

  return configPromise
}

/**
 * 解析文件配置 JSON - 仅返回非敏感信息
 */
export function parseFileConfig(configValue: string | Record<string, any>) {
  try {
    const fullConfig =
      typeof configValue === 'string' ? JSON.parse(configValue) : configValue ?? {}

    return {
      domain: fullConfig?.domain,
      endpoint: fullConfig?.endpoint,
      bucket: fullConfig?.bucket
    }
  } catch (error) {
    console.error('解析文件配置失败:', error)
    return {}
  }
}

function ensureDomainProtocol(domain?: string | null): string | undefined {
  if (!domain) {
    return undefined
  }

  const trimmed = domain.trim()
  if (!trimmed) {
    return undefined
  }

  const runtimeProtocol = typeof window !== 'undefined' && window.location?.protocol
    ? window.location.protocol
    : 'https:'

  if (trimmed.startsWith('//')) {
    return `${runtimeProtocol}${trimmed}`
  }

  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed
  }

  return `${runtimeProtocol}//${trimmed}`
}

/**
 * 构建文件访问 URL
 * @param filePath 文件路径
 * @param domain 可选的域名，如果不提供则从配置中获取
 */
export async function buildFileUrl(filePath: string, domain?: string): Promise<string> {
  console.log('🔍 buildFileUrl - 原始输入 filePath:', filePath)

  if (!filePath) return ''

  if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
    console.log('🔗 buildFileUrl - 文件路径已是完整 URL，尝试标准化后返回')
    return ensureAbsoluteUrl(filePath)
  }

  // 处理 /api/upload/upload 路径，转换为正确的相对路径
  if (filePath.startsWith('/api/upload/upload/')) {
    const originalPath = filePath
    filePath = filePath.replace('/api/upload/upload/', 'upload/')
    console.log('🔄 buildFileUrl - 转换 /api/upload/upload 路径:', {
      原始: originalPath,
      转换后: filePath
    })
  }
  // 处理 /api/upload 路径，转换为正确的相对路径（注意在前面条件之后）
  else if (filePath.startsWith('/api/upload/') && !filePath.startsWith('/api/upload/upload/')) {
    const originalPath = filePath
    filePath = filePath.replace('/api/upload/', 'upload/')
    console.log('🔄 buildFileUrl - 转换 /api/upload 路径:', {
      原始: originalPath,
      转换后: filePath
    })
  } else {
    console.log('📝 buildFileUrl - 路径无需转换:', filePath)
  }

  let finalDomain = domain
  let parsedConfig: Record<string, any> | null = null

  if (!finalDomain) {
    const config = await getMasterFileConfig()
    if (config) {
      finalDomain = config.domain

      if (config.config) {
        parsedConfig = parseFileConfig(config.config) as Record<string, any>
        if (!finalDomain) {
          finalDomain = parsedConfig?.domain
        }
      }

      console.log('buildFileUrl 获取域名:', {
        topLevelDomain: config.domain,
        configJsonDomain: parsedConfig?.domain,
        providedDomain: domain,
        finalDomain
      })
    }

    if (!finalDomain) {
      const envDomain =
        typeof import.meta !== 'undefined' && import.meta.env
          ? import.meta.env.VITE_FILE_BASE_URL
          : undefined
      if (envDomain && typeof envDomain === 'string' && envDomain.trim()) {
        finalDomain = envDomain.trim()
        console.log('buildFileUrl 使用环境变量域名:', finalDomain)
      }
    }
  }

  const domainWithProtocol = ensureDomainProtocol(finalDomain)

  // 打印获取到的 domain 信息，用于调试
  console.log('获取到的 domain 配置:', {
    raw: finalDomain,
    withProtocol: domainWithProtocol
  })

  const normalizedDomain = domainWithProtocol?.endsWith('/')
    ? domainWithProtocol.slice(0, -1)
    : domainWithProtocol
  const normalizedPath = filePath.startsWith('/') ? filePath : `/${filePath}`

  let resultUrl = ''

  if (!normalizedDomain) {
    console.warn('未配置文件访问域名，使用相对路径')
    resultUrl = normalizedPath
  } else {
    resultUrl = `${normalizedDomain}${normalizedPath}`
  }

  // 打印拼接的路径，用于调试
  console.log('构建文件 URL:', {
    filePath,
    domain,
    finalDomain,
    domainWithProtocol,
    normalizedDomain,
    normalizedPath,
    resultUrl
  })

  if (normalizedDomain) {
    return ensureAbsoluteUrl(resultUrl)
  }

  return resultUrl
}

/**
 * 构建文件预览 URL
 * @param fileId 文件 ID
 * @param filePath 文件路径（可选）
 * @param domain 可选的域名
 */
export async function buildPreviewUrl(
  fileId: string | number,
  filePath?: string,
  domain?: string
): Promise<string> {
  // 如果有 filePath，优先使用 domain + path 的方式
  if (filePath) {
    const url = await buildFileUrl(filePath, domain)
    if (url && !url.startsWith('/')) {
      return url
    }
  }

  // 否则使用 API 预览接口
  const apiPrefix = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${apiPrefix}/infra/file/preview/${fileId}`
}

/**
 * 构建文件下载 URL
 * @param fileId 文件 ID
 */
export function buildDownloadUrl(fileId: string | number): string {
  const apiPrefix = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${apiPrefix}/infra/file/download/${fileId}`
}

/**
 * 格式化文件大小
 * @param size 文件大小（字节）
 */
export function formatFileSize(size: number): string {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(2)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

/**
 * 获取文件类型标签类型
 * @param type 文件类型
 */
export function getFileTypeTagType(type: string): string {
  const typeMap: Record<string, string> = {
    image: 'success',
    document: 'info',
    video: 'warning',
    audio: 'primary',
    other: 'info'
  }
  return typeMap[type] || 'info'
}

/**
 * 获取文件类型标签文本
 * @param type 文件类型
 */
export function getFileTypeLabel(type: string): string {
  const typeMap: Record<string, string> = {
    image: '图片',
    document: '文档',
    video: '视频',
    audio: '音频',
    other: '其他'
  }
  return typeMap[type] || '未知'
}

/**
 * 清除文件配置缓存
 */
export function clearFileConfigCache() {
  cachedFileConfig = null
  configPromise = null
}

export interface UploadedFileInfo {
  id?: number
  name: string
  path?: string
  url: string
  type?: string
  size?: number
  uid?: string
  raw?: Record<string, any>
}

interface UploadOptions {
  onProgress?: (progress: number) => void
  signal?: AbortSignal
}

function toPercentage(event: AxiosProgressEvent): number {
  if (!event.total) {
    return event.loaded ? Math.min(99, Math.round(event.loaded / 1024)) : 0
  }
  return Math.round((event.loaded / event.total) * 100)
}

export async function uploadInfraFile(
  file: File,
  options: UploadOptions = {}
): Promise<UploadedFileInfo> {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await service.request({
      url: '/infra/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      signal: options.signal,
      onUploadProgress: (event: AxiosProgressEvent) => {
        if (options.onProgress) {
          options.onProgress(toPercentage(event))
        }
      }
    })

    const fileRecord = response ?? {}
    const rawPath = fileRecord.path || fileRecord.url || ''
    const finalUrl = rawPath ? await buildFileUrl(rawPath) : ''

    return {
      id: fileRecord.id,
      name: fileRecord.name || file.name,
      path: fileRecord.path,
      url: finalUrl || rawPath,
      type: fileRecord.type,
      size: fileRecord.size,
      raw: fileRecord
    }
  } catch (error) {
    options.onProgress?.(0)
    throw error
  }
}
