import request from './request'
import type {
  Category,
  CategoryCreateData,
  CategoryUpdateData,
  CategoryQueryParams,
  CategoryTreeNode,
  CategoryOption,
  CategoryStats,
  CategoryMoveData,
  CategoryBatchOperation,
  CategoryPath,
  CategoryValidationRule,
  CategorySearchResult,
  CategoryArticleInfo,
  CategoryUsageStats,
  PaginatedResponse,
  BatchOperationResponse,
  SearchResponse
} from '@/types'

const ADMIN_CATEGORY_BASE = '/article/admin/categories'

// 分类API基础路径
const CATEGORY_BASE = '/article/categories'

export const categoryApi = {
  // 基础CRUD操作
  getCategories(params?: CategoryQueryParams) {
    return request.get<Category[]>(CATEGORY_BASE, { params })
  },

  getCategoryTree(params?: {
    includeArticleCount?: boolean
    maxDepth?: number
    parentId?: string
  }) {
    return request.get<CategoryTreeNode[]>('/article/categories/tree', { params })
  },

  getCategory(id: string) {
    return request.get<Category>(`/article/categories/${id}`)
  },

  getAdminCategories(params?: CategoryQueryParams) {
    return request.get<PaginatedResponse<Category>>(`${ADMIN_CATEGORY_BASE}`, { params })
  },

  // 兼容旧版本的方法
  create(data: Partial<Category> & { name: string; enabled?: boolean }) {
    return request.post<Category>(`${ADMIN_CATEGORY_BASE}`, data)
  },

  update(data: Partial<Category> & { id: string }) {
    const { id, ...updateData } = data
    return request.put<Category>(`${ADMIN_CATEGORY_BASE}/${id}`, updateData)
  },

  delete(id: string) {
    return request.delete(`${ADMIN_CATEGORY_BASE}/${id}`)
  },

  // 新版本的方法
  async createCategory(data: CategoryCreateData) {
    const result = await request.post<Category>(`${ADMIN_CATEGORY_BASE}`, data)
    return result
  },

  async updateCategory(id: string, data: CategoryUpdateData) {
    const result = await request.put<Category>(`${ADMIN_CATEGORY_BASE}/${id}`, data)
    return result
  },

  async deleteCategory(id: string, options?: {
    moveArticlesTo?: string
    deleteArticles?: boolean
  }) {
    const result = await request.delete(`${ADMIN_CATEGORY_BASE}/${id}`, { 
      params: options 
    })
    return result
  },

  batchDeleteCategories(ids: string[], options?: {
    moveArticlesTo?: string
    deleteArticles?: boolean
  }) {
    return request.delete<BatchOperationResponse>(`${ADMIN_CATEGORY_BASE}/batch`, {
      data: { ids, ...options }
    })
  },

  moveCategory(id: string, data: CategoryMoveData) {
    return request.put<Category>(`${ADMIN_CATEGORY_BASE}/${id}/move`, data)
  },

  sortCategories(parentId: string | null, categoryIds: string[]) {
    return request.put<BatchOperationResponse>(`${ADMIN_CATEGORY_BASE}/sort`, {
      parentId,
      categoryIds
    })
  },

  // 批量操作
  batch: {
    batchUpdate(operation: CategoryBatchOperation) {
      return request.put<BatchOperationResponse>(`${ADMIN_CATEGORY_BASE}/batch`, operation)
    },

    batchMove(ids: string[], parentId: string | null) {
      return request.put<BatchOperationResponse>(`${ADMIN_CATEGORY_BASE}/batch/move`, {
        ids,
        parentId
      })
    }
  },

  // 统计信息
  stats: {
    getStats() {
      return request.get<CategoryStats>(`${ADMIN_CATEGORY_BASE}/stats`)
    },

    getUsageStats(id: string, period: 'day' | 'week' | 'month' | 'year' = 'month') {
      return request.get<CategoryUsageStats>(`${ADMIN_CATEGORY_BASE}/${id}/stats`, { params: { period } })
    },

    getPopularCategories(limit: number = 10, period: 'day' | 'week' | 'month' | 'year' = 'week') {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/popular`, { params: { limit, period } })
    }
  },

  // 搜索功能
  search: {
    searchCategories(keyword: string, filters?: Record<string, any>) {
      return request.get<SearchResponse<Category>>(`${ADMIN_CATEGORY_BASE}/search`, {
        params: { keyword, ...filters }
      })
    },

    getCategorySuggestions(keyword: string, limit: number = 10) {
      return request.get<CategorySearchResult[]>(`${ADMIN_CATEGORY_BASE}/suggestions`, {
        params: { keyword, limit }
      })
    }
  },

  // 选项获取
  options: {
    getCategoryOptions(params?: {
      includeEmpty?: boolean
      maxDepth?: number
      parentId?: string
    }) {
      return request.get<CategoryOption[]>(`${ADMIN_CATEGORY_BASE}/options`, { params })
    },

    getFlatCategoryOptions(params?: {
      includeEmpty?: boolean
      separator?: string
    }) {
      return request.get<CategoryOption[]>(`${ADMIN_CATEGORY_BASE}/options/flat`, { params })
    }
  },

  // 路径相关
  path: {
    getCategoryPath(id: string) {
      return request.get<CategoryPath>(`${ADMIN_CATEGORY_BASE}/${id}/path`)
    },

    getCategoryBreadcrumb(id: string) {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/${id}/breadcrumb`)
    }
  },

  // 文章相关
  articles: {
    getCategoryArticles(id: string, params?: {
      page?: number
      limit?: number
      status?: string
      sortBy?: string
      sortOrder?: 'asc' | 'desc'
    }) {
      return request.get<PaginatedResponse<CategoryArticleInfo>>(`${ADMIN_CATEGORY_BASE}/${id}/articles`, { params })
    },

    getCategoryArticleStats(id: string) {
      return request.get<{
        total: number
        published: number
        draft: number
        archived: number
      }>(`${ADMIN_CATEGORY_BASE}/${id}/articles/stats`)
    }
  },

  // 导入导出
  import: {
    importCategories(file: File, options?: {
      overwrite?: boolean
      preserveHierarchy?: boolean
    }) {
      const formData = new FormData()
      formData.append('file', file)
      if (options) {
        Object.entries(options).forEach(([key, value]) => {
          formData.append(key, String(value))
        })
      }
      
      return request.post<{
        success: number
        failed: number
        errors: string[]
      }>(`${ADMIN_CATEGORY_BASE}/import`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    },

    exportCategories(params?: {
      ids?: string[]
      includeArticles?: boolean
      format?: 'json' | 'csv' | 'xlsx'
    }) {
      return request.get(`${ADMIN_CATEGORY_BASE}/export`, {
        params,
        responseType: 'blob'
      })
    },

    getImportTemplate(format: 'csv' | 'xlsx' = 'xlsx') {
      return request.get(`${ADMIN_CATEGORY_BASE}/import/template`, { params: { format }, responseType: 'blob' })
    }
  },

  // 验证
  validation: {
    validateName(name: string, parentId?: string, excludeId?: string) {
      return request.post<CategoryValidationRule>(`${ADMIN_CATEGORY_BASE}/validate/name`, {
        name,
        parentId,
        excludeId
      })
    },

    validatePath(path: string, excludeId?: string) {
      return request.post<CategoryValidationRule>(`${ADMIN_CATEGORY_BASE}/validate/path`, {
        path,
        excludeId
      })
    },

    getValidationRules() {
      return request.get<CategoryValidationRule[]>(`${ADMIN_CATEGORY_BASE}/validation/rules`)
    }
  },

  // 关系操作
  relations: {
    getChildren(id: string, params?: {
      includeArticleCount?: boolean
      recursive?: boolean
    }) {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/${id}/children`, { params })
    },

    getParent(id: string) {
      return request.get<Category>(`${ADMIN_CATEGORY_BASE}/${id}/parent`)
    },

    getAncestors(id: string) {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/${id}/ancestors`)
    },

    getDescendants(id: string, maxDepth?: number) {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/${id}/descendants`, { params: { maxDepth } })
    },

    getSiblings(id: string) {
      return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/${id}/siblings`)
    }
  },

  // 其他实用方法
  checkNameAvailability(name: string, parentId?: string) {
    return request.get<{ available: boolean; suggestion?: string }>(`${ADMIN_CATEGORY_BASE}/check-name`, {
      params: { name, parentId }
    })
  },

  checkPathAvailability(path: string) {
    return request.get<{ available: boolean; suggestion?: string }>(`${ADMIN_CATEGORY_BASE}/check-path`, { params: { path } })
  },

  getRootCategories(params?: {
    includeArticleCount?: boolean
    sortBy?: string
    sortOrder?: 'asc' | 'desc'
  }) {
    return request.get<Category[]>(`${ADMIN_CATEGORY_BASE}/root`, { params })
  },

  getCategoryDepth(id: string) {
    return request.get<{ depth: number }>(`${ADMIN_CATEGORY_BASE}/${id}/depth`)
  },

  rebuildTree() {
    return request.post<{
      success: boolean
      message: string
      affectedCount: number
    }>(`${ADMIN_CATEGORY_BASE}/rebuild-tree`)
  }
}

// 公共API（不需要认证）
export const publicCategoryApi = {
  getCategories() {
    return request.get<Category[]>(CATEGORY_BASE)
  },

  getCategory(id: string) {
    return request.get<Category>(`${CATEGORY_BASE}/${id}`)
  },

  getHotCategories(limit: number = 10) {
    return request.get<Category[]>(`${CATEGORY_BASE}/hot`, { params: { limit } })
  },

  getCategoryTree() {
    return request.get<CategoryTreeNode[]>(`${CATEGORY_BASE}/tree`)
  },

  getChildren(parentId: string) {
    return request.get<Category[]>(`${CATEGORY_BASE}/children/${parentId}`)
  },

  getCategoriesWithCount() {
    return request.get<Category[]>(`${CATEGORY_BASE}/with-count`)
  },

  // 搜索不使用缓存，因为结果可能经常变化
  search(keyword: string) {
    return request.get<Category[]>(`${CATEGORY_BASE}/search`, { params: { keyword } })
  }
}

export default categoryApi

