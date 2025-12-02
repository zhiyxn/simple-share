<template>
  <div class="editor-toolbar">
    <div class="editor-toolbar__left">
      <el-button class="toolbar-back" text @click="handleBack" v-if="showBackButton">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ backButtonText }}</span>
      </el-button>
      <div class="editor-toolbar__meta">
        <span class="meta-item" v-if="showStatus">
          状态：
          <el-tag :type="getStatusTagType(status)" size="small">
            {{ getStatusText(status) }}
          </el-tag>
        </span>
        <span class="meta-item" v-if="updatedAt">
          更新于：{{ formatDate(updatedAt) }}
        </span>
      </div>
    </div>
    <div class="editor-toolbar__actions">
<el-button text class="toolbar-action" size="small" @click="handlePreview" v-if="showPreviewButton">
  <el-icon><View /></el-icon>
  <span>预览</span>
</el-button>
<el-button class="toolbar-action" size="small" @click="handleSaveDraft" v-if="showSaveDraftButton">
  <el-icon><Document /></el-icon>
  <span>保存草稿</span>
</el-button>
<el-button 
  type="primary" 
  class="toolbar-action" 
  size="small"
  :loading="publishing" 
  @click="handlePublish"
  v-if="showPublishButton"
>
        <el-icon><Upload /></el-icon>
        <span>{{ isPublished ? '更新' : '发布' }}</span>
      </el-button>
      
      <!-- 自定义按钮插槽 -->
      <slot name="custom-actions"></slot>
    </div>
  </div>

  <!-- 设置对话框 -->
  <el-dialog 
    v-model="showSettingsDialog" 
    :title="isPublishMode ? '发布文章设置' : '文章设置'" 
    width="600px" 
    :close-on-click-modal="false" 
    :close-on-press-escape="true" 
    :show-close="true" 
  > 
    <el-form :model="settingsForm" label-width="100px" class="settings-form"> 
      <div class="settings-section"> 
        <h5 class="settings-section__title">b</h5> 
        <el-form-item label="发布状态"> 
          <el-radio-group v-model="settingsForm.status"> 
            <el-radio :label="0">草稿</el-radio> 
            <el-radio :label="1">发布</el-radio> 
          </el-radio-group> 
        </el-form-item>
      </div> 

      <el-divider /> 

      <div class="settings-section">
        <h5 class="settings-section__title">内容属性</h5>
        <CoverImageUpload v-model="settingsForm.coverImage" />
        <el-form-item label="文章摘要">
          <el-input
            v-model="settingsForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="所属分类"> 
          <el-select 
            v-model="settingsForm.categoryId" 
            placeholder="请选择分类" 
            clearable 
            style="width: 100%"
            :loading="categoriesLoading"
          > 
            <el-option 
              v-for="category in availableCategories" 
              :key="getCategoryValue(category)" 
              :label="formatCategoryLabel(category)" 
              :value="getCategoryValue(category)" 
              :title="formatCategoryLabel(category)"
            >
              <div class="category-option">
                <span class="category-option__name">{{ category.name ?? category.categoryName ?? '未命名分类' }}</span>
                <span
                  v-if="category.description ?? category.remark"
                  class="category-option__description"
                >
                  {{ category.description ?? category.remark }}
                </span>
              </div>
            </el-option> 
          </el-select> 
        </el-form-item> 
        <el-form-item label="文章标签"> 
          <div class="tag-input-container"> 
            <el-input 
              v-model="newTag" 
              placeholder="请输入标签后按回车添加" 
              @keyup.enter="addTag" 
              style="width: 100%" 
            /> 
            <div class="tag-list" v-if="settingsForm.tags.length > 0"> 
              <el-tag 
                v-for="(tag, index) in settingsForm.tags" 
                :key="index" 
                closable 
                @close="removeTag(index)" 
                class="tag-item" 
              > 
                {{ tag }} 
              </el-tag> 
            </div> 
          </div> 
        </el-form-item> 
      </div> 

      <el-divider /> 

      <div class="settings-section"> 
        <h5 class="settings-section__title">文章选项</h5> 
        <el-form-item label="允许复制"> 
          <el-switch v-model="settingsForm.allowCopy" :active-value="1" :inactive-value="0" /> 
        </el-form-item>
        <el-form-item label="访问级别"> 
          <el-select 
            v-model="settingsForm.accessLevel" 
            placeholder="请选择访问级别" 
            style="width: 100%" 
          > 
            <el-option label="公开访问" :value="0" /> 
            <el-option label="仅自己" :value="3" /> 
          </el-select> 
        </el-form-item>
      </div> 
      <el-divider />

      <div class="settings-section">
        <h5 class="settings-section__title">安全设置</h5>
        <el-form-item label="启用密码">
          <el-switch v-model="settingsForm.passwordEnabled" />
        </el-form-item>
        <el-form-item label="访问密码" v-if="settingsForm.passwordEnabled">
          <el-input
            v-model="settingsForm.password"
            type="password"
            placeholder="请输入访问密码"
            show-password
            clearable
            autocomplete="new-password"
            :input-style="{ 'autocomplete': 'off' }"
          />
          <div v-if="settingsForm.hasExistingPassword && !settingsForm.password" class="helper-text">
            当前已设置密码，留空将保持不变
          </div>
        </el-form-item>
        <div v-else-if="settingsForm.hasExistingPassword" class="helper-text">
          关闭后将移除当前密码保护
        </div>
      </div>

    </el-form> 
    
    <template #footer> 
      <el-button @click="handleDialogClose">取消</el-button> 
      <el-button type="primary" @click="handleSaveSettings" :loading="dialogSubmitting"> 
        {{ dialogPrimaryLabel }} 
      </el-button> 
    </template> 
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, reactive, watch } from 'vue'
import { ArrowLeft, View, Document, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { usePublicCategoryStore } from '@/stores/publicCategory'
import { articleApi } from '@/api/article'
import CoverImageUpload from '@/components/CoverImageUpload.vue'

// Props
interface Props {
  status?: string
  updatedAt?: Date | string
  publishing?: boolean
  isPublished?: boolean
  showBackButton?: boolean
  showStatus?: boolean
  showPreviewButton?: boolean
  showSaveDraftButton?: boolean
  showPublishButton?: boolean
  backButtonText?: string
  post?: any
  categories?: any[]
  isPublishMode?: boolean
  // 新增编辑器相关props
  fetchEditorData?: () => Record<string, any> | null | undefined
  applyEditorSettings?: (settings: Record<string, any>) => void
  ensureDraftSaved?: () => Promise<any>
  getPublishPayload?: () => Record<string, any> | null | undefined
  getArticleId?: () => string | null | undefined
}

const props = withDefaults(defineProps<Props>(), {
  status: 'draft',
  updatedAt: undefined,
  publishing: false,
  isPublished: false,
  showBackButton: true,
  showStatus: true,
  showPreviewButton: true,
  showSaveDraftButton: true,
  showPublishButton: true,
  backButtonText: '返回我的文章',
  post: () => ({
    status: 'published',
    summary: '',
    categoryId: null,
    tags: [],
    allowCopy: 1,
    accessLevel: 0,
    coverImage: ''
  }),
  categories: () => [],
  isPublishMode: false
})

// Emits
const emit = defineEmits<{
  back: []
  preview: []
  saveDraft: []
  publish: []
  saveSettings: [data: any]
  publishSuccess: [{ id: string }]
  dialogClose: []
}>()

// 响应式数据
const showSettingsDialog = ref(false)
const publishIntent = ref(false)
const newTag = ref('')
const dialogSubmitting = ref(false)

// 设置表单数据
const settingsForm = reactive({
  status: 1, // 默认为已发布
  summary: '',
  categoryId: null as string | null,
  tags: [] as string[],
  allowCopy: 1,
  accessLevel: 0,
  passwordEnabled: false,
  password: '',
  hasExistingPassword: false,
  coverImage: ''
})

// 分类store
const categoryStore = usePublicCategoryStore()
const rawCategories = computed(() =>
  Array.isArray(props.categories) && props.categories.length ? props.categories : categoryStore.categories
)

const normalizeBooleanFlag = (value: any): boolean | undefined => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    if (Number.isNaN(value)) {
      return undefined
    }
    return value !== 0
  }
  const normalized = String(value).trim().toLowerCase()
  if (!normalized) {
    return undefined
  }
  if (['1', 'true', 'yes', 'y', 'enabled', 'active', 'visible', 'on'].includes(normalized)) {
    return true
  }
  if (['0', 'false', 'no', 'n', 'disabled', 'inactive', 'hidden', 'off'].includes(normalized)) {
    return false
  }
  return undefined
}

