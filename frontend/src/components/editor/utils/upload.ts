import type { Editor } from '@tiptap/vue-3'
import type { FileProps, AttachmentLike, UploadResponse } from '../types'
import { UiExtensionImage, UiExtensionVideo, UiExtensionAudio } from '../extensions'

/**
 * Handles file events, determining if the file is an image and triggering the appropriate upload process.
 */
export const handleFileEvent = ({ file, editor }: FileProps) => {
  if (!file) {
    return false
  }

  if (file.type.startsWith('image/')) {
    uploadImage({ file, editor })
    return true
  }

  if (file.type.startsWith('video/')) {
    uploadVideo({ file, editor })
    return true
  }

  if (file.type.startsWith('audio/')) {
    uploadAudio({ file, editor })
    return true
  }

  return true
}

/**
 * Uploads an image file and inserts it into the editor.
 */
export const uploadImage = ({ file, editor }: FileProps) => {
  const { view } = editor
  const node = view.state.schema.nodes[UiExtensionImage.name].create({
    file: file,
  })
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node))
}

/**
 * Uploads a video file and inserts it into the editor.
 */
export const uploadVideo = ({ file, editor }: FileProps) => {
  const { view } = editor
  const node = view.state.schema.nodes[UiExtensionVideo.name].create({
    file: file,
  })
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node))
}

/**
 * Uploads an audio file and inserts it into the editor.
 */
export const uploadAudio = ({ file, editor }: FileProps) => {
  const { view } = editor
  const node = view.state.schema.nodes[UiExtensionAudio.name].create({
    file: file,
  })
  editor.view.dispatch(editor.view.state.tr.replaceSelectionWith(node))
}

/**
 * Uploads a file with progress monitoring, cancellation support, and callbacks for completion and errors.
 */
export const uploadFile = async (
  file: File,
  upload: (file: File) => Promise<AttachmentLike>,
  uploadResponse: UploadResponse
) => {
  const { signal } = uploadResponse.controller

  try {
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      const progress = Math.min(Math.random() * 100, 90)
      uploadResponse.onUploadProgress(progress)
    }, 100)

    const attachment = await upload(file)
    
    clearInterval(progressInterval)
    uploadResponse.onUploadProgress(100)
    uploadResponse.onFinish(attachment)
  } catch (error) {
    uploadResponse.onError(error as Error)
  }
}

/**
 * Converts a file to a Base64 string.
 */
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = function () {
      resolve(reader.result as string)
    }
    reader.onerror = function (error) {
      reject(error)
    }
    reader.readAsDataURL(file)
  })
}

export function extractDataTransferFiles(dataTransfer?: DataTransfer | null): File[] {
  if (!dataTransfer) {
    return []
  }

  const files: File[] = []
  const seen = new Set<string>()

  const pushFile = (file: File | null) => {
    if (!file) {
      return
    }

    const signature = `${file.name}|${file.size}|${file.type}|${file.lastModified ?? 0}`
    if (seen.has(signature)) {
      return
    }

    seen.add(signature)
    files.push(file)
  }

  const fileList = dataTransfer.files
  if (fileList && fileList.length) {
    for (let index = 0; index < fileList.length; index += 1) {
      pushFile(fileList.item(index))
    }
  }

  const itemList = dataTransfer.items
  if (itemList && itemList.length) {
    Array.from(itemList).forEach((item) => {
      if (item.kind === 'file') {
        pushFile(item.getAsFile())
      }
    })
  }

  return files
}

const DATA_URL_PATTERN = /data:image\/[a-zA-Z0-9.+-]+;base64,[a-zA-Z0-9+/=_-]+/g

const createFileSignature = (file?: File | null) => {
  if (!file) {
    return ''
  }
  return [file.name, file.size, file.type, file.lastModified ?? 0].join('|')
}

const dedupeFiles = (files: File[]): File[] => {
  const uniqueMap = new Map<string, File>()
  files.forEach((file) => {
    const signature = createFileSignature(file)
    if (signature && !uniqueMap.has(signature)) {
      uniqueMap.set(signature, file)
    }
  })
  return Array.from(uniqueMap.values())
}

