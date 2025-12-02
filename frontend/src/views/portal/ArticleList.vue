<template>
  <div class="modern-article-page min-h-screen">
    <!-- 动态背景装饰 -->
    <div class="background-decoration">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>
    </div>

    <!-- 科技感搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">
            <span class="title-gradient">探索</span>
            <span class="title-text">知识海洋</span>
          </h1>
          <p class="page-subtitle">发现优质内容，启发无限可能</p>
        </div>

        <div class="search-box">
          <div class="search-input-wrapper">
            <el-icon class="search-icon">
              <Search />
            </el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索文章、探索知识..."
              class="search-input"
              @keyup.enter="handleSearchSubmit"
            />
            <div class="search-line"></div>
            <div class="search-glow"></div>
          </div>
          <button class="search-btn" @click="handleSearchSubmit">
            <span>搜索</span>
            <el-icon class="search-btn-icon"><ArrowRight /></el-icon>
          </button>
        </div>
        
        <!-- 筛选选项 -->
        <div class="filter-options">
          <div class="filter-group">
            <el-select
              v-model="filterState.categoryId"
              placeholder="分类"
              clearable
              class="modern-select"
              @change="handleCategoryChange"
            >
              <el-option label="全部分类" value="" />
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>

            <el-select
              v-model="filterState.sort"
              placeholder="排序"
              class="modern-select"
              @change="handleSortChange"
            >
              <el-option
                v-for="option in sortOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>

            <el-button
              v-if="hasActiveFilters"
              text
              class="reset-btn"
              @click="handleClearFilters"
            >
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </div>

      </div>
    </div>

    <!-- 文章列表 -->
    <div class="articles-container">
      <!-- 统计信息 -->
      <div class="stats-bar">
        <div class="stats-info">
          <span class="stats-text">
            找到 <span class="stats-number">{{ totalVisible }}</span> 篇文章
          </span>
          <span class="stats-update">最近更新：{{ latestUpdateLabel }}</span>
          <span v-if="showSearchMetrics" class="search-metric">搜索耗时：{{ searchMetrics.took }}ms</span>
        </div>
        <div class="view-toggle">
          <el-button-group>
            <el-button 
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
              class="view-btn"
            >
              <el-icon><List /></el-icon>
            </el-button>
            <el-button 
              :type="viewMode === 'grid' ? 'primary' : 'default'"
              @click="viewMode = 'grid'"
              class="view-btn"
            >
              <el-icon><Grid /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>

      <div class="articles-list" v-loading="loading">
        <!-- 列表视图 -->
        <transition-group name="fade-up" tag="div" v-if="viewMode === 'list'" class="articles-list-view">
          <ArticleCard
            v-for="(article, index) in paginatedArticles"
            :key="article.id"
            :article="article"
            :delay="index * 0.1"
            :highlight-keyword="highlightKeyword"
            @click="handleArticleClick"
          />
        </transition-group>

        <!-- 网格视图 -->
        <transition-group name="fade-up" tag="div" class="articles-grid-view" v-else>
          <ArticleGridCard
            v-for="(article, index) in paginatedArticles"
            :key="article.id"
            :article="article"
            :delay="index * 0.1"
            :highlight-keyword="highlightKeyword"
            @click="handleArticleClick"
          />
        </transition-group>
        
        <div v-if="!loading && paginatedArticles.length === 0" class="empty-state">
          <div class="empty-animation">
            <div class="empty-icon">🔍</div>
            <div class="empty-particles">
              <span class="particle"></span>
              <span class="particle"></span>
              <span class="particle"></span>
            </div>
          </div>
          <h3>没有找到相关文章</h3>
          <p>试试调整搜索条件或筛选选项</p>
          <el-button type="primary" @click="handleClearFilters">
            <el-icon><RefreshLeft /></el-icon>
            重置筛选
          </el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalVisible > pagination.size" class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="totalVisible"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, View, ArrowRight, RefreshLeft, List, Grid, Trophy, ChatDotRound } from '@element-plus/icons-vue'
import { articleApi } from '@/api/article'
import { publicCategoryApi } from '@/api/category'
import type { ArticleSearchResult } from '@/types/article'
import type { Category } from '@/types/category'
import { normalizeCategoryList } from '@/utils/category'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import ArticleCard from '@/components/ArticleCard.vue'
import ArticleGridCard from '@/components/ArticleGridCard.vue'

