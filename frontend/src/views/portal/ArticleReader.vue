<template>
  <div class="reader-layout" v-if="!loading && article">
    <header class="reader-hero">
      <div class="hero-overlay"></div>
      <img v-if="heroCoverUrl" :src="heroCoverUrl" :alt="article.title" class="hero-background" />
      <div class="hero-content">
        <div class="hero-meta">
          <button class="back-button" type="button" @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上一页
          </button>
          <div class="meta-divider"></div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>{{ displayAuthorName }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>{{ readableDate }}</span>
          </div>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ formatNumber(article.viewCount) }} 次阅读</span>
          </div>
        </div>
        <div v-if="membershipRequired" class="hero-badge-row">
          <div class="hero-vip-badge" :title="membershipBadgeText">
            <el-icon><StarFilled /></el-icon>
            <span>{{ membershipBadgeText }}</span>
          </div>
        </div>
        <h1 class="hero-title">{{ article.title }}</h1>
        <p v-if="article.summary" class="hero-summary">{{ article.summary }}</p>
        <div class="hero-author">
          <div class="avatar">{{ displayAuthorInitial }}</div>
          <div class="author-text">
            <div class="author-name">{{ displayAuthorName }}</div>
            <div class="author-sub">{{ readableDate }}</div>
          </div>
        </div>
      </div>
    </header>

    <div :class="['reader-body', { 'reader-body--full': !tableOfContents.length }]">
      <aside v-if="tableOfContents.length" class="reader-toc">
        <div class="toc-header">章节导航</div>
        <nav class="toc-list">
          <button
            v-for="item in tableOfContents"
            :key="item.id"
            type="button"
            class="toc-item"
            :class="[`level-${item.level}`, { active: activeHeading === item.id }]"
            @click="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </button>
        </nav>
      </aside>

      <main class="reader-content">
        <div v-if="requiresPassword" class="reader-password-gate">
          <el-icon class="reader-password-gate__icon"><Lock /></el-icon>
          <h3 class="reader-password-gate__title">该文章已加密</h3>
          <p class="reader-password-gate__message">{{ passwordHint }}</p>
          <form class="reader-password-gate__form" @submit.prevent="unlockArticle" autocomplete="off">
            <el-input
              v-model="passwordInput"
              type="password"
              size="large"
              placeholder="输入访问密码"
              autocomplete="new-password"
              class="reader-password-gate__input"
              :disabled="unlocking"
              :input-style="{ 
                'autocomplete': 'new-password',
                'data-form-type': 'other'
              }"
              :native-input-props="{
                'autocomplete': 'new-password',
                'data-form-type': 'other',
                'data-lpignore': 'true',
                'data-1p-ignore': 'true'
              }"
            />
            <div class="reader-password-gate__actions">
              <el-button
                type="primary"
                size="large"
                class="reader-password-gate__submit"
                :loading="unlocking"
                :disabled="unlocking"
                @click.prevent="unlockArticle"
              >
                解锁文章
              </el-button>
              <el-button
                type="default"
                size="large"
                class="reader-password-gate__support"
                :disabled="unlocking"
                @click.prevent="openPasswordSupport"
              >
                获取密码
              </el-button>
            </div>
          </form>
          <p v-if="passwordError" class="reader-password-gate__error">{{ passwordError }}</p>
          <el-dialog
            v-model="passwordSupportVisible"
            v-if="requiresPassword"
            title="联系站长获取密码"
            width="420px"
            class="password-support-dialog"
            append-to-body
          >
            <div class="password-support-dialog__body">
              <p class="password-support-dialog__intro">
                {{ passwordSupportRemark }}
              </p>
              <div v-if="passwordSupportQr" class="password-support-dialog__qr">
                <img :src="passwordSupportQr" alt="站长联系方式二维码" />
              </div>
              <div v-else class="password-support-dialog__placeholder" :class="{ 'password-support-dialog__placeholder--loading': ownerSupportLoading }">
                暂未配置二维码，请通过站长联系方式获取密码。
              </div>
              <div class="password-support-dialog__contact">
                <p v-if="tenantContactName">站长：{{ tenantContactName }}</p>
                <p v-if="tenantContactPhone">联系方式：{{ tenantContactPhone }}</p>
              </div>
            </div>
          </el-dialog>
        </div>
        <template v-else>
          <div v-if="protectContent" class="reader-protect-banner">
          <el-icon class="reader-protect-banner__icon"><Lock /></el-icon>
          <div class="reader-protect-banner__text">
            <strong>版权保护已开启</strong>
            <span>本文已禁用复制、打印及导出，请勿外传。</span>
          </div>
        </div>
        <article
          ref="contentWrapper"
          class="markdown-surface"
          v-content-protect="protectContent"
          v-html="visibleContent"
        ></article>
        <transition name="fade">
          <div v-if="shouldShowPreview" class="preview-gate">
            <div class="preview-gate__content">
              <h3>会员专享 · 解锁全文</h3>
              <p>当前仅展示部分内容，开通会员即可阅读全文、解锁下载与互动功能。</p>
              <el-button type="primary" size="large" class="preview-gate__btn" @click="goToMembership">
                {{ canReadFull ? '查看完整内容' : '立即开通会员' }}
              </el-button>
            </div>
          </div>
        </transition>
        <div class="content-footer">
          <div class="footer-actions">
            <button
              class="favorite-btn"
              type="button"
              :class="{ favorited: isFavorited }"
              :disabled="favoriteLoading"
              @click="toggleFavorite"
            >
              <el-icon>
                <Collection v-if="!isFavorited" />
                <CollectionTag v-else />
              </el-icon>
              <span>{{ isFavorited ? '已收藏' : '收藏文章' }}</span>
            </button>
            <button class="detail-link" type="button" @click="openDetail">
              查看详情页
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
          <div class="credits">
            <span>© {{ new Date().getFullYear() }} {{ tenantName }}</span>
            <span>知识付费 · 版权所有</span>
          </div>
        </div>
        </template>
      </main>
    </div>
  </div>

  <div v-else-if="loading" class="reader-loading">
    <div class="spinner"></div>
    <p>正在加载内容，请稍候…</p>
  </div>

  <div v-else class="reader-empty">
    <p>未找到文章内容或文章已下线。</p>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>

  <transition name="fade">
    <div
      v-if="imageViewer.visible"
      class="reader-image-viewer"
      v-content-protect="protectContent"
    >
      <el-image-viewer
        :key="imageViewer.renderKey"
        :url-list="imageViewer.urls"
        :initial-index="imageViewer.index"
        :hide-on-click-modal="true"
        :close-on-press-escape="true"
        :teleported="false"
        @close="closeImageViewer"
        @switch="handleViewerSwitch"
      />
    </div>
  </transition>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { tenantApi } from '@/api/tenant'
import { ArrowLeft, ArrowRight, Clock, Collection, CollectionTag, Lock, View, StarFilled, User } from '@element-plus/icons-vue'
import { createMarkdownRenderer } from '@/composables/useMarkdown'
import '@/styles/markdown.css'
import { articleApi } from '@/api/article'
import type { Article } from '@/types'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { shouldProtectContent } from '@/directives/content-protect'
import { buildFileUrl } from '@/utils/file'
import { mergeMemberContent, stripMemberBlocks } from '@/utils/memberContent'
import {
  collectSecureResourceConfig,
  ensureSecureResourceConsistency,
  renderSecureResourceHTML,
} from '@/utils/secureResource'
import { ensureAbsoluteUrl } from '@/utils/url'
import { getImageBorderStyleConfig, type ImageBorderStyle } from '@/components/editor/extensions/image/presets'

interface TocItem {
  id: string
  text: string
  level: number
}

type CleanupFn = () => void

