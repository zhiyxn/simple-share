<template>
  <div 
    class="article-card"
    :style="{ '--delay': delay + 's' }"
    @click="$emit('click', article)"
  >
    <!-- 文章封面 -->
    <div v-if="article.coverImage" class="article-cover">
      <div class="cover-image">
        <img :src="getCorrectCoverUrl(article.coverImage)" :alt="article.title" />
        <div class="cover-overlay">
          <el-icon class="cover-icon"><View /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 文章主要信息 -->
    <div class="article-main">
      <div class="article-header">
        <div class="article-badges">
          <span v-if="isVip" class="vip-badge">
            <el-icon><Trophy /></el-icon>
            VIP
          </span>
          <span class="article-category">{{ article.category?.name || '未分类' }}</span>
        </div>
        <div class="article-date">{{ formattedDate }}</div>
      </div>

      <h3 class="article-title" v-html="highlightedTitle"></h3>
      
      <p class="article-excerpt" v-html="highlightedExcerpt"></p>
      
      <div class="article-footer">
        <div class="article-tags">
          <span
            v-for="tag in displayTags"
            :key="tag"
            class="tag"
          >
            #{{ tag }}
          </span>
        </div>
        
        <div class="article-stats">
          <div class="stat-item">
            <el-icon><View /></el-icon>
            <span>{{ formatNumber(article.viewCount) }}</span>
          </div>
          <div class="stat-item">
            <el-icon><Star /></el-icon>
            <span>{{ formatNumber((article as any).likeCount ?? 0) }}</span>
          </div>
          <div class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ formatNumber((article as any).commentCount ?? 0) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { View, Star, Trophy, ChatDotRound } from '@element-plus/icons-vue'
import { buildFileUrl } from '@/utils/file'

interface ArticleCardArticle {
  id: number | string
  title: string
  summary?: string
  content?: string
  coverImage?: string
  category?: { name?: string }
  viewCount?: number
  publishedAt?: string
  [key: string]: any
}

interface Props {
  article: ArticleCardArticle
  delay?: number
  highlightKeyword?: string
}

const props = withDefaults(defineProps<Props>(), {
  delay: 0,
  highlightKeyword: ''
})

defineEmits<{
  click: [article: ArticleCardArticle]
}>()

// 封面图片预览URL（支持动态域名拼接）
const coverImageUrl = ref('')

// 构建封面图片URL
const buildCoverImageUrl = async () => {
  if (!props.article.coverImage) {
    coverImageUrl.value = ''
    return
  }

  if (isAbsoluteUrl(props.article.coverImage)) {
    coverImageUrl.value = props.article.coverImage
    return
  }

  try {
    coverImageUrl.value = await buildFileUrl(props.article.coverImage)
  } catch (error) {
    console.warn('ArticleCard - 构建文章封面图片URL失败:', error)
    coverImageUrl.value = props.article.coverImage
  }
}

const isAbsoluteUrl = (url: string) => /^(?:https?:)?\/\//i.test(url) || url.startsWith('data:')

const normalizeUploadPath = (path: string) => {
  if (!path) return ''
  let processed = path.startsWith('./') ? path.slice(1) : path

  const swap = (target: string, replacement: string) => {
    if (processed.startsWith(target)) {
      processed = processed.replace(target, replacement)
    }
  }

  swap('/api/upload/upload/', '/upload/')
  swap('api/upload/upload/', '/upload/')
  swap('/api/upload/', '/upload/')
  swap('api/upload/', '/upload/')

  if (processed.startsWith('upload/')) {
    processed = `/${processed}`
  }

  return processed
}

// 同步版本的封面URL处理函数
const getCorrectCoverUrl = (coverImage?: string): string => {
  if (!coverImage) return ''

  if (isAbsoluteUrl(coverImage)) {
    return coverImage
  }

  const processedPath = normalizeUploadPath(coverImage)
  const domain = import.meta.env.VITE_FILE_BASE_URL?.replace(/\/$/, '')
  if (domain) {
    const normalized = processedPath.startsWith('/') ? processedPath.slice(1) : processedPath
    return `${domain}/${normalized}`
  }

  return processedPath
}

