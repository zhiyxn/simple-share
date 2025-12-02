import axios from 'axios'
import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  AxiosRequestHeaders,
  InternalAxiosRequestConfig,
  AxiosRequestTransformer
} from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse, RequestConfig } from '@/types'
import router from '@/router'
import { createRequestInterceptor } from '@/utils/requestDeduplication'

type UserStore = ReturnType<(typeof import('@/stores/user'))['useUserStore']>
type TenantStore = ReturnType<(typeof import('@/stores/tenant'))['useTenantStore']>

let cachedUserStore: UserStore | null = null
const getUserStore = async (): Promise<UserStore> => {
  if (!cachedUserStore) {
    const module = await import('@/stores/user')
    cachedUserStore = module.useUserStore()
  }
  return cachedUserStore
}

let cachedTenantStore: TenantStore | null = null
const getTenantStore = async (): Promise<TenantStore> => {
  if (!cachedTenantStore) {
    const module = await import('@/stores/tenant')
    cachedTenantStore = module.useTenantStore()
  }
  return cachedTenantStore
}

const axiosDefaultTransformers = Array.isArray(axios.defaults.transformRequest)
  ? [...axios.defaults.transformRequest]
  : axios.defaults.transformRequest
    ? [axios.defaults.transformRequest]
    : []

const defaultAxiosConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  },
  transformRequest: [
    ...axiosDefaultTransformers,
    ((data: unknown, headers?: AxiosRequestHeaders) =>
      normalizeJsonRequestPayload(data, headers)) as AxiosRequestTransformer
  ]
}

const service: AxiosInstance = axios.create(defaultAxiosConfig)

const { onRequest, onResponse, onResponseError } = createRequestInterceptor()

let redirectingToLogin = false

const redirectToLogin = () => {
  if (redirectingToLogin || router.currentRoute.value.path.startsWith('/auth')) {
    return
  }

  redirectingToLogin = true
  router
    .push('/auth/login')
    .catch(() => {})
    .finally(() => {
      redirectingToLogin = false
    })
}

service.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    normalizeRequestUrl(config)

    const intercepted = onRequest(config as AxiosRequestConfig)
    if (intercepted && typeof (intercepted as PromiseLike<unknown>).then === 'function') {
      return intercepted as Promise<InternalAxiosRequestConfig>
    }

    const [userStore, tenantStore] = await Promise.all([getUserStore(), getTenantStore()])

    const headers: AxiosRequestHeaders =
      (config.headers as AxiosRequestHeaders | undefined) ?? ({} as AxiosRequestHeaders)
    config.headers = headers

    if (userStore.token) {
      headers.Authorization = `Bearer ${userStore.token}`
    }

    if (tenantStore.tenantId) {
      headers['X-Tenant-ID'] = tenantStore.tenantId
    }

    headers['X-Request-ID'] = generateRequestId()

    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  async (response: AxiosResponse<ApiResponse>) => {
    const handled = onResponse(response)
    if (handled !== response) {
      return handled
    }

    const apiData = (response.data ?? {}) as ApiResponse<any>
    const requestMeta = response.config.metadata as RequestConfig | undefined
    const statusCode = apiData.code ?? response.status ?? 200

    if (statusCode === 401) {
      const userStore = await getUserStore()
      const message = apiData.message || apiData.msg || '登录已过期，请重新登录'
      const hadToken = !!userStore.token

      userStore.clearAuth()
      redirectToLogin()

      if (hadToken) {
        ElMessage.warning(message)
      }

      return Promise.reject(new Error(message))
    }

    if (statusCode === 200) {
      if (requestMeta?.showSuccess && requestMeta.successMessage) {
        ElMessage.success(requestMeta.successMessage)
      }

      const fallbackRows =
        apiData.rows ?? apiData.list ?? apiData.records ?? (Array.isArray(apiData.data) ? apiData.data : undefined)

      if (Array.isArray(fallbackRows) && apiData.total !== undefined) {
        const total = Number(apiData.total) || fallbackRows.length
        const rawLimit =
          apiData.pageSize ?? apiData.limit ?? (fallbackRows.length > 0 ? fallbackRows.length : undefined)
        const limit = Number(rawLimit) || fallbackRows.length || 1
        const page = Number(apiData.pageNum ?? apiData.page ?? 1) || 1
        const totalPages = limit > 0 ? Math.ceil(total / limit) : 1

        return {
          items: fallbackRows,
          rows: fallbackRows,
          list: fallbackRows,
          records: fallbackRows,
          total,
          page,
          pageNum: page,
          limit,
          pageSize: limit,
          totalPages,
          hasNext: typeof apiData.hasNext === 'boolean' ? apiData.hasNext : page < totalPages,
          hasPrev: typeof apiData.hasPrev === 'boolean' ? apiData.hasPrev : page > 1
        }
      }

      if (apiData.data !== undefined) {
        return apiData.data as any
      }

      if (Array.isArray(fallbackRows)) {
        return fallbackRows as any
      }

      return apiData as any
    }

    const errorMessage = apiData.message || apiData.msg || '请求失败'

    if (requestMeta?.showError !== false) {
      if (
        response.status === 401 &&
        (response.config.url?.includes('/auth/logout') || response.config.url?.includes('/auth/me'))
      ) {
        // 这些是系统自动调用的认证接口，失败时不重复提示
      } else {
        ElMessage.error(requestMeta?.errorMessage || errorMessage)
      }
    }

    return Promise.reject(new Error(errorMessage))
  },
  async (error) => {
    const dedupeError = onResponseError(error)
    if (dedupeError !== error) {
      return dedupeError
    }

    const { response, config } = error
    const requestMeta = config?.metadata as RequestConfig | undefined
    let errorMessage = '网络错误'

    if (response) {
      const { status, data } = response

      switch (status) {
        case 400:
          errorMessage = data?.message || '请求参数错误'
          break
        case 401: {
          errorMessage = '未授权，请重新登录'
          const userStore = await getUserStore()

          if (config.url?.includes('/auth/logout') || config.url?.includes('/auth/refresh')) {
            userStore.clearAuth()
            if (!router.currentRoute.value.path.startsWith('/auth')) {
              router.push('/auth/login')
            }
            break
          }

          try {
            await userStore.refreshToken()
            return service(config)
          } catch (refreshError) {
            await userStore.logout(false)
            if (!router.currentRoute.value.path.startsWith('/auth')) {
              router.push('/auth/login')
            }
          }
          break
        }
        case 403:
          errorMessage = '拒绝访问'
          break
        case 404:
          errorMessage = '请求的资源不存在'
          break
        case 422:
          errorMessage = data?.message || '数据验证失败'
          break
        case 429:
          errorMessage = '请求过于频繁，请稍后再试'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        case 502:
          errorMessage = '网关错误'
          break
        case 503:
          errorMessage = '服务不可用'
          break
        case 504:
          errorMessage = '网关超时'
          break
        default:
          errorMessage = data?.message || `请求失败 (${status})`
      }
    }

    if (requestMeta?.showError !== false) {
      ElMessage.error(requestMeta?.errorMessage || errorMessage)
    }

    return Promise.reject(error)
  }
)