const route = useRoute()
const router = useRouter()
const tenantStore = useTenantStore()
const userStore = useUserStore()

const loading = ref(true)
const article = ref<Article | null>(null)
const memberContentRaw = ref<string | null>(null)
const fullContentHtml = ref('')
const previewContentHtml = ref('')
const tableOfContents = ref<TocItem[]>([])
const activeHeading = ref('')
const contentWrapper = ref<HTMLElement | null>(null)
const passwordInput = ref('')
const passwordError = ref('')
const unlocking = ref(false)
const passwordSupportVisible = ref(false)
const ownerSupportQr = ref('')
const ownerSupportRemark = ref('')
const ownerSupportLoaded = ref(false)
const ownerSupportLoading = ref(false)
const isFavorited = ref(false)
const heroCoverUrl = ref('')
let heroCoverWatchToken = 0

const truthyStrings = new Set(['1', 'true', 'yes', 'y', 'on', '启用', '开启'])
const falsyStrings = new Set(['0', 'false', 'no', 'n', 'off', '关闭', '禁用'])

const FALLBACK_AUTHOR_NAME = '匿名作者'
const FALLBACK_AUTHOR_AVATAR = '访客'

watch(
  () => article.value?.coverImage,
  async (newCover) => {
    const token = ++heroCoverWatchToken
    if (!newCover) {
      heroCoverUrl.value = ''
      return
    }
    try {
      const resolved = await buildFileUrl(newCover)
      if (token === heroCoverWatchToken) {
        heroCoverUrl.value = resolved
      }
    } catch (error) {
      console.warn('????????', error)
      if (token === heroCoverWatchToken) {
        heroCoverUrl.value = newCover
      }
    }
  },
  { immediate: true }
)

const extractAuthorName = (data?: Article | null) => {
  if (!data) {
    return ''
  }
  const raw =
    (data as any)?.authorName ??
    data.author?.nickname ??
    data.author?.username ??
    ''
  if (typeof raw === 'string') {
    return raw.trim()
  }
  return ''
}

const displayAuthorName = computed(() => {
  const resolved = extractAuthorName(article.value)
  return resolved || FALLBACK_AUTHOR_NAME
})

const displayAuthorInitial = computed(() => {
  const resolved = extractAuthorName(article.value)
  const base = resolved || FALLBACK_AUTHOR_AVATAR
  const trimmed = base.trim()
  if (!trimmed) {
    return FALLBACK_AUTHOR_AVATAR.slice(0, 1).toUpperCase()
  }
  return trimmed.slice(0, 1).toUpperCase()
})

const coerceBooleanFlag = (value: unknown): boolean | undefined => {
  if (value === null || value === undefined) {
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
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (!normalized) {
      return undefined
    }
    if (truthyStrings.has(normalized)) {
      return true
    }
    if (falsyStrings.has(normalized)) {
      return false
    }
  }
  return undefined
}

const parseAccessLevelFromRemark = (remark: unknown): number => {
  if (typeof remark !== 'string') {
    return 0
  }
  const trimmed = remark.trim()
  if (!trimmed) {
    return 0
  }
  const normalized = trimmed.toLowerCase()
  if (/svip|super\s*vip/.test(normalized)) {
    return 2
  }
  if (/vip_only/.test(normalized)) {
    return 1
  }
  if (/(vip|member)/.test(normalized)) {
    return 1
  }
  const numericMatch = normalized.match(/(?:access|level|vip)[^\d]*(\d+)/)
  if (numericMatch) {
    const parsed = Number(numericMatch[1])
    if (!Number.isNaN(parsed) && parsed > 0) {
      return parsed
    }
  }
  return 0
}

const isCurrentUserVip = () => {
  const raw = (userStore as any)?.isVipMember
  if (typeof raw === 'boolean') {
    return raw
  }
  if (raw && typeof raw === 'object' && 'value' in raw) {
    return Boolean((raw as { value: unknown }).value)
  }
  return Boolean(raw)
}

const canRequestMemberContent = () => {
  return isCurrentUserVip()
}

const getCurrentUserId = () => {
  const info = (userStore as any)?.userInfo
  if (!info) {
    return null
  }
  if (typeof info.id !== 'undefined' && info.id !== null) {
    return String(info.id)
  }
  if (typeof info.userId !== 'undefined' && info.userId !== null) {
    return String(info.userId)
  }
  return null
}

const isArticleAuthor = (data?: Partial<Article> | null) => {
  if (!data) {
    return false
  }
  const currentId = getCurrentUserId()
  if (!currentId) {
    return false
  }
  const directAuthorId = (data as any)?.authorId
  if (directAuthorId !== undefined && directAuthorId !== null && String(directAuthorId) === currentId) {
    return true
  }
  const nestedAuthorId = (data as any)?.author?.id
  if (nestedAuthorId !== undefined && nestedAuthorId !== null && String(nestedAuthorId) === currentId) {
    return true
  }
  return false
}

const shouldRenderMemberContent = (data?: Partial<Article> | null) => {
  if (canRequestMemberContent()) {
    return true
  }
  return isArticleAuthor(data)
}

const renderMemberAwareContent = (
  rawContent: string | null | undefined,
  memberRaw: string | null | undefined,
  allowMember: boolean,
  requirePlaceholder: boolean
) => {
  const base = rawContent || ''
  if (allowMember) {
    return mergeMemberContent(base, memberRaw)
  }
  const stripped = stripMemberBlocks(base)
  if (
    requirePlaceholder &&
    !stripped.includes('member-content-locked-placeholder')
  ) {
    return `${stripped}<div class="member-content-locked-placeholder"><div class="member-content-locked-message"><span class="member-content-locked-message__tag">会员专属</span><span class="member-content-locked-message__text">会员查看所有内容</span><a class="member-content-locked-message__cta" href="/membership">立即开通会员</a></div></div>`
  }
  return stripped
}
const favoriteLoading = ref(false)

const imageViewer = reactive({
  visible: false,
  index: 0,
  urls: [] as string[],
  renderKey: 0
})

let imagePreviewCleanup: CleanupFn[] = []

const registerImageCleanup = (fn: CleanupFn) => {
  imagePreviewCleanup.push(fn)
}

const teardownImagePreview = () => {
  if (!imagePreviewCleanup.length) return
  imagePreviewCleanup.forEach(fn => {
    try {
      fn()
    } catch (error) {
      console.warn('image preview cleanup failed', error)
    }
  })
  imagePreviewCleanup = []
}

const openImageViewer = (index: number) => {
  if (!imageViewer.urls.length) return
  imageViewer.index = index
  imageViewer.renderKey += 1
  imageViewer.visible = true
}

const closeImageViewer = () => {
  imageViewer.visible = false
}

const handleViewerSwitch = (index: number) => {
  imageViewer.index = index
}

let observer: IntersectionObserver | null = null

const setupImagePreviewInteractions = (container: HTMLElement) => {
  teardownImagePreview()

  const images = Array.from(container.querySelectorAll<HTMLImageElement>('img'))
  if (!images.length) {
    imageViewer.urls = []
    if (imageViewer.visible) {
      closeImageViewer()
    }
    return
  }

  const urls: string[] = []
  const blockDownload = protectContent.value

  images.forEach((img, index) => {
    const source = img.currentSrc || img.src || img.getAttribute('data-src') || ''
    urls.push(source)
    img.classList.add('reader-image-zoomable')
    img.style.cursor = 'zoom-in'

    const clickHandler = (event: Event) => {
      event.preventDefault()
      event.stopPropagation()
      openImageViewer(index)
    }

    img.addEventListener('click', clickHandler)
    registerImageCleanup(() => {
      img.removeEventListener('click', clickHandler)
      img.classList.remove('reader-image-zoomable')
      if (img.style.cursor === 'zoom-in') {
        img.style.cursor = ''
      }
    })

    if (blockDownload) {
      const guardHandler = (event: Event) => {
        event.preventDefault()
        event.stopPropagation()
      }
      img.setAttribute('draggable', 'false')
      img.dataset.noDownload = 'true'
      img.addEventListener('contextmenu', guardHandler)
      img.addEventListener('dragstart', guardHandler)
      registerImageCleanup(() => {
        img.removeEventListener('contextmenu', guardHandler)
        img.removeEventListener('dragstart', guardHandler)
        img.removeAttribute('data-no-download')
        if (img.getAttribute('draggable') === 'false') {
          img.removeAttribute('draggable')
        }
      })
    } else {
      img.removeAttribute('data-no-download')
      if (img.getAttribute('draggable') === 'false') {
        img.removeAttribute('draggable')
      }
    }
  })

  imageViewer.urls = urls
  if (imageViewer.index >= urls.length) {
    imageViewer.index = Math.max(0, urls.length - 1)
  }
}

