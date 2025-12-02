export interface ArticlePreviewPayload {
  id: string
  title: string
  summary?: string
  content?: string
  coverImage?: string
  categoryId?: string
  categoryName?: string
  category?: {
    id?: string
    name?: string
    slug?: string
  }
  authorName?: string
  tags?: string[]
  viewCount?: number
  likeCount?: number
  commentCount?: number
  accessLevel?: number
  publishedAt?: string
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}
