import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createMarkdownRenderer } from '@/composables/useMarkdown'
import {
  detectContentFormat,
  htmlToMarkdown,
} from '@/components/editor/utils/markdown'
import {
  collectSecureResourceConfig,
  ensureSecureResourceConsistency,
  renderSecureResourceHTML,
} from '@/utils/secureResource'

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

const PRIMARY_SECURE_RESOURCE_ATTRS = new Set([
  'data-type',
  'data-title',
  'data-resource-type',
  'data-url',
  'data-secret',
  'data-notice',
  'data-config',
])

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

const normalizeSecureResourceBlocks = (container: HTMLElement) => {
  const nodes = Array.from(
    container.querySelectorAll<HTMLElement>('[data-type="secure-resource"]')
  )

  nodes.forEach((node) => {
    const replacement = rebuildSecureResourceBlock(node)
    if (replacement && replacement !== node) {
      node.replaceWith(replacement)
    }
  })
}

const bindSecureResourceBlocks = (container: HTMLElement) => {
  const handler = async (event: Event) => {
    const target = event.target as HTMLElement | null
    if (!target) return

    const button = target.closest<HTMLButtonElement>('.secure-resource-block__copy')
    if (!button) return

    const block = button.closest<HTMLElement>('[data-type="secure-resource"]')
    if (!block) return

    const copyKey = (button.dataset.copy as 'url' | 'secret' | undefined) ?? 'url'
    const attrName = copyKey === 'secret' ? 'data-secret' : 'data-url'
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

  container.addEventListener('click', handler)
  return () => container.removeEventListener('click', handler)
}


type SourceGetter = () => string
type ProtectGetter = () => boolean

interface UseArticlePreviewRendererOptions {
  source: SourceGetter
  protectCopy: ProtectGetter
  autoEnhance?: boolean
}

export function useArticlePreviewRenderer({
  source,
  protectCopy,
  autoEnhance = true,
}: UseArticlePreviewRendererOptions) {
  const renderedHtml = ref('')
  const containerRef = ref<HTMLElement | null>(null)
  const markdownRenderer = createMarkdownRenderer()
  const protectCopyComputed = computed(protectCopy)
  let cleanup: (() => void) | null = null

  const cleanupSecureResource = () => {
    if (cleanup) {
      cleanup()
      cleanup = null
    }
  }

  const enhance = async () => {
    const container = containerRef.value
    if (!container) return

    cleanupSecureResource()
    normalizeSecureResourceBlocks(container)
    await markdownRenderer.enhance(container, { protectCopy: protectCopyComputed.value })
    normalizeSecureResourceBlocks(container)

    cleanup = bindSecureResourceBlocks(container)

    const onSuccess = () =>
      ElMessage.success('代码已复制到剪贴板')
    const onFailed = () =>
      ElMessage.error('复制失败，请手动复制')
    const onBlocked = () =>
      ElMessage.warning('当前内容已开启防复制')

    container.addEventListener('md-copy-success', onSuccess, { once: true })
    container.addEventListener('md-copy-failed', onFailed, { once: true })
    container.addEventListener('md-copy-blocked', onBlocked, { once: true })
  }


  const renderHtml = (raw: string) => {
    if (!raw) {
      return ''
    }
    const format = detectContentFormat(raw)
    const markdown = format === 'markdown' ? raw : htmlToMarkdown(raw)
    const html = markdownRenderer.parse(markdown)
    return ensureSecureResourceConsistency(html)
  }

  const refresh = async () => {
    const raw = source()
    if (!raw || raw.trim().length === 0) {
      renderedHtml.value = ''
      cleanupSecureResource()
      return
    }

    renderedHtml.value = renderHtml(raw)

    if (autoEnhance) {
      await nextTick()
      await enhance()
    }
  }

  watch(source, () => {
    void refresh()
  }, { immediate: true })

  watch(protectCopyComputed, async () => {
    if (!autoEnhance) return
    if (!renderedHtml.value) return
    await nextTick()
    await enhance()
  })

  onBeforeUnmount(() => {
    cleanupSecureResource()
  })

  return {
    renderedHtml,
    containerRef,
    enhance,
    refresh,
    cleanupSecureResource,
    protectCopyComputed,
  }
}
