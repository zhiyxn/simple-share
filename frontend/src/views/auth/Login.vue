<template>
  <div class="login-container">
    <!-- 页面标题 -->
    <div class="page-title">
      <h2>欢迎回来</h2>
      <p>请登录您的账号</p>
    </div>

    <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <!-- 用户名/邮箱 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            size="large"
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 验证码（如果需要） -->
        <el-form-item v-if="showCaptcha" prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              size="large"
              clearable
              class="captcha-input"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
              <div v-else class="captcha-placeholder">
                <el-icon><Refresh /></el-icon>
                <span>点击刷新</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 记住我和忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <router-link to="/auth/forgot-password" class="forgot-link">
            忘记密码？
          </router-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'

const router = useRouter()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// 表单引用
const loginFormRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
});

// 状态
const loading = ref(false)
const showCaptcha = ref(false)
const captchaUrl = ref('')
const captchaId = ref('')

const resolveTenantId = () => {
  const tenant = tenantStore.tenantId
  if (tenant === null || tenant === undefined) {
    return undefined
  }
  if (typeof tenant === 'number') {
    return tenant
  }
  const parsed = Number(tenant)
  return Number.isNaN(parsed) ? undefined : parsed
}

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe,
      tenantId: resolveTenantId()
    }

    if (showCaptcha.value) {
      loginData.captcha = loginForm.captcha
      loginData.captchaId = captchaId.value
    }

    await userStore.login(loginData)
    
    // 登录成功，静默跳转到首页
    router.push('/')

  } catch (error) {
    console.error('登录失败:', error)
    
    // 如果需要验证码，显示验证码
    if (error.code === 'CAPTCHA_REQUIRED') {
      showCaptcha.value = true
      await refreshCaptcha()
    }
    
    // 不在这里显示错误消息，因为user store和request拦截器已经处理了
  } finally {
    loading.value = false
  }
}

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await userStore.getCaptcha()
    captchaUrl.value = response.image
    captchaId.value = response.id
  } catch (error) {
    console.error('获取验证码失败:', error)
    // 生成模拟验证码
    const code = Math.random().toString(36).substr(2, 4).toUpperCase()
    captchaUrl.value = `data:image/svg+xml;base64,${btoa(`
      <svg width="100" height="40" xmlns="http://www.w3.org/2000/svg">
        <rect width="100" height="40" fill="#f8f9fa" stroke="#dee2e6"/>
        <text x="50%" y="50%" text-anchor="middle" dy=".3em" font-family="Arial" font-size="16" fill="#495057">
          ${code}
        </text>
      </svg>
    `)}`
    captchaId.value = Date.now().toString()
  }
}

// 初始化
onMounted(async () => {
  // 检查是否需要验证码
  try {
    const config = await tenantStore.fetchTenantConfig()
    showCaptcha.value = config.requireCaptcha
    
    if (showCaptcha.value) {
      await refreshCaptcha()
    }
  } catch (error) {
    console.error('获取配置失败:', error)
  }
})
</script>

<style scoped>
.login-container {
  width: 100%;
  max-width: 380px;
  margin: 0 auto;
  padding: 1.5rem;
}

.page-title {
  text-align: center;
  margin-bottom: 1.5rem;
}

.page-title h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-title p {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
}

.login-form {
  width: 100%;
}

.login-form .el-form-item {
  margin-bottom: 1rem;
}

.login-form .el-form-item:last-child {
  margin-bottom: 0;
}

.captcha-container {
  display: flex;
  gap: 0.75rem;
  align-items: end;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 100px;
  height: 40px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
  background: #f9fafb;
}

.captcha-image:hover {
  border-color: #409eff;
  transform: translateY(-1px);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 0.75rem;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  font-size: 0.875rem;
}

.forgot-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: #337ecc;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  height: 44px;
  font-weight: 500;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
    max-width: 100%;
  }
  
  .captcha-container {
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .captcha-image {
    width: 100%;
    height: 44px;
  }
}
</style>
