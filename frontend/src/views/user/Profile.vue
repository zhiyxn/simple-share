<template>
  <div class="profile-container">
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-description">管理您的个人信息并保持资料最新</p>
    </div>

    <div class="profile-content">
      <el-card class="profile-card" shadow="hover" v-loading="loadingProfile">
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar :size="80" :src="avatar" class="user-avatar">
              <el-icon size="40"><User /></el-icon>
            </el-avatar>
            <el-button type="primary" size="small" class="upload-btn" @click="handleAvatarUpload">
              <el-icon><Upload /></el-icon>
              更换头像
            </el-button>
          </div>
          <div class="user-info">
            <h2 class="username">{{ displayName }}</h2>
            <p class="user-email">{{ form.email }}</p>
            <div class="user-meta">
              <span>用户名：{{ form.username }}</span>
              <span>语言：{{ form.language }}</span>
              <span>时区：{{ form.timezone }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="form-card" shadow="hover" v-loading="loadingProfile">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <el-button type="primary" @click="handleSave" :loading="saving">
              <el-icon><Check /></el-icon>
              保存更改
            </el-button>
          </div>
        </template>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" type="email" disabled />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="个人网站" prop="website">
                <el-input v-model="form.website" placeholder="https://example.com" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Github" prop="github">
                <el-input v-model="form.github" placeholder="https://github.com/username" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="Twitter" prop="twitter">
                <el-input v-model="form.twitter" placeholder="https://twitter.com/username" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="界面语言" prop="language">
                <el-select v-model="form.language" placeholder="选择语言">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="时区" prop="timezone">
            <el-select v-model="form.timezone" placeholder="选择时区" filterable>
              <el-option label="北京时间 (UTC+8)" value="Asia/Shanghai" />
              <el-option label="东京时间 (UTC+9)" value="Asia/Tokyo" />
              <el-option label="纽约时间 (UTC-5)" value="America/New_York" />
              <el-option label="伦敦时间 (UTC+0)" value="Europe/London" />
            </el-select>
          </el-form-item>

          <el-form-item label="邮件通知">
            <div class="notification-settings">
              <el-checkbox v-model="form.emailNotifications.comments">评论通知</el-checkbox>
              <el-checkbox v-model="form.emailNotifications.likes">点赞通知</el-checkbox>
              <el-checkbox v-model="form.emailNotifications.follows">关注通知</el-checkbox>
              <el-checkbox v-model="form.emailNotifications.newsletter">订阅推送</el-checkbox>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="form-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>修改密码</span>
            <el-button type="primary" @click="handlePasswordChange" :loading="changingPassword">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
          </div>
        </template>

        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
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
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { User, Upload, Check, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import type { CurrentUserProfile } from '@/types/user'

const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const loadingProfile = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  bio: '',
  website: '',
  github: '',
  twitter: '',
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  emailNotifications: {
    comments: true,
    likes: true,
    follows: true,
    newsletter: false
  }
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介不能超过 200 个字符', trigger: 'blur' }
  ],
  website: [
    { type: 'url', message: '请输入有效的网址', trigger: 'blur' }
  ],
  github: [
    { type: 'url', message: '请输入有效的 Github 链接', trigger: 'blur' }
  ],
  twitter: [
    { type: 'url', message: '请输入有效的 Twitter 链接', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
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

const avatar = computed(() => userInfo.value?.avatar || '')
const displayName = computed(() => form.nickname || userInfo.value?.nickname || userInfo.value?.username || '')

const populateForm = (profile: CurrentUserProfile) => {
  form.username = profile.username || ''
  form.email = profile.email || ''
  form.nickname = profile.nickname || ''
  form.bio = profile.bio || ''
  form.website = profile.website || ''
  form.github = profile.github || ''
  form.twitter = profile.twitter || ''
  form.language = profile.language || 'zh-CN'
  form.timezone = profile.timezone || 'Asia/Shanghai'

  const notifications = profile.emailNotifications || {}
  form.emailNotifications.comments = notifications.comments ?? true
  form.emailNotifications.likes = notifications.likes ?? true
  form.emailNotifications.follows = notifications.follows ?? true
  form.emailNotifications.newsletter = notifications.newsletter ?? false
}

const loadProfile = async () => {
  loadingProfile.value = true
  try {
    const profile = await userStore.fetchUserInfo().catch(() => userInfo.value)
    if (profile) {
      populateForm(profile)
    } else if (userInfo.value) {
      populateForm(userInfo.value)
    }
  } catch (error) {
    console.error('Failed to load profile:', error)
    ElMessage.error('加载个人信息失败')
  } finally {
    loadingProfile.value = false
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
      emailNotifications: { ...form.emailNotifications }
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

const resetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await userStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })

    resetPasswordForm()
  } catch (error: any) {
    console.error('Failed to change password:', error)
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    changingPassword.value = false
  }
}

const handleAvatarUpload = () => {
  ElMessageBox.confirm('头像上传功能暂未开放，是否继续？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.info('头像上传功能开发中...')
  }).catch(() => {})
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #111827;
}

.page-description {
  margin: 8px 0 0;
  color: #6b7280;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 2px solid var(--el-color-primary-light-5);
}

.upload-btn {
  font-size: 12px;
}

.user-info {
  flex: 1;
}

.username {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.user-email {
  margin: 0;
  color: #6b7280;
}

.user-meta {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
}

.form-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.notification-settings {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-meta {
    justify-content: center;
  }

  .form-card .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
}
</style>
