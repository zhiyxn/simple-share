import { describe, expect, it } from 'vitest'

import {
  decodeSecureResourceConfig,
  encodeSecureResourceConfig,
  ensureSecureResourceConsistency,
} from '@/utils/secureResource'

describe('secureResource utils', () => {
  it('ensures secure resource nodes keep url and secret values', () => {
    const encodedConfig = encodeSecureResourceConfig({
      title: '重要资源',
      type: '百度网盘',
      url: '',
      secret: '',
      notice: '点击按钮即可查看和复制关键信息。',
    })

    const html = `
      <details
        data-type="secure-resource"
        data-title="重要资源"
        data-resource-type="百度网盘"
        data-config="${encodedConfig}"
      >
        <summary class="secure-resource-block__summary">
          <span class="secure-resource-block__icon">🔐</span>
          <span class="secure-resource-block__summary-text">
            <span class="secure-resource-block__title">重要资源</span>
            <span class="secure-resource-block__type">百度网盘</span>
          </span>
          <span class="secure-resource-block__toggle">点击查看</span>
        </summary>
        <div class="secure-resource-block__body">
          <div class="secure-resource-block__field">
            <span class="secure-resource-block__label">访问链接</span>
            <div class="secure-resource-block__value">
              <a
                class="secure-resource-block__link"
                href="https://pan.baidu.com/s/example"
                target="_blank"
                rel="noopener noreferrer"
              >
                https://pan.baidu.com/s/example
              </a>
              <button type="button" class="secure-resource-block__copy" data-copy="url">复制链接</button>
            </div>
          </div>
          <div class="secure-resource-block__field">
            <span class="secure-resource-block__label">提取码/ 密码</span>
            <div class="secure-resource-block__value">
              <span class="secure-resource-block__secret">abcd</span>
              <button type="button" class="secure-resource-block__copy" data-copy="secret">复制密码</button>
            </div>
          </div>
          <div class="secure-resource-block__notice">点击按钮即可查看和复制关键信息。</div>
        </div>
      </details>
    `

    const consistentHtml = ensureSecureResourceConsistency(html)
    const container = document.createElement('div')
    container.innerHTML = consistentHtml
    const details = container.querySelector('details[data-type="secure-resource"]') as HTMLElement

    expect(details).not.toBeNull()
    expect(details.dataset.url).toBe('https://pan.baidu.com/s/example')
    expect(details.dataset.secret).toBe('abcd')

    const decoded = decodeSecureResourceConfig(details.getAttribute('data-config'))
    expect(decoded?.url).toBe('https://pan.baidu.com/s/example')
    expect(decoded?.secret).toBe('abcd')

    const link = details.querySelector<HTMLAnchorElement>('.secure-resource-block__link')
    expect(link?.getAttribute('href')).toBe('https://pan.baidu.com/s/example')
    expect(link?.textContent?.trim()).toBe('https://pan.baidu.com/s/example')

    const secretNode = details.querySelector('.secure-resource-block__secret')
    expect(secretNode?.textContent?.trim()).toBe('abcd')
  })
})
