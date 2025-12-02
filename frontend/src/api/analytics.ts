import request from '@/api/request'
import type { 
  AnalyticsResponse, 
  AnalyticsQuery, 
  OverviewStats, 
  ArticleStats, 
  UserStats, 
  ViewStats, 
  CategoryStats, 
  SystemStats 
} from '@/types/analytics'

const prefix = '/system/admin/analytics'

// 获取综合统计数据
export function getAnalyticsData(params?: AnalyticsQuery) {
  return request<AnalyticsResponse>({
    url: prefix,
    method: 'get',
    params
  })
}

// 获取概览统计
export function getOverviewStats(params?: AnalyticsQuery) {
  return request<OverviewStats>({
    url: `${prefix}/overview`,
    method: 'get',
    params
  })
}

// 获取文章统计
export function getArticleStats(params?: AnalyticsQuery) {
  return request<ArticleStats>({
    url: `${prefix}/articles`,
    method: 'get',
    params
  })
}

// 获取用户统计
export function getUserStats(params?: AnalyticsQuery) {
  return request<UserStats>({
    url: `${prefix}/users`,
    method: 'get',
    params
  })
}

// 获取访问统计
export function getViewStats(params?: AnalyticsQuery) {
  return request<ViewStats>({
    url: `${prefix}/views`,
    method: 'get',
    params
  })
}

// 获取分类统计
export function getCategoryStats(params?: AnalyticsQuery) {
  return request<CategoryStats[]>({
    url: `${prefix}/categories`,
    method: 'get',
    params
  })
}

// 获取系统统计
export function getSystemStats() {
  return request<SystemStats>({
    url: `${prefix}/system`,
    method: 'get'
  })
}

export default {
  getAnalyticsData,
  getOverviewStats,
  getArticleStats,
  getUserStats,
  getViewStats,
  getCategoryStats,
  getSystemStats
}
