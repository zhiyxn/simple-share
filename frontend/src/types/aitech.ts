export interface AiTechCardClickPayload {
  cardKey: string
  cardTitle: string
  cardPath: string
  cardCategory?: string
}

export interface AiTechCardClickStats {
  cardKey: string
  cardTitle: string
  cardPath: string
  cardCategory?: string
  clickCount: number
  lastClickTime?: string
  createTime?: string
  updateTime?: string
}
