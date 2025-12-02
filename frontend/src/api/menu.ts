import request from '@/api/request'
import type { Menu, MenuQuery, MenuForm } from '@/types/menu'

const ADMIN_MENU_BASE = '/system/admin/menus'

export const menuApi = {
  // 获取菜单列表
  getMenuList(params?: MenuQuery) {
    return request.get<Menu[]>(ADMIN_MENU_BASE, params)
  },

  // 获取菜单树
  getMenuTree(params?: MenuQuery) {
    return request.get<Menu[]>('/admin/menu/tree', params)
  },

  // 获取菜单详情
  getMenuDetail(menuId: number) {
    return request.get<Menu>(`${ADMIN_MENU_BASE}/${menuId}`)
  },

  // 创建菜单
  createMenu(data: MenuForm) {
    return request.post<Menu>(ADMIN_MENU_BASE, data)
  },

  // 更新菜单
  updateMenu(menuId: number, data: MenuForm) {
    return request.put<Menu>(`${ADMIN_MENU_BASE}/${menuId}`, data)
  },

  // 删除菜单
  deleteMenu(menuId: number) {
    return request.delete(`${ADMIN_MENU_BASE}/${menuId}`)
  },

  // 批量删除菜单
  batchDeleteMenus(menuIds: number[]) {
    return request.delete(`${ADMIN_MENU_BASE}/batch`, {
      data: menuIds
    })
  },

  // 获取父级菜单选项（用于下拉选择）
  getParentMenuOptions() {
    return request.get(`${ADMIN_MENU_BASE}/treeselect`)
  },

  // 加载对应角色菜单列表
  roleTreeSelect(roleId: string | number) {
    return request.get(`/system/admin/menus/roleMenuTreeselect/${roleId}`)
  }
}
