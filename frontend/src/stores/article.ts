import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { articleApi } from '@/api'
import type { Article, ArticleCreateData, ArticleUpdateData, ArticleListParams } from '@/types/article'
import type { DashboardResponse } from '@/types/dashboard'

// 文章状态枚举
export enum ArticleStatus {
  DRAFT = 'draft',
  PUBLISHED = 'published',
  ARCHIVED = 'archived'
}

// 文章可见性枚举
export enum ArticleVisibility {
  PUBLIC = 'public',
  PRIVATE = 'private',
  PASSWORD = 'password'
}

export const useArticleStore = defineStore('article', () => {
  // 状态
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const isLoading = ref(false)
  const createLoading = ref(false)
  const updateLoading = ref(false)
  const deleteLoading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const searchKeyword = ref('')
  const selectedCategory = ref<string | null>(null)
  const selectedStatus = ref<ArticleStatus | null>(null)
  const selectedVisibility = ref<ArticleVisibility | null>(null)
  
  // 缓存状态
  const featuredArticlesCache = ref<Article[]>([])
  const latestArticlesCache = ref<Article[]>([])
  const statsCache = ref<DashboardResponse | null>(null)
  const cacheTimestamp = ref<number>(0)
  const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

  // 计算属性
  const hasArticles = computed(() => articles.value.length > 0)
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
  const hasNextPage = computed(() => currentPage.value < totalPages.value)
  const hasPrevPage = computed(() => currentPage.value > 1)
  
  const publishedArticles = computed(() => 
    articles.value.filter(article => article.status === ArticleStatus.PUBLISHED)
  )
  
  const draftArticles = computed(() => 
    articles.value.filter(article => article.status === ArticleStatus.DRAFT)
  )
  
  const archivedArticles = computed(() => 
    articles.value.filter(article => article.status === ArticleStatus.ARCHIVED)
  )

  // 获取文章列表
  const fetchArticles = async (params?: Partial<ArticleListParams>) => {
    try {
      isLoading.value = true
      
      const queryParams: ArticleListParams = {
        page: currentPage.value,
        limit: pageSize.value,
        keyword: searchKeyword.value || undefined,
        category: selectedCategory.value || undefined,
        status: selectedStatus.value || undefined,
        visibility: selectedVisibility.value || undefined,
        ...params
      }
      
      const response = await articleApi.getArticles(queryParams)
      articles.value = response.items || response.list || []
      total.value = response.total || 0
      
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章列表失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 获取单篇文章
  const fetchArticle = async (id: string) => {
    try {
      isLoading.value = true
      
      const response = await articleApi.getArticle(id)
      currentArticle.value = response
      
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章详情失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 创建文章
  const createArticle = async (data: ArticleCreateData) => {
    try {
      createLoading.value = true
      
      const response = await articleApi.createArticle(data)
      
      // 如果是发布状态，添加到列表开头
      if (data.status === ArticleStatus.PUBLISHED) {
        articles.value.unshift(response)
        total.value += 1
      }
      
      // API调用已经处理成功提示
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '创建文章失败')
      throw error
    } finally {
      createLoading.value = false
    }
  }

  // 更新文章
  const updateArticle = async (id: string, data: ArticleUpdateData) => {
    try {
      updateLoading.value = true
      
      const response = await articleApi.updateArticle(id, data)
      
      // 更新列表中的文章
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value[index] = response
      }
      
      // 更新当前文章
      if (currentArticle.value?.id === id) {
        currentArticle.value = response
      }
      
      // API调用已经处理成功提示
      return response
    } catch (error: any) {
      ElMessage.error(error.message || '更新文章失败')
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  // 删除文章
  const deleteArticle = async (id: string) => {
    try {
      deleteLoading.value = true
      
      await articleApi.deleteArticle(id)
      
      // 从列表中移除
      const index = articles.value.findIndex(article => article.id === id)
      if (index !== -1) {
        articles.value.splice(index, 1)
        total.value -= 1
      }
      
      // 清除当前文章
      if (currentArticle.value?.id === id) {
        currentArticle.value = null
      }
      
      // API调用已经处理成功提示
    } catch (error: any) {
      ElMessage.error(error.message || '删除文章失败')
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 发布文章
  const publishArticle = async (id: string) => {
    return updateArticle(id, { status: ArticleStatus.PUBLISHED })
  }

  // 归档文章
  const archiveArticle = async (id: string) => {
    return updateArticle(id, { status: ArticleStatus.ARCHIVED })
  }

  // 设置文章为草稿
  const draftArticle = async (id: string) => {
    return updateArticle(id, { status: ArticleStatus.DRAFT })
  }

  // 获取特色文章（包含accessLevel=0和1的文章）
  const getFeaturedArticles = async (limit: number = 3) => {
    // 检查缓存是否有效
    const now = Date.now()
    if (featuredArticlesCache.value.length > 0 && 
        cacheTimestamp.value > 0 && 
        (now - cacheTimestamp.value) < CACHE_DURATION) {
      return featuredArticlesCache.value.slice(0, limit)
    }
    
    try {
      // 首先尝试获取所有特色文章（包括会员文章）
      const response = await articleApi.getArticles({
        page: 1,
        limit: limit * 2, // 获取更多数据以确保有足够的文章
        featured: true,
        status: ArticleStatus.PUBLISHED
      })
      
      const articles = response.items || response.list || []
      
      // 如果获取的文章数量不足，可能是因为用户权限限制
      // 这种情况下，后端只返回了accessLevel=0的文章
      // 我们保持现有逻辑，但确保前端能正确处理accessLevel字段
      
      // 更新缓存
      featuredArticlesCache.value = articles
      cacheTimestamp.value = now
      
      return articles.slice(0, limit)
    } catch (error: any) {
      console.error('获取特色文章失败:', error)
      return featuredArticlesCache.value.slice(0, limit) // 返回缓存数据
    }
  }

  // 获取最新文章（包含accessLevel=0和1的文章）
  const getLatestArticles = async (limit: number = 6) => {
    // 检查缓存是否有效
    const now = Date.now()
    if (latestArticlesCache.value.length > 0 && 
        cacheTimestamp.value > 0 && 
        (now - cacheTimestamp.value) < CACHE_DURATION) {
      return latestArticlesCache.value.slice(0, limit)
    }
    
    try {
      // 首先尝试获取所有最新文章（包括会员文章）
      const response = await articleApi.getArticles({
        page: 1,
        limit: limit * 2, // 获取更多数据以确保有足够的文章
        status: ArticleStatus.PUBLISHED,
        sortBy: 'publishedAt',
        sortOrder: 'desc'
      })
      
      const articles = response.items || response.list || []
      
      // 如果获取的文章数量不足，可能是因为用户权限限制
      // 这种情况下，后端只返回了accessLevel=0的文章
      // 我们保持现有逻辑，但确保前端能正确处理accessLevel字段
      
      // 更新缓存
      latestArticlesCache.value = articles
      if (cacheTimestamp.value === 0) {
        cacheTimestamp.value = now
      }
      
      return articles.slice(0, limit)
    } catch (error: any) {
      console.error('获取最新文章失败:', error)
      return latestArticlesCache.value.slice(0, limit) // 返回缓存数据
    }
  }

  // 获取热门文章
  const getHotArticles = async (limit: number = 10) => {
    try {
      const response = await articleApi.getArticles({
        page: 1,
        limit,
        status: ArticleStatus.PUBLISHED,
        sortBy: 'viewCount',
        sortOrder: 'desc'
      })
      return response.items || response.list || []
    } catch (error: any) {
      console.error('获取热门文章失败:', error)
      return []
    }
  }

  const createEmptyDashboard = (): DashboardResponse => ({
    range: { preset: '30d', startDate: '', endDate: '' },
    overview: {
      totalArticles: 0,
      publishedArticles: 0,
      draftArticles: 0,
      privateArticles: 0,
      reviewPendingArticles: 0,
      reviewRejectedArticles: 0,
      totalViews: 0,
      totalLikes: 0,
      totalCollects: 0,
      totalComments: 0,
      totalShares: 0,
      weeklyNewArticles: 0,
      todayNewArticles: 0,
      viewGrowthRate: 0,
      engagementGrowthRate: 0,
      lastUpdated: ''
    },
    trend: {
      points: [],
      totalViews: 0,
      averageViewsPerArticle: 0,
      wowChange: 0
    },
    funnel: {
      stages: []
    },
    segments: {
      categories: [],
      tags: [],
      accessLevels: []
    },
    leaderboards: {
      topViews: [],
      topEngagement: [],
      latestPublished: []
    },
    moderation: {
      pendingReviews: 0,
      rejectedReviews: 0,
      oldestDraftDays: 0,
      records: []
    },
    insights: []
  })

  // 获取统计数据
  const getStats = async () => {
    // 检查缓存是否有效
    const now = Date.now()
    if (statsCache.value && 
        cacheTimestamp.value > 0 && 
        (now - cacheTimestamp.value) < CACHE_DURATION) {
      return statsCache.value
    }
    
    try {
      const response = await articleApi.getStats()
      statsCache.value = response
      cacheTimestamp.value = now
      return response
    } catch (error: any) {
      console.error('获取统计数据失败:', error)
      if (!statsCache.value) {
        statsCache.value = createEmptyDashboard()
      }
      return statsCache.value
    }
  }

  // 批量操作
  const batchUpdateStatus = async (ids: string[], status: ArticleStatus) => {
    try {
      updateLoading.value = true
      
      // 由于API中没有批量更新状态的方法，我们逐个更新
      const promises = ids.map(id => {
        if (status === ArticleStatus.PUBLISHED) {
          return articleApi.publishArticle(id)
        } else if (status === ArticleStatus.ARCHIVED) {
          return articleApi.archiveArticle(id)
        } else {
          // 对于草稿状态，使用updateArticle方法
          return articleApi.updateArticle(id, { status })
        }
      })
      
      await Promise.all(promises)
      
      // 更新本地状态
      articles.value.forEach(article => {
        if (ids.includes(article.id)) {
          article.status = status
        }
      })
      
      ElMessage.success(`批量${status === ArticleStatus.PUBLISHED ? '发布' : status === ArticleStatus.ARCHIVED ? '归档' : '设为草稿'}成功`)
    } catch (error: any) {
      ElMessage.error(error.message || '批量操作失败')
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  const batchDelete = async (ids: string[]) => {
    try {
      deleteLoading.value = true
      
      await articleApi.batchDeleteArticles(ids)
      
      // 从列表中移除
      articles.value = articles.value.filter(article => !ids.includes(article.id))
      total.value -= ids.length
      
      // API调用已经处理成功提示
    } catch (error: any) {
      ElMessage.error(error.message || '批量删除失败')
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 搜索和筛选
  const setSearchKeyword = (keyword: string) => {
    searchKeyword.value = keyword
    currentPage.value = 1
  }

  const setCategory = (category: string | null) => {
    selectedCategory.value = category
    currentPage.value = 1
  }

  const setStatus = (status: ArticleStatus | null) => {
    selectedStatus.value = status
    currentPage.value = 1
  }

  const setVisibility = (visibility: ArticleVisibility | null) => {
    selectedVisibility.value = visibility
    currentPage.value = 1
  }

  // 分页
  const setPage = (page: number) => {
    currentPage.value = page
  }

  const setPageSize = (size: number) => {
    pageSize.value = size
    currentPage.value = 1
  }

  const nextPage = () => {
    if (hasNextPage.value) {
      currentPage.value += 1
    }
  }

  const prevPage = () => {
    if (hasPrevPage.value) {
      currentPage.value -= 1
    }
  }

  // 重置筛选条件
  const resetFilters = () => {
    searchKeyword.value = ''
    selectedCategory.value = null
    selectedStatus.value = null
    selectedVisibility.value = null
    currentPage.value = 1
  }

  // 清除当前文章
  const clearCurrentArticle = () => {
    currentArticle.value = null
  }

  // 清除所有数据
  const clearAll = () => {
    articles.value = []
    currentArticle.value = null
    total.value = 0
    currentPage.value = 1
    resetFilters()
  }

  return {
    // 状态
    articles,
    currentArticle,
    isLoading,
    createLoading,
    updateLoading,
    deleteLoading,
    total,
    currentPage,
    pageSize,
    searchKeyword,
    selectedCategory,
    selectedStatus,
    selectedVisibility,
    
    // 计算属性
    hasArticles,
    totalPages,
    hasNextPage,
    hasPrevPage,
    publishedArticles,
    draftArticles,
    archivedArticles,
    
    // 方法
    fetchArticles,
    fetchArticle,
    createArticle,
    updateArticle,
    deleteArticle,
    publishArticle,
    archiveArticle,
    draftArticle,
    batchUpdateStatus,
    batchDelete,
    getFeaturedArticles,
    getLatestArticles,
    getHotArticles,
    getStats,
    setSearchKeyword,
    setCategory,
    setStatus,
    setVisibility,
    setPage,
    setPageSize,
    nextPage,
    prevPage,
    resetFilters,
    clearCurrentArticle,
    clearAll
  }
}, {
  persist: {
    key: 'simple-share-article',
    storage: localStorage,
    paths: ['searchKeyword', 'selectedCategory', 'selectedStatus', 'selectedVisibility', 'pageSize']
  }
})
