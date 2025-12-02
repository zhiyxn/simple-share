<template>

  <div class="category-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">分类管理</h2>
        <div class="page-description">管理文章分类和层级结构</div>
      </div>
      
      <div class="header-right">
        <el-button
          v-if="hasPermission('article:category:add')"
          type="primary"
          :icon="Plus"
          @click="createCategory"
        >
          创建分类
        </el-button>
      </div>
    </div>

    <!-- 工具栏 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类名称..."
            :prefix-icon="Search"
            clearable
            style="width: 300px;"
            @input="handleSearch"
          />
          
          <el-button
            :icon="Refresh"
            @click="loadCategories"
          >
            刷新
          </el-button>
        </div>
        
        <div class="toolbar-right">
          <el-button
            :icon="Sort"
            @click="toggleExpandAll"
          >
            {{ expandAll ? '收起全部' : '展开全部' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 分类树表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="filteredCategories"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="expandAll"
        stripe
        style="width: 100%"
      >
        <el-table-column label="分类名称" min-width="200">
          <template #default="{ row }">
            <div class="category-name">
              <el-icon v-if="row.icon" class="category-icon">
                <component :is="row.icon" />
              </el-icon>
              <span class="name-text">{{ row.name }}</span>
              <el-tag v-if="row.isDefault" type="success" size="small">
                默认
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        
        <el-table-column label="排序" width="100" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="0"
              :max="9999"
              size="small"
              controls-position="right"
              @change="updateCategorySort(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="文章数量" width="100" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="viewCategoryArticles(row.id)">
              {{ row.articleCount || 0 }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="updateCategoryStatus(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
                <el-button
                  v-if="hasPermission('article:category:add')"
                  type="text"
                  size="small"
                  :icon="Plus"
                  @click="createSubCategory(row)"
                >
                添加子分类
              </el-button>
              
                <el-button
                  v-if="hasPermission('article:category:edit')"
                  type="text"
                  size="small"
                  :icon="Edit"
                  @click="editCategory(row)"
                >
                编辑
              </el-button>
              
                <el-button
                  v-if="hasPermission('article:category:remove')"
                  type="text"
                  size="small"
                  :icon="Delete"
                  @click="deleteCategory(row)"
                  :disabled="Boolean(row.isDefault || (row.articleCount && row.articleCount > 0))"
                >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑分类对话框 -->
    <el-dialog
      v-model="showCategoryDialog"
      :title="isEdit ? '编辑分类' : '创建分类'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="categoryForm.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            placeholder="请输入分类描述（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryTreeOptions"
            placeholder="选择父级分类（可选）"
            clearable
            check-strictly
            :render-after-expand="false"
            node-key="id"
            :props="{
              label: 'name',
              children: 'children'
            }"
          />
        </el-form-item>
        
        <el-form-item label="分类图标" prop="icon">
          <div class="icon-selector">
            <el-input
              v-model="categoryForm.icon"
              placeholder="选择图标"
              readonly
            >
              <template #prepend>
                <el-icon v-if="categoryForm.icon">
                  <component :is="categoryForm.icon" />
                </el-icon>
                <el-icon v-else><Picture /></el-icon>
              </template>
              <template #append>
                <el-button @click="showIconSelector = true">
                  选择
                </el-button>
              </template>
            </el-input>
          </div>
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="categoryForm.sort"
            :min="0"
            :max="9999"
            placeholder="排序值，数字越小越靠前"
          />
        </el-form-item>
        
        <el-form-item label="SEO设置">
          <el-collapse>
            <el-collapse-item title="SEO优化" name="seo">
              <el-form-item label="SEO标题" prop="seoTitle">
                <el-input
                  v-model="categoryForm.seoTitle"
                  placeholder="SEO标题（可选）"
                  maxlength="60"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="SEO描述" prop="seoDescription">
                <el-input
                  v-model="categoryForm.seoDescription"
                  type="textarea"
                  placeholder="SEO描述（可选）"
                  :rows="2"
                  maxlength="160"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="SEO关键词" prop="seoKeywords">
                <el-input
                  v-model="categoryForm.seoKeywords"
                  placeholder="SEO关键词，用逗号分隔（可选）"
                />
              </el-form-item>
            </el-collapse-item>
          </el-collapse>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch
            v-model="categoryForm.isActive"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item v-if="!isEdit" label="设为默认">
          <el-switch
            v-model="categoryForm.isDefault"
            active-text="是"
            inactive-text="否"
          />
          <div class="form-tip">
            默认分类用于未分类的文章，每个租户只能有一个默认分类
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCategoryDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategoryForm" :loading="saving">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      v-model="showIconSelector"
      title="选择图标"
      width="800px"
    >
      <div class="icon-grid">
        <div
          v-for="icon in availableIcons"
          :key="icon"
          class="icon-item"
          :class="{ selected: categoryForm.icon === icon }"
          @click="selectIcon(icon)"
        >
          <el-icon><component :is="icon" /></el-icon>
          <span class="icon-name">{{ icon }}</span>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showIconSelector = false">取消</el-button>
        <el-button type="primary" @click="confirmIconSelection">
          确认选择
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Sort,
  Edit,
  Delete,
  Picture,
  Folder,
  Document,
  Star,
  Setting,
  User,
  House,
  Monitor,
  Phone,
  Message,
  Location,
  Timer,
  Calendar,
  Bell,
  Flag,
  Trophy,
  Gift,
  Camera,
  VideoCamera,
  Headset,
  Microphone
} from '@element-plus/icons-vue'
import { categoryApi } from '@/api'
import type { Category, CategoryCreateData, CategoryUpdateData } from '@/types'
import { usePermission } from '@/composables/usePermission'

const router = useRouter()
const route = useRoute()
const { hasPermission } = usePermission()

const canCreateCategory = computed(() => hasPermission('article:category:add'))
const canEditCategory = computed(() => hasPermission('article:category:edit'))
const canDeleteCategory = computed(() => hasPermission('article:category:remove'))

// 响应式数据
const categories = ref<Category[]>([])
const searchKeyword = ref('')
const loading = ref(false)
const expandAll = ref(false)
const showCategoryDialog = ref(false)
const showIconSelector = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const categoryFormRef = ref<FormInstance>()
const categoryForm = ref({
  id: '',
  name: '',
  description: '',
  parentId: '',
  icon: '',
  sort: 0,
  seoTitle: '',
  seoDescription: '',
  seoKeywords: '',
  isActive: true,
  isDefault: false
})

const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 可用图标列表
const availableIcons = [
  'Folder', 'Document', 'Star', 'Setting', 'User', 'House',
  'Monitor', 'Phone', 'Message', 'Location', 'Timer', 'Calendar',
  'Bell', 'Flag', 'Trophy', 'Gift', 'Camera', 'VideoCamera',
  'Headset', 'Microphone', 'Picture'
]

// 计算属性
const filteredCategories = computed(() => {
  if (!searchKeyword.value) {
    return categories.value
  }
  
  const filterTree = (items: Category[]): Category[] => {
    return items.filter(item => {
      const matchesKeyword = item.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
      const hasMatchingChildren = item.children && filterTree(item.children).length > 0
      
      if (hasMatchingChildren) {
        item.children = filterTree(item.children!)
      }
      
      return matchesKeyword || hasMatchingChildren
    })
  }
  
  return filterTree(categories.value)
})

const categoryTreeOptions = computed(() => {
  const buildTree = (items: Category[], excludeId?: string): any[] => {
    if (!Array.isArray(items)) {
      return []
    }
    
    return items
      .filter(item => {
        // 确保item存在且有必要的字段
        if (!item || (!item.id && !item.categoryId)) {
          return false
        }
        const itemId = item.id || item.categoryId
        return itemId !== excludeId
      })
      .map(item => {
        const itemId = item.id || item.categoryId || ''
        const itemName = item.name || item.categoryName || ''
        
        return {
          id: itemId,
          name: itemName,
          children: item.children ? buildTree(item.children, excludeId) : []
        }
      })
  }
  
  try {
    return buildTree(categories.value || [], isEdit.value ? categoryForm.value.id : undefined)
  } catch (error) {
    console.error('构建分类树选项失败:', error)
    return []
  }
})

// 搜索防抖
let searchTimer: NodeJS.Timeout | null = null

const TRUE_VALUES = new Set<string>(['1', 'true', 'yes', 'y', 'on', 'enabled', 'active', '正常', '启用'])
const FALSE_VALUES = new Set<string>(['0', 'false', 'no', 'n', 'off', 'disabled', 'inactive', '停用', '禁用'])

const normalizeBooleanFlag = (value: unknown): boolean | undefined => {
  if (value === null || value === undefined) {
    return undefined
  }
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    if (Number.isNaN(value)) {
      return undefined
    }
    return value !== 0
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (!normalized) {
      return undefined
    }
    if (TRUE_VALUES.has(normalized)) {
      return true
    }
    if (FALSE_VALUES.has(normalized)) {
      return false
    }
  }
  return undefined
}

