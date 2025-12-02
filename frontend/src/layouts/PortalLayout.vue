<template>
  <div class="portal-layout">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle decoration-circle-1"></div>
      <div class="decoration-circle decoration-circle-2"></div>
      <div class="decoration-circle decoration-circle-3"></div>
    </div>
    
    <!-- 导航栏组件 -->
    <Header v-if="showHeader" />
    
    <!-- 分类栏组件 -->
    <CategoryBar v-if="showCategoryBar" />
    
    <!-- 主要内容区域 -->
    <main class="main-content" :class="{ 'main-content--no-category': !showCategoryBar, 'main-content--no-header': !showHeader }">
      <router-view />
    </main>
    
    <!-- 页脚组件 -->
    <Footer v-if="showFooter" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import CategoryBar from '@/components/CategoryBar.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()

const aiTechCardRoutes = [
  'SolarSystem', 
  'NPUSimulation', 
  'HarmonicOscillator', 
  'DoubleSlit', 
  'GravitySlingshot', 
  'CellFactory'
]

const hideCategoryBarByName = new Set([
  'CreateArticle', 
  'EditArticle', 
  'PostEditor', 
  'AdvancedEditor', 
  'AdvancedEditorEdit',
  'AiTechList',
  ...aiTechCardRoutes
])

const hideFooterByName = new Set([
  'CreateArticle', 
  'EditArticle', 
  'PostEditor', 
  'AdvancedEditor', 
  'AdvancedEditorEdit',
  ...aiTechCardRoutes
])

const hideHeaderByName = new Set([
  ...aiTechCardRoutes
])

const showHeader = computed(() => {
  // 针对 /aitech 路径下的子页面（如 /aitech/xxx），隐藏头部
  // 但 /aitech 主页面（列表页）需要显示头部
  if (route.path.startsWith('/aitech/') && route.path.length > '/aitech/'.length) {
    return false
  }

  const name = route.name as string | undefined
  if (name && hideHeaderByName.has(name)) {
    return false
  }
  return true
})

const showCategoryBar = computed(() => {
  // 针对 /aitech 路径下的所有页面（包括列表页和子页面），隐藏分类栏
  if (route.path.startsWith('/aitech')) {
    return false
  }

  const name = route.name as string | undefined
  if (name && hideCategoryBarByName.has(name)) {
    return false
  }

  const pathName = route.path
  if (pathName.startsWith('/profile/articles/create') || pathName.startsWith('/advanced-editor')) {
    return false
  }

  return true
})

const showFooter = computed(() => {
  // 针对 /aitech 路径下的子页面（如 /aitech/xxx），隐藏页脚
  // 但 /aitech 主页面（列表页）需要显示页脚
  if (route.path.startsWith('/aitech/') && route.path.length > '/aitech/'.length) {
    return false
  }

  const name = route.name as string | undefined
  if (name && hideFooterByName.has(name)) {
    return false
  }

  const pathName = route.path
  if (
    pathName.startsWith('/profile/articles/create') ||
    (pathName.startsWith('/profile/articles/') && pathName.endsWith('/edit')) ||
    pathName.startsWith('/post-editor') ||
    pathName.startsWith('/advanced-editor')
  ) {
    return false
  }

  return true
})
</script>

<style scoped>
/* 门户布局 */
.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
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

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  animation: float 20s ease-in-out infinite;
}

.decoration-circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation-delay: 0s;
}

.decoration-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation-delay: -10s;
}

.decoration-circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  position: relative;
  z-index: 10;
  margin-top: 96px; /* 为 CategoryBar 留出空间：Header(60px) + CategoryBar(36px) */
  min-height: calc(100vh - 70px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-top: 86px; /* 移动端：Header(50px) + CategoryBar(36px) */
    border-radius: 0;
  }

  .main-content--no-category {
    margin-top: 56px;
  }
}

.main-content {
  --main-top-offset: 96px;
  margin-top: var(--main-top-offset);
}

.main-content--no-category {
  --main-top-offset: 56px;
}

.main-content--no-header {
  --main-top-offset: 36px;
}

.main-content--no-header.main-content--no-category {
  --main-top-offset: 0px;
}

@media (max-width: 768px) {
  .main-content {
    --main-top-offset: 86px;
  }

  .main-content--no-category {
    --main-top-offset: 56px;
  }

  .main-content--no-header {
    --main-top-offset: 36px;
  }

  .main-content--no-header.main-content--no-category {
    --main-top-offset: 0px;
  }
}
</style>
