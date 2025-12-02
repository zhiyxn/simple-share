<template>
  <div class="my-articles-container">
    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">总文章数</div>
          </div>
        </div>
        
        <div class="stat-card published">
          <div class="stat-icon">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.published }}</div>
            <div class="stat-label">已发布</div>
          </div>
        </div>
        
        <div class="stat-card draft">
          <div class="stat-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.draft }}</div>
            <div class="stat-label">草稿</div>
          </div>
        </div>
        
        <div class="stat-card views">
          <div class="stat-icon">
            <el-icon><View /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.views }}</div>
            <div class="stat-label">总阅读量</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-section">
      <el-card class="filter-card" shadow="never">
        <div class="filter-toolbar">
          <div class="filter-left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索文章标题或内容..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <el-select
              v-model="filterStatus"
              placeholder="状态筛选"
              class="filter-select"
              clearable
              @change="handleFilter"
            >
              <el-option label="已发布" value="published">
                <div class="option-item">
                  <el-icon><Check /></el-icon>
                  <span>已发布</span>
                </div>
              </el-option>
              <el-option label="草稿" value="draft">
                <div class="option-item">
                  <el-icon><Edit /></el-icon>
                  <span>草稿</span>
                </div>
              </el-option>
              <el-option label="已下线" value="offline">
                <div class="option-item">
                  <el-icon><Close /></el-icon>
                  <span>已下线</span>
                </div>
              </el-option>
            </el-select>
            
            <el-select
              v-model="filterCategory"
              placeholder="分类筛选"
              class="filter-select"
              clearable
              @change="handleFilter"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <div class="option-item">
                  <el-icon><Folder /></el-icon>
                  <span>{{ category.name }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
          
          <div class="filter-right">
            <el-button-group class="view-toggle">
              <el-button
                :type="viewMode === 'list' ? 'primary' : 'default'"
                @click="viewMode = 'list'"
              >
                <el-icon><List /></el-icon>
                列表
              </el-button>
              <el-button
                :type="viewMode === 'grid' ? 'primary' : 'default'"
                @click="viewMode = 'grid'"
              >
                <el-icon><Grid /></el-icon>
                网格
              </el-button>
            </el-button-group>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 文章列表 -->
    <div class="articles-section">
      <el-card v-if="articles.length > 0" class="articles-card" shadow="never">
        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" class="list-view">
          <div v-for="article in articles" :key="article.id" class="article-item">
            <div class="article-cover" v-if="article.coverImage">
              <img :src="article.coverImage" :alt="article.title" />
              <div class="cover-overlay">
                <el-button type="primary" circle @click="viewArticle(article.id)">
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="article-cover-placeholder" v-else>
              <el-icon><Document /></el-icon>
            </div>
            
            <div class="article-info">
              <div class="article-header">
                <h3 class="article-title" @click="viewArticle(article.id)">{{ article.title }}</h3>
                <div class="article-status">
                  <el-tag
                    :type="getStatusType(article.status)"
                    :effect="article.status === ArticleStatus.PUBLISHED ? 'dark' : 'plain'"
                    size="default"
                  >
                    <el-icon>
                      <Check v-if="article.status === ArticleStatus.PUBLISHED" />
                      <Edit v-else-if="article.status === ArticleStatus.DRAFT" />
                      <Close v-else />
                    </el-icon>
                    {{ getStatusText(article.status) }}
                  </el-tag>
                </div>
              </div>
              
              <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
              
              <div class="article-meta">
                <div class="meta-left">
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.createdAt) }}
                  </span>
                  <span class="meta-item" v-if="article.category">
                    <el-icon><Folder /></el-icon>
                    {{ article.category.name }}
                  </span>
                </div>
                <div class="meta-right">
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }}
                  </span>
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ article.commentCount }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="article-actions">
              <el-dropdown @command="handleAction" trigger="click">
                <el-button type="text" class="action-button">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', id: article.id }">
                      <el-icon><Edit /></el-icon>
                      编辑文章
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'view', id: article.id }">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{ action: article.status === ArticleStatus.PUBLISHED ? 'offline' : 'publish', id: article.id }"
                    >
                      <el-icon><Switch /></el-icon>
                      {{ article.status === ArticleStatus.PUBLISHED ? '下线文章' : '发布文章' }}
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', id: article.id }" divided>
                      <el-icon><Delete /></el-icon>
                      删除文章
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 网格视图 -->
        <div v-else class="grid-view">
          <div class="articles-grid">
            <div
              v-for="article in articles"
              :key="article.id"
              class="article-grid-card"
              @click="viewArticle(article.id)"
            >
              <div class="grid-card-cover" v-if="article.coverImage">
                <img :src="article.coverImage" :alt="article.title" />
                <div class="cover-overlay">
                  <div class="overlay-content">
                    <el-button type="primary" circle size="large">
                      <el-icon><View /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
              <div class="grid-card-cover-placeholder" v-else>
                <el-icon><Document /></el-icon>
              </div>
              
              <div class="grid-card-content">
                <div class="grid-card-header">
                  <h4 class="grid-card-title">{{ article.title }}</h4>
                  <el-tag
                    :type="getStatusType(article.status)"
                    :effect="article.status === ArticleStatus.PUBLISHED ? 'dark' : 'plain'"
                    size="small"
                  >
                    {{ getStatusText(article.status) }}
                  </el-tag>
                </div>
                
                <p class="grid-card-summary">{{ article.summary || '暂无摘要' }}</p>
                
                <div class="grid-card-meta">
                  <div class="meta-date">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.createdAt) }}
                  </div>
                  <div class="meta-stats">
                    <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
                    <span><el-icon><ChatDotRound /></el-icon> {{ article.commentCount }}</span>
                  </div>
                </div>
              </div>
              
              <div class="grid-card-actions" @click.stop>
                <el-dropdown @command="handleAction" trigger="click">
                  <el-button type="text" size="small" class="action-button">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{ action: 'edit', id: article.id }">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'view', id: article.id }">
                        <el-icon><View /></el-icon>
                        查看
                      </el-dropdown-item>
                      <el-dropdown-item
                      :command="{ action: article.status === ArticleStatus.PUBLISHED ? 'offline' : 'publish', id: article.id }"
                      >
                        <el-icon><Switch /></el-icon>
                        {{ article.status === ArticleStatus.PUBLISHED ? '下线' : '发布' }}
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', id: article.id }" divided>
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="articles.length === 0" class="empty-state">
        <el-empty description="还没有文章">
          <template #image>
            <div class="empty-image">
              <el-icon><Document /></el-icon>
            </div>
          </template>
          <template #description>
            <p class="empty-description">开始您的创作之旅，分享您的想法和见解</p>
          </template>
          <el-button type="primary" size="large" @click="$router.push('/profile/articles/create')">
            <el-icon><EditPen /></el-icon>
            创作第一篇文章
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="articles.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  EditPen,
  Search,
  List,
  Grid,
  Calendar,
  View,
  ChatDotRound,
  Folder,
  MoreFilled,
  Edit,
  Switch,
  Delete,
  Document,
  Check,
  Close
} from '@element-plus/icons-vue'
import { articleApi } from '@/api'
import type { Article } from '@/types'
import { ArticleStatus, ArticleVisibility } from '@/types'
import { useFileConfig } from '@/composables/useFileConfig'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const filterStatus = ref('')
const filterCategory = ref('')
const viewMode = ref('list')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 文章列表
const articles = ref<Article[]>([])

