<template>
  <div class="security-settings">
    <header class="page-header">
      <div>
        <h1>
          <el-icon><Lock /></el-icon>
          系统安全设置
        </h1>
        <p>配置密码策略、登录防护与访问控制策略</p>
      </div>
      <div class="actions">
        <el-button :loading="loading" @click="loadSettings">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" :loading="saving" @click="submit">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </header>

    <el-row :gutter="20">
      <el-col :span="24" :xl="12">
        <el-card shadow="never" class="card">
          <template #header>
            <div class="card-header">
              <span>密码策略</span>
            </div>
          </template>
          <el-form
            label-width="120px"
            :model="form.passwordPolicy"
            :rules="rules.passwordPolicy"
            ref="passwordFormRef"
          >
            <el-form-item label="最小长度" prop="minLength">
              <el-input-number v-model="form.passwordPolicy.minLength" :min="4" :max="32" />
            </el-form-item>
            <el-form-item label="复杂度要求">
              <el-checkbox v-model="form.passwordPolicy.requireUppercase">包含大写字母</el-checkbox>
              <el-checkbox v-model="form.passwordPolicy.requireLowercase">包含小写字母</el-checkbox>
              <el-checkbox v-model="form.passwordPolicy.requireNumber">包含数字</el-checkbox>
              <el-checkbox v-model="form.passwordPolicy.requireSymbol">包含特殊符号</el-checkbox>
            </el-form-item>
            <el-form-item label="密码有效期">
              <el-input-number
                v-model="form.passwordPolicy.expireDays"
                :min="0"
                :max="365"
                placeholder="0 表示永不过期"
              />
              <span class="hint">单位：天，设置为 0 表示不限制</span>
            </el-form-item>
            <el-form-item label="历史记录">
              <el-input-number
                v-model="form.passwordPolicy.historyCount"
                :min="0"
                :max="20"
                placeholder="0 表示不限制"
              />
              <span class="hint">禁止重复使用最近 N 次密码</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="24" :xl="12">
        <el-card shadow="never" class="card">
          <template #header>
            <div class="card-header">
              <span>登录防护</span>
            </div>
          </template>
          <el-form
            label-width="140px"
            :model="form.loginProtection"
            :rules="rules.loginProtection"
            ref="loginFormRef"
          >
            <el-form-item label="最大失败次数" prop="maxAttempts">
              <el-input-number v-model="form.loginProtection.maxAttempts" :min="1" :max="20" />
              <span class="hint">连续失败达到次数后锁定账户</span>
            </el-form-item>
            <el-form-item label="锁定时长">
              <el-input-number v-model="form.loginProtection.lockoutMinutes" :min="1" :max="720" />
              <span class="hint">单位：分钟</span>
            </el-form-item>
            <el-form-item label="自动解锁">
              <el-switch v-model="form.loginProtection.autoUnlock" />
            </el-form-item>
            <el-form-item label="登录验证码">
              <el-switch v-model="form.loginProtection.captchaEnabled" />
              <span class="hint">启用后登录时将校验验证码</span>
            </el-form-item>
            <el-form-item label="会话超时">
              <el-input-number v-model="form.loginProtection.sessionTimeoutMinutes" :min="10" :max="720" />
              <span class="hint">单位：分钟</span>
            </el-form-item>
            <el-form-item label="允许记住登录">
              <el-switch v-model="form.loginProtection.rememberMeEnabled" />
            </el-form-item>
            <el-divider content-position="left">双因素认证</el-divider>
            <el-form-item label="启用双因素">
              <el-switch v-model="form.loginProtection.twoFactorEnabled" />
            </el-form-item>
            <el-form-item label="认证方式">
              <el-select
                v-model="form.loginProtection.twoFactorMethods"
                multiple
                clearable
                placeholder="选择可用的二次验证方式"
                :disabled="!form.loginProtection.twoFactorEnabled"
              >
                <el-option label="邮箱验证码" value="email" />
                <el-option label="短信验证码" value="sms" />
                <el-option label="身份验证器" value="totp" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="card">
      <template #header>
        <div class="card-header">
          <span>访问控制</span>
        </div>
      </template>
      <el-form label-width="140px" :model="form.accessControl" ref="accessFormRef">
        <el-form-item label="IP 白名单">
          <el-input
            v-model="ipWhitelistText"
            type="textarea"
            :rows="6"
            placeholder="每行一个 IP 或网段，如 192.168.0.0/24"
          />
        </el-form-item>
        <el-form-item label="IP 黑名单">
          <el-input
            v-model="ipBlacklistText"
            type="textarea"
            :rows="6"
            placeholder="每行一个 IP 或网段"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Lock, Refresh, Check } from '@element-plus/icons-vue'
