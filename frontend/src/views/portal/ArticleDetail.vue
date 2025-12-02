<template>
  <div class="article-detail-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <p class="loading-text">正在加载文章...</p>
      </div>
    </div>

    <!-- 文章内容 -->
    <div v-else-if="article" class="article-wrapper">
      <!-- 文章头部区域 -->
      <header class="article-header">
        <div class="header-background">
          <div class="header-overlay"></div>
          <img 
            v-if="article.coverImage" 
            :src="article.coverImage" 
            :alt="article.title"
            class="header-bg-image"
          />
        </div>
        
        <div class="header-content">
          <div class="breadcrumb">
            <router-link to="/" class="breadcrumb-link">首页</router-link>
            <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
            <router-link 
              v-if="article.categoryName" 
              :to="`/category/${article.categoryId}`" 
              class="breadcrumb-link"
            >
              {{ article.categoryName }}
            </router-link>
            <el-icon v-if="article.categoryName" class="breadcrumb-separator"><ArrowRight /></el-icon>
            <span class="breadcrumb-current">{{ article.title }}</span>
          </div>
          
          <div class="article-meta">
            <!-- 分类标签 -->
            <el-tag v-if="article.categoryName" type="primary" size="large" class="category-tag">
              {{ article.categoryName }}
            </el-tag>
            
            <!-- 文章标题 -->
            <h1 class="article-title">{{ article.title }}</h1>
            
            <!-- 文章摘要 -->
            <p v-if="article.summary" class="article-summary">
              {{ article.summary }}
            </p>
            
            <!-- 作者和元信息 -->
            <div class="author-meta">
              <div class="author-info">
                <el-avatar 
                  :src="article.authorAvatar" 
                  :size="48"
                  class="author-avatar"
                >
                  {{ article.authorName?.charAt(0) }}
                </el-avatar>
                <div class="author-details">
                  <div class="author-name">{{ article.authorName }}</div>
                  <div class="publish-info">
                    <span class="publish-date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                    <span class="meta-separator">·</span>
                    <span class="read-time">{{ estimatedReadTime }} 分钟阅读</span>
                  </div>
                </div>
              </div>
              
              <div class="article-stats">
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ formatNumber(article.viewCount) }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ formatNumber(article.likeCount) }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ formatNumber(article.commentCount || 0) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 主要内容区域 -->
      <main class="main-content">
        <div class="content-container">
          <!-- 文章正文 -->
          <article class="article-content-wrapper">
            <!-- 内容保护提示 -->
            <div v-if="shouldProtect" class="protection-notice">
              <el-icon><Lock /></el-icon>
              <span>本文内容受保护，禁止复制和下载</span>
            </div>
            
            <!-- 文章内容 -->
            <div v-if="previewGateVisible" class="preview-gate">
              <div class="preview-gate__content">
                <h3>会员专享 · 解锁全文</h3>
                <p>当前仅展示部分内容，开通会员即可阅读全文。</p>
                <el-button type="primary" size="large" class="preview-gate__btn" @click="goToMembership">
                  {{ canReadFull ? '查看完整内容' : '立即开通会员' }}
                </el-button>
              </div>
            </div>
            
            <!-- 实际文章内容 -->
            <div
              v-else
              ref="contentWrapper"
              class="article-content markdown-surface"
              v-content-protect="shouldProtect"
              @content-protect-block="handleProtectBlock"
              v-html="renderedContent"
            ></div>
            
            <!-- 标签 -->
            <div v-if="article.tags?.length" class="article-tags">
              <div class="tags-title">标签</div>
              <div class="tags-list">
                <el-tag 
                  v-for="tag in article.tags" 
                  :key="tag" 
                  class="tag-item"
                  type="info"
                  effect="plain"
                  @click="searchByTag(tag)"
                >
                  # {{ tag }}
                </el-tag>
              </div>
            </div>
            
            <!-- 文章操作栏 -->
            <div class="article-actions">
              <div class="action-buttons">
                <!-- 点赞按钮 -->
                <button 
                  class="action-btn like-btn"
                  :class="{ 'liked': isLiked }"
                  @click="toggleLike"
                  :disabled="likeLoading"
                >
                  <el-icon class="action-icon">
                    <Star v-if="!isLiked" />
                    <StarFilled v-else />
                  </el-icon>
                  <span class="action-text">{{ isLiked ? '已点赞' : '点赞' }}</span>
                  <span class="action-count">{{ formatNumber(article.likeCount) }}</span>
                </button>
                
                <!-- 收藏按钮 -->
                <button 
                  class="action-btn favorite-btn"
                  :class="{ 'favorited': isFavorited }"
                  @click="toggleFavorite"
                  :disabled="favoriteLoading"
                >
                  <el-icon class="action-icon">
                    <Collection v-if="!isFavorited" />
                    <CollectionTag v-else />
                  </el-icon>
                  <span class="action-text">{{ isFavorited ? '已收藏' : '收藏' }}</span>
                </button>
                
                <!-- 分享按钮 -->
                <button class="action-btn share-btn" @click="shareArticle">
                  <el-icon class="action-icon"><Share /></el-icon>
                  <span class="action-text">分享</span>
                </button>
              </div>
              
              <!-- 阅读进度 -->
              <div class="reading-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: readingProgress + '%' }"></div>
                </div>
                <span class="progress-text">{{ Math.round(readingProgress) }}%</span>
              </div>
            </div>
          </article>
          
          <!-- 侧边栏 -->
          <aside class="article-sidebar">
            <!-- 目录 -->
            <div v-if="tableOfContents.length" class="sidebar-card toc-card">
              <h3 class="sidebar-title">目录</h3>
              <nav class="table-of-contents">
                <a 
                  v-for="item in tableOfContents" 
                  :key="item.id"
                  :href="`#${item.id}`"
                  :class="['toc-item', `toc-level-${item.level}`, { 'active': activeHeading === item.id }]"
                  @click="scrollToHeading(item.id)"
                >
                  {{ item.text }}
                </a>
              </nav>
            </div>
            
            <!-- 作者信息卡片 -->
            <div class="sidebar-card author-card">
              <div class="author-card-header">
                <el-avatar 
                  :src="article.authorAvatar" 
                  :size="60"
                  class="author-card-avatar"
                >
                  {{ article.authorName?.charAt(0) }}
                </el-avatar>
                <div class="author-card-info">
                  <h4 class="author-card-name">{{ article.authorName }}</h4>
                  <p class="author-card-bio">{{ article.authorBio || '这个作者很懒，什么都没有留下...' }}</p>
                </div>
              </div>
              <div class="author-stats">
                <div class="author-stat">
                  <span class="stat-number">{{ formatNumber(article.authorArticleCount || 0) }}</span>
                  <span class="stat-label">文章</span>
                </div>
                <div class="author-stat">
                  <span class="stat-number">{{ formatNumber(article.authorFollowerCount || 0) }}</span>
                  <span class="stat-label">粉丝</span>
                </div>
              </div>
              <el-button 
                v-if="!isOwnArticle" 
                type="primary" 
                class="follow-btn"
                :loading="followLoading"
                @click="toggleFollow"
              >
                {{ isFollowing ? '已关注' : '关注作者' }}
              </el-button>
            </div>
            
            <!-- 相关文章 -->
            <div v-if="relatedArticles.length" class="sidebar-card related-card">
              <h3 class="sidebar-title">相关文章</h3>
              <div class="related-articles">
                <article 
                  v-for="item in relatedArticles" 
                  :key="item.id"
                  class="related-article"
                  @click="navigateToArticle(item.id)"
                >
                  <img 
                    v-if="item.coverImage"
                    :src="item.coverImage" 
                    :alt="item.title"
                    class="related-image"
                  />
                  <div class="related-content">
                    <h4 class="related-title">{{ item.title }}</h4>
                    <div class="related-meta">
                      <span class="related-date">{{ formatRelativeDate(item.publishedAt || item.createdAt) }}</span>
                      <span class="related-views">{{ formatNumber(item.viewCount) }} 阅读</span>
                    </div>
                  </div>
                </article>
              </div>
            </div>
          </aside>
        </div>
      </main>
      
      <!-- 推荐文章区域 -->
      <section v-if="recommendedArticles.length" class="recommended-section">
        <div class="section-container">
          <h2 class="section-title">推荐阅读</h2>
          <div class="recommended-grid">
            <article 
              v-for="item in recommendedArticles" 
              :key="item.id"
              class="recommended-card"
              @click="navigateToArticle(item.id)"
            >
              <div class="recommended-image">
                <img 
                  v-if="item.coverImage"
                  :src="item.coverImage" 
                  :alt="item.title"
                  class="card-image"
                />
                <div v-else class="card-placeholder">
                  <el-icon size="32"><Document /></el-icon>
                </div>
              </div>
              <div class="recommended-content">
                <h3 class="recommended-title">{{ item.title }}</h3>
                <p class="recommended-summary">{{ item.summary || '暂无摘要' }}</p>
                <div class="recommended-meta">
                  <span class="meta-author">{{ item.authorName }}</span>
                  <span class="meta-separator">·</span>
                  <span class="meta-date">{{ formatRelativeDate(item.publishedAt || item.createdAt) }}</span>
                  <span class="meta-separator">·</span>
                  <span class="meta-views">{{ formatNumber(item.viewCount) }} 阅读</span>
                </div>
              </div>
            </article>
          </div>
        </div>
      </section>
    </div>
    
    <!-- 文章不存在 -->
    <div v-else class="not-found-container">
      <div class="not-found-content">
        <div class="not-found-icon">
          <el-icon size="80"><DocumentDelete /></el-icon>
        </div>
        <h2 class="not-found-title">文章不存在</h2>
        <p class="not-found-description">您访问的文章可能已被删除或不存在</p>
        <div class="not-found-actions">
          <el-button type="primary" @click="$router.push('/')">
            返回首页
          </el-button>
          <el-button @click="$router.go(-1)">
            返回上页
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 回到顶部按钮 -->
    <transition name="fade">
      <button 
        v-show="showBackToTop" 
        class="back-to-top"
        @click="scrollToTop"
      >
        <el-icon><ArrowUp /></el-icon>
      </button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Calendar, 
  View, 
  Star, 
  StarFilled,
  Share, 
  Lock, 
  DocumentDelete,
  ArrowRight,
  ArrowUp,
  ChatDotRound,
  Collection,
  CollectionTag,
  Document
} from '@element-plus/icons-vue'
import { articleApi, type Article } from '@/api/article'
import { shouldProtectContent } from '@/directives/content-protect'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { createMarkdownRenderer } from '@/composables/useMarkdown'
import { isMarkdownContent } from '@/components/editor/utils/markdown'
import { SITE_TITLE } from '@/config/site'
import '@/styles/markdown.css'

