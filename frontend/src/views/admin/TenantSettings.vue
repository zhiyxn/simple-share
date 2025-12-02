<template>
  <div class="tenant-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Setting /></el-icon>
          租户配置
        </h1>
        <p class="page-description">管理租户基本信息、主题配置、功能设置等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="saveAllSettings" :loading="saving">
          <el-icon><Check /></el-icon>
          保存所有设置
        </el-button>
      </div>
    </div>

    <!-- 配置选项卡 -->
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 租户管理 -->
      <el-tab-pane label="租户管理" name="tenants">
        <TenantManagement />
      </el-tab-pane>
      
      <!-- 文件管理 -->
      <el-tab-pane label="文件管理" name="files">
        <FileManagement />
      </el-tab-pane>
      
      <!-- 文件配置 -->
      <el-tab-pane label="文件配置" name="fileConfig">
        <FileConfigManagement />
      </el-tab-pane>
      
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <div class="settings-section">
          <el-form
            ref="basicFormRef"
            :model="basicSettings"
            :rules="basicRules"
            label-width="120px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="租户名称" prop="name">
                  <el-input
                    v-model="basicSettings.name"
                    placeholder="请输入租户名称"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="租户标识" prop="code">
                  <el-input
                    v-model="basicSettings.code"
                    placeholder="请输入租户标识"
                    :disabled="true"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系邮箱" prop="email">
                  <el-input
                    v-model="basicSettings.email"
                    placeholder="请输入联系邮箱"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input
                    v-model="basicSettings.phone"
                    placeholder="请输入联系电话"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="网站标题" prop="title">
              <el-input
                v-model="basicSettings.title"
                placeholder="请输入网站标题"
              />
            </el-form-item>
            
            <el-form-item label="网站描述">
              <el-input
                v-model="basicSettings.description"
                type="textarea"
                :rows="3"
                placeholder="请输入网站描述"
              />
            </el-form-item>
            
            <el-form-item label="网站关键词">
              <el-input
                v-model="basicSettings.keywords"
                placeholder="请输入网站关键词，用逗号分隔"
              />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网站Logo">
                  <el-upload
                    class="logo-uploader"
                    :action="uploadUrl"
                    :show-file-list="false"
                    :on-success="handleLogoSuccess"
                    :before-upload="beforeImageUpload"
                  >
                    <img v-if="basicSettings.logo" :src="basicSettings.logo" class="logo" />
                    <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="网站图标">
                  <el-upload
                    class="favicon-uploader"
                    :action="uploadUrl"
                    :show-file-list="false"
                    :on-success="handleFaviconSuccess"
                    :before-upload="beforeImageUpload"
                  >
                    <img v-if="basicSettings.favicon" :src="basicSettings.favicon" class="favicon" />
                    <el-icon v-else class="favicon-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 站长信息 -->
      <el-tab-pane label="站长信息" name="owner">
        <div class="settings-section owner-section">
          <el-alert
            title="配置站长二维码，方便用户扫码联系或开通会员"
            type="info"
            :closable="false"
            class="owner-alert"
          />
          <el-form label-width="120px" :model="ownerSettings">
            <el-row :gutter="20">
              <el-col :span="12" :xs="24">
                <el-form-item label="站长二维码">
                  <el-upload
                    class="owner-qr-uploader"
                    list-type="picture-card"
                    :file-list="ownerFileList"
                    :limit="1"
                    :disabled="ownerUploading"
                    accept="image/*"
                    :before-upload="beforeOwnerQrUpload"
                    :http-request="handleOwnerQrRequest"
                    @remove="handleOwnerQrRemove"
                    @preview="handleOwnerQrPreview"
                  >
                    <el-icon><Plus /></el-icon>
                    <div class="el-upload__text">
                      {{ ownerSettings.qrCode ? '更换二维码' : '上传二维码' }}
                    </div>
                  </el-upload>
                  <div class="upload-tip">
                    支持 PNG/JPG/JPEG，建议尺寸不小于 400×400 像素，大小不超过 5MB。
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12" :xs="24">
                <el-form-item label="备注信息">
                  <el-input
                    v-model="ownerSettings.remark"
                    type="textarea"
                    :rows="6"
                    maxlength="200"
                    show-word-limit
                    placeholder="如：扫码联系站长开通会员、售后交流群等"
                  />
                </el-form-item>
                <div v-if="ownerSettings.qrCode" class="owner-preview-actions">
                  <el-button
                    size="small"
                    type="primary"
                    @click="openOwnerPreview"
                    :disabled="ownerUploading"
                  >
                    预览二维码
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-tab-pane

      <!-- 功能设置 -->
      <el-tab-pane label="功能设置" name="features">
        <div class="settings-section">
          <el-form label-width="150px">
            <div class="feature-group">
              <h3>用户功能</h3>
              <el-form-item label="用户注册">
                <el-switch v-model="featureSettings.userRegistration" />
                <span class="feature-desc">允许新用户注册账号</span>
              </el-form-item>
              
              <el-form-item label="邮箱验证">
                <el-switch v-model="featureSettings.emailVerification" />
                <span class="feature-desc">注册时需要验证邮箱</span>
              </el-form-item>
              
              <el-form-item label="社交登录">
                <el-switch v-model="featureSettings.socialLogin" />
                <span class="feature-desc">支持第三方社交账号登录</span>
              </el-form-item>
            </div>
            
            <div class="feature-group">
              <h3>内容功能</h3>
              <el-form-item label="文章评论">
                <el-switch v-model="featureSettings.articleComments" />
                <span class="feature-desc">允许用户对文章进行评论</span>
              </el-form-item>
              
              <el-form-item label="文章点赞">
                <el-switch v-model="featureSettings.articleLikes" />
                <span class="feature-desc">允许用户对文章进行点赞</span>
              </el-form-item>
              
              <el-form-item label="文章收藏">
                <el-switch v-model="featureSettings.articleFavorites" />
                <span class="feature-desc">允许用户收藏文章</span>
              </el-form-item>
              
              <el-form-item label="文章分享">
                <el-switch v-model="featureSettings.articleShare" />
                <span class="feature-desc">允许用户分享文章</span>
              </el-form-item>
            </div>
            
            <div class="feature-group">
              <h3>安全功能</h3>
              <el-form-item label="防复制保护">
                <el-switch v-model="featureSettings.copyProtection" />
                <span class="feature-desc">启用文章防复制功能</span>
              </el-form-item>
              
              <el-form-item label="水印显示">
                <el-switch v-model="featureSettings.watermark" />
                <span class="feature-desc">在文章页面显示水印</span>
              </el-form-item>
              
              <el-form-item label="访问统计">
                <el-switch v-model="featureSettings.analytics" />
                <span class="feature-desc">记录文章访问统计</span>
              </el-form-item>
            </div>
            
            <div class="feature-group">
              <h3>其他功能</h3>
              <el-form-item label="搜索功能">
                <el-switch v-model="featureSettings.search" />
                <span class="feature-desc">启用全站搜索功能</span>
              </el-form-item>
              
              <el-form-item label="RSS订阅">
                <el-switch v-model="featureSettings.rss" />
                <span class="feature-desc">提供RSS订阅功能</span>
              </el-form-item>
              
              <el-form-item label="站点地图">
                <el-switch v-model="featureSettings.sitemap" />
                <span class="feature-desc">自动生成站点地图</span>
              </el-form-item>
            </div>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane label="邮件配置" name="email">
        <div class="settings-section">
          <el-form
            ref="emailFormRef"
            :model="emailSettings"
            :rules="emailRules"
            label-width="120px"
          >
            <el-form-item label="SMTP服务器" prop="host">
              <el-input
                v-model="emailSettings.host"
                placeholder="请输入SMTP服务器地址"
              />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="端口" prop="port">
                  <el-input-number
                    v-model="emailSettings.port"
                    :min="1"
                    :max="65535"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="加密方式">
                  <el-select v-model="emailSettings.secure" style="width: 100%">
                    <el-option label="无加密" :value="false" />
                    <el-option label="SSL/TLS" :value="true" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input
                    v-model="emailSettings.username"
                    placeholder="请输入邮箱用户名"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="emailSettings.password"
                    type="password"
                    placeholder="请输入邮箱密码或授权码"
                    show-password
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="发件人名称" prop="fromName">
                  <el-input
                    v-model="emailSettings.fromName"
                    placeholder="请输入发件人名称"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="发件人邮箱" prop="fromEmail">
                  <el-input
                    v-model="emailSettings.fromEmail"
                    placeholder="请输入发件人邮箱"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item>
              <el-button type="primary" @click="testEmailConnection" :loading="testing">
                <el-icon><Connection /></el-icon>
                测试连接
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 存储配置 -->
      <el-tab-pane label="存储配置" name="storage">
        <div class="settings-section">
          <el-form label-width="120px">
            <el-form-item label="存储类型">
              <el-radio-group v-model="storageSettings.type">
                <el-radio label="local">本地存储</el-radio>
                <el-radio label="aliyun">阿里云 OSS</el-radio>
                <el-radio label="qiniu">七牛云存储</el-radio>
              </el-radio-group>
            </el-form-item>

            <template v-if="storageSettings.type === 'local'">
              <el-form-item label="根目录">
                <el-input
                  v-model="storageSettings.local.basePath"
                  placeholder="例如 D:/simpleshare/uploadPath/upload"
                />
              </el-form-item>
              <el-form-item label="访问地址">
                <el-input
                  v-model="storageSettings.local.baseUrl"
                  placeholder="例如 /upload 或 https://cdn.example.com/upload"
                />
              </el-form-item>
              <el-form-item label="路径前缀">
                <el-input
                  v-model="storageSettings.local.pathPrefix"
                  placeholder="可选，默认为空"
                />
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="Endpoint">
                <el-input
                  v-model="activeCloudConfig.endpoint"
                  placeholder="例如 oss-cn-hangzhou.aliyuncs.com"
                />
              </el-form-item>
              <el-form-item label="Bucket">
                <el-input
                  v-model="activeCloudConfig.bucket"
                  placeholder="请输入存储桶名称"
                />
              </el-form-item>
              <el-form-item label="AccessKey">
                <el-input
                  v-model="activeCloudConfig.accessKey"
                  placeholder="请输入AccessKey"
                />
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input
                  v-model="activeCloudConfig.secretKey"
                  type="password"
                  placeholder="请输入SecretKey"
                  show-password
                />
              </el-form-item>
              <el-form-item label="地域 / 区域">
                <el-input
                  v-model="activeCloudConfig.region"
                  placeholder="可选，例如 cn-hangzhou"
                />
              </el-form-item>
              <el-form-item label="自定义域名">
                <el-input
                  v-model="activeCloudConfig.domain"
                  placeholder="可选，例如 https://cdn.example.com"
                />
              </el-form-item>
              <el-form-item label="路径前缀">
                <el-input
                  v-model="activeCloudConfig.pathPrefix"
                  placeholder="可选，例如 uploads/images"
                />
              </el-form-item>
            </template>

            <el-form-item label="最大文件大小">
              <el-input-number
                v-model="storageSettings.maxSize"
                :min="1"
                :max="200"
                style="width: 200px"
              />
              <span style="margin-left: 8px">MB</span>
            </el-form-item>

            <el-form-item label="允许的文件类型">
              <el-checkbox-group v-model="storageSettings.allowedTypes">
                <el-checkbox label="image">图片</el-checkbox>
                <el-checkbox label="document">文档</el-checkbox>
                <el-checkbox label="video">视频</el-checkbox>
                <el-checkbox label="audio">音频</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog
      v-model="ownerPreviewVisible"
      width="360px"
      append-to-body
      title="二维码预览"
    >
      <div class="owner-preview-dialog">
        <img v-if="ownerPreviewUrl" :src="ownerPreviewUrl" alt="站长二维码大图" />
        <div v-else class="owner-preview-placeholder">
          暂无二维码，请先上传后再预览。
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadUserFile, UploadRequestOptions } from 'element-plus'
import {
  Setting,
  Check,
  Plus,
  Connection
} from '@element-plus/icons-vue'
import { tenantApi } from '@/api/tenant'
import { fileApi } from '@/api/infra'
import TenantManagement from './TenantManagement.vue'
import FileManagement from './FileManagement.vue'
import FileConfigManagement from './FileConfigManagement.vue'
import { ensureAbsoluteUrl, fileUrlConfig } from '@/utils/url'

