<template>
  <div class="post-editor" :class="{ 'post-editor--embedded': props.embedded }">
    <div class="editor-shell">
      <PostEditorHeader
        v-if="!props.hideToolbar"
        :post="post"
        :publishing="publishing"
        :is-publish-mode="isPublishMode"
        :fetch-editor-data="getEditorSnapshot"
        :apply-editor-settings="applyToolbarSettings"
        :ensure-draft-saved="ensureDraftSaved"
        :get-publish-payload="getToolbarPublishPayload"
        :get-article-id="getCurrentArticleId"
        @back="handleBack"
        @preview="handlePreview"
        @save-draft="handleSaveDraft"
        @settings="handleSettings"
        @publish="handlePublish"
        @save-settings="handleToolbarSaveSettings"
        @publish-success="handleToolbarPublishSuccess"
        @dialog-close="handleToolbarDialogClose"
        @share="handleShare"
      />

      <div class="editor-container">
        <PostEditorMain
          v-model:title="post.title"
          v-model:content="post.content"
          :show-sidebar="showSidebar"
          :upload-image="uploadImage"
          @title-change="handleTitleChange"
        />

        <PostEditorSidebar
          :show-sidebar="showSidebar"
          :word-count="wordCount"
          :preview-length="post.previewContent.length"
          :outline-headings="outlineHeadings"
          @toggle-sidebar="toggleSidebar"
          @scroll-to-outline="scrollToOutline"
        />
      </div>

    <PostEditorAutoSaveIndicator
      :auto-save-state="autoSaveState"
      :format-save-time="formatSaveTime"
    />
  </div>

  <PostEditorPreviewDialog
    v-model="showPreviewDialog"
    v-model:fullscreen="previewFullscreen"
      :post="post"
      :current-user="currentUser"
      :format-date="formatDate"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api/article'
import { useAutoSave } from '@/composables/useAutoSave'
import PostEditorHeader from './components/PostEditorHeader.vue'
import PostEditorMain from './components/PostEditorMain.vue'
import PostEditorSidebar from './components/PostEditorSidebar.vue'
import PostEditorAutoSaveIndicator from './components/PostEditorAutoSaveIndicator.vue'
import PostEditorPreviewDialog from './components/PostEditorPreviewDialog.vue'
import type { EditorPost, EditorUser, OutlineHeading } from './types'
import { splitMemberContent, mergeMemberContent } from '@/utils/memberContent'
import { ensureAbsoluteUrl } from '@/utils/url'

const props = defineProps<{ embedded?: boolean; id?: string; initial?: any; hideToolbar?: boolean }>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', id: string): void
  (e: 'published', id: string): void
}>()

const route = useRoute()
const router = useRouter()

const normalizeStatus = (value: unknown): EditorPost['status'] => {
  if (value === null || value === undefined) {
    return 'draft'
  }
  const normalized = String(value).trim().toLowerCase()
  if (normalized === '1' || normalized.startsWith('publish') || normalized === 'public') {
    return 'published'
  }
  if (normalized === '2' || normalized === 'private' || normalized === 'archived' || normalized === 'offline') {
    return 'private'
  }
  return 'draft'
}

const post = reactive<EditorPost>({
  title: '',
  content: '',
  memberContent: '',
  excerpt: '',
  summary: '',
  tags: [],
  coverImage: '',
  status: 'draft',
  allowComments: true,
  isTop: 0,
  published: false,
  previewContent: '',
  tieredReading: true,
  allowCopy: 1,
  enableWatermark: false,
  isRecommend: 0,
  accessLevel: 0,
  passwordEnabled: false,
  password: '',
  hasExistingPassword: false,
})

const publishing = ref(false)
const showPreviewDialog = ref(false)
const showSettingsDialog = ref(false)
const isPublishMode = ref(false)
const previewFullscreen = ref(false)
const showSidebar = ref(false)
const newTag = ref('')

const ABSOLUTE_URL_PATTERN = /^(?:https?:)?\/\//i
const SPECIAL_SCHEME_PATTERN = /^(?:data:|blob:|javascript:|mailto:|tel:)/i

