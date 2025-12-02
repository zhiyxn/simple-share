import request from '@/api/request'

export interface PasswordPolicy {
  minLength: number
  requireUppercase: boolean
  requireLowercase: boolean
  requireNumber: boolean
  requireSymbol: boolean
  expireDays: number | null
  historyCount: number | null
}

export interface LoginProtection {
  maxAttempts: number
  lockoutMinutes: number
  autoUnlock: boolean
  captchaEnabled: boolean
  sessionTimeoutMinutes: number
  rememberMeEnabled: boolean
  twoFactorEnabled: boolean
  twoFactorMethods: string[]
}

export interface AccessControl {
  ipWhitelist: string[]
  ipBlacklist: string[]
}

export interface SecuritySettings {
  passwordPolicy: PasswordPolicy
  loginProtection: LoginProtection
  accessControl: AccessControl
}

export function fetchSecuritySettings() {
  return request.get<SecuritySettings>('/system/admin/security-settings')
}

export function updateSecuritySettings(payload: SecuritySettings) {
  return request.put('/system/admin/security-settings', payload)
}