const DISABLED_STATUS_VALUES = new Set([
  '1',
  'disabled',
  'inactive',
  'hidden',
  '停用',
  '禁用',
  '关闭',
  'deactivated',
  'archived'
])

const ENABLED_STATUS_VALUES = new Set([
  '0',
  'enabled',
  'active',
  'visible',
  'normal',
  '启用',
  '正常',
  'open'
])

const isStatusDisabled = (status: any): boolean => {
  if (status === undefined || status === null || status === '') {
    return false
  }

  if (typeof status === 'number') {
    if (Number.isNaN(status)) {
      return false
    }
    return status !== 0
  }

  const text = String(status).trim().toLowerCase()
  if (!text) {
    return false
  }

  if (ENABLED_STATUS_VALUES.has(text)) {
    return false
  }

  if (DISABLED_STATUS_VALUES.has(text)) {
    return true
  }

  const numeric = Number(text)
  if (!Number.isNaN(numeric)) {
    return numeric !== 0
  }

  return false
}

const isCategorySelectable = (category: any): boolean => {
  if (!category) {
    return false
  }

  if (isStatusDisabled(category.status)) {
    return false
  }

  const visibility = normalizeBooleanFlag(category.isVisible ?? category.visible)
  if (visibility === false) {
    return false
  }

  const deletedFlag = category.delFlag ?? category.deleteFlag ?? category.deleted
  if (deletedFlag !== undefined && deletedFlag !== null && deletedFlag !== '') {
    const deletedText = String(deletedFlag).trim()
    if (deletedText && deletedText !== '0') {
      return false
    }
  }

  return true
}

