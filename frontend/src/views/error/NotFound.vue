<template>
  <div class="not-found">
    <div class="error-container">
      <div class="error-illustration">
        <div class="error-code">404</div>
        <div class="error-icon">
          <el-icon size="120"><DocumentDelete /></el-icon>
        </div>
      </div>
      
      <div class="error-content">
        <h1 class="error-title">页面未找到</h1>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
        </p>
        
        <div class="error-actions">
          <el-button type="primary" size="large" @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" @click="goBack">
            <el-icon><Back /></el-icon>
            返回上页
          </el-button>
        </div>
        
        <div class="helpful-links">
          <h3>您可能想要：</h3>
          <ul>
            <li><a @click="$router.push('/articles')">浏览文章</a></li>
            <li><a @click="$router.push('/user/profile')">个人中心</a></li>
            <li><a @click="$router.push('/admin')">管理后台</a></li>
          </ul>
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
import { useRouter } from 'vue-router'
import { DocumentDelete, HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

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
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  max-width: 600px;
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
  color: #f56c6c;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
  background: linear-gradient(45deg, #f56c6c, #ff8a80);
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

.helpful-links {
  text-align: left;
  background: #f8f9fa;
  padding: 24px;
  border-radius: 12px;
  border-left: 4px solid #409eff;
}

.helpful-links h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.helpful-links ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.helpful-links li {
  margin-bottom: 8px;
}

.helpful-links a {
  color: #409eff;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s ease;
}

.helpful-links a:hover {
  color: #66b1ff;
  text-decoration: underline;
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
  .not-found {
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
  
  .helpful-links {
    padding: 16px;
  }
}
</style>