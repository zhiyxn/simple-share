const normalizeString = (value: unknown): string => {
  if (typeof value === 'string') {
    return value.trim()
  }
  if (value === null || value === undefined) {
    return ''
  }
  return String(value).trim()
}

const stripTrailingSlash = (value: string): string => {
  if (!value) {
    return ''
  }
  return value.endsWith('/') ? value.slice(0, -1) : value
}

const computeFileBaseUrl = (): string => {
  const envBase = normalizeString(import.meta.env.VITE_FILE_BASE_URL ?? '')
  if (envBase) {
    return stripTrailingSlash(envBase)
  }

  const apiBase = normalizeString(import.meta.env.VITE_API_BASE_URL ?? '')
  if (apiBase) {
    const withoutTrailingSlash = stripTrailingSlash(apiBase)
    if (withoutTrailingSlash) {
      return withoutTrailingSlash.replace(/\/api$/i, '')
    }
  }

  if (typeof window !== 'undefined' && window.location?.origin) {
    return window.location.origin
  }

  return ''
}

const extractScheme = (baseUrl: string): string => {
  const match = baseUrl.match(/^([a-z]+)(?=:\/\/)/i)
  if (match?.[1]) {
    return match[1]
  }
  if (typeof window !== 'undefined' && window.location?.protocol) {
    const protocol = window.location.protocol.replace(':', '')
    if (protocol) {
      return protocol
    }
  }
  return 'https'
}

const DEFAULT_FILE_BASE_URL = computeFileBaseUrl()
const DEFAULT_FILE_BASE_SCHEME = extractScheme(DEFAULT_FILE_BASE_URL)

const isIpAddress = (value: string): boolean => {
  if (!value) {
    return false
  }
  const parts = value.split('.')
  if (parts.length !== 4) {
    return false
  }
  return parts.every(part => {
    if (!/^\d+$/.test(part)) {
      return false
    }
    const num = Number(part)
    return num >= 0 && num <= 255
  })
}

const looksLikeHostnamePath = (value: string): boolean => {
  const match = value.match(/^([a-z0-9.-]+(?::\d+)?)(?=\/)/i)
  if (!match?.[1]) {
    return false
  }
  const host = match[1]
  if (host === 'localhost') {
    return true
  }
  if (host.includes(':')) {
    return true
  }
  if (host.includes('.')) {
    return true
  }
  return isIpAddress(host)
}

const buildAbsoluteUrl = (input: string, baseUrl: string, scheme: string): string => {
  if (/^(https?:|data:|blob:)/i.test(input)) {
    return input
  }

  if (input.startsWith('//')) {
    return `${scheme}://${input.slice(2)}`
  }

  if (looksLikeHostnamePath(input)) {
    return `${scheme}://${input}`
  }

  const normalizedPath = input.startsWith('/') ? input : `/${input}`
  return baseUrl ? `${baseUrl}${normalizedPath}` : normalizedPath
}

const normalizeUploadPath = (path: string): string => {
  if (!path) {
    return path
  }

  let normalized = path.startsWith('./') ? path.slice(1) : path
  const replacements: Array<[string, string]> = [
    ['/api/upload/upload/', '/upload/'],
    ['api/upload/upload/', 'upload/'],
    ['/api/upload/', '/upload/'],
    ['api/upload/', 'upload/']
  ]

  for (const [source, target] of replacements) {
    if (normalized.startsWith(source)) {
      normalized = normalized.replace(source, target)
      break
    }
  }

  if (normalized.startsWith('upload/')) {
    normalized = `/${normalized}`
  }

  return normalized
}

const normalizeAbsoluteUploadUrl = (value: string): string => {
  try {
    const url = new URL(value)
    const normalizedPath = normalizeUploadPath(url.pathname)
    if (normalizedPath && normalizedPath !== url.pathname) {
      url.pathname = normalizedPath.startsWith('/') ? normalizedPath : `/${normalizedPath}`
    }
    return url.toString()
  } catch {
    return value
  }
}

export interface EnsureAbsoluteUrlOptions {
  baseUrl?: string
}

export const ensureAbsoluteUrl = (value: unknown, options: EnsureAbsoluteUrlOptions = {}): string => {
  const input = normalizeString(value)
  if (!input) {
    return ''
  }

  if (/^https?:\/\//i.test(input)) {
    return normalizeAbsoluteUploadUrl(input)
  }

  const overrideBase = normalizeString(options.baseUrl)
  const baseUrl = stripTrailingSlash(overrideBase) || DEFAULT_FILE_BASE_URL
  const scheme = overrideBase ? extractScheme(baseUrl) : DEFAULT_FILE_BASE_SCHEME

  const sanitizedInput = normalizeUploadPath(input)
  return buildAbsoluteUrl(sanitizedInput, baseUrl, scheme)
}

export const fileUrlConfig = {
  baseUrl: DEFAULT_FILE_BASE_URL,
  scheme: DEFAULT_FILE_BASE_SCHEME
}
