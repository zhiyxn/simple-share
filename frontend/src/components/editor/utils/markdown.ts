import MarkdownIt from 'markdown-it'
import markdownItContainer from 'markdown-it-container'
import markdownItDeflist from 'markdown-it-deflist'
import { full as markdownItEmoji } from 'markdown-it-emoji'
import markdownItFootnote from 'markdown-it-footnote'
import markdownItMark from 'markdown-it-mark'
import markdownItSub from 'markdown-it-sub'
import markdownItSup from 'markdown-it-sup'
import markdownItTaskLists from 'markdown-it-task-lists'
import TurndownService from 'turndown'
import * as turndownPluginGfm from 'turndown-plugin-gfm'

export type ContentFormat = 'markdown' | 'html'

const HTML_PATTERN = /<\/?[a-z][^>]*>/i

const IGNORED_BLOCK_TOKEN_TYPES = new Set([
  'inline',
  'paragraph_open',
  'paragraph_close',
  'text',
  'softbreak',
  'hardbreak',
])

const IGNORED_INLINE_TOKEN_TYPES = new Set(['text', 'softbreak', 'hardbreak'])

const CONTAINER_TYPES: Array<{ name: string; defaultTitle: string }> = [
  { name: 'info', defaultTitle: '信息' },
  { name: 'tip', defaultTitle: '提示' },
  { name: 'note', defaultTitle: '备注' },
  { name: 'success', defaultTitle: '成功' },
  { name: 'warning', defaultTitle: '警告' },
  { name: 'danger', defaultTitle: '危险' },
  { name: 'important', defaultTitle: '重要' },
  { name: 'caution', defaultTitle: '注意' },
]

function createBaseMarkdownIt() {
  const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    breaks: true,
  })

  configureMathSupport(md)

    const usePlugin = (plugin: any, ...params: any[]) => {
      const resolved = typeof plugin === 'function' ? plugin : plugin?.default ?? plugin
      md.use(resolved, ...(params as []))
    }

    usePlugin(markdownItEmoji as any)
  usePlugin(markdownItSub)
  usePlugin(markdownItSup)
  usePlugin(markdownItMark)
  usePlugin(markdownItFootnote)
  usePlugin(markdownItDeflist)
  usePlugin(markdownItTaskLists, {
      enabled: true,
      label: true,
      labelAfter: true,
    })

  CONTAINER_TYPES.forEach(({ name, defaultTitle }) => {
    usePlugin(markdownItContainer, name, {
      validate(params) {
        return params.trim().toLowerCase().startsWith(name)
      },
      render(tokens, idx) {
        const token = tokens[idx]
        if (token.nesting === 1) {
          const rawInfo = token.info.trim().replace(new RegExp(`^${name}`, 'i'), '').trim()
          const title = rawInfo || defaultTitle
          const escaped = md.utils.escapeHtml(title)
          return `<div class="md-admonition md-admonition-${name}"><div class="md-admonition__title">${escaped}</div>\n`
        }
        return '</div>\n'
      },
    })
  })

  return md
}

const markdownParser = createBaseMarkdownIt()

const turndownService = new TurndownService({
  codeBlockStyle: 'fenced',
  fence: '```',
  headingStyle: 'atx',
  bulletListMarker: '-',
  strongDelimiter: '**',
  emDelimiter: '_',
})

const gfmPlugins = [
  turndownPluginGfm.gfm,
  turndownPluginGfm.tables,
  turndownPluginGfm.strikethrough,
  turndownPluginGfm.taskListItems,
  turndownPluginGfm.highlightedCodeBlock,
].filter(Boolean) as ((service: TurndownService) => void)[]

gfmPlugins.forEach((plugin) => {
  turndownService.use(plugin)
})

turndownService.addRule('mathBlock', {
  filter(node) {
    if (node.nodeType !== 1) return false
    const className = (node as Element).getAttribute('class') ?? ''
    return /\bmd-math-block\b/.test(className)
  },
  replacement(content, node) {
    const element = node as Element
    const raw = element.textContent ?? content
    const inner = raw
      .replace(/^\s*\$\$\s*/, '')
      .replace(/\s*\$\$\s*$/, '')
      .trim()
    return `\n$$\n${inner}\n$$\n`
  },
})

