<template>
  <div class="p-6">
    <!-- 页面头部 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">租户配置</h1>
      <p class="text-gray-600 mt-1">管理租户基本信息和系统配置</p>
    </div>
    
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 基本信息 -->
      <div class="lg:col-span-2">
        <div class="modern-card p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-gray-900">基本信息</h2>
            <el-button 
              type="primary" 
              @click="saveBasicInfo"
              :loading="saving.basic"
              :disabled="!hasBasicChanges"
            >
              保存更改
            </el-button>
          </div>
          
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="100px"
            class="space-y-4"
          >
            <el-form-item label="租户名称" prop="name">
              <el-input 
                v-model="basicForm.name" 
                placeholder="请输入租户名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="网站标题" prop="title">
              <el-input 
                v-model="basicForm.title" 
                placeholder="请输入网站标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="网站描述" prop="description">
              <el-input 
                v-model="basicForm.description" 
                type="textarea"
                placeholder="请输入网站描述"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="网站关键词" prop="keywords">
              <el-input 
                v-model="basicForm.keywords" 
                placeholder="请输入网站关键词，用逗号分隔"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input 
                v-model="basicForm.contactEmail" 
                placeholder="请输入联系邮箱"
                type="email"
              />
            </el-form-item>
            
            <el-form-item label="ICP备案号" prop="icpNumber">
              <el-input 
                v-model="basicForm.icpNumber" 
                placeholder="请输入ICP备案号（可选）"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 内容保护配置 -->
        <div class="modern-card p-6 mt-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-lg font-semibold text-gray-900">内容保护配置</h2>
            <el-button 
              type="primary" 
              @click="saveProtectionConfig"
              :loading="saving.protection"
              :disabled="!hasProtectionChanges"
            >
              保存更改
            </el-button>
          </div>
          
          <el-form
            ref="protectionFormRef"
            :model="protectionForm"
            label-width="120px"
            class="space-y-4"
          >
            <el-form-item label="默认复制策略">
              <el-radio-group v-model="protectionForm.defaultCopyPolicy">
                <el-radio value="allow">允许复制</el-radio>
                <el-radio value="deny">禁止复制</el-radio>
                <el-radio value="login_required">登录后可复制</el-radio>
              </el-radio-group>
              <div class="text-sm text-gray-500 mt-1">
                新创建文章的默认复制权限设置
              </div>
            </el-form-item>
            
            <el-form-item label="水印设置">
              <el-switch
                v-model="protectionForm.enableWatermark"
                active-text="启用水印"
                inactive-text="禁用水印"
              />
              <div class="text-sm text-gray-500 mt-1">
                在受保护的内容上显示水印
              </div>
            </el-form-item>
            
            <el-form-item 
              v-if="protectionForm.enableWatermark" 
              label="水印文本"
            >
              <el-input 
                v-model="protectionForm.watermarkText" 
                placeholder="请输入水印文本"
                maxlength="50"
              />
            </el-form-item>
            
            <el-form-item label="右键菜单">
              <el-switch
                v-model="protectionForm.disableRightClick"
                active-text="禁用右键"
                inactive-text="允许右键"
              />
              <div class="text-sm text-gray-500 mt-1">
                禁用页面右键菜单
              </div>
            </el-form-item>
            
            <el-form-item label="文本选择">
              <el-switch
                v-model="protectionForm.disableSelection"
                active-text="禁用选择"
                inactive-text="允许选择"
              />
              <div class="text-sm text-gray-500 mt-1">
                禁用页面文本选择
              </div>
            </el-form-item>
            
            <el-form-item label="开发者工具">
              <el-switch
                v-model="protectionForm.disableDevTools"
                active-text="禁用F12"
                inactive-text="允许F12"
              />
              <div class="text-sm text-gray-500 mt-1">
                禁用浏览器开发者工具快捷键
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 侧边栏 -->
      <div class="lg:col-span-1">
        <!-- Logo设置 -->
        <div class="modern-card p-6 mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Logo设置</h3>
          
          <div class="space-y-4">
            <!-- 当前Logo -->
            <div v-if="basicForm.logo" class="text-center">
              <img 
                :src="basicForm.logo" 
                alt="网站Logo"
                class="max-w-full h-20 mx-auto object-contain"
              />
              <el-button 
                size="small" 
                type="danger" 
                class="mt-2"
                @click="removeLogo"
              >
                移除Logo
              </el-button>
            </div>
            
            <!-- 上传Logo -->
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUploadLogo"
              :http-request="uploadLogo"
              accept="image/*"
              class="w-full"
            >
              <el-button class="w-full" :loading="uploading.logo">
                <el-icon class="mr-2"><Upload /></el-icon>
                {{ basicForm.logo ? '更换Logo' : '上传Logo' }}
              </el-button>
            </el-upload>
            
            <div class="text-xs text-gray-500">
              建议尺寸：200x60px，支持PNG、JPG格式，文件大小不超过2MB
            </div>
          </div>
        </div>

        <!-- Favicon设置 -->
        <div class="modern-card p-6 mb-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">Favicon设置</h3>
          
          <div class="space-y-4">
            <!-- 当前Favicon -->
            <div v-if="basicForm.favicon" class="text-center">
              <img 
                :src="basicForm.favicon" 
                alt="网站图标"
                class="w-8 h-8 mx-auto"
              />
              <el-button 
                size="small" 
                type="danger" 
                class="mt-2"
                @click="removeFavicon"
              >
                移除图标
              </el-button>
            </div>
            
            <!-- 上传Favicon -->
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUploadFavicon"
              :http-request="uploadFavicon"
              accept="image/*"
              class="w-full"
            >
              <el-button class="w-full" :loading="uploading.favicon">
                <el-icon class="mr-2"><Upload /></el-icon>
                {{ basicForm.favicon ? '更换图标' : '上传图标' }}
              </el-button>
            </el-upload>
            
            <div class="text-xs text-gray-500">
              建议尺寸：32x32px，支持ICO、PNG格式，文件大小不超过1MB
            </div>
          </div>
        </div>

        <!-- 站长信息 -->
        <div class="modern-card p-6 mb-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">站长信息</h3>
            <el-button
              size="small"
              type="primary"
              @click="saveOwnerInfo"
              :loading="saving.owner"
              :disabled="!hasOwnerChanges"
            >
              保存
            </el-button>
          </div>

          <el-form :model="ownerForm" label-position="top" class="space-y-4">
            <el-form-item label="站长二维码">
              <el-upload
                class="owner-qr-upload"
                list-type="picture-card"
                :file-list="ownerQrFileList"
                :limit="1"
                :before-upload="beforeUploadOwnerQr"
                :http-request="uploadOwnerQr"
                :disabled="uploading.ownerQr"
                @remove="handleOwnerQrRemove"
                @preview="handleOwnerQrPreview"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
                <div class="el-upload__text">
                  {{ ownerForm.qrCode ? '更换二维码' : '上传二维码' }}
                </div>
              </el-upload>
              <div class="text-xs text-gray-500 mt-2">
                支持 PNG/JPG，建议尺寸不小于 400×400 像素
              </div>
            </el-form-item>

            <el-form-item label="备注信息">
              <el-input
                v-model="ownerForm.remark"
                type="textarea"
                :rows="3"
                maxlength="200"
                show-word-limit
                placeholder="请输入站长介绍或开通会员方式说明"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 统计信息 -->
        <div class="modern-card p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">统计信息</h3>

          <div class="space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-gray-600">文章总数</span>
              <span class="font-medium">{{ stats.articleCount }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">分类数量</span>
              <span class="font-medium">{{ stats.categoryCount }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">总访问量</span>
              <span class="font-medium">{{ stats.totalViews }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">今日访问</span>
              <span class="font-medium">{{ stats.todayViews }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600">注册用户</span>
              <span class="font-medium">{{ stats.userCount }}</span>
            </div>
          </div>
          
          <el-button 
            class="w-full mt-4" 
            @click="refreshStats"
            :loading="loading.stats"
          >
            刷新统计
          </el-button>
        </div>
      </div>
    </div>
  </div>

  <el-dialog
    v-model="ownerPreviewVisible"
    width="360px"
    append-to-body
    title="二维码预览"
  >
    <div class="flex items-center justify-center">
      <img
        v-if="ownerPreviewUrl"
        :src="ownerPreviewUrl"
        alt="站长二维码预览"
        class="max-w-full rounded-lg shadow"
      />
      <div v-else class="text-gray-500 text-sm">暂无预览内容</div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadUserFile } from 'element-plus'
import { Upload, Plus } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { articleApi } from '@/api/article'

const tenantStore = useTenantStore()

// 响应式数据
const basicFormRef = ref<FormInstance>()
const protectionFormRef = ref<FormInstance>()

const loading = reactive({
  stats: false
})

const saving = reactive({
  basic: false,
  protection: false,
  owner: false
})

const uploading = reactive({
  logo: false,
  favicon: false,
  ownerQr: false
})

// 基本信息表单
const basicForm = reactive({
  name: '',
  title: '',
  description: '',
  keywords: '',
  contactEmail: '',
  icpNumber: '',
  logo: '',
  favicon: ''
})

// 原始基本信息（用于检测变更）
const originalBasicForm = reactive({ ...basicForm })

// 站长信息表单
const ownerForm = reactive({
  qrCode: '',
  remark: ''
})

// 原始站长信息
const originalOwnerForm = reactive({ ...ownerForm })

// 内容保护配置表单
const protectionForm = reactive({
  defaultCopyPolicy: 'allow' as 'allow' | 'deny' | 'login_required',
  enableWatermark: false,
  watermarkText: '',
  disableRightClick: false,
  disableSelection: false,
  disableDevTools: false
})

// 原始保护配置（用于检测变更）
const originalProtectionForm = reactive({ ...protectionForm })

// 站长二维码文件列表与预览
const ownerQrFileList = ref<UploadUserFile[]>([])
const ownerPreviewVisible = ref(false)
const ownerPreviewUrl = ref('')

// 统计信息
const stats = reactive({
  articleCount: 0,
  categoryCount: 0,
  totalViews: 0,
  todayViews: 0,
  userCount: 0
})

// 表单验证规则
const basicRules: FormRules = {
  name: [
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { min: 1, max: 50, message: '租户名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入网站标题', trigger: 'blur' },
    { min: 1, max: 100, message: '网站标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 计算属性
const hasBasicChanges = computed(() => {
  return Object.keys(basicForm).some(key => 
    basicForm[key as keyof typeof basicForm] !== originalBasicForm[key as keyof typeof originalBasicForm]
  )
})

const hasProtectionChanges = computed(() => {
  return Object.keys(protectionForm).some(key => 
    protectionForm[key as keyof typeof protectionForm] !== originalProtectionForm[key as keyof typeof originalProtectionForm]
  )
})

const hasOwnerChanges = computed(() => {
  return Object.keys(ownerForm).some(key =>
    ownerForm[key as keyof typeof ownerForm] !== originalOwnerForm[key as keyof typeof originalOwnerForm]
  )
})

watch(
  () => ownerForm.qrCode,
  (value) => {
    const normalized = typeof value === 'string' ? value.trim() : ''
    if (normalized) {
      ownerQrFileList.value = [
        {
          name: 'owner-qr',
          url: normalized,
          status: 'success',
          uid: 'owner-qr'
        }
      ]
      ownerPreviewUrl.value = normalized
    } else {
      ownerQrFileList.value = []
      ownerPreviewUrl.value = ''
      ownerPreviewVisible.value = false
    }
  },
  { immediate: true }
)

// 方法
const fetchTenantInfo = async () => {
  try {
    await tenantStore.fetchTenantInfo()
    const info = tenantStore.tenantInfo
    
    if (info) {
      Object.assign(basicForm, {
        name: info.name || '',
        title: info.title || '',
        description: info.description || '',
        keywords: info.keywords || '',
        contactEmail: info.contactEmail || '',
        icpNumber: info.icpNumber || '',
        logo: info.logo || '',
        favicon: info.favicon || ''
      })
      
      Object.assign(originalBasicForm, { ...basicForm })
    }
  } catch (error) {
    console.error('Failed to fetch tenant info:', error)
  }
}

const fetchTenantConfig = async () => {
  try {
    await tenantStore.fetchTenantConfig()
    const config = tenantStore.tenantConfig
    
    if (config) {
      Object.assign(protectionForm, {
        defaultCopyPolicy: config.defaultCopyPolicy || 'allow',
        enableWatermark: config.enableWatermark || false,
        watermarkText: config.watermarkText || '',
        disableRightClick: config.disableRightClick || false,
        disableSelection: config.disableSelection || false,
        disableDevTools: config.disableDevTools || false
      })

      Object.assign(ownerForm, {
        qrCode: config.ownerQrCode || '',
        remark: config.ownerRemark || ''
      })
      
      Object.assign(originalProtectionForm, { ...protectionForm })
      Object.assign(originalOwnerForm, { ...ownerForm })
    }
  } catch (error) {
    console.error('Failed to fetch tenant config:', error)
  }
}

const saveBasicInfo = async () => {
  if (!basicFormRef.value) return
  
  try {
    await basicFormRef.value.validate()
    saving.basic = true
    
    // 这里应该调用API保存基本信息
    // await tenantApi.updateInfo(basicForm)
    
    Object.assign(originalBasicForm, { ...basicForm })
    ElMessage.success('基本信息保存成功')
  } catch (error) {
    if (error !== false) {
      console.error('Save basic info failed:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.basic = false
  }
}

const saveProtectionConfig = async () => {
  try {
    saving.protection = true
    
    // 这里应该调用API保存保护配置
    // await tenantApi.updateConfig(protectionForm)
    
    Object.assign(originalProtectionForm, { ...protectionForm })
    ElMessage.success('保护配置保存成功')
  } catch (error) {
    console.error('Save protection config failed:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.protection = false
  }
}

const saveOwnerInfo = async () => {
  try {
    saving.owner = true

    // 这里应该调用API保存站长信息
    // await tenantApi.updateOwnerInfo(ownerForm)

    Object.assign(originalOwnerForm, { ...ownerForm })
    ElMessage.success('站长信息已保存')
  } catch (error) {
    console.error('Save owner info failed:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.owner = false
  }
}

const beforeUploadLogo = (file: File) => {
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

const uploadLogo = async ({ file }: { file: File }) => {
  try {
    uploading.logo = true
    const { url } = await articleApi.uploadImage(file)
    basicForm.logo = url
    ElMessage.success('Logo上传成功')
  } catch (error) {
    console.error('Upload logo failed:', error)
    ElMessage.error('Logo上传失败')
  } finally {
    uploading.logo = false
  }
}

const removeLogo = () => {
  basicForm.logo = ''
}

const beforeUploadFavicon = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt1M = file.size / 1024 / 1024 < 1
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt1M) {
    ElMessage.error('图片大小不能超过 1MB')
    return false
  }
  return true
}

const uploadFavicon = async ({ file }: { file: File }) => {
  try {
    uploading.favicon = true
    const { url } = await articleApi.uploadImage(file)
    basicForm.favicon = url
    ElMessage.success('图标上传成功')
  } catch (error) {
    console.error('Upload favicon failed:', error)
    ElMessage.error('图标上传失败')
  } finally {
    uploading.favicon = false
  }
}

const removeFavicon = () => {
  basicForm.favicon = ''
}

const beforeUploadOwnerQr = (file: File) => {
  const isImage = file.type.startsWith('image/')
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

const uploadOwnerQr = async ({ file }: { file: File }) => {
  try {
    uploading.ownerQr = true
    const { url } = await articleApi.uploadImage(file)
    ownerForm.qrCode = url
    ElMessage.success('二维码上传成功')
  } catch (error) {
    console.error('Upload owner QR failed:', error)
    ElMessage.error('二维码上传失败')
  } finally {
    uploading.ownerQr = false
  }
}

const handleOwnerQrRemove = () => {
  ownerForm.qrCode = ''
  ownerPreviewUrl.value = ''
  ownerPreviewVisible.value = false
}

const handleOwnerQrPreview = (file: UploadUserFile) => {
  const previewUrl = typeof file?.url === 'string' && file.url ? file.url : ownerForm.qrCode
  if (previewUrl) {
    ownerPreviewUrl.value = previewUrl
    ownerPreviewVisible.value = true
  }
}

const refreshStats = async () => {
  try {
    loading.stats = true
    
    // 这里应该调用API获取统计信息
    // const data = await tenantApi.getStats()
    // Object.assign(stats, data)
    
    // 模拟数据
    Object.assign(stats, {
      articleCount: 156,
      categoryCount: 12,
      totalViews: 8924,
      todayViews: 234,
      userCount: 89
    })
    
    ElMessage.success('统计信息已刷新')
  } catch (error) {
    console.error('Refresh stats failed:', error)
    ElMessage.error('刷新失败')
  } finally {
    loading.stats = false
  }
}

// 生命周期
onMounted(() => {
  Promise.all([
    fetchTenantInfo(),
    fetchTenantConfig(),
    refreshStats()
  ])
})
</script>

<style scoped>
/* 自定义样式 */
</style>
