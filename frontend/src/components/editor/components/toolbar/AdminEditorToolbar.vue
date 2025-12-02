<template>
  <div class="editor-toolbar">
    <div class="editor-toolbar__left">
      <el-button class="toolbar-back" text @click="handleBack" v-if="showBackButton">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ backButtonText }}</span>
      </el-button>
      <div class="editor-toolbar__meta">
        <span class="meta-item" v-if="showStatus">
          当前状态：
          <el-tag :type="getStatusTagType(status)" size="small">
            {{ getStatusText(status) }}
          </el-tag>
        </span>
        <span class="meta-item" v-if="updatedAt">
          最近更新：{{ formatDate(updatedAt) }}
        </span>
      </div>
    </div>
    <div class="editor-toolbar__actions">
      <el-button text class="toolbar-action" @click="handlePreview" v-if="showPreviewButton">
        <el-icon><View /></el-icon>
        <span>预览</span>
      </el-button>
      <el-button class="toolbar-action" @click="handleSaveDraft" v-if="showSaveDraftButton">
        <el-icon><Document /></el-icon>
        <span>保存草稿</span>
      </el-button>
      <el-button
        type="primary"
        class="toolbar-action"
        :loading="publishing || dialogSubmitting"
        @click="handlePublishClick"
        v-if="showPublishButton"
      >
        <el-icon><Upload /></el-icon>
        <span>{{ isPublished ? '更新发布' : '保存并发布' }}</span>
      </el-button>

      <slot name="custom-actions"></slot>
    </div>
  </div>

  <el-dialog
    v-model="showSettingsDialog"
    title="发布文章设置"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    :show-close="true"
    @close="handleDialogClose"
  >
    <el-form :model="settingsForm" label-width="100px" class="settings-form">
      <div class="settings-section">
        <h5 class="settings-section__title">发布控制</h5>
        <el-form-item label="发布状态">
          <el-radio-group v-model="settingsForm.status">
            <el-radio label="0">草稿</el-radio>
            <el-radio label="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>

      <el-divider />

      <div class="settings-section">
        <h5 class="settings-section__title">内容属性</h5>
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
              v-for="category in categories"
              :key="getCategoryValue(category)"
              :label="formatCategoryLabel(category)"
              :value="getCategoryValue(category)"
              :title="formatCategoryLabel(category)"
            >
              <div class="category-option">
                <span class="category-option__name">
                  {{ category.name || category.categoryName || '' }}
                </span>
                <span v-if="category.description || category.remark" class="category-option__desc">
                  {{ category.description || category.remark }}
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
        <h5 class="settings-section__title">管理员选项</h5>
        <el-form-item label="是否置顶">
          <el-switch v-model="settingsForm.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="是否推荐">
          <el-switch v-model="settingsForm.isRecommend" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="允许复制">
          <el-switch v-model="settingsForm.allowCopy" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="访问级别">
          <el-select v-model="settingsForm.accessLevel" placeholder="请选择访问级别" style="width: 100%">
            <el-option label="公开访问" :value="0" />
            <el-option label="会员访问" :value="1" />
            <el-option label="仅自己" :value="2" />
          </el-select>
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="handleDialogClose">取消</el-button>
      <el-button type="primary" @click="handleConfirmPublish" :loading="dialogSubmitting">
        保存并发布
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ArrowLeft, View, Document, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { articleApi } from '@/api/article'
import { categoryApi } from '@/api/category'
import { normalizeCategoryList } from '@/utils/category'
import type { Category } from '@/types/category'

interface PublishSettings {
  status: string
  summary: string
  categoryId: string | null
  tags: string[]
  isTop: number
  isRecommend: number
  allowCopy: number
  accessLevel: number
  allowComments: boolean
  tieredReading: boolean
  enableWatermark: boolean
  remark: string | null
}

interface Props {
  articleId?: string | null
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
  categories?: any[]
  fetchEditorData?: () => Record<string, any> | null | undefined
  applyEditorSettings?: (settings: Record<string, any>) => void
  ensureDraftSaved?: () => Promise<any>
  getPublishPayload?: () => Record<string, any> | null | undefined
  getArticleId?: () => string | null | undefined
}

const props = withDefaults(defineProps<Props>(), {
  articleId: null,
  status: 'draft',
  updatedAt: undefined,
  publishing: false,
  isPublished: false,
  showBackButton: true,
  showStatus: true,
  showPreviewButton: true,
  showSaveDraftButton: true,
  showPublishButton: true,
  backButtonText: '返回列表',
  categories: () => []
})