const normalizeUploadPath = (value: string) => {
  if (!value) {
    return ''
  }
  let path = value.trim()
  if (!path) {
    return ''
  }

  if (path.startsWith('./')) {
    path = path.slice(1)
  }

  const replace = (source: string, target: string) => {
    if (path.startsWith(source)) {
      path = path.replace(source, target)
    }
  }

  replace('/api/upload/upload/', '/upload/')
  replace('api/upload/upload/', '/upload/')
  replace('/api/upload/', '/upload/')
  replace('api/upload/', '/upload/')

  if (path.startsWith('upload/')) {
    path = `/${path}`
  }

  return path
}

const normalizeMediaSrc = (value?: string | null): string => {
  if (!value) {
    return ''
  }
  const trimmed = value.trim()
  if (!trimmed || SPECIAL_SCHEME_PATTERN.test(trimmed)) {
    return trimmed
  }
  if (ABSOLUTE_URL_PATTERN.test(trimmed)) {
    return trimmed
  }
  const normalizedPath = normalizeUploadPath(trimmed)
  return ensureAbsoluteUrl(normalizedPath)
}

const normalizeContentMediaUrls = (html?: string | null) => {
  if (!html || typeof html !== 'string' || !html.trim()) {
    return html || ''
  }

  if (typeof window === 'undefined' || typeof DOMParser === 'undefined') {
    return html
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')
    doc.body?.querySelectorAll('img').forEach((img) => {
      const src = img.getAttribute('src')
      const normalized = normalizeMediaSrc(src)
      if (normalized && normalized !== src) {
        img.setAttribute('src', normalized)
      }
    })
    return doc.body?.innerHTML ?? html
  } catch (error) {
    console.warn('normalizeContentMediaUrls failed:', error)
    return html
  }
}

const buildPasswordPayload = () => {
  const payload: Record<string, any> = {
    passwordEnabled: post.passwordEnabled,
    isPasswd: post.passwordEnabled ? 0 : 1
  }
  const trimmedPassword = (post.password ?? '').trim()
  if (post.passwordEnabled) {
    payload.passwordProtected = true
    if (trimmedPassword) {
      payload.password = trimmedPassword
    }
  } else {
    payload.passwordProtected = false
    if (post.hasExistingPassword) {
      payload.clearPassword = true
    }
  }
  return payload
}

const prepareContentPayload = () => {
  const { content, memberContent } = splitMemberContent(post.content || '')
  post.memberContent = memberContent || ''
  return { content, memberContent }
}

const normalizeCoverValue = (value?: string | null) => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value)
}

const withCoverAliases = (value?: string | null) => {
  const normalized = normalizeCoverValue(value)
  return {
    coverImage: normalized,
    cover: normalized,
    cover_url: normalized,
    coverUrl: normalized
  }
}

const buildAutoSavePayload = () => {
  const { content, memberContent } = prepareContentPayload()
  return {
    title: post.title,
    content,
    memberContent,
    excerpt: post.excerpt ?? '',
    tags: Array.isArray(post.tags) ? [...post.tags] : [],
    status: post.status,
    published: post.status === 'published',
    allowComments: post.allowComments,
    isTop: post.isTop,
    pinned: post.isTop === 1,
    categoryId: post.categoryId && post.categoryId !== '' ? post.categoryId : undefined,
    summary: post.summary ?? post.excerpt ?? post.previewContent ?? '',
    previewContent: post.previewContent ?? post.excerpt ?? post.summary ?? '',
    tieredReading: post.tieredReading,
    allowCopy: post.allowCopy,
    enableWatermark: post.enableWatermark,
    isRecommend: post.isRecommend === 1,
    accessLevel: post.accessLevel,
    ...buildPasswordPayload(),
    ...withCoverAliases(post.coverImage)
  }
}

const buildPublishPayload = () => {
  const { content, memberContent } = prepareContentPayload()
  return {
    title: post.title,
    content,
    memberContent,
    summary: post.excerpt || post.previewContent || '',
    previewContent: post.previewContent,
    categoryId: post.categoryId && post.categoryId !== '' ? post.categoryId : undefined,
    tags: Array.isArray(post.tags) ? [...post.tags] : [],
    accessLevel: post.accessLevel,
    allowComments: post.allowComments,
    pinned: post.isTop === 1,
    isRecommend: post.isRecommend === 1,
    tieredReading: post.tieredReading,
    allowCopy: post.allowCopy === 1,
    enableWatermark: post.enableWatermark,
    ...buildPasswordPayload(),
    ...withCoverAliases(post.coverImage)
  }
}

