<template>

  <div class="admin-layout">

    <!-- 侧边?-->

    <aside 

      class="sidebar"

      :class="{ 'sidebar-collapsed': sidebarCollapsed }"

    >

      <!-- Logo区域 -->

      <div class="sidebar-header">

        <div class="flex items-center space-x-3">

          <img 

            v-if="tenantStore.tenantInfo?.logo" 

            :src="tenantStore.tenantInfo.logo" 

            :alt="tenantStore.tenantInfo.name"

            class="h-8 w-auto"

          />

          <div v-else class="w-8 h-8 bg-blue-600 rounded flex items-center justify-center">

            <span class="text-white font-bold text-sm">S</span>

          </div>

          <span 

            v-show="!sidebarCollapsed" 

            class="text-lg font-bold text-white transition-opacity duration-200"

          >

            管理后台

          </span>

        </div>

      </div>

      

      <!-- 导航菜单 -->

      <nav class="sidebar-nav">

        <el-menu

          ref="menuRef"

          :default-active="activeMenu"

          :default-openeds="openMenuKeys"

          :collapse="sidebarCollapsed"

          :unique-opened="true"

          background-color="#1f2937"

          text-color="#d1d5db"

          active-text-color="#3b82f6"

          @select="handleMenuSelect"

        >

          <template v-if="renderedMenus.length">

            <SidebarMenuItem

              v-for="menu in renderedMenus"

              :key="menu.id"

              :menu="menu"

            />

          </template>

          <template v-else>

            <el-menu-item index="/admin">

              <el-icon><Odometer /></el-icon>

              <template #title>仪表盘</template>

            </el-menu-item>

          </template>

        </el-menu>

      </nav>

    </aside>

    

    <!-- 主要内容区域 -->

    <div class="main-content">

      <!-- 顶部?-->

      <header class="topbar">

        <div class="topbar-left">

          <!-- 侧边栏切换按?-->

          <el-button 

            text 

            @click="toggleSidebar"

            class="sidebar-toggle"

          >

            <el-icon><Expand v-if="sidebarCollapsed" /><Fold v-else /></el-icon>

          </el-button>

          

          <!-- 面包屑导?-->

          <el-breadcrumb separator="/" class="breadcrumb">

            <el-breadcrumb-item 

              v-for="item in breadcrumbs" 

              :key="item.path"

              :to="item.path"

            >

              {{ item.title }}

            </el-breadcrumb-item>

          </el-breadcrumb>

        </div>

        

        <div class="topbar-right">

          <!-- 快捷操作 -->

          <div class="flex items-center space-x-4">

            <!-- 返回前台 -->

            <el-tooltip content="返回前台" placement="bottom">

              <el-button 

                text 

                @click="$router.push('/')"

                class="text-gray-600 hover:text-blue-600"

              >

                <el-icon><House /></el-icon>

              </el-button>

            </el-tooltip>

            

            <!-- 新建文章 -->

            <el-tooltip content="新建文章" placement="bottom">

              <el-button 

                text 

                @click="$router.push('/admin/articles/create')"

                class="text-gray-600 hover:text-blue-600"

              >

                <el-icon><Plus /></el-icon>

              </el-button>

            </el-tooltip>

            

            <!-- 通知 -->

            <el-badge :value="unreadCount" :hidden="unreadCount === 0">

              <el-button 

                text 

                @click="showNotifications = true"

                class="text-gray-600 hover:text-blue-600"

              >

                <el-icon><Bell /></el-icon>

              </el-button>

            </el-badge>

            

            <!-- 用户菜单 -->

            <el-dropdown trigger="click" @command="handleUserCommand">

              <div class="user-menu">

                <el-avatar 

                  :src="userStore.userAvatar" 

                  :size="32"

                  class="border border-gray-200"

                >

                  {{ userStore.displayName?.charAt(0) }}

                </el-avatar>

                <span class="user-name">{{ userStore.displayName }}</span>

                <el-icon class="text-gray-400"><ArrowDown /></el-icon>

              </div>

              

              <template #dropdown>

                <el-dropdown-menu>

                  <el-dropdown-item command="profile">

                    <el-icon class="mr-2"><User /></el-icon>

                    个人资料

                  </el-dropdown-item>

                  <el-dropdown-item command="settings">

                    <el-icon class="mr-2"><Setting /></el-icon>

                    账户设置

                  </el-dropdown-item>

                  <el-dropdown-item divided command="logout">

                    <el-icon class="mr-2"><SwitchButton /></el-icon>

                    退出登?

                  </el-dropdown-item>

                </el-dropdown-menu>

              </template>

            </el-dropdown>

          </div>

        </div>

      </header>

      

      <!-- 页面内容 -->

      <main class="page-content">

        <router-view />

      </main>

    </div>

    

    <!-- 通知抽屉 -->

    <el-drawer

      v-model="showNotifications"

      title="通知消息"

      direction="rtl"

      size="400px"

    >

      <div class="notifications-content">

        <div v-if="notifications.length === 0" class="empty-notifications">

          <el-empty description="暂无通知" />

        </div>

        

        <div v-else class="notification-list">

          <div 

            v-for="notification in notifications" 

            :key="notification.id"

            class="notification-item"

            :class="{ 'unread': !notification.read }"

            @click="markAsRead(notification.id)"

          >

            <div class="notification-icon">

              <el-icon 

                :class="getNotificationIcon(notification.type)"

                :style="{ color: getNotificationColor(notification.type) }"

              />

            </div>

            <div class="notification-content">

              <div class="notification-title">{{ notification.title }}</div>

              <div class="notification-message">{{ notification.message }}</div>

              <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>

            </div>

          </div>

        </div>

        

        <div v-if="notifications.length > 0" class="notification-actions">

          <el-button text @click="markAllAsRead">全部标记为已读</el-button>

          <el-button text @click="clearAllNotifications">清空通知</el-button>

        </div>

      </div>

    </el-drawer>

  </div>

