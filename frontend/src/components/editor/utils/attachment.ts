import type { AttachmentLike, AttachmentAttr } from '../types'

export function getContents(attachments: AttachmentLike[]) {
  return attachments
    .map((attachment) => {
      if (typeof attachment === 'string') {
        return {
          type: 'image',
          attrs: {
            src: attachment,
          },
        }
      }

      if (attachment.url) {
        const { url, type, name } = attachment
        
        if (type?.startsWith('image/')) {
          return {
            type: 'image',
            attrs: {
              src: url,
              alt: name,
            },
          }
        }

        if (type?.startsWith('video/')) {
          return {
            type: 'video',
            attrs: {
              src: url,
            },
          }
        }

        if (type?.startsWith('audio/')) {
          return {
            type: 'audio',
            attrs: {
              src: url,
            },
          }
        }

        return {
          type: 'text',
          marks: [
            {
              type: 'link',
              attrs: {
                href: url,
              },
            },
          ],
          text: name || url,
        }
      }
    })
    .filter(Boolean)
}

export function getAttachmentUrl(attachment: AttachmentLike): AttachmentAttr {
  let url: string | undefined = undefined
  let name: string | undefined = undefined
  
  if (typeof attachment === 'string') {
    url = attachment
  } else if (attachment.url) {
    url = attachment.url
    name = attachment.name
  }

  return {
    url,
    name,
  }
}