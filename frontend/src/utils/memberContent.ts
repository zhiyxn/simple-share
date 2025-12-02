export interface MemberContentBlock {
  placeholderId: string
  html: string
}

interface MemberContentPayload {
  blocks: MemberContentBlock[]
}

const MEMBER_PLACEHOLDER_ATTR = 'data-member-placeholder'
const MEMBER_BLOCK_SELECTOR = 'section[data-type="member-content"]'

const isDomAvailable = typeof window !== 'undefined' && typeof document !== 'undefined'

const createContainer = () => {
  if (!isDomAvailable) {
    return null
  }
  return document.createElement('div')
}

const buildPlaceholder = (placeholderId: string) => {
  const placeholder = document.createElement('div')
  placeholder.setAttribute(MEMBER_PLACEHOLDER_ATTR, placeholderId)
  placeholder.setAttribute('data-member-locked', 'true')
  placeholder.className = 'member-content-locked-placeholder'
  placeholder.innerHTML =
    '<div class="member-content-locked-message"><span class="member-content-locked-message__tag">会员专属</span><span class="member-content-locked-message__text">会员查看所有内容</span><a class="member-content-locked-message__cta" href="/membership">立即开通会员</a></div>'
  return placeholder
}

const parseMemberPayload = (raw: string | null | undefined): MemberContentBlock[] => {
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw) as MemberContentPayload
    if (parsed && Array.isArray(parsed.blocks)) {
      return parsed.blocks.filter(
        (block): block is MemberContentBlock =>
          !!block && typeof block.placeholderId === 'string' && typeof block.html === 'string'
      )
    }
  } catch {
    // ignore malformed payload
  }
  return []
}

export function splitMemberContent(html: string) {
  if (!isDomAvailable) {
    return {
      content: html || '',
      memberContent: '',
      blocks: [] as MemberContentBlock[]
    }
  }

  const container = createContainer()
  if (!container) {
    return {
      content: html || '',
      memberContent: '',
      blocks: [] as MemberContentBlock[]
    }
  }

  container.innerHTML = html || ''
  const blocks: MemberContentBlock[] = []
  const nodes = Array.from(container.querySelectorAll<HTMLElement>(MEMBER_BLOCK_SELECTOR))

  nodes.forEach((node, index) => {
    const placeholderId = `member-block-${index}`
    blocks.push({
      placeholderId,
      html: node.outerHTML
    })

    const placeholder = buildPlaceholder(placeholderId)
    node.replaceWith(placeholder)
  })

  const memberContent = blocks.length ? JSON.stringify({ blocks }) : ''

  return {
    content: container.innerHTML,
    memberContent,
    blocks
  }
}

export function mergeMemberContent(content: string, memberContent?: string | null) {
  if (!isDomAvailable) {
    return content || ''
  }

  const blocks = parseMemberPayload(memberContent)
  if (blocks.length === 0) {
    return content || ''
  }

  const container = createContainer()
  if (!container) {
    return content || ''
  }

  container.innerHTML = content || ''

  blocks.forEach((block) => {
    const placeholder = container.querySelector<HTMLElement>(
      `[${MEMBER_PLACEHOLDER_ATTR}="${block.placeholderId}"]`
    )

    const temp = document.createElement('div')
    temp.innerHTML = block.html
    const node = temp.firstElementChild

    if (placeholder) {
      if (node) {
        placeholder.replaceWith(node)
      } else {
        placeholder.remove()
      }
    } else if (node) {
      container.appendChild(node)
    }
  })

  return container.innerHTML
}

export function stripMemberBlocks(html: string) {
  if (!isDomAvailable) {
    return html || ''
  }

  const container = createContainer()
  if (!container) {
    return html || ''
  }

  container.innerHTML = html || ''
  const nodes = Array.from(container.querySelectorAll<HTMLElement>(MEMBER_BLOCK_SELECTOR))
  nodes.forEach((node) => {
    const placeholder = buildPlaceholder(`member-block-${Math.random().toString(36).slice(2)}`)
    node.replaceWith(placeholder)
  })

  return container.innerHTML
}
