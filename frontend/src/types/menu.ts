// 菜单类型
export interface Menu {
  menuId: number
  menuName: string
  parentId: number
  parentName?: string
  orderNum: number
  path?: string
  component?: string
  query?: string
  isFrame: string // 0是 1否
  isCache: string // 0缓存 1不缓存
  menuType: string // M目录 C菜单 F按钮
  visible: string // 0显示 1隐藏
  status: string // 0正常 1停用
  perms?: string
  icon?: string
  remark?: string
  createTime: string
  updateTime: string
  children?: Menu[]
}

// 菜单查询参数
export interface MenuQuery {
  menuName?: string
  status?: string
  menuType?: string
}

// 菜单表单
export interface MenuForm {
  menuId?: number
  menuName: string
  parentId: number
  orderNum: number
  path?: string
  component?: string
  query?: string
  isFrame: string
  isCache: string
  menuType: string
  visible: string
  status: string
  perms?: string
  icon?: string
  remark?: string
}

// 父级菜单选项
export interface ParentMenuOption {
  value: number
  label: string
  children?: ParentMenuOption[]
}