interface TableOfContentsItem {
  id: string
  text: string
  level: number
}

const route = useRoute()
const router = useRouter()
const tenantStore = useTenantStore()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const article = ref<Article | null>(null)
const recommendedArticles = ref<Article[]>([])
const relatedArticles = ref<Article[]>([])
const isLiked = ref(false)
const isFavorited = ref(false)
const isFollowing = ref(false)
const likeLoading = ref(false)
const favoriteLoading = ref(false)
const followLoading = ref(false)
const readingProgress = ref(0)
const showBackToTop = ref(false)
const activeHeading = ref('')
const tableOfContents = ref<TableOfContentsItem[]>([])
const contentWrapper = ref<HTMLElement | null>(null)

// 计算属性
const canReadFull = computed(() => article.value?.fullReadable === true)
const previewOnly = computed(() => article.value?.previewOnly === true)
const visibleContent = computed(() => {
  if (!article.value) return ''
  if (previewOnly.value && article.value.previewContent) {
    return article.value.previewContent
  }
  if (article.value.content) {
    return article.value.content
  }
  return article.value.summary || ''
})

const previewGateVisible = computed(() => previewOnly.value && !canReadFull.value)

const shouldProtect = computed(() => {
  if (!article.value) return false
  return shouldProtectContent(article.value.allowCopy)
})

