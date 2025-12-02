import { type Directive } from 'vue'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'

type CleanupFn = () => void

interface ProtectionState {
  cleanupFns: CleanupFn[]
  observer?: MutationObserver
  previousUserSelect?: string
  previousWebkitUserSelect?: string
  previousMozUserSelect?: string
  previousMsUserSelect?: string
  previousPosition?: string
  positionPatched?: boolean
  devtoolsOverlay?: HTMLElement | null
  devtoolsLocked?: boolean
}

const protectedRoots = new Set<HTMLElement>()
const elementStates = new WeakMap<HTMLElement, ProtectionState>()

const BLOCKED_CTRL_KEYS = new Set(['A', 'C', 'S', 'X', 'P', 'U'])
const BLOCKED_CTRL_SHIFT_KEYS = new Set(['I', 'J', 'C', 'K', 'P'])
const BLOCKED_CTRL_ALT_KEYS = new Set(['I', 'J'])
const DEVTOOLS_KEYS = new Set(['F12'])
const DEVTOOLS_CHECK_INTERVAL = 800
const DEVTOOLS_DIMENSION_THRESHOLD = 120
const DEVTOOLS_ATTRIBUTE = 'data-devtools-open'
const DEVTOOLS_OVERLAY_CLASS = 'content-protect-devtools-overlay'
const DEVTOOLS_MESSAGE_CLASS = 'content-protect-devtools-message'

const MEDIA_GUARD_KEY = '__contentProtectGuard__'
const BLOCK_EVENT_NAME = 'content-protect-block'

let globalHandlersAttached = false

const hasDocument = typeof document !== 'undefined'
const hasWindow = typeof window !== 'undefined'

const globalClipboardHandler = (event: ClipboardEvent) => {
  if (!selectionTouchesProtected()) return
  preventAndReset(event)
  event.clipboardData?.clearData()
  clearProtectedSelection()
  emitBlockEvent('copy', event)
}

const globalPasteHandler = (event: ClipboardEvent) => {
  if (!isTargetWithinProtected(event.target)) return
  preventAndReset(event)
  emitBlockEvent('paste', event)
}

const globalContextMenuHandler = (event: MouseEvent) => {
  if (!shouldBlockForTarget(event.target)) return
  preventAndReset(event)
  emitBlockEvent('contextmenu', event)
}

const globalSelectStartHandler = (event: Event) => {
  if (!shouldBlockForTarget(event.target)) return
  preventAndReset(event)
  emitBlockEvent('select', event)
}

const globalDragStartHandler = (event: DragEvent) => {
  if (!shouldBlockForTarget(event.target)) return
  preventAndReset(event)
  emitBlockEvent('drag', event)
}

const globalBeforePrintHandler = (event: Event) => {
  if (protectedRoots.size === 0) return
  preventAndReset(event)
  emitBlockEvent('print', event)
}

const globalKeydownHandler = (event: KeyboardEvent) => {
  if (!shouldBlockKeyboard(event)) return
  preventAndReset(event)
  emitBlockEvent('shortcut', event, {
    key: event.key,
    code: event.code,
    ctrlKey: event.ctrlKey,
    metaKey: event.metaKey,
    shiftKey: event.shiftKey,
    altKey: event.altKey
  })
}

function shouldBlockForTarget(target: EventTarget | null): boolean {
  if (isEditableTarget(target)) return false
  return isTargetWithinProtected(target)
}

function attachGlobalHandlers() {
  if (globalHandlersAttached || !hasDocument) return
  document.addEventListener('copy', globalClipboardHandler, true)
  document.addEventListener('cut', globalClipboardHandler, true)
  document.addEventListener('paste', globalPasteHandler, true)
  document.addEventListener('contextmenu', globalContextMenuHandler, true)
  document.addEventListener('selectstart', globalSelectStartHandler, true)
  document.addEventListener('dragstart', globalDragStartHandler, true)
  document.addEventListener('keydown', globalKeydownHandler, true)
  if (hasWindow) {
    window.addEventListener('beforeprint', globalBeforePrintHandler, true)
  }
  globalHandlersAttached = true
}

