import request, { service } from './request'
import type {
  Tenant,
  TenantConfig,
  TenantCreateData,
  TenantStats,
  TenantUsage,
  TenantPlan,
  TenantDomain,
  TenantBackup,
  TenantInviteCode,
  TenantAuditLog,
  PaginatedResponse,
  BatchOperationResponse,
  RequestConfig
} from '@/types'

const TENANT_CONFIG_BASE = '/system/admin/tenant-config'
const TENANT_ADMIN_BASE = '/system/admin/tenants'

// 租户API
export const tenantApi = {
  // 租户管理
  // 获取当前租户信息
  getCurrentTenant() {
    return request.get<Tenant>('/system/tenants/current')
  },

  // 获取当前域名的租户信息（公开接口，不需要登录）
  getCurrentTenantByDomain(domain?: string) {
    return request.get<Tenant>('/system/public/tenants/by-domain', { domain })
  },

  getPublicOwner() {
    return request.get<{ qrCode: string; remark: string }>('/system/public/tenants/current/owner')
  },

  // 租户后台配置
  getSettings(tenantId?: string) {
    return request.get(`${TENANT_CONFIG_BASE}/settings`, tenantId ? { tenantId } : undefined)
  },

  updateSettings(settings: any, tenantId?: string) {
    const payload = tenantId ? { ...settings, tenantId } : settings
    return request.put(`${TENANT_CONFIG_BASE}/settings`, payload, {
      showLoading: true,
      showSuccess: false
    })
  },

  testEmailConnection(settings: any) {
    return request.post(`${TENANT_CONFIG_BASE}/email/test`, settings, {
      showLoading: true,
      showSuccess: false
    })
  },

  // 获取租户配置（公开接口，不需要登录）
  getTenantConfigPublic(tenantId: string) {
    return request.get<any>(`/system/public/tenants/current/config`)
  },
  
  // 获取租户列表
  getTenants(params?: Record<string, any>) {
    return request.get<PaginatedResponse<Tenant>>(`${TENANT_ADMIN_BASE}/list`, params)
  },
  
  // 获取租户详情
  getTenant(id: string) {
    return request.get<Tenant>(`${TENANT_ADMIN_BASE}/${id}`)
  },
  
  // 创建租户
  createTenant(data: TenantCreateData) {
    return request.post<Tenant>(TENANT_ADMIN_BASE, data, {
      showLoading: true,
      showSuccess: true,
      successMessage: '租户创建成功'
    })
  },
  
  // 更新租户
  updateTenant(id: string | number, data: Partial<Tenant> & { tenantId?: string | number }) {
    const payload = {
      ...data,
      tenantId: data.tenantId ?? id
    }
    return request.put<Tenant>(TENANT_ADMIN_BASE, payload, {
      showLoading: true,
      showSuccess: true,
      successMessage: '租户更新成功'
    })
  },
  
  // 删除租户
  deleteTenant(id: string | number) {
    return request.delete(`${TENANT_ADMIN_BASE}/${id}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '租户删除成功'
    })
  },
  
  // 批量删除租户
  batchDeleteTenants(ids: Array<string | number>) {
    const tenantIds = ids.map(id => String(id)).join(',')
    return request.delete<BatchOperationResponse>(`${TENANT_ADMIN_BASE}/${tenantIds}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '批量删除成功'
    })
  },
  
  // 启用/禁用租户
  updateTenantStatus(id: string | number, status: string, options?: Partial<RequestConfig>) {
    return request.put<Tenant>(`${TENANT_ADMIN_BASE}/changeStatus`, {
      tenantId: id,
      status
    }, {
      showLoading: options?.showLoading ?? true,
      showSuccess: options?.showSuccess ?? true,
      successMessage: options?.successMessage ?? (status === '0' ? '租户已启用' : '租户已禁用')
    })
  },
  
  // 租户配置
  config: {
    // 获取租户配置
    getConfig(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/config` : '/system/tenants/current/config'
      return request.get<TenantConfig>(url)
    },
    
    // 更新租户配置
    updateConfig(config: Partial<TenantConfig>, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/config` : '/system/tenants/current/config'
      return request.put<TenantConfig>(url, config, {
        showLoading: true,
        showSuccess: true,
        successMessage: '配置更新成功'
      })
    },
    
    // 重置租户配置
    resetConfig(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/config/reset` : '/system/tenants/current/config/reset'
      return request.post<TenantConfig>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '配置已重置'
      })
    }
  },
  
  // 统计信息
  stats: {
    // 获取租户统计信息
    getStats(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/stats` : '/system/stats'
      return request.get<TenantStats>(url)
    },
    
    // 获取租户使用情况
    getUsage(tenantId?: string, period: 'day' | 'week' | 'month' | 'year' = 'month') {
      const url = tenantId ? `/system/tenants/${tenantId}/usage` : '/system/stats/usage'
      return request.get<TenantUsage>(url, { params: { period } })
    },
    
    // 获取租户活动统计
    getActivityStats(tenantId?: string, period: 'day' | 'week' | 'month' | 'year' = 'month') {
      const url = tenantId ? `/system/tenants/${tenantId}/activity` : '/system/stats/activity'
      return request.get<any>(url, { params: { period } })
    }
  },
  
  // 租户套餐
  plan: {
    // 获取租户套餐
    getPlan(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/plan` : '/system/tenants/current/plan'
      return request.get<TenantPlan>(url)
    },
    
    // 升级套餐
    upgradePlan(planId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/plan/upgrade` : '/system/tenants/current/plan/upgrade'
      return request.post<TenantPlan>(url, { planId }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '套餐升级成功'
      })
    },
    
    // 降级套餐
    downgradePlan(planId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/plan/downgrade` : '/system/tenants/current/plan/downgrade'
      return request.post<TenantPlan>(url, { planId }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '套餐降级成功'
      })
    },
    
    // 取消套餐
    cancelPlan(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/plan/cancel` : '/system/tenants/current/plan/cancel'
      return request.post<void>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '套餐已取消'
      })
    }
  },
  
  // 域名管理
  domain: {
    // 获取域名列表
    getDomains(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/domains` : '/system/tenants/current/domains'
      return request.get<TenantDomain[]>(url)
    },
    
    // 添加域名
    addDomain(domain: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/domains` : '/system/tenants/current/domains'
      return request.post<TenantDomain>(url, { domain }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '域名添加成功'
      })
    },
    
    // 验证域名
    verifyDomain(domainId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/domains/${domainId}/verify` : `/system/tenants/current/domains/${domainId}/verify`
      return request.post<TenantDomain>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '域名验证成功'
      })
    },
    
    // 设置主域名
    setPrimaryDomain(domainId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/domains/${domainId}/primary` : `/system/tenants/current/domains/${domainId}/primary`
      return request.post<TenantDomain>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '主域名设置成功'
      })
    },
    
    // 删除域名
    deleteDomain(domainId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/domains/${domainId}` : `/system/tenants/current/domains/${domainId}`
      return request.delete(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '域名删除成功'
      })
    }
  },
  
  // 备份管理
  backup: {
    // 获取备份列表
    getBackups(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/backups` : '/system/tenants/current/backups'
      return request.get<TenantBackup[]>(url)
    },
    
    // 创建备份
    createBackup(type: 'full' | 'incremental' = 'full', tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/backups` : '/system/tenants/current/backups'
      return request.post<TenantBackup>(url, { type }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '备份创建成功'
      })
    },
    
    // 恢复备份
    restoreBackup(backupId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/backups/${backupId}/restore` : `/system/tenants/current/backups/${backupId}/restore`
      return request.post<void>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '备份恢复成功'
      })
    },
    
    // 下载备份
    downloadBackup(backupId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/backups/${backupId}/download` : `/system/tenants/current/backups/${backupId}/download`
      return request.get(url, { responseType: 'blob' })
    },
    
    // 删除备份
    deleteBackup(backupId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/backups/${backupId}` : `/system/tenants/current/backups/${backupId}`
      return request.delete(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '备份删除成功'
      })
    }
  },
  
  // 邀请码管理
  invite: {
    // 获取邀请码列表
    getInviteCodes(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/invite-codes` : '/system/tenants/current/invite-codes'
      return request.get<TenantInviteCode[]>(url)
    },
    
    // 创建邀请码
    createInviteCode(data: {
      role: string
      expiresAt?: string
      maxUses?: number
      note?: string
    }, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/invite-codes` : '/system/tenants/current/invite-codes'
      return request.post<TenantInviteCode>(url, data, {
        showLoading: true,
        showSuccess: true,
        successMessage: '邀请码创建成功'
      })
    },
    
    // 禁用邀请码
    disableInviteCode(codeId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/invite-codes/${codeId}/disable` : `/system/tenants/current/invite-codes/${codeId}/disable`
      return request.post<TenantInviteCode>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '邀请码已禁用'
      })
    },
    
    // 删除邀请码
    deleteInviteCode(codeId: string, tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/invite-codes/${codeId}` : `/system/tenants/current/invite-codes/${codeId}`
      return request.delete(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '邀请码删除成功'
      })
    },
    
    // 通过邀请码加入租户
    joinByInviteCode(code: string) {
      return request.post<void>('/system/tenants/join', { code }, {
        showLoading: true,
        showSuccess: true,
        successMessage: '加入租户成功'
      })
    }
  },
  
  // 审计日志
  audit: {
    // 获取审计日志
    getAuditLogs(tenantId?: string, params?: {
      page?: number
      limit?: number
      action?: string
      userId?: string
      startDate?: string
      endDate?: string
    }) {
      const url = tenantId ? `/system/tenants/${tenantId}/audit-logs` : '/system/tenants/current/audit-logs'
      return request.get<PaginatedResponse<TenantAuditLog>>(url, { params })
    },
    
    // 导出审计日志
    exportAuditLogs(tenantId?: string, params?: {
      action?: string
      userId?: string
      startDate?: string
      endDate?: string
    }) {
      const url = tenantId ? `/system/tenants/${tenantId}/audit-logs/export` : '/system/tenants/current/audit-logs/export'
      return request.get(url, {
        params,
        responseType: 'blob'
      })
    }
  },
  
  // 搜索功能
  search: {
    // 搜索租户
    searchTenants(keyword: string, filters?: Record<string, any>) {
      return request.get<PaginatedResponse<Tenant>>('/system/tenants/search', {
        params: { keyword, ...filters }
      })
    },
    
    // 获取租户建议
    getTenantSuggestions(keyword: string, limit: number = 10) {
      return request.get<Tenant[]>('/system/tenants/suggestions', {
        params: { keyword, limit }
      })
    }
  },
  
  // 导入导出
  import: {
    // 导入租户
    importTenants(file: File) {
      const formData = new FormData()
      formData.append('file', file)
      return request.post<BatchOperationResponse>('/system/tenants/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    
    // 导出租户
    exportTenants(params?: Record<string, any>) {
      return service.post(`${TENANT_ADMIN_BASE}/export`, {}, {
        params,
        responseType: 'blob'
      }).then(response => {
        const blob = new Blob([response.data])
        const downloadUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = '租户列表.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(downloadUrl)
      })
    }
  },
  
  // 初始化
  init: {
    // 初始化租户
    initTenant(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/init` : '/system/tenants/current/init'
      return request.post<void>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '租户初始化成功'
      })
    },
    
    // 重置租户
    resetTenant(tenantId?: string) {
      const url = tenantId ? `/system/tenants/${tenantId}/reset` : '/system/tenants/current/reset'
      return request.post<void>(url, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '租户重置成功'
      })
    }
  },
  
  // 通过域名获取租户
  getTenantByDomain(domain: string) {
    return request.get<Tenant>(`/system/public/tenants/by-domain?domain=${domain}`)
  },
  
  // 检查租户名称可用性
  checkNameAvailability(name: string) {
    return request.get<{ available: boolean }>(`/system/tenants/check-name?name=${name}`)
  },
  
  // 检查域名可用性
  checkDomainAvailability(domain: string) {
    return request.get<{ available: boolean }>(`/system/tenants/check-domain?domain=${domain}`)
  }
}

export default tenantApi
