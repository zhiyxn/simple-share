package com.simpleshare.system.controller;

import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.system.domain.SysMenu;
import com.simpleshare.common.core.domain.TreeSelect;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.dto.MenuAdminRequest;
import com.simpleshare.system.dto.MenuAdminResponse;
import com.simpleshare.system.service.ISysMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理后台REST API
 */
@RestController
@RequestMapping("/system/admin/menus")
public class MenuAdminRestController extends BaseController {

    private final ISysMenuService menuService;

    public MenuAdminRestController(ISysMenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping
    public AjaxResult list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        List<MenuAdminResponse> items = menus.stream()
                .map(MenuAdminResponse::fromSysMenu)
                .collect(Collectors.toList());
        return AjaxResult.success(items);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping("/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId) {
        SysMenu menu = menuService.selectMenuById(menuId);
        if (menu == null) {
            return AjaxResult.error("菜单不存在");
        }
        return AjaxResult.success(MenuAdminResponse.fromSysMenu(menu));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
        return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysMenu> menus = menuService.selectMenuList(new SysMenu(), getUserId());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MenuAdminRequest request) {
        if (!menuService.checkMenuNameUnique(request.toSysMenu())) {
            return AjaxResult.error("新增菜单'" + request.getMenuName() + "'失败，菜单名称已存在");
        }
        
        SysMenu menu = request.toSysMenu();
        menu.setCreateBy(getUserId());
        int result = menuService.insertMenu(menu);
        if (result > 0) {
            return AjaxResult.success("菜单创建成功", MenuAdminResponse.fromSysMenu(menu));
        }
        return AjaxResult.error("菜单创建失败");
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @PutMapping("/{menuId}")
    public AjaxResult edit(@PathVariable Long menuId, @Validated @RequestBody MenuAdminRequest request) {
        SysMenu existingMenu = menuService.selectMenuById(menuId);
        if (existingMenu == null) {
            return AjaxResult.error("菜单不存在");
        }

        SysMenu menu = request.toSysMenu();
        menu.setMenuId(menuId);
        
        if (!menuService.checkMenuNameUnique(menu)) {
            return AjaxResult.error("修改菜单'" + request.getMenuName() + "'失败，菜单名称已存在");
        } else if (menuId.equals(menu.getParentId())) {
            return AjaxResult.error("修改菜单'" + request.getMenuName() + "'失败，上级菜单不能选择自身");
        }
        
        menu.setUpdateBy(getUserId());
        int result = menuService.updateMenu(menu);
        if (result > 0) {
            return AjaxResult.success("菜单更新成功", MenuAdminResponse.fromSysMenu(menu));
        }
        return AjaxResult.error("菜单更新失败");
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return AjaxResult.error("存在子菜单，不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return AjaxResult.error("菜单已分配，不允许删除");
        }
        
        int result = menuService.deleteMenuById(menuId);
        if (result > 0) {
            return AjaxResult.success("菜单删除成功");
        }
        return AjaxResult.error("菜单删除失败");
    }

    /**
     * 批量删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return AjaxResult.error("请选择要删除的菜单");
        }

        // 检查是否有子菜单或已分配给角色
        for (Long menuId : menuIds) {
            if (menuService.hasChildByMenuId(menuId)) {
                SysMenu menu = menuService.selectMenuById(menuId);
                return AjaxResult.error("菜单'" + (menu != null ? menu.getMenuName() : menuId) + "'存在子菜单，不允许删除");
            }
            if (menuService.checkMenuExistRole(menuId)) {
                SysMenu menu = menuService.selectMenuById(menuId);
                return AjaxResult.error("菜单'" + (menu != null ? menu.getMenuName() : menuId) + "'已分配，不允许删除");
            }
        }

        int successCount = 0;
        for (Long menuId : menuIds) {
            if (menuService.deleteMenuById(menuId) > 0) {
                successCount++;
            }
        }

        if (successCount == menuIds.size()) {
            return AjaxResult.success("批量删除成功，共删除" + successCount + "个菜单");
        } else if (successCount > 0) {
            return AjaxResult.success("部分删除成功，共删除" + successCount + "个菜单");
        } else {
            return AjaxResult.error("批量删除失败");
        }
    }
}