const getEditorSnapshot = () => ({
  status: post.status,
  summary: post.summary ?? post.excerpt ?? '',
  categoryId: post.categoryId ?? null,
  tags: Array.isArray(post.tags) ? [...post.tags] : [],
  allowCopy: typeof post.allowCopy === 'number' ? post.allowCopy : post.allowCopy ? 1 : 0,
  accessLevel: post.accessLevel,
  ...withCoverAliases(post.coverImage),
  passwordEnabled: post.passwordEnabled,
  hasExistingPassword: post.hasExistingPassword
})

const ensureDraftSaved = async () => {
  const savedArticle = await saveImmediately(buildAutoSavePayload())
  if (savedArticle) {
    applyArticleMeta(savedArticle as any)
  }
}

const getToolbarPublishPayload = () => {
  const basePayload = buildPublishPayload()

  return {
    ...basePayload,
    summary: post.summary || basePayload.summary || '',
    previewContent:
      post.previewContent || basePayload.previewContent || post.summary || post.excerpt || '',
    categoryId: post.categoryId ?? undefined,
    tags: Array.isArray(post.tags) ? [...post.tags] : basePayload.tags,
    allowCopy: post.allowCopy,
    accessLevel: post.accessLevel
  }
}

const getCurrentArticleId = () => currentArticleId.value || null

const resetPublishDialogState = () => {
  showSettingsDialog.value = false
  isPublishMode.value = false
}

const handleToolbarSaveSettings = (settings: Record<string, any>) => {
  applyToolbarSettings(settings)
  resetPublishDialogState()
}

const handleToolbarPublishSuccess = ({ id }: { id: string }) => {
  post.status = 'published'
  post.published = true
  post.publishedAt = new Date()
  post.hasExistingPassword = post.passwordEnabled
  post.password = ''
  resetPublishDialogState()
  publishing.value = false
  if (id) {
    setArticleId(id)
  }
  emit('published', id)
  router.push('/profile/articles')
}

const handleToolbarDialogClose = () => {
  resetPublishDialogState()
}

const autoSaveOptions = {
  interval: 15000,
  debounceDelay: 5000,
  minLength: 10,
  maxSaveFrequency: 8000,
  getData: buildAutoSavePayload
}

const {
  autoSaveState,
  currentArticleId,
  setArticleId,
  startAutoSave,
  stopAutoSave,
  saveImmediately
} = useAutoSave(autoSaveOptions)

const extractArticleId = (article?: Partial<EditorPost> | null): string | null => {
  if (!article) {
    return null
  }

  const candidates = [
    (article as any)?.articleId,
    (article as any)?.id
  ]

  const detected = candidates.find(value => value !== undefined && value !== null)
  return detected !== undefined && detected !== null ? detected.toString() : null
}

const applyContentFromSource = (source?: Partial<EditorPost> | null) => {
  if (!source) {
    return
  }

  const memberValue = (source as any)?.memberContent ?? (source as any)?.member_content
  if (memberValue !== undefined) {
    post.memberContent = typeof memberValue === 'string' ? memberValue : ''
  }

  if ((source as any)?.content !== undefined) {
    const merged = mergeMemberContent((source as any).content || '', post.memberContent)
    post.content = normalizeContentMediaUrls(merged)
  } else if (memberValue !== undefined) {
    const merged = mergeMemberContent(post.content, post.memberContent)
    post.content = normalizeContentMediaUrls(merged)
  }
}

