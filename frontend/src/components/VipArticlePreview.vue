<template>
  <div class="vip-article-preview bg-white/90 backdrop-blur-sm rounded-2xl shadow-lg border border-amber-200/50 overflow-hidden">
    <!-- VIP标识横幅 -->
    <div class="vip-banner bg-gradient-to-r from-amber-400 to-orange-500 text-white px-6 py-3">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <el-icon class="text-lg"><Star /></el-icon>
          <span class="font-semibold">会员专享文章</span>
        </div>
        <el-button 
          type="primary" 
          size="small" 
          class="bg-white/20 border-white/30 hover:bg-white/30"
          @click="handleUpgrade"
        >
          立即开通
        </el-button>
      </div>
    </div>

    <!-- 文章封面 -->
    <div v-if="article.coverImage" class="relative h-48 overflow-hidden">
      <img 
        :src="article.coverImage" 
        :alt="article.title"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent"></div>
      
      <!-- 锁定图标覆盖层 -->
      <div class="absolute inset-0 flex items-center justify-center bg-black/20">
        <div class="bg-white/90 rounded-full p-4 shadow-lg">
          <el-icon class="text-3xl text-amber-500"><Lock /></el-icon>
        </div>
      </div>

      <!-- 分类标签 -->
      <div class="absolute top-4 left-4">
        <el-tag 
          v-if="article.category" 
          type="primary" 
          effect="dark" 
          size="small"
          :style="{ backgroundColor: article.category.color }"
        >
          {{ article.category.name }}
        </el-tag>
      </div>
    </div>

    <!-- 文章内容预览 -->
    <div class="p-6">
      <!-- 无封面时的分类标签 -->
      <div v-if="!article.coverImage" class="flex items-center justify-between mb-4">
        <el-tag 
          v-if="article.category" 
          type="primary" 
          size="small"
          :style="{ backgroundColor: article.category.color }"
        >
          {{ article.category.name }}
        </el-tag>
        <div class="text-amber-500">
          <el-icon class="text-xl"><Lock /></el-icon>
        </div>
      </div>

      <!-- 文章标题 -->
      <h3 class="text-xl font-bold text-gray-900 mb-3 line-clamp-2">
        {{ article.title }}
      </h3>

      <!-- 文章摘要 -->
      <div class="mb-4">
        <p class="text-gray-600 line-clamp-3 leading-relaxed">
          {{ article.summary || getContentPreview(article.content) }}
        </p>
        <div class="mt-2 text-sm text-amber-600 font-medium">
          {{ accessDeniedMessage }}
        </div>
      </div>

      <!-- 标签列表 -->
      <div v-if="article.tags && article.tags.length > 0" class="mb-4">
        <div class="flex flex-wrap gap-2">
          <el-tag
            v-for="tag in article.tags.slice(0, 3)"
            :key="tag.id || tag"
            size="small"
            type="info"
            effect="plain"
          >
            # {{ typeof tag === 'string' ? tag : tag.name }}
          </el-tag>
          <span v-if="article.tags.length > 3" class="text-gray-400 text-sm self-center">
            +{{ article.tags.length - 3 }}
          </span>
        </div>
      </div>

      <!-- 作者信息和统计 -->
      <div class="flex items-center justify-between pt-4 border-t border-gray-100">
        <!-- 作者信息 -->
        <div class="flex items-center gap-3">
          <el-avatar 
            :src="article.author?.avatar || article.authorAvatar" 
            :size="32"
          >
            {{ (article.author?.username || article.authorName)?.charAt(0) }}
          </el-avatar>
          <div>
            <div class="text-sm font-medium text-gray-900">
              {{ article.author?.username || article.authorName }}
            </div>
            <div class="text-xs text-gray-500">
              {{ formatTime(article.publishedAt || article.createdAt) }}
            </div>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="flex items-center gap-3 text-sm text-gray-500">
          <div class="flex items-center gap-1">
            <el-icon><View /></el-icon>
            <span>{{ formatNumber(article.viewsCount || article.viewCount) }}</span>
          </div>
          <div class="flex items-center gap-1">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ formatNumber(article.commentsCount || article.commentCount) }}</span>
          </div>
        </div>
      </div>

      <!-- VIP升级引导 -->
      <div class="mt-6 p-4 bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl border border-amber-200">
        <div class="flex items-start gap-3">
          <div class="flex-shrink-0 w-8 h-8 bg-gradient-to-r from-amber-400 to-orange-500 rounded-full flex items-center justify-center">
            <el-icon class="text-white text-sm"><Crown /></el-icon>
          </div>
          <div class="flex-1">
            <h4 class="font-semibold text-gray-900 mb-2">开通会员，解锁全部内容</h4>
            <ul class="text-sm text-gray-600 space-y-1 mb-3">
              <li class="flex items-center gap-2">
                <el-icon class="text-green-500 text-xs"><Check /></el-icon>
                <span>无限制阅读所有会员文章</span>
              </li>
              <li class="flex items-center gap-2">
                <el-icon class="text-green-500 text-xs"><Check /></el-icon>
                <span>专属会员内容和深度解析</span>
              </li>
              <li class="flex items-center gap-2">
                <el-icon class="text-green-500 text-xs"><Check /></el-icon>
                <span>优先获得最新资讯和更新</span>
              </li>
            </ul>
            <div class="flex gap-2">
              <el-button 
                type="primary" 
                size="small"
                class="bg-gradient-to-r from-amber-400 to-orange-500 border-0"
                @click="handleUpgrade"
              >
                立即开通会员
              </el-button>
              <el-button 
                size="small" 
                @click="handleLearnMore"
              >
                了解更多
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { 
  Star, 
  Lock, 
  Crown,
  Check,
  View, 
  ChatDotRound
} from '@element-plus/icons-vue'

