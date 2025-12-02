import { Node } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import type { AttachmentLike } from '../../types'
import AudioView from './AudioView.vue'

interface UiAudioOptions {
  uploadAudio?: (file: File) => Promise<AttachmentLike>
}

const Audio = Node.create<UiAudioOptions>({
  name: 'audio',

  addOptions() {
    return {
      uploadAudio: undefined,
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
        tag: 'audio',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['audio', HTMLAttributes]
  },

  addNodeView() {
    return VueNodeViewRenderer(AudioView)
  },

  addCommands() {
    return {
      setAudio: options => ({ commands }) => {
        return commands.insertContent({
          type: this.name,
          attrs: options,
        })
      },
    }
  },
})

export default Audio