// 分类列表
const categories = ref([
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '数据库' },
  { id: 4, name: '运维部署' }
])

// 文章统计信息
const stats = ref({
  total: 0,
  published: 0,
  draft: 0,
  offline: 0,
  views: 0
})

const { buildFileUrl, init: initFileConfig } = useFileConfig()
const fileConfigReady = initFileConfig().catch((error) => {
  console.warn('初始化文件配置失败:', error)
})

const resolveCoverUrl = async (value?: string | null) => {
  const raw =
    typeof value === 'string'
      ? value.trim()
      : value !== undefined && value !== null
        ? String(value).trim()
        : ''

  if (!raw) {
    return ''
  }

  if (/^https?:\/\//i.test(raw)) {
    return raw
  }

  try {
    await fileConfigReady
    const resolved = await buildFileUrl(raw)
    return resolved || raw
  } catch (error) {
    console.warn('解析封面地址失败:', error)
    return raw
  }
}

// 获取状态类型
const getStatusType = (status: ArticleStatus) => {
  const typeMap: Record<ArticleStatus, string> = {
    [ArticleStatus.PUBLISHED]: 'success',
    [ArticleStatus.DRAFT]: 'warning',
    [ArticleStatus.ARCHIVED]: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: ArticleStatus) => {
  const textMap: Record<ArticleStatus, string> = {
    [ArticleStatus.PUBLISHED]: '已发布',
    [ArticleStatus.DRAFT]: '草稿',
    [ArticleStatus.ARCHIVED]: '已下线'
  }
  return textMap[status] || '未知'
}

// 格式化日期
const formatDate = (date: string | Date) => {
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return dateObj.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

// 筛选处理
const handleFilter = () => {
  currentPage.value = 1
  loadArticles()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadArticles()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadArticles()
}

// 查看文章
const viewArticle = (id: string) => {
  // 在当前profile布局下，跳转到文章详情页面
  router.push(`/article/${id}/reader`)
}

// 操作处理
const handleAction = async (command: { action: string; id: string }) => {
  const { action, id } = command
  
  switch (action) {
    case 'edit':
      router.push(`/profile/articles/${id}/edit`)
      break
      
    case 'view':
      viewArticle(id)
      break
      
    case 'publish':
      await handlePublish(id)
      break
      
    case 'offline':
      await handleOffline(id)
      break
      
    case 'delete':
      await handleDelete(id)
      break
  }
}

// 发布文章
const handlePublish = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要发布这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await articleApi.user.publishUserArticle(id.toString())
    
    // 重新加载文章列表
    await loadArticles()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 下线文章
const handleOffline = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要下线这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await articleApi.user.offlineUserArticle(id)
    
    // 重新加载文章列表
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下线失败:', error)
      ElMessage.error('下线失败')
    }
  }
}

