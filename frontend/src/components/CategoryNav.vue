<template>
  <div class="category-nav">
    <!-- 移动端分类选择器 -->
    <div class="mobile-category-selector" v-if="isMobile">
      <el-select
        v-model="selectedCategoryId"
        placeholder="选择分类"
        size="large"
        @change="handleCategoryChange"
      >
        <el-option
          label="全部分类"
          value=""
          :class="{ 'is-active': !selectedCategoryId }"
        />
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
          :class="{ 'is-active': selectedCategoryId === category.id }"
        >
          <div class="category-option">
            <span class="category-name">{{ category.name }}</span>
            <span class="category-count">({{ category.articleCount }})</span>
          </div>
        </el-option>
      </el-select>
    </div>

    <!-- 桌面端分类导航 -->
    <div class="desktop-category-nav" v-else>
      <!-- 分类标题 -->
      <div class="nav-header">
        <h3 class="nav-title">
          <el-icon><FolderOpened /></el-icon>
          文章分类
        </h3>
        
        <el-button
          v-if="showCollapseBtn"
          type="text"
          size="small"
          @click="toggleCollapse"
        >
          <el-icon>
            <ArrowUp v-if="!collapsed" />
            <ArrowDown v-if="collapsed" />
          </el-icon>
        </el-button>
      </div>

      <!-- 分类列表 -->
      <el-collapse-transition>
        <div v-show="!collapsed" class="category-list">
          <!-- 全部分类 -->
          <div
            class="category-item all-category"
            :class="{ 'is-active': !selectedCategoryId }"
            @click="selectCategory('')"
          >
            <div class="category-content">
              <el-icon class="category-icon">
                <Grid />
              </el-icon>
              <span class="category-name">全部分类</span>
            </div>
            <span class="category-count">{{ totalArticleCount }}</span>
          </div>

          <!-- 分类项 -->
          <div
            v-for="category in displayCategories"
            :key="category.id"
            class="category-item"
            :class="{ 
              'is-active': selectedCategoryId === category.id,
              'has-children': category.children && category.children.length > 0
            }"
            @click="selectCategory(category.id)"
          >
            <div class="category-content">
              <!-- 展开/收起按钮 -->
              <el-button
                v-if="category.children && category.children.length > 0"
                type="text"
                size="small"
                class="expand-btn"
                @click.stop="toggleCategory(category.id)"
              >
                <el-icon>
                  <ArrowRight v-if="!expandedCategories.includes(category.id)" />
                  <ArrowDown v-else />
                </el-icon>
              </el-button>
              
              <!-- 分类图标 -->
              <el-icon class="category-icon" :style="{ color: category.color }">
                <component :is="getCategoryIcon(category.icon)" />
              </el-icon>
              
              <!-- 分类名称 -->
              <span class="category-name">{{ category.name }}</span>
            </div>
            
            <!-- 文章数量 -->
            <span class="category-count">{{ category.articleCount }}</span>
          </div>

          <!-- 子分类 -->
          <el-collapse-transition>
            <div
              v-for="category in displayCategories"
              :key="`children-${category.id}`"
              v-show="expandedCategories.includes(category.id)"
              class="sub-categories"
            >
              <div
                v-for="child in category.children"
                :key="child.id"
                class="category-item sub-category"
                :class="{ 'is-active': selectedCategoryId === child.id }"
                @click="selectCategory(child.id)"
              >
                <div class="category-content">
                  <el-icon class="category-icon sub-icon" :style="{ color: child.color }">
                    <component :is="getCategoryIcon(child.icon)" />
                  </el-icon>
                  <span class="category-name">{{ child.name }}</span>
                </div>
                <span class="category-count">{{ child.articleCount }}</span>
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </el-collapse-transition>

      <!-- 展开更多 -->
      <div v-if="hasMoreCategories" class="show-more">
        <el-button
          type="text"
          size="small"
          @click="toggleShowAll"
        >
          {{ showAll ? '收起' : `展开更多 (${hiddenCategoriesCount})` }}
          <el-icon>
            <ArrowUp v-if="showAll" />
            <ArrowDown v-else />
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- 分类管理按钮 (仅管理员可见) -->
    <div v-if="canManageCategories" class="category-actions">
      <el-button
        type="primary"
        size="small"
        text
        @click="$emit('manage')"
      >
        <el-icon><Setting /></el-icon>
        管理分类
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import {
  FolderOpened,
  Grid,
  ArrowUp,
  ArrowDown,
  ArrowRight,
  Setting,
  Document,
  Folder,
  Star,
  Flag,
  Trophy,
  Lightning,
  Sunny,
  Heart
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { categoryApi } from '@/api'
import type { Category } from '@/types'

interface Props {
  modelValue?: string
  showCollapseBtn?: boolean
  maxDisplayCount?: number
}

interface Emits {
  'update:modelValue': [value: string]
  change: [categoryId: string]
  manage: []
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  showCollapseBtn: true,
  maxDisplayCount: 10
})

