// 用户相关类型定义

// 用户角色枚举
export enum UserRole {
  SUPER_ADMIN = 'super_admin',
  ADMIN = 'admin',
  EDITOR = 'editor',
  USER = 'user'
}

// 用户类型枚举（对应后台 UserTypeEnum）
export enum UserType {
  NORMAL = '0',    // 普通用户
  ADMIN = '1',     // 管理员
  VIP = '3'        // VIP会员
}
// 用户状态枚举
export enum UserStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  SUSPENDED = 'suspended',
  PENDING = 'pending'
}

export interface CurrentUserProfile {
  id: string
  username: string
  email: string
  emailVerified?: boolean
  nickname?: string
  avatar?: string
  bio?: string
  website?: string
  github?: string
  twitter?: string
  twoFactorEnabled?: boolean
  language?: string
  timezone?: string
  emailNotifications?: {
    comments?: boolean
    likes?: boolean
    follows?: boolean
    newsletter?: boolean
  }
  userType?: string
  vipExpireTime?: string | number | null
  vipActive?: boolean
  roles: string[]
  permissions: string[]
  createdAt?: string
  updatedAt?: string
}

export interface CurrentUserUpdatePayload {
  nickname?: string
  bio?: string
  website?: string
  github?: string
  twitter?: string
  language?: string
  timezone?: string
  emailNotifications?: {
    comments?: boolean
    likes?: boolean
    follows?: boolean
    newsletter?: boolean
  }
}

// 用户基础信息
export interface User {
  id: string
  username: string
  email: string
  nickname?: string
  avatar?: string
  bio?: string
  website?: string
  location?: string
  role: UserRole
  status: UserStatus
  userType: UserType  // 用户类型
  vipExpireTime?: string  // VIP过期时间
  articleReviewRequired?: boolean
  isEmailVerified: boolean
  isTwoFactorEnabled: boolean
  lastLoginAt?: string
  lastLoginIp?: string
  loginCount: number
  articleCount: number
  followerCount: number
  followingCount: number
  preferences: UserPreferences
  socialAccounts: UserSocialAccount[]
  createdAt: string
  updatedAt: string
  tenant: {
    id: string
    name: string
    domain: string
  }
}

// 用户偏好设置
export interface UserPreferences {
  language: string
  timezone: string
  theme: 'light' | 'dark' | 'auto'
  emailNotifications: {
    newComment: boolean
    newFollower: boolean
    articleLiked: boolean
    systemUpdate: boolean
    newsletter: boolean
  }
  pushNotifications: {
    newComment: boolean
    newFollower: boolean
    articleLiked: boolean
  }
  privacy: {
    showEmail: boolean
    showLastLogin: boolean
    allowFollow: boolean
    allowMessage: boolean
  }
  editor: {
    autoSave: boolean
    autoSaveInterval: number // seconds
    defaultVisibility: 'public' | 'private'
    enableMarkdown: boolean
    enableCodeHighlight: boolean
  }
}

// 用户社交账户
export interface UserSocialAccount {
  id: string
  provider: 'github' | 'google' | 'wechat' | 'weibo'
  providerId: string
  username?: string
  email?: string
  avatar?: string
  accessToken?: string
  refreshToken?: string
  expiresAt?: string
  createdAt: string
}

// 用户登录凭据
export interface LoginCredentials {
  email: string
  password: string
  rememberMe?: boolean
  captcha?: string
  twoFactorCode?: string
}

// 用户注册数据
export interface RegisterData {
  username: string
  email: string
  password: string
  inviteCode?: string
  captcha?: string
  agreeToTerms: boolean
}

// 用户资料更新数据
export interface ProfileUpdateData {
  nickname?: string
  bio?: string
  website?: string
  location?: string
  avatar?: string
  preferences?: Partial<UserPreferences>
}