function detachGlobalHandlers() {
  if (!globalHandlersAttached || !hasDocument || protectedRoots.size > 0) return
  document.removeEventListener('copy', globalClipboardHandler, true)
  document.removeEventListener('cut', globalClipboardHandler, true)
  document.removeEventListener('paste', globalPasteHandler, true)
  document.removeEventListener('contextmenu', globalContextMenuHandler, true)
  document.removeEventListener('selectstart', globalSelectStartHandler, true)
  document.removeEventListener('dragstart', globalDragStartHandler, true)
  document.removeEventListener('keydown', globalKeydownHandler, true)
  if (hasWindow) {
    window.removeEventListener('beforeprint', globalBeforePrintHandler, true)
  }
  globalHandlersAttached = false
}

function enableProtection(el: HTMLElement) {
  disableProtection(el)

  const tenantStore = useTenantStore()
  const userStore = useUserStore()

  const state: ProtectionState = {
    cleanupFns: [],
    previousUserSelect: el.style.userSelect,
    previousWebkitUserSelect: (el.style as any).webkitUserSelect,
    previousMozUserSelect: (el.style as any).MozUserSelect,
    previousMsUserSelect: (el.style as any).msUserSelect
  }

  elementStates.set(el, state)

  if (hasWindow) {
    const computed = window.getComputedStyle?.(el)
    if (!computed || computed.position === 'static') {
      state.previousPosition = el.style.position
      el.style.position = 'relative'
      state.positionPatched = true
    }
  }

  el.classList.add('no-select')
  el.setAttribute('data-content-protected', 'true')
  el.setAttribute('draggable', 'false')
  el.style.userSelect = 'none'
  ;(el.style as any).webkitUserSelect = 'none'
  ;(el.style as any).MozUserSelect = 'none'
  ;(el.style as any).msUserSelect = 'none'

  attachGlobalHandlers()
  protectedRoots.add(el)

  const localHandlers: Array<[string, (event: Event) => void]> = [
    ['copy', preventAndReset],
    ['cut', preventAndReset],
    ['contextmenu', preventAndReset],
    ['selectstart', preventAndReset],
    ['dragstart', preventAndReset]
  ]

  localHandlers.forEach(([eventName, handler]) => {
    const bound = (event: Event) => {
      if (!shouldBlockForTarget(event.target)) return
      handler(event)
      emitBlockEvent(eventName, event)
    }
    el.addEventListener(eventName, bound, true)
    state.cleanupFns.push(() => el.removeEventListener(eventName, bound, true))
  })

  if (hasDocument) {
    const visibilityHandler = () => {
      if (document.visibilityState === 'hidden') {
        clearProtectedSelection()
      }
    }
    document.addEventListener('visibilitychange', visibilityHandler, true)
    state.cleanupFns.push(() => document.removeEventListener('visibilitychange', visibilityHandler, true))
  }

  const observer = new MutationObserver(() => {
    applyMediaGuards(el)
  })
  observer.observe(el, { childList: true, subtree: true })
  state.observer = observer
  state.cleanupFns.push(() => observer.disconnect())

  applyMediaGuards(el)

  setupDevtoolsMonitor(el, state)
  state.cleanupFns.push(() => clearDevtoolsState(el, state))

  if (tenantStore.enableWatermark) {
    createWatermark(el, {
      text: tenantStore.watermarkText,
      username: userStore.displayName,
      opacity: tenantStore.watermarkOpacity
    })
  }
}