const emit = defineEmits<{
  back: []
  preview: []
  saveDraft: []
  publish: []
  publishSuccess: [{ id: string }]
}>()

const showSettingsDialog = ref(false)
const dialogSubmitting = ref(false)
const newTag = ref('')
const categories = ref<Category[]>([])
const categoriesLoading = ref(false)

const settingsForm = reactive<PublishSettings>({
  status: '1',
  summary: '',
  categoryId: null,
  tags: [],
  isTop: 0,
  isRecommend: 0,
  allowCopy: 1,
  accessLevel: 0,
  allowComments: true,
  tieredReading: false,
  enableWatermark: false,
  remark: null
})

const resolveCategoryArray = (value: any): any[] => {
  if (Array.isArray(value)) return value
  if (Array.isArray(value?.data)) return value.data
  if (Array.isArray(value?.items)) return value.items
  if (Array.isArray(value?.rows)) return value.rows
  return []
}

const normaliseAllowCopy = (value: unknown) => {
  const numberValue = Number(value)
  if (Number.isNaN(numberValue)) {
    return 1
  }
  return numberValue === 0 ? 0 : 1
}

const normalizeStatusValue = (value: unknown): string | null => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const status = String(value).toLowerCase()
  if (status === '1' || status === 'published' || status === 'public') {
    return '1'
  }
  if (status === '0' || status === 'draft') {
    return '0'
  }
  return null
}

const syncSettingsFromEditor = (snapshot?: Record<string, any> | null | undefined) => {
  const data = snapshot ?? props.fetchEditorData?.()
  if (!data) return

  const normalizedStatus = normalizeStatusValue(data.status)
  if (normalizedStatus !== null) {
    settingsForm.status = normalizedStatus
  }
  settingsForm.summary = data.summary ?? data.excerpt ?? data.previewContent ?? ''
  settingsForm.categoryId = data.categoryId === null || data.categoryId === undefined || data.categoryId === ''
    ? null
    : String(data.categoryId)
  settingsForm.tags = Array.isArray(data.tags) ? [...data.tags] : []
  settingsForm.isTop = Number(data.isTop ?? 0)
  settingsForm.isRecommend = Number(data.isRecommend ?? 0)
  settingsForm.allowCopy = normaliseAllowCopy(data.allowCopy)
  settingsForm.accessLevel = Number(data.accessLevel ?? 0)
  settingsForm.allowComments = data.allowComments === false ? false : true
  settingsForm.tieredReading = Boolean(data.tieredReading)
  settingsForm.enableWatermark = Boolean(data.enableWatermark)
  settingsForm.remark = data.remark ?? null
}

watch(
  () => props.categories,
  (val) => {
    if (!val) return
    const normalized = normalizeCategoryList(resolveCategoryArray(val))
    if (normalized.length) {
      categories.value = normalized
    }
  },
  { immediate: true, deep: true }
)

const getCategoryValue = (category: Category | Record<string, any>) => {
  const value = category?.id ?? category?.categoryId
  return value !== undefined && value !== null ? value.toString() : ''
}

const formatCategoryLabel = (category: Category | Record<string, any>) => {
  const name = category?.name ?? category?.categoryName ?? ''
  const description = category?.description ?? category?.remark ?? ''
  return description ? `${name}（${description}）` : name
}

