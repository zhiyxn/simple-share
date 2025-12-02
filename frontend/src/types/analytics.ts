// 统计分析相关类型定义

export interface OverviewStats {
  totalArticles: number
  totalUsers: number
  totalViews: number
  totalComments: number
  todayArticles: number
  todayUsers: number
  todayViews: number
  todayComments: number
}

export interface ArticleStats {
  publishedCount: number
  draftCount: number
  totalViews: number
  totalComments: number
  avgViewsPerArticle: number
  popularArticles: PopularArticle[]
}

export interface PopularArticle {
  id: number
  title: string
  views: number
  comments: number
  publishTime: string
  author: string
}

export interface UserStats {
  totalUsers: number
  activeUsers: number
  newUsersToday: number
  newUsersThisWeek: number
  newUsersThisMonth: number
  userGrowthData: UserGrowthData[]
}

export interface UserGrowthData {
  date: string
  newUsers: number
  totalUsers: number
}

export interface ViewStats {
  totalViews: number
  todayViews: number
  avgDailyViews: number
  viewTrends: ViewTrendData[]
  topViewedArticles: PopularArticle[]
}

export interface ViewTrendData {
  date: string
  views: number
  uniqueViews: number
}

export interface CategoryStats {
  categoryName: string
  articleCount: number
  totalViews: number
  percentage: number
}

export interface SystemStats {
  serverInfo: {
    os: string
    javaVersion: string
    memory: {
      total: string
      used: string
      free: string
    }
    disk: {
      total: string
      used: string
      free: string
    }
  }
  databaseInfo: {
    type: string
    version: string
    size: string
  }
}

export interface AnalyticsQuery {
  startDate?: string
  endDate?: string
  period?: 'day' | 'week' | 'month' | 'year'
}

export interface AnalyticsResponse {
  overview: OverviewStats
  articles: ArticleStats
  users: UserStats
  views: ViewStats
  categories: CategoryStats[]
  system: SystemStats
}