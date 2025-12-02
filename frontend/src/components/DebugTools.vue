<template>
  <div v-if="showDebugTools" class="debug-tools">
    <!-- 调试工具按钮 -->
    <div class="debug-toggle" @click="toggleDebugPanel">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
      </svg>
      <span>调试</span>
    </div>

    <!-- 调试面板 -->
    <div v-if="debugPanelVisible" class="debug-panel">
      <div class="debug-header">
        <h3>调试工具</h3>
        <button @click="toggleDebugPanel" class="close-btn">×</button>
      </div>
      
      <div class="debug-content">
        <!-- 页面信息 -->
        <div class="debug-section">
          <h4>页面信息</h4>
          <div class="info-item">
            <span>当前路由:</span>
            <code>{{ currentRoute }}</code>
          </div>
          <div class="info-item">
            <span>页面标题:</span>
            <code>{{ pageTitle }}</code>
          </div>
          <div class="info-item">
            <span>用户代理:</span>
            <code class="user-agent">{{ userAgent }}</code>
          </div>
        </div>

        <!-- 状态管理 -->
        <div class="debug-section">
          <h4>状态管理</h4>
          <button @click="showStoreState" class="debug-btn">查看Store状态</button>
          <button @click="clearLocalStorage" class="debug-btn">清除本地存储</button>
          <button @click="clearSessionStorage" class="debug-btn">清除会话存储</button>
        </div>

        <!-- 网络请求 -->
        <div class="debug-section">
          <h4>网络调试</h4>
          <button @click="toggleNetworkLog" class="debug-btn">
            {{ networkLogEnabled ? '关闭' : '开启' }}网络日志
          </button>
          <button @click="showNetworkRequests" class="debug-btn">查看请求历史</button>
        </div>

        <!-- 控制台工具 -->
        <div class="debug-section">
          <h4>控制台工具</h4>
          <button @click="toggleVConsole" class="debug-btn">
            {{ vConsoleVisible ? '关闭' : '开启' }}移动端控制台
          </button>
          <button @click="toggleEruda" class="debug-btn">
            {{ erudaVisible ? '关闭' : '开启' }}桌面端控制台
          </button>
        </div>

        <!-- 性能监控 -->
        <div class="debug-section">
          <h4>性能监控</h4>
          <button @click="showPerformanceInfo" class="debug-btn">查看性能信息</button>
          <button @click="measurePageLoad" class="debug-btn">测量页面加载时间</button>
        </div>

        <!-- 环境信息 -->
        <div class="debug-section">
          <h4>环境信息</h4>
          <div class="info-item">
            <span>环境:</span>
            <code>{{ environment }}</code>
          </div>
          <div class="info-item">
            <span>API地址:</span>
            <code>{{ apiBaseUrl }}</code>
          </div>
          <div class="info-item">
            <span>构建时间:</span>
            <code>{{ buildTime }}</code>
          </div>
        </div>
      </div>
    </div>

    <!-- 网络请求日志 -->
    <div v-if="networkLogVisible" class="network-log">
      <div class="log-header">
        <h4>网络请求日志</h4>
        <button @click="clearNetworkLog" class="clear-btn">清除</button>
        <button @click="networkLogVisible = false" class="close-btn">×</button>
      </div>
      <div class="log-content">
        <div v-for="(request, index) in networkRequests" :key="index" class="request-item">
          <div class="request-method" :class="request.method.toLowerCase()">
            {{ request.method }}
          </div>
          <div class="request-url">{{ request.url }}</div>
          <div class="request-status" :class="getStatusClass(request.status)">
            {{ request.status }}
          </div>
          <div class="request-time">{{ request.time }}ms</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 响应式数据
const debugPanelVisible = ref(false)
const networkLogVisible = ref(false)
const networkLogEnabled = ref(false)
const vConsoleVisible = ref(false)
const erudaVisible = ref(false)
const networkRequests = ref<any[]>([])

// 计算属性
const showDebugTools = computed(() => {
  return import.meta.env.VITE_SHOW_DEBUG_TOOLS === 'true' || import.meta.env.DEV
})

const currentRoute = computed(() => route.fullPath)
const pageTitle = computed(() => document.title)
const userAgent = computed(() => navigator.userAgent)
const environment = computed(() => import.meta.env.MODE)
const apiBaseUrl = computed(() => import.meta.env.VITE_API_BASE_URL)
const buildTime = computed(() => new Date().toLocaleString())

// 方法
const toggleDebugPanel = () => {
  debugPanelVisible.value = !debugPanelVisible.value
}

const showStoreState = () => {
  console.group('🏪 Store State')
  // 这里可以根据你使用的状态管理库来获取状态
  console.log('Pinia stores:', window.__PINIA__)
  console.groupEnd()
}

const clearLocalStorage = () => {
  localStorage.clear()
  console.log('✅ LocalStorage cleared')
  alert('本地存储已清除')
}

const clearSessionStorage = () => {
  sessionStorage.clear()
  console.log('✅ SessionStorage cleared')
  alert('会话存储已清除')
}

const toggleNetworkLog = () => {
  networkLogEnabled.value = !networkLogEnabled.value
  if (networkLogEnabled.value) {
    setupNetworkInterceptor()
  }
}

