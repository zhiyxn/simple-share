import { request } from './request'

export interface FavoriteArticle {
  id: string
  title: string
  summary?: string
  cover?: string
  author: string
  authorAvatar?: string
  authorId?: string
  categoryId?: string
  categoryName?: string
  status?: number
}

export interface FavoriteItem {
  id: string
  favoriteId: string
  articleId: string
  folderName?: string
  tags?: string[]
  notes?: string
  created_at?: string
  updated_at?: string
  article: FavoriteArticle
}

export interface FavoriteListResponse {
  items: FavoriteItem[]
  total: number
  page: number
  pageSize: number
}

export interface FavoriteQueryParams {
  page?: number
  pageSize?: number
  sortBy?: 'favoriteTime' | 'articleTitle' | 'readTime'
  sortOrder?: 'asc' | 'desc'
  categoryId?: string
  keyword?: string
}

/**
 * 获取收藏列表
 */
const FAVORITE_BASE = '/favorites'

export const getFavorites = (params: FavoriteQueryParams = {}): Promise<FavoriteListResponse> => {
  return request.get(`${FAVORITE_BASE}`, { params })
}

/**
 * 添加收藏
 */
export const addFavorite = (articleId: string): Promise<void> => {
  return request.post(`${FAVORITE_BASE}/${articleId}`)
}

/**
 * 取消收藏
 */
export const removeFavorite = (favoriteId: string): Promise<void> => {
  return request.delete(`${FAVORITE_BASE}/records/${favoriteId}`)
}

/**
 * 检查是否已收藏
 */
export const checkFavorite = (articleId: string): Promise<{ favorited: boolean }> => {
  return request.get(`${FAVORITE_BASE}/${articleId}/check`)
}

/**
 * 批量取消收藏
 */
export const batchRemoveFavorites = (favoriteIds: string[]): Promise<void> => {
  return request.delete(`${FAVORITE_BASE}/batch`, { data: { favoriteIds } })
}

export const favoriteApi = {
  getFavorites,
  addFavorite,
  removeFavorite,
  checkFavorite,
  batchRemoveFavorites
}