</template>



<script setup lang="ts">

import { ref, computed, onMounted, watch, defineComponent, h } from 'vue'

import type { Component, PropType } from 'vue'

import { useRoute, useRouter } from 'vue-router'

import { ElMessage, ElMessageBox, ElSubMenu, ElMenuItem, ElIcon } from 'element-plus'

import {

  Odometer,

  Document,

  EditPen,

  DocumentChecked,

  Collection,

  User,

  Avatar,

  Key,

  Setting,

  OfficeBuilding,

  Lock,

  List,

  TrendCharts,

  Expand,

  Fold,

  House,

  Plus,

  Bell,

  ArrowDown,

  SwitchButton,

  Menu

} from '@element-plus/icons-vue'

import { useTenantStore } from '@/stores/tenant'

import { useUserStore } from '@/stores/user'

import { menuApi } from '@/api/menu'

import type { Menu as SysMenu } from '@/types/menu'



interface Notification {

  id: string

  type: 'info' | 'success' | 'warning' | 'error'

  title: string

  message: string

  read: boolean

  createdAt: string

}



interface BreadcrumbItem {

  title: string

  path: string

}



interface SidebarMenuNode {

  id: string

  title: string

  icon?: string

  route?: string

  children?: SidebarMenuNode[]

  sort?: number

}



const route = useRoute()

const router = useRouter()

const tenantStore = useTenantStore()

const userStore = useUserStore()



const dashboardMenu: SidebarMenuNode = {
  id: 'dashboard',
  title: 'Dashboard',
  icon: 'ep-odometer',
  route: '/admin',
  sort: 0
}