const normalizeStatusValue = (value: unknown): '0' | '1' | undefined => {
  if (value === null || value === undefined || value === '') {
    return undefined
  }
  if (typeof value === 'number') {
    return value === 0 ? '0' : '1'
  }
  const normalized = String(value).trim()
  if (!normalized) {
    return undefined
  }
  const lower = normalized.toLowerCase()
  if (normalized === '0' || TRUE_VALUES.has(lower)) {
    return '0'
  }
  if (normalized === '1' || FALSE_VALUES.has(lower)) {
    return '1'
  }
  return undefined
}

// 方法
const loadCategories = async () => {
  try {
    loading.value = true
    const response = await categoryApi.getAdminCategories()
    
    const processCategories = (items: any[]): Category[] => {
      if (!Array.isArray(items)) {
        return []
      }
      
      return items.map(item => {
        const itemRecord = item as Record<string, unknown>
        const normalizedStatus =
          normalizeStatusValue(item.status) ??
          normalizeStatusValue(itemRecord.state) ??
          normalizeStatusValue(itemRecord.enabledStatus)

        const enabledFlag =
          normalizeBooleanFlag(item.enabled) ??
          normalizeBooleanFlag(item.isActive) ??
          normalizeBooleanFlag(itemRecord.active) ??
          normalizeBooleanFlag(item.isVisible) ??
          (normalizedStatus ? normalizedStatus === '0' : undefined) ??
          true

        const visibleFlag =
          normalizeBooleanFlag(item.visible) ??
          normalizeBooleanFlag(item.isVisible) ??
          enabledFlag

        const resolvedStatus = normalizedStatus ?? (enabledFlag ? '0' : '1')
        
        return {
          id: item.categoryId || item.id || '',
          name: item.categoryName || item.name || '',
          description: item.description || '',
          parentId: item.parentId || '',
          icon: item.icon || '',
          sort: item.orderNum || item.sort || 0,
          isVisible: visibleFlag,
          isActive: enabledFlag,
          articleCount: item.articleCount || 0,
          createdAt: item.createTime || item.createdAt || '',
          updatedAt: item.updateTime || item.updatedAt || '',
          children: item.children ? processCategories(item.children) : [],
          tenantId: item.tenantId || '',
          categoryId: item.categoryId || item.id || '',
          categoryName: item.categoryName || item.name || '',
          orderNum: item.orderNum || item.sort || 0,
          status: resolvedStatus,
          enabled: enabledFlag,
          visible: visibleFlag
        }
      })
    }
    
    const data = response.items || response.rows || response.list || response.records || response.data || response || []
    categories.value = processCategories(data)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
    categories.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(() => {
    // 搜索逻辑已在计算属性中处理
  }, 300)
}

const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
}

