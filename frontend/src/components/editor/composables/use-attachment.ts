import type { AttachmentLike, AttachmentResult } from '../types'

interface UseAttachmentSelectReturn {
  onAttachmentSelect: (attachments: AttachmentLike[]) => void
  attachmentResult: AttachmentResult
}

export function useAttachmentSelect(): UseAttachmentSelectReturn {
  const attachmentResult = {
    updateAttachment: (attachments: AttachmentLike[]) => {
      return attachments
    },
  }
  
  const onAttachmentSelect = (attachmentLikes: AttachmentLike[]) => {
    attachmentResult.updateAttachment(attachmentLikes)
  }

  return {
    onAttachmentSelect,
    attachmentResult,
  }
}