// 删除文章
const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？删除后无法恢复。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await articleApi.user.deleteUserArticle(id)
    
    // 重新加载文章列表
    await loadArticles()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 加载文章列表
const loadArticles = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params: any = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加搜索条件
    if (searchQuery.value) {
      params.title = searchQuery.value
    }
    
    // 添加状态筛选
    if (filterStatus.value) {
      // 转换前端状态到后端状态码
      const statusMap: Record<string, string> = {
        'published': '1',
        'draft': '0',
        'offline': '2'
      }
      params.status = statusMap[filterStatus.value]
    }
    
    // 添加分类筛选
    if (filterCategory.value) {
      params.categoryId = filterCategory.value
    }
    
    const response = await articleApi.user.getUserArticles(params)

    if (response && response.items) {
      // 转换后端数据格式到前端格式，并解析封面地址
      const mappedArticles = await Promise.all(
        response.items.map(async (article: any) => {
          const resolvedTime = article.publishTime ?? article.createTime
          const coverRaw =
            article.coverImage ??
            article.cover_image ??
            article.cover ??
            article.cover_url ??
            article.coverUrl ??
            ''

          const coverImage = await resolveCoverUrl(coverRaw)

          return {
            id: article.articleId?.toString() || '',
            title: article.title || '',
            content: article.content || '',
            summary: article.summary || '',
          coverImage,
            status: article.status as ArticleStatus,
            visibility: ArticleVisibility.PUBLIC,
            tags: article.tags || [],
            viewCount: article.viewCount || 0,
            likeCount: article.likeCount || 0,
          commentCount: article.commentCount || 0,
          allowCopy: true,
          categoryId: article.categoryId?.toString() || '',
          category: article.categoryName ? { 
            id: article.categoryId?.toString() || '', 
            name: article.categoryName, 
            slug: article.categorySlug || '' 
          } : undefined,
          author: {
            id: article.authorId?.toString() || '',
            username: article.authorUsername || '',
            nickname: article.authorNickname || '',
            avatar: article.authorAvatar || ''
          },
          tenant: {
            id: article.tenantId?.toString() || '',
            name: article.tenantName || '',
            domain: article.tenantDomain || ''
          },
          createdAt: resolvedTime || new Date().toISOString(),
          updatedAt: article.updateTime || resolvedTime || new Date().toISOString(),
          publishedAt: article.publishTime || undefined
        }
        })
      )
      articles.value = mappedArticles
      total.value = response.total || 0
    } else {
      articles.value = []
      total.value = 0
    }
    
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await articleApi.user.getUserArticleStats()
    if (response) {
      stats.value = {
        total: Number(response.total) || 0,
        published: Number(response.published) || 0,
        draft: Number(response.draft) || 0,
        offline: Number(response.offline) || 0,
        views: Number(response.views) || 0
      }
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await fileConfigReady.catch(() => undefined)
  await Promise.all([loadArticles(), loadStats()])
})
</script>

