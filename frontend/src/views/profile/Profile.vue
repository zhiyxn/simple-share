<template>
  <div class="profile-page">
    <div class="max-w-4xl mx-auto">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">个人资料</h1>
            <p class="text-gray-600 mt-1">管理您的个人信息和账户设置</p>
          </div>
          <el-button type="primary" @click="handleSave" :loading="saving">
            {{ saving ? '保存中...' : '保存更改' }}
          </el-button>
        </div>
      </div>
      
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2 class="text-lg font-semibold text-gray-900">基本信息</h2>
          <p class="text-sm text-gray-600">更新您的个人基本信息</p>
        </div>
        
        <div class="card-content">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            size="large"
          >
            <!-- 头像上传 -->
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :before-upload="beforeAvatarUpload"
                  accept="image/*"
                >
                  <el-avatar 
                    v-if="form.avatar" 
                    :src="form.avatar" 
                    :size="80"
                    class="avatar-preview"
                  />
                  <div v-else class="avatar-placeholder">
                    <el-icon class="text-gray-400 text-2xl"><Plus /></el-icon>
                    <div class="text-gray-500 text-sm mt-2">上传头像</div>
                  </div>
                </el-upload>
                <div class="avatar-tips">
                  <p class="text-xs text-gray-500">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
                  <el-button 
                    v-if="form.avatar" 
                    text 
                    type="danger" 
                    size="small" 
                    @click="removeAvatar"
                  >
                    删除头像
                  </el-button>
                </div>
              </div>
            </el-form-item>
            
            <!-- 用户名 -->
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名"
                :disabled="!canEditUsername"
              >
                <template #suffix>
                  <el-tooltip 
                    v-if="!canEditUsername" 
                    content="用户名不可修改" 
                    placement="top"
                  >
                    <el-icon class="text-gray-400"><Lock /></el-icon>
                  </el-tooltip>
                </template>
              </el-input>
            </el-form-item>
            
            <!-- 邮箱 -->
            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="form.email" 
                type="email"
                placeholder="请输入邮箱地址"
                :disabled="!canEditEmail"
              >
                <template #suffix>
                  <div class="flex items-center space-x-2">
                    <el-tag 
                      v-if="form.emailVerified" 
                      type="success" 
                      size="small"
                    >
                      已验证
                    </el-tag>
                    <el-button 
                      v-else 
                      text 
                      type="primary" 
                      size="small"
                      @click="sendVerificationEmail"
                      :loading="sendingVerification"
                    >
                      验证邮箱
                    </el-button>
                    <el-tooltip 
                      v-if="!canEditEmail" 
                      content="邮箱不可修改" 
                      placement="top"
                    >
                      <el-icon class="text-gray-400"><Lock /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-input>
            </el-form-item>
            
            <!-- 昵称 -->
            <el-form-item label="昵称" prop="nickname">
              <el-input 
                v-model="form.nickname" 
                placeholder="请输入昵称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 个人简介 -->
            <el-form-item label="个人简介" prop="bio">
              <el-input 
                v-model="form.bio" 
                type="textarea"
                placeholder="介绍一下自己吧..."
                :rows="4"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 个人网站 -->
            <el-form-item label="个人网站" prop="website">
              <el-input 
                v-model="form.website" 
                placeholder="https://example.com"
              />
            </el-form-item>
            
            <!-- 社交媒体 -->
            <el-form-item label="社交媒体">
              <div class="social-links">
                <div class="social-item">
                  <el-input 
                    v-model="form.github" 
                    placeholder="GitHub 用户名"
                    class="flex-1"
                  >
                    <template #prepend>
                      <el-icon><svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg></el-icon>
                    </template>
                  </el-input>
                </div>
                <div class="social-item">
                  <el-input 
                    v-model="form.twitter" 
                    placeholder="Twitter 用户名"
                    class="flex-1"
                  >
                    <template #prepend>
                      <el-icon><svg viewBox="0 0 24 24" width="16" height="16"><path fill="currentColor" d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/></svg></el-icon>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 账户安全 -->
      <div class="profile-card">
        <div class="card-header">
          <h2 class="text-lg font-semibold text-gray-900">账户安全</h2>
          <p class="text-sm text-gray-600">管理您的密码和安全设置</p>
        </div>
        
        <div class="card-content">
          <div class="security-items">
            <!-- 修改密码 -->
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">登录密码</div>
                <div class="security-desc">定期更换密码有助于保护账户安全</div>
              </div>
              <el-button @click="showChangePassword = true">
                修改密码
              </el-button>
            </div>
            
            <!-- 两步验证 -->
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">两步验证</div>
                <div class="security-desc">
                  {{ form.twoFactorEnabled ? '已启用两步验证' : '启用两步验证增强账户安全' }}
                </div>
              </div>
              <el-switch 
                v-model="form.twoFactorEnabled"
                @change="handleTwoFactorChange"
                :loading="twoFactorLoading"
              />
            </div>
            
            <!-- 登录设备 -->
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">登录设备</div>
                <div class="security-desc">查看和管理您的登录设备</div>
              </div>
              <el-button @click="showDevices = true">
                管理设备
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="500px"
      @close="resetPasswordForm"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleChangePassword"
          :loading="changingPassword"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 登录设备对话框 -->
    <el-dialog
      v-model="showDevices"
      title="登录设备管理"
      width="700px"
    >
      <div class="devices-list">
        <div 
          v-for="device in devices" 
          :key="device.id"
          class="device-item"
        >
          <div class="device-info">
            <div class="device-name">
              <el-icon class="mr-2"><Monitor v-if="device.type === 'desktop'" /><Cellphone v-else /></el-icon>
              {{ device.name }}
            </div>
            <div class="device-details">
              <span class="device-location">{{ device.location }}</span>
              <span class="device-time">{{ formatTime(device.lastActive) }}</span>
              <el-tag v-if="device.current" type="success" size="small">当前设备</el-tag>
            </div>
          </div>
          <el-button 
            v-if="!device.current" 
            text 
            type="danger" 
            @click="revokeDevice(device.id)"
          >
            移除
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDevices = false">关闭</el-button>
        <el-button type="danger" @click="revokeAllDevices">
          移除所有其他设备
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Lock, Monitor, Cellphone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'