const refreshFavoriteStatus = async () => {
  const articleId = route.params.id as string | undefined
  if (!articleId) {
    isFavorited.value = false
    return
  }
  if (!userStore.isLoggedIn) {
    isFavorited.value = false
    return
  }
  try {
    const status = await articleApi.checkFavoriteStatus(articleId)
    isFavorited.value = Boolean(status?.favorited ?? status?.isFavorited)
  } catch (error) {
    console.warn('Failed to check favorite status', error)
    isFavorited.value = false
  }
}

const toggleFavorite = async () => {
  if (favoriteLoading.value) {
    return
  }

  const articleId = route.params.id as string | undefined
  if (!articleId) {
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    favoriteLoading.value = true
    if (isFavorited.value) {
      await articleApi.unfavorite(articleId)
      isFavorited.value = false
      ElMessage.success('取消收藏')
    } else {
      await articleApi.favorite(articleId)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('Failed to toggle favorite', error)
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const PRIMARY_SECURE_RESOURCE_ATTRS = new Set([
  'data-type',
  'data-title',
  'data-resource-type',
  'data-url',
  'data-secret',
  'data-notice',
  'data-config',
  'data-secure-state',
  'data-secure-has-sensitive',
])

const PASSWORD_SUPPORT_QR_KEYS = [
  'passwordSupportQr',
  'passwordSupportQRCode',
  'passwordQrCode',
  'contactQrCode',
  'supportQrCode',
  'ownerQrCode',
  'wechatQrCode',
  'wechatQr',
  'contactWechatQr',
  'contactWeChatQr',
] as const

const rebuildSecureResourceBlock = (node: HTMLElement): HTMLElement | null => {
  const config = collectSecureResourceConfig(node)
  const wrapper = document.createElement('div')
  wrapper.innerHTML = renderSecureResourceHTML(config)
  const next = wrapper.firstElementChild as HTMLElement | null
  if (!next) {
    return null
  }

  if (node.id && !next.id) {
    next.id = node.id
  }

  Array.from(node.classList).forEach((cls) => {
    if (!next.classList.contains(cls)) {
      next.classList.add(cls)
    }
  })

  Array.from(node.attributes).forEach((attr) => {
    const { name, value } = attr
    if (name === 'class' || name === 'id') {
      return
    }
    if (name.startsWith('data-') && PRIMARY_SECURE_RESOURCE_ATTRS.has(name)) {
      return
    }
    if (!next.hasAttribute(name)) {
      next.setAttribute(name, value)
    }
  })

  return next
}

const syncSecureResourceBlockState = (block: HTMLElement) => {
  const hasSensitive = block.dataset.secureHasSensitive === 'true'
  const currentState = block.dataset.secureState || (hasSensitive ? 'concealed' : 'revealed')
  const normalizedState = hasSensitive ? currentState : 'revealed'
  block.dataset.secureState = normalizedState

  const mask = block.querySelector<HTMLElement>('[data-role="mask"]')
  const content = block.querySelector<HTMLElement>('[data-role="content"]')

  if (normalizedState === 'revealed') {
    if (mask && !mask.classList.contains('secure-resource-block__mask--hidden')) {
      mask.classList.add('secure-resource-block__mask--hidden')
      mask.setAttribute('hidden', 'hidden')
    }
    if (content) {
      content.classList.remove('secure-resource-block__content--hidden')
    }
  } else {
    if (mask) {
      mask.classList.remove('secure-resource-block__mask--hidden')
      mask.removeAttribute('hidden')
    }
    if (content && !content.classList.contains('secure-resource-block__content--hidden')) {
      content.classList.add('secure-resource-block__content--hidden')
    }
  }
}

const revealSecureResourceBlock = (block: HTMLElement): boolean => {
  const hasSensitive = block.dataset.secureHasSensitive === 'true'
  if (!hasSensitive) {
    block.dataset.secureState = 'revealed'
    syncSecureResourceBlockState(block)
    return false
  }

  if (block.dataset.secureState === 'revealed') {
    syncSecureResourceBlockState(block)
    return false
  }

  block.dataset.secureState = 'revealed'
  syncSecureResourceBlockState(block)
  return true
}

const normalizeSecureResourceBlocks = (container: HTMLElement) => {
  const nodes = Array.from(
    container.querySelectorAll<HTMLElement>('[data-type="secure-resource"]')
  )

  nodes.forEach((node) => {
    const replacement = rebuildSecureResourceBlock(node)
    if (replacement && replacement !== node) {
      node.replaceWith(replacement)
      syncSecureResourceBlockState(replacement)
    } else if (replacement) {
      syncSecureResourceBlockState(replacement)
    }
  })
}

const tenantName = computed(() => tenantStore.tenantInfo?.name || 'Simple Share')

const tenantContactName = computed(() => {
  const contact = tenantStore.currentTenant?.contactUserName || tenantStore.tenantInfo?.name
  if (typeof contact === 'string') {
    const trimmed = contact.trim()
    if (trimmed) {
      return trimmed
    }
  }
  return tenantName.value
})

const tenantContactPhone = computed(() => {
  const contactPhone = tenantStore.currentTenant?.contactPhone
  if (typeof contactPhone === 'string') {
    const trimmed = contactPhone.trim()
    if (trimmed) {
      return trimmed
    }
  }
  const config = tenantStore.tenantConfig as Record<string, unknown> | null
  if (config && typeof config === 'object') {
    const normalized = config as Record<string, unknown>
    const value = normalized['contactPhone']
    if (typeof value === 'string') {
      const trimmed = value.trim()
      if (trimmed) {
        return trimmed
      }
    }
  }
  return ''
})

const passwordSupportQr = computed(() => {
  if (ownerSupportQr.value) {
    return ownerSupportQr.value
  }
  const config = tenantStore.tenantConfig as Record<string, unknown> | null
  if (config && typeof config === 'object') {
    const normalized = config as Record<string, unknown>
    const candidate =
      normalized['ownerQrCode'] ??
      normalized['owner_qr_code'] ??
      (normalized['owner'] as Record<string, unknown> | undefined)?.qrCode ??
      (normalized['owner'] as Record<string, unknown> | undefined)?.qr_code
    if (typeof candidate === 'string' && candidate.trim()) {
      return ensureAbsoluteUrl(candidate)
    }
    for (const key of PASSWORD_SUPPORT_QR_KEYS) {
      const value = normalized[key]
      if (typeof value === 'string' && value.trim()) {
        return ensureAbsoluteUrl(value)
      }
    }
  }
  const tenant = tenantStore.currentTenant as unknown as Record<string, unknown> | null
  if (tenant && typeof tenant === 'object') {
    const direct = tenant['contactQrCode'] ?? tenant['wechatQrCode']
    if (typeof direct === 'string' && direct.trim()) {
      return ensureAbsoluteUrl(direct)
    }
  }
  return ''
})

const passwordSupportRemark = computed(() => {
  if (ownerSupportRemark.value) {
    return ownerSupportRemark.value
  }
  const config = tenantStore.tenantConfig as Record<string, unknown> | null
  if (config && typeof config === 'object') {
    const normalized = config as Record<string, unknown>
    const candidate =
      normalized['ownerRemark'] ??
      normalized['owner_remark'] ??
      (normalized['owner'] as Record<string, unknown> | undefined)?.remark
    if (typeof candidate === 'string' && candidate.trim()) {
      return candidate.trim()
    }
  }
  const tenant = tenantStore.currentTenant as unknown as Record<string, unknown> | null
  if (tenant && typeof tenant === 'object') {
    const direct = tenant['contactRemark']
    if (typeof direct === 'string' && direct.trim()) {
      return direct.trim()
    }
  }
  return '请联系站长获取访问密码'
})

const loadOwnerSupportInfo = async () => {
  if (ownerSupportLoaded.value || ownerSupportLoading.value) {
    return
  }
  try {
    ownerSupportLoading.value = true
    const response = await tenantApi.getPublicOwner()
    const data = (response as any)?.data ?? response
    if (typeof data?.qrCode === 'string' && data.qrCode.trim()) {
      ownerSupportQr.value = ensureAbsoluteUrl(data.qrCode)
    }
    if (typeof data?.remark === 'string' && data.remark.trim()) {
      ownerSupportRemark.value = data.remark.trim()
    }
    ownerSupportLoaded.value = true
  } catch (error) {
    console.error('获取站长二维码失败:', error)
  } finally {
    ownerSupportLoading.value = false
  }
}

const normalizedUserType = computed(() => {
  const raw = userStore.userInfo?.userType
  if (raw === undefined || raw === null) {
    return ''
  }
  return raw.toString().trim().toLowerCase()
})
const isVipUser = computed(
  () => normalizedUserType.value === '3' || normalizedUserType.value === 'vip' || normalizedUserType.value === 'member'
)
const isAdminUser = computed(() => {
  if (userStore.isAdmin) {
    return true
  }
  return normalizedUserType.value === 'admin' || normalizedUserType.value === 'super_admin'
})
const hasMembershipAccess = computed(() => isVipUser.value || isAdminUser.value)
const backendHasMemberContent = computed(() => Boolean((article.value as any)?.hasMemberContent))
const backendMemberContentLocked = computed(() => Boolean((article.value as any)?.memberContentLocked))

const articleAccessLevel = computed(() => {
  const raw =
    article.value?.accessLevel ??
    (article.value as any)?.access_level ??
    0
  let derived = 0

  const numeric = Number(raw)
  if (!Number.isNaN(numeric) && numeric > 0) {
    derived = numeric
  } else if (typeof raw === 'string') {
    const normalized = raw.trim().toLowerCase()
    if (normalized === 'vip' || normalized === 'member' || normalized === 'vip_only') {
      derived = 1
    } else {
      const parsed = Number(normalized)
      if (!Number.isNaN(parsed) && parsed > 0) {
        derived = parsed
      }
    }
  }

  if (derived <= 0 && backendMemberContentLocked.value) {
    derived = 1
  }

  if (derived <= 0 && memberContentRaw.value && memberContentRaw.value.trim() !== '') {
    derived = 1
  }

  if (derived <= 0) {
    const remarkLevel = parseAccessLevelFromRemark((article.value as any)?.remark)
    if (remarkLevel > 0) {
      derived = remarkLevel
    }
  }

  if (derived <= 0 && backendHasMemberContent.value) {
    derived = 1
  }

  if (derived <= 0) {
    const tieredRaw = (article.value as any)?.enableTieredRead ?? (article.value as any)?.tieredReading
    const tieredFlag = coerceBooleanFlag(tieredRaw)
    if (tieredFlag === true) {
      derived = 1
    }
  }

  if (derived <= 0) {
    const vipOnlyFlag = coerceBooleanFlag((article.value as any)?.vipOnly ?? (article.value as any)?.vip_only)
    if (vipOnlyFlag === true) {
      derived = 1
    }
  }

  return derived > 0 ? derived : 0
})

const basePreviewOnly = computed(() => article.value?.previewOnly === true)
const membershipRequired = computed(() => {
  if (!article.value) {
    return false
  }
  if (backendMemberContentLocked.value) {
    return true
  }
  if (articleAccessLevel.value > 0 && backendHasMemberContent.value) {
    return true
  }
  return basePreviewOnly.value && backendHasMemberContent.value
})
const membershipBadgeText = computed(() => {
  if (!membershipRequired.value) {
    return ''
  }
  switch (articleAccessLevel.value) {
    case 1:
      return '会员专享'
    case 2:
      return 'VIP专享'
    default:
      return '会员内容'
  }
})

const canReadFull = computed(() => {
  if (article.value?.fullReadable === true) {
    return true
  }
  if (membershipRequired.value && hasMembershipAccess.value) {
    return true
  }
  return false
})

const hasPreviewContent = computed(() => {
  if (previewContentHtml.value.trim().length > 0) {
    return true
  }
  return backendMemberContentLocked.value
})

const previewOnly = computed(() => {
  if (canReadFull.value) {
    return false
  }
  if (backendMemberContentLocked.value) {
    return true
  }
  if (basePreviewOnly.value) {
    return true
  }
  return false
})

const requiresPassword = computed(() => {
  // 如果API明确返回需要密码验证，则显示密码输入界面
  // 即使是管理员，也需要输入正确的文章密码
  return article.value?.passwordProtected === true && article.value?.passwordVerified !== true
})

const shouldShowPreview = computed(() => {
  if (canReadFull.value) {
    return false
  }
  return previewOnly.value && hasPreviewContent.value
})

const visibleContent = computed(() => (shouldShowPreview.value ? previewContentHtml.value : fullContentHtml.value))
const protectContent = computed(() => shouldProtectContent(article.value?.allowCopy))
const passwordHint = computed(() => {
  const hint = (article.value as any)?.accessDeniedReason
  if (typeof hint === 'string' && hint.trim()) {
    return hint.trim()
  }
  return '请输入访问密码以解锁全文内容。'
})

const readableDate = computed(() => {
  if (!article.value?.createdAt && !article.value?.publishedAt) {
    return '日期未知'
  }
  const source = article.value.createdAt || article.value.publishedAt
  return formatDate(source!)
})

const formatDate = (time: string) => {
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return '日期未知'
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatNumber = (value?: number | string | null) => {
  const num = Number(value ?? 0)
  if (Number.isNaN(num)) return '0'
  if (num > 10000) {
    return `${(num / 10000).toFixed(1)} 万`
  }
  return num.toLocaleString('zh-CN')
}

const md = createMarkdownRenderer()

const ABSOLUTE_URL_PATTERN = /^(?:https?:)?\/\//i
const SPECIAL_SCHEME_PATTERN = /^(?:data:|blob:|javascript:|mailto:|tel:)/i

const alignmentStyleMap: Record<string, Record<string, string>> = {
  left: {
    display: 'inline-flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    justifyContent: 'center',
    verticalAlign: 'top',
    margin: '4px 6px'
  },
  center: {
    display: 'inline-flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    verticalAlign: 'top',
    margin: '12px auto'
  },
  right: {
    display: 'inline-flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    justifyContent: 'center',
    verticalAlign: 'top',
    margin: '4px 6px'
  }
}

const normalizeUploadPath = (value: string) => {
  let path = value.trim()
  if (!path) {
    return path
  }
  const stripLeadingDot = path.startsWith('./') ? path.slice(1) : path
  path = stripLeadingDot

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

const normalizeContentMediaUrls = (html: string) => {
  if (!html) {
    return ''
  }
  if (typeof window === 'undefined' || typeof DOMParser === 'undefined') {
    return html
  }
  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')

    const applyAlignment = (img: HTMLImageElement, alignment?: string | null) => {
      const key = (alignment ?? '').toLowerCase()
      const styles = alignmentStyleMap[key]
      if (!styles) {
        return
      }
      Object.entries(styles).forEach(([prop, value]) => {
        img.style.setProperty(prop, value)
      })
    }

    const ensureWidth = (img: HTMLImageElement, rawValue?: string | null) => {
      if (!rawValue) {
        img.style.removeProperty('width')
        return ''
      }
      const value = rawValue.trim()
      if (!value) {
        img.style.removeProperty('width')
        return ''
      }
      const hasUnit = /%|px|vw|vh|em|rem$/.test(value)
      const normalizedValue = hasUnit ? value : `${value}%`
      img.style.setProperty('width', normalizedValue)
      return normalizedValue
    }

    const ensureHeight = (img: HTMLImageElement, rawValue?: string | null) => {
      if (!rawValue) {
        return
      }
      const value = rawValue.trim()
      if (!value) {
        return
      }
      if (/^auto$/i.test(value)) {
        return
      }
      const hasUnit = /%|px|vw|vh|em|rem$/.test(value)
      const normalizedValue = hasUnit ? value : `${value}px`
      img.style.setProperty('height', normalizedValue)
    }

    const ensureBorderWrapper = (img: HTMLImageElement, style?: string | null) => {
      if (!style) {
        return
      }
      const normalized = style as ImageBorderStyle
      if (normalized === 'none') {
        return
      }
      const config = getImageBorderStyleConfig(normalized)
      if (img.parentElement && img.parentElement.hasAttribute('data-reader-image-border')) {
        Object.entries(config.image).forEach(([prop, value]) => {
          if (value) {
            img.style.setProperty(prop, value)
          }
        })
        return
      }
      const wrapper = doc.createElement('span')
      wrapper.setAttribute('data-reader-image-border', normalized)
      Object.entries(config.wrapper).forEach(([prop, value]) => {
        wrapper.style.setProperty(prop, value)
      })
      Object.entries(config.image).forEach(([prop, value]) => {
        if (value) {
          img.style.setProperty(prop, value)
        }
      })
      wrapper.style.display = 'inline-flex'
      wrapper.style.verticalAlign = 'top'
      wrapper.style.margin = img.style.margin || '4px 6px'

      const parent = img.parentElement
      if (parent) {
        parent.insertBefore(wrapper, img)
      } else {
        doc.body?.appendChild(wrapper)
      }
      wrapper.appendChild(img)
    }

    const updateImage = (img: HTMLImageElement) => {
      const src = img.getAttribute('src')
      const normalized = normalizeMediaSrc(src)
      if (normalized && normalized !== src) {
        img.setAttribute('src', normalized)
      }

      const widthAttr = img.getAttribute('data-image-width') ?? img.getAttribute('width')
      const widthValue = ensureWidth(img, widthAttr)

      const heightAttr = img.getAttribute('data-image-height') ?? img.getAttribute('height')
      ensureHeight(img, heightAttr)

      const alignmentAttr = img.getAttribute('data-image-align')
      applyAlignment(img, alignmentAttr)
      // 保持图片本身的宽度控制，避免影响 markdown 容器的整体宽度

      const borderAttr = img.getAttribute('data-image-border')
      ensureBorderWrapper(img, borderAttr as ImageBorderStyle | null)
    }

    doc.body?.querySelectorAll('img').forEach(updateImage)

    return doc.body?.innerHTML ?? html
  } catch (error) {
    console.warn('normalizeContentMediaUrls failed:', error)
    return html
  }
}

const toNormalizedHtml = (raw?: string | null) => {
  if (!raw || typeof raw !== 'string') {
    return ''
  }
  const trimmed = raw.trim()
  if (!trimmed) {
    return ''
  }
  const html = md.parse(trimmed)
  const secured = ensureSecureResourceConsistency(html)
  return normalizeContentMediaUrls(secured)
}

const fetchArticle = async () => {
  memberContentRaw.value = null
  closeImageViewer()
  try {
    loading.value = true
    const id = route.params.id as string
    const includeMember = canRequestMemberContent()
    let data = await articleApi.getArticle(id, undefined, includeMember)
    if (!includeMember && isArticleAuthor(data)) {
      data = await articleApi.getArticle(id, undefined, true)
    }
    article.value = data
    await refreshFavoriteStatus()
    if (!data?.passwordProtected || data?.passwordVerified) {
      passwordInput.value = ''
      passwordError.value = ''
    }
    const memberPayload = (data as any)?.memberContent ?? (data as any)?.member_content ?? null
    memberContentRaw.value = memberPayload
    const allowMemberContent = shouldRenderMemberContent(data)
    const requirePlaceholder = backendMemberContentLocked.value && !allowMemberContent
    fullContentHtml.value = toNormalizedHtml(
      renderMemberAwareContent(data?.content, memberPayload, allowMemberContent, requirePlaceholder)
    )
    previewContentHtml.value = toNormalizedHtml(
      renderMemberAwareContent(
        data?.previewContent ?? '',
        memberPayload,
        allowMemberContent,
        requirePlaceholder
      )
    )
    await articleApi.view(id)
  } catch (error: any) {
    memberContentRaw.value = null
    const status = error?.response?.status
    if (status === 403) {
      const message = error?.response?.data?.msg || '该文章为会员专享，请登录并开通会员后访问'
      ElMessage.closeAll()
      ElMessage.warning(message)
      article.value = null
      fullContentHtml.value = ''
      previewContentHtml.value = ''
      memberContentRaw.value = null
      if (!userStore.isLoggedIn) {
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } })
      } else {
        router.push('/profile')
      }
    } else {
      console.error('Failed to load article', error)
      ElMessage.error('加载文章失败，请稍后再试')
    }
  } finally {
    loading.value = false
    if (article.value) {
      await nextTick()
      await enhanceRenderedContent()
      generateTableOfContents()
    }
  }
}

const generateTableOfContents = () => {
  tableOfContents.value = []
  observer?.disconnect()
  observer = null

  nextTick(() => {
    const container = contentWrapper.value
    if (!container) return

    const headings = Array.from(
      container.querySelectorAll<HTMLElement>('h1, h2, h3, h4')
    )

    const toc: TocItem[] = []
    headings.forEach((heading, index) => {
      const id = `reader-heading-${index}`
      heading.id = id
      toc.push({
        id,
        text: heading.textContent?.trim() || `无标题 ${index + 1}`,
        level: Number(heading.tagName.charAt(1)) || 2
      })
    })

    tableOfContents.value = toc
    setupObserver(headings)
  })
}

const setupObserver = (headings: HTMLElement[]) => {
  if (!headings.length) return

  observer = new IntersectionObserver(
    entries => {
      const visibleEntry = entries
        .filter(entry => entry.isIntersecting)
        .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0]

      if (visibleEntry?.target?.id) {
        activeHeading.value = visibleEntry.target.id
      }
    },
    {
      root: null,
      rootMargin: '-30% 0px -50% 0px',
      threshold: [0.1, 0.5, 1]
    }
  )

  headings.forEach(heading => observer?.observe(heading))
}

