// 角色实体
export interface Role {
  id: string
  name: string
  key: string
  sort: number
  status: '0' | '1' // 0: 启用, 1: 禁用
  remark?: string
  createTime: string
  updateTime: string
}

// 角色查询参数
export interface RoleQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: '0' | '1'
}

// 角色表单数据
export interface RoleForm {
  id?: string
  name: string
  key: string
  sort: number
  status: '0' | '1'
  remark?: string
}

// 权限实体
export interface Permission {
  id: string
  name: string
  key: string
  type: 'menu' | 'button' | 'api'
  parentId?: string
  children?: Permission[]
}

// 角色权限关联
export interface RolePermission {
  roleId: string
  permissionId: string
}