// 密码修改数据
export interface PasswordChangeData {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

// 用户统计信息
export interface UserStats {
  totalUsers: number
  activeUsers: number
  newUsersToday: number
  newUsersThisWeek: number
  newUsersThisMonth: number
  usersByRole: Record<UserRole, number>
  usersByStatus: Record<UserStatus, number>
  topAuthors: Array<{
    id: string
    username: string
    nickname?: string
    avatar?: string
    articleCount: number
    totalViews: number
  }>
}

// 用户查询参数
export interface UserListParams {
  page?: number
  limit?: number
  keyword?: string
  role?: UserRole
  status?: UserStatus
  isEmailVerified?: boolean
  sortBy?: 'createdAt' | 'lastLoginAt' | 'articleCount' | 'username'
  sortOrder?: 'asc' | 'desc'
  startDate?: string
  endDate?: string
}

// 用户创建数据（管理员使用）
export interface UserCreateData {
  username: string
  email: string
  password: string
  nickname?: string
  role: UserRole
  status?: UserStatus
  userType?: UserType
  vipExpireTime?: string
  articleReviewRequired?: boolean
  sendWelcomeEmail?: boolean
}
// 用户更新数据（管理员使用）
export interface UserUpdateData {
  username?: string
  email?: string
  nickname?: string
  bio?: string
  website?: string
  location?: string
  role?: UserRole
  status?: UserStatus
  userType?: UserType
  vipExpireTime?: string
  articleReviewRequired?: boolean
  isEmailVerified?: boolean
}
// 用户登录设备
export interface UserLoginDevice {
  id: string
  userId: string
  deviceName: string
  deviceType: 'desktop' | 'mobile' | 'tablet'
  browser: string
  os: string
  ipAddress: string
  location?: string
  isCurrent: boolean
  lastActiveAt: string
  createdAt: string
}

// 用户活动日志
export interface UserActivityLog {
  id: string
  userId: string
  action: string
  resource?: string
  resourceId?: string
  details: Record<string, any>
  ipAddress: string
  userAgent: string
  createdAt: string
}

// 用户关注关系
export interface UserFollow {
  id: string
  followerId: string
  followingId: string
  createdAt: string
  follower: {
    id: string
    username: string
    nickname?: string
    avatar?: string
  }
  following: {
    id: string
    username: string
    nickname?: string
    avatar?: string
  }
}

// 用户通知
export interface UserNotification {
  id: string
  userId: string
  type: 'comment' | 'like' | 'follow' | 'system' | 'article'
  title: string
  content: string
  data?: Record<string, any>
  isRead: boolean
  createdAt: string
  readAt?: string
}

// 用户权限
export interface UserPermission {
  id: string
  name: string
  description: string
  resource: string
  action: string
  conditions?: Record<string, any>
}

// 用户角色权限
export interface UserRolePermissions {
  role: UserRole
  permissions: UserPermission[]
  description: string
}

// 用户邀请
export interface UserInvitation {
  id: string
  email: string
  role: UserRole
  invitedBy: {
    id: string
    username: string
    nickname?: string
  }
  token: string
  expiresAt: string
  acceptedAt?: string
  createdAt: string
}

// 用户搜索结果
export interface UserSearchResult {
  id: string
  username: string
  nickname?: string
  avatar?: string
  bio?: string
  articleCount: number
  followerCount: number
  isFollowing?: boolean
  highlight?: {
    username?: string[]
    nickname?: string[]
    bio?: string[]
  }
}

// 用户导出数据
export interface UserExportData {
  users: User[]
  exportedAt: string
  version: string
  includePersonalData: boolean
}

// 用户批量操作
export interface UserBatchOperation {
  ids: string[]
  operation: 'activate' | 'deactivate' | 'suspend' | 'delete' | 'updateRole' | 'sendEmail'
  data?: {
    status?: UserStatus
    role?: UserRole
    emailTemplate?: string
    emailSubject?: string
    emailContent?: string
  }
}

// 忘记密码请求
export interface ForgotPasswordRequest {
  email: string
  captcha?: string
  captchaId?: string
  tenantId?: number
}

// 重置密码数据
export interface ResetPasswordData {
  token?: string
  email?: string
  code?: string
  password: string
  confirmPassword?: string
  tenantId?: number
  captcha?: string
  captchaId?: string
}

// 邮箱验证数据
export interface EmailVerificationData {
  token: string
}

// 两步验证设置
export interface TwoFactorAuthSetup {
  secret: string
  qrCode: string
  backupCodes: string[]
}

// 两步验证验证
export interface TwoFactorAuthVerification {
  code: string
  backupCode?: string
}

// VIP管理相关接口
export interface VipManagementData {
  userId: string
  days?: number  // 设置VIP天数或延长天数
}

// VIP用户查询参数
export interface VipUserQueryParams {
  page?: number
  pageSize?: number
  days?: number  // 查询即将过期的天数
}

// 用户类型工具函数
export const UserTypeUtils = {
  // 获取用户类型描述
  getTypeDesc(userType: UserType): string {
    switch (userType) {
      case UserType.NORMAL:
        return '普通用户'
      case UserType.ADMIN:
        return '管理员'
      case UserType.VIP:
        return 'VIP会员'
      default:
        return '未知'
    }
  },

  // 检查是否为VIP
  isVip(userType: UserType): boolean {
    return userType === UserType.VIP
  },

  // 检查是否为管理员
  isAdmin(userType: UserType): boolean {
    return userType === UserType.ADMIN
  },

  // 检查VIP是否过期
  isVipExpired(vipExpireTime?: string): boolean {
    if (!vipExpireTime) return true
    return new Date(vipExpireTime) < new Date()
  },

  // 获取VIP剩余天数
  getVipRemainingDays(vipExpireTime?: string): number {
    if (!vipExpireTime) return 0
    const expireDate = new Date(vipExpireTime)
    const now = new Date()
    const diffTime = expireDate.getTime() - now.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return Math.max(0, diffDays)
  }
}

// 兼容历史类型导出
export type UserProfile = CurrentUserProfile
export type UserQueryParams = UserListParams
export type SocialAccount = UserSocialAccount