turndownService.addRule('mathInline', {
  filter(node) {
    if (node.nodeType !== 1) return false
    const className = (node as Element).getAttribute('class') ?? ''
    return /\bmd-math-inline\b/.test(className)
  },
  replacement(content, node) {
    const element = node as Element
    const raw = element.textContent ?? content
    const inner = raw
      .replace(/^\s*\$\s*/, '')
      .replace(/\s*\$\s*$/, '')
      .trim()
    return `$${inner}$`
  },
})

export function isHtmlContent(value: string | null | undefined): boolean {
  return !!value && HTML_PATTERN.test(value)
}

export function isMarkdownContent(value: string | null | undefined): boolean {
  const trimmed = value?.trim()

  if (!trimmed) {
    return false
  }

  let hasMarkdownStructures = false
  let hasHtmlContent = false

  const HEURISTIC_PATTERNS = [
    /^\s{0,3}#{1,6}\s+\S+/m, // headings
    /^\s{0,3}(?:-|\*|\+)\s+\S+/m, // unordered list
    /^\s{0,3}\d+\.\s+\S+/m, // ordered list
    /```[\s\S]*?```/, // fenced code block
    /~~[^~]+~~/, // strikethrough
    /\*\*[^*]+\*\*/, // bold
    /\*[^*]+\*/, // italic
    /^\|(?:.+\|)+\s*$/m, // table row
    /^\s{0,3}:-{2,}:?\s*\|/m, // table alignment row
    /\$\$[\s\S]*?\$\$/, // block math
    /(^|[^\$])\$(?!\s)[^$\n]+(?!\s)\$(?!\d)/, // inline math
    /\[[^\]]+\]\([^)]+\)/, // links
    /!\[[^\]]*\]\([^)]+\)/, // images
    /^>+\s+\S+/m, // blockquote
    /:::\s*\w+/, // custom container/admonition
    /-\s\[[ xX]\]\s+\S+/, // task list
    /^\s{0,3}\*\s+\[[ xX]\]\s+\S+/m, // task list with *
    /^\s{0,3}\+?\s+\[[ xX]\]\s+\S+/m, // task list with +
  ]

  try {
    const tokens = markdownParser.parse(trimmed, {})

    for (const token of tokens) {
      if (token.type === 'html_block') {
        hasHtmlContent = true
        continue
      }

      if (!IGNORED_BLOCK_TOKEN_TYPES.has(token.type)) {
        hasMarkdownStructures = true
        break
      }

      if (token.type === 'inline' && Array.isArray(token.children)) {
        for (const child of token.children) {
          if (child.type === 'html_inline') {
            hasHtmlContent = true
            continue
          }

          if (!IGNORED_INLINE_TOKEN_TYPES.has(child.type)) {
            hasMarkdownStructures = true
            break
          }
        }
      }

      if (hasMarkdownStructures) {
        break
      }
    }
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('Failed to analyze markdown content', error)
    }
  }

  if (hasMarkdownStructures) {
    return true
  }

  if (HEURISTIC_PATTERNS.some((pattern) => pattern.test(trimmed))) {
    return true
  }

  if (hasHtmlContent) {
    return false
  }

  return false
}

export function detectContentFormat(value: string | null | undefined): ContentFormat {
  return isMarkdownContent(value) ? 'markdown' : 'html'
}

export function markdownToHtml(markdown: string): string {
  if (!markdown) {
    return ''
  }

  try {
    const normalized = normalizeMarkdownTables(markdown)
    return markdownParser.render(normalized)
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('Failed to transform markdown to HTML', error)
    }
    return markdown
  }
}

export function htmlToMarkdown(html: string): string {
  if (!html) {
    return ''
  }

  try {
    return turndownService.turndown(html)
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('Failed to transform HTML to markdown', error)
    }
    return html
  }
}

export function createMarkdownContainer(html: string): HTMLElement | null {
  if (typeof document === 'undefined') {
    return null
  }

  const container = document.createElement('div')
  container.innerHTML = html
  sanitizeMarkdownContainer(container)
  return container
}