const createCategory = () => {
  if (!hasPermission('article:category:add')) {
    ElMessage.warning('暂无创建分类的权限')
    return
  }
  resetCategoryForm()
  isEdit.value = false
  showCategoryDialog.value = true
}

const createSubCategory = (parent: Category) => {
  if (!hasPermission('article:category:add')) {
    ElMessage.warning('暂无创建分类的权限')
    return
  }
  resetCategoryForm()
  categoryForm.value.parentId = parent.id
  isEdit.value = false
  showCategoryDialog.value = true
}

const editCategory = (category: Category) => {
  if (!hasPermission('article:category:edit')) {
    ElMessage.warning('暂无编辑分类的权限')
    return
  }
  categoryForm.value = {
    id: category.id,
    name: category.name,
    description: category.description || '',
    parentId: category.parentId || '',
    icon: category.icon || '',
    sort: category.sort || 0,
    seoTitle: category.seoTitle || '',
    seoDescription: category.seoDescription || '',
    seoKeywords: category.seoKeywords || '',
    isActive: category.isActive !== false,
    isDefault: category.isDefault || false
  }
  
  isEdit.value = true
  showCategoryDialog.value = true
}

const resetCategoryForm = () => {
  categoryForm.value = {
    id: '',
    name: '',
    description: '',
    parentId: '',
    icon: '',
    sort: 0,
    seoTitle: '',
    seoDescription: '',
    seoKeywords: '',
    isActive: true,
    isDefault: false
  }
  
  nextTick(() => {
    categoryFormRef.value?.clearValidate()
  })
}

