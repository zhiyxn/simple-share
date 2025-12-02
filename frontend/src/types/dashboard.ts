export interface DashboardRange {
  preset?: string
  startDate: string
  endDate: string
}

export interface DashboardOverview {
  totalArticles: number
  publishedArticles: number
  draftArticles: number
  privateArticles: number
  reviewPendingArticles: number
  reviewRejectedArticles: number
  totalViews: number
  totalLikes: number
  totalCollects: number
  totalComments: number
  totalShares: number
  weeklyNewArticles: number
  todayNewArticles: number
  viewGrowthRate: number
  engagementGrowthRate: number
  lastUpdated: string
}

export interface DashboardTrendPoint {
  date: string
  published: number
  views: number
  likes: number
  comments: number
  shares: number
}

export interface DashboardTrend {
  points: DashboardTrendPoint[]
  totalViews: number
  averageViewsPerArticle: number
  wowChange: number
}

export interface DashboardFunnelStage {
  key: string
  label: string
  value: number
  conversion: number
  hint: string
}

export interface DashboardFunnel {
  stages: DashboardFunnelStage[]
}

export interface DashboardSegmentItem {
  key: string
  label: string
  articleCount: number
  viewCount: number
  likeCount: number
  commentCount: number
  shareCount: number
  percentage: number
}

export interface DashboardSegments {
  categories: DashboardSegmentItem[]
  tags: DashboardSegmentItem[]
  accessLevels: DashboardSegmentItem[]
}

export interface DashboardArticleTile {
  articleId: number
  title: string
  summary: string
  authorName: string
  categoryName: string
  status: number
  accessLevel: number
  recommend: boolean
  top: boolean
  passwordProtected: boolean
  publishTime: string | null
  viewCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  shareCount: number
  engagement: number
}

export interface DashboardLeaderboard {
  topViews: DashboardArticleTile[]
  topEngagement: DashboardArticleTile[]
  latestPublished: DashboardArticleTile[]
}

export interface DashboardModerationRecord {
  articleId: number
  title: string
  reviewStatus: number | null
  status: number | null
  reviewer: string
  updatedAt: string | null
}

export interface DashboardModeration {
  pendingReviews: number
  rejectedReviews: number
  oldestDraftDays: number
  records: DashboardModerationRecord[]
}

export interface DashboardInsight {
  title: string
  description: string
  severity: 'success' | 'info' | 'warning' | 'danger' | string
}

export interface DashboardResponse {
  range: DashboardRange
  overview: DashboardOverview
  trend: DashboardTrend
  funnel: DashboardFunnel
  segments: DashboardSegments
  leaderboards: DashboardLeaderboard
  moderation: DashboardModeration
  insights: DashboardInsight[]
}

export interface DashboardFilters {
  range?: string
  startDate?: string
  endDate?: string
  status?: number
  accessLevel?: number
  categoryId?: number
  authorId?: number
}
