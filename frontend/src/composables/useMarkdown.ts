import hljsCore from 'highlight.js/lib/core'
import hljsJavascript from 'highlight.js/lib/languages/javascript'
import hljsTypescript from 'highlight.js/lib/languages/typescript'
import hljsPython from 'highlight.js/lib/languages/python'
import hljsJava from 'highlight.js/lib/languages/java'
import hljsCss from 'highlight.js/lib/languages/css'
import hljsXml from 'highlight.js/lib/languages/xml'
import hljsJson from 'highlight.js/lib/languages/json'
import hljsBash from 'highlight.js/lib/languages/bash'
import mermaid from 'mermaid'
// KaTeX auto-render 扫描 DOM 渲染数学公式
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore - 类型定义在 katex 包内
import renderMathInElement from 'katex/contrib/auto-render'
import 'katex/dist/katex.min.css'
import { createMarkdownParser } from '@/components/editor/utils/markdown'

let hljsRegistered = false
function ensureHighlightLanguages() {
  if (hljsRegistered) return
  hljsCore.registerLanguage('javascript', hljsJavascript)
  hljsCore.registerLanguage('typescript', hljsTypescript)
  hljsCore.registerLanguage('python', hljsPython)
  hljsCore.registerLanguage('java', hljsJava)
  hljsCore.registerLanguage('css', hljsCss)
  hljsCore.registerLanguage('html', hljsXml)
  hljsCore.registerLanguage('json', hljsJson)
  hljsCore.registerLanguage('bash', hljsBash)
  hljsRegistered = true
}

export interface EnhanceOptions {
  protectCopy?: boolean
}