interface Article {
  id: string | number
  title: string
  summary?: string
  content?: string
  coverImage?: string
  category?: {
    id: string | number
    name: string
    color?: string
  }
  tags?: Array<string | { id: string | number; name: string }>
  author?: {
    id: string | number
    username: string
    avatar?: string
  }
  authorName?: string
  authorAvatar?: string
  publishedAt?: string
  createdAt?: string
  viewsCount?: number
  viewCount?: number
  commentsCount?: number
  commentCount?: number
  accessDeniedReason?: string
  hasAccess?: boolean
}

interface Props {
  article: Article
}

interface Emits {
  upgrade: []
  learnMore: []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 计算属性
const accessDeniedMessage = computed(() => {
  return props.article.accessDeniedReason || '该文章为会员专享内容，请开通会员后访问'
})

// 方法
const handleUpgrade = () => {
  emit('upgrade')
}

const handleLearnMore = () => {
  emit('learnMore')
}

const getContentPreview = (content?: string) => {
  if (!content) return ''
  
  // 移除 Markdown 标记并截取前100个字符
  const plainText = content
    .replace(/[#*`_~\[\]()]/g, '')
    .replace(/\n/g, ' ')
    .trim()
  
  return plainText.length > 100 
    ? plainText.substring(0, 100) + '...' 
    : plainText
}

const formatTime = (time?: string) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }
}

const formatNumber = (num?: number) => {
  if (!num) return '0'
  
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}
</script>

<style scoped>
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

.vip-banner {
  position: relative;
}

.vip-banner::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .p-6 {
    @apply p-4;
  }
  
  .vip-banner {
    @apply px-4 py-2;
  }
  
  .flex.items-center.justify-between {
    @apply flex-col items-start;
    gap: 0.75rem;
  }
}

@media (max-width: 640px) {
  .text-xl {
    @apply text-lg;
  }
  
  .vip-banner .flex {
    @apply flex-col gap-2;
  }
  
  .vip-banner .el-button {
    @apply w-full;
  }
}
</style>