const fallbackMenuTree: SidebarMenuNode[] = [
  {
    id: 'content',
    title: '内容管理',
    icon: 'document',
    sort: 1,
    children: [
      { id: 'content-articles', title: '文章管理', icon: 'edit-pen', route: '/admin/articles', sort: 1 },
      { id: 'content-categories', title: '分类管理', icon: 'collection', route: '/admin/categories', sort: 2 },
      { id: 'content-article-review', title: '文章审核', icon: 'ep-document-checked', route: '/admin/articles/review', sort: 3 }
    ]
  },
  {
    id: 'users',
    title: '用户管理',
    icon: 'user',
    sort: 2,
    children: [
      { id: 'users-list', title: '用户列表', icon: 'avatar', route: '/admin/users', sort: 1 },
      { id: 'users-roles', title: '角色权限', icon: 'key', route: '/admin/roles', sort: 2 }
    ]
  },
  {
    id: 'system',
    title: '系统设置',
    icon: 'setting',
    sort: 3,
    children: [
      { id: 'system-menus', title: '菜单管理', icon: 'menu', route: '/admin/menus', sort: 1 },
      { id: 'system-config', title: '租户配置', icon: 'office-building', route: '/admin/config', sort: 2 },
      { id: 'system-security', title: '安全设置', icon: 'lock', route: '/admin/security', sort: 3 },
      { id: 'system-logs', title: '操作日志', icon: 'list', route: '/admin/logs', sort: 4 }
    ]
  },
  {
    id: 'analytics',
    title: '数据分析',
    icon: 'trend-charts',
    route: '/admin/analytics',
    sort: 4
  },
  {
    id: 'aitech-stats',
    title: 'AiTech 统计',
    icon: 'trend-charts',
    route: '/admin/aitech/clicks',
    sort: 5
  }
]

const routeAlias: Record<string, string> = {

  article: '/admin/articles',

  articles: '/admin/articles',

  'articles/review': '/admin/articles/review',

  category: '/admin/categories',

  categories: '/admin/categories',

  tag: '/admin/tags',

  tags: '/admin/tags',

  user: '/admin/users',

  role: '/admin/roles',

  menu: '/admin/menus',

  menus: '/admin/menus',

  config: '/admin/config',

  security: '/admin/security',

  logs: '/admin/logs',

  analytics: '/admin/analytics',

  'aitech/clicks': '/admin/aitech/clicks'

}



const iconAliasMap: Record<string, Component> = {

  'ep-odometer': Odometer,

  odometer: Odometer,

  document: Document,

  documentation: Document,

  'ep-document': Document,

  'ep-document-checked': DocumentChecked,

  documentchecked: DocumentChecked,

  'edit-pen': EditPen,

  edit: EditPen,

  collection: Collection,

  tree: Collection,

  'tree-table': Collection,

  'price-tag': Collection,

  user: User,

  'ep-user': User,

  avatar: Avatar,

  peoples: Avatar,

  key: Key,

  setting: Setting,

  system: Setting,

  menu: Menu,

  menus: Menu,

  'office-building': OfficeBuilding,

  lock: Lock,

  list: List,

  'trend-charts': TrendCharts,

  analytics: TrendCharts

}



const resolveIconComponent = (icon?: string): Component => {

  if (!icon) {

    return Document

  }

  const normalized = icon.toLowerCase()

  if (iconAliasMap[normalized]) {

    return iconAliasMap[normalized]

  }

  if (normalized.startsWith('ep-')) {

    const key = normalized.slice(3)

    if (iconAliasMap[key]) {

      return iconAliasMap[key]

    }

  }

  return Document

}



