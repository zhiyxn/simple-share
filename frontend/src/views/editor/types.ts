export interface EditorPost {
  id?: string
  title: string
  content: string
  memberContent?: string
  excerpt?: string
  summary?: string
  tags: string[]
  categoryId?: string | null
  coverImage?: string
  status: 'draft' | 'published' | 'private'
  publishedAt?: Date
  allowComments: boolean
  isTop: number
  published: boolean
  previewContent: string
  tieredReading: boolean
  allowCopy: number
  enableWatermark: boolean
  isRecommend: number
  accessLevel: number
  createdAt?: Date
  updatedAt?: Date
}

export interface EditorUser {
  id: string
  name: string
  email: string
}

export interface OutlineHeading {
  text: string
  level: number
}