const scrollToHeading = (id: string) => {
  const el = document.getElementById(id)
  if (!el) return
  window.scrollTo({ top: el.getBoundingClientRect().top + window.scrollY - 80, behavior: 'smooth' })
}

const navigateBack = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    router.push('/')
  }
}

const openDetail = () => {
  if (!article.value) return
  const url = router.resolve({ name: 'ArticleDetail', params: { id: article.value.id } }).href
  window.open(url, '_blank', 'noopener')
}

const goToMembership = () => {
  const redirectTarget = route.fullPath || '/article'
  router.push({
    name: 'Membership',
    query: { redirect: redirectTarget }
  })
}

const unlockArticle = async () => {
  if (unlocking.value) {
    return
  }

  const password = passwordInput.value.trim()
  if (!password) {
    passwordError.value = '请输入访问密码'
    ElMessage.warning('请输入访问密码')
    return
  }

  try {
    unlocking.value = true
    passwordError.value = ''
    const id = route.params.id as string
    const includeMember = canRequestMemberContent()
    let data = await articleApi.getArticle(id, { password }, includeMember)
    if (!includeMember && isArticleAuthor(data)) {
      data = await articleApi.getArticle(id, { password }, true)
    }
    article.value = data
    await refreshFavoriteStatus()
    const memberPayload = (data as any)?.memberContent ?? (data as any)?.member_content ?? null
    memberContentRaw.value = memberPayload
    const allowMemberContent = shouldRenderMemberContent(data)
    const requirePlaceholder = backendMemberContentLocked.value && !allowMemberContent
    fullContentHtml.value = toNormalizedHtml(
      renderMemberAwareContent(data?.content, memberPayload, allowMemberContent, requirePlaceholder)
    )
    previewContentHtml.value = toNormalizedHtml(
      renderMemberAwareContent(
        data?.previewContent ?? '',
        memberPayload,
        allowMemberContent,
        requirePlaceholder
      )
    )
    if (data?.passwordVerified) {
      passwordInput.value = ''
      passwordError.value = ''
      ElMessage.success('文章已解锁')
      passwordSupportVisible.value = false
      await articleApi.view(id)
    } else {
      passwordError.value = '密码错误，请重试'
      ElMessage.warning('密码错误，请重试')
    }
  } catch (error) {
    console.error('Failed to unlock article', error)
    passwordError.value = '解锁失败，请稍后再试'
    ElMessage.error('解锁失败，请稍后再试')
    memberContentRaw.value = null
  } finally {
    unlocking.value = false
  }
}

