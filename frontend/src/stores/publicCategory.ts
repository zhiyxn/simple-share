import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { publicCategoryApi } from '@/api/category'
import type { Category } from '@/types/category'
import { normalizeCategory, normalizeCategoryList } from '@/utils/category'

export const usePublicCategoryStore = defineStore('publicCategory', () => {
  // 状态
  const categories = ref<Category[]>([])
  const hotCategories = ref<Category[]>([])
  const categoryTree = ref<Category[]>([])
  const isLoading = ref(false)

  // 计算属性
  const hasCategories = computed(() => categories.value.length > 0)

  const rootCategories = computed(() =>
    categories.value.filter(category => !category.parentId)
  )

  const resolveCategoryArray = (value: any): any[] => {
    if (Array.isArray(value)) return value
    if (Array.isArray(value?.items)) return value.items
    if (Array.isArray(value?.data)) return value.data
    if (Array.isArray(value?.rows)) return value.rows
    return []
  }

  // 获取分类列表
  const fetchCategories = async (options?: { force?: boolean }) => {
    const force = options?.force ?? false

    // 如果已经有分类数据且不在加载中，直接返回
    if (!force && hasCategories.value && !isLoading.value) {
      categories.value = normalizeCategoryList(categories.value as any)
      return categories.value
    }

    // 如果正在加载中，等待加载完成
    if (isLoading.value) {
      return new Promise((resolve) => {
        const checkLoading = () => {
          if (!isLoading.value) {
            resolve(categories.value)
          } else {
            setTimeout(checkLoading, 100)
          }
        }
        checkLoading()
      })
    }

    try {
      isLoading.value = true

      console.log('调用API获取分类数据...')
      const response = await publicCategoryApi.getCategories()
      console.log('API响应原始数据:', response)
      const list = resolveCategoryArray(response)
      categories.value = normalizeCategoryList(list)
      console.log('设置到store的分类数据:', categories.value)

      return categories.value
    } catch (error: any) {
      console.error('获取分类列表失败:', error)
      categories.value = []
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 刷新分类列表
  const refreshCategories = async () => {
    await fetchCategories({ force: true })
  }

  // 获取热门分类
  const fetchHotCategories = async (limit: number = 10) => {
    try {
      const response = await publicCategoryApi.getHotCategories(limit)
      const list = resolveCategoryArray(response)
      hotCategories.value = normalizeCategoryList(list)
      return hotCategories.value
    } catch (error: any) {
      console.error('获取热门分类失败:', error)
      hotCategories.value = []
      throw error
    }
  }

  // 获取分类树
  const fetchCategoryTree = async () => {
    try {
      const response = await publicCategoryApi.getCategoryTree()
      const list = resolveCategoryArray(response)
      categoryTree.value = normalizeCategoryList(list)
      return categoryTree.value
    } catch (error: any) {
      console.error('获取分类树失败:', error)
      categoryTree.value = []
      throw error
    }
  }

  // 获取分类详情
  const fetchCategory = async (id: string) => {
    try {
      const response = await publicCategoryApi.getCategory(id)
      return normalizeCategory(response)
    } catch (error: any) {
      console.error('获取分类详情失败:', error)
      throw error
    }
  }

  // 获取子分类
  const fetchChildren = async (parentId: string) => {
    try {
      const response = await publicCategoryApi.getChildren(parentId)
      const list = resolveCategoryArray(response)
      return normalizeCategoryList(list)
    } catch (error: any) {
      console.error('获取子分类失败:', error)
      return []
    }
  }

  // 搜索分类
  const searchCategories = async (keyword: string) => {
    try {
      const response = await publicCategoryApi.search(keyword)
      const list = resolveCategoryArray(response)
      return normalizeCategoryList(list)
    } catch (error: any) {
      console.error('搜索分类失败:', error)
      return []
    }
  }

  // 根据ID获取分类
  const getCategoryById = (id: string): Category | undefined => {
    return categories.value.find(category => category.id === id)
  }

  // 清除所有数据
  const clearAll = () => {
    categories.value = []
    hotCategories.value = []
    categoryTree.value = []
  }

  // 初始化
  const init = async (options?: { force?: boolean }) => {
    if (options?.force) {
      await fetchCategories({ force: true })
      return
    }
    if (!hasCategories.value) {
      await fetchCategories()
    }
  }

  return {
    // 状态
    categories,
    hotCategories,
    categoryTree,
    isLoading,

    // 计算属性
    hasCategories,
    rootCategories,

    // 方法
    fetchCategories,
    refreshCategories,
    fetchHotCategories,
    fetchCategoryTree,
    fetchCategory,
    fetchChildren,
    searchCategories,
    getCategoryById,
    clearAll,
    init
  }
}, {
  persist: {
    key: 'simple-share-public-category',
    storage: localStorage,
    paths: ['categories', 'hotCategories', 'categoryTree']
  }
})