const enhanceContent = async () => {
  await nextTick()
  if (contentWrapper.value) {
    await markdownRenderer.enhance(contentWrapper.value, {
      protectCopy: shouldProtect.value
    })
  }
}

const markdownRenderer = createMarkdownRenderer()
const HTML_SNIPPET_REGEX = /<\/?[a-z][^>]*>/i

const renderedContent = computed(() => {
  const source = visibleContent.value
  if (!source) return ''
  const trimmed = source.trim()
  if (!HTML_SNIPPET_REGEX.test(trimmed) || isMarkdownContent(trimmed)) {
    return markdownRenderer.parse(source)
  }
  return source
})

const estimatedReadTime = computed(() => {
  if (!article.value?.content) return 0
  const wordsPerMinute = 200
  const wordCount = article.value.content.length
  return Math.ceil(wordCount / wordsPerMinute)
})

const isOwnArticle = computed(() => {
  return userStore.user?.id === article.value?.authorId
})

// 方法
const fetchArticle = async () => {
  try {
    loading.value = true
    const id = route.params.id as string
    
    // 获取文章详情
    const data = await articleApi.getArticle(id)
    article.value = data
    
    // 增加浏览量
    await articleApi.view(id)
    
    // 获取推荐文章
    const recommended = await articleApi.getRecommended(id)
    recommendedArticles.value = recommended
    
    // 获取相关文章
    const related = await articleApi.getRelated(id)
    relatedArticles.value = related.slice(0, 5)
    
    // 检查用户状态
    if (userStore.isLoggedIn) {
      checkUserStatus()
    }
    
    // 更新页面标题
    document.title = `${data.title} - ${tenantStore.tenantInfo?.name || SITE_TITLE}`
    
    await enhanceContent()
    generateTableOfContents()
    
  } catch (error) {
    console.error('Failed to fetch article:', error)
    article.value = null
  } finally {
    loading.value = false
  }
}

