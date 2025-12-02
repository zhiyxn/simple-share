<template>
  <footer class="footer">
    <div class="footer-content">
      <!-- 主要内容区域 -->
      <div class="footer-main">
        <!-- 品牌信息 -->
        <div class="footer-brand">
          <div class="footer-logo">
            <img 
              v-if="tenantStore.tenantInfo?.logo" 
              :src="tenantStore.tenantInfo.logo" 
              :alt="tenantStore.tenantInfo.name"
              class="footer-logo-image"
            />
            <div v-else class="footer-logo-default">
              <span class="footer-logo-text">{{ siteDefaults.initial }}</span>
            </div>
          </div>
          <div class="footer-site-info">
            <h3 class="footer-site-title">
              {{ tenantStore.tenantInfo?.title || siteDefaults.title }}
            </h3>
            <p class="footer-description">
              {{ tenantStore.tenantInfo?.description || siteDefaults.description }}
            </p>
          </div>
        </div>
        
        <!-- 联系信息 -->
        <div class="footer-contact">
          <h4 class="footer-section-title">联系我们</h4>
          <div class="footer-contact-info">
            <div v-if="tenantStore.tenantInfo?.email" class="contact-item">
              <el-icon class="contact-icon"><Message /></el-icon>
              <span>{{ tenantStore.tenantInfo.email }}</span>
            </div>
            <div v-if="tenantStore.tenantInfo?.phone" class="contact-item">
              <el-icon class="contact-icon"><Phone /></el-icon>
              <span>{{ tenantStore.tenantInfo.phone }}</span>
            </div>
            <div v-if="tenantStore.tenantInfo?.address" class="contact-item">
              <el-icon class="contact-icon"><Location /></el-icon>
              <span>{{ tenantStore.tenantInfo.address }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部信息 -->
      <div class="footer-bottom">
        <div class="footer-bottom-content">
          <!-- 版权信息 -->
          <div class="footer-copyright">
            <span>{{ footerCopyright }}</span>
          </div>
          
          <!-- 备案信息 -->
          <div class="footer-beian" v-if="tenantStore.tenantInfo?.icpBeian || defaultIcpNumber">
            <a 
              v-if="tenantStore.tenantInfo?.icpBeian" 
              :href="`https://beian.miit.gov.cn/`" 
              target="_blank" 
              rel="noopener noreferrer"
              class="beian-link"
            >
              {{ tenantStore.tenantInfo.icpBeian }}
            </a>
            <a 
              v-if="defaultIcpNumber"
              href="https://beian.miit.gov.cn/" 
              target="_blank" 
              rel="noopener noreferrer"
              class="beian-link"
            >
              {{ defaultIcpNumber }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Message, Phone, Location } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { siteDefaults } from '@/config/site'

const tenantStore = useTenantStore()

// 计算属性
const currentYear = computed(() => new Date().getFullYear())

const envFooterCopyright = (import.meta.env.VITE_APP_FOOTER_COPYRIGHT ?? '').trim()
const envFooterIcp = (import.meta.env.VITE_APP_FOOTER_ICP ?? '').trim()
const defaultIcpNumber = envFooterIcp

const footerCopyright = computed(() => {
  if (envFooterCopyright) {
    return envFooterCopyright
  }

  const tenantTitle = tenantStore.tenantInfo?.title?.trim()
  const title = tenantTitle || siteDefaults.title

  return `© ${currentYear.value} ${title}. 保留所有权利.`
})
</script>

<style scoped>
.footer {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: #e2e8f0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem 1rem;
}

/* 主要内容区域 */
.footer-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 3rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 品牌信息 */
.footer-brand {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.footer-logo {
  flex-shrink: 0;
}

.footer-logo-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.footer-logo-default {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-logo-text {
  color: white;
  font-weight: bold;
  font-size: 1.5rem;
}

.footer-site-info {
  flex: 1;
}

.footer-site-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-description {
  margin: 0;
  color: #94a3b8;
  font-size: 0.875rem;
  line-height: 1.5;
}

/* 联系信息 */
.footer-contact {
  flex-shrink: 0;
}

.footer-section-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #f1f5f9;
}

.footer-contact-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #94a3b8;
}

.contact-icon {
  width: 16px;
  height: 16px;
  color: #667eea;
  flex-shrink: 0;
}

/* 底部信息 */
.footer-bottom {
  padding-top: 1rem;
}

.footer-bottom-content {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2rem;
  text-align: center;
}

.footer-copyright {
  font-size: 0.875rem;
  color: #94a3b8;
}

.footer-beian {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.beian-link {
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.beian-link:hover {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 1.5rem 0.75rem 1rem;
  }
  
  .footer-main {
    flex-direction: column;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .footer-brand {
    max-width: none;
  }
  
  .footer-bottom-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .footer-beian {
    justify-content: center;
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .footer-content {
    padding: 1rem 0.75rem;
  }
  
  .footer-main {
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .footer-brand {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 0.75rem;
  }
  
  .footer-contact {
    text-align: center;
  }
  
  .footer-bottom-content {
    gap: 0.75rem;
  }
  
  .footer-beian {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
