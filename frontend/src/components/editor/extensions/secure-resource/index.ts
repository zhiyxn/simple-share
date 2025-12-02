import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import type { ExtensionOptions } from '../../types'
import SecureResourceView from './SecureResourceView.vue'
import { Lock } from '@element-plus/icons-vue'
import {
  SECURE_RESOURCE_DEFAULTS,
  SECURE_RESOURCE_PLACEHOLDER_TEXT,
} from '@/utils/secureResource'

interface SecureResourceOptions {
  title?: string
  type?: string
  url?: string
  secret?: string
  notice?: string
}

const DEFAULT_TITLE = SECURE_RESOURCE_DEFAULTS.title
const DEFAULT_TYPE = SECURE_RESOURCE_DEFAULTS.type
const DEFAULT_NOTICE = SECURE_RESOURCE_DEFAULTS.notice

interface SecureResourceConfigPayload {
  title?: string
  type?: string
  url?: string
  secret?: string
  notice?: string
}

function decodeConfig(raw: string | null): SecureResourceConfigPayload | null {
  if (!raw) {
    return null
  }
  try {
    const decoded = decodeURIComponent(raw)
    const parsed = JSON.parse(decoded)
    if (parsed && typeof parsed === 'object') {
      return parsed as SecureResourceConfigPayload
    }
  } catch {
    // swallow parsing errors, fall back to defaults
  }
  return null
}

function encodeConfig(config: SecureResourceConfigPayload): string {
  try {
    return encodeURIComponent(JSON.stringify(config))
  } catch {
    return ''
  }
}