const availableCategories = computed(() => {
  const list = rawCategories.value ?? []
  return list.filter(isCategorySelectable)
})

watch(
  availableCategories,
  (list) => {
    if (!settingsForm.categoryId) {
      return
    }
    const exists = list.some((category) => getCategoryValue(category) === settingsForm.categoryId)
    if (!exists) {
      settingsForm.categoryId = null
    }
  },
  { immediate: true }
)
const dialogPrimaryLabel = computed(() => (props.isPublishMode || publishIntent.value ? '立即发布' : '保存设置'))

// 分类数据
const categoriesLoading = ref(false)

const getCategoryValue = (category: any): string => {
  const value = category?.id ?? category?.categoryId
  return value !== undefined && value !== null ? value.toString() : ''
}

const formatCategoryLabel = (category: any): string => {
  const name = category?.name ?? category?.categoryName ?? ''
  const description = category?.description ?? category?.remark ?? ''
  if (!name) return ''
  return description ? `${name} (${description})` : name
}

// 数据同步函数
const syncSettingsFromEditor = (snapshot?: Record<string, any> | null | undefined) => {
  const data = snapshot ?? props.fetchEditorData?.() ?? props.post
  if (!data) return

  // 同步状态
  const status = data.status
  if (status === 'published' || status === '1' || status === 1) {
    settingsForm.status = 1
  } else {
    settingsForm.status = 0
  }

  settingsForm.summary = data.summary ?? ''
  settingsForm.categoryId = data.categoryId === null || data.categoryId === undefined || data.categoryId === ''
    ? null
    : String(data.categoryId)
  settingsForm.tags = Array.isArray(data.tags) ? [...data.tags] : []
  settingsForm.allowCopy = Number(data.allowCopy ?? 1)
  settingsForm.accessLevel = Number(data.accessLevel ?? 0)
  settingsForm.coverImage = data.coverImage ?? data.cover_image ?? data.cover ?? ''

  const incomingIsPasswd = data.isPasswd ?? (data as any).is_passwd
  if (incomingIsPasswd !== undefined && incomingIsPasswd !== null) {
    const numeric = Number(incomingIsPasswd)
    if (!Number.isNaN(numeric)) {
      settingsForm.passwordEnabled = numeric === 0
      settingsForm.hasExistingPassword = settingsForm.passwordEnabled
    }
  } else {
    const passwordProtected =
      data.passwordEnabled ??
      data.passwordProtected ??
      data.hasExistingPassword ??
      (data.password ? true : false)
    settingsForm.passwordEnabled =
      passwordProtected === true || passwordProtected === '1' || passwordProtected === 1

    const existingPassword =
      data.hasExistingPassword ??
      data.passwordProtected ??
      false
    settingsForm.hasExistingPassword = settingsForm.passwordEnabled ? Boolean(existingPassword) : false
  }
  settingsForm.password = ''
}

