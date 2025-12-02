<template>
  <div class="auth-layout">
    <!-- 简洁的背景 -->
    <div class="auth-background">
      <div class="bg-gradient"></div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="auth-container">
      <!-- 简洁的头部 -->
      <div class="auth-header">
        <router-link to="/" class="brand-link">
          <div class="flex items-center space-x-3">
            <img 
              v-if="tenantStore.tenantInfo?.logo" 
              :src="tenantStore.tenantInfo.logo" 
              :alt="tenantStore.tenantInfo.name"
              class="h-8 w-auto"
            />
            <div v-else class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span class="text-white font-bold text-sm">{{ siteDefaults.initial }}</span>
            </div>
            <span class="text-xl font-semibold text-gray-800">
              {{ tenantStore.tenantInfo?.title || siteDefaults.title }}
            </span>
          </div>
        </router-link>
      </div>
      
      <!-- 表单内容区域 -->
      <div class="auth-content">
        <router-view />
      </div>
      
      <!-- 简洁的页脚 -->
      <div class="auth-footer">
        <div class="footer-links">
          <router-link 
            v-if="$route.name !== 'Login'" 
            to="/auth/login" 
            class="footer-link"
          >
            已有账号？立即登录
          </router-link>
          <router-link 
            v-if="$route.name !== 'Register'" 
            to="/auth/register" 
            class="footer-link"
          >
            没有账号？立即注册
          </router-link>
          <router-link 
            v-if="$route.name !== 'ForgotPassword'" 
            to="/auth/forgot-password" 
            class="footer-link"
          >
            忘记密码？
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTenantStore } from '@/stores/tenant'
import { siteDefaults } from '@/config/site'

const tenantStore = useTenantStore()
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.auth-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.1;
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.auth-header {
  padding: 2rem 2rem 1rem;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
}

.brand-link {
  display: inline-flex;
  text-decoration: none;
  transition: opacity 0.2s;
}

.brand-link:hover {
  opacity: 0.8;
}

.auth-content {
  padding: 2rem;
}

.auth-footer {
  padding: 1rem 2rem 2rem;
  border-top: 1px solid #f3f4f6;
  background: #f9fafb;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: center;
}

.footer-link {
  color: #6b7280;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #3b82f6;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .auth-layout {
    padding: 0.5rem;
  }
  
  .auth-container {
    max-width: 100%;
    margin: 0;
    border-radius: 8px;
  }
  
  .auth-header {
    padding: 1.5rem 1.5rem 1rem;
  }
  
  .auth-content {
    padding: 1.5rem;
  }
  
  .auth-footer {
    padding: 1rem 1.5rem 1.5rem;
  }
}

/* 超小屏幕 */
@media (max-width: 480px) {
  .auth-layout {
    padding: 0;
    align-items: stretch;
  }
  
  .auth-container {
    border-radius: 0;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}
</style>