function disableProtection(el: HTMLElement) {
  const state = elementStates.get(el)
  if (state) {
    state.cleanupFns.forEach(fn => fn())
    elementStates.delete(el)
  }

  releaseMediaGuards(el)
  removeWatermark(el)
  el.removeAttribute(DEVTOOLS_ATTRIBUTE)

  el.classList.remove('no-select')
  el.removeAttribute('data-content-protected')
  el.removeAttribute('draggable')

  if (state) {
    el.style.userSelect = state.previousUserSelect ?? ''
    ;(el.style as any).webkitUserSelect = state.previousWebkitUserSelect ?? ''
    ;(el.style as any).MozUserSelect = state.previousMozUserSelect ?? ''
    ;(el.style as any).msUserSelect = state.previousMsUserSelect ?? ''
    if (state.positionPatched) {
      el.style.position = state.previousPosition ?? ''
    }
  }

  protectedRoots.delete(el)
  detachGlobalHandlers()
}

function applyMediaGuards(root: HTMLElement) {
  const mediaElements = root.querySelectorAll<HTMLElement>('img, video, audio, svg, canvas')
  mediaElements.forEach(media => {
    media.setAttribute('draggable', 'false')
    attachMediaGuard(media)
  })
}

function attachMediaGuard(node: HTMLElement) {
  const existing = (node as any)[MEDIA_GUARD_KEY] as EventListener | undefined
  if (existing) return
  const guard: EventListener = event => {
    preventAndReset(event)
    emitBlockEvent(event.type, event)
  }
  node.addEventListener('dragstart', guard, true)
  node.addEventListener('contextmenu', guard, true)
  ;(node as any)[MEDIA_GUARD_KEY] = guard
}

function releaseMediaGuards(root: HTMLElement) {
  const mediaElements = root.querySelectorAll<HTMLElement>('img, video, audio, svg, canvas')
  mediaElements.forEach(media => {
    const guard = (media as any)[MEDIA_GUARD_KEY] as EventListener | undefined
    if (guard) {
      media.removeEventListener('dragstart', guard, true)
      media.removeEventListener('contextmenu', guard, true)
      delete (media as any)[MEDIA_GUARD_KEY]
    }
    media.removeAttribute('draggable')
  })
}

function setupDevtoolsMonitor(el: HTMLElement, state: ProtectionState) {
  if (!hasWindow) {
    return
  }

  const evaluate = () => {
    const widthGap = Math.abs(window.outerWidth - window.innerWidth)
    const heightGap = Math.abs(window.outerHeight - window.innerHeight)
    const adaptiveThreshold = Math.max(DEVTOOLS_DIMENSION_THRESHOLD, Math.round(window.innerWidth * 0.1))
    const detected = widthGap > adaptiveThreshold || heightGap > adaptiveThreshold
    setDevtoolsStatus(el, state, detected)
  }

  const intervalId = window.setInterval(evaluate, DEVTOOLS_CHECK_INTERVAL)
  state.cleanupFns.push(() => window.clearInterval(intervalId))

  const resizeHandler = () => evaluate()
  window.addEventListener('resize', resizeHandler, true)
  state.cleanupFns.push(() => window.removeEventListener('resize', resizeHandler, true))

  const focusHandler = () => setTimeout(evaluate, 80)
  const blurHandler = () => setTimeout(evaluate, 80)
  window.addEventListener('focus', focusHandler, true)
  window.addEventListener('blur', blurHandler, true)
  state.cleanupFns.push(() => {
    window.removeEventListener('focus', focusHandler, true)
    window.removeEventListener('blur', blurHandler, true)
  })

  evaluate()
}

function setDevtoolsStatus(el: HTMLElement, state: ProtectionState, open: boolean) {
  if (state.devtoolsLocked === open) {
    return
  }

  state.devtoolsLocked = open

  if (open) {
    el.setAttribute(DEVTOOLS_ATTRIBUTE, 'true')
    showDevtoolsOverlay(el, state)
    emitBlockEvent('devtools', new Event('devtools-detect'), { devtools: true })
    clearProtectedSelection()
  } else {
    el.removeAttribute(DEVTOOLS_ATTRIBUTE)
    hideDevtoolsOverlay(state)
  }
}