const showNetworkRequests = () => {
  networkLogVisible.value = true
}

const clearNetworkLog = () => {
  networkRequests.value = []
}

const toggleVConsole = async () => {
  if (!vConsoleVisible.value) {
    const VConsole = (await import('vconsole')).default
    window.vConsole = new VConsole()
    vConsoleVisible.value = true
  } else {
    if (window.vConsole) {
      window.vConsole.destroy()
      window.vConsole = null
    }
    vConsoleVisible.value = false
  }
}

const toggleEruda = async () => {
  if (!erudaVisible.value) {
    const eruda = await import('eruda')
    eruda.default.init()
    erudaVisible.value = true
  } else {
    if (window.eruda) {
      window.eruda.destroy()
    }
    erudaVisible.value = false
  }
}

const showPerformanceInfo = () => {
  const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
  console.group('⚡ Performance Info')
  console.log('DNS查询时间:', perfData.domainLookupEnd - perfData.domainLookupStart, 'ms')
  console.log('TCP连接时间:', perfData.connectEnd - perfData.connectStart, 'ms')
  console.log('请求响应时间:', perfData.responseEnd - perfData.requestStart, 'ms')
  console.log('DOM解析时间:', perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart, 'ms')
  console.log('页面加载完成时间:', perfData.loadEventEnd - perfData.loadEventStart, 'ms')
  console.groupEnd()
}

const measurePageLoad = () => {
  const loadTime = performance.now()
  console.log(`📊 当前页面加载时间: ${loadTime.toFixed(2)}ms`)
  alert(`页面加载时间: ${loadTime.toFixed(2)}ms`)
}

const getStatusClass = (status: number) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 400 && status < 500) return 'client-error'
  if (status >= 500) return 'server-error'
  return 'info'
}

const setupNetworkInterceptor = () => {
  // 拦截fetch请求
  const originalFetch = window.fetch
  window.fetch = async (...args) => {
    const startTime = performance.now()
    const url = args[0] as string
    const options = args[1] || {}
    
    try {
      const response = await originalFetch(...args)
      const endTime = performance.now()
      
      networkRequests.value.unshift({
        method: options.method || 'GET',
        url,
        status: response.status,
        time: Math.round(endTime - startTime),
        timestamp: new Date().toLocaleTimeString()
      })
      
      // 只保留最近50个请求
      if (networkRequests.value.length > 50) {
        networkRequests.value = networkRequests.value.slice(0, 50)
      }
      
      return response
    } catch (error) {
      const endTime = performance.now()
      networkRequests.value.unshift({
        method: options.method || 'GET',
        url,
        status: 0,
        time: Math.round(endTime - startTime),
        timestamp: new Date().toLocaleTimeString(),
        error: true
      })
      throw error
    }
  }
}

// 生命周期
onMounted(() => {
  // 添加全局错误处理
  window.addEventListener('error', (event) => {
    console.error('🚨 Global Error:', event.error)
  })
  
  window.addEventListener('unhandledrejection', (event) => {
    console.error('🚨 Unhandled Promise Rejection:', event.reason)
  })
})

onUnmounted(() => {
  // 清理
  if (window.vConsole) {
    window.vConsole.destroy()
  }
  if (window.eruda) {
    window.eruda.destroy()
  }
})

// 全局声明
declare global {
  interface Window {
    vConsole: any
    eruda: any
    __PINIA__: any
  }
}
</script>

<style scoped>
.debug-tools {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  font-family: 'Courier New', monospace;
}

.debug-toggle {
  background: #2563eb;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;
}

.debug-toggle:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.debug-panel {
  position: absolute;
  top: 40px;
  right: 0;
  width: 400px;
  max-height: 600px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.debug-header {
  background: #f3f4f6;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.debug-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.debug-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
}

.debug-section {
  margin-bottom: 20px;
}

.debug-section h4 {
  margin: 0 0 8px 0;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
  font-size: 11px;
}

.info-item span {
  color: #6b7280;
  min-width: 80px;
}

.info-item code {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  max-width: 250px;
  word-break: break-all;
}

.user-agent {
  max-width: 200px !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.debug-btn {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  margin-right: 8px;
  margin-bottom: 6px;
  transition: all 0.2s;
}

.debug-btn:hover {
  background: #e5e7eb;
}

.network-log {
  position: absolute;
  top: 40px;
  right: 420px;
  width: 500px;
  max-height: 400px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.log-header {
  background: #f3f4f6;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.clear-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 11px;
  cursor: pointer;
  margin-right: 8px;
}

.log-content {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 11px;
}

.request-method {
  min-width: 50px;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
  text-align: center;
  margin-right: 8px;
}

.request-method.get { background: #10b981; color: white; }
.request-method.post { background: #3b82f6; color: white; }
.request-method.put { background: #f59e0b; color: white; }
.request-method.delete { background: #ef4444; color: white; }

.request-url {
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-status {
  min-width: 40px;
  text-align: center;
  margin-right: 8px;
  font-weight: 600;
}

.request-status.success { color: #10b981; }
.request-status.client-error { color: #f59e0b; }
.request-status.server-error { color: #ef4444; }

.request-time {
  min-width: 50px;
  text-align: right;
  color: #6b7280;
}
</style>