export function createMarkdownRenderer() {
  ensureHighlightLanguages()

  const mdInstance = createMarkdownParser()

  const mergeRelAttributes = (value?: string | null) => {
    const relParts = new Set((value ?? '').split(/\s+/).filter(Boolean))
    relParts.add('noopener')
    relParts.add('noreferrer')
    return Array.from(relParts).join(' ')
  }

  const shouldForceNewTab = (href?: string | null) => {
    if (!href) return false
    const normalized = href.trim()
    if (!normalized) return false
    return !/^javascript:/i.test(normalized)
  }

  const escapeHtml = (value: string) =>
    value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')

  const highlightCode = (code: string, language?: string) => {
    const lang = language?.toLowerCase() ?? ''
    let highlighted = ''

    if (code) {
      if (lang && hljsCore.getLanguage(lang)) {
        try {
          highlighted = hljsCore.highlight(code, { language: lang }).value
        } catch {
          highlighted = escapeHtml(code)
        }
      } else {
        try {
          highlighted = hljsCore.highlightAuto(code).value
        } catch {
          highlighted = escapeHtml(code)
        }
      }
    }

    if (!highlighted) {
      highlighted = escapeHtml(code)
    }

    const label = lang ? lang.toUpperCase() : 'TEXT'
    const languageClass = lang ? `language-${lang}` : ''

    return { highlighted, label, languageClass }
  }

  const renderCodeBlock = (code: string, language?: string) => {
    const lang = language?.trim() ?? ''
    if (lang === 'mermaid') {
      return `<pre class="md-mermaid"><code class="language-mermaid">${escapeHtml(code)}</code></pre>`
    }

    const { highlighted, label, languageClass } = highlightCode(code, lang)
    const codeClasses = ['hljs']
    if (languageClass) {
      codeClasses.push(languageClass)
    }

    return `
<div class="md-code-block" data-lang="${label}">
  <div class="md-code-toolbar">
    <span class="md-code-lang">${label}</span>
    <button class="md-code-copy" type="button" aria-label="复制代码">复制</button>
  </div>
  <pre class="md-code-pre">
    <code class="${codeClasses.join(' ')}">${highlighted}</code>
  </pre>
</div>`
  }

  mdInstance.renderer.rules.fence = (tokens, idx) => {
    const token = tokens[idx]
    const info = token.info ? token.info.trim() : ''
    const lang = info.split(/\s+/)[0]
    return renderCodeBlock(token.content, lang)
  }

  mdInstance.renderer.rules.code_block = (tokens, idx) => {
    const token = tokens[idx]
    return renderCodeBlock(token.content, '')
  }

  mdInstance.renderer.rules.table_open = () => '<div class="md-table-wrapper"><table>'
  mdInstance.renderer.rules.table_close = () => '</table></div>'

  const defaultImageRenderer =
    mdInstance.renderer.rules.image ??
    ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

  mdInstance.renderer.rules.image = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    const altText = token.content ? mdInstance.utils.escapeHtml(token.content) : ''
    if (altText) {
      token.attrSet('alt', altText)
    } else if (!token.attrGet('alt')) {
      token.attrSet('alt', '')
    }
    if (!token.attrGet('loading')) {
      token.attrSet('loading', 'lazy')
    }
    return defaultImageRenderer(tokens, idx, options, env, self)
  }

  const defaultLinkOpen =
    mdInstance.renderer.rules.link_open ??
    ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

  mdInstance.renderer.rules.link_open = (tokens, idx, options, env, self) => {
    const token = tokens[idx]
    const href = token.attrGet('href') ?? ''
    if (shouldForceNewTab(href)) {
      token.attrSet('target', '_blank')
      token.attrSet('rel', mergeRelAttributes(token.attrGet('rel')))
    }
    return defaultLinkOpen(tokens, idx, options, env, self)
  }

  const renderEmbeddedMarkdownBlocks = (container: HTMLElement) => {
    const blocks = Array.from(
      container.querySelectorAll<HTMLElement>('div[data-type="markdown-block"]')
    )

    for (const block of blocks) {
      if (block.dataset.mdProcessed === 'true') {
        continue
      }

      const source = block.dataset.content ?? block.getAttribute('data-content') ?? ''
      try {
        block.innerHTML = mdInstance.render(source)
        block.dataset.mdProcessed = 'true'
        block.classList.add('markdown-block-rendered', 'markdown-surface')
      } catch (error) {
        console.warn('Markdown block render failed', error)
        block.dataset.mdProcessed = 'true'
      }
    }
  }

  const enhance = async (container: HTMLElement, options?: EnhanceOptions) => {
    ensureHighlightLanguages()
    if (!container) return

    renderEmbeddedMarkdownBlocks(container)

    // 1) Mermaid：将 <pre><code class="language-mermaid"> 替换为 <div class="mermaid"> 并渲染
    const mermaidBlocks = Array.from(container.querySelectorAll('pre > code.language-mermaid'))
    if (mermaidBlocks.length) {
      mermaid.initialize({ startOnLoad: false, securityLevel: 'loose' })
      for (const codeEl of mermaidBlocks) {
        const pre = codeEl.parentElement as HTMLElement
        const graphDef = codeEl.textContent || ''
        const div = document.createElement('div')
        div.className = 'mermaid'
        div.textContent = graphDef
        pre.replaceWith(div)
      }
      try {
        await mermaid.run({ nodes: [container] as unknown as NodeListOf<Element> })
      } catch (error) {
        console.warn('Mermaid render failed', error)
      }
    }

    // 2) KaTeX：扫描并渲染数学公式，忽略代码块/脚本
    try {
      renderMathInElement(container, {
        delimiters: [
          { left: '$$', right: '$$', display: true },
          { left: '$', right: '$', display: false },
          { left: '\\(', right: '\\)', display: false },
          { left: '\\[', right: '\\]', display: true },
        ],
        ignoredTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code'] as never,
      })
    } catch (error) {
      console.warn('KaTeX render failed', error)
    }

    // 3) 高亮兜底：对未带 .hljs 的代码块进行高亮
    container.querySelectorAll('pre > code:not(.hljs)').forEach((code) => {
      try {
        hljsCore.highlightElement(code as HTMLElement)
      } catch {}
    })

    // 4) 为已有 pre>code（无工具栏）补充复制工具栏
    container.querySelectorAll('pre').forEach((pre) => {
      if (pre.closest('.md-code-block')) {
        return
      }
      const code = pre.querySelector('code')
      const parent = pre.parentElement
      if (!code || !parent) return
      const langClass = Array.from(code.classList).find((cls) => cls.startsWith('language-'))
      const label = langClass ? langClass.replace('language-', '').toUpperCase() : 'TEXT'
      const wrapper = document.createElement('div')
      wrapper.className = 'md-code-block'
      wrapper.setAttribute('data-lang', label)
      const toolbar = document.createElement('div')
      toolbar.className = 'md-code-toolbar'
      toolbar.innerHTML = `<span class="md-code-lang">${label}</span><button class="md-code-copy" type="button" aria-label="复制代码">复制</button>`
      parent.insertBefore(wrapper, pre)
      wrapper.appendChild(toolbar)
      pre.classList.add('md-code-pre')
      wrapper.appendChild(pre)
    })

    // 5) 表格外包裹容器（若缺失）
    container.querySelectorAll('table').forEach((table) => {
      if (table.closest('.md-table-wrapper')) return
      const wrapper = document.createElement('div')
      wrapper.className = 'md-table-wrapper'
      table.parentElement?.insertBefore(wrapper, table)
      wrapper.appendChild(table)
    })

    // 6) 任务清单列表样式
    container.querySelectorAll('li > input[type="checkbox"]').forEach((input) => {
      const parent = input.closest('ul,ol')
      parent?.classList.add('md-task-list')
    })

    // 7) 链接统一强制在新标签页打开
    enforceLinksOpenNewTab(container)

    // 8) 代码复制事件代理
    bindCopy(container, options?.protectCopy === true)
  }

  const enforceLinksOpenNewTab = (container: HTMLElement) => {
    const anchors = Array.from(container.querySelectorAll<HTMLAnchorElement>('a[href]'))
    anchors.forEach((anchor) => {
      if (!shouldForceNewTab(anchor.getAttribute('href'))) return
      anchor.setAttribute('target', '_blank')
      anchor.setAttribute('rel', mergeRelAttributes(anchor.getAttribute('rel')))
    })
  }

  const bindCopy = (container: HTMLElement, protectCopy: boolean) => {
    container.addEventListener('click', async (event) => {
      const target = event.target as HTMLElement
      if (!target || !target.classList.contains('md-code-copy')) return
      if (protectCopy) {
        const blocked = new CustomEvent('md-copy-blocked', { bubbles: true })
        target.dispatchEvent(blocked)
        return
      }
      const wrapper = target.closest('.md-code-block')
      const pre = wrapper?.querySelector('pre')
      const code = pre?.querySelector('code') ?? wrapper?.querySelector('code')
      if (!code) return
      const text = code.textContent ?? ''
      try {
        await navigator.clipboard.writeText(text)
        const success = new CustomEvent('md-copy-success', { bubbles: true })
        target.dispatchEvent(success)
      } catch {
        try {
          const selection = window.getSelection?.()
          const range = document.createRange()
          if (code && selection) {
            range.selectNodeContents(code)
            selection.removeAllRanges()
            selection.addRange(range)
            document.execCommand?.('copy')
            selection.removeAllRanges()
            const success = new CustomEvent('md-copy-success', { bubbles: true })
            target.dispatchEvent(success)
          } else {
            throw new Error('no selection')
          }
        } catch {
          const failed = new CustomEvent('md-copy-failed', { bubbles: true })
          target.dispatchEvent(failed)
        }
      }
    })
  }

  return {
    parse: (src: string) => mdInstance.render(src),
    enhance,
    ensureHighlightLanguages,
  }
}