watch(
  () => route.params.id,
  async () => {
    passwordInput.value = ''
    passwordError.value = ''
    unlocking.value = false
    passwordSupportVisible.value = false
    isFavorited.value = false
    favoriteLoading.value = false
    memberContentRaw.value = null
    await fetchArticle()
  }
)

watch(visibleContent, async () => {
  if (!loading.value) {
    await nextTick()
    await enhanceRenderedContent()
    generateTableOfContents()
  }
})

watch(protectContent, async () => {
  if (!loading.value) {
    await nextTick()
    await enhanceRenderedContent()
  }
})

watch(passwordInput, () => {
  if (passwordError.value) {
    passwordError.value = ''
  }
})

watch(requiresPassword, (value) => {
  if (!value) {
    passwordSupportVisible.value = false
  }
})

watch(
  () => userStore.isLoggedIn,
  async (loggedIn) => {
    if (loggedIn) {
      await refreshFavoriteStatus()
    } else {
      isFavorited.value = false
    }
  }
)

const openPasswordSupport = async () => {
  if (!requiresPassword.value) {
    return
  }
  if (!passwordSupportQr.value || !passwordSupportRemark.value) {
    await loadOwnerSupportInfo()
  } else {
    ownerSupportLoaded.value = true
  }
  passwordSupportVisible.value = true
}

