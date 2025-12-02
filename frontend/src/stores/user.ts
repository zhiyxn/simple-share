import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useTenantStore } from './tenant'
import { UserType } from '@/types/user'
import type { CurrentUserProfile, CurrentUserUpdatePayload } from '@/types/user'

const TOKEN_EXPIRY_LEEWAY = 10

interface SafeStorage {
  hasUsableStorage: boolean
  getItem: (key: string) => string | null
  setItem: (key: string, value: string) => boolean
  removeItem: (key: string) => void
  persistStorage: {
    getItem: (key: string) => string | null
    setItem: (key: string, value: string) => void
    removeItem: (key: string) => void
  }
}

const createSafeStorage = (): SafeStorage => {
  if (typeof window === 'undefined') {
    return {
      hasUsableStorage: false,
      getItem: () => null,
      setItem: () => false,
      removeItem: () => {},
      persistStorage: {
        getItem: () => null,
        setItem: () => {},
        removeItem: () => {}
      }
    }
  }

  const candidates: Array<{ name: string; storage: Storage }> = []

  if (typeof window.localStorage !== 'undefined') {
    candidates.push({ name: 'localStorage', storage: window.localStorage })
  }
  if (typeof window.sessionStorage !== 'undefined') {
    candidates.push({ name: 'sessionStorage', storage: window.sessionStorage })
  }

  const usable = candidates.filter(({ storage, name }) => {
    try {
      const testKey = '__simpleshare_storage_test__'
      storage.setItem(testKey, '1')
      storage.removeItem(testKey)
      return true
    } catch (error) {
      console.warn(`Storage ${name} is not available:`, error)
      return false
    }
  })

  const trySetItem = (key: string, value: string): boolean => {
    for (const { storage, name } of usable) {
      try {
        storage.setItem(key, value)
        return true
      } catch (error) {
        console.warn(`Failed to write ${key} to ${name}:`, error)
      }
    }
    return false
  }

  const tryGetItem = (key: string): string | null => {
    for (const { storage, name } of usable) {
      try {
        const value = storage.getItem(key)
        if (value !== null && value !== undefined) {
          return value
        }
      } catch (error) {
        console.warn(`Failed to read ${key} from ${name}:`, error)
      }
    }
    return null
  }

  const tryRemoveItem = (key: string) => {
    for (const { storage, name } of usable) {
      try {
        storage.removeItem(key)
      } catch (error) {
        console.warn(`Failed to remove ${key} from ${name}:`, error)
      }
    }
  }

  return {
    hasUsableStorage: usable.length > 0,
    getItem: tryGetItem,
    setItem: trySetItem,
    removeItem: tryRemoveItem,
    persistStorage: {
      getItem: tryGetItem,
      setItem: (key: string, value: string) => {
        trySetItem(key, value)
      },
      removeItem: (key: string) => {
        tryRemoveItem(key)
      }
    }
  }
}

const safeStorage = createSafeStorage()
let storageWarningShown = false
const warnStorageUnavailable = () => {
  if (storageWarningShown) {
    return
  }
  storageWarningShown = true
  try {
    ElMessage.warning('浏览器本地存储不可用，登录状态仅在当前会话有效')
  } catch (error) {
    console.warn('Failed to display storage warning message:', error)
  }
}

const serializeUserInfo = (user: CurrentUserProfile): string => {
  return JSON.stringify(user, (_key, value) => {
    if (typeof value === 'bigint') {
      return value.toString()
    }
    return value
  })
}

const decodeBase64Url = (input: string): string => {
  const base64 = input.replace(/-/g, '+').replace(/_/g, '/')
  const padLength = (4 - (base64.length % 4)) % 4
  const padded = base64 + '='.repeat(padLength)
  if (typeof globalThis.atob === 'function') {
    return globalThis.atob(padded)
  }
  if (typeof globalThis !== 'undefined' && (globalThis as any).Buffer) {
    return (globalThis as any).Buffer.from(padded, 'base64').toString('utf-8')
  }
  throw new Error('Base64 decoding is not supported in this environment')
}

