<template>
  <div class="category-bar-wrapper">
    <div class="category-bar">
      <div class="category-bar-content">
        <div class="category-bar-inner">
          <button
            type="button"
            class="scroll-button scroll-button-left"
            aria-label="向左滚动分类"
            :disabled="!canScrollLeft"
            @click="scrollCategories('left')"
          >
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <div
            ref="categoryScrollRef"
            class="category-scroll-container"
            @scroll="handleScroll"
          >
            <!-- 分类列表 -->
            <div class="category-list">
              <button
                v-for="item in categoryItems"
                :key="item.key"
                type="button"
                class="category-item"
                :class="{
                  'is-active': isActive(item.id),
                  'is-ai-gold': item.id === '8888'
                }"
                @click="handleCategoryClick(item.id)"
              >
                <el-icon v-if="item.icon" class="category-item-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span v-else-if="item.id === '8888'" class="category-item-icon">⚡</span>
                <span class="category-item-name">{{ item.name }}</span>
              </button>
            </div>
          </div>
          <button
            type="button"
            class="scroll-button scroll-button-right"
            aria-label="向右滚动分类"
            :disabled="!canScrollRight"
            @click="scrollCategories('right')"
          >
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { usePublicCategoryStore } from '@/stores/publicCategory'

const router = useRouter()
const route = useRoute()
const categoryStore = usePublicCategoryStore()

interface CategoryItem {
  id: string | null
  key: string
  name: string
  icon?: string
}

const categoryScrollRef = ref<HTMLElement | null>(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
const scrollAmount = 240

const updateScrollState = () => {
  const container = categoryScrollRef.value
  if (!container) {
    canScrollLeft.value = false
    canScrollRight.value = false
    return
  }

  const maxScrollLeft = container.scrollWidth - container.clientWidth
  canScrollLeft.value = container.scrollLeft > 4
  canScrollRight.value = maxScrollLeft - container.scrollLeft > 4
}

const handleScroll = () => {
  updateScrollState()
}

const scrollCategories = (direction: 'left' | 'right') => {
  const container = categoryScrollRef.value
  if (!container) return

  const delta = direction === 'left' ? -scrollAmount : scrollAmount
  container.scrollBy({ left: delta, behavior: 'smooth' })
}

const handleResize = () => {
  updateScrollState()
}

// 获取分类数据
onMounted(async () => {
  window.addEventListener('resize', handleResize)
  try {
    await categoryStore.fetchCategories()
  } catch (error) {
    console.error('获取分类数据失败:', error)
  } finally {
    nextTick(() => updateScrollState())
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

// 过滤可见的分类
const visibleCategories = computed(() => {
  return categoryStore.categories.filter(category => {
    const status = category.status
    if (status === undefined || status === null || status === '') {
      return true
    }
    return status === '0' || status === 'active' || status === 'enabled'
  })
})

// 构建分类项目列表
const categoryItems = computed<CategoryItem[]>(() => {
  if (!visibleCategories.value.length) {
    return []
  }

  const mapped = visibleCategories.value.map<CategoryItem>(category => ({
    id: category.id != null ? String(category.id) : null,
    key: `category-${category.id ?? 'unknown'}`,
    name: category.name || category.categoryName || '未命名分类',
    icon: category.icon
  }))

  const prioritizedId = '8888'
  const prioritized = mapped.filter(item => item.id === prioritizedId)
  const others = mapped.filter(item => item.id !== prioritizedId)

  return [...prioritized, ...others]
})

watch(
  () => categoryItems.value.length,
  () => {
    nextTick(() => updateScrollState())
  },
  { immediate: true }
)

// 判断分类是否激活
const isActive = (categoryId: string | null) => {
  if (route.name !== 'Home') {
    return false
  }

  const rawQuery = route.query.category
  const queryValue = Array.isArray(rawQuery) ? rawQuery[0] : rawQuery

  return queryValue === categoryId
}

// 处理分类点击
const handleCategoryClick = (categoryId: string | null) => {
  const nextQuery = { ...route.query }

  if (categoryId === null) {
    if ('category' in nextQuery) {
      delete nextQuery.category
    }
  } else {
    nextQuery.category = categoryId
  }

  router.push({
    name: 'Home',
    query: nextQuery
  })
}
</script>

<style scoped>
.category-bar-wrapper {
  position: sticky;
  top: 60px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.category-bar {
  width: 100%;
}

.category-bar-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 clamp(0.75rem, 3vw, 2.5rem);
}

.category-bar-inner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  min-height: 36px;
}

.category-scroll-container {
  position: relative;
  flex: 1;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  scroll-behavior: smooth;
  padding: 0.25rem 0;
  mask-image: linear-gradient(to right, transparent, black 32px, black calc(100% - 32px), transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, black 32px, black calc(100% - 32px), transparent);
  touch-action: pan-x;
}

.category-scroll-container::-webkit-scrollbar {
  display: none;
}

.category-list {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
  flex-wrap: nowrap;
  width: max-content;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.875rem;
  border-radius: 18px;
  border: 1px solid transparent;
  background: rgba(248, 250, 252, 0.9);
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.2s ease;
  cursor: pointer;
}

.category-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-color: rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.category-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.category-item.is-ai-gold {
  position: relative;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
  font-weight: 600;
  animation: aiGoldPulse 3s ease-in-out infinite;
}

.category-item.is-ai-gold:hover {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.5);
}

.category-item.is-ai-gold.is-active {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.5);
}

.category-item.is-ai-gold::before {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(45deg, #f59e0b, #f97316, #ef4444, #f59e0b);
  border-radius: 20px;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: aiGoldBorderRotate 3s linear infinite;
}

.category-item.is-ai-gold:hover::before,
.category-item.is-ai-gold.is-active::before {
  opacity: 0.6;
}

.category-item-icon {
  width: 14px;
  height: 14px;
}

.category-item-name {
  font-weight: inherit;
}

.scroll-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.95);
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.08);
}

.scroll-button:hover:not(:disabled) {
  color: #4c51bf;
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 8px 18px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.scroll-button:disabled {
  opacity: 0.35;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 768px) {
  .category-bar-wrapper {
    top: 50px;
  }

  .category-bar-inner {
    padding: 0.375rem 0;
    min-height: 32px;
  }

  .category-item {
    padding: 0.3rem 0.7rem;
    font-size: 0.82rem;
  }

  .category-list {
    gap: 0.375rem;
  }

  .scroll-button {
    display: none;
  }
}

@media (max-width: 480px) {
  .category-bar-content {
    padding: 0 0.75rem;
  }

  .category-bar-inner {
    padding: 0.25rem 0;
  }

  .category-list {
    gap: 0.3rem;
  }
}

/* AI Gold Animations */
@keyframes aiGoldPulse {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
  }
  50% {
    box-shadow: 0 4px 20px rgba(245, 158, 11, 0.6);
  }
}

@keyframes aiGoldBorderRotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