const copyPlainText = async (value: string) => {
  if (!value) {
    throw new Error('empty')
  }
  if (navigator?.clipboard?.writeText) {
    await navigator.clipboard.writeText(value)
    return
  }

  const textarea = document.createElement('textarea')
  textarea.value = value
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  textarea.style.pointerEvents = 'none'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  const ok = document.execCommand?.('copy')
  document.body.removeChild(textarea)
  if (!ok) {
    throw new Error('copy failed')
  }
}

let secureResourceClickHandler: ((event: Event) => void) | null = null

const bindSecureResourceBlocks = (container: HTMLElement) => {
  if (secureResourceClickHandler) {
    container.removeEventListener('click', secureResourceClickHandler)
  }

  secureResourceClickHandler = async (event: Event) => {
    const target = event.target as HTMLElement | null
    if (!target) return

    const revealTrigger =
      target.closest<HTMLElement>('.secure-resource-block__reveal') ??
      target.closest<HTMLElement>('.secure-resource-block__mask')

    if (revealTrigger) {
      const block = revealTrigger.closest<HTMLElement>('[data-type="secure-resource"]')
      if (!block) {
        return
      }
      const changed = revealSecureResourceBlock(block)
      if (changed) {
        ElMessage.success('敏感信息已解锁')
      }
      event.preventDefault()
      event.stopPropagation()
      return
    }

    const button = target.closest<HTMLButtonElement>('.secure-resource-block__copy')
    if (!button) return

    const block = button.closest<HTMLElement>('[data-type="secure-resource"]')
    if (!block) return

    syncSecureResourceBlockState(block)

    if (
      block.dataset.secureHasSensitive === 'true' &&
      block.dataset.secureState !== 'revealed'
    ) {
      ElMessage.warning('请先点击"查看敏感信息"解锁卡密')
      event.preventDefault()
      event.stopPropagation()
      return
    }

    const copyKey = button.dataset.copy as 'url' | 'secret' | undefined
    if (!copyKey) return

    const attrName = copyKey === 'url' ? 'data-url' : 'data-secret'
    const rawValue = block.dataset[copyKey] ?? block.getAttribute(attrName) ?? ''
    if (!rawValue || rawValue.trim().length === 0) {
      const tip = copyKey === 'url'
        ? '暂无可复制的链接'
        : '暂无可复制的密码'
      ElMessage.warning(tip)
      event.preventDefault()
      event.stopPropagation()
      return
    }

    try {
      await copyPlainText(rawValue)
      const successTip = copyKey === 'url'
        ? '链接已复制'
        : '密码已复制'
      ElMessage.success(successTip)
    } catch (error) {
      console.warn('secure resource copy failed', error)
      ElMessage.error('复制失败，请手动复制')
    } finally {
      event.preventDefault()
      event.stopPropagation()
    }
  }

  container.addEventListener('click', secureResourceClickHandler)
}

onMounted(async () => {
  await Promise.all([
    fetchArticle(),
    tenantStore.fetchTenantConfig().catch(() => {})
  ])
})

onUnmounted(() => {
  if (secureResourceClickHandler && contentWrapper.value) {
    contentWrapper.value.removeEventListener('click', secureResourceClickHandler)
  }
  observer?.disconnect()
  observer = null
  teardownImagePreview()
})
const enhanceRenderedContent = async () => {
  const container = contentWrapper.value
  if (!container) return

  normalizeSecureResourceBlocks(container)
  await md.enhance(container, { protectCopy: protectContent.value })
  normalizeSecureResourceBlocks(container)

  bindSecureResourceBlocks(container)

  const onSuccess = () => ElMessage.success('代码已复制到剪贴板')
  const onFailed = () => ElMessage.error('复制失败，请手动复制')
  const onBlocked = () => ElMessage.warning('当前内容已开启防复制')

  container.addEventListener('md-copy-success', onSuccess, { once: true })
  container.addEventListener('md-copy-failed', onFailed, { once: true })
  container.addEventListener('md-copy-blocked', onBlocked, { once: true })

  setupImagePreviewInteractions(container)
}

</script>

<style scoped>
.reader-layout {
  min-height: 100vh;
  background: #f8fafc;
  color: #0f172a;
  display: flex;
  flex-direction: column;
}

.reader-hero {
  position: relative;
  padding: 72px 0 56px;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: blur(24px);
  transform: scale(1.15);
  opacity: 0.55;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.75));
  backdrop-filter: blur(12px);
}

