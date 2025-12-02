import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import directives from './directives'
import { useUserStore } from './stores/user'
import './styles/index.css'

// 引入调试工具
import './utils/debug'

const app = createApp(App)

// 配置Pinia
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)

// 初始化用户store
const userStore = useUserStore()
userStore.init()

// 配置Element Plus
app.use(ElementPlus)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册自定义指令
app.use(directives)

app.use(router)

app.mount('#app')