// 监听封面图片变化
watch(() => props.article.coverImage, async () => {
  await buildCoverImageUrl()
}, { immediate: true })

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const stripHtml = (value?: string) => {
  if (!value) return ''
  return value.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim()
}

const highlightPattern = computed(() => {
  const keyword = (props.highlightKeyword ?? '').trim()
  if (!keyword) {
    return null
  }
  const tokens = keyword
    .split(/\s+/)
    .filter(Boolean)
    .map((part) => part.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
  if (!tokens.length) {
    return null
  }
  return new RegExp(`(${tokens.join('|')})`, 'gi')
})

const applyHighlight = (text: string) => {
  const safe = escapeHtml(text)
  const pattern = highlightPattern.value
  if (!pattern) {
    return safe
  }
  return safe.replace(pattern, '<mark class="keyword-highlight">$1</mark>')
}

const isVip = computed(() => {
  return Number((props.article as any).accessLevel ?? 0) === 1
})

const contentPreview = computed(() => {
  if (!props.article.content) return ''
  return props.article.content.replace(/<[^>]*>/g, '').substring(0, 100)
})

const rawExcerpt = computed(() => {
  return props.article.summary || contentPreview.value || '这篇文章暂未提供摘要...'
})

const highlightedTitle = computed(() => {
  const title = stripHtml(props.article.title ?? '')
  return applyHighlight(title || '未命名文章')
})

const highlightedExcerpt = computed(() => {
  return applyHighlight(stripHtml(rawExcerpt.value))
})

const displayTags = computed(() => {
  const tags = (props.article as any).tags
  return Array.isArray(tags) ? tags.slice(0, 3) : []
})

const formattedDate = computed(() => {
  const dateString = (props.article as any).publishedAt ?? 
                    (props.article as any).publishTime ?? 
                    (props.article as any).createTime
  
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
})

const formatNumber = (value: number | string | undefined | null) => {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num) || num <= 0) return '0'
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}
</script>

<style scoped>
.article-card {
  display: flex;
  align-items: center;
  height: 150px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 12px;
  border: 1px solid rgba(139, 92, 246, 0.1);
  backdrop-filter: blur(20px);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 20px 40px rgba(139, 92, 246, 0.15),
    0 0 0 1px rgba(139, 92, 246, 0.2);
  border-color: rgba(139, 92, 246, 0.3);
}

.article-cover {
  order: 2;
  flex-shrink: 0;
  width: 160px;
  height: 100%;
  margin-left: 16px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  position: relative;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cover-image:hover .cover-overlay {
  opacity: 1;
}

.cover-icon {
  color: white;
  font-size: 24px;
}

.article-main {
  order: 1;
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.article-badges {
  display: flex;
  gap: 8px;
  align-items: center;
}

.vip-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.article-category {
  background: rgba(139, 92, 246, 0.1);
  color: #7c3aed;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.article-date {
  color: #64748b;
  font-size: 12px;
}

.article-title {
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 8px;
}

.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
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
  color: #64748b;
  font-size: 12px;
}

.stat-item .el-icon {
  font-size: 14px;
}

.keyword-highlight {
  background: rgba(251, 191, 36, 0.35);
  color: #92400e;
  border-radius: 4px;
  padding: 0 2px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设定 */
@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
    align-items: stretch;
    height: auto;
    padding: 16px;
  }
  
  .article-cover {
    order: 0;
    width: 100%;
    height: 200px;
    margin-left: 0;
    margin-bottom: 16px;
  }
  
  .article-main {
    height: auto;
  }
  
  .article-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .article-stats {
    justify-content: center;
  }
}
</style>