const applyArticleMeta = (article?: Partial<EditorPost> | null) => {
  const detectedId = extractArticleId(article)
  if (detectedId) {
    post.id = detectedId
    setArticleId(detectedId)
  }

  if (article) {
    applyContentFromSource(article)
    const incomingIsPasswd = (article as any).isPasswd ?? (article as any).is_passwd
    if (incomingIsPasswd !== undefined && incomingIsPasswd !== null) {
      const numeric = Number(incomingIsPasswd)
      if (!Number.isNaN(numeric)) {
        post.passwordEnabled = numeric === 0
        post.hasExistingPassword = post.passwordEnabled
      }
    } else {
      const passwordProtected =
        article.passwordEnabled ??
        (article as any).passwordProtected ??
        (article as any).hasExistingPassword ??
        (article as any).hasPassword
      post.passwordEnabled = passwordProtected === true || passwordProtected === '1' || passwordProtected === 1
      post.hasExistingPassword = post.passwordEnabled
    }
    post.password = ''

    const rawCover =
      (article as any).coverImage ??
      (article as any).cover_image ??
      (article as any).coverUrl ??
      (article as any).cover ??
      ''
    post.coverImage = typeof rawCover === 'string' ? rawCover : rawCover ? String(rawCover) : ''
  }
}

const currentUser = ref<EditorUser>({
  id: '1',
  name: '当前用户',
  email: 'user@example.com',
})

const availableTags = ref<string[]>([
  'Vue.js',
  'TypeScript',
  'JavaScript',
  'CSS',
  'HTML',
  '前端',
  '后端',
  '全栈',
])

const isEditing = computed(() => !!(props?.id || route.params.id || (route.query.id as string | undefined)))

const wordCount = computed(() => {
  const text = post.content.replace(/<[^>]*>/g, '')
  return text.length
})

const outlineHeadings = computed<OutlineHeading[]>(() => {
  const headings: OutlineHeading[] = []
  const parser = new DOMParser()
  const doc = parser.parseFromString(post.content, 'text/html')
  const headingElements = doc.querySelectorAll('h1, h2, h3, h4, h5, h6')

  headingElements.forEach((heading) => {
    headings.push({
      text: heading.textContent || '',
      level: parseInt(heading.tagName.charAt(1))
    })
  })

  return headings
})

watch(
  () => post.tieredReading,
  (enabled) => {
    if (!enabled) {
      post.previewContent = ''
    }
  }
)

watch(showSettingsDialog, (visible) => {
  if (!visible) {
    isPublishMode.value = false
  }
})

const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

