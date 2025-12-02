import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api'
import type { Category, CategoryCreateData, CategoryUpdateData } from '@/types/category'
import { normalizeCategory, normalizeCategoryList } from '@/utils/category'

export const useCategoryStore = defineStore('category', () => {
  // 状态
  const categories = ref<Category[]>([])
  const currentCategory = ref<Category | null>(null)
  const isLoading = ref(false)
  const createLoading = ref(false)
  const updateLoading = ref(false)
  const deleteLoading = ref(false)

  // 计算属性
  const hasCategories = computed(() => categories.value.length > 0)
  
  const rootCategories = computed(() => 
    categories.value.filter(category => !category.parentId)
  )
  
  const categoryTree = computed(() => {
    const buildTree = (parentId: string | null = null): Category[] => {
      return categories.value
        .filter(category => category.parentId === parentId)
        .map(category => ({
          ...category,
          children: buildTree(category.id)
        }))
        .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    }
    return buildTree()
  })
  
  const categoryOptions = computed(() => {
    const buildOptions = (cats: Category[], level = 0): Array<{ label: string; value: string; level: number }> => {
      const options: Array<{ label: string; value: string; level: number }> = []
      
      cats.forEach(cat => {
        options.push({
          label: '　'.repeat(level) + cat.name,
          value: cat.id,
          level
        })
        
        if (cat.children && cat.children.length > 0) {
          options.push(...buildOptions(cat.children, level + 1))
        }
      })
      
      return options
    }
    
    return buildOptions(categoryTree.value)
  })

  // 根据ID获取分类
  const getCategoryById = (id: string): Category | undefined => {
    const findCategory = (cats: Category[]): Category | undefined => {
      for (const cat of cats) {
        if (cat.id === id) return cat
        if (cat.children) {
          const found = findCategory(cat.children)
          if (found) return found
        }
      }
      return undefined
    }
    return findCategory(categories.value)
  }

  // 获取分类路径
  const getCategoryPath = (id: string): Category[] => {
    const path: Category[] = []
    
    const findPath = (cats: Category[], targetId: string, currentPath: Category[]): boolean => {
      for (const cat of cats) {
        const newPath = [...currentPath, cat]
        
        if (cat.id === targetId) {
          path.push(...newPath)
          return true
        }
        
        if (cat.children && findPath(cat.children, targetId, newPath)) {
          return true
        }
      }
      return false
    }
    
    findPath(categoryTree.value, id, [])
    return path
  }

  // 获取子分类
  const getChildCategories = (parentId: string | null = null): Category[] => {
    return categories.value.filter(category => category.parentId === parentId)
  }

  // 检查是否有子分类
  const hasChildren = (id: string): boolean => {
    return categories.value.some(category => category.parentId === id)
  }

  const resolveCategoryArray = (response: any): any[] => {
    if (Array.isArray(response)) return response
    if (Array.isArray(response?.items)) return response.items
    if (Array.isArray(response?.data)) return response.data
    if (Array.isArray(response?.rows)) return response.rows
    return []
  }

  // 获取分类列表
  const fetchCategories = async () => {
    // 如果已经有分类数据且不在加载中，直接返回
    if (hasCategories.value && !isLoading.value) {
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
      
      const response = await categoryApi.getCategories()
      const items = resolveCategoryArray(response)
      const normalized = normalizeCategoryList(items)
      categories.value = normalized
      
      return normalized
    } catch (error: any) {
      ElMessage.error(error.message || '获取分类列表失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 获取单个分类
  const fetchCategory = async (id: string) => {
    try {
      isLoading.value = true
      
      const response = await categoryApi.getCategory(id)
      const normalized = normalizeCategory(response)
      currentCategory.value = normalized
      
      return normalized
    } catch (error: any) {
      ElMessage.error(error.message || '获取分类详情失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 创建分类
  const createCategory = async (data: CategoryCreateData) => {
    try {
      createLoading.value = true
      
      const response = await categoryApi.createCategory(data)
      const normalized = normalizeCategory(response)
      
      if (normalized.id) {
        categories.value.push(normalized)
      }
      
      ElMessage.success('分类创建成功')
      return normalized
    } catch (error: any) {
      ElMessage.error(error.message || '创建分类失败')
      throw error
    } finally {
      createLoading.value = false
    }
  }

  // 更新分类
  const updateCategory = async (id: string, data: CategoryUpdateData) => {
    try {
      updateLoading.value = true
      
      const response = await categoryApi.updateCategory(id, data)
      const normalized = normalizeCategory(response)
      
      // 更新列表中的分类
      const index = categories.value.findIndex(category => category.id === id)
      if (index !== -1) {
        categories.value[index] = normalized
      }
      
      // 更新当前分类
      if (currentCategory.value?.id === id) {
        currentCategory.value = normalized
      }
      
      ElMessage.success('分类更新成功')
      return normalized
    } catch (error: any) {
      ElMessage.error(error.message || '更新分类失败')
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  // 删除分类
  const deleteCategory = async (id: string) => {
    try {
      deleteLoading.value = true
      
      await categoryApi.deleteCategory(id)
      
      // 从列表中移除（包括子分类）
      const removeCategory = (categoryId: string) => {
        const children = getChildCategories(categoryId)
        children.forEach(child => removeCategory(child.id))
        
        const index = categories.value.findIndex(category => category.id === categoryId)
        if (index !== -1) {
          categories.value.splice(index, 1)
        }
      }
      
      removeCategory(id)
      
      // 清除当前分类
      if (currentCategory.value?.id === id) {
        currentCategory.value = null
      }
      
      ElMessage.success('分类删除成功')
    } catch (error: any) {
      ElMessage.error(error.message || '删除分类失败')
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 批量删除分类
  const batchDeleteCategories = async (ids: string[]) => {
    try {
      deleteLoading.value = true
      
      await categoryApi.batchDeleteCategories(ids)
      
      // 从列表中移除
      categories.value = categories.value.filter(category => !ids.includes(category.id))
      
      ElMessage.success('批量删除成功')
    } catch (error: any) {
      ElMessage.error(error.message || '批量删除失败')
      throw error
    } finally {
      deleteLoading.value = false
    }
  }

  // 更新分类排序
  const updateCategorySort = async (id: string, sort: number) => {
    return updateCategory(id, { sort })
  }

  // 移动分类
  const moveCategory = async (id: string, parentId: string | null) => {
    return updateCategory(id, { parentId })
  }

  // 批量更新排序
  const batchUpdateSort = async (updates: Array<{ id: string; sort: number }>) => {
    try {
      updateLoading.value = true
      
      await categoryApi.batchUpdateSort(updates)
      
      // 更新本地状态
      updates.forEach(update => {
        const category = categories.value.find(cat => cat.id === update.id)
        if (category) {
          category.sort = update.sort
        }
      })
      
      ElMessage.success('排序更新成功')
    } catch (error: any) {
      ElMessage.error(error.message || '更新排序失败')
      throw error
    } finally {
      updateLoading.value = false
    }
  }

  // 清除当前分类
  const clearCurrentCategory = () => {
    currentCategory.value = null
  }

  // 清除所有数据
  const clearAll = () => {
    categories.value = []
    currentCategory.value = null
  }

  // 初始化
  const init = async () => {
    if (!hasCategories.value) {
      await fetchCategories()
    }
  }

  return {
    // 状态
    categories,
    currentCategory,
    isLoading,
    createLoading,
    updateLoading,
    deleteLoading,
    
    // 计算属性
    hasCategories,
    rootCategories,
    categoryTree,
    categoryOptions,
    
    // 方法
    getCategoryById,
    getCategoryPath,
    getChildCategories,
    hasChildren,
    fetchCategories,
    fetchCategory,
    createCategory,
    updateCategory,
    deleteCategory,
    batchDeleteCategories,
    updateCategorySort,
    moveCategory,
    batchUpdateSort,
    clearCurrentCategory,
    clearAll,
    init
  }
}, {
  persist: {
    key: 'simple-share-category',
    storage: localStorage,
    paths: ['categories']
  }
})