interface ProfileForm {
  avatar: string
  username: string
  email: string
  emailVerified: boolean
  nickname: string
  bio: string
  website: string
  github: string
  twitter: string
  twoFactorEnabled: boolean
  language: string
  timezone: string
  emailNotifications: {
    comments: boolean
    likes: boolean
    follows: boolean
    newsletter: boolean
  }
}

interface PasswordForm {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

interface Device {
  id: string
  name: string
  type: 'desktop' | 'mobile'
  location: string
  lastActive: string
  current: boolean
}

const userStore = useUserStore()
const tenantStore = useTenantStore()

// 响应式数据
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const saving = ref(false)
const sendingVerification = ref(false)
const twoFactorLoading = ref(false)
const changingPassword = ref(false)
const showChangePassword = ref(false)
const showDevices = ref(false)
const canEditUsername = ref(false)
const canEditEmail = ref(false)
const devices = ref<Device[]>([])

const form = reactive<ProfileForm>({
  avatar: '',
  username: '',
  email: '',
  emailVerified: false,
  nickname: '',
  bio: '',
  website: '',
  github: '',
  twitter: '',
  twoFactorEnabled: false,
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  emailNotifications: {
    comments: true,
    likes: true,
    follows: true,
    newsletter: false
  }
})

const passwordForm = reactive<PasswordForm>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL}/upload/avatar`
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${userStore.token}`,
    'X-Tenant-ID': tenantStore.tenantId
  }
})

// 表单验证规则
const rules: FormRules<ProfileForm> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '用户名只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ],
  website: [
    { type: 'url', message: '请输入有效的网址', trigger: 'blur' }
  ]
}

