export interface OperationLog {
  id: number
  tenantId?: number
  traceId?: string
  title?: string
  businessType?: string
  className?: string
  methodName?: string
  requestMethod: string
  requestUri: string
  requestParams?: string
  requestBody?: string
  responseStatus?: number
  status: number
  errorMessage?: string
  operatorId?: number
  operatorName?: string
  operatorType?: string
  clientIp?: string
  userAgent?: string
  duration?: number
  requestId?: string
  createTime?: string
}

export interface OperationLogQuery {
  page?: number
  pageSize?: number
  requestUri?: string
  requestMethod?: string
  operatorName?: string
  status?: number
  businessType?: string
  startTime?: string
  endTime?: string
}
