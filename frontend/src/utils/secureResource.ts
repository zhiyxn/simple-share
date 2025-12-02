export type SecureResourceConfigInput = {
  title?: string | null
  type?: string | null
  url?: string | null
  secret?: string | null
  notice?: string | null
}

export type SecureResourceConfig = {
  title: string
  type: string
  url: string
  secret: string
  notice: string
}

export const SECURE_RESOURCE_PLACEHOLDER_TEXT = '未填写'

export const SECURE_RESOURCE_DEFAULTS: SecureResourceConfig = {
  title: '重要资源',
  type: '百度网盘',
  url: '',
  secret: '',
  notice: '链接及密码已展示，可直接复制使用。',
}

export const SECURE_RESOURCE_PLACEHOLDER_TAG = 'secure-resource-placeholder'

const escapeHtml = (value: string): string =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

const escapeAttribute = (value: string): string => escapeHtml(value).replace(/"/g, '&quot;')

const toString = (value: unknown): string => {
  if (value === null || value === undefined) {
    return ''
  }
  return String(value)
}

const isPlaceholderContent = (value: string): boolean => value === SECURE_RESOURCE_PLACEHOLDER_TEXT

export const normalizeSecureResourceConfig = (
  input?: SecureResourceConfigInput | null
): SecureResourceConfig => {
  const title = toString(input?.title).trim()
  const type = toString(input?.type).trim()
  const url = toString(input?.url).trim()
  const secret = toString(input?.secret).trim()
  const notice = toString(input?.notice).trim()

  return {
    title: title || SECURE_RESOURCE_DEFAULTS.title,
    type: type || SECURE_RESOURCE_DEFAULTS.type,
    url,
    secret,
    notice,
  }
}

export const encodeSecureResourceConfig = (
  input: SecureResourceConfigInput | SecureResourceConfig
): string => {
  try {
    const normalized = normalizeSecureResourceConfig(input)
    return encodeURIComponent(JSON.stringify(normalized))
  } catch {
    return ''
  }
}

export const decodeSecureResourceConfig = (
  raw: string | null | undefined
): SecureResourceConfig | null => {
  if (!raw) {
    return null
  }
  try {
    const decoded = decodeURIComponent(raw)
    const parsed = JSON.parse(decoded)
    if (!parsed || typeof parsed !== 'object') {
      return null
    }
    return normalizeSecureResourceConfig(parsed as SecureResourceConfigInput)
  } catch {
    return null
  }
}

export const collectSecureResourceConfig = (element: Element): SecureResourceConfig => {
  const base =
    decodeSecureResourceConfig(element.getAttribute('data-config')) ?? SECURE_RESOURCE_DEFAULTS

  const titleAttr = element.getAttribute('data-title')
  const typeAttr = element.getAttribute('data-resource-type')
  const urlAttr = element.getAttribute('data-url')
  const secretAttr = element.getAttribute('data-secret')
  const noticeAttr = element.getAttribute('data-notice')

  let title = titleAttr ? titleAttr.trim() : base.title
  let type = typeAttr ? typeAttr.trim() : base.type
  let url = urlAttr ? urlAttr.trim() : base.url
  let secret = secretAttr ? secretAttr.trim() : base.secret
  let notice = noticeAttr ? noticeAttr.trim() : base.notice

  if (element instanceof HTMLElement) {
    const readText = (node: Element | null | undefined): string =>
      node?.textContent?.trim() || ''

    if (!title) {
      const fromSummary =
        element.querySelector<HTMLElement>('.secure-resource-block__title')
        ?? element.querySelector<HTMLElement>('.secure-resource-title')
      title = readText(fromSummary) || title
    }
    if (!type) {
      const fromType =
        element.querySelector<HTMLElement>('.secure-resource-block__type')
        ?? element.querySelector<HTMLElement>('.secure-resource-type')
      type = readText(fromType) || type
    }
    if (!url) {
      const link =
        element.querySelector<HTMLAnchorElement>('.secure-resource-block__link')
        ?? element.querySelector<HTMLAnchorElement>('.secure-resource-field-value a')
      const href = link?.getAttribute('href')?.trim()
      const text = link?.textContent?.trim()
      if (href && !isPlaceholderContent(href)) {
        url = href
      } else if (text && !isPlaceholderContent(text)) {
        url = text
      }
    }
    if (!secret) {
      const secretSpan =
        element.querySelector<HTMLElement>('.secure-resource-block__secret')
        ?? element.querySelector<HTMLElement>('.secure-resource-field-value span')
      const candidate = readText(secretSpan)
      if (candidate && !isPlaceholderContent(candidate)) {
        secret = candidate
      }
    }
    if (!notice) {
      const noticeEl = element.querySelector<HTMLElement>('.secure-resource-block__notice')
      notice = readText(noticeEl) || notice
    }

    if (!url || !secret) {
      const fields = Array.from(
        element.querySelectorAll<HTMLElement>('.secure-resource-field')
      )
      fields.forEach((field) => {
        const label = readText(field.querySelector('.secure-resource-field-label'))
        const normalizedLabel = label.replace(/\s+/g, '')
        const valueRoot = field.querySelector<HTMLElement>('.secure-resource-field-value')
        if (!valueRoot) {
          return
        }

        if (!url && normalizedLabel.includes('访问链接')) {
          const link =
            valueRoot.querySelector<HTMLAnchorElement>('a[href]')
            ?? valueRoot.querySelector<HTMLAnchorElement>('a')
          const href = link?.getAttribute('href')?.trim()
          const text = link?.textContent?.trim()
          if (href && !isPlaceholderContent(href)) {
            url = href
          } else if (text && !isPlaceholderContent(text)) {
            url = text
          } else {
            const fallback = Array.from(valueRoot.childNodes)
              .map((node) => (node.textContent ?? '').trim())
              .find((textContent) => textContent && !isPlaceholderContent(textContent))
            if (fallback) {
              url = fallback
            }
          }
        } else if (!secret && /提取|密码/.test(normalizedLabel)) {
          const secretNode =
            valueRoot.querySelector<HTMLElement>('.secure-resource-block__secret')
            ?? valueRoot.querySelector<HTMLElement>('span')
            ?? valueRoot
          const candidate = readText(secretNode)
          if (candidate && !/复制|button/i.test(candidate) && !isPlaceholderContent(candidate)) {
            secret = candidate
          }
        }
      })
    }
  }

  return normalizeSecureResourceConfig({
    title,
    type,
    url,
    secret,
    notice,
  })
}

export const createSecureResourcePlaceholderHTML = (config: SecureResourceConfig): string => {
  const encodedConfig = encodeSecureResourceConfig(config)
  const attributes = [
    ['data-config', encodedConfig],
    ['data-title', config.title],
    ['data-resource-type', config.type],
    ['data-url', config.url],
    ['data-secret', config.secret],
    ['data-notice', config.notice],
  ]
    .filter(([, value]) => value !== undefined && value !== null)
    .map(([key, value]) => ` ${key}="${escapeAttribute(String(value ?? ''))}"`)
    .join('')

  return `<${SECURE_RESOURCE_PLACEHOLDER_TAG}${attributes}></${SECURE_RESOURCE_PLACEHOLDER_TAG}>`
}

export const renderSecureResourceHTML = (
  input: SecureResourceConfigInput | SecureResourceConfig
): string => {
  const config = normalizeSecureResourceConfig(input)
  const encodedConfig = encodeSecureResourceConfig(config)

  const attrSegments: Array<[string, string]> = [
    ['data-type', 'secure-resource'],
    ['class', 'secure-resource-block'],
    ['data-title', config.title],
    ['data-resource-type', config.type],
    ['data-config', encodedConfig],
  ]

  if (config.url) {
    attrSegments.push(['data-url', config.url])
  }
  if (config.secret) {
    attrSegments.push(['data-secret', config.secret])
  }
  if (config.notice) {
    attrSegments.push(['data-notice', config.notice])
  }

  const attributes = attrSegments.map(([key, value]) => ` ${key}="${escapeAttribute(value)}"`).join('')

  const header = [
    '<div class="secure-resource-block__header">',
    '<div class="secure-resource-block__heading">',
    '<span class="secure-resource-block__icon">🔐</span>',
    '<div class="secure-resource-block__title-group">',
    `<span class="secure-resource-block__title">${escapeHtml(config.title)}</span>`,
    `<span class="secure-resource-block__type">${escapeHtml(config.type)}</span>`,
    '</div>',
    '</div>',
    '</div>',
  ].join('')

  const linkValue = config.url
    ? `<a href="${escapeAttribute(config.url)}" target="_blank" rel="noopener noreferrer" class="secure-resource-block__link">${escapeHtml(config.url)}</a><button type="button" class="secure-resource-block__copy" data-copy="url">复制链接</button>`
    : `<span class="secure-resource-block__placeholder">${escapeHtml(SECURE_RESOURCE_PLACEHOLDER_TEXT)}</span>`

  const secretValue = config.secret
    ? `<span class="secure-resource-block__secret">${escapeHtml(config.secret)}</span><button type="button" class="secure-resource-block__copy" data-copy="secret">复制密码</button>`
    : `<span class="secure-resource-block__placeholder">${escapeHtml(SECURE_RESOURCE_PLACEHOLDER_TEXT)}</span>`

  const linkField = [
    '<div class="secure-resource-block__field" data-field="url">',
    '<span class="secure-resource-block__label">访问链接</span>',
    `<div class="secure-resource-block__value${config.url ? '' : ' secure-resource-block__value--empty'}">`,
    linkValue,
    '</div>',
    '</div>',
  ].join('')

  const secretField = [
    '<div class="secure-resource-block__field" data-field="secret">',
    '<span class="secure-resource-block__label">提取码 / 密码</span>',
    `<div class="secure-resource-block__value${config.secret ? '' : ' secure-resource-block__value--empty'}">`,
    secretValue,
    '</div>',
    '</div>',
  ].join('')

  const body = `<div class="secure-resource-block__body">${linkField}${secretField}</div>`

  const noticeText =
    config.notice || (!config.url && !config.secret ? SECURE_RESOURCE_DEFAULTS.notice : '')
  const noticeSection = noticeText
    ? `<div class="secure-resource-block__notice">${escapeHtml(noticeText)}</div>`
    : ''

  return `<div${attributes}>${header}${body}${noticeSection}</div>`
}

const SECURE_RESOURCE_PRIMARY_DATA_ATTRIBUTES = new Set([
  'data-type',
  'data-title',
  'data-resource-type',
  'data-url',
  'data-secret',
  'data-notice',
  'data-config',
])

export const ensureSecureResourceConsistency = (html: string): string => {
  if (!html) {
    return ''
  }
  if (typeof document === 'undefined') {
    return html
  }

  const container = document.createElement('div')
  container.innerHTML = html

  const nodes = Array.from(
    container.querySelectorAll<HTMLElement>('details[data-type="secure-resource"], div[data-type="secure-resource"]')
  )

  nodes.forEach((node) => {
    const config = collectSecureResourceConfig(node)
    const wrapper = document.createElement('div')
    wrapper.innerHTML = renderSecureResourceHTML(config)
    const next = wrapper.firstElementChild as HTMLElement | null
    if (!next) {
      return
    }

    if (node.hasAttribute('open')) {
      next.setAttribute('open', '')
    }

    if (node.id) {
      next.id = node.id
    }

    const existingClass = node.getAttribute('class')
    if (existingClass) {
      existingClass
        .split(/\s+/)
        .filter(Boolean)
        .forEach((cls) => {
          if (!next.classList.contains(cls)) {
            next.classList.add(cls)
          }
        })
    }

    Array.from(node.attributes).forEach((attr) => {
      const name = attr.name
      if (name === 'class' || name === 'open' || name === 'id') {
        return
      }
      if (SECURE_RESOURCE_PRIMARY_DATA_ATTRIBUTES.has(name)) {
        return
      }
      if (!next.hasAttribute(name)) {
        next.setAttribute(name, attr.value)
      }
    })

    node.replaceWith(next)
  })

  return container.innerHTML
}
