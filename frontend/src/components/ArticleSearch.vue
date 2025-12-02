<template>
  <div class="article-search">
    <!-- 搜索输入框 -->
    <div class="search-input">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文章标题、内容或标签..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClear"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #suffix>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
            :loading="searching"
          >
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 高级搜索 -->
    <el-collapse v-model="activeCollapse" class="advanced-search">
      <el-collapse-item title="高级搜索" name="advanced">
        <div class="advanced-form">
          <el-row :gutter="16">
            <!-- 分类筛选 -->
            <el-col :span="8">
              <el-form-item label="分类">
                <el-select
                  v-model="filters.categoryId"
                  placeholder="选择分类"
                  clearable
                  filterable
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 标签筛选 -->
            <el-col :span="8">
              <el-form-item label="标签">
                <el-select
                  v-model="filters.tags"
                  placeholder="选择标签"
                  multiple
                  filterable
                  allow-create
                  collapse-tags
                  collapse-tags-tooltip
                >
                  <el-option
                    v-for="tag in tags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 作者筛选 -->
            <el-col :span="8">
              <el-form-item label="作者">
                <el-input
                  v-model="filters.author"
                  placeholder="输入作者名称"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <!-- 时间范围 -->
            <el-col :span="12">
              <el-form-item label="发布时间">
                <el-date-picker
                  v-model="filters.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>

            <!-- 排序方式 -->
            <el-col :span="6">
              <el-form-item label="排序">
                <el-select v-model="filters.sortBy" placeholder="排序方式">
                  <el-option label="最新发布" value="createdAt" />
                  <el-option label="最多浏览" value="viewCount" />
                  <el-option label="最多点赞" value="likesCount" />
                  <el-option label="最多评论" value="commentsCount" />
                  <el-option label="相关度" value="relevance" />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 排序顺序 -->
            <el-col :span="6">
              <el-form-item label="顺序">
                <el-select v-model="filters.sortOrder" placeholder="排序顺序">
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <el-button @click="resetFilters">重置</el-button>
            <el-button type="primary" @click="handleAdvancedSearch">
              应用筛选
            </el-button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 搜索结果统计 -->
    <div v-if="showStats" class="search-stats">
      <div class="stats-info">
        <span class="result-count">
          找到 <strong>{{ totalResults }}</strong> 篇相关文章
        </span>
        <span v-if="searchTime" class="search-time">
          (用时 {{ searchTime }}ms)
        </span>
      </div>
      
      <!-- 当前搜索条件 -->
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="filter-label">当前筛选：</span>
        
        <el-tag
          v-if="searchQuery"
          type="primary"
          closable
          @close="clearSearchQuery"
        >
          关键词: {{ searchQuery }}
        </el-tag>
        
        <el-tag
          v-if="filters.categoryId"
          type="success"
          closable
          @close="clearCategory"
        >
          分类: {{ getCategoryName(filters.categoryId) }}
        </el-tag>
        
        <el-tag
          v-for="tag in filters.tags"
          :key="tag"
          type="warning"
          closable
          @close="removeTag(tag)"
        >
          标签: {{ tag }}
        </el-tag>
        
        <el-tag
          v-if="filters.author"
          type="info"
          closable
          @close="clearAuthor"
        >
          作者: {{ filters.author }}
        </el-tag>
        
        <el-tag
          v-if="filters.dateRange && filters.dateRange.length === 2"
          type="danger"
          closable
          @close="clearDateRange"
        >
          时间: {{ filters.dateRange[0] }} ~ {{ filters.dateRange[1] }}
        </el-tag>
        
        <el-button
          type="text"
          size="small"
          @click="clearAllFilters"
        >
          清除所有
        </el-button>
      </div>
    </div>

    <!-- 搜索建议 -->
    <div v-if="showSuggestions && suggestions.length > 0" class="search-suggestions">
      <div class="suggestions-title">搜索建议：</div>
      <div class="suggestions-list">
        <el-tag
          v-for="suggestion in suggestions"
          :key="suggestion"
          type="info"
          effect="plain"
          class="suggestion-tag"
          @click="applySuggestion(suggestion)"
        >
          {{ suggestion }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { categoryApi } from '@/api'
import type { Category } from '@/types'

interface SearchFilters {
  categoryId?: string
  tags: string[]
  author?: string
  dateRange?: [string, string]
  sortBy: string
  sortOrder: 'asc' | 'desc'
}

interface Props {
  modelValue?: string
  showStats?: boolean
  totalResults?: number
  searchTime?: number
  suggestions?: string[]
}

interface Emits {
  'update:modelValue': [value: string]
  search: [query: string, filters: SearchFilters]
  clear: []
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  showStats: false,
  totalResults: 0,
  searchTime: 0,
  suggestions: () => []
})