const saveCategoryForm = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    saving.value = true
    
    // 转换数据格式以匹配后端期望的CategoryAdminRequest
    const data = {
      name: categoryForm.value.name,
      description: categoryForm.value.description,
      parentId: categoryForm.value.parentId ? parseInt(categoryForm.value.parentId) : null,
      icon: categoryForm.value.icon,
      sort: categoryForm.value.sort,
      enabled: categoryForm.value.isActive
    }
    
    if (isEdit.value) {
      if (!hasPermission('article:category:edit')) {
        ElMessage.warning('暂无编辑分类的权限')
        return
      }
      // 使用updateCategory方法，传递ID作为单独参数
      await categoryApi.updateCategory(categoryForm.value.id, data as CategoryUpdateData)
      ElMessage.success('分类更新成功')
    } else {
      if (!hasPermission('article:category:add')) {
        ElMessage.warning('暂无创建分类的权限')
        return
      }
      await categoryApi.createCategory(data as CategoryCreateData)
      ElMessage.success('分类创建成功')
    }
    
    showCategoryDialog.value = false
    loadCategories()
  } catch (error) {
    if (error !== 'validation') {
      console.error('保存分类失败:', error)
      ElMessage.error('保存分类失败')
    }
  } finally {
    saving.value = false
  }
}

const deleteCategory = async (category: Category) => {
  if (!hasPermission('article:category:remove')) {
    ElMessage.warning('暂无删除分类的权限')
    return
  }
  if (category.isDefault) {
    ElMessage.warning('默认分类不能删除')
    return
  }
  
  if (category.articleCount && category.articleCount > 0) {
    ElMessage.warning('该分类下还有文章，不能删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${category.name}」吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await categoryApi.deleteCategory(category.id)
    ElMessage.success('分类删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

const updateCategorySort = async (category: Category) => {
  try {
    await categoryApi.updateCategory(category.id, { sort: category.sort })
    ElMessage.success('排序更新成功')
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
    loadCategories() // 重新加载以恢复原值
  }
}

const updateCategoryStatus = async (category: Category) => {
  const previousState = category.isActive
  const previousStatus = category.status
  try {
    await categoryApi.updateCategory(category.id, { enabled: category.isActive })
    category.status = category.isActive ? '0' : '1'
    category.enabled = category.isActive
    category.visible = category.isActive
    category.isVisible = category.isActive
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
    category.isActive = previousState
    category.enabled = previousState
    category.visible = previousState
    category.isVisible = previousState
    category.status = previousStatus
  }
}

const viewCategoryArticles = (categoryId: string) => {
  router.push({
    path: '/admin/articles',
    query: { categoryId }
  })
}

const selectIcon = (icon: string) => {
  categoryForm.value.icon = icon
}

const confirmIconSelection = () => {
  showIconSelector.value = false
}

const formatDate = (date: string | Date) => {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  // 只在当前路由匹配时才加载数据，避免组件被预加载时发出不必要的请求
  if (route.path.startsWith('/admin/categories')) {
    loadCategories()
  }
})

// 监听路由变化，当路由匹配时加载数据
watch(
  () => route.fullPath, // 使用fullPath而不是path，确保查询参数变化也会触发
  (newPath, oldPath) => {
    // 只有当路由真正发生变化且新路由匹配时才重新加载
    if (newPath !== oldPath && newPath.startsWith('/admin/categories')) {
      // 强制刷新数据
      categories.value = []
      nextTick(() => {
        loadCategories()
      })
    }
  }
  // 移除 immediate: true，避免与onMounted重复加载
)
</script>

<style scoped>
.category-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  color: #909399;
  font-size: 14px;
}

.toolbar-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  border-radius: 8px;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  color: #409eff;
}

.name-text {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.icon-selector {
  width: 100%;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.icon-item.selected {
  border-color: #409eff;
  background: #409eff;
  color: #fff;
}

.icon-item .el-icon {
  font-size: 24px;
}

.icon-name {
  font-size: 12px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .category-management {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}
</style>