// 响应式数据
const activeTab = ref('tenants')
const saving = ref(false)
const testing = ref(false)
const uploadUrl = ref('/upload/image')

// 表单引用
const basicFormRef = ref()
const emailFormRef = ref()
const ownerFileList = ref<UploadUserFile[]>([])
const ownerPreviewVisible = ref(false)
const ownerPreviewUrl = ref('')
const ownerUploading = ref(false)

const toRelativeFilePath = (value: string): string => {
  const input = (value ?? '').trim()
  if (!input) {
    return ''
  }
  const absolute = ensureAbsoluteUrl(input)
  if (!absolute) {
    return input
  }
  try {
    const absoluteUrl = new URL(absolute)
  if (fileUrlConfig.baseUrl) {
    const baseUrl = new URL(fileUrlConfig.baseUrl)
      if (absoluteUrl.origin === baseUrl.origin) {
        const relative = absoluteUrl.pathname + absoluteUrl.search + absoluteUrl.hash
        return relative || '/'
      }
    }
    return absolute
  } catch {
    if (input.startsWith('/')) {
      return input
    }
    return `/${input.replace(/^\/+/, '')}`
  }
}

// 基本设置
const basicSettings = reactive({
  name: '',
  code: '',
  email: '',
  phone: '',
  title: '',
  description: '',
  keywords: '',
  logo: '',
  favicon: ''
})

