import request from './request'
import type { CurrentUserProfile } from '@/types'

// 认证相关API接口
export interface LoginParams {
  username: string
  password: string
  rememberMe?: boolean
  captcha?: string
  captchaId?: string
  tenantId?: number
}

export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  email: string
  emailCode: string
  tenantId?: number
}

export interface SendEmailCodeParams {
  email: string
  scene: 'register' | 'reset_password'
  tenantId?: number
  captcha?: string
  captchaId?: string
}

export interface ResetPasswordParams {
  token?: string
  email?: string
  code?: string
  password: string
  confirmPassword?: string
  tenantId?: number
  captcha?: string
  captchaId?: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  user: CurrentUserProfile
}

export interface RefreshTokenResponse {
  token: string
  refreshToken: string
}

const authApi = {
  // 用户登录
  login(data: LoginParams): Promise<LoginResponse> {
    return request.post<LoginResponse>('/system/auth/login', data)
  },

  // 用户注册
  register(data: RegisterParams): Promise<void> {
    return request.post('/system/auth/register', data, { showError: false })
  },

  // 发送邮箱验证码
  sendEmailCode(data: SendEmailCodeParams): Promise<{ expiresIn: number; scene: string; token?: string }> {
    return request.post('/system/auth/send-email-code', data)
  },

  // 注册验证码
  sendRegisterCode(email: string, tenantId?: number): Promise<{ expiresIn: number; scene: string; token?: string }> {
    return request.post('/system/auth/send-email-code', {
      email,
      scene: 'register',
      tenantId
    })
  },

  // 忘记密码验证码
  sendResetCode(params: { email: string; tenantId?: number; captcha?: string; captchaId?: string }): Promise<{ expiresIn: number; scene: string; token?: string }> {
    return request.post('/system/auth/send-email-code', {
      ...params,
      scene: 'reset_password'
    })
  },

  // 验证重置验证码
  verifyResetCode(data: { email: string; code: string; tenantId?: number }): Promise<{ token: string; expiresIn?: number }> {
    return request.post('/system/auth/verify-reset-code', data)
  },

  // 用户登出
  logout(): Promise<void> {
    return request.post('/system/auth/logout')
  },

  // 刷新Token
  refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
    return request.post('/system/auth/refresh', { refreshToken })
  },

  // 获取当前用户信息
  getCurrentUser(): Promise<CurrentUserProfile> {
    return request.get<CurrentUserProfile>('/system/auth/me')
  },

  // 修改密码
  changePassword(data: {
    currentPassword: string
    newPassword: string
    confirmPassword: string
  }): Promise<void> {
    return request.put('/system/auth/change-password', data)
  },

  // 忘记密码 - 发送重置邮件
  forgotPassword(email: string, tenantId?: number): Promise<void> {
    return request.post('/system/auth/forgot-password', { email, tenantId })
  },

  // 重置密码
  resetPassword(data: ResetPasswordParams): Promise<void> {
    const payload = {
      ...data,
      confirmPassword: data.confirmPassword ?? data.password
    }
    return request.post('/system/auth/reset-password', payload)
  },

  // 验证邮箱
  verifyEmail(token: string): Promise<void> {
    return request.post('/system/auth/verify-email', { token })
  },

  // 重新发送验证邮件
  resendVerificationEmail(): Promise<void> {
    return request.post('/system/auth/resend-verification')
  },

  // 获取验证码
  getCaptcha(): Promise<{
    captchaId: string
    captchaImage: string
  }> {
    return request.get('/system/auth/captcha')
  },

  // 验证Token有效性
  validateToken(): Promise<boolean> {
    return request.get('/system/auth/validate')
  },

  // 获取用户权限
  getUserPermissions(): Promise<string[]> {
    return request.get('/system/auth/permissions')
  },

  // 检查用户名是否可用
  checkUsername(username: string): Promise<{ available: boolean }> {
    return request.get(`/system/auth/check-username?username=${username}`)
  },

  // 检查邮箱是否可用
  checkEmail(email: string): Promise<{ available: boolean }> {
    return request.get(`/system/auth/check-email?email=${email}`)
  }
}

export default authApi
export { authApi }
