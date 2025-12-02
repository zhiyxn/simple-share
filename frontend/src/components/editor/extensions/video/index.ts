import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import type { AttachmentLike } from '../../types'
import VideoView from './VideoView.vue'

interface UiVideoOptions {
  uploadVideo?: (file: File) => Promise<AttachmentLike>
}

const Video = Node.create<UiVideoOptions>({
  name: 'video',

  addOptions() {
    return {
      uploadVideo: undefined,
      getCommandMenuItems() {
        return {
          priority: 100,
          icon: 'video',
          title: '视频',
          keywords: ['video', 'shipin', 'mp4'],
          command: ({ editor, range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setVideo({ src: '' })
              .run()
          },
        }
      },
      getToolboxItems({ editor }) {
        return {
          priority: 20,
          icon: 'video',
          title: '视频',
          description: '插入视频文件或视频链接',
          action: () => {
            editor.chain().focus().setVideo({ src: '' }).run()
          },
        }
      },
    }
  },

  group: 'block',

  draggable: true,

  addAttributes() {
    return {
      src: {
        default: null,
        parseHTML: element => element.getAttribute('src'),
        renderHTML: attributes => {
          if (!attributes.src) {
            return {}
          }
          return { src: attributes.src }
        },
      },
      controls: {
        default: true,
        parseHTML: element => element.hasAttribute('controls'),
        renderHTML: attributes => {
          if (!attributes.controls) {
            return {}
          }
          return { controls: '' }
        },
      },
      autoplay: {
        default: false,
        parseHTML: element => element.hasAttribute('autoplay'),
        renderHTML: attributes => {
          if (!attributes.autoplay) {
            return {}
          }
          return { autoplay: '' }
        },
      },
      loop: {
        default: false,
        parseHTML: element => element.hasAttribute('loop'),
        renderHTML: attributes => {
          if (!attributes.loop) {
            return {}
          }
          return { loop: '' }
        },
      },
      width: {
        default: '100%',
        parseHTML: element => element.getAttribute('width') || element.style.width,
        renderHTML: attributes => {
          if (!attributes.width) {
            return {}
          }
          return { width: attributes.width }
        },
      },
      height: {
        default: 'auto',
        parseHTML: element => element.getAttribute('height') || element.style.height,
        renderHTML: attributes => {
          if (!attributes.height) {
            return {}
          }
          return { height: attributes.height }
        },
      },
      file: {
        default: null,
        renderHTML() {
          return {}
        },
        parseHTML() {
          return null
        },
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'video',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['video', HTMLAttributes]
  },

  addNodeView() {
    return VueNodeViewRenderer(VideoView)
  },

  addCommands() {
    return {
      setVideo: options => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: options,
        })
      },
    }
  },
})

export default Video