// 主题设置
const themeSettings = reactive({
  mode: 'light',
  primaryColor: '#409EFF',
  successColor: '#67C23A',
  warningColor: '#E6A23C',
  dangerColor: '#F56C6C',
  fontFamily: 'system',
  fontSize: 14,
  sidebarWidth: 240,
  headerHeight: 60,
  borderRadius: 4,
  enableShadow: true
})

// 功能设置
const featureSettings = reactive({
  userRegistration: true,
  emailVerification: false,
  socialLogin: false,
  articleComments: true,
  articleLikes: true,
  articleFavorites: true,
  articleShare: true,
  copyProtection: false,
  watermark: false,
  analytics: true,
  search: true,
  rss: true,
  sitemap: true
})

// 邮件设置
const emailSettings = reactive({
  host: '',
  port: 587,
  secure: false,
  username: '',
  password: '',
  fromName: '',
  fromEmail: '',
  replyTo: ''
})

// 存储设置
const storageSettings = reactive({
  type: 'local',
  maxSize: 10,
  allowedTypes: ['image', 'document'],
  local: {
    basePath: '',
    baseUrl: '/upload',
    pathPrefix: ''
  },
  aliyun: {
    endpoint: '',
    bucket: '',
    accessKey: '',
    secretKey: '',
    region: '',
    domain: '',
    pathPrefix: ''
  },
  qiniu: {
    endpoint: '',
    bucket: '',
    accessKey: '',
    secretKey: '',
    region: '',
    domain: '',
    pathPrefix: ''
  }
})

