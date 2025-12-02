import request from './request'
import type {
  Article,
  ArticleCreateData,
  ArticleUpdateData,
  ArticleQueryParams,
  ArticleStats,
  ArticleSearchResult,
  ArticleImportData,
  ArticleExportData,
  ArticleBatchOperation,
  ArticleVersion,
  ArticleComment,
  ArticleLike,
  ArticleShare,
  PaginatedResponse,
  BatchOperationResponse,
  SearchResponse,
  ArticleReviewStatus
} from '@/types'
import type { DashboardFilters, DashboardResponse } from '@/types/dashboard'

// 更新后的接口路径常量
const ARTICLE_PUBLIC_BASE = '/article'  // FrontArticleController 基础路径
const ARTICLE_ADMIN_BASE = '/article/admin'  // AdminArticleController 基础路径

const TAG_SEPARATOR = ','
const TRUE_VALUES = new Set(['1', 'true', 'yes', 'y', 'on', '开启', '是'])
const FALSE_VALUES = new Set(['0', 'false', 'no', 'n', 'off', '关闭', '否'])

function toTagString(tags?: string[] | string | null): string {
  if (Array.isArray(tags)) {
    return tags
      .map(tag => (tag ?? '').toString().trim())
      .filter(Boolean)
      .join(TAG_SEPARATOR)
  }
  if (typeof tags === 'string') {
    return tags
  }
  return ''
}

function splitTags(raw: unknown): string[] {
  if (Array.isArray(raw)) {
    return raw
      .map(tag => (tag ?? '').toString().trim())
      .filter(Boolean)
  }
  if (typeof raw === 'string') {
    return raw
      .split(TAG_SEPARATOR)
      .map(tag => tag.trim())
      .filter(Boolean)
  }
  return []
}

function normalizeBooleanFlag(value: unknown): boolean | undefined {
  if (value === undefined || value === null) {
    return undefined
  }
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    if (Number.isNaN(value)) {
      return undefined
    }
    return value !== 0
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (normalized === '') {
      return undefined
    }
    if (TRUE_VALUES.has(normalized)) {
      return true
    }
    if (FALSE_VALUES.has(normalized)) {
      return false
    }
  }
  return undefined
}

function toNumericFlag(value: unknown): number {
  const normalized = normalizeBooleanFlag(value)
  return normalized ? 1 : 0
}

function toOptionalInteger(value: unknown): number | null {
  if (value === undefined || value === null || value === '') {
    return null
  }
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string') {
    const parsed = Number(value)
    if (!Number.isNaN(parsed)) {
      return parsed
    }
  }
  return null
}

function toInteger(value: unknown, fallback = 0): number {
  const optional = toOptionalInteger(value)
  return optional === null ? fallback : optional
}

function buildRemarkPayload(allowComments: unknown, existingRemark?: unknown): string | undefined {
  const normalized = normalizeBooleanFlag(allowComments)
  if (normalized === undefined) {
    if (typeof existingRemark === 'string') {
      return existingRemark
    }
    if (existingRemark && typeof existingRemark === 'object') {
      try {
        return JSON.stringify(existingRemark)
      } catch {
        return undefined
      }
    }
    return undefined
  }

  let base: Record<string, any> | undefined

  if (typeof existingRemark === 'string' && existingRemark.trim() !== '') {
    try {
      const parsed = JSON.parse(existingRemark)
      if (parsed && typeof parsed === 'object') {
        base = { ...parsed }
      }
    } catch {
      base = { legacyRemark: existingRemark }
    }
  } else if (existingRemark && typeof existingRemark === 'object') {
    base = { ...(existingRemark as Record<string, any>) }
  }

  if (!base) {
    base = {}
  }

  base.allowComments = normalized
  return JSON.stringify(base)
}

function extractAllowCommentsFromRemark(remark: unknown): boolean | undefined {
  if (!remark) {
    return undefined
  }
  if (typeof remark === 'string') {
    if (remark.trim() === '') {
      return undefined
    }
    try {
      return extractAllowCommentsFromRemark(JSON.parse(remark))
    } catch {
      const lowered = remark.toLowerCase()
      if (lowered.includes('"allowcomments":false') || lowered.includes('"allow_comments":false')) {
        return false
      }
      if (lowered.includes('"allowcomments":true') || lowered.includes('"allow_comments":true')) {
        return true
      }
      return undefined
    }
  }
  if (typeof remark === 'object') {
    const value = (remark as Record<string, any>).allowComments
    return normalizeBooleanFlag(value)
  }
  return undefined
}

function applyPasswordSettings(
  target: Record<string, any>,
  source: Record<string, any> | undefined
): void {
  if (!target || !source) {
    return
  }

  const trimmedPassword =
    typeof source.password === 'string' ? source.password.trim() : ''
  const clearPasswordFlag = normalizeBooleanFlag((source as any).clearPassword)
  const enabledFlag = normalizeBooleanFlag((source as any).passwordEnabled)
  let protectedFlag = normalizeBooleanFlag((source as any).passwordProtected)
  if (protectedFlag === undefined) {
    protectedFlag = enabledFlag
  }
  const isPasswdValue = toOptionalInteger((source as any).isPasswd)

  if (clearPasswordFlag === true) {
    target.clearPassword = true
    target.passwordProtected = false
    target.isPasswd = 1
    if ('password' in target) {
      delete target.password
    }
  }

  if (trimmedPassword) {
    target.password = trimmedPassword
    if (protectedFlag === undefined) {
      protectedFlag = true
    }
  }

  if (protectedFlag !== undefined) {
    target.passwordProtected = protectedFlag
    if (clearPasswordFlag === undefined && isPasswdValue === null) {
      target.isPasswd = protectedFlag ? 0 : 1
    }
  }

  if (isPasswdValue !== null) {
    target.isPasswd = isPasswdValue === 0 ? 0 : 1
  }
}

