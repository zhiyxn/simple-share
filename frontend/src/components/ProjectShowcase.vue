<template>
  <div class="project-showcase">
    <!-- 项目网格布局 -->
    <div class="projects-grid">
      <div
        v-for="project in projects"
        :key="project.id"
        class="project-card"
        @click="openProject(project)"
      >
        <!-- 项目封面 -->
        <div class="project-cover">
          <div v-if="project.coverImage" class="cover-image">
            <img
              :src="getCoverImageUrl(project.coverImage)"
              :alt="project.title"
              class="cover-img"
            />
            <div class="cover-overlay">
              <div class="view-project-btn">
                <el-icon><View /></el-icon>
                <span>查看项目</span>
              </div>
            </div>
          </div>
          <div v-else class="cover-placeholder">
            <div class="placeholder-icon">📁</div>
            <div class="placeholder-text">暂无封面</div>
          </div>

          <!-- 项目标签 -->
          <div class="project-badges">
            <span v-if="project.isTop" class="project-badge project-badge--top">
              <el-icon><Star /></el-icon>
              置顶
            </span>
            <span v-if="project.isRecommend" class="project-badge project-badge--recommend">
              <el-icon><Trophy /></el-icon>
              推荐
            </span>
            <span v-if="isVipProject(project)" class="project-badge project-badge--vip">
              <el-icon><Medal /></el-icon>
              {{ getAccessLevelText(project) }}
            </span>
          </div>
        </div>

        <!-- 项目信息 -->
        <div class="project-info">
          <div class="project-header">
            <h3 class="project-title">{{ project.title }}</h3>
            <div class="project-meta">
              <span class="category-tag">{{ project.categoryName || '项目' }}</span>
              <span class="author-info">
                <el-icon><User /></el-icon>
                {{ project.authorName || '匿名作者' }}
              </span>
            </div>
          </div>

          <p class="project-description">
            {{ project.summary || '这个项目暂无简介，点击查看详细信息。' }}
          </p>

          <div class="project-stats">
            <div class="stat-item">
              <el-icon class="stat-icon"><View /></el-icon>
              <span>{{ formatNumber(project.viewCount || 0) }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-item">
              <el-icon class="stat-icon"><Clock /></el-icon>
              <span>{{ formatDate(project.publishedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-card" v-for="n in 6" :key="n">
        <div class="loading-cover">
          <div class="skeleton shimmer"></div>
        </div>
        <div class="loading-info">
          <div class="skeleton skeleton-title shimmer"></div>
          <div class="skeleton skeleton-text shimmer"></div>
          <div class="skeleton skeleton-text shimmer"></div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!isLoading && projects.length === 0" class="empty-state">
      <div class="empty-icon">📂</div>
      <h3 class="empty-title">暂无项目</h3>
      <p class="empty-description">
        当前分类下还没有项目，<br>
        前往创作中心分享你的第一个项目吧！
      </p>
      <router-link to="/editor" class="create-btn">
        <el-icon><Edit /></el-icon>
        创建项目
      </router-link>
    </div>

    <!-- 无限滚动触发器 -->
    <div v-if="showInfiniteTrigger" ref="infiniteTrigger" class="infinite-trigger"></div>

    <!-- 加载更多 -->
    <div v-if="isLoadingMore" class="loading-more">
      <el-icon class="animate-spin"><Loading /></el-icon>
      <span>正在加载更多项目...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  View,
  Star,
  Trophy,
  Medal,
  User,
  Clock,
  Edit,
  Loading
} from '@element-plus/icons-vue'

interface Project {
  id: string
  title: string
  summary: string
  content: string
  coverImage?: string
  categoryName?: string
  authorName?: string
  viewCount: number
  publishedAt?: string
  accessLevel?: number
  isTop?: boolean
  isRecommend?: boolean
}

const props = defineProps<{
  projects: Project[]
  isLoading?: boolean
  isLoadingMore?: boolean
  showInfiniteTrigger?: boolean
}>()

const emit = defineEmits<{
  openProject: [project: Project]
  loadMore: []
}>()

const router = useRouter()
const infiniteTrigger = ref<HTMLElement | null>(null)

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

const isAbsoluteUrl = (url: string) =>
  /^(https?:)?\/\//i.test(url) || url.startsWith('data:')

const getCoverImageUrl = (coverImage?: string): string => {
  if (!coverImage) return ''

  if (isAbsoluteUrl(coverImage)) {
    return coverImage
  }

  let processedPath = coverImage
  if (coverImage.startsWith('/api/upload/upload/')) {
    processedPath = coverImage.replace('/api/upload/upload/', 'upload/')
  } else if (coverImage.startsWith('/api/upload/')) {
    processedPath = coverImage.replace('/api/upload/', 'upload/')
  }

  const domain = import.meta.env?.VITE_FILE_BASE_URL?.replace(/\/$/, '')
  if (domain) {
    const normalizedPath = processedPath.startsWith('/') ? processedPath.slice(1) : processedPath
    return `${domain}/${normalizedPath}`
  }

  return processedPath
}

const formatDate = (date?: string) => {
  if (!date) return '未知时间'
  const time = new Date(date)
  if (isNaN(time.getTime())) return '未知时间'

  const now = new Date()
  const diff = now.getTime() - time.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

const openProject = (project: Project) => {
  emit('openProject', project)
}

const isVipProject = (project: Project) => {
  return (project.accessLevel ?? 0) > 0
}

const getAccessLevelText = (project: Project) => {
  const level = project.accessLevel ?? 0
  switch (level) {
    case 1:
      return '会员专享'
    case 2:
      return 'VIP专享'
    default:
      return '会员内容'
  }
}
</script>

<style scoped>
.project-showcase {
  width: 100%;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.project-card {
  @apply bg-white/80 backdrop-blur-md rounded-2xl overflow-hidden shadow-lg border border-gray-100/50 transition-all duration-300 cursor-pointer;
  position: relative;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}

.project-card:hover .cover-overlay {
  opacity: 1;
}

.project-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.cover-image {
  position: relative;
  width: 100%;
  height: 100%;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.project-card:hover .cover-img {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.3));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.view-project-btn {
  @apply flex items-center gap-2 px-6 py-3 bg-white/90 backdrop-blur rounded-xl text-gray-900 font-semibold;
  transition: all 0.3s ease;
}

.view-project-btn:hover {
  background: white;
  transform: translateY(-2px);
}

.cover-placeholder {
  @apply flex flex-col items-center justify-center h-full bg-gradient-to-br from-gray-50 to-gray-100;
  border: 2px dashed rgba(156, 163, 175, 0.3);
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 8px;
  opacity: 0.5;
}

.placeholder-text {
  @apply text-sm text-gray-500;
}

.project-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 6px;
  z-index: 10;
}

.project-badge {
  @apply flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold backdrop-blur-sm;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.project-badge--top {
  background: rgba(251, 191, 36, 0.9);
  color: white;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.project-badge--recommend {
  background: rgba(59, 130, 246, 0.9);
  color: white;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.project-badge--vip {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: white;
  border: 1px solid rgba(245, 158, 11, 0.3);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.project-info {
  padding: 20px;
}

.project-header {
  margin-bottom: 12px;
}

.project-title {
  @apply text-lg font-bold text-gray-900 mb-2 line-clamp-2;
  line-height: 1.3;
}

.project-meta {
  @apply flex items-center gap-3 text-xs;
}

.category-tag {
  @apply px-2 py-1 rounded-full bg-blue-50 text-blue-600 font-medium;
}

.author-info {
  @apply flex items-center gap-1 text-gray-500;
}

.project-description {
  @apply text-sm text-gray-600 mb-4 line-clamp-3;
  line-height: 1.5;
}

.project-stats {
  @apply flex items-center justify-between pt-3 border-t border-gray-100;
}

.stat-item {
  @apply flex items-center gap-1 text-xs text-gray-500;
}

.stat-icon {
  @apply text-blue-500;
}

.stat-label {
  @apply hidden sm:inline ml-1;
}

/* 加载状态 */
.loading-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.loading-card {
  @apply bg-white/80 backdrop-blur-md rounded-2xl overflow-hidden shadow-lg border border-gray-100/50;
}

.loading-cover {
  height: 200px;
  position: relative;
}

.loading-info {
  padding: 20px;
}

.skeleton {
  @apply bg-gray-200 rounded-lg;
}

.skeleton-title {
  height: 20px;
  margin-bottom: 12px;
  width: 80%;
}

.skeleton-text {
  height: 14px;
  margin-bottom: 8px;
}

.skeleton-text:last-child {
  width: 60%;
}

.shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 空状态 */
.empty-state {
  @apply flex flex-col items-center justify-center py-16 text-center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  @apply text-xl font-semibold text-gray-900 mb-2;
}

.empty-description {
  @apply text-gray-600 mb-6 leading-relaxed;
}

.create-btn {
  @apply inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-xl hover:shadow-lg transition-all duration-300;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

/* 无限滚动触发器 */
.infinite-trigger {
  height: 2px;
  width: 100%;
}

/* 加载更多 */
.loading-more {
  @apply flex items-center justify-center gap-2 py-6 text-sm text-gray-500;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 640px) {
  .projects-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .project-cover {
    height: 180px;
  }

  .project-info {
    padding: 16px;
  }

  .project-title {
    font-size: 16px;
  }

  .loading-section {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .project-cover {
    height: 160px;
  }

  .project-info {
    padding: 12px;
  }

  .project-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .project-stats {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 文本截断工具类 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