const applySettingsToEditor = () => {
  const settings: Record<string, any> = {
    status: settingsForm.status,
    summary: settingsForm.summary,
    categoryId: settingsForm.categoryId,
    tags: [...settingsForm.tags],
    allowCopy: settingsForm.allowCopy,
    accessLevel: settingsForm.accessLevel,
    passwordEnabled: settingsForm.passwordEnabled,
    hasExistingPassword: settingsForm.passwordEnabled ? settingsForm.hasExistingPassword : false,
    coverImage: settingsForm.coverImage
  }
  const trimmedPassword = typeof settingsForm.password === 'string' ? settingsForm.password.trim() : ''
  if (trimmedPassword) {
    settings.password = trimmedPassword
  }

  if (props.applyEditorSettings) {
    props.applyEditorSettings(settings)
  } else {
    Object.assign(props.post, settings)
  }
}


// 监听编辑器数据变化
watch(
  () => props.fetchEditorData?.(),
  (snapshot) => {
    syncSettingsFromEditor(snapshot)
  },
  { deep: true, immediate: true }
)

// 监听props.post变化
watch(
  () => props.post,
  (newPost) => {
    if (newPost) {
      syncSettingsFromEditor(newPost)
    }
  },
  { deep: true, immediate: true }
)

watch(showSettingsDialog, (visible) => {
  if (visible) {
    syncSettingsFromEditor()
  } else {
    publishIntent.value = false
  }
})

// 方法
const handleBack = () => {
  emit('back')
}

const handlePreview = () => {
  emit('preview')
}

const handleSaveDraft = () => {
  emit('saveDraft')
}

const handlePublish = () => {
  syncSettingsFromEditor()
  publishIntent.value = true
  showSettingsDialog.value = true
  emit('publish')
}

const handleDialogClose = () => {
  showSettingsDialog.value = false
  dialogSubmitting.value = false
  newTag.value = ''
  emit('dialogClose')
}

