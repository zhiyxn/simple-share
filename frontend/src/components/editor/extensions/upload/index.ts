import { Extension } from '@tiptap/core'
import { Plugin, PluginKey } from '@tiptap/pm/state'
import type { Editor } from '@tiptap/vue-3'
import {
  extractClipboardFiles,
  extractDataTransferFiles,
  handleFileEvent,
} from '../../utils/upload'

export const Upload = Extension.create({
  name: 'upload',

  addProseMirrorPlugins() {
    const { editor }: { editor: Editor } = this

    return [
      new Plugin({
        key: new PluginKey('upload'),
        props: {
          handlePaste: (view, event: ClipboardEvent) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false
            }

            if (!event.clipboardData) {
              return false
            }

            const files = extractClipboardFiles(event.clipboardData)

            if (!files.length) {
              return false
            }

            event.preventDefault()
            files.forEach((file) => {
              handleFileEvent({ editor, file })
            })
            return true
          },
          handleDrop: (view, event) => {
            if (view.props.editable && !view.props.editable(view.state)) {
              return false
            }

            if (!event.dataTransfer) {
              return false
            }

            const files = extractDataTransferFiles(event.dataTransfer)
            if (!files.length) {
              return false
            }

            event.preventDefault()
            files.forEach((file: File) => {
              handleFileEvent({ editor, file })
            })
            return true
          },
        },
      }),
    ]
  },
})

export default Upload