export function sanitizeMarkdownContainer(container: HTMLElement) {
  const whitespaceNodes: Text[] = []

  const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT)
  let current = walker.nextNode()

  while (current) {
    const textNode = current as Text
    const value = textNode.nodeValue ?? ''

    if (value.trim().length === 0 && /[\r\n]/.test(value)) {
      const parent = textNode.parentNode
      const parentElement =
        parent && parent.nodeType === Node.ELEMENT_NODE ? (parent as HTMLElement) : null

      if (!parentElement || !parentElement.closest('pre, code, textarea')) {
        whitespaceNodes.push(textNode)
      }
    }

    current = walker.nextNode()
  }

  whitespaceNodes.forEach((node) => node.remove())

  const paragraphs = Array.from(container.querySelectorAll('p'))
  paragraphs.forEach((p) => {
    if (!p.textContent || !p.textContent.trim()) {
      p.remove()
    }
  })

  const breaks = Array.from(container.querySelectorAll('br'))
  breaks.forEach((br) => {
    let prev: ChildNode | null = br.previousSibling

    while (
      prev &&
      prev.nodeType === Node.TEXT_NODE &&
      !(prev.textContent && prev.textContent.trim())
    ) {
      prev = prev.previousSibling
    }

    if (
      prev &&
      prev.nodeType === Node.ELEMENT_NODE &&
      (prev as HTMLElement).tagName === 'BR'
    ) {
      br.remove()
    }
  })
}

export function createMarkdownParser() {
  return createBaseMarkdownIt()
}

function configureMathSupport(md: MarkdownIt) {
  const escapeHtml = md.utils.escapeHtml.bind(md.utils)

  const mathBlock = (state: any, startLine: number, endLine: number, silent: boolean) => {
    let pos = state.bMarks[startLine] + state.tShift[startLine]
    const max = state.eMarks[startLine]

    if (pos + 2 > max) return false
    if (state.src.charCodeAt(pos) !== 0x24 || state.src.charCodeAt(pos + 1) !== 0x24) {
      return false
    }

    pos += 2
    let currentLine = startLine
    let content = ''
    let haveEndMarker = false

    let firstLine = state.src.slice(pos, max)
    if (firstLine.trim().endsWith('$$')) {
      haveEndMarker = true
      firstLine = firstLine.trim()
      content = firstLine.slice(0, -2).trim()
    } else {
      content = firstLine
    }

    while (!haveEndMarker) {
      currentLine += 1
      if (currentLine >= endLine) {
        return false
      }

      pos = state.bMarks[currentLine] + state.tShift[currentLine]
      const lineMax = state.eMarks[currentLine]
      const lineText = state.src.slice(pos, lineMax)
      const trimmed = lineText.trim()

      if (trimmed.startsWith('$$')) {
        const closing = trimmed.slice(2)
        if (closing.length > 0) {
          content += '\n' + closing.trim()
        }
        haveEndMarker = true
        break
      }

      content += '\n' + lineText
    }

    if (!haveEndMarker) {
      return false
    }

    if (silent) {
      return true
    }

    state.line = currentLine + 1

    const token = state.push('math_block', 'math', 0)
    token.block = true
    token.content = content.trim()
    token.map = [startLine, state.line]
    token.markup = '$$'

    return true
  }

  const mathInline = (state: any, silent: boolean) => {
    const start = state.pos
    if (state.src.charCodeAt(start) !== 0x24 /* $ */) {
      return false
    }

    let pos = start + 1
    const max = state.posMax
    let found = false

    while (pos < max) {
      if (state.src.charCodeAt(pos) === 0x24 /* $ */ && state.src.charCodeAt(pos - 1) !== 0x5c /* \ */) {
        found = true
        break
      }
      pos += 1
    }

    if (!found || pos === start + 1) {
      return false
    }

    const content = state.src.slice(start + 1, pos)
    if (content.trim().length === 0) {
      return false
    }

    if (!silent) {
      const token = state.push('math_inline', 'math', 0)
      token.markup = '$'
      token.content = content
    }

    state.pos = pos + 1
    return true
  }

  md.block.ruler.before('fence', 'math_block', mathBlock, {
    alt: ['paragraph', 'reference', 'blockquote', 'list'],
  })
  md.inline.ruler.before('escape', 'math_inline', mathInline)

  md.renderer.rules.math_block = (tokens, idx) => {
    const content = tokens[idx].content ?? ''
    return `<div class="md-math-block">$$\n${escapeHtml(content)}\n$$</div>\n`
  }

  md.renderer.rules.math_inline = (tokens, idx) => {
    const content = tokens[idx].content ?? ''
    return `<span class="md-math-inline">$${escapeHtml(content)}$</span>`
  }
}