.hero-content {
  position: relative;
  max-width: 880px;
  margin: 0 auto;
  padding: 0 24px;
  color: #334155;
}

.hero-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #0f172a;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-button:hover {
  background: #f1f5f9;
  border-color: #94a3b8;
}

.meta-divider {
  width: 1px;
  height: 14px;
  background: rgba(148, 163, 184, 0.4);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.4);
  color: #0f172a;
  font-weight: 500;
}

.meta-item span {
  white-space: nowrap;
}

.hero-badge-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.hero-vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: linear-gradient(135deg, #fbbf24, #f97316);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.6px;
  box-shadow: 0 10px 25px rgba(249, 115, 22, 0.25);
  text-transform: uppercase;
}

.hero-vip-badge :deep(svg) {
  width: 1.1em;
  height: 1.1em;
}

.hero-title {
  font-size: clamp(34px, 3.6vw, 48px);
  line-height: 1.1;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 20px;
}

.hero-summary {
  max-width: 680px;
  font-size: 17px;
  line-height: 1.65;
  color: #475569;
  margin-bottom: 24px;
}

.hero-author {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #0f172a;
}

.author-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.avatar {
  display: inline-flex;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  font-weight: 600;
  font-size: 18px;
}

.author-name {
  font-weight: 600;
  letter-spacing: 0.3px;
  color: #0f172a;
}

.author-sub {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.reader-body {
  display: grid;
  grid-template-columns: minmax(200px, 260px) minmax(0, 1fr);
  gap: clamp(20px, 4vw, 36px);
  max-width: 1280px;
  margin: -28px auto 56px;
  padding: 0 32px;
  position: relative;
  align-items: start;
  width: 100%;
}

.reader-body--full {
  grid-template-columns: minmax(0, 1fr);
}

.reader-body--full .reader-content {
  margin: 0 auto;
  max-width: 880px;
  width: 100%;
}

.reader-image-zoomable {
  cursor: zoom-in;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.reader-image-zoomable:hover {
  transform: translateY(-2px) scale(1.01);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.25);
}

.reader-image-viewer :deep(.el-image-viewer__wrapper) {
  z-index: 4000;
}

.reader-image-viewer :deep(.el-image-viewer__close),
.reader-image-viewer :deep(.el-image-viewer__prev),
.reader-image-viewer :deep(.el-image-viewer__next) {
  background-color: rgba(15, 23, 42, 0.85);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.reader-toc {
  position: sticky;
  top: 96px;
  align-self: start;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  padding: 20px 18px;
  box-shadow: 0 16px 30px -24px rgba(15, 23, 42, 0.35);
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}

.toc-header {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 16px;
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toc-item {
  text-align: left;
  background: transparent;
  border: none;
  font-size: 14px;
  color: #475569;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toc-item:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.toc-item.active {
  background: #e2e8f0;
  color: #1e293b;
  font-weight: 600;
}

.toc-item.level-1 {
  padding-left: 12px;
}

.toc-item.level-2 {
  padding-left: 24px;
}

.toc-item.level-3 {
  padding-left: 36px;
  font-size: 13px;
}

.reader-content {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 18px 36px -28px rgba(15, 23, 42, 0.28);
  padding: clamp(20px, 3vw, 32px);
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: clamp(24px, 3vw, 32px);
  min-width: 0;
  justify-self: stretch;
  align-items: center;
}

.reader-password-gate {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 36px;
  margin-bottom: 18px;
  border-radius: 20px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  background: linear-gradient(135deg, rgba(191, 219, 254, 0.45), rgba(239, 246, 255, 0.9));
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.08);
  color: #0f172a;
}

.reader-password-gate__icon {
  font-size: 34px;
  color: #1d4ed8;
}

.reader-password-gate__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.reader-password-gate__message {
  margin: 0;
  font-size: 15px;
  color: #475569;
  line-height: 1.6;
}

.reader-password-gate__form {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.reader-password-gate__input {
  flex: 1 1 260px;
}

.reader-password-gate__actions {
  display: flex;
  flex: 0 0 auto;
  gap: 12px;
  align-items: center;
}

.reader-password-gate__submit {
  flex: 0 0 auto;
  padding: 0 26px;
  border-radius: 10px;
}

.reader-password-gate__support {
  flex: 0 0 auto;
  padding: 0 24px;
  border-radius: 10px;
}

.reader-password-gate__error {
  margin: 0;
  font-size: 13px;
  color: #dc2626;
}

@media (max-width: 768px) {
  .reader-password-gate {
    padding: 28px;
  }

  .reader-password-gate__form {
    flex-direction: column;
    align-items: stretch;
  }

  .reader-password-gate__actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .reader-password-gate__submit {
    width: 100%;
  }

  .reader-password-gate__support {
    width: 100%;
  }
}

.password-support-dialog :deep(.el-dialog__body) {
  padding: 24px 28px 32px;
}

.password-support-dialog__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  text-align: center;
}

.password-support-dialog__intro {
  margin: 0;
  font-size: 15px;
  color: #475569;
  line-height: 1.6;
}

.password-support-dialog__qr {
  width: 220px;
  height: 220px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  box-shadow: 0 18px 32px -26px rgba(15, 23, 42, 0.3);
}

.password-support-dialog__qr img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 12px;
}

.password-support-dialog__placeholder {
  width: 220px;
  min-height: 220px;
  border-radius: 16px;
  border: 1px dashed #cbd5f5;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #64748b;
  line-height: 1.6;
}

.password-support-dialog__placeholder--loading {
  border-style: solid;
  border-color: #60a5fa;
  color: #3b82f6;
}

.password-support-dialog__contact {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: #334155;
  align-items: center;
  text-align: center;
}

.password-support-dialog__contact p {
  margin: 0;
}

.reader-protect-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  margin-bottom: 4px;
  border: 1px solid rgba(59, 130, 246, 0.25);
  background: rgba(59, 130, 246, 0.08);
  border-radius: 14px;
  color: #1d4ed8;
  font-size: 14px;
}

.reader-protect-banner__icon {
  font-size: 20px;
}

.reader-protect-banner__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reader-protect-banner__text strong {
  font-weight: 600;
}

.reader-content .markdown-surface {
  width: 100%;
  max-width: 900px;
  min-width: 0;
  margin: 0 auto;
}

.reader-content .markdown-surface :deep(.md-table-wrapper) {
  width: 100%;
  overflow-x: auto;
}

.reader-content .markdown-surface :deep(table) {
  width: 100%;
  margin: 16px 0;
}

.markdown-surface {
  font-size: 16.5px;
  line-height: 1.75;
  color: #0f172a;
  letter-spacing: 0.01em;
  word-break: break-word;
}

.markdown-surface :deep(p) {
  margin: 0.75em 0;
}

.markdown-surface :deep(p:empty) {
  display: none;
  margin: 0;
}

@supports selector(:has(*)) {
  .markdown-surface :deep(p:has(> br:only-child)) {
    display: none;
  }
}

.markdown-surface :deep(a) {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px dashed rgba(37, 99, 235, 0.35);
}

.markdown-surface :deep(a:hover) {
  border-bottom-color: rgba(37, 99, 235, 0.6);
}

.markdown-surface :deep(h1),
.markdown-surface :deep(h2),
.markdown-surface :deep(h3),
.markdown-surface :deep(h4) {
  color: #0f172a;
  font-weight: 700;
  margin: 1.5em 0 0.65em;
  scroll-margin-top: 120px;
}

.markdown-surface :deep(h1) {
  font-size: 2.2rem;
}

.markdown-surface :deep(h2) {
  font-size: 1.7rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 0.35em;
}

.markdown-surface :deep(h3) {
  font-size: 1.4rem;
}

.markdown-surface :deep(code) {
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  background: rgba(15, 23, 42, 0.85);
  color: #e2e8f0;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 0.9em;
}