interface PortalArticle {
  id: string
  title: string
  summary?: string
  content?: string
  coverImage?: string
  category?: {
    id?: string
    name?: string
  }
  tags?: string[]
  publishedAt?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  accessLevel?: number
  raw?: any
  highlight?: ArticleSearchResult['highlight']
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const articles = ref<PortalArticle[]>([])
const categories = ref<Category[]>([])
const searchKeyword = ref('')
const viewMode = ref<'list' | 'grid'>('list')
const totalCount = ref(0)
const searchMetrics = reactive({
  took: 0
})
const isSearchRoute = computed(() => route.name === 'SearchResults')
const trimmedKeyword = computed(() => searchKeyword.value.trim())
const useServerSearch = computed(() => isSearchRoute.value && trimmedKeyword.value.length > 0)
const highlightKeyword = computed(() => trimmedKeyword.value)
const showSearchMetrics = computed(() => useServerSearch.value && searchMetrics.took > 0)
let fetchRequestId = 0

const filterState = reactive({
  categoryId: '',
  tag: '',
  sort: 'publishedAt-desc'
})

const pagination = reactive({
  page: 1,
  size: 10
})

const sortOptions = [
  { label: '最新发布', value: 'publishedAt-desc' },
  { label: '最早发布', value: 'publishedAt-asc' },
  { label: '最多阅读', value: 'viewCount-desc' },
  { label: '最多点赞', value: 'likeCount-desc' },
  { label: '讨论最多', value: 'commentCount-desc' }
]

const hasActiveFilters = computed(
  () => Boolean(filterState.categoryId || filterState.tag || trimmedKeyword.value)
)
const resolveCategoryArray = (value: any): any[] => {
  if (Array.isArray(value)) return value
  if (Array.isArray(value?.items)) return value.items
  if (Array.isArray(value?.rows)) return value.rows
  if (Array.isArray(value?.list)) return value.list
  if (Array.isArray(value?.records)) return value.records
  if (Array.isArray(value?.data)) {
    if (Array.isArray(value.data)) return value.data
    if (Array.isArray(value.data.items)) return value.data.items
    if (Array.isArray(value.data.rows)) return value.data.rows
  }
  return []
}

const getQueryValue = (value: unknown): string | undefined => {
  if (Array.isArray(value)) {
    const [first] = value
    return typeof first === 'string' ? first : undefined
  }
  return typeof value === 'string' ? value : undefined
}

const buildSearchQuery = (keywordValue?: string) => {
  const query: Record<string, string> = {}
  const keyword = (keywordValue ?? trimmedKeyword.value).trim()
  if (keyword) {
    query.keyword = keyword
  }
  if (filterState.categoryId) {
    query.category = filterState.categoryId
  }
  if (filterState.tag) {
    query.tag = filterState.tag
  }
  return query
}

const mapSearchResultToArticle = (item: ArticleSearchResult): PortalArticle => {
  return {
    id: String(item.id),
    title: item.title ?? '',
    summary: item.summary ?? '',
    content: '',
    coverImage: item.coverImage,
    category: item.categoryName ? { name: item.categoryName } : undefined,
    tags: Array.isArray(item.tags) ? item.tags : [],
    publishedAt: item.publishedAt,
    viewCount: Number((item as any).viewCount ?? 0),
    likeCount: Number((item as any).likeCount ?? 0),
    commentCount: Number((item as any).commentCount ?? 0),
    accessLevel: (item as any).accessLevel != null ? Number((item as any).accessLevel) : undefined,
    raw: item,
    highlight: item.highlight
  }
}

const resolveSort = (value: string) => {
  const [field, direction] = value.split('-')
  let column = 'publish_time'
  switch (field) {
    case 'viewCount':
      column = 'view_count'
      break
    case 'likeCount':
      column = 'like_count'
      break
    case 'commentCount':
      column = 'comment_count'
      break
    case 'publishedAt':
    default:
      column = 'publish_time'
  }
  return { column, direction: direction === 'asc' ? 'asc' : 'desc', field }
}

const fetchCategories = async () => {
  try {
    const response = await publicCategoryApi.getCategories()
    const list = resolveCategoryArray(response)
    categories.value = normalizeCategoryList(list)
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    categories.value = []
  }
}

const fetchArticles = async () => {
  const requestId = ++fetchRequestId
  try {
    loading.value = true
    if (useServerSearch.value) {
      articles.value = []
      totalCount.value = 0
      searchMetrics.took = 0
    }
    const { column, direction } = resolveSort(filterState.sort)

    if (useServerSearch.value) {
      const params: Record<string, any> = {
        pageNum: pagination.page,
        pageSize: pagination.size,
        categoryId: filterState.categoryId || undefined,
        tag: filterState.tag || undefined,
        tags: filterState.tag || undefined,
        orderByColumn: column,
        isAsc: direction,
        highlight: true,
        includeContent: true
      }

      const response = await articleApi.search.searchArticles(trimmedKeyword.value, params)
      if (requestId !== fetchRequestId) {
        return
      }
      const results = response?.results ?? []
      articles.value = results.map(mapSearchResultToArticle)
      totalCount.value = Number(response?.total ?? results.length)
      searchMetrics.took = Number(response?.took ?? 0)
      return
    }

    const params = {
      pageNum: 1,
      pageSize: 200,
      title: trimmedKeyword.value || undefined,
      categoryId: filterState.categoryId || undefined,
      tag: filterState.tag || undefined,
      tags: filterState.tag || undefined,
      orderByColumn: column,
      isAsc: direction,
      status: 'published'
    }

    const data: any = await articleApi.getArticles(params)
    if (requestId !== fetchRequestId) {
      return
    }
    const items = data?.items ?? data?.rows ?? data?.list ?? []
    articles.value = (Array.isArray(items) ? items : []) as PortalArticle[]
    totalCount.value = articles.value.length
    searchMetrics.took = 0
  } catch (error) {
    if (requestId !== fetchRequestId) {
      return
    }
    console.error('Failed to fetch articles:', error)
    articles.value = []
    totalCount.value = 0
    searchMetrics.took = 0
  } finally {
    if (requestId === fetchRequestId) {
      loading.value = false
    }
  }
}

const getArticleDate = (article: PortalArticle): string => {
  const raw =
    (article as any).publishedAt ??
    (article as any).publishTime ??
    (article as any).createdAt ??
    (article as any).createTime ??
    ''
  return typeof raw === 'string' ? raw : ''
}

const getContentPreview = (content: string): string => {
  if (!content) return ''
  // 移除HTML标签
  const textContent = content.replace(/<[^>]*>/g, '')
  // 移除多余的空白字符
  const cleanText = textContent.replace(/\s+/g, ' ').trim()
  // 返回前200个字符作为预览
  return cleanText.length > 200 ? cleanText.substring(0, 200) + '...' : cleanText
}

const getArticleTimestamp = (article: PortalArticle): number => {
  const time = new Date(getArticleDate(article)).getTime()
  return Number.isFinite(time) ? time : 0
}

const getSortValue = (article: PortalArticle, field: string): number => {
  switch (field) {
    case 'viewCount':
      return Number((article as any).viewCount ?? 0)
    case 'likeCount':
      return Number((article as any).likeCount ?? 0)
    case 'commentCount':
      return Number((article as any).commentCount ?? 0)
    case 'publishedAt':
    default:
      return getArticleTimestamp(article)
  }
}


const getAccessLevel = (article: PortalArticle): number => {
  if (!article) {
    return 0
  }

  const directLevel = (article as any).accessLevel
  if (typeof directLevel === 'number' && Number.isFinite(directLevel)) {
    return directLevel
  }

  const fallbackLevel =
    (article as any).access_level ??
    (article as any).vipLevel ??
    (article as any).raw?.accessLevel ??
    (article as any).raw?.access_level ??
    (article as any).raw?.vipLevel

  const parsed = Number(fallbackLevel)
  return Number.isFinite(parsed) ? parsed : 0
}

const isVipArticle = (article: PortalArticle): boolean => getAccessLevel(article) > 0


const filteredArticles = computed(() => {
  const keyword = trimmedKeyword.value.toLowerCase()
  const { field, direction } = resolveSort(filterState.sort)
  const multiplier = direction === 'asc' ? 1 : -1

  let result = [...articles.value]

  if (!useServerSearch.value && filterState.tag) {
    const target = filterState.tag.toLowerCase()
    result = result.filter((article) => {
      const tags = (article as any).tags
      return Array.isArray(tags) && tags.some((tag: unknown) => String(tag).toLowerCase() === target)
    })
  }

  if (!useServerSearch.value && keyword) {
    result = result.filter((article) => {
      const title = (article.title ?? '').toLowerCase()
      const summary = (article.summary ?? '').toLowerCase()
      const preview = getContentPreview(article.content).toLowerCase()
      return title.includes(keyword) || summary.includes(keyword) || preview.includes(keyword)
    })
  }

  result.sort((a, b) => {
    const valueA = getSortValue(a, field)
    const valueB = getSortValue(b, field)
    return (valueA - valueB) * multiplier
  })

  return result
})

const totalVisible = computed(() => {
  if (useServerSearch.value) {
    return totalCount.value
  }
  return filteredArticles.value.length
})

const paginatedArticles = computed(() => {
  if (useServerSearch.value) {
    return filteredArticles.value
  }
  const start = (pagination.page - 1) * pagination.size
  return filteredArticles.value.slice(start, start + pagination.size)
})

const latestUpdateLabel = computed(() => {
  if (!articles.value.length) {
    return '暂无数据'
  }
  const timestamps = articles.value.map(getArticleTimestamp).filter((value) => value > 0)
  if (!timestamps.length) {
    return '暂无数据'
  }
  return formatDate(new Date(Math.max(...timestamps)).toISOString())
})

const handleSearchSubmit = () => {
  const keyword = trimmedKeyword.value
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  pagination.page = 1
  router.push({
    name: 'SearchResults',
    query: buildSearchQuery(keyword)
  })
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  pagination.page = 1
  if (isSearchRoute.value) {
    router.push({ name: 'ArticleList' })
  } else {
    fetchArticles()
  }
}

const handleCategoryChange = () => {
  pagination.page = 1
  if (useServerSearch.value) {
    router.push({
      name: 'SearchResults',
      query: buildSearchQuery()
    })
    return
  }
  fetchArticles()
}

const handleSortChange = () => {
  pagination.page = 1
  fetchArticles()
}

const handleClearFilters = () => {
  searchKeyword.value = ''
  filterState.categoryId = ''
  filterState.tag = ''
  filterState.sort = sortOptions[0].value
  pagination.page = 1
  if (isSearchRoute.value) {
    router.push({ name: 'ArticleList' })
  } else {
    fetchArticles()
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  if (useServerSearch.value) {
    fetchArticles()
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handlePageSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  if (useServerSearch.value) {
    fetchArticles()
  }
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '日期未知'
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return '日期未知'

  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

const formatNumber = (value: number | string | undefined | null) => {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num) || num <= 0) return '0'
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}



const handleArticleClick = async (article: PortalArticle) => {
  if (isVipArticle(article)) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('该文章为会员专享，请先登录再访问')
      router.push({ path: '/auth/login', query: { redirect: route.fullPath } })
      return
    }

    try {
      await userStore.fetchUserInfo()
    } catch (error: any) {
      if (error?.response?.status === 401) {
        ElMessage.warning('登录状态已失效，请重新登录后访问会员文章')
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } })
      } else {
        ElMessage.error('无法获取用户信息，请稍后再试')
      }
      return
    }

    if (!userStore.isVipMember || !userStore.vipActive) {
      ElMessage.warning('该文章仅对会员开放，请前往个人中心开通或续费会员')
      router.push('/profile')
      return
    }
  }

  router.push({ name: 'ArticleReader', params: { id: article.id } })
}

