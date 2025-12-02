import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { tenantApi } from '@/api/tenant'
import { siteDefaults } from '@/config/site'
import type { Tenant, TenantConfig, TenantCreateData, TenantUpdateData } from '@/types/tenant'
import { ensureAbsoluteUrl } from '@/utils/url'

const legacySiteNames = new Set(['SimpleShare', '简单分享'])

const toAbsoluteFileUrl = (value: string): string => ensureAbsoluteUrl(value)

// 接口定义已移至 @/types/tenant

export const useTenantStore = defineStore('tenant', () => {
  // 状态
  const currentTenant = ref<Tenant | null>(null)
  const tenantConfig = ref<Partial<TenantConfig> | null>(null)
  const tenants = ref<Tenant[]>([])
  const isLoading = ref(false)
  const configLoading = ref(false)
  const createLoading = ref(false)
  const updateLoading = ref(false)
  const deleteLoading = ref(false)

  // 计算属性
  const hasTenant = computed(() => !!currentTenant.value)
  const tenantId = computed(() => currentTenant.value?.id || null)
  const tenantName = computed(() => {
    const value = currentTenant.value?.name
    if (typeof value === 'string') {
      const trimmed = value.trim()
      if (trimmed && !legacySiteNames.has(trimmed)) {
        return trimmed
      }
    }
    return siteDefaults.title
  })
  const tenantDomain = computed(() => currentTenant.value?.domain || '')
  const tenantLogo = computed(() => currentTenant.value?.logo || '')
  const isActive = computed(() => currentTenant.value?.status === 'active')
  
  const hasConfig = computed(() => !!tenantConfig.value)
  const siteTitle = computed(() => {
    const value = tenantConfig.value?.siteTitle
    if (typeof value === 'string') {
      const trimmed = value.trim()
      if (trimmed && !legacySiteNames.has(trimmed)) {
        return trimmed
      }
    }
    return siteDefaults.title
  })
  const siteDescription = computed(() => {
    const value = tenantConfig.value?.siteDescription
    return typeof value === 'string' && value.trim() ? value.trim() : siteDefaults.description
  })
  const allowRegistration = computed(() => tenantConfig.value?.allowRegistration ?? true)
  const requireEmailVerification = computed(() => tenantConfig.value?.requireEmailVerification ?? false)
  const enableComments = computed(() => tenantConfig.value?.enableComments ?? true)
  const enableLikes = computed(() => tenantConfig.value?.enableLikes ?? true)
  const enableSharing = computed(() => tenantConfig.value?.enableSharing ?? true)
  const maxFileSize = computed(() => tenantConfig.value?.maxFileSize || 10)
  const allowedFileTypes = computed(() => tenantConfig.value?.allowedFileTypes || [])
  
  const copyPolicy = computed(() => (tenantConfig.value?.copyPolicy as string) || 'follow_article')
  const enableWatermark = computed(() => parseBoolean(tenantConfig.value?.enableWatermark, true))
  const watermarkText = computed(() => {
    const value = tenantConfig.value?.watermarkText
    if (typeof value === 'string') {
      const trimmed = value.trim()
      if (trimmed && !legacySiteNames.has(trimmed)) {
        return trimmed
      }
    }
    const name = tenantName.value.trim()
    if (name && !legacySiteNames.has(name)) {
      return name
    }
    return siteDefaults.watermarkText
  })
  const watermarkOpacity = computed(() => Math.min(Math.max(parseNumber((tenantConfig.value as any)?.watermarkOpacity, 0.3), 0), 1))
  const disableRightClick = computed(() => parseBoolean(tenantConfig.value?.disableRightClick, false))
  const disableTextSelection = computed(() => parseBoolean(tenantConfig.value?.disableTextSelection, false))
  const disableDevTools = computed(() => parseBoolean(tenantConfig.value?.disableDevTools, false))
  
  const themeConfig = computed(() => tenantConfig.value?.theme || {
    primaryColor: '#409EFF',
    secondaryColor: '#67C23A',
    backgroundColor: '#FFFFFF',
    textColor: '#303133'
  })

  // 租户信息（用于首页显示）
  const tenantInfo = computed(() => ({
    title: siteTitle.value,
    name: tenantName.value,
    description: siteDescription.value,
    logo: tenantLogo.value,
    domain: tenantDomain.value
  }))

  const parseBoolean = (value: any, fallback = false) => {
    if (value === undefined || value === null) {
      return fallback
    }
    if (typeof value === 'boolean') {
      return value
    }
    if (typeof value === 'number') {
      return value !== 0
    }
    if (typeof value === 'string') {
      const lowered = value.toLowerCase()
      return ['true', '1', 'yes', 'on'].includes(lowered)
    }
    return fallback
  }

  const parseNumber = (value: any, fallback: number) => {
    if (value === undefined || value === null) {
      return fallback
    }
    const numeric = Number(value)
    return Number.isNaN(numeric) ? fallback : numeric
  }

  const normalizeAllowedFileTypes = (value: any): string[] => {
    if (Array.isArray(value)) {
      return value
        .map(item => (item == null ? '' : String(item)))
        .map(item => item.trim())
        .filter(Boolean)
    }
    if (typeof value === 'string') {
      return value
        .split(',')
        .map(item => item.trim())
        .filter(Boolean)
    }
    return []
  }

  const normalizeTenantConfig = (config: any): Partial<TenantConfig> => {
    const base: Record<string, any> = {
      siteTitle: siteDefaults.title,
      siteDescription: siteDefaults.description,
      allowRegistration: true,
      requireEmailVerification: false,
      enableComments: true,
      enableLikes: true,
      enableSharing: true,
      maxFileSize: 10,
      allowedFileTypes: ['jpg', 'jpeg', 'png', 'gif', 'pdf', 'doc', 'docx'],
      theme: {
        primaryColor: '#409EFF',
        secondaryColor: '#67C23A',
        backgroundColor: '#FFFFFF',
        textColor: '#303133'
      },
      copyPolicy: 'follow_article',
      enableWatermark: true,
      watermarkText: siteDefaults.watermarkText,
      watermarkOpacity: 0.3,
      disableRightClick: true,
      disableTextSelection: true,
      disableDevTools: false,
      ownerQrCode: '',
      ownerRemark: ''
    }

    const merged: Record<string, any> = { ...base, ...(tenantConfig.value || {}), ...(config || {}) }
    const ownerConfig = (config as any)?.owner ?? {}
    merged.copyPolicy = config?.copyPolicy ?? config?.copy_policy ?? merged.copyPolicy
    merged.enableWatermark = parseBoolean(config?.enableWatermark ?? config?.watermark_enabled ?? merged.enableWatermark, true)
    merged.watermarkText = config?.watermarkText ?? config?.watermark_text ?? merged.watermarkText
    merged.watermarkOpacity = Math.min(Math.max(parseNumber(config?.watermarkOpacity ?? config?.watermark_opacity ?? merged.watermarkOpacity, merged.watermarkOpacity), 0), 1)
    merged.disableRightClick = parseBoolean(config?.disableRightClick ?? config?.disable_right_click ?? merged.disableRightClick, merged.disableRightClick)
    merged.disableTextSelection = parseBoolean(config?.disableTextSelection ?? config?.disable_text_selection ?? merged.disableTextSelection, merged.disableTextSelection)
    merged.disableDevTools = parseBoolean(config?.disableDevTools ?? config?.disable_dev_tools ?? merged.disableDevTools, merged.disableDevTools)

    const normalizedFileTypes = normalizeAllowedFileTypes(merged.allowedFileTypes)
    merged.allowedFileTypes = normalizedFileTypes.length
      ? normalizedFileTypes
      : normalizeAllowedFileTypes(base.allowedFileTypes)

    const normalizedTitle = typeof merged.siteTitle === 'string' ? merged.siteTitle.trim() : ''
    merged.siteTitle = normalizedTitle && !legacySiteNames.has(normalizedTitle) ? normalizedTitle : siteDefaults.title

    const normalizedWatermark = typeof merged.watermarkText === 'string' ? merged.watermarkText.trim() : ''
    merged.watermarkText = normalizedWatermark && !legacySiteNames.has(normalizedWatermark) ? normalizedWatermark : siteDefaults.watermarkText

    const ownerQrCandidate =
      (config as any)?.ownerQrCode ??
      ownerConfig.qrCode ??
      ownerConfig.qr_url ??
      ownerConfig.qrUrl ??
      ownerConfig.url ??
      ownerConfig.path ??
      merged.ownerQrCode
    const ownerRemarkCandidate =
      (config as any)?.ownerRemark ??
      ownerConfig.remark ??
      ownerConfig.note ??
      merged.ownerRemark
    const normalizedOwnerQr = typeof ownerQrCandidate === 'string' ? ownerQrCandidate.trim() : ''
    merged.ownerQrCode = normalizedOwnerQr ? toAbsoluteFileUrl(normalizedOwnerQr) : ''
    merged.ownerRemark = typeof ownerRemarkCandidate === 'string' ? ownerRemarkCandidate.trim() : ''
    merged.owner = {
      qrCode: merged.ownerQrCode,
      remark: merged.ownerRemark
    }
    return merged
  }

  // 获取当前租户信息
  const fetchCurrentTenant = async () => {
    try {
      isLoading.value = true

      // 暂时使用默认租户配置，避免API调用问题
      currentTenant.value = {
        id: 'default',
        name: siteDefaults.title,
        domain: window.location.hostname,
        status: 'active',
        description: siteDefaults.description,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      return currentTenant.value
    } catch (error: any) {
      // 如果出错，也使用默认值
      console.log('使用默认租户配置')
      currentTenant.value = {
        id: 'default',
        name: siteDefaults.title,
        domain: window.location.hostname,
        status: 'active',
        description: siteDefaults.description,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      return currentTenant.value
    } finally {
      isLoading.value = false
    }
  }

  // 获取租户配置
  const fetchTenantConfig = async () => {
    try {
      configLoading.value = true

      // 使用公开接口获取租户配置
      const tenantId = currentTenant.value?.id || '1'
      const response = await tenantApi.getTenantConfigPublic(tenantId)
      tenantConfig.value = normalizeTenantConfig(response)

      return tenantConfig.value
    } catch (error: any) {
      // 如果公开接口失败，使用默认配置
      console.log('使用默认租户配置')
      tenantConfig.value = normalizeTenantConfig({})
      return tenantConfig.value
    } finally {
      configLoading.value = false
    }
  }

  // 更新租户配置
  const updateTenantConfig = async (config: Partial<TenantConfig>) => {
    try {
      configLoading.value = true
      
      const response = await tenantApi.config.updateConfig(config)
      tenantConfig.value = response
      
      ElMessage.success('配置更新成功')
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '更新配置失败')
      throw error
    } finally {
      configLoading.value = false
    }
  }

  // 获取租户列表（管理员功能）
  const fetchTenants = async () => {
    try {
      isLoading.value = true
      
      const response = await tenantApi.getTenants()
      tenants.value = response
      
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '获取租户列表失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 创建租户（超级管理员功能）
  const createTenant = async (data: TenantCreateData) => {
    try {
      createLoading.value = true
      
      const response = await tenantApi.createTenant(data)
      
      // 添加到列表
      tenants.value.push(response)
      
      ElMessage.success('租户创建成功')
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '创建租户失败')
      throw error
    } finally {
      createLoading.value = false
    }
  }

  // 更新租户信息
  const updateTenant = async (id: string, data: TenantUpdateData) => {
    try {
      updateLoading.value = true
      
      const response = await tenantApi.updateTenant(id, data)
      
      // 更新列表中的租户
      const index = tenants.value.findIndex(tenant => tenant.id === id)
      if (index !== -1) {
        tenants.value[index] = response
      }
      
      // 更新当前租户
      if (currentTenant.value?.id === id) {
        currentTenant.value = response
      }
      
      ElMessage.success('租户信息更新成功')
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '更新租户信息失败')
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  // 删除租户（超级管理员功能）
  const deleteTenant = async (id: string) => {
    try {
      deleteLoading.value = true
      
      await tenantApi.deleteTenant(id)
      
      // 从列表中移除
      const index = tenants.value.findIndex(tenant => tenant.id === id)
      if (index !== -1) {
        tenants.value.splice(index, 1)
      }
      
      ElMessage.success('租户删除成功')
    } catch (error: any) {
      ElMessage.error(error.message || '删除租户失败')
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 设置当前租户
  const setCurrentTenant = (tenant: Tenant) => {
    currentTenant.value = tenant
  }

  // 清除当前租户
  const clearCurrentTenant = () => {
    currentTenant.value = null
    tenantConfig.value = null
  }

  // 初始化
  const initTenant = async () => {
    // 如果已经有租户信息，跳过初始化
    if (hasTenant.value && hasConfig.value) {
      return
    }

    try {
      const promises = []

      // 只在没有租户信息时获取
      if (!hasTenant.value) {
        promises.push(fetchCurrentTenant())
      }

      // 只在没有配置信息时获取
      if (!hasConfig.value) {
        promises.push(fetchTenantConfig())
      }

      if (promises.length > 0) {
        await Promise.all(promises)
      }
    } catch (error) {
      console.error('租户初始化失败:', error)
      // fetchCurrentTenant 和 fetchTenantConfig 已经有默认值处理，这里不需要额外处理
    }
  }

  return {
    // 状态
    currentTenant,
    tenantConfig,
    tenants,
    isLoading,
    configLoading,
    createLoading,
    updateLoading,
    deleteLoading,
    
    // 计算属性
    hasTenant,
    tenantId,
    tenantName,
    tenantDomain,
    tenantLogo,
    isActive,
    hasConfig,
    siteTitle,
    siteDescription,
    allowRegistration,
    requireEmailVerification,
    enableComments,
    enableLikes,
    enableSharing,
    maxFileSize,
    allowedFileTypes,
    copyPolicy,
    enableWatermark,
    watermarkText,
    watermarkOpacity,
    disableRightClick,
    disableTextSelection,
    disableDevTools,
    themeConfig,
    tenantInfo,
    
    // 方法
    fetchCurrentTenant,
    fetchTenantConfig,
    updateTenantConfig,
    fetchTenants,
    createTenant,
    updateTenant,
    deleteTenant,
    setCurrentTenant,
    clearCurrentTenant,
    initTenant
  }
}, {
  persist: {
    key: 'simple-share-tenant',
    storage: localStorage,
    paths: ['currentTenant', 'tenantConfig']
  }
})