const activeCloudConfig = computed(() => storageSettings.type === 'qiniu' ? storageSettings.qiniu : storageSettings.aliyun)

// 站长设置
const ownerSettings = reactive({
  qrCode: '',
  remark: ''
})

const syncOwnerFileList = () => {
  const raw = typeof ownerSettings.qrCode === 'string' ? ownerSettings.qrCode.trim() : ''
  if (raw) {
    const url = ensureAbsoluteUrl(raw)
    ownerFileList.value = [
      {
        name: 'owner-qr',
        url,
        status: 'success',
        uid: 'owner-qr'
      }
    ]
    ownerPreviewUrl.value = url
  } else {
    ownerFileList.value = []
    if (!ownerPreviewVisible.value) {
      ownerPreviewUrl.value = ''
    }
  }
}

watch(
  () => ownerSettings.qrCode,
  () => {
    syncOwnerFileList()
  },
  { immediate: true }
)

// 预定义颜色
const predefineColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#c71585',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#c71585'
]

// 表单验证规则
const basicRules = {
  name: [
    { required: true, message: '请输入租户名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入租户标识', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入网站标题', trigger: 'blur' }
  ]
}

const emailRules = {
  host: [
    { required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  fromEmail: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '发件人邮箱格式不正确', trigger: ['blur', 'change'] }
  ]
}

// 计算属性
const fontPreviewStyle = computed(() => {
  return {
    fontFamily: themeSettings.fontFamily === 'system' ? 'system-ui' : themeSettings.fontFamily,
    fontSize: `${themeSettings.fontSize}px`
  }
})

// 方法
const loadSettings = async () => {
  try {
    const settings: any = await tenantApi.getSettings()

    if (settings) {
      Object.assign(basicSettings, settings.basic || {})
      Object.assign(themeSettings, settings.theme || {})
      Object.assign(featureSettings, settings.features || {})

      const emailConfig = settings.email || {}
      emailSettings.host = emailConfig.host ?? ''
      emailSettings.port = emailConfig.port ?? 587
      emailSettings.secure = emailConfig.secure ?? false
      emailSettings.username = emailConfig.username ?? ''
      emailSettings.password = emailConfig.password ?? ''
      emailSettings.fromName = emailConfig.senderName ?? emailConfig.fromName ?? ''
      emailSettings.fromEmail =
        emailConfig.from ??
        emailConfig.fromEmail ??
        emailConfig.username ??
        ''
      emailSettings.replyTo = emailConfig.replyTo ?? ''
      const ownerConfig = settings.owner || {}
      const resolvedQr =
        ownerConfig.qrCode ||
        ownerConfig.qr_url ||
        ownerConfig.qrUrl ||
        ownerConfig.url ||
        ownerConfig.path ||
        settings.ownerQrCode ||
        ''
      const resolvedRemark = ownerConfig.remark || ownerConfig.note || settings.ownerRemark || ''
      ownerSettings.qrCode = typeof resolvedQr === 'string' ? normalizeFileUrl(resolvedQr) : ''
      ownerSettings.remark = typeof resolvedRemark === 'string' ? resolvedRemark : ''
      syncOwnerFileList()
      if (settings.storage) {
        const storage = settings.storage
        storageSettings.type = storage.type || 'local'
        storageSettings.maxSize = storage.maxSize ?? 10
        storageSettings.allowedTypes = Array.isArray(storage.allowedTypes) && storage.allowedTypes.length > 0
          ? [...storage.allowedTypes]
          : ['image', 'document']
        Object.assign(storageSettings.local, storage.local || {})
        Object.assign(storageSettings.aliyun, storage.aliyun || {})
        Object.assign(storageSettings.qiniu, storage.qiniu || {})
      }
    }
  } catch (error) {
    ElMessage.error('加载设置失败')
  }
}

const saveAllSettings = async () => {
  try {
    // 验证基本信息表单
    await basicFormRef.value?.validate()
    
    saving.value = true
    
    const ownerQrRelative = toRelativeFilePath(ownerSettings.qrCode)
    const ownerPayload = {
      qrCode: ownerQrRelative,
      remark: ownerSettings.remark
    }

    const emailPayload = buildEmailPayload()

    const settings = {
      basic: basicSettings,
      theme: themeSettings,
      features: featureSettings,
      email: emailPayload,
      owner: ownerPayload,
      ownerQrCode: ownerQrRelative,
      ownerRemark: ownerPayload.remark,
      storage: JSON.parse(JSON.stringify(storageSettings))
    }
    
    await tenantApi.updateSettings(settings)
    await loadSettings()
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('设置保存失败')
  } finally {
    saving.value = false
  }
}

const testEmailConnection = async () => {
  try {
    await emailFormRef.value?.validate()
    
    testing.value = true
    await tenantApi.testEmailConnection(buildEmailPayload())
    ElMessage.success('邮件连接测试成功')
  } catch (error) {
    ElMessage.error('邮件连接测试失败')
  } finally {
    testing.value = false
  }
}

const buildEmailPayload = () => {
  const payload: Record<string, any> = {
    host: emailSettings.host?.trim() || '',
    port: emailSettings.port,
    secure: Boolean(emailSettings.secure),
    username: emailSettings.username?.trim() || '',
    password: emailSettings.password || '',
    senderName: emailSettings.fromName?.trim() || undefined,
    from: emailSettings.fromEmail?.trim() || ''
  }
  if (emailSettings.replyTo?.trim()) {
    payload.replyTo = emailSettings.replyTo.trim()
  }
  return payload
}

const handleLogoSuccess = (response: any) => {
  basicSettings.logo = response.data.url
}

const handleFaviconSuccess = (response: any) => {
  basicSettings.favicon = response.data.url
}

const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const beforeOwnerQrUpload = (file: File) => {
  const isImage = file.type?.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

function resolveUploadedUrl(payload: any): string {
  if (!payload) {
    return ''
  }
  if (typeof payload === 'string') {
    return payload
  }
  if (typeof payload.url === 'string' && payload.url) {
    return payload.url
  }
  if (typeof payload.path === 'string' && payload.path) {
    return payload.path
  }
  if (payload.data) {
    return resolveUploadedUrl(payload.data)
  }
  if (payload.result) {
    return resolveUploadedUrl(payload.result)
  }
  return ''
}

function normalizeFileUrl(url: string): string {
  return ensureAbsoluteUrl(url)
}

const handleOwnerQrRequest = async (options: UploadRequestOptions) => {
  const { file, onError, onSuccess } = options
  const uploadFile = file as File
  try {
    ownerUploading.value = true
    const response = await fileApi.uploadFile(uploadFile)
    const rawUrl = resolveUploadedUrl(response)
    if (!rawUrl) {
      throw new Error('未获取到二维码地址，请重试')
    }
    const finalUrl = normalizeFileUrl(rawUrl)
    ownerSettings.qrCode = finalUrl
    ownerPreviewUrl.value = finalUrl
    ElMessage.success('二维码上传成功')
    onSuccess?.(response as any, options.file as any)
  } catch (error: any) {
    const message = error?.message || '二维码上传失败'
    ElMessage.error(message)
    const err = error instanceof Error ? error : new Error(message)
    onError?.(err)
  } finally {
    ownerUploading.value = false
  }
}

const handleOwnerQrRemove = () => {
  ownerSettings.qrCode = ''
  ownerPreviewUrl.value = ''
  ownerPreviewVisible.value = false
}

const handleOwnerQrPreview = (file: UploadUserFile) => {
  const candidate = typeof file.url === 'string' && file.url ? file.url : ownerSettings.qrCode
  const previewUrl = normalizeFileUrl(candidate || '')
  if (previewUrl) {
    ownerPreviewUrl.value = previewUrl
    ownerPreviewVisible.value = true
  }
}

const openOwnerPreview = () => {
  if (ownerSettings.qrCode) {
    ownerPreviewUrl.value = normalizeFileUrl(ownerSettings.qrCode)
    ownerPreviewVisible.value = true
  }
}



// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.tenant-settings {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.settings-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-section {
  padding: 20px;
}

.color-picker-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-label {
  font-family: monospace;
  color: #606266;
  font-size: 12px;
}

.font-preview {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #303133;
}

.feature-group {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.feature-group:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.feature-group h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.feature-desc {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.owner-alert {
  margin-bottom: 16px;
}

.owner-section .upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.owner-qr-uploader :deep(.el-upload--picture-card) {
  width: 160px;
  height: 160px;
}

.owner-qr-uploader :deep(.el-upload--picture-card .el-icon) {
  font-size: 28px;
  color: #409eff;
}

.owner-qr-uploader :deep(.el-upload-list__item) {
  border-radius: 12px;
}

.owner-preview-actions {
  margin-top: 12px;
}

.owner-preview-dialog {
  display: flex;
  justify-content: center;
  align-items: center;
}

.owner-preview-dialog img {
  max-width: 100%;
  border-radius: 12px;
}

.owner-preview-placeholder {
  padding: 24px;
  text-align: center;
  color: #909399;
}

.logo-uploader:hover,
.favicon-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon,
.favicon-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 80px;
  line-height: 80px;
  text-align: center;
}

.favicon-uploader-icon {
  width: 60px;
  height: 60px;
  line-height: 60px;
  font-size: 20px;
}

.logo {
  width: 120px;
  height: 80px;
  display: block;
  object-fit: contain;
}

.favicon {
  width: 60px;
  height: 60px;
  display: block;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tenant-settings {
    padding: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .settings-section {
    padding: 16px;
  }
  
  .color-picker-group {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