const parseTokenPayload = (token: string): Record<string, any> | null => {
  try {
    const parts = token.split('.')
    if (parts.length < 2) {
      return null
    }
    const payload = decodeBase64Url(parts[1])
    return JSON.parse(payload)
  } catch (error) {
    console.warn('Failed to parse token payload:', error)
    return null
  }
}

const isTokenExpired = (token: string): boolean => {
  if (!token) {
    return true
  }
  const payload = parseTokenPayload(token)
  if (!payload) {
    return true
  }
  const expValue = Number(payload.exp)
  if (!Number.isFinite(expValue)) {
    return false
  }
  const exp = Math.floor(expValue)
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime >= exp - TOKEN_EXPIRY_LEEWAY
}


export type UserInfo = CurrentUserProfile

export interface LoginCredentials {
  username: string
  password: string
  rememberMe?: boolean
  tenantId?: number
}

export interface RegisterData {
  username: string
  email: string
  password: string
  emailCode: string
  inviteCode?: string
  tenantId?: number
}

export type ProfileUpdateData = CurrentUserUpdatePayload

export interface PasswordChangeData {
  currentPassword: string
  newPassword: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const refreshTokenValue = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  const isLoading = ref(false)
  const loginLoading = ref(false)
  const registerLoading = ref(false)