const loadCategories = async () => {
  try {
    categoriesLoading.value = true
    const response = await categoryApi.getCategories()
    const list = resolveCategoryArray(response)
    const normalized = normalizeCategoryList(list)
    if (normalized.length > 0) {
      categories.value = normalized
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
    if (!categories.value.length && props.categories?.length) {
      categories.value = normalizeCategoryList(resolveCategoryArray(props.categories))
    }
  } finally {
    categoriesLoading.value = false
  }
}

onMounted(() => {
  if (!categories.value.length) {
    categories.value = normalizeCategoryList(resolveCategoryArray(props.categories))
  }
  loadCategories()
  syncSettingsFromEditor()
})

watch(
  () => props.fetchEditorData?.(),
  (snapshot) => {
    syncSettingsFromEditor(snapshot)
  },
  { deep: true, immediate: true }
)

const handleBack = () => emit('back')
const handlePreview = () => emit('preview')
const handleSaveDraft = () => emit('saveDraft')

const handlePublishClick = () => {
  syncSettingsFromEditor()
  showSettingsDialog.value = true
  emit('publish')
}

const handleDialogClose = () => {
  showSettingsDialog.value = false
  dialogSubmitting.value = false
  newTag.value = ''
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

const applySettingsToEditor = () => {
  props.applyEditorSettings?.({
    status: settingsForm.status,
    summary: settingsForm.summary,
    categoryId: settingsForm.categoryId,
    tags: [...settingsForm.tags],
    isTop: settingsForm.isTop,
    isRecommend: settingsForm.isRecommend,
    allowCopy: settingsForm.allowCopy,
    accessLevel: settingsForm.accessLevel,
    allowComments: settingsForm.allowComments,
    tieredReading: settingsForm.tieredReading,
    enableWatermark: settingsForm.enableWatermark,
    remark: settingsForm.remark
  })
}

const handleConfirmPublish = async () => {
  if (dialogSubmitting.value) return

  if (!settingsForm.categoryId) {
    ElMessage.error('请选择文章分类')
    return
  }

  dialogSubmitting.value = true

  try {
    if (!props.applyEditorSettings || !props.ensureDraftSaved || !props.getArticleId) {
      ElMessage.error('缺少编辑器上下文，无法发布')
      return
    }

    applySettingsToEditor()
    await props.ensureDraftSaved()

    const articleId = props.getArticleId()
    if (!articleId) {
      ElMessage.error('文章未保存，无法发布')
      return
    }

    let publishPayload = props.getPublishPayload?.() ?? null
    if (!publishPayload) {
      publishPayload = {
        summary: settingsForm.summary,
        categoryId: settingsForm.categoryId,
        tags: [...settingsForm.tags],
        isTop: settingsForm.isTop,
        isRecommend: settingsForm.isRecommend,
        allowCopy: settingsForm.allowCopy,
        accessLevel: settingsForm.accessLevel,
        allowComments: settingsForm.allowComments,
        tieredReading: settingsForm.tieredReading,
        enableWatermark: settingsForm.enableWatermark,
        remark: settingsForm.remark
      }
    } else {
      publishPayload = {
        ...publishPayload,
        summary: settingsForm.summary,
        categoryId: settingsForm.categoryId,
        tags: [...settingsForm.tags],
        isTop: settingsForm.isTop,
        isRecommend: settingsForm.isRecommend,
        allowCopy: settingsForm.allowCopy,
        accessLevel: settingsForm.accessLevel,
        allowComments: settingsForm.allowComments,
        tieredReading: settingsForm.tieredReading,
        enableWatermark: settingsForm.enableWatermark,
        remark: settingsForm.remark
      }
    }

    await articleApi.publishArticle(articleId, publishPayload)

    settingsForm.status = '1'
    emit('publishSuccess', { id: articleId })
    handleDialogClose()
  } catch (error: any) {
    console.error('发布文章失败:', error)
    // 错误消息已经在request拦截器中显示，这里不再重复显示
  } finally {
    dialogSubmitting.value = false
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'published':
    case '1':
      return 'success'
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
    case 'draft':
    case '0':
    default:
      return '草稿'
  }
}

const formatDate = (date?: Date | string) => {
  if (!date) return '未知'
  const dateObj = typeof date === 'string' ? new Date(date) : date
  return dateObj.toLocaleString('zh-CN')
}
</script>

<style scoped>
.editor-toolbar {
  @apply flex items-center justify-between px-6 py-4 border-b border-gray-200;
  min-height: 64px;
  background: transparent;
}

.editor-toolbar__left {
  @apply flex items-center gap-6;
}

.toolbar-back {
  @apply text-gray-600 hover:text-gray-900;
}

.toolbar-back :deep(.el-button__text-wrapper) {
  @apply flex items-center gap-2;
}

.editor-toolbar__meta {
  @apply flex items-center gap-4;
}

.meta-item {
  @apply text-sm text-gray-600 flex items-center gap-2;
}

.editor-toolbar__actions {
  @apply flex items-center gap-3;
}

.toolbar-action {
  @apply flex items-center gap-2;
}

.toolbar-action :deep(.el-button__text-wrapper) {
  @apply flex items-center gap-1;
}

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
  @apply flex flex-col leading-tight;
}

.category-option__name {
  @apply text-sm text-gray-900;
}

.category-option__desc {
  @apply text-xs text-gray-500;
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

@media (max-width: 768px) {
  .editor-toolbar {
    @apply flex-col items-stretch gap-3 px-4 py-3;
  }

  .editor-toolbar__left {
    @apply flex-col items-start gap-2;
  }

  .editor-toolbar__meta {
    @apply flex-wrap gap-2;
  }

  .editor-toolbar__actions {
    @apply flex-wrap gap-2;
  }

  .toolbar-action {
    @apply flex-1 justify-center min-w-0;
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
