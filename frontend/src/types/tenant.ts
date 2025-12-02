// 租户相关类型定义

// 租户基础信息
export interface Tenant {
  tenantId: string | number
  tenantCode: string
  tenantName: string
  domain: string
  contactUserName?: string
  contactPhone?: string
  companyName?: string
  licenseNumber?: string
  address?: string
  intro?: string
  remark?: string
  packageId?: string | number
  expireTime?: string
  accountCount?: string | number
  status: string // '0' 正常, '1' 停用
  delFlag?: string // '0' 存在, '2' 删除
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string

  // 兼容前端使用的字段
  id?: string
  name?: string
  logo?: string
  favicon?: string
  description?: string
  subdomain?: string
  plan?: 'free' | 'basic' | 'pro' | 'enterprise'
  maxUsers?: number
  maxArticles?: number
  maxStorage?: number
  usedStorage?: number
  userCount?: number
  articleCount?: number
  createdAt?: string
  updatedAt?: string
  expiredAt?: string
  owner?: {
    id: string
    username: string
    email: string
    nickname?: string
  }
}

// 租户配置
export interface TenantConfig {
  // 站点基础配置
  siteTitle: string
  siteDescription: string
  siteKeywords: string
  siteLogo?: string
  siteFavicon?: string
  siteUrl: string
  
  // 用户注册配置
  allowRegistration: boolean
  requireEmailVerification: boolean
  requireInviteCode: boolean
  defaultUserRole: 'user' | 'editor' | 'admin'
  
  // 文章功能配置
  enableComments: boolean
  enableLikes: boolean
  enableSharing: boolean
  enableRating: boolean
  moderateComments: boolean
  
  // 文件上传配置
  maxFileSize: number // MB
  allowedFileTypes: string[]
  enableImageCompression: boolean
  imageQuality: number
  
  // 防复制配置
  copyPolicy: 'follow_article' | 'global_allow' | 'global_deny'
  enableWatermark: boolean
  watermarkText?: string
  watermarkOpacity: number
  disableRightClick: boolean
  disableTextSelection: boolean
  disableDevTools: boolean
  
  // SEO配置
  enableSEO: boolean
  googleAnalyticsId?: string
  baiduAnalyticsId?: string
  googleSiteVerification?: string
  baiduSiteVerification?: string
  
  // 社交登录配置
  enableGithubLogin: boolean
  githubClientId?: string
  enableGoogleLogin: boolean
  googleClientId?: string
  enableWechatLogin: boolean
  wechatAppId?: string
  
  // 邮件配置
  smtpHost?: string
  smtpPort?: number
  smtpUser?: string
  smtpPassword?: string
  smtpSecure: boolean
  emailFrom?: string
  emailFromName?: string
  
  // 主题配置
  theme: {
    primaryColor: string
    secondaryColor: string
    backgroundColor: string
    textColor: string
    linkColor: string
    borderColor: string
    headerBackground: string
    footerBackground: string
    sidebarBackground: string
    customCSS?: string
    customJS?: string
  }
  
  // 其他配置
  timezone: string
  language: string

  // 站长信息
  ownerQrCode?: string
  ownerRemark?: string
  owner?: {
    qrCode?: string
    remark?: string
  }
  dateFormat: string
  timeFormat: string
  enableMaintenance: boolean
  maintenanceMessage?: string
  enableBackup: boolean
  backupFrequency: 'daily' | 'weekly' | 'monthly'
  
  createdAt: string
  updatedAt: string
}

// 租户创建数据
export interface TenantCreateData {
  name: string
  domain: string
  subdomain?: string
  description?: string
  plan: 'free' | 'basic' | 'pro' | 'enterprise'
  ownerId: string
  maxUsers?: number
  maxArticles?: number
  maxStorage?: number
  expiredAt?: string
}

// 租户更新数据
export interface TenantUpdateData {
  name?: string
  domain?: string
  subdomain?: string
  logo?: string
  favicon?: string
  description?: string
  status?: 'active' | 'inactive' | 'suspended'
  plan?: 'free' | 'basic' | 'pro' | 'enterprise'
  maxUsers?: number
  maxArticles?: number
  maxStorage?: number
  expiredAt?: string
}

// 租户统计信息
export interface TenantStats {
  userStats: {
    totalUsers: number
    activeUsers: number
    newUsersToday: number
    newUsersThisWeek: number
    newUsersThisMonth: number
  }
  articleStats: {
    totalArticles: number
    publishedArticles: number
    draftArticles: number
    newArticlesToday: number
    newArticlesThisWeek: number
    newArticlesThisMonth: number
  }
  storageStats: {
    totalStorage: number // MB
    usedStorage: number // MB
    availableStorage: number // MB
    usagePercentage: number
  }
  trafficStats: {
    totalViews: number
    viewsToday: number
    viewsThisWeek: number
    viewsThisMonth: number
    uniqueVisitors: number
    bounceRate: number
  }
  performanceStats: {
    averageLoadTime: number
    uptime: number
    errorRate: number
  }
}

// 租户使用限制
export interface TenantLimits {
  maxUsers: number
  maxArticles: number
  maxStorage: number // MB
  maxFileSize: number // MB
  maxCategories: number
  maxTags: number
  enableCustomDomain: boolean
  enableCustomTheme: boolean
  enableAdvancedSEO: boolean
  enablePrioritySupport: boolean
  enableBackup: boolean
  enableAnalytics: boolean
}

// 租户计划信息
export interface TenantPlan {
  id: string
  name: string
  displayName: string
  description: string
  price: number
  currency: string
  billingCycle: 'monthly' | 'yearly'
  limits: TenantLimits
  features: string[]
  isPopular: boolean
  isActive: boolean
}

// 租户域名信息
export interface TenantDomain {
  id: string
  tenantId: string
  domain: string
  isCustom: boolean
  isVerified: boolean
  sslEnabled: boolean
  sslExpiredAt?: string
  createdAt: string
  verificationCode?: string
}

// 租户备份信息
export interface TenantBackup {
  id: string
  tenantId: string
  filename: string
  size: number // bytes
  type: 'manual' | 'automatic'
  status: 'pending' | 'processing' | 'completed' | 'failed'
  progress: number // 0-100
  createdAt: string
  completedAt?: string
  downloadUrl?: string
  expiresAt?: string
}

// 租户邀请码
export interface TenantInvite {
  id: string
  tenantId: string
  code: string
  email?: string
  role: 'user' | 'editor' | 'admin'
  maxUses: number
  usedCount: number
  expiresAt?: string
  isActive: boolean
  createdBy: {
    id: string
    username: string
  }
  createdAt: string
}

// 租户审计日志
export interface TenantAuditLog {
  id: string
  tenantId: string
  action: string
  resource: string
  resourceId?: string
  details: Record<string, any>
  ipAddress: string
  userAgent: string
  userId?: string
  username?: string
  createdAt: string
}

// 兼容历史类型导出
export type TenantUsage = TenantStats
export type TenantInviteCode = TenantInvite
