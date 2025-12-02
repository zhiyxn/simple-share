/**
 * 认证工具函数
 */

/**
 * 获取存储在localStorage中的token
 * @returns {string} token字符串，如果不存在则返回空字符串
 */
export function getToken(): string {
  return localStorage.getItem('token') || ''
}

/**
 * 设置token到localStorage
 * @param token - 要存储的token
 */
export function setToken(token: string): void {
  localStorage.setItem('token', token)
}

/**
 * 移除localStorage中的token
 */
export function removeToken(): void {
  localStorage.removeItem('token')
}

/**
 * 检查是否有有效的token
 * @returns {boolean} 是否存在token
 */
export function hasToken(): boolean {
  return !!getToken()
}

/**
 * 获取Authorization头部值
 * @returns {string} Bearer token格式的Authorization头部值
 */
export function getAuthHeader(): string {
  const token = getToken()
  return token ? `Bearer ${token}` : ''
}