watch(filteredArticles, (list) => {
  if (useServerSearch.value) {
    return
  }
  const maxPage = Math.max(1, Math.ceil(list.length / pagination.size) || 1)
  if (pagination.page > maxPage) {
    pagination.page = maxPage
  }
})

watch(
  () => pagination.size,
  () => {
    pagination.page = 1
    if (useServerSearch.value) {
      fetchArticles()
    }
  }
)

const syncFiltersFromRoute = (query: Record<string, any>) => {
  const keyword = getQueryValue(query.keyword)
  searchKeyword.value = keyword ?? ''
  const category = getQueryValue(query.category)
  filterState.categoryId = category ?? ''
  const tag = getQueryValue(query.tag)
  filterState.tag = tag ?? ''
}

watch(
  () => ({ name: route.name, query: { ...route.query } }),
  ({ query }) => {
    syncFiltersFromRoute(query)
    pagination.page = 1
    fetchArticles()
  }
)

onMounted(async () => {
  syncFiltersFromRoute(route.query)
  await Promise.all([fetchCategories(), fetchArticles()])
})
</script>

<style scoped>
/* 现代文章页面 */
.modern-article-page {
  position: relative;
  overflow-x: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 50%, #cbd5e1 100%);
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 背景装饰 */
.background-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(59, 130, 246, 0.1));
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  animation-delay: -5s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: -10s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  top: 30%;
  right: 30%;
  animation-delay: -15s;
}

