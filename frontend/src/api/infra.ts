import request from '@/api/request'

// 文件管理API
export const fileApi = {
  // 获取文件列表
  getFileList(params: any) {
    return request({
      url: '/infra/file/list',
      method: 'get',
      params
    })
  },

  // 获取文件详情
  getFileInfo(id: number) {
    return request({
      url: `/infra/file/${id}`,
      method: 'get'
    })
  },

  // 上传文件
  uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/infra/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除文件
  deleteFile(ids: number[]) {
    return request({
      url: `/infra/file/${ids.join(',')}`,
      method: 'delete'
    })
  },

  // 根据路径删除文件
  deleteFileByPath(path: string) {
    return request({
      url: '/infra/file/path',
      method: 'delete',
      params: { path }
    })
  },

  // 获取文件统计信息
  getFileStatistics() {
    return request({
      url: '/infra/file/statistics',
      method: 'get'
    })
  },

  // 导出文件列表
  exportFileList(params: any) {
    return request({
      url: '/infra/file/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  }
}

// 文件配置API
export const fileConfigApi = {
  // 获取配置列表
  getConfigList(params: any) {
    return request({
      url: '/infra/file-config/list',
      method: 'get',
      params
    })
  },

  // 获取配置详情
  getConfigInfo(id: number) {
    return request({
      url: `/infra/file-config/${id}`,
      method: 'get'
    })
  },

  // 新增配置
  addConfig(data: any) {
    return request({
      url: '/infra/file-config',
      method: 'post',
      data
    })
  },

  // 修改配置
  updateConfig(data: any) {
    return request({
      url: '/infra/file-config',
      method: 'put',
      data
    })
  },

  // 删除配置
  deleteConfig(ids: number[]) {
    return request({
      url: `/infra/file-config/${ids.join(',')}`,
      method: 'delete'
    })
  },

  // 获取主配置
  getMasterConfig() {
    return request({
      url: '/infra/file-config/master',
      method: 'get'
    })
  },

  // 设置主配置
  setMasterConfig(id: number) {
    return request({
      url: `/infra/file-config/${id}/master`,
      method: 'put'
    })
  },

  // 测试配置
  testConfig(id: number) {
    return request({
      url: `/infra/file-config/${id}/test`,
      method: 'post'
    })
  },

  // 导出配置列表
  exportConfigList(params: any) {
    return request({
      url: '/infra/file-config/export',
      method: 'post',
      data: params,
      responseType: 'blob'
    })
  }
}
