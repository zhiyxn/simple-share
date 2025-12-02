import type { AxiosRequestConfig } from 'axios'
import type { RequestConfig } from '@/types'

// 请求去重机制
interface PendingRequest {
  config: AxiosRequestConfig
  resolve: (value: any) => void
  reject: (reason?: any) => void
}

class RequestDeduplication {
  private pendingRequests = new Map<string, PendingRequest[]>()

  // 生成请求唯一标识
  private generateRequestKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return `${method?.toUpperCase() || 'GET'}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
  }

  // 检查是否有重复请求
  private hasPendingRequest(key: string): boolean {
    return this.pendingRequests.has(key)
  }

  // 添加待处理请求
  public addPendingRequest(config: AxiosRequestConfig): Promise<any> | null {
    const key = this.generateRequestKey(config)

    if (this.hasPendingRequest(key)) {
      const pendingRequests = this.pendingRequests.get(key)!
      return new Promise((resolve, reject) => {
        pendingRequests.push({ config, resolve, reject })
      })
    }

    return null
  }

  // 开始处理请求
  public startRequest(config: AxiosRequestConfig, promise: Promise<any>): void {
    const key = this.generateRequestKey(config)

    promise
      .then((response) => {
        this.resolvePendingRequests(key, response)
      })
      .catch((error) => {
        this.rejectPendingRequests(key, error)
      })
      .finally(() => {
        this.removePendingRequest(key)
      })

    this.pendingRequests.set(key, [{ config, resolve: () => {}, reject: () => {} }])
  }

  // 解析所有待处理请求
  private resolvePendingRequests(key: string, response: any): void {
    const pendingRequests = this.pendingRequests.get(key)
    if (pendingRequests) {
      pendingRequests.forEach(request => {
        request.resolve(response)
      })
    }
  }

  // 拒绝所有待处理请求
  private rejectPendingRequests(key: string, error: any): void {
    const pendingRequests = this.pendingRequests.get(key)
    if (pendingRequests) {
      pendingRequests.forEach(request => {
        request.reject(error)
      })
    }
  }

  // 移除待处理请求
  private removePendingRequest(key: string): void {
    this.pendingRequests.delete(key)
  }

  // 清除所有待处理请求
  public clearAll(): void {
    this.pendingRequests.clear()
  }
}

export const requestDeduplication = new RequestDeduplication()

// 请求缓存机制
interface CacheItem {
  data: any
  timestamp: number
}

class RequestCache {
  private cache = new Map<string, CacheItem>()
  private defaultTTL = 5 * 60 * 1000 // 5分钟缓存

  private generateRequestKey(config: AxiosRequestConfig): string {
    const { method, url, params, data } = config
    return `${method?.toUpperCase() || 'GET'}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
  }

  private generateCacheKey(config: AxiosRequestConfig): string {
    return this.generateRequestKey(config)
  }

  public get(config: AxiosRequestConfig): any | null {
    const key = this.generateCacheKey(config)
    const item = this.cache.get(key)

    if (item && Date.now() - item.timestamp < this.defaultTTL) {
      return item.data
    }

    if (item) {
      this.cache.delete(key)
    }

    return null
  }

  public set(config: AxiosRequestConfig, data: any): void {
    const key = this.generateCacheKey(config)
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    })
  }

  public clear(config: AxiosRequestConfig): void {
    const key = this.generateCacheKey(config)
    this.cache.delete(key)
  }

  public clearAll(): void {
    this.cache.clear()
  }

  public setTTL(ttl: number): void {
    this.defaultTTL = ttl
  }
}

export const requestCache = new RequestCache()

const CACHE_HIT_FLAG = '__cacheHit__'
const CACHE_DATA_KEY = '__cacheData__'

export const createRequestInterceptor = () => {
  return {
    onRequest: (config: AxiosRequestConfig) => {
      const requestConfig = config.metadata as RequestConfig | undefined
      const enableCache = requestConfig?.useCache === true
      const enableDedupe = requestConfig?.dedupe === true

      if (enableCache) {
        const cachedData = requestCache.get(config)
        if (cachedData) {
          (config as any)[CACHE_HIT_FLAG] = true
          (config as any)[CACHE_DATA_KEY] = cachedData
          return config
        }
      }

      if (enableDedupe) {
        const duplicateRequest = requestDeduplication.addPendingRequest(config)
        if (duplicateRequest) {
          return Promise.reject({ config, isDuplicate: true, promise: duplicateRequest })
        }
      }

      return config
    },

    onResponse: (response: any) => {
      const config = response.config
      const requestConfig = config.metadata as RequestConfig | undefined
      const enableCache = requestConfig?.useCache === true
      const enableDedupe = requestConfig?.dedupe === true

      if (enableCache && (config as any)[CACHE_HIT_FLAG]) {
        return {
          ...response,
          data: (config as any)[CACHE_DATA_KEY],
          cached: true
        }
      }

      if (enableCache && config.method?.toLowerCase() === 'get') {
        requestCache.set(config, response.data)
      }

      if (enableDedupe) {
        requestDeduplication.startRequest(config, Promise.resolve(response))
      }

      return response
    },

    onResponseError: (error: any) => {
      const config = error.config
      const requestConfig = config?.metadata as RequestConfig | undefined
      const enableDedupe = requestConfig?.dedupe === true

      if (error.isDuplicate) {
        return error.promise
      }

      if (enableDedupe && config) {
        requestDeduplication.startRequest(config, Promise.reject(error))
      }

      return Promise.reject(error)
    }
  }
}
