import request from '@/api/request'
import type { Role, RoleQuery, RoleForm } from '@/types/role'

export const roleApi = {
  // 获取角色列表
  getRoles(params: RoleQuery) {
    return request.get<{
      items: Role[]
      total: number
      page: number
      pageSize: number
    }>('/system/admin/roles', { params })
  },

  // 获取角色详情
  getRoleDetail(id: string) {
    return request.get<Role>(`/system/admin/roles/${id}`)
  },

  // 创建角色
  createRole(data: RoleForm) {
    return request.post<Role>('/system/admin/roles', data)
  },

  // 更新角色
  updateRole(id: string, data: Partial<RoleForm>) {
    return request.put<Role>(`/system/admin/roles/${id}`, data)
  },

  // 删除角色
  deleteRole(id: string) {
    return request.delete(`/system/admin/roles/${id}`)
  },

  // 批量删除角色
  batchDeleteRoles(ids: string[]) {
    return request.post('/system/admin/roles/batch-delete', { ids })
  },

  // 获取角色权限
  getRolePermissions(roleId: string) {
    return request.get<string[]>(`/system/admin/roles/${roleId}/permissions`)
  },

  // 保存角色权限
  saveRolePermissions(roleId: string, permissionIds: string[]) {
    return request.put(`/system/admin/roles/${roleId}/permissions`, { permissionIds })
  },

  // 获取所有角色（用于下拉选择）
  getAllRoles() {
    return request.get<{
      items: Role[]
      total: number
      page: number
      pageSize: number
    }>('/system/admin/roles', { 
      params: { 
        page: 1, 
        pageSize: 1000 // 获取足够多的角色数据
      } 
    }).then(response => response.items || [])
  }
}