const handleSaveSettings = async () => {
  if (dialogSubmitting.value) return

  // 添加详细日志
  console.log('🔍 [FrontEditorToolbar] handleSaveSettings 开始执行')
  console.log('  - 当前表单状态:', JSON.parse(JSON.stringify(settingsForm)))
  console.log('  - 表单状态值:', settingsForm.status, `(类型: ${typeof settingsForm.status})`)

  if (!settingsForm.categoryId) {
    ElMessage.error('请选择文章分类后再发布')
    return
  }

  const rawPassword = typeof settingsForm.password === 'string' ? settingsForm.password : ''
  const trimmedPassword = rawPassword.trim()

  if (settingsForm.passwordEnabled && !trimmedPassword && !settingsForm.hasExistingPassword) {
    ElMessage.error('启用密码保护时请填写访问密码')
    return
  }

  const applyPasswordFields = (target: Record<string, any>) => {
    if (settingsForm.passwordEnabled) {
      target.passwordProtected = true
      target.isPasswd = 0
      if (trimmedPassword) {
        target.password = trimmedPassword
      } else if ('password' in target) {
        delete target.password
      }
      if ('clearPassword' in target) {
        delete target.clearPassword
      }
    } else {
      target.passwordProtected = false
      target.isPasswd = 1
      if (settingsForm.hasExistingPassword) {
        target.clearPassword = true
      }
      if ('password' in target) {
        delete target.password
      }
    }
  }

  dialogSubmitting.value = true

  try {
    const pendingTag = newTag.value.trim()
    if (pendingTag && !settingsForm.tags.includes(pendingTag)) {
      settingsForm.tags.push(pendingTag)
    }

    applySettingsToEditor()

    if (props.ensureDraftSaved && props.getArticleId) {
      console.log('📝 [FrontEditorToolbar] 开始保存草稿和发布文章')

      try {
        await props.ensureDraftSaved()
      } catch (error) {
        console.error('📝 [FrontEditorToolbar] 保存草稿失败:', error)
        ElMessage.error('保存草稿失败，请重试')
        return
      }

      const articleId = props.getArticleId()
      console.log('📋 [FrontEditorToolbar] 获取到文章ID:', articleId)
      if (!articleId) {
        // 如果保存失败，尝试使用简单模式发布
        console.log('🔄 [FrontEditorToolbar] 文章ID为空，尝试使用简单模式发布')
        publishIntent.value = false

        const editorSnapshot = props.fetchEditorData?.() ?? (props.post ? { ...props.post } : {})
        const simplePayload: Record<string, any> = {
          title: editorSnapshot?.title ?? '',
          content: editorSnapshot?.content ?? '',
          summary: settingsForm.summary,
          categoryId: settingsForm.categoryId,
          tags: [...settingsForm.tags],
          allowCopy: settingsForm.allowCopy,
          accessLevel: settingsForm.accessLevel,
          status: settingsForm.status,
          coverImage: settingsForm.coverImage
        }
        applyPasswordFields(simplePayload)
        console.log('📋 [FrontEditorToolbar] 简单载荷:', JSON.parse(JSON.stringify(simplePayload)))

        try {
          // 直接创建文章
          const result = await articleApi.createArticle(simplePayload)
          const newArticleId = result?.articleId || result?.id
          if (newArticleId) {
            ElMessage.success('文章发布成功')
            emit('publishSuccess', { id: newArticleId })
            dialogSubmitting.value = false
            showSettingsDialog.value = false
            return
          }
        } catch (createError: any) {
          console.error('🚫 [FrontEditorToolbar] 创建文章失败:', createError)
          ElMessage.error(createError.message || '文章发布失败，请重试')
          dialogSubmitting.value = false
          return
        }

        ElMessage.error('文章未保存，无法发布')
        return
      }

      const basePayload = props.getPublishPayload?.() ?? {}
      const editorSnapshot = props.fetchEditorData?.() ?? (props.post ? { ...props.post } : {})

      console.log('📊 [FrontEditorToolbar] 编辑器快照:', editorSnapshot)

      const publishPayload: Record<string, any> = {
        ...basePayload,
        summary: settingsForm.summary,
        categoryId: settingsForm.categoryId,
        tags: [...settingsForm.tags],
        allowCopy: settingsForm.allowCopy,
        accessLevel: settingsForm.accessLevel,
        status: settingsForm.status,
        coverImage: settingsForm.coverImage
      }
      applyPasswordFields(publishPayload)

      console.log('🚀 [FrontEditorToolbar] 发布载荷（状态处理前）:', JSON.parse(JSON.stringify(publishPayload)))

      const normalizedPayload: Record<string, any> = {
        ...publishPayload,
        title: publishPayload.title ?? editorSnapshot?.title ?? '',
        content: publishPayload.content ?? editorSnapshot?.content ?? '',
        previewContent:
          publishPayload.previewContent ??
          editorSnapshot?.previewContent ??
          publishPayload.summary ??
          editorSnapshot?.summary ??
          '',
        allowComments:
          publishPayload.allowComments ??
          editorSnapshot?.allowComments ??
          true,
        tieredReading:
          publishPayload.tieredReading ??
          editorSnapshot?.tieredReading ??
          false,
        enableWatermark:
          publishPayload.enableWatermark ??
          editorSnapshot?.enableWatermark ??
          false,
        summary: settingsForm.summary,
        categoryId: settingsForm.categoryId,
        tags: [...settingsForm.tags],
        allowCopy: settingsForm.allowCopy,
        accessLevel: settingsForm.accessLevel,
        status: settingsForm.status
      }

      console.log('🎯 [FrontEditorToolbar] 最终载荷（状态处理后）:', JSON.parse(JSON.stringify(normalizedPayload)))
      console.log('  - 最终状态值:', normalizedPayload.status, `(类型: ${typeof normalizedPayload.status})`)

      if (!Array.isArray(normalizedPayload.tags)) {
        normalizedPayload.tags = [...settingsForm.tags]
      }

      console.log('💾 [FrontEditorToolbar] 调用 autosave.saveDraft')
      await articleApi.autosave.saveDraft(articleId, normalizedPayload)
      
      console.log('📤 [FrontEditorToolbar] 调用 publishFrontArticle')
      await articleApi.publishFrontArticle(articleId, normalizedPayload)

      ElMessage.success('文章发布成功')
      emit('saveSettings', normalizedPayload)
      emit('publishSuccess', { id: articleId })
    } else {
      console.log('🔄 [FrontEditorToolbar] 使用简单模式发布')
      const editorSnapshot = props.fetchEditorData?.() ?? (props.post ? { ...props.post } : {})
      const simplePayload: Record<string, any> = {
        title: editorSnapshot?.title ?? '',
        content: editorSnapshot?.content ?? '',
        summary: settingsForm.summary,
        categoryId: settingsForm.categoryId,
        tags: [...settingsForm.tags],
        allowCopy: settingsForm.allowCopy,
        accessLevel: settingsForm.accessLevel,
        status: settingsForm.status,
        coverImage: settingsForm.coverImage
      }
      applyPasswordFields(simplePayload)
      console.log('📋 [FrontEditorToolbar] 简单载荷:', JSON.parse(JSON.stringify(simplePayload)))
      emit('saveSettings', simplePayload)
    }

    if (settingsForm.passwordEnabled) {
      if (trimmedPassword) {
        settingsForm.hasExistingPassword = true
      }
      settingsForm.password = ''
    } else {
      settingsForm.hasExistingPassword = false
      settingsForm.password = ''
    }

    handleDialogClose()
  } catch (error: any) {
    console.error('❌ [FrontEditorToolbar] 发布失败:', error)
    console.error('  - 错误详情:', error.response?.data)
    console.error('  - 状态码:', error.response?.status)
    ElMessage.error(error.message || '发布失败')
  } finally {
    dialogSubmitting.value = false
  }
}


