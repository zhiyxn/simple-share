import type { Category } from '@/types/category'

const toStringOrUndefined = (value: unknown): string | undefined => {
  if (value === null || value === undefined) return undefined
  return String(value)
}

const normalizeParent = (raw: any) => {
  const parentRaw = raw?.parent ?? (raw?.parentName ? { id: raw.parentId, name: raw.parentName } : undefined)
  if (!parentRaw) return undefined

  const idSource = parentRaw?.id ?? parentRaw?.categoryId ?? raw?.parentId
  if (idSource === null || idSource === undefined) return undefined

  return {
    id: String(idSource),
    name: parentRaw?.name ?? parentRaw?.categoryName ?? '',
    slug: parentRaw?.slug ?? parentRaw?.categoryPath ?? ''
  }
}

export function normalizeCategory(raw: any): Category {
  const idSource = raw?.id ?? raw?.categoryId ?? raw?.category_id
  const nameSource = raw?.name ?? raw?.categoryName ?? raw?.category_name ?? ''

  const children = Array.isArray(raw?.children) ? normalizeCategoryList(raw.children) : undefined

  const category: Category = {
    id: toStringOrUndefined(idSource) ?? '',
    name: nameSource,
    slug: raw?.slug ?? raw?.categoryPath ?? raw?.path ?? '',
    description: raw?.description ?? raw?.remark ?? '',
    icon: raw?.icon ?? '',
    color: raw?.color,
    parentId: toStringOrUndefined(raw?.parentId ?? raw?.parent_id ?? raw?.parent?.id ?? raw?.parent?.categoryId),
    sort: Number(raw?.sort ?? raw?.orderNum ?? raw?.order_num ?? 0),
    isVisible: raw?.isVisible ?? (raw?.status !== undefined ? String(raw.status) === '0' : undefined),
    articleCount: raw?.articleCount ?? raw?.article_count ?? undefined,
    createdAt: raw?.createdAt ?? raw?.createTime ?? raw?.created_at,
    updatedAt: raw?.updatedAt ?? raw?.updateTime ?? raw?.updated_at,
    tenantId: raw?.tenantId,
    categoryId: toStringOrUndefined(raw?.categoryId),
    categoryName: raw?.categoryName ?? nameSource,
    orderNum: raw?.orderNum ?? raw?.order_num ?? raw?.sort ?? 0,
    status: raw?.status,
    children
  }

  const parent = normalizeParent(raw)
  if (parent) {
    category.parent = parent
  }

  return category
}

export function normalizeCategoryList(list: any[]): Category[] {
  return list
    .map(item => normalizeCategory(item))
    .filter(category => category.id !== '')
}