const goToMembership = () => {
  if (userStore.isLoggedIn) {
    router.push('/membership')
  } else {
    router.push('/login')
  }
}

const checkUserStatus = async () => {
  if (!article.value || !userStore.isLoggedIn) return
  
  try {
    // 检查点赞状态
    const likeStatus = await articleApi.checkLikeStatus(article.value.id)
    isLiked.value = likeStatus.isLiked
    
    // 检查收藏状态
    const favoriteStatus = await articleApi.checkFavoriteStatus(article.value.id)
    isFavorited.value = favoriteStatus.isFavorited
    
    // 检查关注状态
    if (article.value.authorId !== userStore.user?.id) {
      const followStatus = await articleApi.checkFollowStatus(article.value.authorId)
      isFollowing.value = followStatus.isFollowing
    }
  } catch (error) {
    console.error('Failed to check user status:', error)
  }
}

const generateTableOfContents = () => {
  nextTick(() => {
    const container = contentWrapper.value
    if (!container) {
      tableOfContents.value = []
      return
    }

    const headings = Array.from(
      container.querySelectorAll<HTMLElement>('h1, h2, h3, h4')
    )

    const toc: TableOfContentsItem[] = headings.map((heading, index) => {
      const id = `heading-${index}`
      heading.id = id
      return {
        id,
        text: heading.textContent?.trim() || '',
        level: Number.parseInt(heading.tagName.charAt(1), 10) || 2
      }
    })

    tableOfContents.value = toc
  })
}