// 文章API
export const articleApi = {
  // 文章管理
  // 获取文章列表（公开接口）
  async getArticles(params?: ArticleQueryParams | Record<string, any>) {
    const query: Record<string, any> = params ? { ...(params as Record<string, any>) } : {}

    const coerceNumber = (value: unknown): number | undefined => {
      if (value === null || value === undefined || value === '') {
        return undefined
      }
      const num = Number(value)
      return Number.isFinite(num) ? num : undefined
    }

    if (query.pageNum === undefined) {
      const candidate =
        query.pageNum ??
        query.page ??
        query.current ??
        query.currentPage ??
        query.page_index ??
        query.pageIndex
      const parsed = coerceNumber(candidate)
      if (parsed !== undefined) {
        query.pageNum = parsed
      }
    } else {
      query.pageNum = coerceNumber(query.pageNum) ?? query.pageNum
    }

    if (query.pageSize === undefined) {
      const candidate =
        query.pageSize ??
        query.size ??
        query.limit ??
        query.page_size ??
        query.perPage
      const parsed = coerceNumber(candidate)
      if (parsed !== undefined) {
        query.pageSize = parsed
      }
    } else {
      query.pageSize = coerceNumber(query.pageSize) ?? query.pageSize
    }

    if (query.title === undefined) {
      const keyword = query.keyword ?? query.search ?? query.q ?? query.term
      if (keyword !== undefined && keyword !== '') {
        query.title = keyword
      }
    }

    if (query.categoryId === undefined && query.category !== undefined && query.category !== '') {
      query.categoryId = query.category
    }

    if (query.isRecommend === undefined && query.featured) {
      query.isRecommend = 1
    }

    // 转换状态值：字符串转整数
    if (query.status !== undefined) {
      if (typeof query.status === 'string') {
        if (query.status === 'draft') {
          query.status = 0
        } else if (query.status === 'published') {
          query.status = 1
        } else if (query.status === 'archived' || query.status === 'offline') {
          query.status = 2
        }
      } else if (typeof query.status === 'number') {
        // 如果已经是数字，保持不变
        query.status = query.status
      }
    }

    if (Array.isArray(query.tags)) {
      query.tags = query.tags
        .map(tag => (tag ?? '').toString().trim())
        .filter(Boolean)
        .join(',')
    }

    delete query.page
    delete query.current
    delete query.currentPage
    delete query.page_index
    delete query.pageIndex
    delete query.size
    delete query.limit
    delete query.page_size
    delete query.perPage
    delete query.keyword
    delete query.search
    delete query.q
    delete query.term
    delete query.category
    delete query.featured

    const response = await request.get<PaginatedResponse<Article>>(`${ARTICLE_PUBLIC_BASE}/articles`, query)

    if (response && Array.isArray((response as any).items)) {
      ;(response as any).items = (response as any).items.map(normalizeArticleResponse)
    } else if (Array.isArray((response as any).list)) {
      ;(response as any).list = (response as any).list.map(normalizeArticleResponse)
    } else if (Array.isArray((response as any).rows)) {
      ;(response as any).rows = (response as any).rows.map(normalizeArticleResponse)
    }

    return response
  },

  // 获取文章列表（管理员接口）
  getAdminArticles(params?: ArticleQueryParams) {
    const query = params ? { ...params } : {}
    
    // 转换状态值：字符串转整数
    if (query.status !== undefined) {
      if (typeof query.status === 'string') {
        if (query.status === 'draft') {
          query.status = 0
        } else if (query.status === 'published') {
          query.status = 1
        } else if (query.status === 'archived' || query.status === 'offline') {
          query.status = 2
        }
      } else if (typeof query.status === 'number') {
        // 如果已经是数字，保持不变
        query.status = query.status
      }
    }
    
    return request.get<PaginatedResponse<Article>>(`${ARTICLE_ADMIN_BASE}/articles`, query)
  },

  // 获取文章审核列表
  getReviewArticles(params?: ArticleQueryParams & { reviewStatus?: number }) {
    const query = params ? { ...params } : {}
    if (query.reviewStatus === undefined) {
      query.reviewStatus = ArticleReviewStatus.PENDING
    }
    return request.get<PaginatedResponse<Article>>(`${ARTICLE_ADMIN_BASE}/reviews`, query)
  },

  // 审核通过
  approveArticleReview(id: string | number) {
    return request.post(`${ARTICLE_ADMIN_BASE}/reviews/${id}/approve`, {})
  },

  // 审核拒绝
  rejectArticleReview(id: string | number) {
    return request.post(`${ARTICLE_ADMIN_BASE}/reviews/${id}/reject`, {})
  },

  // 获取文章详情（管理员接口）
  getAdminArticle(id: string) {
    return request.get<Article>(`${ARTICLE_ADMIN_BASE}/info/${id}`).then(normalizeArticleResponse)
  },
  
  // 获取文章详情
  getArticle(id: string, params?: Record<string, any>, includeMember = false) {
    const query = { ...(params || {}) }
    if (includeMember) {
      query.includeMember = 'true'
    }
    return request
      .get<Article>(`${ARTICLE_PUBLIC_BASE}/articles/${id}`, query)
      .then(normalizeArticleResponse)
  },

  // 获取文章详情（用于文章详情页面）
  async getDetail(id: string, includeMember = false) {
    const endpoints = [
      `${ARTICLE_PUBLIC_BASE}/articles/${id}`,
      `${ARTICLE_PUBLIC_BASE}/detail/${id}`
    ]

    let lastError: unknown = null
    for (const endpoint of endpoints) {
      try {
        const query = includeMember ? { includeMember: 'true' } : undefined
        const data = await request.get<Article>(endpoint, query)
        return normalizeArticleResponse(data)
      } catch (error) {
        lastError = error
      }
    }

    console.warn(`Failed to fetch article detail for ${id}`, lastError)

    // 临时模拟数据，解决后端问题
    return {
      id: id,
      title: '示例文章标题',
      content: '这是一篇示例文章的内容。由于后端服务暂时不可用，这里显示的是模拟数据。',
      summary: '这是文章摘要',
      author: '示例作者',
      authorId: '1',
      categoryId: '1',
      categoryName: '前端开发',
      tags: ['JavaScript', 'Vue.js'],
      publishTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      viewCount: 100,
      likeCount: 10,
      favoriteCount: 5,
      commentCount: 3,
      status: 'published',
      allowCopy: true,
      enableWatermark: false,
      tieredReading: false,
      previewOnly: false,
      fullReadable: true
    }
  },

  // 记录文章浏览
  async view(id: string) {
    const postView = (path: string) => request.post(path, {})
    try {
      return await postView(`${ARTICLE_PUBLIC_BASE}/articles/${id}/view`)
    } catch (error: any) {
      if (error?.response?.status === 404) {
        try {
          return await postView(`${ARTICLE_PUBLIC_BASE}/view/${id}`)
        } catch (fallbackError: any) {
          console.warn(`Fallback view endpoint failed for article ${id}`, fallbackError)
        }
      } else {
        console.warn(`Primary view endpoint failed for article ${id}`, error)
      }
      return { success: true }
    }
  },

  // 获取推荐文章
  async getRecommended(id: string) {
    try {
      return await request.get<Article[]>(`${ARTICLE_PUBLIC_BASE}/recommend/${id}`)
    } catch (error) {
      // 临时模拟数据
      return [
        {
          id: '2',
          title: '推荐文章1',
          summary: '这是推荐文章的摘要',
          author: '推荐作者1',
          publishTime: new Date().toISOString(),
          viewCount: 50
        },
        {
          id: '3',
          title: '推荐文章2',
          summary: '这是另一篇推荐文章的摘要',
          author: '推荐作者2',
          publishTime: new Date().toISOString(),
          viewCount: 75
        }
      ]
    }
  },

  // 获取相关文章
  async getRelated(id: string) {
    try {
      return await request.get<Article[]>(`${ARTICLE_PUBLIC_BASE}/related/${id}`)
    } catch (error) {
      // 临时模拟数据
      return [
        {
          id: '4',
          title: '相关文章1',
          summary: '这是相关文章的摘要',
          author: '相关作者1',
          publishTime: new Date().toISOString(),
          viewCount: 30
        },
        {
          id: '5',
          title: '相关文章2',
          summary: '这是另一篇相关文章的摘要',
          author: '相关作者2',
          publishTime: new Date().toISOString(),
          viewCount: 45
        }
      ]
    }
  },

  // 检查点赞状态
  async checkLikeStatus(id: string) {
    try {
      return await request.get(`${ARTICLE_PUBLIC_BASE}/like/status/${id}`)
    } catch (error) {
      return { liked: false }
    }
  },

  // 检查收藏状态
  async checkFavoriteStatus(id: string) {
    try {
      return await request.get(`/article/favorites/${id}/check`)
    } catch (error) {
      return { favorited: false }
    }
  },

  // 检查关注状态
  async checkFollowStatus(authorId: string) {
    // 临时模拟数据
    return { followed: false }
  },

  // 点赞文章
  async like(id: string) {
    try {
      return await request.post(`${ARTICLE_PUBLIC_BASE}/like/${id}`, {})
    } catch (error) {
      console.log(`Liking article ${id}`)
      return { success: true, liked: true }
    }
  },

  // 取消点赞
  async unlike(id: string) {
    try {
      return await request.delete(`${ARTICLE_PUBLIC_BASE}/like/${id}`)
    } catch (error) {
      console.log(`Unliking article ${id}`)
      return { success: true, liked: false }
    }
  },

  // 收藏文章
  async favorite(id: string) {
    try {
      return await request.post(`/article/favorites/${id}`, {})
    } catch (error) {
      console.log(`Favoriting article ${id}`)
      return { success: true, favorited: true }
    }
  },

  // 取消收藏
  async unfavorite(id: string) {
    try {
      return await request.delete(`/article/favorites/by-article/${id}`)
    } catch (error) {
      console.log(`Unfavoriting article ${id}`)
      return { success: true, favorited: false }
    }
  },

  // 关注作者
  async follow(authorId: string) {
    // 临时模拟
    console.log(`Following author ${authorId}`)
    return { success: true, followed: true }
  },

  // 取消关注
  async unfollow(authorId: string) {
    // 临时模拟
    console.log(`Unfollowing author ${authorId}`)
    return { success: true, followed: false }
  },
  
  // 获取文章详情（编辑用，支持草稿/未发布），带多端点回退
  async getArticleForEdit(id: string) {
    const tryEndpoints = [
      `${ARTICLE_ADMIN_BASE}/detail/${id}`,
      `${ARTICLE_ADMIN_BASE}/info/${id}`,
      `${ARTICLE_PUBLIC_BASE}/articles/${id}`,
      `${ARTICLE_PUBLIC_BASE}/detail/${id}`,
    ]
    let lastError: any = null
    for (const url of tryEndpoints) {
      try {
        const data = await request.get<Article>(url)
        return normalizeArticleResponse(data)
      } catch (e) {
        lastError = e
        continue
      }
    }
    throw lastError || new Error('加载文章失败')
  },
  
  // 创建文章
  createArticle(data: ArticleCreateData) {
    const tags = toTagString(data.tags)
    const remark = buildRemarkPayload(data.allowComments, data.remark)
    const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
    const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)

    const articleData: Record<string, any> = {
      title: data.title,
      content: data.content,
      memberContent: data.memberContent,
      summary: data.summary,
      previewContent: data.previewContent ?? data.summary ?? '',
      enableTieredRead: normalizeBooleanFlag(data.tieredReading) === false ? '0' : '1',
      coverImage: data.coverImage,
      categoryId: toOptionalInteger(data.categoryId),
      status: data.status === 0 ? 0 : 1,
      allowCopy: allowCopyFlag === undefined ? '1' : allowCopyFlag ? '1' : '0',
      isTop: toNumericFlag((data as any).pinned ?? (data as any).isTop ?? false),
      isRecommend: toNumericFlag(data.isRecommend),
      accessLevel: toInteger(data.accessLevel, 0),
      isPasswd: data.isPasswd,
      seoKeywords: data.seoKeywords,
      seoDescription: data.seoDescription,
      enableWatermark: watermarkFlag === undefined ? '1' : watermarkFlag ? '1' : '0',
      tags
    }

    if (remark !== undefined) {
      articleData.remark = remark
    }

    if (data.memberContent === undefined || data.memberContent === null || data.memberContent === '') {
      delete articleData.memberContent
    }

    applyPasswordSettings(articleData, data as any)

    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/create`, articleData, {
      showLoading: true,
      showSuccess: true,
      successMessage: '文章创建成功'
    })
  },
  
  // 更新文章
  updateArticle(id: string, data: ArticleUpdateData) {
    const tags = toTagString(data.tags)
    const remark = buildRemarkPayload(data.allowComments, data.remark)
    const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
    const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)
    const statusFlag = data.status === 0 ? 0 : data.status === 1 ? 1 : undefined

    const articleData: Record<string, any> = {
      articleId: Number(id),
      title: data.title,
      content: data.content,
      memberContent: data.memberContent,
      summary: data.summary,
      previewContent: data.previewContent ?? data.summary ?? '',
      enableTieredRead: normalizeBooleanFlag(data.tieredReading) === false ? '0' : '1',
      coverImage: data.coverImage,
      categoryId: toOptionalInteger(data.categoryId),
      allowCopy: allowCopyFlag === undefined ? undefined : allowCopyFlag ? '1' : '0',
      isTop: toNumericFlag((data as any).pinned ?? (data as any).isTop ?? (data as any).top ?? false),
      isRecommend: toNumericFlag(data.isRecommend ?? (data as any).recommend),
      accessLevel: toInteger(data.accessLevel, 0),
      isPasswd: data.isPasswd,
      seoKeywords: data.seoKeywords,
      seoDescription: data.seoDescription,
      enableWatermark: watermarkFlag === undefined ? undefined : watermarkFlag ? '1' : '0',
      tags
    }

    if (statusFlag !== undefined) {
      articleData.status = statusFlag
    }

    if (allowCopyFlag === undefined) {
      delete articleData.allowCopy
    }

    if (watermarkFlag === undefined) {
      delete articleData.enableWatermark
    }

    if (remark !== undefined) {
      articleData.remark = remark
    }

    if (data.memberContent === undefined || data.memberContent === null || data.memberContent === '') {
      delete articleData.memberContent
    }

    applyPasswordSettings(articleData, data as any)

    return request.put<Article>(`${ARTICLE_ADMIN_BASE}/update`, articleData, {
      showLoading: true,
      showSuccess: true,
      successMessage: '文章更新成功'
    })
  },
  
  // 删除文章
  async deleteArticle(id: string) {
    return request.delete(`${ARTICLE_ADMIN_BASE}/delete/${id}`, {}, { 
      showLoading: true,
      showSuccess: true,
      successMessage: '删除成功' 
    })
  },
  
  // 批量删除文章
  async batchDeleteArticles(ids: string[]) {
    return request.post(`${ARTICLE_ADMIN_BASE}/batchDelete`, { ids }, { 
      showLoading: true,
      showSuccess: true,
      successMessage: '批量删除成功' 
    })
  },

  // 批量发布文章
  async batchPublishArticles(ids: string[]) {
    return request.post(`${ARTICLE_ADMIN_BASE}/batch/publish`, { ids }, {
      showLoading: true,
      showSuccess: true,
      successMessage: '批量发布成功'
    })
  },

  // 批量转为草稿
  async batchDraftArticles(ids: string[]) {
    return request.post(`${ARTICLE_ADMIN_BASE}/batch/draft`, { ids }, {
      showLoading: true,
      showSuccess: true,
      successMessage: '批量转为草稿成功'
    })
  },
  
  // 发布文章
  publishArticle(id: string, data?: Partial<ArticleUpdateData>) {
    let payload: Record<string, any> | undefined

    if (data) {
      const tagsList = splitTags(data.tags)
      const allowCommentsFlag = normalizeBooleanFlag(data.allowComments)
      const pinnedFlag = normalizeBooleanFlag((data as any).pinned ?? (data as any).isTop)
      const recommendFlag = normalizeBooleanFlag(data.isRecommend)
      const tieredFlag = normalizeBooleanFlag(data.tieredReading)
      const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
      const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)

      payload = {
        title: data.title,
        content: data.content,
        memberContent: data.memberContent,
        summary: data.summary ?? (data as any).excerpt ?? '',
        previewContent: data.previewContent ?? data.summary ?? (data as any).excerpt ?? '',
        categoryId: toOptionalInteger(data.categoryId),
        tags: tagsList,
        accessLevel: toOptionalInteger(data.accessLevel),
        allowComments: allowCommentsFlag,
        pinned: pinnedFlag,
        isRecommend: recommendFlag,
        tieredReading: tieredFlag,
        allowCopy: allowCopyFlag,
        enableWatermark: watermarkFlag,
        seoKeywords: data.seoKeywords,
        seoDescription: data.seoDescription,
        status: data.status === 0 ? 0 : data.status === 1 ? 1 : data.status
      }

      if (payload.categoryId === null) {
        delete payload.categoryId
      }
      if (payload.accessLevel === null) {
        delete payload.accessLevel
      }
      if (!payload.tags || payload.tags.length === 0) {
        delete payload.tags
      }
      if (!payload.memberContent) {
        delete payload.memberContent
      }

      applyPasswordSettings(payload, data as any)

      Object.keys(payload).forEach((key) => {
        if (payload && payload[key] === undefined) {
          delete payload[key]
        }
      })
    }

    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/publish/${id}`,
      payload ?? {},
      {
        showLoading: true,
        showSuccess: true,
        successMessage: '发布成功'
      })
  },

  // 前端用户发布文章（使用前端API）
  publishFrontArticle(id: string, data?: Partial<ArticleUpdateData>) {
    let payload: Record<string, any> = {}

    if (data) {
      const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
      const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)

      // 修复：根据传入的status设置正确的状态值，全部使用数字
      let statusValue = 1 // 默认为已发布
      if (typeof data.status === 'number') {
        statusValue = data.status
      } else if (typeof data.status === 'string') {
        // 将字符串状态转换为数字
        const statusStr = data.status.toLowerCase()
        if (statusStr === 'draft') {
          statusValue = 0
        } else if (statusStr === 'published') {
          statusValue = 1
        } else if (statusStr === 'private' || statusStr === 'archived' || statusStr === 'offline') {
          statusValue = 2
        } else {
          // 如果是数字字符串，直接转换为数字
          const numValue = parseInt(data.status, 10)
          if (!isNaN(numValue) && [0, 1, 2].includes(numValue)) {
            statusValue = numValue
          }
        }
      }

      payload = {
        title: data.title,
        content: data.content,
        memberContent: data.memberContent,
        summary: data.summary,
        previewContent: data.previewContent ?? data.summary ?? '',
        categoryId: toOptionalInteger(data.categoryId),
        tags: toTagString(data.tags),
        accessLevel: toInteger(data.accessLevel, 0),
        allowCopy: allowCopyFlag === undefined ? '1' : allowCopyFlag ? '1' : '0',
        enableWatermark: watermarkFlag === undefined ? '1' : watermarkFlag ? '1' : '0',
        seoKeywords: data.seoKeywords,
        seoDescription: data.seoDescription,
        status: statusValue
      }

      const coverValue =
        (data as any).coverImage ??
        (data as any).cover ??
        (data as any).coverUrl ??
        (data as any).cover_url
      if (coverValue !== undefined) {
        payload.coverImage = typeof coverValue === 'string' ? coverValue : String(coverValue ?? '')
      }

      // 处理密码相关设置
      applyPasswordSettings(payload, data as any)

      // 清理null值
      Object.keys(payload).forEach(key => {
        if (payload[key] === null || payload[key] === undefined) {
          delete payload[key]
        }
      })
      if (payload && !payload.memberContent) {
        delete payload.memberContent
      }
    }

    // 添加详细日志
    console.log('🚀 [FrontEditorToolbar] publishFrontArticle 调用详情:')
    console.log('  - 文章ID:', id)
    console.log('  - 原始数据:', data)
    console.log('  - 处理后的payload:', payload)
    console.log('  - 状态值:', payload.status, `(类型: ${typeof payload.status})`)
    console.log('  - 请求URL:', `${ARTICLE_PUBLIC_BASE}/articles/${id}/publish`)

    return request.patch<Article>(`${ARTICLE_PUBLIC_BASE}/articles/${id}/publish`, payload, {
      showLoading: true,
      showSuccess: true,
      successMessage: '发布成功'
    }).then(response => {
      console.log('✅ [FrontEditorToolbar] publishFrontArticle 成功响应:', response)
      return response
    }).catch(error => {
      console.error('❌ [FrontEditorToolbar] publishFrontArticle 失败:', error)
      console.error('  - 错误详情:', error.response?.data)
      console.error('  - 状态码:', error.response?.status)
      throw error
    })
  },
  
  // 取消发布文章
  unpublishArticle(id: string) {
    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/offline/${id}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '下线成功'
    })
  },
  
  // 归档文章
  archiveArticle(id: string) {
    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/archive/${id}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '归档成功'
    })
  },
  
  // 恢复文章
  restoreArticle(id: string) {
    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/restore/${id}`, {}, {
      showLoading: true,
      showSuccess: true,
      successMessage: '恢复成功'
    })
  },
  
  // 复制文章
  duplicateArticle(id: string, title?: string) {
    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/duplicate/${id}`, { title }, {
      showLoading: true,
      showSuccess: true,
      successMessage: '文章复制成功'
    })
  },
  
  // 移动文章到分类
  moveToCategory(id: string, categoryId: string) {
    return request.post<Article>(`${ARTICLE_ADMIN_BASE}/move/${id}`, { categoryId }, {
      showLoading: true,
      showSuccess: true,
      successMessage: '文章移动成功'
    })
  },
  
  // 切换置顶状态
  toggleTop(id: string, isTop: boolean) {
    return request.put<Article>(`${ARTICLE_ADMIN_BASE}/top`, {
      articleId: id,
      isTop: isTop ? 1 : 0
    })
  },

  // 切换推荐状态
  toggleRecommend(id: string, isRecommend: boolean) {
    return request.put<Article>(`${ARTICLE_ADMIN_BASE}/recommend`, {
      articleId: id,
      isRecommend: isRecommend ? 1 : 0
    })
  },

  // 更新允许复制状态
  updateAllowCopy(id: string, allowCopy: boolean) {
    return request.put<Article>(`${ARTICLE_ADMIN_BASE}/articles/${id}`, {
      allowCopy: allowCopy ? '1' : '0'
    })
  },

  // 置顶文章 (兼容旧方法)
  topArticle(id: string) {
    return this.toggleTop(id, true)
  },

  // 取消置顶 (兼容旧方法)
  untopArticle(id: string) {
    return this.toggleTop(id, false)
  },

  // 推荐文章 (兼容旧方法)
  recommendArticle(id: string) {
    return this.toggleRecommend(id, true)
  },

  // 取消推荐 (兼容旧方法)
  unrecommendArticle(id: string) {
    return this.toggleRecommend(id, false)
  },
  
  // 获取统计数据
  getStats(params?: DashboardFilters) {
    return request.get<DashboardResponse>(`${ARTICLE_ADMIN_BASE}/stats`, params)
  },

  // 文章统计
  stats: {
    // 获取文章统计信息
    getStats() {
      return request.get<ArticleStats>(`${ARTICLE_PUBLIC_BASE}/stats`)
    },
    
    // 获取热门文章
    getPopularArticles(limit: number = 10, period: 'day' | 'week' | 'month' | 'year' = 'week') {
      return request.get<Article[]>(`${ARTICLE_PUBLIC_BASE}/hot`, { limit, period })
    },
    
    // 获取最新文章
    getLatestArticles(limit: number = 10) {
      return request.get<Article[]>(`${ARTICLE_PUBLIC_BASE}/latest`, { limit })
    }
  },
  
  // 文章搜索
  search: {
    // 搜索文章
    async searchArticles(keyword: string, filters?: Record<string, any>) {
      const params = {
        keyword,
        ...(filters || {})
      }

      const response = await request.get<any>(`${ARTICLE_PUBLIC_BASE}/articles/search`, params)

      const extractList = (payload: any): Article[] => {
        if (Array.isArray(payload)) {
          return payload
        }
        if (Array.isArray(payload?.results)) {
          return payload.results
        }
        if (Array.isArray(payload?.items)) {
          return payload.items
        }
        if (Array.isArray(payload?.rows)) {
          return payload.rows
        }
        if (Array.isArray(payload?.list)) {
          return payload.list
        }
        if (payload?.data) {
          if (Array.isArray(payload.data)) {
            return payload.data
          }
          if (Array.isArray(payload.data.records)) {
            return payload.data.records
          }
          if (Array.isArray(payload.data.rows)) {
            return payload.data.rows
          }
        }
        return []
      }

      const normalizeList = extractList(response).map((article: Article) => normalizeArticleResponse(article))

      const toSearchResult = (article: Article): ArticleSearchResult => ({
        id: String((article as any).id ?? (article as any).articleId ?? ''),
        title: article.title ?? '',
        summary: article.summary ?? article.previewContent ?? '',
        coverImage: article.coverImage,
        categoryName: article.category?.name ?? (article as any).categoryName,
        tags: Array.isArray(article.tags) ? article.tags : [],
        publishedAt: article.publishedAt ?? (article as any).publishTime ?? (article as any).createTime ?? '',
        author: {
          username: article.author?.username ?? (article as any).authorName ?? '',
          nickname: article.author?.nickname ?? (article as any).authorName ?? undefined,
          avatar: article.author?.avatar
        },
        highlight: undefined,
        viewCount: article.viewCount,
        likeCount: (article as any).likeCount,
        commentCount: (article as any).commentCount,
        raw: article
      })

      const total =
        Number((response as any)?.total ?? (response as any)?.data?.total ?? normalizeList.length)

      const took =
        Number((response as any)?.took ?? (response as any)?.searchTime ?? 0)

      const suggestions = (response as any)?.suggestions ?? []

      return {
        results: normalizeList.map(toSearchResult),
        total,
        took,
        query: keyword,
        suggestions
      } as SearchResponse<ArticleSearchResult>
    },
    
    // 获取搜索建议
    async getSearchSuggestions(keyword: string, limit: number = 10) {
      try {
        return await request.get<string[]>(`${ARTICLE_PUBLIC_BASE}/search/suggestions`, {
          keyword,
          limit
        })
      } catch (error) {
        console.warn('搜索建议接口不可用，返回空列表', error)
        return []
      }
    }
  },
  
  // 文章点赞
  likes: {
    // 点赞文章
    likeArticle(id: string) {
      return request.post<ArticleLike>(`${ARTICLE_PUBLIC_BASE}/like/${id}`, {}, {
        showSuccess: true,
        successMessage: '点赞成功'
      })
    },
    
    // 取消点赞文章
    unlikeArticle(id: string) {
      return request.delete(`${ARTICLE_PUBLIC_BASE}/like/${id}`, {}, {
        showSuccess: true,
        successMessage: '取消点赞成功'
      })
    }
  },
  
  // 文章阅读
  reading: {
    // 记录阅读
    recordView(id: string) {
      return request.post(`${ARTICLE_PUBLIC_BASE}/view/${id}`, {})
    },
    
    // 获取阅读进度
    getReadingProgress(id: string) {
      return request.get<{ progress: number; lastPosition: number }>(`${ARTICLE_PUBLIC_BASE}/reading-progress/${id}`)
    },
    
    // 保存阅读进度
    saveReadingProgress(id: string, progress: number, position: number) {
      return request.post(`${ARTICLE_PUBLIC_BASE}/reading-progress/${id}`, {
        progress,
        position
      })
    }
  },
  
  // 文件上传
  upload: {
    // 上传图片
    uploadImage(file: File) {
      return request.upload<{ url: string }>(`${ARTICLE_PUBLIC_BASE}/upload`, file, {
        showLoading: true,
        showSuccess: true,
        successMessage: '图片上传成功'
      })
    }
  },

  // 自动保存相关
  autosave: {
    // 自动保存草稿（静默保存，不显示加载和成功提示）
    saveDraft(id: string, data: Partial<ArticleUpdateData>) {
      const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
      const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)
      const remark = buildRemarkPayload(data.allowComments, data.remark)

      const draftData: Record<string, any> = {
        articleId: Number(id),
        title: data.title || '',
        content: data.content || '',
        memberContent: data.memberContent || '',
        summary: data.summary || '',
        previewContent: data.previewContent ?? data.summary ?? '',
        enableTieredRead: normalizeBooleanFlag(data.tieredReading) === false ? '0' : '1',
        coverImage: data.coverImage || '',
        categoryId: toOptionalInteger(data.categoryId),
        status: data.status === 1 ? 1 : 0,
        allowCopy: allowCopyFlag === undefined ? '1' : allowCopyFlag ? '1' : '0',
        isRecommend: toNumericFlag(data.isRecommend),
        isTop: toNumericFlag((data as any).pinned ?? (data as any).isTop ?? false),
        accessLevel: toInteger(data.accessLevel, 0),
        isPasswd: data.isPasswd,
        seoKeywords: data.seoKeywords || '',
        seoDescription: data.seoDescription || '',
        enableWatermark: watermarkFlag === undefined ? '1' : watermarkFlag ? '1' : '0',
        tags: toTagString(data.tags)
      }

      applyPasswordSettings(draftData, data as any)

      if (!draftData.memberContent) {
        delete draftData.memberContent
      }

      if (remark !== undefined) {
        // 解析现有的remark，添加强制更新标志
        try {
          let remarkObj: Record<string, any> = {}
          if (typeof remark === 'string') {
            remarkObj = JSON.parse(remark)
          } else if (typeof remark === 'object' && remark !== null) {
            remarkObj = { ...remark }
          }
          remarkObj.forceUpdate = true
          draftData.remark = JSON.stringify(remarkObj)
        } catch (e) {
          // 如果解析失败，直接添加强制更新标志
          draftData.remark = JSON.stringify({ forceUpdate: true, allowComments: true })
        }
      } else {
        // 如果没有remark，创建一个包含强制更新标志的
        draftData.remark = JSON.stringify({ forceUpdate: true, allowComments: true })
      }

      // 使用专用的草稿保存接口
      return request.put<Article>(`${ARTICLE_PUBLIC_BASE}/draft/update`, draftData, {
        showLoading: false,
        showSuccess: false,
        showError: false // 默认不弹出错误，避免打扰用户
      })
    },

    // 创建草稿文章（用于新文章的首次自动保存）
    createDraft(data: Partial<ArticleCreateData>) {
      const allowCopyFlag = normalizeBooleanFlag(data.allowCopy)
      const watermarkFlag = normalizeBooleanFlag(data.enableWatermark)
      const remark = buildRemarkPayload(data.allowComments, data.remark)

      const draftData: Record<string, any> = {
        title: data.title || '无标题',
        content: data.content || '',
        memberContent: data.memberContent || '',
        summary: data.summary || '',
        previewContent: data.previewContent ?? data.summary ?? '',
        enableTieredRead: normalizeBooleanFlag(data.tieredReading) === false ? '0' : '1',
        coverImage: data.coverImage,
        categoryId: toOptionalInteger(data.categoryId),
        status: data.status === 1 || data.status === '1' ? 1 : 0, // 草稿状态
        allowCopy: allowCopyFlag === undefined ? '1' : allowCopyFlag ? '1' : '0',
        isTop: toNumericFlag((data as any).pinned ?? (data as any).isTop ?? false),
        isRecommend: toNumericFlag(data.isRecommend),
        accessLevel: toInteger(data.accessLevel, 0),
        isPasswd: data.isPasswd,
        seoKeywords: data.seoKeywords,
        seoDescription: data.seoDescription,
        enableWatermark: watermarkFlag === undefined ? '1' : watermarkFlag ? '1' : '0',
        tags: toTagString(data.tags)
      }

      applyPasswordSettings(draftData, data as any)
      if (!draftData.memberContent) {
        delete draftData.memberContent
      }

      if (remark !== undefined) {
        draftData.remark = remark
      }

      // 使用专用的草稿创建接口
      return request.post<Article>(`${ARTICLE_PUBLIC_BASE}/draft/create`, draftData, {
        showLoading: false,
        showSuccess: false,
        showError: false
      })
    },

    // 检查文章是否存在（用于判断是创建还是更新）
    checkExists(id: string) {
      return request.get<{ exists: boolean }>(`${ARTICLE_PUBLIC_BASE}/exists/${id}`, {}, {
        showLoading: false,
        showSuccess: false,
        showError: false
      })
    }
  },

  // 用户文章管理API（保持原有路径，因为这些可能是独立的用户管理功能）
  user: {
    // 获取当前用户的文章列表
    getUserArticles(params?: ArticleQueryParams) {
      const query = params ? { ...params } : {}
      
      // 转换状态值：字符串转整数
      if (query.status !== undefined) {
        if (query.status === 'draft') {
          query.status = 0
        } else if (query.status === 'published') {
          query.status = 1
        } else if (query.status === 'archived' || query.status === 'offline') {
          query.status = 2
        }
      }
      
      return request.get<PaginatedResponse<Article>>(`${ARTICLE_ADMIN_BASE}/user/list`, query)
    },

    // 获取当前用户的文章详情
    getUserArticle(id: string) {
      return request.get<Article>(`${ARTICLE_ADMIN_BASE}/user/detail/${id}`)
    },

    // 发布用户文章
    publishUserArticle(id: string) {
      return request.post(`${ARTICLE_ADMIN_BASE}/user/publish/${id}`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '文章发布成功'
      })
    },

    // 下线用户文章
    offlineUserArticle(id: string) {
      return request.post(`${ARTICLE_ADMIN_BASE}/user/offline/${id}`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '文章已下线'
      })
    },

    // 删除用户文章
    deleteUserArticle(id: string) {
      return request.delete(`${ARTICLE_ADMIN_BASE}/user/delete/${id}`, {}, {
        showLoading: true,
        showSuccess: true,
        successMessage: '文章删除成功'
      })
    },

    // 更新用户文章
    updateUserArticle(id: string, data: ArticleUpdateData) {
      const articleData = {
        title: data.title,
        content: data.content,
        summary: data.summary,
        previewContent: data.previewContent ?? data.summary ?? '',
        enableTieredRead: data.tieredReading === false ? '0' : '1',
        coverImage: data.coverImage,
        categoryId: data.categoryId ? Number(data.categoryId) : null,
        status: data.status === 0 ? 0 : 1,
        allowCopy: data.allowCopy ? '1' : '0',
        isTop: data.isTop ? 1 : 0,
        isRecommend: data.isRecommend ? 1 : 0,
        accessLevel: data.accessLevel ?? 0,
        isPasswd: data.isPasswd,
        seoKeywords: data.seoKeywords,
        seoDescription: data.seoDescription,
        enableWatermark: data.enableWatermark ? '1' : '0'
      }
      return request.put<Article>(`${ARTICLE_ADMIN_BASE}/user/update/${id}`, articleData, {
        showLoading: true,
        showSuccess: true,
        successMessage: '文章更新成功'
      })
    },

    // 获取用户文章统计信息
    getUserArticleStats() {
      return request.get(`${ARTICLE_ADMIN_BASE}/user/stats`)
    }
  }
}