function normalizeMarkdownTables(input: string): string {
  const lines = input.split(/\r?\n/)
  const normalized: string[] = []

  let inFence = false
  let fenceChar = ''
  let fenceLength = 0

  let index = 0
  while (index < lines.length) {
    const line = lines[index]
    const trimmedLine = line.trim()

    const fenceInfo = matchFence(trimmedLine)
    if (fenceInfo) {
      if (!inFence) {
        inFence = true
        fenceChar = fenceInfo.char
        fenceLength = fenceInfo.length
      } else if (fenceInfo.char === fenceChar && fenceInfo.length >= fenceLength) {
        inFence = false
        fenceChar = ''
        fenceLength = 0
      }
      normalized.push(line)
      index += 1
      continue
    }

    if (inFence) {
      normalized.push(line)
      index += 1
      continue
    }

    if (!isTableCandidateLine(line)) {
      normalized.push(line)
      index += 1
      continue
    }

    const blockLines: string[] = []
    let cursor = index
    while (cursor < lines.length) {
      const current = lines[cursor]
      if (current.trim() === '' || isTableCandidateLine(current)) {
        blockLines.push(current)
        cursor += 1
        continue
      }
      break
    }

    const nonBlank = blockLines.filter((item) => item.trim() !== '')
    const trailingBlankCount = countTrailingBlankLines(blockLines)

    if (qualifiesAsTable(nonBlank)) {
      normalized.push(...nonBlank)
      for (let i = 0; i < trailingBlankCount; i += 1) {
        normalized.push('')
      }
    } else {
      normalized.push(...blockLines)
    }

    index = cursor
  }

  return normalized.join('\n')
}

function isTableCandidateLine(line: string): boolean {
  const trimmed = line.trim()
  if (trimmed.length === 0 || !trimmed.includes('|')) {
    return false
  }

  if (!trimmed.startsWith('|')) {
    return false
  }

  const cells = splitTableRow(trimmed)
  return cells.length >= 2
}

function countTrailingBlankLines(lines: string[]): number {
  let count = 0
  for (let i = lines.length - 1; i >= 0; i -= 1) {
    if (lines[i].trim() === '') {
      count += 1
    } else {
      break
    }
  }
  return count
}

function qualifiesAsTable(nonBlankLines: string[]): boolean {
  if (nonBlankLines.length < 2) {
    return false
  }

  const header = nonBlankLines[0]
  const divider = nonBlankLines[1]

  if (!isTableRow(header) || !isDividerRow(divider)) {
    return false
  }

  for (let i = 2; i < nonBlankLines.length; i += 1) {
    if (!isTableRow(nonBlankLines[i])) {
      return false
    }
  }

  return true
}

function splitTableRow(line: string): string[] {
  let content = line.trim()
  if (!content.includes('|')) {
    return []
  }

  if (content.startsWith('|')) {
    content = content.slice(1)
  }

  if (content.endsWith('|')) {
    content = content.slice(0, -1)
  }

  return content.split('|').map((cell) => cell.trim())
}

function isDividerRow(line: string): boolean {
  const cells = splitTableRow(line)
  if (cells.length < 2) {
    return false
  }

  return cells.every((cell) => {
    const normalized = cell.replace(/\s+/g, '')
    return /^:?-{3,}:?$/.test(normalized)
  })
}

function isTableRow(line: string): boolean {
  const cells = splitTableRow(line)
  if (cells.length < 2) {
    return false
  }

  return cells.some((cell) => cell.length > 0)
}

function matchFence(line: string): { char: string; length: number } | null {
  if (line.length < 3) {
    return null
  }

  const fenceMatch = line.match(/^(`{3,}|~{3,})/)
  if (!fenceMatch) {
    return null
  }

  const marker = fenceMatch[0]
  return { char: marker[0], length: marker.length }
}
