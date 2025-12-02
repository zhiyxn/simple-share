import request from '@/api/request'
import type { OperationLog, OperationLogQuery } from '@/types/operation-log'
import type { PaginatedResponse } from '@/types'

const prefix = '/system/admin/operation-logs'

export function getOperationLogs(params: OperationLogQuery) {
  return request<PaginatedResponse<OperationLog>>({
    url: prefix,
    method: 'get',
    params
  })
}

export function cleanOperationLogs() {
  return request({
    url: `${prefix}/clean`,
    method: 'delete'
  })
}

export default {
  getOperationLogs,
  cleanOperationLogs
}
