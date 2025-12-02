<template>
  <div class="forgot-password-container">
    <!-- 页面标题 -->
    <div class="page-title">
      <h2>找回密码</h2>
      <p>请输入您的邮箱地址重置密码</p>
    </div>

    <!-- 找回密码表单 -->
    <el-form
      ref="forgotFormRef"
      :model="forgotForm"
      :rules="forgotRules"
      class="forgot-form"
      @submit.prevent="handleSubmit"
    >
      <!-- 邮箱 -->
      <el-form-item prop="email">
        <el-input
          v-model="forgotForm.email"
          type="email"
          placeholder="邮箱地址"
          size="large"
          clearable
          autocomplete="off"
          @keyup.enter="handleSubmit"
        >
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 验证码（如果需要） -->
      <el-form-item v-if="showCaptcha" prop="captcha">
        <div class="captcha-container">
          <el-input
            v-model="forgotForm.captcha"
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

      <!-- 邮箱验证码 -->
      <el-form-item prop="emailCode">
        <div class="code-container">
          <el-input
            v-model="forgotForm.emailCode"
            placeholder="邮箱验证码"
            size="large"
            clearable
            class="code-input"
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <el-button
            class="code-button"
            type="primary"
            size="large"
            plain
            :loading="sendingCode"
            :disabled="codeCountdown > 0 || sendingCode || !forgotForm.email"
            @click="handleSendCode"
          >
            {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item prop="newPassword">
        <el-input
          v-model="forgotForm.newPassword"
          type="password"
          placeholder="新密码"
          size="large"
          show-password
          clearable
          autocomplete="new-password"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 确认新密码 -->
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="forgotForm.confirmPassword"
          type="password"
          placeholder="确认新密码"
          size="large"
          show-password
          clearable
          autocomplete="new-password"
          @keyup.enter="handleSubmit"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="submit-button"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '重置中...' : '重置密码' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 返回登录 -->
    <div class="back-to-login">
      <router-link to="/auth/login" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
        返回登录
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'

const router = useRouter()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// 表单引用
const forgotFormRef = ref()

// 表单数据
const forgotForm = reactive({
  email: '',
  captcha: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 状态
const loading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
const showCaptcha = ref(false)
const captchaUrl = ref('')
const captchaId = ref('')

let codeTimer = null

const resolvedTenantId = computed(() => {
  const tenant = tenantStore.tenantId
  if (tenant === null || tenant === undefined) {
    return undefined
  }
  if (typeof tenant === 'number') {
    return tenant
  }
  const parsed = Number(tenant)
  return Number.isNaN(parsed) ? undefined : parsed
})

// 自定义验证器
const validateEmail = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入邮箱地址'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认新密码'))
  } else if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateEmailCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱验证码'))
  } else {
    callback()
  }
}

// 表单验证规则
const forgotRules = {
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  captcha: showCaptcha.value ? [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ] : [],
  emailCode: [
    { validator: validateEmailCode, trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  if (!forgotForm.email) {
    ElMessage.warning('请先填写邮箱地址')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(forgotForm.email)) {
    ElMessage.error('请输入有效的邮箱地址')
    return
  }

  if (showCaptcha.value && !forgotForm.captcha) {
    ElMessage.error('请输入验证码')
    return
  }

  try {
    sendingCode.value = true
    const requestData = {
      email: forgotForm.email.trim(),
      tenantId: resolvedTenantId.value
    }
    if (showCaptcha.value) {
      requestData.captcha = forgotForm.captcha
      requestData.captchaId = captchaId.value
    }
    await authApi.sendResetCode(requestData)
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    
    // 如果需要验证码，显示验证码
    if (error.code === 'CAPTCHA_REQUIRED') {
      showCaptcha.value = true
      await refreshCaptcha()
    }
    
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    sendingCode.value = false
  }
}

// 提交重置密码
const handleSubmit = async () => {
  if (!forgotFormRef.value) return

  try {
    await forgotFormRef.value.validate()

    if (showCaptcha.value && !forgotForm.captcha) {
      ElMessage.error('请输入验证码')
      return
    }

    loading.value = true

    const resetData = {
      email: forgotForm.email.trim(),
      code: forgotForm.emailCode,
      password: forgotForm.newPassword,
      confirmPassword: forgotForm.confirmPassword,
      tenantId: resolvedTenantId.value
    }

    // 如果需要验证码，添加验证码参数
    if (showCaptcha.value) {
      resetData.captcha = forgotForm.captcha
      resetData.captchaId = captchaId.value
    }

    await userStore.resetPassword(resetData)
    
    ElMessage.success('密码重置成功，请登录')
    router.push('/auth/login')

  } catch (error) {
    console.error('重置密码失败:', error)
    
    // 如果需要验证码，显示验证码
    if (error.code === 'CAPTCHA_REQUIRED') {
      showCaptcha.value = true
      await refreshCaptcha()
    }
    
    // 不在这里显示错误消息，因为request拦截器已经显示了
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

const startCountdown = () => {
  codeCountdown.value = 60
  if (codeTimer) {
    clearInterval(codeTimer)
  }
  codeTimer = setInterval(() => {
    if (codeCountdown.value > 0) {
      codeCountdown.value -= 1
    } else if (codeTimer) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

// 加载配置
const loadConfig = async () => {
  try {
    // 加载配置，判断是否需要验证码
    const config = await authApi.getConfig()
    showCaptcha.value = config.requireCaptcha
    
    if (showCaptcha.value) {
      await refreshCaptcha()
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    // 默认不需要验证码
    showCaptcha.value = false
  }
}

// 生命周期
onMounted(async () => {
  await loadConfig()
  
  // 如果已经登录，重定向到首页
  if (userStore.isLoggedIn) {
    router.push('/')
  }
})

onUnmounted(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<style scoped>
.forgot-password-container {
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

.forgot-form {
  width: 100%;
}

.forgot-form .el-form-item {
  margin-bottom: 1rem;
}

.forgot-form .el-form-item:last-child {
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

.code-container {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.code-input {
  flex: 1;
}

.code-button {
  min-width: 120px;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-weight: 500;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.back-to-login {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.875rem;
  transition: color 0.2s ease;
}

.back-link:hover {
  color: #5a67d8;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .forgot-password-container {
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
  
  .code-container {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }
  
  .code-button {
    width: 100%;
  }
}
</style>