function showDevtoolsOverlay(el: HTMLElement, state: ProtectionState) {
  if (state.devtoolsOverlay && state.devtoolsOverlay.isConnected) {
    return
  }

  const overlay = document.createElement('div')
  overlay.className = DEVTOOLS_OVERLAY_CLASS
  overlay.setAttribute('role', 'alert')
  overlay.innerHTML = `
    <div class="${DEVTOOLS_MESSAGE_CLASS}">
      <strong>检测到开发者工具已开启</strong>
      <span>为保护作者权益，已临时锁定本文内容。请关闭开发者工具后继续阅读。</span>
    </div>
  `

  if (getComputedStyle(el).position === 'static' && !state.positionPatched) {
    state.previousPosition = el.style.position
    el.style.position = 'relative'
    state.positionPatched = true
  }

  el.appendChild(overlay)
  state.devtoolsOverlay = overlay
}

function hideDevtoolsOverlay(state: ProtectionState) {
  const overlay = state.devtoolsOverlay
  if (overlay && overlay.parentNode) {
    overlay.parentNode.removeChild(overlay)
  }
  state.devtoolsOverlay = null
}

function clearDevtoolsState(el: HTMLElement, state: ProtectionState) {
  state.devtoolsLocked = false
  el.removeAttribute(DEVTOOLS_ATTRIBUTE)
  hideDevtoolsOverlay(state)
}

function preventAndReset(event: Event) {
  event.preventDefault()
  event.stopPropagation()
  if ('stopImmediatePropagation' in event) {
    event.stopImmediatePropagation()
  }
  ;(event as any).returnValue = false
}

function resolveRootForEvent(target: EventTarget | null): HTMLElement | null {
  if (target instanceof Node) {
    for (const root of protectedRoots) {
      if (root === target || root.contains(target)) {
        return root
      }
    }
  }
  const iterator = protectedRoots.values()
  const first = iterator.next()
  return first.done ? null : first.value
}

function emitBlockEvent(type: string, sourceEvent: Event, extra?: Record<string, unknown>) {
  const root = resolveRootForEvent(sourceEvent.target)
  if (!root) return
  const detail = {
    type,
    timestamp: Date.now(),
    ...(extra ?? {})
  }
  root.dispatchEvent(new CustomEvent(BLOCK_EVENT_NAME, {
    detail,
    bubbles: true
  }))
}

function isEditableTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false
  if (target.isContentEditable) return true
  const tag = target.tagName
  return tag === 'INPUT' || tag === 'TEXTAREA'
}

function isTargetWithinProtected(target: EventTarget | null): boolean {
  if (!(target instanceof Node)) return false
  for (const root of protectedRoots) {
    if (root === target || root.contains(target)) {
      return true
    }
  }
  return false
}

function isNodeWithinProtected(node: Node | null): boolean {
  if (!node) return false
  let current: Node | null = node
  while (current) {
    if (current instanceof HTMLElement && protectedRoots.has(current)) {
      return true
    }
    current = current.parentNode
  }
  return false
}

function selectionTouchesProtected(): boolean {
  if (!hasWindow) return false
  const selection = window.getSelection?.()
  if (!selection || selection.rangeCount === 0) return false
  for (let index = 0; index < selection.rangeCount; index += 1) {
    const range = selection.getRangeAt(index)
    if (isNodeWithinProtected(range.startContainer) || isNodeWithinProtected(range.endContainer)) {
      return true
    }
  }
  return false
}

function clearProtectedSelection() {
  if (!hasWindow) return
  if (!selectionTouchesProtected()) return
  const selection = window.getSelection?.()
  selection?.removeAllRanges()
}