const addTag = () => {
  const value = newTag.value.trim()
  if (!value) return
  if (!settingsForm.tags.includes(value)) {
    settingsForm.tags.push(value)
  }
  newTag.value = ''
}

const removeTag = (index: number) => {
  settingsForm.tags.splice(index, 1)
}

// 加载分类数据
const loadCategories = async () => {
  try {
    console.log('FrontEditorToolbar: 开始加载分类数据...')
    categoriesLoading.value = true
    await categoryStore.fetchCategories()
    console.log('FrontEditorToolbar: 分类数据加载完成:', categoryStore.categories)
    console.log('FrontEditorToolbar: 分类数据长度:', categoryStore.categories.length)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    categoriesLoading.value = false
  }
}

// 组件挂载时加载分类数据和同步设置
onMounted(() => {
  loadCategories()
  syncSettingsFromEditor()
})

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'published':
    case '1':
      return 'success'
    case 'private':
    case '2':
      return 'warning'
    case 'draft':
    case '0':
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'published':
    case '1':
      return '已发布'
    case 'private':
    case '2':
      return '已下线'
    case 'draft':
    case '0':
    default:
      return '草稿'
  }
}

const formatDate = (date?: Date | string): string => {
  if (!date) return '未知'
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return dateObj.toLocaleString('zh-CN')
}
</script>