const SidebarMenuItem = defineComponent({
  name: 'SidebarMenuItem',
  props: {
    menu: {
      type: Object as PropType<SidebarMenuNode>,
      required: true
    }
  },
  setup(props) {
    const renderMenuLabel = () => {
      const IconComponent = resolveIconComponent(props.menu.icon)
      return [
        h(
          ElIcon,
          { class: 'sidebar-menu-icon' },
          { default: () => h(IconComponent) }
        ),
        h('span', { class: 'sidebar-menu-text' }, props.menu.title)
      ]
    }

    return () => {
      const children = props.menu.children ?? []
      if (children.length > 0) {
        return h(
          ElSubMenu,
          { index: String(props.menu.id) },
          {
            title: () => renderMenuLabel(),
            default: () =>
              children.map((child) =>
                h(SidebarMenuItem, {
                  key: child.id,
                  menu: child
                })
              )
          }
        )
      }

      const menuIndex =
        typeof props.menu.route === 'string' && props.menu.route.length > 0
          ? props.menu.route
          : `menu-${props.menu.id}`

      return h(
        ElMenuItem,
        { index: menuIndex },
        { default: () => renderMenuLabel() }
      )
    }
  }
})

const resolveRoutePath = (path?: string | null): string | undefined => {

  if (!path) {

    return undefined

  }

  const trimmed = path.trim()

  if (routeAlias[trimmed]) {

    return routeAlias[trimmed]

  }

  if (/^https?:\/\//i.test(trimmed)) {

    return trimmed

  }

  if (trimmed.startsWith('/admin')) {

    return trimmed

  }

  if (trimmed.startsWith('/')) {

    return `/admin${trimmed}`

  }

  return `/admin/${trimmed}`

}



const extractMenuTree = (payload: unknown): SysMenu[] => {

  if (!payload) {

    return []

  }

  if (Array.isArray(payload)) {

    return payload as SysMenu[]

  }

  const maybeAny = payload as Record<string, unknown>

  const candidates = ['data', 'rows', 'list', 'items', 'records']

  for (const key of candidates) {

    const value = maybeAny[key]

    if (Array.isArray(value)) {

      return value as SysMenu[]

    }

  }

  return []

}



const transformMenuTree = (menus: SysMenu[] = []): SidebarMenuNode[] => {

  return menus

    .map((menu) => {

      const children = menu.children ? transformMenuTree(menu.children as SysMenu[]) : []

      const node: SidebarMenuNode = {

        id: String(menu.menuId ?? menu.menuName ?? Math.random()),

        title: menu.menuName,

        icon: menu.icon || undefined,

        sort: menu.orderNum ?? 0

      }

      if (menu.menuType === 'C') {

        node.route = resolveRoutePath(menu.path)

      }

      if (children.length) {

        node.children = children

      }

      if (!node.route && (!node.children || node.children.length === 0)) {

        return null

      }

      return node

    })

    .filter((node): node is SidebarMenuNode => !!node)

    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))

}



const buildRouteParents = (nodes: SidebarMenuNode[], parents: string[] = [], map: Record<string, string[]> = {}) => {

  nodes.forEach((node) => {

    if (node.route) {

      map[node.route] = parents

    }

    if (node.children?.length) {

      buildRouteParents(node.children, [...parents, node.id], map)

    }

  })

  return map

}



const loadMenus = async () => {

  try {

    const response = await menuApi.getMenuTree()

    const list = extractMenuTree(response)

    const tree = transformMenuTree(list)

    sidebarMenus.value = tree.length ? tree : fallbackMenuTree

    routeParentMap.value = buildRouteParents(sidebarMenus.value)

  } catch (error) {

    console.error('加载菜单失败:', error)

    sidebarMenus.value = fallbackMenuTree

    routeParentMap.value = buildRouteParents(fallbackMenuTree)

    ElMessage.error('加载菜单失败，已展示默认菜单')

  }

}

const menuRef = ref<any>(null)

const sidebarMenus = ref<SidebarMenuNode[]>([])

const routeParentMap = ref<Record<string, string[]>>({})







// 响应式数?

const sidebarCollapsed = ref(false)

const showNotifications = ref(false)

const notifications = ref<Notification[]>([])



// 计算属?

const activeMenu = computed(() => {

  return route.path

})