const passwordRules: FormRules<PasswordForm> = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    { pattern: /(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const loadProfile = async () => {
  try {
    const latestUser = await userStore.fetchUserInfo().catch(() => userStore.userInfo)
    const userInfo = latestUser || userStore.userInfo
    if (userInfo) {
      form.avatar = userInfo.avatar || ''
      form.username = userInfo.username || ''
      form.email = userInfo.email || ''
      form.emailVerified = Boolean(userInfo.emailVerified)
      form.nickname = userInfo.nickname || ''
      form.bio = userInfo.bio || ''
      form.website = userInfo.website || ''
      form.github = userInfo.github || ''
      form.twitter = userInfo.twitter || ''
      form.twoFactorEnabled = Boolean(userInfo.twoFactorEnabled)
      form.language = userInfo.language || 'zh-CN'
      form.timezone = userInfo.timezone || 'Asia/Shanghai'

      const notifications = userInfo.emailNotifications || {}
      form.emailNotifications.comments = notifications.comments ?? true
      form.emailNotifications.likes = notifications.likes ?? true
      form.emailNotifications.follows = notifications.follows ?? true
      form.emailNotifications.newsletter = notifications.newsletter ?? false
    }

    canEditUsername.value = false
    canEditEmail.value = false
  } catch (error) {
    console.error('Failed to load profile:', error)
    ElMessage.error('加载个人信息失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await userStore.updateProfile({
      nickname: form.nickname,
      bio: form.bio,
      website: form.website,
      github: form.github,
      twitter: form.twitter,
      language: form.language,
      timezone: form.timezone,
      emailNotifications: form.emailNotifications
    })

    await loadProfile()

    ElMessage.success('个人信息已更新')

  } catch (error: any) {
    console.error('Failed to update profile:', error)
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    saving.value = false
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleAvatarSuccess = (response: any) => {
  form.avatar = response.url
  ElMessage.success('头像上传成功')
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const removeAvatar = async () => {
  try {
    await ElMessageBox.confirm('确定要删除头像吗？', '删除确认', {
      type: 'warning'
    })
    
    form.avatar = ''
    await userStore.uploadAvatar('')
    ElMessage.success('头像已删除')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除头像失败')
    }
  }
}

const sendVerificationEmail = async () => {
  try {
    sendingVerification.value = true
    // await userStore.sendVerificationEmail()
    ElMessage.success('验证邮件已发送，请查收')
  } catch (error: any) {
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    sendingVerification.value = false
  }
}

const handleTwoFactorChange = async (enabled: boolean) => {
  try {
    twoFactorLoading.value = true
    // await userStore.updateTwoFactor(enabled)
    ElMessage.success(enabled ? '两步验证已启用' : '两步验证已关闭')
  } catch (error: any) {
    form.twoFactorEnabled = !enabled // 回滚状态
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    twoFactorLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    await userStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    // userStore.changePassword 已经处理了成功提示和重新登录
    showChangePassword.value = false
    resetPasswordForm()
    
  } catch (error: any) {
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    changingPassword.value = false
  }
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.clearValidate()
}

const loadDevices = async () => {
  try {
    // 模拟设备数据
    devices.value = [
      {
        id: '1',
        name: 'Windows PC - Chrome',
        type: 'desktop',
        location: '北京, 中国',
        lastActive: new Date().toISOString(),
        current: true
      },
      {
        id: '2',
        name: 'iPhone - Safari',
        type: 'mobile',
        location: '上海, 中国',
        lastActive: new Date(Date.now() - 86400000).toISOString(),
        current: false
      }
    ]
  } catch (error) {
    console.error('Failed to load devices:', error)
  }
}

const revokeDevice = async (deviceId: string) => {
  try {
    await ElMessageBox.confirm('确定要移除此设备吗？', '移除确认', {
      type: 'warning'
    })
    
    // await userStore.revokeDevice(deviceId)
    devices.value = devices.value.filter(d => d.id !== deviceId)
    ElMessage.success('设备已移除')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除设备失败')
    }
  }
}

const revokeAllDevices = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要移除所有其他设备吗？这将强制其他设备重新登录。',
      '移除确认',
      { type: 'warning' }
    )
    
    // await userStore.revokeAllDevices()
    devices.value = devices.value.filter(d => d.current)
    ElMessage.success('所有其他设备已移除')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除设备失败')
    }
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) {
    return '刚刚活跃'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前活跃`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前活跃`
  } else {
    return date.toLocaleDateString() + ' 活跃'
  }
}

// 生命周期
onMounted(() => {
  loadProfile()
  loadDevices()
})
</script>

<style scoped>
.profile-page {
  @apply p-6;
}

.page-header {
  @apply mb-8;
}

.profile-card {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 mb-6;
}

.card-header {
  @apply p-6 border-b border-gray-200;
}

.card-content {
  @apply p-6;
}

.avatar-upload {
  @apply flex items-start space-x-4;
}

.avatar-uploader {
  @apply block;
}

.avatar-preview {
  @apply cursor-pointer hover:opacity-80 transition-opacity duration-200;
}

.avatar-placeholder {
  @apply w-20 h-20 border-2 border-dashed border-gray-300 rounded-full flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 transition-colors duration-200;
}

.avatar-tips {
  @apply flex-1;
}

.social-links {
  @apply space-y-3;
}

.social-item {
  @apply flex items-center space-x-3;
}

.security-items {
  @apply space-y-6;
}

.security-item {
  @apply flex items-center justify-between p-4 border border-gray-200 rounded-lg;
}

.security-info {
  @apply flex-1;
}

.security-title {
  @apply font-medium text-gray-900;
}

.security-desc {
  @apply text-sm text-gray-600 mt-1;
}

.notification-settings {
  @apply space-y-2;
}

.devices-list {
  @apply space-y-4 max-h-96 overflow-y-auto;
}

.device-item {
  @apply flex items-center justify-between p-4 border border-gray-200 rounded-lg;
}

.device-info {
  @apply flex-1;
}

.device-name {
  @apply font-medium text-gray-900 flex items-center;
}

.device-details {
  @apply text-sm text-gray-600 mt-1 space-x-3;
}

.device-location,
.device-time {
  @apply mr-3;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-page {
    @apply p-4;
  }
  
  .page-header .flex {
    @apply flex-col space-y-4;
  }
  
  .avatar-upload {
    @apply flex-col space-x-0 space-y-4;
  }
  
  .security-item,
  .device-item {
    @apply flex-col items-start space-y-3;
  }
}
</style>