<style scoped>
.editor-toolbar {
  position: fixed;
  top: 110px;
  right: clamp(18px, 3vw, 64px);
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 14px;
  border-radius: 22px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: #fffffffa;
  box-shadow: 0 18px 36px #0f172a1f;
  z-index: 120;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  overflow-x: hidden;
}

.editor-toolbar__left {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-back {
  @apply text-gray-600 hover:text-gray-900;
}

.toolbar-back :deep(.el-button__text-wrapper) {
  @apply flex items-center gap-2;
}

.editor-toolbar__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-item {
  @apply text-sm text-gray-600 flex items-center gap-2;
}

.editor-toolbar__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: stretch;
  justify-self: end;
}

.toolbar-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 92%;
  max-width: 180px;
  align-self: center;
  min-width: 0;
  height: 34px;
  padding: 0 10px;
  border-radius: 11px;
  font-weight: 600;
  font-size: 13px;
}

.toolbar-action :deep(.el-button__text-wrapper) {
  @apply flex items-center gap-1;
  width: 100%;
  justify-content: center;
}

.toolbar-action :deep(.el-icon) {
  margin-right: 2px;
}

.toolbar-action.is-text,
.toolbar-action.el-button.is-text {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #0f172a;
}

.toolbar-action.el-button--primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-color: #1d4ed8;
  color: #fff;
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.18);
}

/* 模态框样式 */
.settings-form {
  max-height: 60vh;
  overflow-y: auto;
}

.settings-section {
  margin-bottom: 20px;
}

.settings-section__title {
  @apply text-base font-medium text-gray-800 mb-4;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.category-option {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.category-option__name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.category-option__description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tag-input-container {
  width: 100%;
}

.tag-list {
  @apply flex flex-wrap gap-2 mt-3;
}

.tag-item {
  @apply mb-1;
}
.helper-text {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}


/* 响应式设计 */
@media (max-width: 1279px) {
  .editor-toolbar {
    position: static;
    right: auto;
    top: auto;
    width: 100%;
    max-height: none;
    overflow: visible;
  }
}

@media (max-width: 768px) {
  .editor-toolbar {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 12px 16px;
  }
  
  .editor-toolbar__left {
    gap: 8px;
  }
  
  .editor-toolbar__meta {
    gap: 8px;
  }
  
  .editor-toolbar__actions {
    flex-direction: column;
    flex-wrap: nowrap;
  }
  
  .toolbar-action {
    width: 100%;
    min-width: 0;
  }
  
  .toolbar-action span {
    @apply hidden;
  }
}

@media (max-width: 480px) {
  .meta-item span:not(.el-tag) {
    @apply hidden;
  }
}
</style>