const createFileName = (mime: string, index: number) => {
  const [, subtype = 'octet-stream'] = mime.split('/')
  const sanitizedSubtype = subtype.replace(/[^a-zA-Z0-9.+-]/g, '') || 'bin'
  return `clipboard-${Date.now()}-${index}.${sanitizedSubtype}`
}

export function createFileFromDataUrl(dataUrl: string, index = 0): File | null {
  if (!dataUrl || !dataUrl.startsWith('data:')) {
    return null
  }

  const matches = dataUrl.match(/^data:([^;]+);base64,(.+)$/)
  if (!matches) {
    return null
  }

  const [, mime, base64Data] = matches

  try {
    const binaryString = atob(base64Data)
    const length = binaryString.length
    const bytes = new Uint8Array(length)
    for (let i = 0; i < length; i += 1) {
      bytes[i] = binaryString.charCodeAt(i)
    }

    const fileName = createFileName(mime, index)

    try {
      return new File([bytes], fileName, { type: mime })
    } catch (error) {
      const blob = new Blob([bytes], { type: mime }) as File
      ;(blob as any).name = fileName
      return blob
    }
  } catch (error) {
    console.warn('Failed to convert data URL to file', error)
    return null
  }
}

export function extractDataUrlFilesFromString(input?: string | null): File[] {
  if (!input) {
    return []
  }

  const matches = input.match(DATA_URL_PATTERN)
  if (!matches) {
    return []
  }

  return matches
    .map((dataUrl, index) => createFileFromDataUrl(dataUrl, index))
    .filter((file): file is File => Boolean(file))
}

export function extractDataUrlFilesFromHtml(html?: string | null): File[] {
  if (!html) {
    return []
  }

  if (typeof window === 'undefined' || typeof DOMParser === 'undefined') {
    return []
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')
    const images = Array.from(doc.querySelectorAll('img'))

    const dataUrls = images
      .map((img) => img.getAttribute('src')?.trim())
      .filter((src): src is string => Boolean(src && src.startsWith('data:image/')))

    if (!dataUrls.length) {
      return []
    }

    return dataUrls
      .map((dataUrl, index) => createFileFromDataUrl(dataUrl, index))
      .filter((file): file is File => Boolean(file))
  } catch (error) {
    console.warn('Failed to parse clipboard HTML for images', error)
    return []
  }
}

const safeGetData = (dataTransfer: DataTransfer, format: string) => {
  try {
    return dataTransfer.getData(format)
  } catch (error) {
    console.warn('Failed to read clipboard data', error)
    return ''
  }
}

export function extractClipboardFiles(dataTransfer?: DataTransfer | null): File[] {
  const directFiles = extractDataTransferFiles(dataTransfer)
  if (directFiles.length || !dataTransfer) {
    return dedupeFiles(directFiles)
  }

  const uniqueFiles = new Map<string, File>()

  const addFiles = (incoming: File[]) => {
    incoming.forEach((file) => {
      const key = `${file.name}|${file.size}|${file.type}`
      if (!uniqueFiles.has(key)) {
        uniqueFiles.set(key, file)
      }
    })
  }

  const html = safeGetData(dataTransfer, 'text/html')
  if (html) {
    addFiles(extractDataUrlFilesFromHtml(html))
  }

  if (!uniqueFiles.size) {
    const plain = safeGetData(dataTransfer, 'text/plain')
    if (plain) {
      addFiles(extractDataUrlFilesFromString(plain))
    }
  }

  return Array.from(uniqueFiles.values())
}

export function containsFileClipboardIdentifier(
  types: readonly string[],
  dataTransfer?: DataTransfer | null
) {
  const fileTypes = ['files', 'application/x-moz-file', 'public.file-url']
  if (types.some((type) => fileTypes.includes(type.toLowerCase()))) {
    return true
  }

  return extractClipboardFiles(dataTransfer).length > 0
}
