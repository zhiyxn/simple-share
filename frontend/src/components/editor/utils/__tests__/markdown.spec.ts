import { describe, expect, it } from 'vitest'

import {
  detectContentFormat,
  htmlToMarkdown,
  isHtmlContent,
  isMarkdownContent,
  markdownToHtml,
} from '../markdown'

describe('markdown utils', () => {
  it('detects markdown structures', () => {
    expect(isMarkdownContent('# Heading')).toBe(true)
    expect(isMarkdownContent('Plain text without syntax')).toBe(false)
    expect(isMarkdownContent('**bold** inline')).toBe(true)
    expect(isMarkdownContent('1. ordered list item')).toBe(true)
    expect(isMarkdownContent('::: tip\ncontent\n:::')).toBe(true)
    expect(
      isMarkdownContent('# Title\n\n<div class="note">内嵌 HTML</div>\n\n- 列表项'),
    ).toBe(true)
    expect(isMarkdownContent('<p>Just HTML block</p>')).toBe(false)
    expect(
      isMarkdownContent(
        [
          '| 列一 | 列二 |',
          '| --- | --- |',
          '| A | B |',
        ].join('\n'),
      ),
    ).toBe(true)
    expect(isMarkdownContent('这是一个块级公式:\n\n$$E=mc^2$$')).toBe(true)
    expect(isMarkdownContent('行内公式比如 $a^2 + b^2 = c^2$ 应该被识别')).toBe(true)
  })

  it('detects html content', () => {
    expect(isHtmlContent('<p>Paragraph</p>')).toBe(true)
    expect(isHtmlContent('<div class="test">text</div>')).toBe(true)
    expect(isHtmlContent('No tags here')).toBe(false)
  })

  it('determines content format', () => {
    expect(detectContentFormat('# Title')).toBe('markdown')
    expect(detectContentFormat('<p>Title</p>')).toBe('html')
    expect(detectContentFormat('Simple text')).toBe('html')
  })

  it('converts markdown to html', () => {
    const html = markdownToHtml('# Heading')
    expect(html).toContain('<h1')
    expect(html).toContain('Heading')
  })

  it('renders extended markdown syntax', () => {
    const html = markdownToHtml(
      [
        '::: tip 提示',
        '这里是提示内容',
        ':::',
        '',
        '- [x] 已完成任务',
        '',
        '术语',
        ': 定义内容',
        '',
        '==高亮文本== 与 H~2~O 以及 E=mc^2[^1]',
        '',
        '[^1]: 能量等式',
      ].join('\n'),
    )

    expect(html).toContain('md-admonition md-admonition-tip')
    expect(html).toContain('task-list-item-checkbox')
    expect(html).toContain('<dl>')
    expect(html).toContain('<mark>')
    expect(html).toContain('footnote-ref')
    expect(html).toContain('section class="footnotes"')
  })

  it('handles comprehensive markdown sample', () => {
    const sample = `# Markdown 全面测试文档

## 标题层级测试

# 一级标题 (H1)
## 二级标题 (H2)
### 三级标题 (H3)
#### 四级标题 (H4)
##### 五级标题 (H5)
###### 六级标题 (H6)

## 文本格式测试

**这是粗体文本**  
*这是斜体文本*  
***这是粗斜体文本***  
~~这是删除线文本~~  
==这是高亮文本== (如果支持)  
\`这是行内代码\`

## 列表测试

### 无序列表
- 列表项一
- 列表项二
  - 嵌套列表项一
  - 嵌套列表项二
- 列表项三

### 有序列表
1. 第一项
2. 第二项
   1. 嵌套有序一
   2. 嵌套有序二
3. 第三项

### 任务列表
- [ ] 未完成任务
- [x] 已完成任务
- [ ] 另一个未完成任务

## 链接和图片测试

[普通链接](https://www.example.com)

![替代文本](https://via.placeholder.com/150 "图片标题")

## 代码块测试

\`\`\`javascript
function greet(name) {
  return \`Hello, \${name}!\`;
}
\`\`\`

## 表格测试

| 姓名 | 年龄 | 城市 |
|------|------|------|
| 张三 | 25   | 北京 |

### 定义列表
术语一
: 定义一

术语二
: 定义二

### 上标和下标
H~2~O 是水的化学式。
E = mc^2^ 是质能方程。

## 数学公式测试
勾股定理：$a^2 + b^2 = c^2$

## HTML 混合测试

<div>这是 <span>混合</span> 内容。</div>

## 转义字符测试

\\*这不是斜体\\*

## 表情符号测试
:smile: :rocket:`

    const html = markdownToHtml(sample)

    expect(html).toContain('<h1>Markdown 全面测试文档</h1>')
    expect(html).toContain('<ul>')
    expect(html).toContain('task-list-item-checkbox')
    expect(html).toContain('<code class="language-javascript">')
    expect(html).toContain('<table>')
    expect(html).toContain('<dl>')
    expect(html).toContain('<mark>')
    expect(html).toContain('<sup')
    expect(html).toContain('😄')
  })

  it('keeps math syntax round-trip', () => {
    const sample = [
      '块级公式',
      '$$',
      '\\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi}',
      '$$',
      '',
      '行内公式 $a^2 + b^2 = c^2$ 在文本中出现',
    ].join('\n')

    const html = markdownToHtml(sample)
    expect(html).toContain('md-math-block')
    expect(html).toContain('md-math-inline')

    const roundTrip = htmlToMarkdown(html)
    expect(roundTrip).toContain('$$')
    expect(roundTrip).toContain('\\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi}')
    expect(roundTrip).toContain('$a^2 + b^2 = c^2$')
  })

  it('keeps table structure round-trip', () => {
    const sample = [
      '| 姓名 | 年龄 | 城市 |',
      '| --- | --- | --- |',
      '| 张三 | 25   | 北京 |',
      '| 李四 | 30   | 上海 |',
      '| 王五 | 28   | 广州 |',
    ].join('\n')

    const html = markdownToHtml(sample)
    const roundTrip = htmlToMarkdown(html).trim()

    expect(html).toContain('<table')
    expect(roundTrip).toContain('| 姓名 | 年龄 | 城市 |')
    expect(roundTrip).toContain('| 张三 | 25 | 北京 |')
    expect(roundTrip.split('\n').length).toBeGreaterThanOrEqual(5)
    expect(roundTrip).not.toMatch(/\|\s*\|\s*\|\s*\|/)
  })

  it('handles tables with blank separator lines', () => {
    const sample = [
      '| 姓名 | 年龄 | 城市 |',
      '',
      '| --- | --- | --- |',
      '',
      '| 张三 | 25   | 北京 |',
      '',
      '| 李四 | 30   | 上海 |',
      '',
      '| 王五 | 28   | 广州 |',
    ].join('\n')

    const html = markdownToHtml(sample)
    const roundTrip = htmlToMarkdown(html).trim()

    expect(html).toContain('<table')
    expect(html).toContain('<td>广州</td>')
    expect(roundTrip.split('\n').length).toBeGreaterThanOrEqual(5)
    expect(roundTrip).toContain('| 张三 | 25 | 北京 |')
  })

  it('converts html to markdown with gfm support', () => {
    const markdown = htmlToMarkdown('<ul><li>item</li></ul>')
    expect(markdown.trim()).toMatch(/^- +item$/)

    const tableMarkdown = htmlToMarkdown(
      '<table><thead><tr><th>A</th></tr></thead><tbody><tr><td>B</td></tr></tbody></table>',
    )
    expect(tableMarkdown).toContain('| A |')
    expect(tableMarkdown).toContain('| B |')
  })
})