const emit = defineEmits<Emits>()

const userStore = useUserStore()
const { width } = useWindowSize()

// 响应式数据
const categories = ref<Category[]>([])
const selectedCategoryId = ref(props.modelValue)
const collapsed = ref(false)
const expandedCategories = ref<string[]>([])
const showAll = ref(false)
const loading = ref(false)

// 计算属性
const isMobile = computed(() => width.value < 768)

const canManageCategories = computed(() => {
  return userStore.user?.role === 'admin' || userStore.user?.role === 'editor'
})

const totalArticleCount = computed(() => {
  return categories.value.reduce((total, category) => {
    return total + category.articleCount + 
      (category.children?.reduce((childTotal, child) => childTotal + child.articleCount, 0) || 0)
  }, 0)
})

const displayCategories = computed(() => {
  if (showAll.value) {
    return categories.value
  }
  return categories.value.slice(0, props.maxDisplayCount)
})

const hasMoreCategories = computed(() => {
  return categories.value.length > props.maxDisplayCount
})

const hiddenCategoriesCount = computed(() => {
  return Math.max(0, categories.value.length - props.maxDisplayCount)
})

// 监听器
watch(() => props.modelValue, (newValue) => {
  selectedCategoryId.value = newValue
})

watch(selectedCategoryId, (newValue) => {
  emit('update:modelValue', newValue)
})

// 方法
const selectCategory = (categoryId: string) => {
  selectedCategoryId.value = categoryId
  emit('change', categoryId)
}

const handleCategoryChange = (categoryId: string) => {
  selectCategory(categoryId)
}

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const toggleCategory = (categoryId: string) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

const toggleShowAll = () => {
  showAll.value = !showAll.value
}

const getCategoryIcon = (iconName?: string) => {
  const iconMap: Record<string, any> = {
    document: Document,
    folder: Folder,
    star: Star,
    flag: Flag,
    trophy: Trophy,
    lightning: Lightning,
    fire: Sunny,
    heart: Heart
  }
  
  return iconMap[iconName || 'folder'] || Folder
}

const loadCategories = async () => {
  try {
    loading.value = true
    const response = await categoryApi.getList({
      includeChildren: true,
      includeArticleCount: true
    })
    categories.value = response.data.items
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-nav {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.mobile-category-selector {
  padding: 16px;
}

.category-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.desktop-category-nav {
  /* 桌面端样式 */
}

.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.category-list {
  padding: 8px 0;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item.is-active {
  background-color: #ecf5ff;
  border-left-color: #409eff;
  color: #409eff;
}

.category-item.is-active .category-name {
  font-weight: 600;
}

.category-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.expand-btn {
  padding: 4px;
  margin-right: 4px;
}

.category-icon {
  font-size: 16px;
  color: #606266;
  flex-shrink: 0;
}

.sub-icon {
  font-size: 14px;
}

.category-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-count {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.category-item.is-active .category-count {
  background: #409eff;
  color: #fff;
}

.sub-categories {
  background: #fafbfc;
}

.sub-category {
  padding-left: 52px;
  border-left: none;
}

.sub-category.is-active {
  background-color: #e1f3d8;
  border-left: 3px solid #67c23a;
  color: #67c23a;
}

.show-more {
  padding: 8px 20px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.category-actions {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}

.all-category .category-icon {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-nav {
    margin-bottom: 16px;
  }
}

/* 选择器样式优化 */
:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-option.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

:deep(.el-option.is-active .category-count) {
  background: #409eff;
  color: #fff;
}
</style>