function generateRequestId(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 11)}`
}

type RequestOptions = (RequestConfig & AxiosRequestConfig) | undefined

interface RequestExecutor {
  <T = any>(config: AxiosRequestConfig & { metadata?: RequestConfig }): Promise<T>
  get<T = any>(url: string, params?: any, config?: RequestOptions): Promise<T>
  post<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T>
  put<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T>
  patch<T = any>(url: string, data?: any, config?: RequestOptions): Promise<T>
  delete<T = any>(url: string, params?: any, config?: RequestOptions): Promise<T>
  upload<T = any>(
    url: string,
    file: File,
    config?: (RequestConfig & AxiosRequestConfig) & {
      onProgress?: (progress: number) => void
    }
  ): Promise<T>
  download(url: string, filename?: string, params?: any, config?: RequestOptions): Promise<void>
  all<T = any>(requests: Promise<T>[]): Promise<T[]>
  retry<T = any>(requestFn: () => Promise<T>, maxRetries?: number, delay?: number): Promise<T>
}

const REQUEST_METADATA_FIELDS: Array<keyof RequestConfig> = [
  'retries',
  'retryDelay',
  'showLoading',
  'showError',
  'showSuccess',
  'successMessage',
  'errorMessage',
  'useCache',
  'dedupe'
]

function isPlainObject(value: unknown): value is Record<string, any> {
  return Object.prototype.toString.call(value) === '[object Object]'
}

const AXIOS_CONFIG_FIELDS = new Set([
  'url',
  'method',
  'baseURL',
  'headers',
  'params',
  'data',
  'timeout',
  'withCredentials',
  'auth',
  'responseType',
  'responseEncoding',
  'xsrfCookieName',
  'xsrfHeaderName',
  'onUploadProgress',
  'onDownloadProgress',
  'paramsSerializer',
  'cancelToken',
  'signal',
  'transformRequest',
  'transformResponse',
  'metadata',
  'retries',
  'retryDelay',
  'showLoading',
  'showError',
  'showSuccess',
  'successMessage',
  'errorMessage',
  'useCache',
  'dedupe'
])

function splitRequestOptions(options?: RequestOptions) {
  if (!options || typeof options !== 'object') {
    return {
      axiosConfig: undefined as AxiosRequestConfig | undefined,
      metadata: undefined as RequestConfig | undefined
    }
  }

  const cloned = { ...(options as Record<string, any>) }
  const metadata: Record<string, any> = {}

  if (cloned.metadata && typeof cloned.metadata === 'object') {
    Object.assign(metadata, cloned.metadata)
  }
  delete cloned.metadata

  for (const field of REQUEST_METADATA_FIELDS) {
    if (field in cloned) {
      metadata[field] = cloned[field]
      delete cloned[field]
    }
  }

  const axiosConfig = Object.keys(cloned).length > 0 ? (cloned as AxiosRequestConfig) : undefined
  const finalMetadata = Object.keys(metadata).length > 0 ? (metadata as RequestConfig) : undefined

  return { axiosConfig, metadata: finalMetadata }
}

function mergeAxiosConfig(
  baseConfig?: AxiosRequestConfig,
  extraConfig?: AxiosRequestConfig
): AxiosRequestConfig | undefined {
  if (!baseConfig) {
    return extraConfig ? { ...extraConfig } : undefined
  }

  if (!extraConfig) {
    return { ...baseConfig }
  }

  const merged: AxiosRequestConfig = { ...baseConfig, ...extraConfig }

  if (baseConfig.headers || extraConfig.headers) {
    if (isPlainObject(baseConfig.headers) || isPlainObject(extraConfig.headers)) {
      merged.headers = {
        ...(isPlainObject(baseConfig.headers) ? (baseConfig.headers as Record<string, any>) : {}),
        ...(isPlainObject(extraConfig.headers) ? (extraConfig.headers as Record<string, any>) : {})
      }
    } else {
      merged.headers = extraConfig.headers ?? baseConfig.headers
    }
  }

  if (isPlainObject(baseConfig.params) || isPlainObject(extraConfig.params)) {
    merged.params = {
      ...(isPlainObject(baseConfig.params) ? (baseConfig.params as Record<string, any>) : {}),
      ...(isPlainObject(extraConfig.params) ? (extraConfig.params as Record<string, any>) : {})
    }
  }

  return merged
}

function mergeMetadata(base?: RequestConfig, extra?: RequestConfig): RequestConfig | undefined {
  if (base && extra) {
    return { ...base, ...extra }
  }
  return base ?? extra
}

function attachMetadata(
  config: AxiosRequestConfig | undefined,
  metadata?: RequestConfig
): AxiosRequestConfig | undefined {
  if (!metadata) {
    return config ? { ...config } : undefined
  }

  const cloned = { ...(config ?? {}) } as AxiosRequestConfig & { metadata?: RequestConfig }
  cloned.metadata = {
    ...(cloned.metadata ?? {}),
    ...metadata
  }

  return cloned
}

function isAxiosConfigLike(value: unknown): value is AxiosRequestConfig & { metadata?: RequestConfig } {
  if (!isPlainObject(value)) {
    return false
  }

  return Object.keys(value).some((key) => AXIOS_CONFIG_FIELDS.has(key))
}

const request = Object.assign(
  ((config: AxiosRequestConfig & { metadata?: RequestConfig }) => service(config)) as RequestExecutor,
  {
    get<T = any>(url: string, paramsOrConfig?: any, options?: RequestOptions): Promise<T> {
      let baseConfig: AxiosRequestConfig | undefined
      let baseMetadata: RequestConfig | undefined

      if (isAxiosConfigLike(paramsOrConfig)) {
        const { axiosConfig, metadata } = splitRequestOptions(paramsOrConfig)
        baseConfig = axiosConfig
        baseMetadata = metadata
      } else if (paramsOrConfig !== undefined) {
        baseConfig = { params: paramsOrConfig }
      }

      const { axiosConfig: extraConfig, metadata: extraMetadata } = splitRequestOptions(options)
      const mergedConfig = mergeAxiosConfig(baseConfig, extraConfig)
      const mergedMetadata = mergeMetadata(baseMetadata, extraMetadata)

      return service.get(url, attachMetadata(mergedConfig, mergedMetadata))
    },

    post<T = any>(url: string, data?: any, options?: RequestOptions): Promise<T> {
      const { axiosConfig, metadata } = splitRequestOptions(options)
      return service.post(url, data, attachMetadata(axiosConfig, metadata))
    },

    put<T = any>(url: string, data?: any, options?: RequestOptions): Promise<T> {
      const { axiosConfig, metadata } = splitRequestOptions(options)
      return service.put(url, data, attachMetadata(axiosConfig, metadata))
    },

    patch<T = any>(url: string, data?: any, options?: RequestOptions): Promise<T> {
      const { axiosConfig, metadata } = splitRequestOptions(options)
      return service.patch(url, data, attachMetadata(axiosConfig, metadata))
    },

    delete<T = any>(url: string, paramsOrConfig?: any, options?: RequestOptions): Promise<T> {
      let baseConfig: AxiosRequestConfig | undefined
      let baseMetadata: RequestConfig | undefined

      if (isAxiosConfigLike(paramsOrConfig)) {
        const { axiosConfig, metadata } = splitRequestOptions(paramsOrConfig)
        baseConfig = axiosConfig
        baseMetadata = metadata
      } else if (paramsOrConfig !== undefined) {
        baseConfig = { params: paramsOrConfig }
      }

      const { axiosConfig: extraConfig, metadata: extraMetadata } = splitRequestOptions(options)
      const mergedConfig = mergeAxiosConfig(baseConfig, extraConfig)
      const mergedMetadata = mergeMetadata(baseMetadata, extraMetadata)

      return service.delete(url, attachMetadata(mergedConfig, mergedMetadata))
    },

    upload<T = any>(
      url: string,
      file: File,
      options?: (RequestConfig & AxiosRequestConfig) & {
        onProgress?: (progress: number) => void
      }
    ): Promise<T> {
      const progressHandler = options?.onProgress
      const { axiosConfig, metadata } = splitRequestOptions(options)

      const uploadConfig: AxiosRequestConfig = {
        ...(axiosConfig ?? {}),
        headers: {
          ...(isPlainObject(axiosConfig?.headers) ? (axiosConfig?.headers as Record<string, any>) : {}),
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          if (typeof axiosConfig?.onUploadProgress === 'function') {
            axiosConfig.onUploadProgress(progressEvent)
          }
          if (progressHandler && progressEvent.total) {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            progressHandler(progress)
          }
        }
      }

      const formData = new FormData()
      formData.append('file', file)

      return service.post(url, formData, attachMetadata(uploadConfig, metadata))
    },

    async download(url: string, filename?: string, params?: any, options?: RequestOptions): Promise<void> {
      const { axiosConfig, metadata } = splitRequestOptions(options)
      const downloadConfig = mergeAxiosConfig(
        {
          params,
          responseType: 'blob'
        },
        axiosConfig
      )

      const response = await service.get(url, attachMetadata(downloadConfig, metadata))
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    },

    all<T = any>(requests: Promise<T>[]): Promise<T[]> {
      return Promise.all(requests)
    },

    async retry<T = any>(requestFn: () => Promise<T>, maxRetries: number = 3, delay: number = 1000): Promise<T> {
      let lastError: unknown

      for (let attempt = 0; attempt <= maxRetries; attempt += 1) {
        try {
          return await requestFn()
        } catch (error) {
          lastError = error
          if (attempt < maxRetries) {
            await new Promise((resolve) => setTimeout(resolve, delay * Math.pow(2, attempt)))
          }
        }
      }

      throw lastError
    }
  }
) as RequestExecutor

declare module 'axios' {
  interface AxiosRequestConfig {
    metadata?: RequestConfig
  }

  interface InternalAxiosRequestConfig {
    metadata?: RequestConfig
  }
}

export default request
export { request, service }

const JSON_COLON_NEEDS_SPACE = /":(?=\S)/g

function normalizeJsonRequestPayload(data: unknown, headers?: AxiosRequestHeaders) {
  if (typeof data !== 'string') {
    return data
  }

  if (!isJsonContentType(headers)) {
    return data
  }

  const trimmed = data.trim()
  if (!trimmed) {
    return data
  }

  try {
    JSON.parse(trimmed)
  } catch {
    return data
  }

  const needsSpacing = JSON_COLON_NEEDS_SPACE.test(trimmed)
  JSON_COLON_NEEDS_SPACE.lastIndex = 0
  if (!needsSpacing) {
    return trimmed
  }

  return trimmed.replace(JSON_COLON_NEEDS_SPACE, '": ')
}

function isJsonContentType(headers?: AxiosRequestHeaders) {
  if (!headers) {
    return false
  }

  const entries: Array<[string, unknown]> =
    typeof (headers as any).entries === 'function'
      ? Array.from((headers as any).entries())
      : Object.entries(headers)

  const header = entries.find(([key]) => key?.toLowerCase() === 'content-type')
  if (!header) {
    return false
  }

  const value = Array.isArray(header[1]) ? header[1].join(';') : String(header[1] ?? '')
  return value.toLowerCase().includes('application/json')
}

const ABSOLUTE_URL_PATTERN = /^[a-z][a-z\d+\-.]*:/i

function normalizeRequestUrl(config: InternalAxiosRequestConfig) {
  const rawUrl = config.url

  if (typeof rawUrl !== 'string' || !rawUrl) {
    return
  }

  if (ABSOLUTE_URL_PATTERN.test(rawUrl) || rawUrl.startsWith('//') || !rawUrl.startsWith('/')) {
    return
  }

  const normalized = rawUrl.replace(/^\/+/, '')
  config.url = normalized.length > 0 ? normalized : '/'
}