const scrollToOutline = (index: number) => {
  const headings = document.querySelectorAll<HTMLElement>('.content-section .ProseMirror h1, .content-section .ProseMirror h2, .content-section .ProseMirror h3, .content-section .ProseMirror h4, .content-section .ProseMirror h5, .content-section .ProseMirror h6')
  const target = headings[index]
  if (!target) return
  target.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const handleBack = async () => {
  if (hasUnsavedChanges()) {
    try {
      await ElMessageBox.confirm(
        '您有未保存的更改，确定要离开吗？',
        '确认离开',
        {
          confirmButtonText: '离开',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
    } catch {
      return
    }
  }
  if (props.embedded) {
    emit('close')
  } else {
    router.push('/profile/articles')
  }
}

const handleTitleChange = (title: string) => {
  post.title = title
}

const handlePreview = () => {
  showPreviewDialog.value = true
}

const handleSettings = () => {
  isPublishMode.value = false
  showSettingsDialog.value = true
}

const handleSaveSettings = async () => {
  try {
    const savedArticle = await saveImmediately(buildAutoSavePayload())
    if (savedArticle) {
      applyArticleMeta(savedArticle as any)
    } else {
      post.password = post.passwordEnabled ? post.password : ''
      if (!post.passwordEnabled) {
        post.hasExistingPassword = false
      }
    }

    if (isPublishMode.value && currentArticleId.value) {
      const publishPayload = buildPublishPayload()
      const publishedArticle = await articleApi.publishArticle(currentArticleId.value, publishPayload)
      applyArticleMeta(publishedArticle as any)
      post.status = 'published'
      post.publishedAt = new Date()
      post.published = true
      post.password = ''

      showSettingsDialog.value = false
      isPublishMode.value = false

      ElMessage.success('文章发布成功！正在跳转到个人文章页面...')
      setTimeout(() => {
        router.push('/profile/articles')
      }, 1500)
    } else {
      if (!post.passwordEnabled) {
        post.password = ''
      }
      showSettingsDialog.value = false
      ElMessage.success('设置保存成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleImmediatePublish = async () => {
  try {
    publishing.value = true

    if (!post.categoryId) {
      ElMessage.error('请选择文章分类')
      publishing.value = false
      return
    }

    const draftPayload = buildAutoSavePayload()
    const savedArticle = await saveImmediately(draftPayload)
    if (savedArticle) {
      applyArticleMeta(savedArticle as any)
    }

    if (currentArticleId.value) {
      const publishPayload = buildPublishPayload()
      const publishedArticle = await articleApi.publishArticle(currentArticleId.value, publishPayload)
      applyArticleMeta(publishedArticle as any)
      post.status = 'published'
      post.publishedAt = new Date()
      post.published = true
      post.password = ''

      showSettingsDialog.value = false
      isPublishMode.value = false

      ElMessage.success('文章发布成功！正在跳转到个人文章页面...')
      setTimeout(() => {
        router.push('/profile/articles')
      }, 1500)
    } else {
      ElMessage.error('文章ID不存在，无法发布')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发布失败')
    console.error('Immediate publish error:', error)
  } finally {
    publishing.value = false
  }
}

const handlePublish = async () => {
  try {
    publishing.value = true

    const userStore = useUserStore()
    if (!userStore.hasValidToken()) {
      ElMessage.error('请先登录后再发布文章')
      router.push('/auth/login')
      return
    }

    if (!post.title || post.title.trim() === '') {
      ElMessage.error('请输入文章标题')
      publishing.value = false
      return
    }

    if (!post.content || post.content.trim() === '') {
      ElMessage.error('请输入文章内容')
      publishing.value = false
      return
    }

    const draftPayload = buildAutoSavePayload()
    const savedArticle = await saveImmediately(draftPayload)
    if (savedArticle) {
      applyArticleMeta(savedArticle as any)
    }

    if (!currentArticleId.value) {
      ElMessage.error('保存文章失败，无法发布')
      publishing.value = false
      return
    }

    isPublishMode.value = true
    showSettingsDialog.value = true

  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
    console.error('Publish error:', error)
  } finally {
    publishing.value = false
  }
}

const handleSaveDraft = async () => {
  try {
    const userStore = useUserStore()

    if (!userStore.hasValidToken()) {
      ElMessage.error('请先登录后再保存草稿')
      router.push('/auth/login')
      return
    }

    post.status = 'draft'
    post.published = false

    const savedArticle = await saveImmediately(buildAutoSavePayload())
    if (savedArticle) {
      applyArticleMeta(savedArticle as any)
    }
    ElMessage.success('保存为草稿成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
    console.error('Save draft error:', error)
    console.error('Error response:', error.response)
  }
}

const handleShare = async () => {
  try {
    if (post.status !== 'published') {
      ElMessage.warning('请先发布文章后再分享')
      return
    }

    if (!currentArticleId.value) {
      ElMessage.error('无法获取文章信息')
      return
    }

    const shareUrl = `${window.location.origin}/article/${currentArticleId.value}/reader`

    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('文章链接已复制到剪贴板')

    console.log('分享文章:', {
      articleId: currentArticleId.value,
      title: post.title,
      shareUrl
    })
  } catch (error: any) {
    console.error('Share error:', error)
    ElMessage.error('分享失败')
  }
}

const uploadImage = async (file: File): Promise<{ url: string }> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const url = URL.createObjectURL(file)
      resolve({ url })
    }, 1000)
  })
}

const addTag = () => {
  if (newTag.value.trim() && !post.tags.includes(newTag.value.trim())) {
    post.tags.push(newTag.value.trim())
    newTag.value = ''
  }
}

const removeTag = (index: number) => {
  post.tags.splice(index, 1)
}

const hasUnsavedChanges = (): boolean => {
  return post.title.trim() !== '' || post.content.trim() !== ''
}

const formatDate = (date?: Date): string => {
  if (!date) return '未知'
  return new Date(date).toLocaleString('zh-CN')
}

const formatSaveTime = (date: Date): string => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  } else if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  } else {
    return date.toLocaleString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const prefillFromInitial = (src: any) => {
  if (!src) return
  try {
    post.id = (src.id || src.articleId)?.toString() || post.id
    post.title = src.title ?? post.title
    applyContentFromSource(src)
    post.summary = src.summary ?? src.excerpt ?? post.summary ?? ''
    post.excerpt = src.excerpt ?? src.summary ?? post.excerpt
    post.previewContent = src.previewContent ?? src.summary ?? post.previewContent
    const tieredValue = src.tieredReading ?? src.enableTieredRead
    post.tieredReading = tieredValue === undefined ? post.tieredReading : (tieredValue === true || tieredValue === '1')
    const allowCopyValue = src.allowCopy
    if (allowCopyValue !== undefined) {
      post.allowCopy = (allowCopyValue === true || allowCopyValue === '1' || allowCopyValue === 1) ? 1 : 0
    }
    const watermarkValue = src.enableWatermark ?? src.watermarkEnabled
    if (watermarkValue !== undefined) post.enableWatermark = watermarkValue === true || watermarkValue === '1'
    post.tags = Array.isArray(src.tags) ? [...src.tags] : post.tags
    post.categoryId = src.categoryId === null || src.categoryId === undefined || src.categoryId === ''
      ? null
      : String(src.categoryId)
    const statusRaw = src.status
    if (statusRaw !== undefined && statusRaw !== null) {
      const normalizedStatus = normalizeStatus(statusRaw)
      post.status = normalizedStatus
      post.published = normalizedStatus === 'published'
    }
    if (src.allowComments !== undefined) {
      post.allowComments = !(src.allowComments === false || src.allowComments === '0' || src.allowComments === 0)
    }
    post.isTop = src.isTop === '1' || src.isTop === 1 || src.pinned === true ? 1 : 0
    const recommendValue = src.isRecommend
    if (recommendValue !== undefined) {
      post.isRecommend = (recommendValue === true || recommendValue === '1' || recommendValue === 1) ? 1 : 0
    }
    const accessLevelValue = src.accessLevel
    if (accessLevelValue !== undefined) post.accessLevel = typeof accessLevelValue === 'number' ? accessLevelValue : parseInt(accessLevelValue) || 0
    post.publishedAt = src.publishTime ? new Date(src.publishTime) : (src.publishedAt ? new Date(src.publishedAt) : post.publishedAt)
    post.createdAt = src.createTime ? new Date(src.createTime) : (src.createdAt ? new Date(src.createdAt) : post.createdAt)
    post.updatedAt = src.updateTime ? new Date(src.updateTime) : (src.updatedAt ? new Date(src.updatedAt) : post.updatedAt)
    applyArticleMeta(src)
  } catch {}
}

let lastLoadId = ''
let isLoadingPost = false

const loadPost = async () => {
  try {
    if (props.initial) {
      prefillFromInitial(props.initial)
    }
    const rawId = (props?.id as string | undefined) || (route.params.id as string | undefined) || (route.query.id as string | undefined)
    if (!rawId) return
    const id = String(rawId)

    if (isLoadingPost && lastLoadId === id) {
      return
    }

    if (lastLoadId === id && post.id === id) {
      return
    }

    lastLoadId = id
    isLoadingPost = true

  const result = await articleApi.getArticleForEdit(id)

  post.id = extractArticleId(result) || id
  post.title = result.title || ''
  applyContentFromSource(result as any)
    post.summary = (result as any).summary || (result as any).excerpt || ''
    post.excerpt = (result as any).excerpt || (result as any).summary || ''
    post.previewContent = (result as any).previewContent || (result as any).summary || ''
    const tieredValue = (result as any).tieredReading ?? (result as any).enableTieredRead
    post.tieredReading = tieredValue === undefined ? true : tieredValue === true || tieredValue === '1'
    const allowCopyValue = (result as any).allowCopy
    post.allowCopy = (allowCopyValue === true || allowCopyValue === '1' || allowCopyValue === 1) ? 1 : 0
    const watermarkValue = (result as any).watermarkEnabled ?? (result as any).enableWatermark
    post.enableWatermark = watermarkValue === true || watermarkValue === '1'
    const resultTags = (result as any).tags
    post.tags = Array.isArray(resultTags) ? [...resultTags] : []
    post.categoryId = (result as any).categoryId ? String((result as any).categoryId) : null
    const normalizedStatus = normalizeStatus((result as any).status)
    post.status = normalizedStatus
    post.published = normalizedStatus === 'published'
    post.allowComments = (result as any).allowComments !== false
    post.isTop = (result as any).isTop === '1' || (result as any).isTop === 1 ? 1 : 0
    post.isRecommend = (result as any).isRecommend === '1' || (result as any).isRecommend === 1 || (result as any).isRecommend === true ? 1 : 0
    post.accessLevel = typeof (result as any).accessLevel === 'number' ? (result as any).accessLevel : parseInt(String((result as any).accessLevel)) || 0
    post.publishedAt = (result as any).publishTime ? new Date((result as any).publishTime) : undefined
    post.createdAt = (result as any).createTime ? new Date((result as any).createTime) : undefined
    post.updatedAt = (result as any).updateTime ? new Date((result as any).updateTime) : undefined
    applyArticleMeta(result as any)
  } catch (error: any) {
    console.warn('Load post warn:', error?.message || error)
    if (!props.initial) {
      ElMessage.warning(error?.message || '文章详情未获取，已保留现有内容')
    }
  } finally {
    isLoadingPost = false
  }
}

onMounted(async () => {
  await loadPost()
})

onMounted(async () => {
  const userStore = useUserStore()
  userStore.init()

  if (isEditing.value) {
    const articleId = (props.id as string | undefined) || (route.params.id as string | undefined) || (route.query.id as string | undefined)
    if (articleId) setArticleId(articleId)

    try {
      await loadPost()
    } catch (error) {
      console.warn('加载文章失败，将作为新文章处理', error)
    }
  }

  startAutoSave()

  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  stopAutoSave()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  if (hasUnsavedChanges()) {
    event.preventDefault()
    event.returnValue = ''
  }
}

const normalizeTagInput = (tags: unknown): string[] => {
  if (Array.isArray(tags)) {
    return tags.map(tag => String(tag).trim()).filter(tag => tag.length > 0)
  }
  if (typeof tags === 'string') {
    return tags
      .split(',')
      .map(tag => tag.trim())
      .filter(tag => tag.length > 0)
  }
  return []
}

const applyToolbarSettings = (settings: Partial<EditorPost> & Record<string, any>) => {
  if (!settings) return

  if (Object.prototype.hasOwnProperty.call(settings, 'status')) {
    const value = settings.status
    if (value !== undefined && value !== null && value !== '') {
      const statusValue = String(value)
      const normalizedStatus = normalizeStatus(statusValue)
      post.status = normalizedStatus
      post.published = normalizedStatus === 'published'
    }
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'summary')) {
    post.summary = settings.summary ?? ''
    post.excerpt = settings.summary ?? ''
  }

  if (
    Object.prototype.hasOwnProperty.call(settings, 'coverImage') ||
    Object.prototype.hasOwnProperty.call(settings, 'cover_image') ||
    Object.prototype.hasOwnProperty.call(settings, 'cover') ||
    Object.prototype.hasOwnProperty.call(settings, 'coverUrl')
  ) {
    const value =
      (settings as any).coverImage ??
      (settings as any).cover_image ??
      (settings as any).cover ??
      (settings as any).coverUrl ??
      (settings as any).cover_url ??
      ''
    post.coverImage = typeof value === 'string' ? value : value ? String(value) : ''
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'categoryId')) {
    const value = settings.categoryId
    post.categoryId = value === null || value === undefined || value === '' ? null : String(value)
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'tags')) {
    post.tags = normalizeTagInput(settings.tags)
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'isTop')) {
    const value = Number(settings.isTop)
    post.isTop = Number.isNaN(value) ? post.isTop : value
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'isRecommend')) {
    const value = Number(settings.isRecommend)
    post.isRecommend = Number.isNaN(value) ? post.isRecommend : value
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'allowCopy')) {
    const value = Number(settings.allowCopy)
    post.allowCopy = value === 0 ? 0 : 1
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'accessLevel')) {
    const value = Number(settings.accessLevel)
    post.accessLevel = Number.isNaN(value) ? post.accessLevel : value
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'isPasswd')) {
    const value = Number((settings as any).isPasswd)
    if (!Number.isNaN(value)) {
      post.passwordEnabled = value === 0
      post.hasExistingPassword = post.passwordEnabled
      if (!post.passwordEnabled) {
        post.password = ''
      }
    }
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'passwordEnabled')) {
    post.passwordEnabled = Boolean(settings.passwordEnabled)
    if (!post.passwordEnabled) {
      post.password = ''
    }
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'hasExistingPassword')) {
    post.hasExistingPassword = Boolean(settings.hasExistingPassword)
  } else if (!post.passwordEnabled) {
    post.hasExistingPassword = false
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'password')) {
    const value = settings.password
    if (typeof value === 'string') {
      post.password = value
    }
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'allowComments')) {
    const value = settings.allowComments
    if (typeof value === 'boolean') {
      post.allowComments = value
    } else if (value === 0 || value === '0') {
      post.allowComments = false
    } else if (value === 1 || value === '1') {
      post.allowComments = true
    }
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'tieredReading')) {
    post.tieredReading = Boolean(settings.tieredReading)
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'enableWatermark')) {
    post.enableWatermark = Boolean(settings.enableWatermark)
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'previewContent')) {
    post.previewContent = settings.previewContent ?? post.previewContent
  }

  if (Object.prototype.hasOwnProperty.call(settings, 'remark')) {
    (post as any).remark = settings.remark
  }
}

