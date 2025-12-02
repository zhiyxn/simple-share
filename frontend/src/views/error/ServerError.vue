<template>
  <div class="server-error">
    <div class="error-container">
      <div class="error-illustration">
        <div class="error-code">500</div>
        <div class="error-icon">
          <el-icon size="120"><Warning /></el-icon>
        </div>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">服务器错误</h1>
        <p class="error-description">
          服务器遇到了一个错误，无法完成您的请求。我们正在努力修复这个问题。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" size="large" @click="refreshPage">
            <el-icon><Refresh /></el-icon>
            刷新页面
          </el-button>
          <el-button size="large" @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" @click="goBack">
            <el-icon><Back /></el-icon>
            返回上页
          </el-button>
        </div>
        
        <div class="error-details" v-if="showDetails">
          <h3>错误详情：</h3>
          <div class="error-info">
            <p><strong>错误时间：</strong>{{ errorTime }}</p>
            <p><strong>错误ID：</strong>{{ errorId }}</p>
            <p v-if="errorMessage"><strong>错误信息：</strong>{{ errorMessage }}</p>
          </div>
        </div>
        
        <div class="helpful-info">
          <h3>可能的解决方案：</h3>
          <ul>
            <li>刷新页面重试</li>
            <li>检查网络连接</li>
            <li>稍后再试</li>
            <li>联系技术支持</li>
          </ul>
          
          <div class="contact-info">
            <p>如果问题持续存在，请联系我们：</p>
            <p>📧 support@simpleshare.com</p>
            <p>📞 400-123-4567</p>
          </div>
        </div>
        
        <div class="toggle-details">
          <el-button 
            type="text" 
            size="small" 
            @click="showDetails = !showDetails"
          >
            {{ showDetails ? '隐藏' : '显示' }}错误详情
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 装饰性背景 -->
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Warning, Refresh, HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const showDetails = ref(false)
const errorTime = ref('')
const errorId = ref('')
const errorMessage = ref('')

const refreshPage = () => {
  window.location.reload()
}

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

const generateErrorId = () => {
  return 'ERR-' + Date.now().toString(36).toUpperCase() + '-' + Math.random().toString(36).substr(2, 5).toUpperCase()
}

onMounted(() => {
  errorTime.value = new Date().toLocaleString('zh-CN')
  errorId.value = generateErrorId()
  
  // 从路由参数或查询参数中获取错误信息
  if (route.query.message) {
    errorMessage.value = route.query.message
  }
})
</script>

<style scoped>
.server-error {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffa726 0%, #ff7043 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.error-container {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 700px;
  width: 100%;
  position: relative;
  z-index: 2;
}

.error-illustration {
  margin-bottom: 40px;
  position: relative;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: #ffa726;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 8px rgba(255, 167, 38, 0.3);
  background: linear-gradient(45deg, #ffa726, #ffb74d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-icon {
  color: #909399;
  opacity: 0.6;
}

.error-content {
  margin-bottom: 40px;
}

.error-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.error-description {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.error-details {
  text-align: left;
  background: #fff3e0;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
  border-left: 4px solid #ffa726;
}

.error-details h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.error-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.helpful-info {
  text-align: left;
  background: #f8f9fa;
  padding: 24px;
  border-radius: 12px;
  border-left: 4px solid #ffa726;
  margin-bottom: 20px;
}

.helpful-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.helpful-info ul {
  list-style: none;
  padding: 0;
  margin: 0 0 20px 0;
}

.helpful-info li {
  margin-bottom: 8px;
  color: #606266;
  position: relative;
  padding-left: 16px;
}

.helpful-info li::before {
  content: '•';
  color: #ffa726;
  position: absolute;
  left: 0;
  font-weight: bold;
}

.contact-info {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.contact-info p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

.toggle-details {
  margin-top: 20px;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 80px;
  height: 80px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-container {
    padding: 40px 24px;
    margin: 20px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 100%;
    max-width: 200px;
  }
}

@media (max-width: 480px) {
  .server-error {
    padding: 10px;
  }
  
  .error-container {
    padding: 30px 20px;
  }
  
  .error-code {
    font-size: 60px;
  }
  
  .error-title {
    font-size: 20px;
  }
  
  .helpful-info {
    padding: 16px;
  }
  
  .contact-info {
    padding: 12px;
  }
}
</style>