import request from '@/api/request'
import type { AiTechCardClickPayload, AiTechCardClickStats } from '@/types/aitech'

const PUBLIC_BASE = '/aitech/cards'
const ADMIN_BASE = '/system/admin/aitech/clicks'

export const recordAiTechCardClick = (payload: AiTechCardClickPayload) => {
  return request.post(`${PUBLIC_BASE}/click`, payload)
}

export const getAiTechCardStats = () => {
  return request.get<AiTechCardClickStats[]>(ADMIN_BASE)
}

export const getTopAiTechCards = (limit = 6) => {
  return request.get<AiTechCardClickStats[]>(`${ADMIN_BASE}/top`, { limit })
}

export default {
  recordAiTechCardClick,
  getAiTechCardStats,
  getTopAiTechCards
}