const emit = defineEmits<Emits>()

// 响应式数据
const searchQuery = ref(props.modelValue)
const searching = ref(false)
const activeCollapse = ref<string[]>([])
const categories = ref<Category[]>([])
const tags = ref<string[]>([])

const filters = ref<SearchFilters>({
  tags: [],
  sortBy: 'createdAt',
  sortOrder: 'desc'
})

// 计算属性
const hasActiveFilters = computed(() => {
  return searchQuery.value ||
    filters.value.categoryId ||
    filters.value.tags.length > 0 ||
    filters.value.author ||
    (filters.value.dateRange && filters.value.dateRange.length === 2)
})

const showSuggestions = computed(() => {
  return !searchQuery.value && props.suggestions.length > 0
})

// 监听器
watch(() => props.modelValue, (newValue) => {
  searchQuery.value = newValue
})

watch(searchQuery, (newValue) => {
  emit('update:modelValue', newValue)
})

// 方法
const handleSearch = async () => {
  if (!searchQuery.value.trim() && !hasActiveFilters.value) {
    return
  }
  
  try {
    searching.value = true
    emit('search', searchQuery.value.trim(), { ...filters.value })
  } finally {
    searching.value = false
  }
}

const handleClear = () => {
  searchQuery.value = ''
  emit('clear')
}

const handleAdvancedSearch = () => {
  handleSearch()
  activeCollapse.value = []
}

const resetFilters = () => {
  filters.value = {
    tags: [],
    sortBy: 'createdAt',
    sortOrder: 'desc'
  }
}

const clearAllFilters = () => {
  searchQuery.value = ''
  resetFilters()
  emit('clear')
}

const clearSearchQuery = () => {
  searchQuery.value = ''
  handleSearch()
}

const clearCategory = () => {
  filters.value.categoryId = undefined
  handleSearch()
}

const removeTag = (tag: string) => {
  const index = filters.value.tags.indexOf(tag)
  if (index > -1) {
    filters.value.tags.splice(index, 1)
    handleSearch()
  }
}

const clearAuthor = () => {
  filters.value.author = undefined
  handleSearch()
}

const clearDateRange = () => {
  filters.value.dateRange = undefined
  handleSearch()
}

const getCategoryName = (categoryId: string) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || categoryId
}

const applySuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  handleSearch()
}

const loadCategories = async () => {
  try {
    const response = await categoryApi.getList()
    categories.value = response.data.items
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    // 这里应该调用获取热门标签的API
    // const response = await articleApi.getPopularTags()
    // tags.value = response.data
    
    // 临时数据
    tags.value = [
      'Vue.js', 'React', 'JavaScript', 'TypeScript', 'Node.js',
      'Python', 'Java', 'Go', 'Rust', 'Docker',
      '前端开发', '后端开发', '全栈开发', '移动开发', '数据库'
    ]
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.article-search {
  margin-bottom: 24px;
}

.search-input {
  margin-bottom: 16px;
}

.advanced-search {
  margin-bottom: 16px;
}

.advanced-form {
  padding: 16px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.search-stats {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.result-count {
  color: #303133;
  font-size: 14px;
}

.search-time {
  color: #909399;
  font-size: 12px;
}

.active-filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-label {
  color: #606266;
  font-size: 13px;
  margin-right: 4px;
}

.search-suggestions {
  background: #f0f9ff;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e1f5fe;
}

.suggestions-title {
  color: #0277bd;
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: 500;
}

.suggestions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-tag:hover {
  background-color: #e3f2fd;
  border-color: #2196f3;
  color: #1976d2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .advanced-form .el-col {
    margin-bottom: 16px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .active-filters {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .suggestions-list {
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .search-input :deep(.el-input__suffix) {
    position: static;
    transform: none;
    margin-top: 8px;
  }
  
  .search-input :deep(.el-input-group__append) {
    border-left: 1px solid #dcdfe6;
  }
}
</style>