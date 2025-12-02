package com.simpleshare.system.controller;

import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.system.domain.SysMenu;
import com.simpleshare.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 后台菜单树接口，供管理端动态加载菜单使用
 */
@RestController
@RequestMapping("/admin/menu")
public class MenuTreeController extends BaseController {

    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取当前用户可见的菜单树
     */
    @GetMapping("/tree")
    public AjaxResult getMenuTree() {
        Long userId = getUserId();
        if (userId == null) {
            return AjaxResult.success(Collections.emptyList());
        }

        SysMenu query = new SysMenu();
        List<SysMenu> menus = menuService.selectMenuList(query, userId);
        List<SysMenu> tree = menuService.buildMenuTree(menus);
        return AjaxResult.success(tree);
    }
}