const getPostSnapshot = () => {
  return {
    title: post.title,
    content: post.content,
    memberContent: post.memberContent,
    summary: post.summary,
    coverImage: post.coverImage,
    status: post.status,
    categoryId: post.categoryId,
    tags: Array.isArray(post.tags) ? [...post.tags] : [],
    isTop: post.isTop,
    isRecommend: post.isRecommend,
    allowCopy: post.allowCopy,
    accessLevel: post.accessLevel,
    allowComments: post.allowComments,
    tieredReading: post.tieredReading,
    enableWatermark: post.enableWatermark,
    previewContent: post.previewContent,
    remark: (post as any).remark ?? null,
    passwordEnabled: post.passwordEnabled,
    hasExistingPassword: post.hasExistingPassword
  }
}

const saveDraftForPublish = async () => {
  const savedArticle = await saveImmediately(buildAutoSavePayload())
  if (savedArticle) {
    applyArticleMeta(savedArticle as any)
  }
  return currentArticleId.value
}

const getPublishPayloadForToolbar = () => buildPublishPayload()

const getCurrentArticleIdentifier = () => currentArticleId.value

defineExpose({
  applyToolbarSettings,
  getPostSnapshot,
  saveDraftForPublish,
  getPublishPayload: getPublishPayloadForToolbar,
  getCurrentArticleId: getCurrentArticleIdentifier
})
</script>

<style scoped>
.post-editor {
  @apply flex flex-col;
  background: transparent;
  min-height: calc(100vh - var(--main-top-offset, 96px) - 20px);
  height: calc(100vh - var(--main-top-offset, 96px) - 20px);
  padding-bottom: 20px;
  overflow-y: auto;
  scrollbar-gutter: stable;
}

.post-editor--embedded {
  min-height: auto;
  height: auto;
  overflow: visible;
}

.editor-shell {
  @apply flex flex-col gap-6;
  flex: 1 1 auto;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 32px;
  min-height: 100%;
  overflow: visible;
}

.post-editor--embedded .editor-shell {
  min-height: auto;
}

.editor-container {
  @apply flex-1 flex gap-6;
  width: 100%;
  overflow-x: hidden;
  overflow-y: visible;
  min-height: 0;
}

.post-editor--embedded .editor-container {
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}

@media (max-width: 1024px) {
  .editor-shell {
    padding: 20px;
  }

  .editor-container {
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .post-editor {
    padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
    scroll-padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  }

  .editor-shell {
    padding: 16px;
  }

  .editor-container {
    @apply flex-col;
  }
}
</style>