<style scoped>
.my-articles-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  flex: 1;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
  font-weight: 300;
}

.header-actions {
  margin-left: 24px;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.published {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.stat-card.draft {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-card.views {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon {
  font-size: 32px;
  opacity: 0.9;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
}

/* 筛选区域 */
.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.search-input {
  width: 320px;
}

.filter-select {
  width: 140px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-toggle {
  border-radius: 8px;
  overflow: hidden;
}

/* 文章区域 */
.articles-section {
  margin-bottom: 32px;
}

.articles-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 列表视图 */
.list-view {
  width: 100%;
}

.article-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  border-bottom: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.article-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.article-item:hover {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  transform: translateX(4px);
}

.article-cover {
  width: 140px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
  cursor: pointer;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-cover:hover img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-cover:hover .cover-overlay {
  opacity: 1;
}

.article-cover-placeholder {
  width: 140px;
  height: 100px;
  border-radius: 8px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #1976d2;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  margin: 0;
  cursor: pointer;
  transition: color 0.3s ease;
  flex: 1;
  margin-right: 16px;
}

.article-title:hover {
  color: #667eea;
}

.article-summary {
  font-size: 14px;
  color: #718096;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #94a3b8;
}

.meta-left,
.meta-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-actions {
  flex-shrink: 0;
}

.action-button {
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: #f1f5f9;
}

/* 网格视图 */
.grid-view {
  margin-top: 16px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.article-grid-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
}

.article-grid-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.grid-card-cover {
  width: 100%;
  height: 160px;
  position: relative;
  overflow: hidden;
}

.grid-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.grid-card-cover-placeholder {
  width: 100%;
  height: 160px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #1976d2;
}

.grid-card-content {
  padding: 16px;
}

.grid-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.grid-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
  flex: 1;
  margin-right: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.grid-card-summary {
  font-size: 13px;
  color: #718096;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #a0aec0;
}

.meta-date {
  font-size: 12px;
}

.meta-stats {
  display: flex;
  gap: 12px;
}

.meta-stats span {
  display: flex;
  align-items: center;
  gap: 2px;
}

.grid-card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px;
  backdrop-filter: blur(8px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.empty-image {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-description {
  font-size: 16px;
  color: #64748b;
  margin: 16px 0 24px 0;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .articles-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .my-articles-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px;
    margin-bottom: 24px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-actions {
    margin-left: 0;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .filter-left {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .search-input,
  .filter-select {
    width: 100% !important;
  }
  
  .article-item {
    flex-direction: column;
    gap: 16px;
  }
  
  .article-cover,
  .article-cover-placeholder {
    width: 100%;
    height: 180px;
  }
  
  .article-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .article-meta {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 24px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .grid-card-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