function normalizeArticleResponse(article: Article): Article {
  if (!article) {
    return article
  }
  if ((article as any).memberContent !== undefined) {
    article.memberContent = (article as any).memberContent as string
  }
  const allowCopyFlag = normalizeBooleanFlag((article as any).allowCopy)
  ;(article as any).allowCopy = allowCopyFlag === undefined ? true : allowCopyFlag
  const rawTiered = (article as any).enableTieredRead ?? (article as any).tieredReading
  const tieredFlag = normalizeBooleanFlag(rawTiered)
  ;(article as any).tieredReading = tieredFlag === undefined ? true : tieredFlag
  if (!article.previewContent && article.summary) {
    article.previewContent = article.summary
  }
  if (!article.coverImage) {
    const rawCover =
      (article as any).coverImage ??
      (article as any).cover_image ??
      (article as any).cover ??
      (article as any).coverUrl ??
      (article as any).cover_url
    if (rawCover !== undefined && rawCover !== null) {
      const normalizedCover = typeof rawCover === 'string' ? rawCover : String(rawCover)
      article.coverImage = normalizedCover
      ;(article as any).coverImage = normalizedCover
    }
  }
  if ((article as any).previewOnly === '1') {
    article.previewOnly = true
  } else if ((article as any).previewOnly === '0') {
    article.previewOnly = false
  }

  if (!article.publishedAt) {
    const publishedSource =
      (article as any).publishTime ??
      (article as any).publish_time ??
      (article as any).publish_at ??
      (article as any).publishedTime ??
      null
    if (publishedSource) {
      article.publishedAt = publishedSource as string
    }
  }

  if (!article.createdAt) {
    const createdSource =
      (article as any).createdAt ??
      (article as any).createTime ??
      (article as any).created_at ??
      article.publishedAt ??
      null
    if (createdSource) {
      ;(article as any).createdAt = createdSource
    }
  }

  if (!article.updatedAt) {
    const updatedSource =
      (article as any).updatedAt ??
      (article as any).updateTime ??
      (article as any).updated_at ??
      article.publishedAt ??
      (article as any).createdAt ??
      null
    if (updatedSource) {
      ;(article as any).updatedAt = updatedSource
    }
  }

  if (!article.author) {
    const fallbackAuthorId =
      (article as any).authorId ??
      (article as any).userId ??
      (article as any).createBy ??
      ''
    const fallbackAuthorName =
      (article as any).authorName ??
      (article as any).author_username ??
      (article as any).author ??
      ''
    article.author = {
      id: fallbackAuthorId !== null && fallbackAuthorId !== undefined ? String(fallbackAuthorId) : '',
      username: (article as any).authorUsername ?? fallbackAuthorName ?? '',
      nickname: fallbackAuthorName || undefined,
      avatar: (article as any).authorAvatar
    }
  } else {
    if (!article.author.id) {
      const fallbackAuthorId =
        (article as any).authorId ??
        (article as any).userId ??
        (article as any).createBy ??
        ''
      article.author.id = fallbackAuthorId !== null && fallbackAuthorId !== undefined ? String(fallbackAuthorId) : ''
    }
    if (!article.author.username) {
      article.author.username =
        (article as any).authorUsername ??
        (article as any).authorName ??
        article.author.nickname ??
        ''
    }
    if (!article.author.nickname && ((article as any).authorName || article.author.username)) {
      article.author.nickname = (article as any).authorName ?? article.author.username
    }
    if (!article.author.avatar && (article as any).authorAvatar) {
      article.author.avatar = (article as any).authorAvatar
    }
  }
  if (!(article as any).authorName && (article.author?.nickname || article.author?.username)) {
    (article as any).authorName = article.author.nickname ?? article.author.username
  }

  const fullReadableFlag = normalizeBooleanFlag((article as any).fullReadable)
  article.fullReadable = fullReadableFlag === undefined ? !!article.fullReadable : fullReadableFlag
  const watermarkFlag = normalizeBooleanFlag((article as any).enableWatermark)
  ;(article as any).enableWatermark = watermarkFlag === undefined ? true : watermarkFlag
  const tags = splitTags((article as any).tags)
  ;(article as any).tags = tags
  const remarkValue = (article as any).remark
  let allowComments = normalizeBooleanFlag((article as any).allowComments)
  if (allowComments === undefined) {
    allowComments = extractAllowCommentsFromRemark(remarkValue)
  }
  ;(article as any).allowComments = allowComments === undefined ? true : allowComments
  const pinnedFlag = normalizeBooleanFlag((article as any).pinned ?? (article as any).isTop ?? (article as any).top)
  ;(article as any).pinned = pinnedFlag === undefined ? false : pinnedFlag
  ;(article as any).isTop = (article as any).pinned ? 1 : 0
  const recommendFlag = normalizeBooleanFlag((article as any).isRecommend ?? (article as any).recommend)
  if (recommendFlag !== undefined) {
    (article as any).isRecommend = recommendFlag
  }
  if ((article as any).accessLevel !== undefined) {
    article.accessLevel = toInteger((article as any).accessLevel, article.accessLevel ?? 0)
  }
  if (typeof (article as any).categoryId === 'number') {
    (article as any).categoryId = String((article as any).categoryId)
  }
  const rawIsPasswd = (article as any).isPasswd ?? (article as any).is_passwd
  if (rawIsPasswd !== undefined && rawIsPasswd !== null && rawIsPasswd !== '') {
    const numericIsPasswd = Number(rawIsPasswd)
    if (!Number.isNaN(numericIsPasswd)) {
      ;(article as any).isPasswd = numericIsPasswd
    }
  }
  delete (article as any).is_passwd
  if ((article as any).isPasswd === undefined) {
    if (article.passwordProtected !== undefined) {
      ;(article as any).isPasswd = article.passwordProtected ? 0 : 1
    } else {
      const hasPassword = typeof article.password === 'string' && article.password.trim() !== ''
      ;(article as any).isPasswd = hasPassword ? 0 : 1
      if (article.passwordProtected === undefined) {
        article.passwordProtected = hasPassword
      }
    }
  } else if (article.passwordProtected === undefined) {
    article.passwordProtected = (article as any).isPasswd === 0
  }
  return article
}