.markdown-surface :deep(pre) {
  background: linear-gradient(135deg, rgba(8, 12, 24, 0.98), rgba(17, 24, 39, 0.95));
  color: #e2e8f0;
  border: 1px solid rgba(30, 41, 59, 0.7);
  border-radius: 18px;
  overflow: auto;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  font-size: 0.94em;
  margin: 1.6em 0;
  box-shadow: 0 18px 35px rgba(2, 6, 23, 0.55);
}

.markdown-surface :deep(.md-code-block) {
  position: relative;
  margin: 1.5em 0;
  border: 1px solid #1f2937;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(145deg, rgba(9, 12, 20, 0.98), rgba(15, 23, 42, 0.93));
  box-shadow: 0 20px 35px rgba(2, 6, 23, 0.55);
  color: #e2e8f0;
}

.markdown-surface :deep(.md-code-pre) {
  margin: 0;
  border: none;
  border-radius: 0;
  background: transparent;
  font-family: 'Fira Code', 'JetBrains Mono', Consolas, monospace;
  font-size: 0.94em;
  overflow: auto;
  border-left: 3px solid #38bdf8;
}

.markdown-surface :deep(.md-code-pre > code) {
  display: block;
  padding: 16px 20px 18px;
  white-space: pre;
  color: #f8fafc;
}

.markdown-surface :deep(.md-code-toolbar) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  background: linear-gradient(120deg, rgba(15, 23, 42, 0.85), rgba(30, 41, 59, 0.85));
}

.markdown-surface :deep(.md-code-lang) {
  font-size: 12px;
  color: #94a3b8;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}

.markdown-surface :deep(.md-code-copy) {
  appearance: none;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(15, 23, 42, 0.6);
  color: #e2e8f0;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.markdown-surface :deep(.md-code-copy:hover) {
  background: rgba(56, 189, 248, 0.12);
  border-color: rgba(56, 189, 248, 0.5);
}

.markdown-surface :deep(pre > code) {
  display: block;
  padding: 12px 16px 16px;
  overflow-x: auto;
  white-space: pre;
}

.markdown-surface :deep(blockquote) {
  border-left: 4px solid #3b82f6;
  background: #eff6ff;
  color: #1e3a8a;
  border-radius: 0 14px 14px 0;
  padding: 14px 18px;
  margin: 1.2em 0;
}

.markdown-surface :deep(hr) {
  border: none;
  border-top: 1px dashed #e2e8f0;
  margin: 2em 0;
}

.markdown-surface :deep(kbd) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.85em;
  padding: 2px 6px;
  border: 1px solid #cbd5e1;
  border-bottom-width: 3px;
  border-radius: 6px;
  background: #f8fafc;
}

.markdown-surface :deep(del) {
  opacity: 0.75;
}

.markdown-surface :deep(ul),
.markdown-surface :deep(ol) {
  margin: 0.75em 0 0.75em 1.6em;
}

.markdown-surface :deep(.md-table-wrapper) {
  width: 100%;
  overflow-x: auto;
  margin: 1.4em 0;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.markdown-surface :deep(.md-table-wrapper > table) {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
  table-layout: auto;
}

.markdown-surface :deep(th),
.markdown-surface :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  text-align: left;
}

.markdown-surface :deep(thead th) {
  background: #f8fafc;
}

.markdown-surface :deep(img) {
  display: block !important;
  width: auto;
  max-width: 100%;
  height: auto;
  border-radius: 18px;
  margin: 1.5em auto !important;
  vertical-align: top;
  box-shadow: 0 14px 40px -30px rgba(15, 23, 42, 0.55);
}

.reader-content :deep([data-reader-image-border]) {
  display: flex !important;
  flex-direction: column;
  align-items: center !important;
  justify-content: center !important;
  margin-left: auto !important;
  margin-right: auto !important;
}

.markdown-surface :deep(img[data-image-align='left']),
.markdown-surface :deep(img[data-image-align='center']),
.markdown-surface :deep(img[data-image-align='right']) {
  margin-left: auto !important;
  margin-right: auto !important;
  text-align: center;
}

.markdown-surface :deep(section[data-type='member-content']) {
  border: 1px solid rgba(249, 115, 22, 0.35);
  background: linear-gradient(135deg, rgba(255, 248, 235, 0.9), rgba(255, 241, 213, 0.9));
  border-radius: 16px;
  padding: 24px;
  margin: 28px 0;
  position: relative;
  overflow: hidden;
}

.markdown-surface :deep(section[data-type='member-content']::before) {
  content: '会员专属内容';
  position: absolute;
  top: 12px;
  right: 18px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #b45309;
  background: rgba(249, 115, 22, 0.12);
  border: 1px solid rgba(249, 115, 22, 0.35);
  padding: 4px 10px;
  border-radius: 999px;
}

.markdown-surface :deep(.member-content-locked-placeholder) {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border: 1px dashed rgba(249, 115, 22, 0.6);
  background: rgba(255, 247, 237, 0.95);
  color: #b45309;
  border-radius: 16px;
  padding: 24px;
  margin: 28px 0;
  text-align: center;
}

.markdown-surface :deep(.member-content-locked-message) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.markdown-surface :deep(.member-content-locked-message__tag) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f97316, #fb923c);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
}

.markdown-surface :deep(.member-content-locked-message__text) {
  font-size: 14px;
  font-weight: 500;
  color: #92400e;
}

.markdown-surface :deep(.member-content-locked-message__cta) {
  display: inline-flex;
  margin-top: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #f97316, #fb923c);
  color: #fff !important;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.markdown-surface :deep(.member-content-locked-message__cta:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(249, 115, 22, 0.25);
}

.markdown-surface :deep(ul.md-task-list),
.markdown-surface :deep(ol.md-task-list) {
  list-style: none;
  padding-left: 0;
}

.markdown-surface :deep(li > input[type='checkbox']) {
  margin-right: 8px;
  transform: translateY(1px);
}

.content-footer {
  margin-top: 48px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.favorite-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #1d4ed8;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-btn:hover {
  background: #1e40af;
  border-color: #1e3a8a;
}

.favorite-btn:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.favorite-btn.favorited {
  background: #0f172a;
  border-color: #0f172a;
}

.preview-gate {
  margin-top: 28px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(79, 70, 229, 0.08));
  border: 1px dashed rgba(79, 70, 229, 0.25);
  border-radius: 18px;
  padding: 24px;
}

.preview-gate__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.preview-gate__content h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1d4ed8;
}

.preview-gate__content p {
  margin: 0;
  font-size: 14px;
  color: #1e293b;
  opacity: 0.8;
}

.preview-gate__btn {
  margin-top: 8px;
  padding: 10px 18px;
  border-radius: 10px;
}

.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  border: 1px solid #3b82f6;
  background: #ffffff;
  color: #1d4ed8;
  cursor: pointer;
  transition: all 0.2s ease;
}

.detail-link:hover {
  background: #eff6ff;
}

.credits {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  text-align: right;
}

.reader-loading,
.reader-empty {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  color: #475569;
}

.spinner {
  width: 54px;
  height: 54px;
  border: 4px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1100px) {
  .reader-body {
    grid-template-columns: 1fr;
    margin: -20px auto 52px;
    padding: 0 18px;
  }

  .reader-toc {
    display: none;
  }

  .reader-content {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .reader-hero {
    padding: 56px 0 44px;
  }

  .hero-content {
    padding: 0 14px;
  }

  .hero-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .meta-divider {
    display: none;
  }

  .reader-body {
    margin: -18px auto 48px;
    padding: 0 14px;
  }

  .reader-content {
    padding: 16px;
    border-radius: 12px;
  }

  .content-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .credits {
    text-align: left;
  }
}
</style>
