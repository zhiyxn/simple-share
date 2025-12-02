// 分类相关类型定义

// 分类基础信息
export interface Category {
  id: string
  name: string
  slug?: string
  description?: string
  icon?: string
  color?: string
  parentId?: string
  sort: number
  isVisible?: boolean
  isActive?: boolean
  enabled?: boolean
  visible?: boolean
  articleCount?: number
  createdAt?: string
  updatedAt?: string
  children?: Category[]
  parent?: {
    id: string
    name: string
    slug: string
  }
  tenantId?: string
  // 兼容后端字段
  categoryId?: string
  categoryName?: string
  orderNum?: number
  status?: string
}

// 分类创建数据
export interface CategoryCreateData {
  name: string
  slug?: string
  description?: string
  icon?: string
  color?: string
  parentId?: string
  sort?: number
  isVisible?: boolean
}

// 分类更新数据
export interface CategoryUpdateData {
  name?: string
  slug?: string
  description?: string
  icon?: string
  color?: string
  parentId?: string
  sort?: number
  isVisible?: boolean
}

// 分类树节点
export interface CategoryTreeNode extends Category {
  level: number
  hasChildren: boolean
  expanded?: boolean
  children?: CategoryTreeNode[]
}

// 分类选项
export interface CategoryOption {
  label: string
  value: string
  level: number
  disabled?: boolean
  children?: CategoryOption[]
}

// 分类统计信息
export interface CategoryStats {
  totalCategories: number
  rootCategories: number
  maxDepth: number
  categoriesWithArticles: number
  emptyCategoriesCount: number
  mostUsedCategory?: {
    id: string
    name: string
    articleCount: number
  }
}

// 分类查询参数
export interface CategoryListParams {
  parentId?: string
  includeChildren?: boolean
  includeEmpty?: boolean
  sortBy?: 'name' | 'sort' | 'createdAt' | 'articleCount'
  sortOrder?: 'asc' | 'desc'
  keyword?: string
}

// 分类移动操作
export interface CategoryMoveOperation {
  categoryId: string
  newParentId?: string
  newSort?: number
}

// 分类批量操作
export interface CategoryBatchOperation {
  ids: string[]
  operation: 'delete' | 'move' | 'updateVisibility' | 'updateSort'
  data?: {
    parentId?: string
    isVisible?: boolean
    sortUpdates?: Array<{ id: string; sort: number }>
  }
}

// 分类导入/导出
export interface CategoryExportData {
  categories: Category[]
  exportedAt: string
  version: string
}

export interface CategoryImportData {
  categories: CategoryCreateData[]
  overwriteExisting?: boolean
  preserveHierarchy?: boolean
}

// 分类路径
export interface CategoryPath {
  id: string
  name: string
  slug: string
  level: number
}

// 分类验证规则
export interface CategoryValidation {
  nameRequired: boolean
  nameMinLength: number
  nameMaxLength: number
  slugRequired: boolean
  slugPattern: RegExp
  descriptionMaxLength: number
  maxDepth: number
  allowDuplicateNames: boolean
}

// 分类搜索结果
export interface CategorySearchResult {
  id: string
  name: string
  slug: string
  description?: string
  articleCount: number
  path: CategoryPath[]
  highlight?: {
    name?: string[]
    description?: string[]
  }
}

// 分类关联的文章信息
export interface CategoryArticleInfo {
  id: string
  title: string
  status: string
  publishedAt?: string
  viewCount: number
  author: {
    username: string
    nickname?: string
  }
}

// 分类使用统计
export interface CategoryUsageStats {
  categoryId: string
  categoryName: string
  articleCount: number
  totalViews: number
  totalLikes: number
  lastArticleDate?: string
  growthRate: number
}

// 兼容历史类型导出
export type CategoryQueryParams = CategoryListParams
export type CategoryMoveData = CategoryMoveOperation
export type CategoryValidationRule = CategoryValidation