function shouldBlockKeyboard(event: KeyboardEvent): boolean {
  if (protectedRoots.size === 0) return false

  const key = event.key
  const upper = key.toUpperCase()

  if (DEVTOOLS_KEYS.has(upper)) {
    return true
  }

  const ctrlLike = event.ctrlKey || event.metaKey

  if (ctrlLike && event.shiftKey && BLOCKED_CTRL_SHIFT_KEYS.has(upper)) {
    return true
  }

  if (ctrlLike && event.altKey && BLOCKED_CTRL_ALT_KEYS.has(upper)) {
    return true
  }

  if (ctrlLike && BLOCKED_CTRL_KEYS.has(upper)) {
    if (isEditableTarget(event.target)) return false
    return isTargetWithinProtected(event.target) || selectionTouchesProtected()
  }

  return false
}

// 防复制指令
export const vContentProtect: Directive = {
  mounted(el: HTMLElement, binding) {
    const shouldProtect = binding.value !== false
    if (shouldProtect) {
      enableProtection(el)
    } else {
      disableProtection(el)
    }
  },

  updated(el: HTMLElement, binding) {
    const shouldProtect = binding.value !== false
    if (shouldProtect) {
      enableProtection(el)
    } else {
      disableProtection(el)
    }
  },

  unmounted(el: HTMLElement) {
    disableProtection(el)
  }
}

// 创建水印
function createWatermark(container: HTMLElement, options: {
  text: string
  username: string
  opacity: number
}) {
  const existingWatermark = container.querySelector('.content-watermark')
  if (existingWatermark) {
    existingWatermark.remove()
  }

  const normalizedOpacity = Math.min(
    Math.max(Number.isFinite(options.opacity) ? Number(options.opacity) : 0.3, 0),
    1
  )

  const watermark = document.createElement('div')
  watermark.className = 'content-watermark'
  watermark.style.cssText = `
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 9999;
    background-image: repeating-linear-gradient(
      45deg,
      transparent,
      transparent 120px,
      rgba(0, 0, 0, ${Math.min(normalizedOpacity / 2, 0.1)}) 120px,
      rgba(0, 0, 0, ${Math.min(normalizedOpacity / 2, 0.1)}) 240px
    );
  `

  const textWatermark = document.createElement('div')
  textWatermark.style.cssText = `
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) rotate(-45deg);
    font-size: 24px;
    color: rgba(0, 0, 0, ${Math.min(normalizedOpacity, 0.35)});
    font-weight: bold;
    white-space: nowrap;
    user-select: none;
    pointer-events: none;
  `
  textWatermark.textContent = `${options.text} - ${options.username}`

  watermark.appendChild(textWatermark)

  if (getComputedStyle(container).position === 'static') {
    container.style.position = 'relative'
  }

  container.appendChild(watermark)
}

function removeWatermark(container: HTMLElement) {
  const watermark = container.querySelector('.content-watermark')
  if (watermark) {
    watermark.remove()
  }
}

export function shouldProtectContent(articleAllowCopy: boolean | string | number | undefined): boolean {
  const tenantStore = useTenantStore()
  const copyPolicy = tenantStore.copyPolicy

  let allowCopy = true
  if (typeof articleAllowCopy === 'string') {
    const normalized = articleAllowCopy.trim().toLowerCase()
    if (normalized === '') {
      allowCopy = true
    } else if (['1', 'true', 'yes', 'allow', 'y', 'on', 'allow_copy'].includes(normalized)) {
      allowCopy = true
    } else if (['0', 'false', 'no', 'deny', 'denied', 'n', 'off', 'forbid', 'forbidden', 'ban', 'block', 'disabled'].includes(normalized)) {
      allowCopy = false
    }
  } else if (typeof articleAllowCopy === 'boolean') {
    allowCopy = articleAllowCopy
  } else if (typeof articleAllowCopy === 'number') {
    allowCopy = articleAllowCopy !== 0
  }

  switch (copyPolicy) {
    case 'global_allow':
      return false
    case 'global_deny':
      return true
    case 'follow_article':
    default:
      return !allowCopy
  }
}

export default {
  install(app: any) {
    app.directive('content-protect', vContentProtect)
  }
}