const toggleLike = async () => {
  if (!article.value || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    likeLoading.value = true
    
    if (isLiked.value) {
      await articleApi.unlike(article.value.id)
      article.value.likeCount--
      isLiked.value = false
      ElMessage.success('取消点赞')
    } else {
      await articleApi.like(article.value.id)
      article.value.likeCount++
      isLiked.value = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('Failed to toggle like:', error)
    ElMessage.error('操作失败')
  } finally {
    likeLoading.value = false
  }
}

const toggleFavorite = async () => {
  if (!article.value || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    favoriteLoading.value = true
    
    if (isFavorited.value) {
      await articleApi.unfavorite(article.value.id)
      isFavorited.value = false
      ElMessage.success('取消收藏')
    } else {
      await articleApi.favorite(article.value.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

watch(renderedContent, () => {
  enhanceContent()
  generateTableOfContents()
})

watch(() => shouldProtect.value, () => {
  enhanceContent()
})

const toggleFollow = async () => {
  if (!article.value || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    followLoading.value = true
    
    if (isFollowing.value) {
      await articleApi.unfollow(article.value.authorId)
      isFollowing.value = false
      ElMessage.success('取消关注')
    } else {
      await articleApi.follow(article.value.authorId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('Failed to toggle follow:', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const shareArticle = async () => {
  if (!article.value) return

  const shareData = {
    title: article.value.title,
    text: article.value.summary || article.value.title,
    url: window.location.href
  }
  
  try {
    if (navigator.share) {
      await navigator.share(shareData)
    } else {
      await navigator.clipboard.writeText(window.location.href)
      ElMessage.success('链接已复制到剪贴板')
    }
  } catch (error) {
    console.error('Failed to share:', error)
    ElMessage.error('分享失败')
  }
}

const searchByTag = (tag: string) => {
  router.push(`/search?tag=${encodeURIComponent(tag)}`)
}

const BLOCK_MESSAGE_INTERVAL = 1500
let lastProtectNoticeAt = 0

const handleProtectBlock = (event: CustomEvent<{ type?: string }>) => {
  if (!shouldProtect.value) return

  const now = Date.now()
  if (now - lastProtectNoticeAt < BLOCK_MESSAGE_INTERVAL) return
  lastProtectNoticeAt = now

  const type = event.detail?.type ?? ''
  const messageMap: Record<string, string> = {
    contextmenu: '该文章已开启版权保护，禁止右键操作',
    select: '该文章已开启版权保护，禁止选择文本内容',
    copy: '该文章已开启版权保护，禁止复制内容',
    cut: '该文章已开启版权保护，禁止剪切内容',
    shortcut: '该文章已开启版权保护，禁止复制相关快捷键',
    paste: '该文章已开启版权保护，禁止粘贴内容',
    print: '该文章已开启版权保护，禁止打印内容',
    drag: '该文章已开启版权保护，禁止拖拽内容'
  }

  const message = messageMap[type] || '该文章内容受保护，无法执行此操作'
  ElMessage.warning(message)
}

const navigateToArticle = (id: string) => {
  router.push(`/article/${id}/reader`)
}

const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight - window.innerHeight
  
  // 计算阅读进度
  readingProgress.value = (scrollTop / scrollHeight) * 100
  
  // 显示/隐藏回到顶部按钮
  showBackToTop.value = scrollTop > 300
  
  // 更新活跃的标题
  const headings = document.querySelectorAll('.article-content h1, .article-content h2, .article-content h3, .article-content h4')
  let currentHeading = ''
  
  headings.forEach((heading) => {
    const rect = heading.getBoundingClientRect()
    if (rect.top <= 100) {
      currentHeading = heading.id
    }
  })
  
  activeHeading.value = currentHeading
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatRelativeDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

const formatNumber = (num: number) => {
  if (num < 1000) return num.toString()
  if (num < 10000) return (num / 1000).toFixed(1) + 'k'
  return (num / 10000).toFixed(1) + 'w'
}

// 监听路由变化
watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchArticle()
  }
})

// 生命周期
onMounted(async () => {
  await fetchArticle()
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
  
  // 滚动到顶部
  nextTick(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.article-detail-container {
  min-height: 100vh;
  background: #fafafa;
}

/* 加载状态 */
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.loading-spinner {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #6b7280;
  font-size: 0.9rem;
}

/* 文章头部 */
.article-header {
  position: relative;
  height: 60vh;
  min-height: 400px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.header-bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.3) 0%,
    rgba(0, 0, 0, 0.5) 100%
  );
}

.header-content {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  color: white;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
  font-size: 0.9rem;
}

.breadcrumb-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-link:hover {
  color: white;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.8rem;
}

.breadcrumb-current {
  color: white;
  font-weight: 600;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.article-meta {
  max-width: 800px;
}

.category-tag {
  margin-bottom: 1rem;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.article-title {
  font-size: 2.5rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 0.8rem;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(135deg, #ffffff 0%, rgba(255, 255, 255, 0.9) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.article-summary {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  opacity: 0.95;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.1);
  padding: 1rem;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.author-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 1rem;
  border-radius: 16px;
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.author-avatar {
  border: 3px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.author-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.publish-info {
  font-size: 0.9rem;
  opacity: 0.9;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.meta-separator {
  margin: 0 0.5rem;
}

.article-stats {
  display: flex;
  gap: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  opacity: 0.95;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 0.8rem;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.article-content {
  padding: 3rem;
  font-size: 16px;
  line-height: 1.65;
  color: #0f172a;
  letter-spacing: 0.01em;
  word-break: break-word;
}

.article-content > :first-child {
  margin-top: 0 !important;
}

.article-content > :last-child {
  margin-bottom: 0 !important;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4) {
  color: #0f172a;
  font-weight: 700;
  margin: 1.2rem 0 0.5rem;
  line-height: 1.3;
}

.article-content :deep(h1) {
  font-size: 2rem;
}

.article-content :deep(h2) {
  font-size: 1.6rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.3em;
}

.article-content :deep(h3) {
  font-size: 1.25rem;
}

.article-content :deep(h4) {
  font-size: 1.25rem;
}

.article-content :deep(p) {
  margin: 0.6em 0;
}

.article-content :deep(p:empty) {
  display: none;
  margin: 0;
}

@supports selector(:has(*)) {
  .article-content :deep(p:has(> br:only-child)) {
    display: none;
  }
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 16px;
  margin: 1.2rem 0;
  box-shadow: 0 14px 36px -32px rgba(15, 23, 42, 0.55);
}

.article-content :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  padding: 0.9rem 1.4rem;
  margin: 1rem 0;
  background: #eff6ff;
  border-radius: 0 14px 14px 0;
  color: #1e3a8a;
}

.article-content :deep(code) {
  background: #f1f5f9;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  font-size: 0.9em;
}

.article-content :deep(pre) {
  background: #f8fafc;
  color: #0f172a;
  padding: 1.1rem 1.4rem;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  overflow-x: auto;
  margin: 1.2rem 0;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  font-size: 0.92em;
}

.article-content :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.article-tags {
  padding: 2rem 3rem;
  border-top: 1px solid #e5e7eb;
}

.tags-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-actions {
  padding: 2rem 3rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 50px;
  background: white;
  color: #6b7280;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  border-color: #10b981;
  color: #10b981;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.action-btn.liked,
.action-btn.favorited {
  border-color: #10b981;
  color: #10b981;
  background: #f0fdf4;
}

.action-icon {
  font-size: 1.1rem;
}

.action-count {
  font-size: 0.9rem;
  opacity: 0.8;
}

.reading-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-bar {
  width: 100px;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #10b981;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.9rem;
  color: #6b7280;
  min-width: 40px;
}

/* 侧边栏 */
.article-sidebar {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.sidebar-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  position: sticky;
  top: 2rem;
}

.sidebar-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.98);
}

.sidebar-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.toc-card {
  padding: 1.5rem;
}

.table-of-contents {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.toc-item {
  display: block;
  padding: 0.5rem 0;
  color: #6b7280;
  text-decoration: none;
  font-size: 0.9rem;
  line-height: 1.4;
  border-left: 2px solid transparent;
  padding-left: 1rem;
  transition: all 0.3s ease;
  border-radius: 0 8px 8px 0;
}

.toc-item:hover {
  color: #6366f1;
  border-left-color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
  transform: translateX(4px);
}

.toc-item.active {
  color: #6366f1;
  border-left-color: #6366f1;
  font-weight: 600;
  background: rgba(99, 102, 241, 0.1);
  transform: translateX(4px);
}

.toc-level-2 { padding-left: 1.5rem; }
.toc-level-3 { padding-left: 2rem; }
.toc-level-4 { padding-left: 2.5rem; }

.author-card {
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%);
}

.author-card-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.author-card-avatar {
  flex-shrink: 0;
  border: 3px solid rgba(99, 102, 241, 0.2);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.author-card-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #1f2937 0%, #6366f1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.author-card-bio {
  font-size: 0.9rem;
  color: #6b7280;
  line-height: 1.5;
  margin: 0;
}

.author-stats {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.author-stat {
  text-align: center;
  flex: 1;
}

.stat-number {
  display: block;
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 0.8rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.follow-btn {
  width: 100%;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.related-card {
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%);
}

.related-articles {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.related-article {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.related-article:hover {
  background: rgba(99, 102, 241, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.2);
}

.related-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  border: 2px solid rgba(99, 102, 241, 0.1);
}

.related-content {
  flex: 1;
  min-width: 0;
}

.related-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  font-size: 0.8rem;
  color: #6b7280;
}

.related-date,
.related-views {
  margin-right: 0.5rem;
}

/* 推荐文章 */
.recommended-section {
  background: white;
  padding: 4rem 0;
  margin-top: 4rem;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin-bottom: 3rem;
}

.recommended-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.recommended-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommended-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.recommended-image {
  height: 200px;
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.recommended-card:hover .card-image {
  transform: scale(1.05);
}

.card-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #9ca3af;
}

.recommended-content {
  padding: 1.5rem;
}

.recommended-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  margin-bottom: 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommended-summary {
  font-size: 0.9rem;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommended-meta {
  font-size: 0.8rem;
  color: #9ca3af;
}

.meta-author {
  font-weight: 500;
}

.meta-separator {
  margin: 0 0.5rem;
}

/* 404页面 */
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
}

.not-found-content {
  text-align: center;
  max-width: 400px;
}

.not-found-icon {
  color: #9ca3af;
  margin-bottom: 2rem;
}

.not-found-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.not-found-description {
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.not-found-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

/* 回到顶部按钮 */
.back-to-top {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 48px;
  height: 48px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  transition: all 0.3s ease;
  z-index: 1000;
}

.back-to-top:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .article-sidebar {
    order: -1;
  }
  
  .toc-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .article-header {
    height: 50vh;
    min-height: 300px;
  }
  
  .header-content {
    padding: 1.5rem;
  }
  
  .article-title {
    font-size: 2rem;
  }
  
  .article-summary {
    font-size: 1.1rem;
  }
  
  .author-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .main-content {
    padding: 1rem;
    margin-top: -2rem;
  }
  
  .article-content {
    padding: 2rem 1.5rem;
    font-size: 1rem;
  }
  
  .article-tags,
  .article-actions {
    padding: 1.5rem;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
  
  .reading-progress {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .progress-bar {
    width: 100%;
  }
  
  .recommended-grid {
    grid-template-columns: 1fr;
  }
  
  .section-container {
    padding: 0 1rem;
  }
}

@media (max-width: 480px) {
  .breadcrumb {
    font-size: 0.8rem;
  }
  
  .breadcrumb-current {
    max-width: 150px;
  }
  
  .article-title {
    font-size: 1.75rem;
  }
  
  .article-summary {
    font-size: 1rem;
  }
  
  .article-content {
    padding: 1.5rem 1rem;
  }
  
  .article-tags,
  .article-actions {
    padding: 1rem;
  }
  
  .action-btn {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
  
  .back-to-top {
    bottom: 1rem;
    right: 1rem;
    width: 40px;
    height: 40px;
  }
}
.preview-gate {
  margin-top: 24px;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px dashed rgba(79, 70, 229, 0.25);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(79, 70, 229, 0.08));
}

.preview-gate__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.preview-gate__content h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d4ed8;
}

.preview-gate__content p {
  margin: 0;
  font-size: 14px;
  color: #1e293b;
  opacity: 0.8;
}

.preview-gate__btn {
  margin-top: 4px;
  border-radius: 10px;
}

</style>