    const normalizeUserTypeValue = (value?: string | null) => value?.toString().trim().toLowerCase() || ''
    const isAdminType = (value?: string | null) => {
      const normalized = normalizeUserTypeValue(value)
      const adminNormalized = normalizeUserTypeValue(UserType.ADMIN)
      return normalized === adminNormalized || normalized === 'admin' || normalized === '1'
    }
  const parseVipExpire = (value?: string | number | null): Date | null => {
    if (value === null || value === undefined) {
      return null
    }
    if (typeof value === 'number') {
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? null : date
    }
    const normalized = value.replace(' ', 'T')
    const date = new Date(normalized)
    return Number.isNaN(date.getTime()) ? null : date
  }

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!userInfo.value)
  const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => {
      if (!userInfo.value) {
        return false
      }

      const permissions = Array.isArray(userInfo.value.permissions) ? userInfo.value.permissions : []
      if (permissions.includes('*:*:*')) {
        return true
      }

      const roles = Array.isArray(userInfo.value.roles) ? userInfo.value.roles : []
      if (roles.some(role => role?.toLowerCase() === 'admin')) {
        return true
      }

      return isAdminType(userInfo.value.userType)
    })
  const userName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '用户')
  const userAvatar = computed(() => userInfo.value?.avatar || '')
  const isEmailVerified = computed(() => userInfo.value?.emailVerified || false)
  const vipExpireDate = computed(() => parseVipExpire(userInfo.value?.vipExpireTime ?? null))
  const vipActive = computed(() => {
    if (typeof userInfo.value?.vipActive === 'boolean') {
      return userInfo.value.vipActive
    }
    const expireDate = vipExpireDate.value
    if (!expireDate) {
      return false
    }
    return expireDate.getTime() > Date.now()
  })
  const isVipMember = computed(() => {
    if (!userInfo.value) {
      return false
    }
    const userTypeNormalized = normalizeUserTypeValue(userInfo.value.userType)
    const isVipType = userTypeNormalized === UserType.VIP || userTypeNormalized === 'vip' || userTypeNormalized === 'member' || userTypeNormalized === '3'
    if (!isVipType) {
      return false
    }
    return vipActive.value
  })

  // 初始化
  const init = () => {
    const savedToken = safeStorage.getItem('token')
    const savedRefreshToken = safeStorage.getItem('refreshToken')
    const savedUser = safeStorage.getItem('userInfo')

    if (savedRefreshToken) {
      refreshTokenValue.value = savedRefreshToken
    }

    if (savedToken && !isTokenExpired(savedToken)) {
      token.value = savedToken
    } else if (savedToken) {
      safeStorage.removeItem('token')
    }

    if (savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser)

        if (token.value) {
          userInfo.value = parsedUser
        } else {
          userInfo.value = null
          safeStorage.removeItem('userInfo')
        }
      } catch (error) {
        console.error('Failed to parse saved user info:', error)
        safeStorage.removeItem('userInfo')
      }
    }
  }


  // 保存认证信息
  const saveAuth = (authToken: string, user: UserInfo, refreshToken?: string) => {
    try {
      token.value = authToken
      userInfo.value = user
      
      let storageOk = true
      
      if (refreshToken) {
        refreshTokenValue.value = refreshToken
        storageOk = safeStorage.setItem('refreshToken', refreshToken) && storageOk
      }
      
      storageOk = safeStorage.setItem('token', authToken) && storageOk
      storageOk = safeStorage.setItem('userInfo', serializeUserInfo(user)) && storageOk

      if (!storageOk) {
        warnStorageUnavailable()
      }
    } catch (error) {
      console.error('缓存用户信息失败:', error)
      // 清理可能损坏的数据
      safeStorage.removeItem('token')
      safeStorage.removeItem('refreshToken')
      safeStorage.removeItem('userInfo')
      throw new Error('缓存用户信息失败，请检查浏览器存储设置')
    }
  }

  // 清除认证信息
  const clearAuth = () => {
    token.value = ''
    refreshTokenValue.value = ''
    userInfo.value = null

    safeStorage.removeItem('token')
    safeStorage.removeItem('refreshToken')
    safeStorage.removeItem('userInfo')
  }

  const resolveTenantId = () => {
    const tenantStore = useTenantStore()
    const tenant = tenantStore.tenantId as unknown as string | number | null | undefined
    if (tenant === null || tenant === undefined) {
      return undefined
    }
    if (typeof tenant === 'number') {
      return tenant
    }
    const parsed = Number(tenant)
    return Number.isNaN(parsed) ? undefined : parsed
  }

  // 登录
  const login = async (credentials: LoginCredentials) => {
    try {
      loginLoading.value = true

      const response = await authApi.login({
        ...credentials,
        tenantId: credentials.tenantId ?? resolveTenantId()
      })
      
      saveAuth(response.token, response.user, response.refreshToken)
      
      // 登录成功，静默跳转到首页或之前访问的页面
      const redirect = router.currentRoute.value.query.redirect as string
      await router.push(redirect || '/')
      
      return response
    } catch (error: any) {
      // 不在这里显示错误消息，让request拦截器统一处理
      throw error
    } finally {
      loginLoading.value = false
    }
  }

  // 注册
  const register = async (data: RegisterData) => {
    try {
      registerLoading.value = true

      await authApi.register({
        username: data.username,
        password: data.password,
        confirmPassword: data.password, // 假设确认密码与密码相同
        email: data.email,
        emailCode: data.emailCode,
        tenantId: data.tenantId ?? resolveTenantId()
      })

      // 注册成功，不自动登录，由调用方决定后续操作
      return { success: true }
    } catch (error: any) {
      const message = error?.message || error?.response?.data?.message || '注册失败'
      throw new Error(message)
    } finally {
      registerLoading.value = false
    }
  }

  // 登出
  const logout = async (showMessage = true) => {
    try {
      // 调用后端登出接口
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      clearAuth()
      
      if (showMessage) {
        ElMessage.success('已退出登录')
      }
      
      // 跳转到首页
      await router.push('/')
    }
  }

  // 刷新token
  const refreshToken = async () => {
    if (!refreshTokenValue.value) {
      throw new Error('No refresh token available')
    }
    
    try {
      const response = await authApi.refreshToken(refreshTokenValue.value)
      token.value = response.token
      
      let storageOk = safeStorage.setItem('token', response.token)
      
      if (response.refreshToken) {
        refreshTokenValue.value = response.refreshToken
        storageOk = safeStorage.setItem('refreshToken', response.refreshToken) && storageOk
      }
      
      if (!storageOk) {
        warnStorageUnavailable()
      }
      
      return response.token
    } catch (error) {
      // 刷新失败，清除认证信息
      clearAuth()
      throw error
    }
  }

  // 获取用户信息
  const fetchUserInfo = async (retryCount = 0) => {
    try {
      isLoading.value = true

      const user = await authApi.getCurrentUser()
      userInfo.value = user
      
      if (!safeStorage.setItem('userInfo', serializeUserInfo(user))) {
        console.warn('保存用户信息到本地存储失败')
      }

      return user
    } catch (error: any) {
      // 如果是认证错误且还没有重试过，尝试刷新 token
      if (error.response?.status === 401 && retryCount === 0) {
        try {
          await refreshToken()
          // 刷新成功后重试获取用户信息
          return await fetchUserInfo(retryCount + 1)
        } catch (refreshError) {
          await logout(false)
          throw error
        }
      } else if (error.response?.status === 401) {
        // 重试后仍然失败，清除认证数据
        await logout(false)
      } else {
        console.error('Failed to fetch user info:', error)
      }

      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 更新个人资料
  const updateProfile = async (data: ProfileUpdateData) => {
    try {
      const updatedUser = await userApi.updateCurrentUser(data)
      
      // 更新本地用户信息
      if (userInfo.value) {
        Object.assign(userInfo.value, updatedUser)
        if (!safeStorage.setItem('userInfo', serializeUserInfo(userInfo.value))) {
          console.warn('保存用户信息到本地存储失败')
        }
      }
      
      return updatedUser
    } catch (error: any) {
      console.error('Failed to update profile:', error)
      throw error
    }
  }

  // 修改密码
  const changePassword = async (data: PasswordChangeData) => {
    try {
      await authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      
      // 修改密码后需要重新登录
      await logout(false)
    } catch (error: any) {
      console.error('Failed to change password:', error)
      throw error
    }
  }

  // 上传头像
  const uploadAvatar = async (file: File | string, onProgress?: (progress: number) => void) => {
    try {
      let avatarUrl: string
      
      if (typeof file === 'string') {
        // 删除头像的情况
        avatarUrl = file
      } else {
        // 上传新头像 - 暂时返回空字符串，因为API方法不存在
        avatarUrl = ''
      }
      
      // 更新用户信息中的头像
      if (userInfo.value) {
        userInfo.value.avatar = avatarUrl
        if (!safeStorage.setItem('userInfo', serializeUserInfo(userInfo.value))) {
          console.warn('保存用户信息到本地存储失败')
        }
      }
      
      return avatarUrl
    } catch (error: any) {
      console.error('Failed to upload avatar:', error)
      throw error
    }
  }

  // 发送邮箱验证
  const sendEmailVerification = async () => {
    try {
      await authApi.resendVerificationEmail()
      return true
    } catch (error: any) {
      console.error('Failed to send verification email:', error)
      throw error
    }
  }

  // 验证邮箱
  const verifyEmail = async (token: string) => {
    try {
      await authApi.verifyEmail(token)
      
      // 更新用户信息中的邮箱验证状态
      if (userInfo.value) {
        userInfo.value.emailVerified = true
        if (!safeStorage.setItem('userInfo', serializeUserInfo(userInfo.value))) {
          console.warn('保存用户信息到本地存储失败')
        }
      }
      
      ElMessage.success('邮箱验证成功')
      return true
    } catch (error: any) {
      console.error('Failed to verify email:', error)
      ElMessage.error(error.message || '邮箱验证失败')
      throw error
    }
  }

  // 忘记密码
  const forgotPassword = async (email: string) => {
    try {
      await authApi.forgotPassword(email, resolveTenantId())
      ElMessage.success('重置密码邮件已发送，请查收')
      return true
    } catch (error: any) {
      console.error('Failed to send reset password email:', error)
      throw error
    }
  }

  // 重置密码
  const resetPassword = async (params: {
    token?: string
    email?: string
    code?: string
    password: string
    confirmPassword?: string
    tenantId?: number
    captcha?: string
    captchaId?: string
  }) => {
    try {
      const tenantId = params.tenantId ?? resolveTenantId()
      const payload = {
        ...params,
        tenantId
      }
      await authApi.resetPassword(payload)
      // 不在这里显示成功消息，让调用方处理
      return true
    } catch (error: any) {
      console.error('Failed to reset password:', error)
      throw error
    }
  }

  // 获取登录设备
  const getLoginDevices = async () => {
    try {
      // 暂时返回空数组，因为API方法不存在
      return []
    } catch (error: any) {
      console.error('Failed to get devices:', error)
      throw error
    }
  }

  // 移除设备
  const removeDevice = async (deviceId: string) => {
    try {
      // 暂时返回true，因为API方法不存在
      return true
    } catch (error: any) {
      console.error('Failed to revoke device:', error)
      throw error
    }
  }

  // 移除所有其他设备
  const revokeAllDevices = async () => {
    try {
      // 暂时返回true，因为API方法不存在
      return true
    } catch (error: any) {
      console.error('Failed to revoke all devices:', error)
      throw error
    }
  }

  // 检查认证状态
  const checkAuthStatus = async (showErrors = false) => {
    if (!token.value) {
      return false
    }

    try {
      await fetchUserInfo()
      return true
    } catch (error: any) {
      // 如果是认证错误，尝试刷新 token
      if (error.response?.status === 401) {
        try {
          await refreshToken()
          // 刷新成功后重新获取用户信息
          await fetchUserInfo()
          return true
        } catch (refreshError) {
          // 刷新失败，静默清理认证数据
          if (showErrors) {
            console.log('Token refresh failed, clearing auth data')
          }
          await logout(false)
          return false
        }
      }
      return false
    }
  }

  // 轻量级认证状态检查 - 用于非关键场景
  const hasValidToken = () => {
    if (!token.value) {
      return false
    }

    if (isTokenExpired(token.value)) {
      console.warn('Detected expired access token, clearing local credentials')
      token.value = ''
      safeStorage.removeItem('token')

      userInfo.value = null
      safeStorage.removeItem('userInfo')

      return false
    }

    return true
  }


  // 验证认证状态 - 用于关键操作
  const validateAuth = async (showError = true) => {
    if (!token.value) {
      if (showError) {
        ElMessage.warning('请先登录')
      }
      return false
    }

    try {
      await fetchUserInfo()
      return true
    } catch (error) {
      if (showError) {
        ElMessage.warning('登录已过期，请重新登录')
      }
      await logout(false)
      return false
    }
  }

  // 更新两步验证状态
  const updateTwoFactorAuth = async (enabled: boolean) => {
    try {
      // 这里应该调用相应的API
      // await userApi.updateTwoFactor(enabled)
      
      if (userInfo.value) {
        userInfo.value.twoFactorEnabled = enabled
        if (!safeStorage.setItem('userInfo', serializeUserInfo(userInfo.value))) {
          console.warn('保存用户信息到本地存储失败')
        }
      }
      
      return true
    } catch (error: any) {
      console.error('Failed to update two factor:', error)
      throw error
    }
  }

  const getCaptcha = async () => {
    return { image: '', id: '' }
  }

  const getConfig = async () => {
    return { requireCaptcha: false }
  }

  return {
    // 状态
    token,
    userInfo,
    isLoading,
    loginLoading,
    registerLoading,

    // 计算属性
    isAuthenticated,
    isLoggedIn,
    isAdmin,
    userName,
    displayName,
    userAvatar,
    isEmailVerified,
    vipExpireDate,
    vipActive,
    isVipMember,

    // 方法
    init,
    login,
    register,
    logout,
    clearAuth,
    refreshToken,
    fetchUserInfo,
    updateProfile,
    changePassword,
    uploadAvatar,
    sendEmailVerification,
    verifyEmail,
    forgotPassword,
    resetPassword,
    getLoginDevices,
    removeDevice,
    checkAuthStatus,
    hasValidToken,
    validateAuth,
    updateTwoFactorAuth,
    getCaptcha,
    getConfig
  }
}, {
  persist: {
    key: 'user-store',
    storage: safeStorage.persistStorage,
    pick: ['token', 'refreshTokenValue', 'userInfo']
  }
})