const breadcrumbs = computed((): BreadcrumbItem[] => {

  const pathSegments = route.path.split('/').filter(Boolean)

  const breadcrumbs: BreadcrumbItem[] = []

  

  // 根据路由生成面包?

  if (pathSegments[0] === 'admin') {

    breadcrumbs.push({ title: '管理后台', path: '/admin' })

    

    if (pathSegments[1]) {

      const routeMap: Record<string, string> = {

        'articles': '文章管理',

        'categories': '分类管理',

        'users': '用户管理',

        'roles': '角色权限',

        'menus': '菜单管理',

        'config': '租户配置',

        'security': '安全设置',

        'logs': '操作日志',

        'analytics': '统计分析'

      }



      const isArticleReview = pathSegments[1] === 'articles' && pathSegments[2] === 'review'

      const title = isArticleReview ? '文章审核' : (routeMap[pathSegments[1]] || pathSegments[1])

      const basePath = isArticleReview ? '/admin/articles/review' : `/admin/${pathSegments[1]}`



      breadcrumbs.push({

        title,

        path: basePath

      })



      // 处理子页?

      if (pathSegments[2] && !isArticleReview) {

        if (pathSegments[2] === 'create') {

          breadcrumbs.push({ title: '新建', path: route.path })

        } else if (pathSegments[2] === 'edit') {

          breadcrumbs.push({ title: '编辑', path: route.path })

        } else {

          breadcrumbs.push({ title: '详情', path: route.path })

        }

      }

    }

  }

  

  return breadcrumbs

})



const unreadCount = computed(() => {

  return notifications.value.filter(n => !n.read).length

})



const renderedMenus = computed(() => {
  const menus = sidebarMenus.value.length ? sidebarMenus.value : fallbackMenuTree
  const others = menus.filter(menu => menu.route !== '/admin')
  return [dashboardMenu, ...others]
})



const openMenuKeys = computed(() => {

  return routeParentMap.value[route.path] || []

})



// 方法

const toggleSidebar = () => {

  sidebarCollapsed.value = !sidebarCollapsed.value

  // 保存到本地存?

  localStorage.setItem('admin-sidebar-collapsed', String(sidebarCollapsed.value))

}



const handleMenuSelect = (index: string) => {

  if (!index) {

    return

  }

  if (/^https?:\/\//i.test(index)) {

    window.open(index, '_blank', 'noopener')

    return

  }

  if (!index.startsWith('/')) {

    return

  }

  if (index !== route.path) {

    router.push(index)

  }

}



const handleUserCommand = async (command: string) => {

  switch (command) {

    case 'profile':

      router.push('/profile')

      break

    case 'settings':

      router.push('/profile/settings')

      break

    case 'logout':

      await handleLogout()

      break

  }

}



const handleLogout = async () => {

  try {

    await ElMessageBox.confirm(
      'Are you sure you want to log out?',
      'Confirmation',
      {
        type: 'warning'
      }
    )


    

    await userStore.logout(false) // 不显示提示，静默退出，会自动跳转到首页

  } catch (error) {

    if (error !== 'cancel') {

      console.error('Logout failed:', error)

      ElMessage.error('Logout failed')

    }

  }

}



const getNotificationIcon = (type: string) => {

  const iconMap: Record<string, string> = {

    'info': 'InfoFilled',

    'success': 'SuccessFilled',

    'warning': 'WarningFilled',

    'error': 'CircleCloseFilled'

  }

  return iconMap[type] || 'InfoFilled'

}



const getNotificationColor = (type: string) => {

  const colorMap: Record<string, string> = {

    'info': '#409eff',

    'success': '#67c23a',

    'warning': '#e6a23c',

    'error': '#f56c6c'

  }

  return colorMap[type] || '#409eff'

}



const formatTime = (time: string) => {

  const date = new Date(time)

  const now = new Date()

  const diff = now.getTime() - date.getTime()

  

  if (diff < 60000) {

    return '刚刚'

  } else if (diff < 3600000) {

    return `${Math.floor(diff / 60000)}分钟前`

  } else if (diff < 86400000) {

    return `${Math.floor(diff / 3600000)}小时前`

  } else {

    return date.toLocaleDateString()

  }

}



