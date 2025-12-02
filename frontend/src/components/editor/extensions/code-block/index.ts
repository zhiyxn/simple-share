import { CodeBlockLowlight } from '@tiptap/extension-code-block-lowlight'
import { createLowlight } from 'lowlight'

// 导入常用语言
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import css from 'highlight.js/lib/languages/css'
import html from 'highlight.js/lib/languages/xml'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'

const lowlight = createLowlight()

// 注册语言
lowlight.register('javascript', javascript)
lowlight.register('typescript', typescript)
lowlight.register('python', python)
lowlight.register('java', java)
lowlight.register('css', css)
lowlight.register('html', html)
lowlight.register('json', json)
lowlight.register('bash', bash)
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { mergeAttributes } from '@tiptap/core'
import { markRaw } from 'vue'
import type { ExtensionOptions } from '../../types'
import CodeBlockView from './CodeBlockView.vue'

// 图标组件
import { Document } from '@element-plus/icons-vue'

export default CodeBlockLowlight.extend<ExtensionOptions>({
  renderHTML({ node, HTMLAttributes }) {
    return [
      'pre',
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      [
        'code',
        {
          class: node.attrs.language ? `language-${node.attrs.language}` : null,
        },
        0,
      ],
    ]
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      language: {
        default: null,
        parseHTML: element => element.getAttribute('data-language'),
        renderHTML: attributes => {
          if (!attributes.language) {
            return {}
          }
          return {
            'data-language': attributes.language,
          }
        },
      },
      collapsed: {
        default: false,
        parseHTML: element => element.getAttribute('data-collapsed') === 'true',
        renderHTML: attributes => {
          if (!attributes.collapsed) {
            return {}
          }
          return {
            'data-collapsed': attributes.collapsed,
          }
        },
      },
    }
  },

  addOptions() {
    return {
      ...this.parent?.(),
      lowlight,
      languages: [
        { label: 'JavaScript', value: 'javascript' },
        { label: 'TypeScript', value: 'typescript' },
        { label: 'Python', value: 'python' },
        { label: 'Java', value: 'java' },
        { label: 'CSS', value: 'css' },
        { label: 'HTML', value: 'html' },
        { label: 'JSON', value: 'json' },
        { label: 'Bash', value: 'bash' },
        { label: 'Plain Text', value: '' },
      ],
      getCommandMenuItems() {
        return [{
          priority: 80,
          icon: markRaw(Document),
          title: '代码块',
          keywords: ['codeblock', 'daimakuai'],
          command: ({ editor, range }) => {
            editor.chain().focus().deleteRange(range).setCodeBlock().run()
          },
        }]
      },
      getToolboxItems({ editor }) {
        return {
          priority: 25,
          icon: markRaw(Document),
          title: '代码块',
          description: '插入代码块',
          action: () => {
            editor.chain().focus().setCodeBlock().run()
          },
        }
      },
    }
  },

  addNodeView() {
    return VueNodeViewRenderer(CodeBlockView)
  },
})
