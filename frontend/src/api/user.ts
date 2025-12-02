import request from './request'
import type {
  User,
  UserCreateData,
  UserUpdateData,
  UserQueryParams,
  LoginCredentials,
  RegisterData,
  UserProfile,
  UserPreferences,
  UserStats,
  SocialAccount,
  PaginatedResponse,
  BatchOperationResponse,
  VipManagementData,
  VipUserQueryParams,
  CurrentUserProfile,
  CurrentUserUpdatePayload
} from '@/types'

const ADMIN_USER_BASE = '/system/admin/users'

// 用户API
export const userApi = {
  // 用户认证
  auth: {
    // 登录
    login(credentials: LoginCredentials) {
      return request.post<{
        user: User
        token: string
        refreshToken: string
        expiresIn: number
      }>('/system/auth/login', credentials, {
        showLoading: true,
        showSuccess: false
      })
    },
    
    // 注册
    register(data: RegisterData) {
      return request.post<{
        user: User
        token: string
        refreshToken: string
        expiresIn: number
      }>('/system/auth/register', data, {
        showLoading: true,
        showSuccess: false
      })
    },
    
    // 登出
    logout() {
      return request.post('/system/auth/logout', {}, {
        showLoading: true
      })
    },
    
    // 刷新token
    refreshToken(refreshToken: string) {
      return request.post<{
        token: string
        refreshToken: string
        expiresIn: number
      }>('/system/auth/refresh', { refreshToken })
    },
    
    // 忘记密码
    forgotPassword(email: string) {
      return request.post('/auth/forgot-password', { email }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '重置密码邮件已发送'
      })
    },
    
    // 重置密码
    resetPassword(token: string, password: string) {
      return request.post('/auth/reset-password', { token, password }, {
        showLoading: true,
        showSuccess: false // 由组件层处理成功提示
      })
    },
    
    // 验证邮箱
    verifyEmail(token: string) {
      return request.post('/auth/verify-email', { token }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '邮箱验证成功'
      })
    },
    
    // 重发验证邮件
    resendVerification(email: string) {
      return request.post('/auth/resend-verification', { email }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '验证邮件已重新发送'
      })
    }
  },
  
  // 用户管理
  // 获取当前用户信息
  getCurrentUser() {
    return request.get<User>('/users/me')
  },
  
  // 更新当前用户信息
  updateCurrentUser(data: CurrentUserUpdatePayload) {
    return request.put<CurrentUserProfile>('/users/me', data, {
      showLoading: true,
      showSuccess: true,
      successMessage: '用户信息更新成功'
    })
  },
  
  // 更改密码
  changePassword(oldPassword: string, newPassword: string) {
    return request.put('/users/me/password', {
      oldPassword,
      newPassword
    }, {
      showLoading: true,
      showSuccess: false // 由组件层处理成功提示
    })
  },
  
  // 获取用户列表
  getUsers(params?: UserQueryParams) {
    return request.get<PaginatedResponse<User>>(ADMIN_USER_BASE, params)
  },
  
  // 获取用户详情
  getUser(id: string) {
    return request.get<User>(`${ADMIN_USER_BASE}/${id}`)
  },
  
  // 创建用户
  createUser(data: UserCreateData) {
    return request.post<User>(ADMIN_USER_BASE, data, {
      showLoading: true,
      showSuccess: true,
      successMessage: '用户创建成功'
    })
  },
  
  // 更新用户
  updateUser(id: string, data: UserUpdateData) {
    return request.put<User>(`${ADMIN_USER_BASE}/${id}`, data, {
      showLoading: true,
      showSuccess: true,
      successMessage: '用户更新成功'
    })
  },
  
  // 删除用户
  deleteUser(id: string) {
    return request.delete(`${ADMIN_USER_BASE}/${id}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '用户删除成功'
    })
  },
  
  // 批量删除用户
  batchDeleteUsers(ids: string[]) {
    return request.post<BatchOperationResponse>(`${ADMIN_USER_BASE}/batch-delete`, { ids }, {
      showLoading: true,
      showSuccess: true,
      successMessage: '批量删除成功'
    })
  },
  
  // 启用/禁用用户
  toggleUserStatus(id: string, enabled: boolean) {
    const status = enabled ? 'active' : 'disabled'
    return request.patch<User>(`${ADMIN_USER_BASE}/${id}/status`, { status }, {
      showLoading: true,
      showSuccess: true,
      successMessage: enabled ? '用户已启用' : '用户已禁用'
    })
  },
  
  // 重置用户密码
  resetUserPassword(id: string, password: string) {
    return request.patch(`${ADMIN_USER_BASE}/${id}/password`, { password }, {
      showLoading: true,
      showSuccess: false // 由组件层处理成功提示
    })
  },
  
  // 用户资料
  // ???  // ?????  // Admin user status helpers
  updateUserStatus(id: string, status: string) {
    return request.patch<User>(`${ADMIN_USER_BASE}/${id}/status`, { status }, {
      showLoading: true,
      showSuccess: true,
      successMessage: 'User status updated'
    })
  },

  batchUpdateStatus(ids: string[], status: string) {
    return request.post(`${ADMIN_USER_BASE}/batch/status`, { ids, status }, {
      showLoading: true,
      showSuccess: true,
      successMessage: 'Batch update succeeded'
    })
  },

  resetPassword(id: string) {
    return request.post<{ password: string }>(`${ADMIN_USER_BASE}/${id}/reset-password`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: 'Temporary password generated'
    })
  },

  profile: {
    // 获取用户资料
    getProfile(userId?: string) {
      const url = userId ? `/users/${userId}/profile` : '/users/me/profile'
      return request.get<UserProfile>(url)
    },
    
    // 更新用户资料
    updateProfile(data: Partial<UserProfile>) {
      return request.put<UserProfile>('/users/me/profile', data, {
        showLoading: true,
        showSuccess: true,
        successMessage: '资料更新成功'
      })
    },
    
    // 上传头像
    uploadAvatar(file: File) {
      return request.upload<{ url: string }>('/users/me/avatar', file, {
        showLoading: true,
        showSuccess: true,
        successMessage: '头像上传成功'
      })
    }
  },
  
  // 用户偏好设置
  preferences: {
    // 获取用户偏好
    getPreferences() {
      return request.get<UserPreferences>('/users/me/preferences')
    },
    
    // 更新用户偏好
    updatePreferences(data: Partial<UserPreferences>) {
      return request.put<UserPreferences>('/users/me/preferences', data, {
        showLoading: true,
        showSuccess: true,
        successMessage: '偏好设置已保存'
      })
    }
  },
  
  // 社交账户
  social: {
    // 获取社交账户列表
    getSocialAccounts() {
      return request.get<SocialAccount[]>('/users/me/social-accounts')
    },
    
    // 绑定社交账户
    bindSocialAccount(provider: string, code: string) {
      return request.post<SocialAccount>('/users/me/social-accounts', {
        provider,
        code
      }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '社交账户绑定成功'
      })
    },
    
    // 解绑社交账户
    unbindSocialAccount(id: string) {
      return request.delete(`/users/me/social-accounts/${id}`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '社交账户解绑成功'
      })
    }
  },
  
  // 用户统计
  stats: {
    // 获取用户统计信息
    getUserStats(userId?: string) {
      const url = userId ? `/users/${userId}/stats` : '/users/me/stats'
      return request.get<UserStats>(url)
    },
    
    // 获取用户活动统计
    getActivityStats(userId?: string, period: 'day' | 'week' | 'month' | 'year' = 'month') {
      const url = userId ? `/users/${userId}/activity-stats` : '/users/me/activity-stats'
      return request.get(url, { period })
    }
  },
  
  // 用户搜索
  search: {
    // 搜索用户
    searchUsers(keyword: string, filters?: Record<string, any>) {
      return request.get<PaginatedResponse<User>>('/users/search', {
        keyword,
        ...filters
      })
    },
    
    // 获取用户建议
    getUserSuggestions(keyword: string, limit: number = 10) {
      return request.get<User[]>('/users/suggestions', {
        keyword,
        limit
      })
    }
  },
  
  // 用户导入导出
  import: {
    // 导入用户
    importUsers(file: File) {
      return request.upload('/users/import', file, {
        showLoading: true,
        showSuccess: true,
        successMessage: '用户导入任务已启动'
      })
    },
    
    // 导出用户
    exportUsers(params?: UserQueryParams) {
      return request.post('/users/export', params, {
        showLoading: true,
        showSuccess: true,
        successMessage: '用户导出任务已启动'
      })
    }
  },
  
  // 用户操作日志
  logs: {
    // 获取用户操作日志
    getUserLogs(userId?: string, params?: {
      page?: number
      limit?: number
      action?: string
      startDate?: string
      endDate?: string
    }) {
      const url = userId ? `/users/${userId}/logs` : '/users/me/logs'
      return request.get(url, params)
    }
  },
  
  // 用户会话管理
  sessions: {
    // 获取用户会话列表
    getSessions() {
      return request.get('/users/me/sessions')
    },
    
    // 终止指定会话
    terminateSession(sessionId: string) {
      return request.delete(`/users/me/sessions/${sessionId}`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '会话已终止'
      })
    },
    
    // 终止所有其他会话
    terminateOtherSessions() {
      return request.delete('/users/me/sessions/others', {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '其他会话已全部终止'
      })
    }
  },

  // VIP管理相关API
  vip: {
    // 设置用户为VIP
    setUserVip(data: VipManagementData) {
      return request.post(`${ADMIN_USER_BASE}/${data.userId}/vip`, { days: data.days }, {
        showLoading: true,
        showSuccess: true,
        successMessage: 'VIP设置成功'
      })
    },

    // 取消用户VIP
    cancelUserVip(userId: string) {
      return request.delete(`${ADMIN_USER_BASE}/${userId}/vip`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: 'VIP取消成功'
      })
    },

    // 延长用户VIP
    extendUserVip(data: VipManagementData) {
      return request.patch(`${ADMIN_USER_BASE}/${data.userId}/vip/extend`, { days: data.days }, {
        showLoading: true,
        showSuccess: true,
        successMessage: 'VIP延长成功'
      })
    },

    // 获取即将过期的VIP用户
    getExpiringVipUsers(params?: VipUserQueryParams) {
      return request.get<PaginatedResponse<User>>(`${ADMIN_USER_BASE}/expiring-vip`, params)
    },

    // 获取已过期的VIP用户
    getExpiredVipUsers(params?: VipUserQueryParams) {
      return request.get<PaginatedResponse<User>>(`${ADMIN_USER_BASE}/expired-vip`, params)
    },

    // 处理过期VIP用户
    processExpiredVipUsers() {
      return request.post(`${ADMIN_USER_BASE}/process-expired-vip`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '过期VIP用户处理完成'
      })
    }
  }
}

export default userApi