.shape-5 {
  width: 250px;
  height: 250px;
  bottom: 10%;
  right: 10%;
  animation-delay: -7s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(90deg); }
  50% { transform: translateY(-40px) rotate(180deg); }
  75% { transform: translateY(-20px) rotate(270deg); }
}

/* 搜索区域 */
.search-section {
  padding: 30px 24px 25px;
  position: relative;
  z-index: 10;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.7), rgba(168, 85, 247, 0.6));
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(139, 92, 246, 0.3);
}

.search-container {
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

/* 页面标题 */
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin: 0 0 16px 0;
  line-height: 1.1;
}

.title-gradient {
  background: linear-gradient(135deg, #8b5cf6, #06b6d4, #10b981);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient-shift 3s ease-in-out infinite;
}

.title-text {
  color: #1e293b;
  margin-left: 0.5rem;
}

.page-subtitle {
  font-size: 1.2rem;
  color: #475569;
  font-weight: 400;
  margin: 0;
  opacity: 0.9;
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.search-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #8b5cf6;
  font-size: 16px;
  z-index: 2;
}

.search-input {
  width: 100%;
  height: 48px;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(139, 92, 246, 0.4);
  border-radius: 24px;
  padding: 0 60px 0 48px;
  color: #1e293b;
  font-size: 15px;
  font-weight: 400;
  outline: none;
  transition: all 0.3s ease;
  backdrop-filter: blur(20px);
  box-shadow: 
    0 6px 24px rgba(139, 92, 246, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.search-input:focus {
  border-color: #8b5cf6;
  box-shadow: 
    0 0 0 4px rgba(139, 92, 246, 0.1),
    0 8px 32px rgba(139, 92, 246, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.search-input-wrapper::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(59, 130, 246, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.search-input:focus + .search-input-wrapper::after {
  opacity: 1;
}

.search-input::placeholder {
  color: #94a3b8;
  font-weight: 400;
}

.search-line {
  position: absolute;
  bottom: 8px; /* 调整位置 */
  left: 48px;
  right: 18px;
  height: 2px; /* 减小高度 */
  background: linear-gradient(90deg, #a78bfa, #06b6d4, #8b5cf6);
  border-radius: 1px;
  transform: scaleX(0);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-input:focus + .search-line {
  transform: scaleX(1);
}

.search-btn {
  height: 56px;
  padding: 0 32px;
  background: linear-gradient(135deg, #8b5cf6 0%, #06b6d4 100%);
  border: none;
  border-radius: 28px;
  color: white;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 
    0 4px 15px rgba(102, 126, 234, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 25px rgba(102, 126, 234, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.search-btn:active {
  transform: translateY(-1px);
}

/* 筛选选项 - 优化高度 */
.filter-options {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px; /* 减小间距 */
  margin-bottom: 20px; /* 减小底部间距 */
  flex-wrap: wrap;
}

.modern-select {
  width: 140px; /* 减小宽度 */
}

:deep(.modern-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
  box-shadow: 
    0 2px 8px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

:deep(.modern-select .el-input__wrapper:hover) {
  border-color: rgba(102, 126, 234, 0.4);
  background: rgba(255, 255, 255, 0.95);
}

:deep(.modern-select .el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 
    0 0 15px rgba(102, 126, 234, 0.2),
    0 4px 12px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

:deep(.modern-select .el-input__inner) {
  color: #1e293b;
  font-weight: 500;
}

:deep(.modern-select .el-input__inner::placeholder) {
  color: #64748b;
}

.reset-btn {
  color: #64748b;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 8px 16px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  font-size: 14px;
}

.reset-btn:hover {
  color: #667eea;
  border-color: rgba(102, 126, 234, 0.4);
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
}

/* 热门标签 */
.popular-tags {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px; /* 减小间距 */
  flex-wrap: wrap;
}

.tags-label {
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tags-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-item {
  padding: 8px 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 20px;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 2px 8px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.tag-item:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(102, 126, 234, 0.4);
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 
    0 4px 12px rgba(102, 126, 234, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.tag-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 
    0 4px 15px rgba(102, 126, 234, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 文章容器 */
.articles-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 60px;
  position: relative;
  z-index: 5;
}

/* 统计信息栏 */
.stats-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.05);
}

.stats-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stats-text {
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

.stats-number {
  color: #667eea;
  font-weight: 600;
}

.stats-update {
  color: #64748b;
  font-size: 13px;
}

.search-metric {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

.view-toggle {
  display: flex;
  align-items: center;
}

.view-btn {
  border: 1px solid rgba(102, 126, 234, 0.2);
  background: rgba(255, 255, 255, 0.8);
  color: #64748b;
  transition: all 0.3s ease;
}

.view-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.articles-list {
  margin-bottom: 60px;
}

/* 文章行 */
.article-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: 
    0 2px 12px rgba(102, 126, 234, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.article-row::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(102, 126, 234, 0.05) 0%, 
    rgba(118, 75, 162, 0.03) 50%, 
    rgba(102, 126, 234, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.article-row:hover::before {
  opacity: 1;
}

.article-row:hover {
  transform: translateY(-2px);
  border-color: rgba(102, 126, 234, 0.2);
  box-shadow: 
    0 8px 25px rgba(102, 126, 234, 0.15),
    0 0 20px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
}

.article-main {
  flex: 1;
  min-width: 0;
}

.article-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px; /* 减小间距 */
  margin-bottom: 8px; /* 减小底部间距 */
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.3;
  margin: 0;
  letter-spacing: -0.025em;
  transition: color 0.3s ease;
}

.article-row:hover .article-title {
  color: #667eea;
}

.article-badges {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.vip-badge {
  padding: 2px 8px; /* 减小内边距 */
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: white;
  font-size: 10px; /* 减小字体 */
  font-weight: 600; /* 减小字重 */
  border-radius: 8px; /* 减小圆角 */
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 
    0 2px 8px rgba(245, 158, 11, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.article-category {
  padding: 2px 8px; /* 减小内边距 */
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
  font-size: 10px; /* 减小字体 */
  font-weight: 500; /* 减小字重 */
  border-radius: 8px; /* 减小圆角 */
  border: 1px solid rgba(167, 139, 250, 0.3);
  backdrop-filter: blur(10px);
}

.article-excerpt {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px 0;
  font-weight: 400;
  letter-spacing: 0.01em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 4px 10px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 6px;
  color: #667eea;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.tag:hover {
  background: rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
  color: #5a67d8;
  transform: translateY(-1px);
}

.article-date {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.article-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 500;
}

.stat-item .el-icon {
  font-size: 14px;
  opacity: 0.8;
  color: #667eea;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: rgba(248, 250, 252, 0.6);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 600;
  color: rgba(248, 250, 252, 0.8);
  margin: 0 0 12px 0;
}

.empty-state p {
  font-size: 16px;
  color: rgba(248, 250, 252, 0.5);
  margin: 0;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.8);
  --el-pagination-text-color: #64748b;
  --el-pagination-border-radius: 8px;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next),
:deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.2);
  color: #64748b;
  backdrop-filter: blur(10px);
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover),
:deep(.el-pagination .el-pager li:hover) {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(102, 126, 234, 0.4);
  color: #667eea;
  transform: translateY(-1px);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

/* 动画效果 */
.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-box {
    flex-direction: column;
    gap: 16px;
  }
  
  .search-input-wrapper {
    max-width: 100%;
  }
  
  .search-btn {
    width: 100%;
    justify-content: center;
  }
  
  .filter-options {
    flex-direction: column;
    gap: 12px;
  }
  
  .article-row {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 20px;
  }
  
  .article-title-row {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .article-stats {
    flex-direction: row;
    justify-content: center;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #7c3aed, #8b5cf6);
}

/* 文章列表布局 */
.articles-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  position: relative;
  z-index: 10;
}

/* 列表视图 */
.articles-list-view {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 网格视图 */
.articles-grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  align-items: stretch;
}

/* 响应式网格 */
@media (max-width: 1200px) {
  .articles-grid-view {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .articles-grid-view {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .articles-list {
    padding: 20px 16px;
  }
}

/* 视图切换动画 */
.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
