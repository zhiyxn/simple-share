// 文章相关类型定义

// 文章状态
export enum ArticleStatus {
  DRAFT = 0,
  PUBLISHED = 1,
  ARCHIVED = 2
}

// 文章审核状态
export enum ArticleReviewStatus {
  PENDING = 0,
  APPROVED = 1,
  REJECTED = 2
}


// 文章可见性
export enum ArticleVisibility {
  PUBLIC = 'public',
  PRIVATE = 'private',
  PASSWORD = 'password'
}

// 文章基础信息
export interface Article {
  id: string
  title: string
  content: string
  memberContent?: string
  hasMemberContent?: boolean
  memberContentLocked?: boolean
  summary?: string
  coverImage?: string
  status: ArticleStatus
  reviewStatus?: ArticleReviewStatus
  visibility: ArticleVisibility
  password?: string
  isPasswd?: number
  categoryId?: string
  category?: {
    id: string
    name: string
    slug: string
  }
  tags: string[]
  viewCount: number
  likeCount: number
  commentCount: number
  allowCopy: boolean | string
  copyProtection?: {
    enabled?: boolean
    disableCopy?: boolean
    disableSelect?: boolean
    disableRightClick?: boolean
    disablePrint?: boolean
    watermark?: {
      enabled?: boolean
      text?: string
      includeUser?: boolean
      includeTime?: boolean
      opacity?: number
      fontSize?: number
      angle?: number
      color?: string
    }
  }
  tieredReading?: boolean
  previewContent?: string
  previewOnly?: boolean
  fullReadable?: boolean
  enableWatermark?: boolean | string
  watermarkText?: string
  pinned?: boolean
  isRecommend?: boolean | number
  accessLevel?: number
  allowComments?: boolean
  remark?: string
  passwordProtected?: boolean
  passwordVerified?: boolean
  seoTitle?: string
  seoDescription?: string
  seoKeywords?: string
  publishedAt?: string
  createdAt: string
  updatedAt: string
  reviewStatusLabel?: string
  pendingReview?: boolean
  reviewApproved?: boolean
  reviewRejected?: boolean
  author: {
    id: string
    username: string
    nickname?: string
    avatar?: string
  }
  tenant: {
    id: string
    name: string
    domain: string
  }
}

// 文章创建数据
export interface ArticleCreateData {
  title: string
  content: string
  memberContent?: string
  summary?: string
  coverImage?: string
  status: ArticleStatus
  visibility: ArticleVisibility
  password?: string
  categoryId?: string
  tags: string[]
  allowCopy?: boolean
  tieredReading?: boolean
  previewContent?: string
  enableWatermark?: boolean
  watermarkText?: string
  pinned?: boolean
  isRecommend?: boolean
  accessLevel?: number
  seoTitle?: string
  seoDescription?: string
  seoKeywords?: string
  publishedAt?: string
  allowComments?: boolean
  remark?: string
  passwordEnabled?: boolean
  passwordProtected?: boolean
  isPasswd?: number
  hasExistingPassword?: boolean
  clearPassword?: boolean
}

// 文章更新数据
export interface ArticleUpdateData {
  title?: string
  content?: string
  memberContent?: string
  summary?: string
  coverImage?: string
  status?: ArticleStatus
  visibility?: ArticleVisibility
  password?: string
  categoryId?: string
  tags?: string[]
  allowCopy?: boolean
  tieredReading?: boolean
  previewContent?: string
  enableWatermark?: boolean
  watermarkText?: string
  pinned?: boolean
  isTop?: boolean
  isRecommend?: boolean
  accessLevel?: number
  seoTitle?: string
  seoDescription?: string
  seoKeywords?: string
  publishedAt?: string
  allowComments?: boolean
  remark?: string
  passwordEnabled?: boolean
  passwordProtected?: boolean
  isPasswd?: number
  hasExistingPassword?: boolean
  clearPassword?: boolean
}

// 文章列表查询参数
export interface ArticleListParams {
  page?: number
  limit?: number
  keyword?: string
  category?: string
  status?: ArticleStatus
  reviewStatus?: ArticleReviewStatus
  visibility?: ArticleVisibility
  authorId?: string
  tags?: string[]
  startDate?: string
  endDate?: string
  sortBy?: 'createdAt' | 'updatedAt' | 'publishedAt' | 'viewCount' | 'likeCount'
  sortOrder?: 'asc' | 'desc'
}

// 文章查询参数（兼容性）
export interface ArticleQueryParams extends ArticleListParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  title?: string
  categoryId?: number
  isRecommend?: number
}

// 文章统计信息
export interface ArticleStats {
  totalArticles: number
  publishedArticles: number
  draftArticles: number
  archivedArticles: number
  pendingReviews?: number
  rejectedArticles?: number
  totalViews: number
  totalLikes: number
  totalComments: number
  todayViews: number
  todayLikes: number
  todayComments: number
}

// 文章搜索结果
export interface ArticleSearchResult {
  id: string
  title: string
  summary: string
  coverImage?: string
  categoryName?: string
  tags: string[]
  publishedAt: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  author: {
    username: string
    nickname?: string
    avatar?: string
  }
  highlight?: {
    title?: string[]
    content?: string[]
    summary?: string[]
  }
  raw?: Article
}

// 文章导入/导出
export interface ArticleExportData {
  articles: Article[]
  categories: any[]
  exportedAt: string
  version: string
}

export interface ArticleImportData {
  articles: ArticleCreateData[]
  categories?: any[]
  overwriteExisting?: boolean
}

// 文章批量操作
export interface ArticleBatchOperation {
  ids: string[]
  operation: 'publish' | 'archive' | 'delete' | 'updateCategory' | 'updateTags'
  data?: {
    status?: ArticleStatus
    categoryId?: string
    tags?: string[]
  }
}

// 文章版本历史
export interface ArticleVersion {
  id: string
  articleId: string
  version: number
  title: string
  content: string
  summary?: string
  changeLog?: string
  createdAt: string
  createdBy: {
    id: string
    username: string
    nickname?: string
  }
}

// 文章评论
export interface ArticleComment {
  id: string
  articleId: string
  content: string
  parentId?: string
  author: {
    id: string
    username: string
    nickname?: string
    avatar?: string
  }
  likeCount: number
  isLiked: boolean
  status: 'approved' | 'pending' | 'rejected'
  createdAt: string
  updatedAt: string
  replies?: ArticleComment[]
}

// 文章点赞
export interface ArticleLike {
  id: string
  articleId: string
  userId: string
  createdAt: string
}

// 文章分享
export interface ArticleShare {
  id: string
  articleId: string
  platform: 'wechat' | 'weibo' | 'qq' | 'link' | 'email'
  sharedBy?: string
  createdAt: string
}

// 兼容历史类型导出
export type Comment = ArticleComment