const SecureResource = Node.create<ExtensionOptions>({
  name: 'secureResource',
  group: 'block',
  atom: true,
  selectable: true,
  draggable: false,

  addOptions() {
    return {
      getCommandMenuItems: () => ({
        priority: 160,
        icon: markRaw(Lock),
        title: '重要链接',
        keywords: ['secure', 'link', 'baidupan', 'quark', 'mima', 'password', '资源'],
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setSecureResource()
            .run()
        },
      }),
    }
  },

  addAttributes() {
    return {
      title: {
        default: DEFAULT_TITLE,
        parseHTML: (element) =>
          element.getAttribute('data-title') ||
          decodeConfig(element.getAttribute('data-config'))?.title ||
          DEFAULT_TITLE,
        renderHTML: (attributes) => ({
          'data-title': attributes.title || DEFAULT_TITLE,
        }),
      },
      type: {
        default: DEFAULT_TYPE,
        parseHTML: (element) =>
          element.getAttribute('data-resource-type') ||
          decodeConfig(element.getAttribute('data-config'))?.type ||
          DEFAULT_TYPE,
        renderHTML: (attributes) => ({
          'data-resource-type': attributes.type || DEFAULT_TYPE,
        }),
      },
      url: {
        default: '',
        parseHTML: (element) =>
          element.getAttribute('data-url') ||
          decodeConfig(element.getAttribute('data-config'))?.url ||
          '',
        renderHTML: (attributes) =>
          attributes.url ? { 'data-url': attributes.url } : {},
      },
      secret: {
        default: '',
        parseHTML: (element) =>
          element.getAttribute('data-secret') ||
          decodeConfig(element.getAttribute('data-config'))?.secret ||
          '',
        renderHTML: (attributes) =>
          attributes.secret ? { 'data-secret': attributes.secret } : {},
      },
      notice: {
        default: DEFAULT_NOTICE,
        parseHTML: (element) =>
          element.getAttribute('data-notice') ||
          decodeConfig(element.getAttribute('data-config'))?.notice ||
          DEFAULT_NOTICE,
        renderHTML: (attributes) => ({
          'data-notice': attributes.notice || DEFAULT_NOTICE,
        }),
      },
      config: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-config') || '',
        renderHTML: (attributes) =>
          attributes.config ? { 'data-config': attributes.config } : {},
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'details[data-type="secure-resource"]',
      },
      {
        tag: 'div[data-type="secure-resource"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
      const configFromAttr = typeof HTMLAttributes.config === 'string'
        ? decodeConfig(HTMLAttributes.config)
        : null

      const title =
        typeof HTMLAttributes.title === 'string' && HTMLAttributes.title.trim()
          ? HTMLAttributes.title
          : (configFromAttr?.title && configFromAttr.title.trim()) || DEFAULT_TITLE
      const resourceType =
        typeof HTMLAttributes.type === 'string' && HTMLAttributes.type.trim()
          ? HTMLAttributes.type
          : (configFromAttr?.type && configFromAttr.type.trim()) || DEFAULT_TYPE
      const url =
        typeof HTMLAttributes.url === 'string' ? HTMLAttributes.url.trim() : ''
          || (configFromAttr?.url && configFromAttr.url.trim()) || ''
      const secret =
        typeof HTMLAttributes.secret === 'string'
          ? HTMLAttributes.secret.trim()
          : ''
          || (configFromAttr?.secret && configFromAttr.secret.trim()) || ''
      const notice =
        typeof HTMLAttributes.notice === 'string' && HTMLAttributes.notice.trim()
          ? HTMLAttributes.notice
          : (configFromAttr?.notice && configFromAttr.notice.trim()) || ''
      const className =
        typeof HTMLAttributes.class === 'string' ? HTMLAttributes.class : ''
      const style =
        typeof HTMLAttributes.style === 'string' ? HTMLAttributes.style : undefined
      const id =
      typeof HTMLAttributes.id === 'string' ? HTMLAttributes.id : undefined

    const attrs: Record<string, string> = {
      'data-type': 'secure-resource',
      'data-title': title,
      'data-resource-type': resourceType,
      class: ['secure-resource-block', className].filter(Boolean).join(' '),
    }

    if (style) {
      attrs.style = style
    }
    if (id) {
      attrs.id = id
    }
    if (url) {
      attrs['data-url'] = url
    }
    if (secret) {
      attrs['data-secret'] = secret
    }
    if (notice) {
      attrs['data-notice'] = notice
    }
    const configPayload: SecureResourceConfigPayload = {
      title,
      type: resourceType,
      url,
      secret,
      notice: notice || DEFAULT_NOTICE,
    }
    const encodedConfig = encodeConfig(configPayload)
    if (encodedConfig) {
      attrs['data-config'] = encodedConfig
    } else if (typeof HTMLAttributes.config === 'string' && HTMLAttributes.config) {
      attrs['data-config'] = HTMLAttributes.config
    }

    const children: any[] = []

    children.push([
      'div',
      { class: 'secure-resource-block__header' },
      [
        'div',
        { class: 'secure-resource-block__heading' },
        ['span', { class: 'secure-resource-block__icon' }, '🔐'],
        [
          'div',
          { class: 'secure-resource-block__title-group' },
          ['span', { class: 'secure-resource-block__title' }, title],
          ['span', { class: 'secure-resource-block__type' }, resourceType],
        ],
      ],
    ])

    const bodyFields: any[] = []

    const urlValueChildren: any[] = []
    if (url) {
      urlValueChildren.push([
        'a',
        {
          href: url,
          target: '_blank',
          rel: 'noopener noreferrer',
          class: 'secure-resource-block__link',
        },
        url,
      ])
      urlValueChildren.push([
        'button',
        {
          type: 'button',
          class: 'secure-resource-block__copy',
          'data-copy': 'url',
        },
        '复制链接',
      ])
    } else {
      urlValueChildren.push([
        'span',
        { class: 'secure-resource-block__placeholder' },
        SECURE_RESOURCE_PLACEHOLDER_TEXT,
      ])
    }

    bodyFields.push([
      'div',
      { class: 'secure-resource-block__field', 'data-field': 'url' },
      ['span', { class: 'secure-resource-block__label' }, '访问链接'],
      [
        'div',
        {
          class: [
            'secure-resource-block__value',
            url ? '' : 'secure-resource-block__value--empty',
          ]
            .filter(Boolean)
            .join(' '),
        },
        ...urlValueChildren,
      ],
    ])

    const secretValueChildren: any[] = []
    if (secret) {
      secretValueChildren.push([
        'span',
        { class: 'secure-resource-block__secret' },
        secret,
      ])
      secretValueChildren.push([
        'button',
        {
          type: 'button',
          class: 'secure-resource-block__copy',
          'data-copy': 'secret',
        },
        '复制密码',
      ])
    } else {
      secretValueChildren.push([
        'span',
        { class: 'secure-resource-block__placeholder' },
        SECURE_RESOURCE_PLACEHOLDER_TEXT,
      ])
    }

    bodyFields.push([
      'div',
      { class: 'secure-resource-block__field', 'data-field': 'secret' },
      ['span', { class: 'secure-resource-block__label' }, '提取码 / 密码'],
      [
        'div',
        {
          class: [
            'secure-resource-block__value',
            secret ? '' : 'secure-resource-block__value--empty',
          ]
            .filter(Boolean)
            .join(' '),
        },
        ...secretValueChildren,
      ],
    ])

    if (bodyFields.length) {
      children.push(['div', { class: 'secure-resource-block__body' }, ...bodyFields])
    }

    const displayNotice = notice || (!url && !secret ? DEFAULT_NOTICE : '')
    if (displayNotice) {
      children.push([
        'div',
        { class: 'secure-resource-block__notice' },
        displayNotice,
      ])
    }

    return ['div', attrs, ...children]
  },

  addNodeView() {
    return VueNodeViewRenderer(SecureResourceView)
  },

  addCommands() {
    return {
      setSecureResource:
        (options: SecureResourceOptions = {}) =>
        ({ commands }) => {
          const normalized: SecureResourceConfigPayload = {
            title: options.title || DEFAULT_TITLE,
            type: options.type || DEFAULT_TYPE,
            url: options.url || '',
            secret: options.secret || '',
            notice: options.notice || DEFAULT_NOTICE,
          }
          return commands.insertContent({
            type: this.name,
            attrs: {
              ...normalized,
              config: encodeConfig(normalized),
            },
          })
        },
      updateSecureResource:
        (options: SecureResourceOptions = {}) =>
        ({ commands }) => {
          const attrs: Record<string, any> = {
            ...options,
          }
          const providedKeys = ['title', 'type', 'url', 'secret', 'notice'] as const
          const hasAllFields = providedKeys.every((key) => options[key] !== undefined)
          if (hasAllFields) {
            const normalized: SecureResourceConfigPayload = {
              title: options.title || DEFAULT_TITLE,
              type: options.type || DEFAULT_TYPE,
              url: options.url || '',
              secret: options.secret || '',
              notice: options.notice || DEFAULT_NOTICE,
            }
            attrs.config = encodeConfig(normalized)
          }
          return commands.updateAttributes(this.name, attrs)
        },
    }
  },
})

export default SecureResource
