package com.simpleshare.system.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.system.domain.SysMenu;
import com.simpleshare.system.domain.SysRoleMenu;
import com.simpleshare.system.mapper.SysMenuMapper;
import com.simpleshare.system.mapper.SysRoleMenuMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Initializes the default menu and button permissions required by the admin portal.
 */
@Component
public class MenuPermissionInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuPermissionInitializer.class);

    private static final Long ROOT_PARENT_ID = 0L;
    private static final Long DEFAULT_OPERATOR_ID = 1L;
    private static final Long DEFAULT_TENANT_ID = 1L;

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Value("${app.security.initialize-menu-permissions:true}")
    private boolean initializePermissions;

    public MenuPermissionInitializer(SysMenuMapper menuMapper, SysRoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!initializePermissions) {
            log.info("Menu permission initialization disabled via configuration");
            return;
        }

        log.info("Initializing default menu button permissions");
        removeDeprecatedMenus();
        List<MenuDefinition> definitions = buildDefinitions();
        for (MenuDefinition definition : definitions) {
            ensureMenu(definition, ROOT_PARENT_ID);
        }
        log.info("Menu permission initialization completed");
    }

    private List<MenuDefinition> buildDefinitions() {
        MenuDefinition content = dir("内容管理", "content", "", "Document", 10)
            .add(menu("文章管理", "articles", "admin/AdminArticles", "Document", 1)
                .add(button("文章列表", "article:article:list", 1))
                .add(button("文章详情", "article:article:query", 2))
                .add(button("新增文章", "article:article:add", 3))
                .add(button("编辑文章", "article:article:edit", 4))
                .add(button("删除文章", "article:article:remove", 5))
                .add(button("发布文章", "article:article:publish", 6))
                .add(button("下线文章", "article:article:offline", 7))
                .add(button("文章置顶", "article:article:top", 8))
                .add(button("推荐设置", "article:article:recommend", 9))
                .add(button("导出文章", "article:article:export", 10)))
            .add(menu("分类管理", "categories", "admin/CategoryManagement", "Collection", 2)
                .add(button("分类列表", "article:category:list", 1))
                .add(button("分类详情", "article:category:query", 2))
                .add(button("新增分类", "article:category:add", 3))
                .add(button("编辑分类", "article:category:edit", 4))
                .add(button("删除分类", "article:category:remove", 5))
                .add(button("导出分类", "article:category:export", 6)));

        MenuDefinition system = dir("系统管理", "system", "", "Setting", 20)
            .add(menu("用户管理", "users", "admin/UserManagement", "User", 1)
                .add(button("用户列表", "system:user:list", 1))
                .add(button("查看用户", "system:user:query", 2))
                .add(button("新增用户", "system:user:add", 3))
                .add(button("编辑用户", "system:user:edit", 4))
                .add(button("删除用户", "system:user:remove", 5))
                .add(button("重置密码", "system:user:resetPwd", 6)))
            .add(menu("角色管理", "roles", "admin/RoleManagement", "Key", 2)
                .add(button("角色列表", "system:role:list", 1))
                .add(button("查看角色", "system:role:query", 2))
                .add(button("新增角色", "system:role:add", 3))
                .add(button("编辑角色", "system:role:edit", 4))
                .add(button("删除角色", "system:role:remove", 5)))
            .add(menu("菜单管理", "menus", "admin/MenuManagement", "Menu", 3)
                .add(button("菜单列表", "system:menu:list", 1))
                .add(button("查看菜单", "system:menu:query", 2))
                .add(button("新增菜单", "system:menu:add", 3))
                .add(button("编辑菜单", "system:menu:edit", 4))
                .add(button("删除菜单", "system:menu:remove", 5)))
            .add(menu("安全设置", "security", "admin/SecuritySettings", "Lock", 4)
                .add(button("查看配置", "system:security:query", 1))
                .add(button("修改配置", "system:security:edit", 2)))
            .add(menu("操作日志", "logs", "admin/OperationLog", "DocumentChecked", 5)
                .add(button("查询日志", "system:operation-log:list", 1))
                .add(button("清理日志", "system:operation-log:clean", 2)))
            .add(menu("租户管理", "tenants", "admin/TenantManagement", "OfficeBuilding", 6)
                .add(button("租户列表", "system:tenant:list", 1))
                .add(button("查看租户", "system:tenant:query", 2))
                .add(button("新增租户", "system:tenant:add", 3))
                .add(button("编辑租户", "system:tenant:edit", 4))
                .add(button("删除租户", "system:tenant:remove", 5))
                .add(button("导出租户", "system:tenant:export", 6)));

        return Arrays.asList(content, system);
    }

    private void removeDeprecatedMenus() {
        List<String> deprecatedNames = Arrays.asList("文件管理", "文件列表", "存储配置");
        List<String> deprecatedPaths = Arrays.asList("infra", "files", "file-config");

        List<SysMenu> candidates = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getMenuName, deprecatedNames)
                        .or(wrapper -> wrapper.in(SysMenu::getPath, deprecatedPaths))
                        .or(wrapper -> wrapper.like(SysMenu::getPerms, "infra:%"))
        );

        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        List<Long> ids = candidates.stream()
                .map(SysMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return;
        }

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getMenuId, ids));
        menuMapper.deleteBatchIds(ids);
        log.info("Removed {} deprecated file management menu entries", ids.size());
    }

    private SysMenu ensureMenu(MenuDefinition definition, Long parentId) {
        SysMenu existing = findExisting(definition, parentId);
        if (existing == null) {
            existing = createMenu(definition, parentId);
        }

        if (existing != null && !definition.children.isEmpty()) {
            Long childParentId = existing.getMenuId();
            for (MenuDefinition child : definition.children) {
                ensureMenu(child, childParentId);
            }
        }
        return existing;
    }

    private SysMenu createMenu(MenuDefinition definition, Long parentId) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(definition.name);
        menu.setParentId(parentId);
        menu.setMenuType(definition.menuType);
        menu.setOrderNum(definition.orderNum);
        menu.setPath(StringUtils.defaultString(definition.path));
        menu.setComponent(StringUtils.defaultString(definition.component));
        menu.setIcon(StringUtils.defaultString(definition.icon));
        menu.setPerms(definition.perms);
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setIsCache(UserConstants.TYPE_BUTTON.equals(definition.menuType) ? "1" : "0");
        menu.setVisible("0");
        menu.setStatus(UserConstants.NORMAL);
        menu.setCreateBy(DEFAULT_OPERATOR_ID);
        menu.getParams().put("tenantId", DEFAULT_TENANT_ID);
        menuMapper.insertMenu(menu);
        return menu;
    }

    private SysMenu findExisting(MenuDefinition definition, Long parentId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (UserConstants.TYPE_BUTTON.equals(definition.menuType)) {
            wrapper.eq(SysMenu::getPerms, definition.perms);
        } else {
            wrapper.eq(SysMenu::getParentId, parentId)
                    .eq(SysMenu::getMenuName, definition.name);
        }
        return menuMapper.selectOne(wrapper);
    }

    private static MenuDefinition dir(String name, String path, String component, String icon, int order) {
        return new MenuDefinition(name, UserConstants.TYPE_DIR, path, component, icon, null, order);
    }

    private static MenuDefinition menu(String name, String path, String component, String icon, int order) {
        return new MenuDefinition(name, UserConstants.TYPE_MENU, path, component, icon, null, order);
    }

    private static MenuDefinition button(String name, String perms, int order) {
        return new MenuDefinition(name, UserConstants.TYPE_BUTTON, null, null, null, perms, order);
    }

    private static final class MenuDefinition {
        private final String name;
        private final String menuType;
        private final String path;
        private final String component;
        private final String icon;
        private final String perms;
        private final int orderNum;
        private final List<MenuDefinition> children = new ArrayList<>();

        private MenuDefinition(String name, String menuType, String path, String component, String icon, String perms, int orderNum) {
            this.name = name;
            this.menuType = menuType;
            this.path = path;
            this.component = component;
            this.icon = icon;
            this.perms = perms;
            this.orderNum = orderNum;
        }

        private MenuDefinition add(MenuDefinition child) {
            this.children.add(child);
            return this;
        }
    }
}