import {
  fetchSecuritySettings,
  updateSecuritySettings,
  type SecuritySettings
} from '@/api/security'

const loading = ref(false)
const saving = ref(false)

const passwordFormRef = ref<FormInstance>()
const loginFormRef = ref<FormInstance>()
const accessFormRef = ref<FormInstance>()

const form = reactive<SecuritySettings>({
  passwordPolicy: {
    minLength: 8,
    requireUppercase: false,
    requireLowercase: true,
    requireNumber: true,
    requireSymbol: false,
    expireDays: null,
    historyCount: 5
  },
  loginProtection: {
    maxAttempts: 5,
    lockoutMinutes: 30,
    autoUnlock: true,
    captchaEnabled: false,
    sessionTimeoutMinutes: 60,
    rememberMeEnabled: true,
    twoFactorEnabled: false,
    twoFactorMethods: ['email']
  },
  accessControl: {
    ipWhitelist: [],
    ipBlacklist: []
  }
})

const ipWhitelistText = ref('')
const ipBlacklistText = ref('')

const rules = {
  passwordPolicy: {
    minLength: [
      { required: true, message: '请输入密码最小长度', trigger: 'blur' },
      { type: 'number', min: 4, max: 32, message: '长度范围 4-32', trigger: 'blur' }
    ]
  },
  loginProtection: {
    maxAttempts: [
      { required: true, message: '请输入最大失败次数', trigger: 'blur' },
      { type: 'number', min: 1, max: 20, message: '范围 1-20', trigger: 'blur' }
    ]
  }
}

const normalizeList = (value: string) => {
  if (!value) return []
  return value
    .split(/\r?\n/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
}

const serializeList = (values: string[]) => (values && values.length ? values.join('\n') : '')

const loadSettings = async () => {
  try {
    loading.value = true
    const data = await fetchSecuritySettings()
    Object.assign(form.passwordPolicy, data.passwordPolicy)
    Object.assign(form.loginProtection, data.loginProtection)
    form.loginProtection.twoFactorMethods = data.loginProtection.twoFactorMethods || []
    form.accessControl.ipWhitelist = data.accessControl.ipWhitelist || []
    form.accessControl.ipBlacklist = data.accessControl.ipBlacklist || []
    ipWhitelistText.value = serializeList(form.accessControl.ipWhitelist)
    ipBlacklistText.value = serializeList(form.accessControl.ipBlacklist)
  } catch (error) {
    console.error('加载安全设置失败', error)
    ElMessage.error('加载安全设置失败')
  } finally {
    loading.value = false
  }
}

const validateForms = async () => {
  const forms: (FormInstance | undefined)[] = [
    passwordFormRef.value,
    loginFormRef.value,
    accessFormRef.value
  ]
  for (const instance of forms) {
    if (instance) {
      await instance.validate()
    }
  }
}

const submit = async () => {
  try {
    await validateForms()

    form.accessControl.ipWhitelist = normalizeList(ipWhitelistText.value)
    form.accessControl.ipBlacklist = normalizeList(ipBlacklistText.value)

    if (form.loginProtection.twoFactorEnabled && form.loginProtection.twoFactorMethods.length === 0) {
      ElMessage.warning('请至少选择一种双因素认证方式')
      return
    }

    await ElMessageBox.confirm('确认保存当前的安全设置吗？', '确认操作', {
      type: 'warning'
    })

    saving.value = true
    await updateSecuritySettings(form)
    ElMessage.success('安全设置已更新')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存安全设置失败', error)
      ElMessage.error('保存安全设置失败')
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.security-settings {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 12px;
}

.card {
  border-radius: 14px;
}

.card-header {
  font-weight: 600;
}

.hint {
  margin-left: 12px;
  color: #9ca3af;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .security-settings {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
