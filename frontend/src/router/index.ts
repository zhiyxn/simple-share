import { createRouter, createWebHistory } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { SITE_TITLE } from '@/config/site'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Portal',
      component: () => import('@/layouts/PortalLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/portal/Home.vue'),
          meta: { title: '首页' }
        },
        {
          path: '/articles',
          name: 'ArticleList',
          component: () => import('@/views/portal/ArticleList.vue'),
          meta: { title: '文章列表' }
        },
        {
          path: '/article/:id',
          name: 'ArticleDetail',
          redirect: to => `/article/${to.params.id}/reader`
        },
        {
          path: '/article/:id/reader',
          name: 'ArticleReader',
          component: () => import('@/views/portal/ArticleReader.vue'),
          meta: { title: '沉浸阅读' }
        },
        {
          path: '/category/:id',
          name: 'CategoryArticles',
          component: () => import('@/views/portal/ArticleList.vue'),
          meta: { title: '分类文章' }
        },
        {
          path: '/search',
          name: 'SearchResults',
          component: () => import('@/views/portal/ArticleList.vue'),
          meta: { title: '搜索结果' }
        },
        {
          path: '/membership',
          name: 'Membership',
          component: () => import('@/views/portal/Membership.vue'),
          meta: { title: '会员中心' }
        },
        {
          path: '/post-editor',
          name: 'PostEditor',
          component: () => import('@/views/editor/PostEditor.vue'),
          meta: { title: '文章编辑器', requiresAuth: true }
        },
      ]
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '数据看板', icon: 'ep-odometer' }
        },
        {
          path: 'articles',
          name: 'AdminArticles',
          component: () => import('@/views/admin/AdminArticles.vue'),
          meta: { title: '文章管理', icon: 'ep-document' }
        },
        {
          path: 'articles/review',
          name: 'ArticleReview',
          component: () => import('@/views/admin/ArticleReview.vue'),
          meta: { title: '内容审核', icon: 'ep-document-checked' }
        },
        {
          path: 'articles/create',
          name: 'AdminArticleCreate',
          component: () => import('@/views/admin/AdminArticles.vue'),
          meta: { title: '创建文章', icon: 'ep-document-add' }
        },
        {
          path: 'articles/:id/edit',
          name: 'AdminArticleEdit',
          component: () => import('@/views/admin/AdminArticles.vue'),
          meta: { title: '编辑文章', icon: 'ep-edit' }
        },
        {
          path: 'categories',
          name: 'CategoryManage',
          component: () => import('@/views/admin/CategoryManagement.vue'),
          meta: { title: '分类管理', icon: 'ep-folder' }
        },
        {
          path: 'users',
          name: 'UserManage',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: { title: '用户管理', icon: 'ep-user' }
        },
        {
          path: 'roles',
          name: 'RoleManage',
          component: () => import('@/views/admin/RoleManagement.vue'),
          meta: { title: '角色权限', icon: 'ep-key' }
        },
        {
          path: 'menus',
          name: 'MenuManage',
          component: () => import('@/views/admin/MenuManagement.vue'),
          meta: { title: '菜单管理', icon: 'ep-menu' }
        },
        {
          path: 'logs',
          name: 'OperationLog',
          component: () => import('@/views/admin/OperationLog.vue'),
          meta: { title: '操作日志', icon: 'ep-document-checked' }
        },
        {
          path: 'config',
          name: 'TenantConfig',
          component: () => import('@/views/admin/TenantSettings.vue'),
          meta: { title: '租户配置', icon: 'ep-setting' }
        },
        {
          path: 'security',
          name: 'SecuritySettings',
          component: () => import('@/views/admin/SecuritySettings.vue'),
          meta: { title: '安全设置', icon: 'ep-lock' }
        },
        {
          path: 'files',
          name: 'FileConfig',
          component: () => import('@/views/admin/FileConfigManagement.vue'),
          meta: { title: '文件配置', icon: 'ep-files' }
        },
        {
          path: 'tenants',
          name: 'TenantManagement',
          component: () => import('@/views/admin/TenantManagement.vue'),
          meta: { title: '租户管理', icon: 'ep-office-building' }
        },
        {
          path: 'analytics',
          name: 'Analytics',
          component: () => import('@/views/admin/Analytics.vue'),
          meta: { title: '统计分析', icon: 'ep-trend-charts' }
        },
        {
          path: 'aitech/clicks',
          name: 'AiTechClickStats',
          component: () => import('@/views/admin/AiTechClickStats.vue'),
          meta: { title: 'AiTech 点击统计', icon: 'ep-data-line' }
        }
      ]
    },
    {
      path: '/auth',
      name: 'Auth',
      component: () => import('@/layouts/AuthLayout.vue'),
      children: [
        {
          path: 'login',
          name: 'Login',
          component: () => import('@/views/auth/Login.vue'),
          meta: { title: '登录' }
        },
        {
          path: 'register',
          name: 'Register',
          component: () => import('@/views/auth/Register.vue'),
          meta: { title: '注册' }
        },
        {
          path: 'forgot-password',
          name: 'ForgotPassword',
          component: () => import('@/views/auth/ForgotPassword.vue'),
          meta: { title: '忘记密码' }
        }
      ]
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/layouts/PortalLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'UserProfile',
          component: () => import('@/views/user/Profile.vue'),
          meta: { title: '个人资料' }
        },
        {
          path: 'articles',
          name: 'MyArticles',
          component: () => import('@/views/user/MyArticles.vue'),
          meta: { title: '我的文章' }
        },
        {
          path: 'favorites',
          name: 'MyFavorites',
          component: () => import('@/views/user/MyFavorites.vue'),
          meta: { title: '我的收藏' }
        },
        {
          path: 'articles/create',
          name: 'CreateArticle',
          component: () => import('@/views/editor/PostEditor.vue'),
          meta: { title: '写作台' }
        },
        {
          path: 'articles/:id/edit',
          name: 'EditArticle',
          component: () => import('@/views/editor/PostEditor.vue'),
          meta: { title: '编辑文章' }
        }
      ]
    },
    
    {
      path: '/advanced-editor',
      name: 'AdvancedEditor',
      component: () => import('@/views/editor/PostEditor.vue'),
      meta: { requiresAuth: true, title: '高级编辑器' }
    },
    {
      path: '/advanced-editor/:id',
      name: 'AdvancedEditorEdit',
      component: () => import('@/views/editor/PostEditor.vue'),
      meta: { requiresAuth: true, title: '高级编辑器' }
    },
    {
      path: '/404',
      name: 'NotFound',
      component: () => import('@/views/error/NotFound.vue'),
      meta: { title: '页面未找到' }
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/Forbidden.vue'),
      meta: { title: '访问被拒绝' }
    },
    {
      path: '/500',
      name: 'ServerError',
      component: () => import('@/views/error/ServerError.vue'),
      meta: { title: '服务器错误' }
    },
    {
      path: '/aitech',
      name: 'AiTech',
      component: () => import('@/layouts/PortalLayout.vue'),
      children: [
        {
          path: '',
          name: 'AiTechList',
          component: () => import('@/views/aitech/AiTechList.vue'),
          meta: { title: 'AI 教程' }
        },
        {
          path: 'chinese-history',
          name: 'ChineseHistory',
          component: () => import('@/views/aitech/history/ChineseHistory.vue'),
          meta: { title: '中华上下五千年' }
        },
        {
          path: 'silk-road',
          name: 'SilkRoad3D',
          component: () => import('@/views/aitech/SilkRoad3D.vue'),
          meta: { title: '丝绸之路' }
        },
        {
          path: 'solar-system',
          name: 'SolarSystem',
          component: () => import('@/views/aitech/solar/SolarSystem.vue'),
          meta: { title: '太阳系模拟' }
        },
        {
          path: 'npu-simulation',
          name: 'NPUSimulation',
          component: () => import('@/views/aitech/npu/NPUSimulation.vue'),
          meta: { title: 'NPU 原理模拟' }
        },
        {
          path: 'harmonic-oscillator',
          name: 'HarmonicOscillator',
          component: () => import('@/views/aitech/physics/HarmonicOscillator.vue'),
          meta: { title: '简谐振动模拟' }
        },
        {
          path: 'double-slit',
          name: 'DoubleSlit',
          component: () => import('@/views/aitech/physics/DoubleSlit.vue'),
          meta: { title: '光的双缝干涉' }
        },
        {
          path: 'gravity-slingshot',
          name: 'GravitySlingshot',
          component: () => import('@/views/aitech/physics/GravitySlingshot.vue'),
          meta: { title: '引力弹弓模拟' }
        },
        {
          path: 'solid-geometry',
          name: 'SolidGeometry',
          component: () => import('@/views/aitech/physics/SolidGeometry.vue'),
          meta: { title: '立体几何可视化' }
        },
        {
          path: 'photosynthesis',
          name: 'PhotosynthesisLab',
          component: () => import('@/views/aitech/biology/PhotosynthesisLab.vue'),
          meta: { title: '光合作用实验室' }
        },
        {
          path: 'cell-factory',
          name: 'CellFactory',
          component: () => import('@/views/aitech/biology/CellFactory.vue'),
          meta: { title: '细胞工厂模拟' }
        },
        {
          path: 'contour-terrain',
          name: 'ContourTerrain',
          component: () => import('@/views/aitech/geography/ContourTerrain.vue'),
          meta: { title: '等高线与3D地形' }
        },
        {
          path: 'doppler-effect',
          name: 'DopplerEffect',
          component: () => import('@/views/aitech/physics/DopplerEffect.vue'),
          meta: { title: '多普勒效应模拟' }
        },
        {
          path: 'chinese-history',
          name: 'ChineseHistory',
          component: () => import('@/views/aitech/ChineseHistory.vue'),
          meta: { title: '中华上下5000年' }
        },
        {
          path: 'hologomoku',
          name: 'HoloGomoku',
          component: () => import('@/views/aitech/hologomoku/HoloGomoku.vue'),
          meta: { title: '全息手势五子棋' }
        },
        {
          path: 'holo-album',
          name: 'HoloAlbum',
          component: () => import('@/views/aitech/album/HoloAlbum.vue'),
          meta: { title: '全息3D相册' }
        },
        {
          path: 'solar-system',
          name: 'SolarSystem',
          component: () => import('@/views/aitech/solar/SolarSystem.vue'),
          meta: { title: '星际探索系统' }
        },
        {
          path: 'neon-smash',
          name: 'NeonSmash',
          component: () => import('@/views/aitech/physics/NeonSmash.vue'),
          meta: { title: '霓虹粉碎室' }
        },
        {
          path: 'fortune',
          name: 'FortuneBlessing',
          component: () => import('@/views/aitech/fortune/FortuneBlessing.vue'),
          meta: { title: '新春福气' }
        },
        {
          path: 'holo-earth',
          name: 'HoloEarth',
          component: () => import('@/views/aitech/earth/HoloEarth.vue'),
          meta: { title: '全息地球监控' }
        },
        {
          path: 'mantle-earth',
          name: 'MantleEarth',
          component: () => import('@/views/aitech/earth/MantleEarth.vue'),
          meta: { title: '地质动力学模拟' }
        },
        {
          path: 'modern-history',
          name: 'ModernHistory',
          component: () => import('@/views/aitech/ModernHistory.vue'),
          meta: { title: '中国近代史：觉醒之路' }
        },
        {
          path: 'japan-invasion-history',
          name: 'JapanInvasionHistory',
          component: () => import('@/views/aitech/history/JapanInvasionHistory.vue'),
          meta: { title: '日本侵华史实' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const tenantStore = useTenantStore()
  const userStore = useUserStore()

  // 初始化租户信息 - 异步进行，不阻塞路由
  if (!tenantStore.hasTenant) {
    // 确保租户初始化不会阻塞路由
    tenantStore.initTenant().catch(error => {
      console.error('Failed to initialize tenant:', error)
    })
  }
  
  // 设置页面标题
  const title = to.meta.title as string
  if (title) {
    const tenantTitle = tenantStore.siteTitle || SITE_TITLE
    document.title = `${title} - ${tenantTitle}`
  }
  
  // 检查认证要求 - 检查当前路由和所有父路由的认证要求
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  
  if (requiresAuth) {
    if (!userStore.hasValidToken()) {
      ElMessage.warning('请先登录')
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 如果有token但没有用户信息，尝试获取用户信息
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        // 获取用户信息失败，重定向到登录页
        ElMessage.warning('登录已过期，请重新登录')
        next({
          name: 'Login',
          query: { redirect: to.fullPath }
        })
        return
      }
    }

    // 检查管理员权限
    if (requiresAdmin && !userStore.isAdmin) {
      ElMessage.error('您没有访问权限')
      next({ name: 'Forbidden' })
      return
    }
  }
  
  // 如果已登录用户访问认证页面，重定向到首页
  if (to.path.startsWith('/auth') && userStore.hasValidToken()) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
  ElMessage.error('页面加载失败')
})

export default router

// 导出路由类型
export type RouteNames = 
  | 'Home'
  | 'ArticleDetail'
  | 'CategoryArticles'
  | 'SearchResults'
  | 'AdminDashboard'
  | 'ArticleManage'
  | 'ArticleCreate'
  | 'ArticleEdit'
  | 'CategoryManage'
  | 'UserManage'
  | 'RoleManage'
  | 'MenuManage'
  | 'TenantConfig'
  | 'SecuritySettings'
  | 'Login'
  | 'Register'
  | 'ForgotPassword'
  | 'UserProfile'
  | 'MyArticles'
  | 'MyFavorites'
  | 'ArticleEditor'
  | 'CreateArticle'
  | 'EditArticle'
  | 'ModernEditor'
  | 'ModernEditorEdit'
  | 'AdvancedEditor'
  | 'AdvancedEditorEdit'
  | 'AiTechClickStats'
  | 'NotFound'
  | 'Forbidden'
  | 'ServerError'

// 管理后台菜单配置
export const adminMenus = [
  {
    name: 'AdminDashboard',
    title: '仪表盘',
    icon: 'ep-odometer',
    path: '/admin'
  },
  {
    name: 'ArticleManage',
    title: '文章管理',
    icon: 'ep-document',
    path: '/admin/articles'
  },
  {
    name: 'ArticleReview',
    title: '文章审核',
    icon: 'ep-document-checked',
    path: '/admin/articles/review'
  },
  {
    name: 'CategoryManage',
    title: '分类管理',
    icon: 'ep-folder',
    path: '/admin/categories'
  },
  {
    name: 'UserManage',
    title: '用户管理',
    icon: 'ep-user',
    path: '/admin/users'
  },
  {
    name: 'RoleManage',
    title: '角色权限',
    icon: 'ep-key',
    path: '/admin/roles'
  },
  {
    name: 'TenantConfig',
    title: '系统设置',
    icon: 'ep-setting',
    path: '/admin/config'
  },
  {
    name: 'SecuritySettings',
    title: '安全设置',
    icon: 'ep-lock',
    path: '/admin/security'
  },
  {
    name: 'AiTechClickStats',
    title: 'AiTech 点击统计',
    icon: 'ep-data-line',
    path: '/admin/aitech/clicks'
  }
]

// 用户菜单配置
export const userMenus = [
  {
    name: 'UserProfile',
    title: '个人资料',
    icon: 'ep-user',
    path: '/profile'
  },
  {
    name: 'MyArticles',
    title: '我的文章',
    icon: 'ep-document',
    path: '/profile/articles'
  },
  {
    name: 'MyFavorites',
    title: '我的收藏',
    icon: 'ep-star',
    path: '/profile/favorites'
  }
]
