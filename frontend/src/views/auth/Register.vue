<template>
  <div class="register-container">
    <!-- 页面标题 -->
    <div class="page-title">
      <h2>创建账号</h2>
      <p>加入我们，开始分享您的内容</p>
    </div>

    <!-- 注册表单 -->
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            size="large"
            clearable
            autocomplete="off"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="邮箱地址"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
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

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
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

        <!-- 邮箱验证码 -->
        <el-form-item prop="emailCode">
          <div class="code-container">
            <el-input
              v-model="registerForm.emailCode"
              placeholder="邮箱验证码"
              size="large"
              clearable
              class="code-input"
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
              :disabled="codeCountdown > 0 || sendingCode"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 同意条款 -->
        <el-form-item prop="agreeTerms">
          <el-checkbox v-model="registerForm.agreeTerms">
            我已阅读并同意
            <a href="#" class="terms-link" @click.prevent="showTerms = true">
              服务条款
            </a>
            和
            <a href="#" class="terms-link" @click.prevent="showPrivacy = true">
              隐私政策
            </a>
          </el-checkbox>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            native-type="submit"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

    <!-- 服务条款对话框 -->
    <el-dialog
      v-model="showTerms"
      title="服务条款"
      width="80%"
      max-width="600px"
    >
      <div class="terms-content">
        <h3>1. 服务说明</h3>
        <p>{{ tenantStore.siteTitle }} 是一个内容分享平台，为用户提供内容创作、分享和管理服务。</p>
        
        <h3>2. 用户责任</h3>
        <p>用户应对其发布的内容负责，不得发布违法、有害或侵犯他人权益的内容。</p>
        
        <h3>3. 隐私保护</h3>
        <p>我们承诺保护用户隐私，不会未经授权泄露用户个人信息。</p>
        
        <h3>4. 服务变更</h3>
        <p>我们保留随时修改或终止服务的权利，但会提前通知用户。</p>
      </div>
      <template #footer>
        <el-button @click="showTerms = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog
      v-model="showPrivacy"
      title="隐私政策"
      width="80%"
      max-width="600px"
    >
      <div class="terms-content">
        <h3>1. 信息收集</h3>
        <p>我们仅收集必要的用户信息，包括用户名、邮箱等基本信息。</p>
        
        <h3>2. 信息使用</h3>
        <p>收集的信息仅用于提供服务、改善用户体验和必要的沟通。</p>
        
        <h3>3. 信息保护</h3>
        <p>我们采用行业标准的安全措施保护用户信息安全。</p>
        
        <h3>4. 信息共享</h3>
        <p>除法律要求外，我们不会与第三方共享用户个人信息。</p>
      </div>
      <template #footer>
        <el-button @click="showPrivacy = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// 表单引用
const registerFormRef = ref()

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  emailCode: '',
  agreeTerms: false
})

// 状态
// 状态
const loading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
const showTerms = ref(false)
const showPrivacy = ref(false)
let codeTimer: number | null = null
// 保留兼容字段
const showCaptcha = ref(false)
const captchaUrl = ref('')
const captchaId = ref('')

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
const validatePassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateEmailCode = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱验证码'))
  } else {
    callback()
  }
}

const validateAgreeTerms = (rule: any, value: boolean, callback: any) => {
  if (!value) {
    callback(new Error('请同意服务条款和隐私政策'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  emailCode: [
    { validator: validateEmailCode, trigger: 'blur' }
  ],
  agreeTerms: [
    { validator: validateAgreeTerms, trigger: 'change' }
  ]
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()

    loading.value = true

    await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      emailCode: registerForm.emailCode
    })
    
    ElMessage.success('注册成功，请登录')
    router.push('/auth/login')

  } catch (error) {
    console.error('注册失败:', error)

    const fields = error && typeof error === 'object' ? (error as any).fields : null
    if (fields && typeof fields === 'object') {
      const firstFieldErrors = Object.values(fields)[0] as any
      const message = Array.isArray(firstFieldErrors)
        ? firstFieldErrors[0]?.message
        : firstFieldErrors?.message
      if (message) {
        ElMessage.warning(message)
      }
      return
    }

    const message = (error as any)?.message || '注册失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    loading.value = false
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

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先填写邮箱地址')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.error('请输入有效的邮箱地址')
    return
  }

  try {
    sendingCode.value = true
    await authApi.sendRegisterCode(registerForm.email.trim(), resolvedTenantId.value)
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    sendingCode.value = false
  }
}

/* // 刷新验证码
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
    const config = await userStore.getConfig()
    showCaptcha.value = config.requireCaptcha
    
    if (showCaptcha.value) {
      await refreshCaptcha()
    }
  } catch (error) {
    console.error('获取配置失败:', error)
  }
}) */

onUnmounted(() => {
  if (codeTimer) {
    clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<style scoped>
.register-container {
  width: 100%;
}

.page-title {
  text-align: center;
  margin-bottom: 2rem;
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
}

.register-form {
  margin-bottom: 1.5rem;
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

.terms-link {
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s ease;
}

.terms-link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

.register-button {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-weight: 600;
  transition: all 0.2s ease;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.terms-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 1rem 0;
}

.terms-content h3 {
  color: #1f2937;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 1.5rem 0 0.5rem 0;
}

.terms-content h3:first-child {
  margin-top: 0;
}

.terms-content p {
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 1rem 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 1rem;
  }
  
  .register-card {
    padding: 1.5rem;
  }
  
  .logo h1 {
    font-size: 1.75rem;
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