const markAsRead = (id: string) => {

  const notification = notifications.value.find(n => n.id === id)

  if (notification) {

    notification.read = true

  }

}



const markAllAsRead = () => {

  notifications.value.forEach(n => n.read = true)

}



const clearAllNotifications = () => {

  notifications.value = []

  showNotifications.value = false

}



const loadNotifications = () => {

  // 模拟通知数据

  notifications.value = [

    {

      id: '1',

      type: 'info',

      title: '系统通知',

      message: '系统将于今晚23:00进行维护，预计持?小时',

      read: false,

      createdAt: new Date(Date.now() - 3600000).toISOString()

    },

    {

      id: '2',

      type: 'success',

      title: '文章审核',

      message: '您的文章《Vue 3 最佳实践》已通过审核',

      read: false,

      createdAt: new Date(Date.now() - 7200000).toISOString()

    }

  ]

}



// 生命周期

onMounted(() => {

  void loadMenus()

  // 恢复侧边栏状?

  const collapsed = localStorage.getItem('admin-sidebar-collapsed')

  if (collapsed !== null) {

    sidebarCollapsed.value = collapsed === 'true'

  }

  

  // 加载通知

  loadNotifications()

})



// 监听路由变化，自动收起移动端侧边?

watch(() => route.path, () => {

  if (window.innerWidth < 768) {

    sidebarCollapsed.value = true

  }

})

</script>



<style scoped>

.admin-layout {

  @apply flex h-screen bg-gray-50;

}



.sidebar {

  @apply w-64 bg-gray-800 shadow-lg transition-all duration-300 ease-in-out;

}



.sidebar-collapsed {

  @apply w-16;

}



.sidebar-header {

  @apply p-4 border-b border-gray-700;

}



.sidebar-nav {

  @apply flex-1 overflow-y-auto;

}



.main-content {

  @apply flex-1 flex flex-col overflow-hidden;

}



.topbar {

  @apply h-16 bg-white shadow-sm border-b border-gray-200 flex items-center justify-between px-6;

}



.topbar-left {

  @apply flex items-center space-x-4;

}



.topbar-right {

  @apply flex items-center;

}



.sidebar-toggle {

  @apply text-gray-600 hover:text-blue-600;

}



.breadcrumb {

  @apply text-sm;

}



.user-menu {

  @apply flex items-center space-x-2 cursor-pointer hover:bg-gray-50 rounded-lg p-2 transition-colors duration-200;

}



.user-name {

  @apply text-sm font-medium text-gray-700;

}



.page-content {

  @apply flex-1 overflow-y-auto p-6;

}



.notifications-content {

  @apply h-full flex flex-col;

}



.empty-notifications {

  @apply flex-1 flex items-center justify-center;

}



.notification-list {

  @apply flex-1 overflow-y-auto;

}



.notification-item {

  @apply flex items-start space-x-3 p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors duration-200;

}



.notification-item.unread {

  @apply bg-blue-50 border-blue-100;

}



.notification-icon {

  @apply flex-shrink-0 mt-1;

}



.notification-content {

  @apply flex-1 min-w-0;

}



.notification-title {

  @apply font-medium text-gray-900 text-sm;

}



.notification-message {

  @apply text-gray-600 text-sm mt-1 line-clamp-2;

}



.notification-time {

  @apply text-gray-400 text-xs mt-2;

}



.notification-actions {

  @apply p-4 border-t border-gray-200 flex justify-between;

}



/* 响应式设?*/

@media (max-width: 768px) {

  .sidebar {

    @apply fixed inset-y-0 left-0 z-50;

  }

  

  .sidebar-collapsed {

    @apply -translate-x-full;

  }

  

  .main-content {

    @apply w-full;

  }

  

  .user-name {

    @apply hidden;

  }

}

</style>

