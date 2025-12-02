package com.simpleshare.system.dto;

import com.simpleshare.system.domain.SysMenu;
import com.simpleshare.common.utils.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 菜单管理请求DTO
 */
public class MenuAdminRequest {

    /** 菜单ID */
    private Long menuId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    /** 父菜单ID */
    private Long parentId;

    /** 显示顺序 */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    /** 路由地址 */
    @Size(max = 200, message = "路由地址不能超过200个字符")
    private String path;

    /** 组件路径 */
    @Size(max = 200, message = "组件路径不能超过200个字符")
    private String component;

    /** 路由参数 */
    private String query;

    /** 是否为外链（0是 1否） */
    private String isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    private String isCache;

    /** 菜单类型（M目录 C菜单 F按钮） */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    private String visible;

    /** 菜单状态（0正常 1停用） */
    private String status;

    /** 权限标识 */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public MenuAdminRequest() {
    }

    /**
     * 转换为SysMenu实体
     */
    public SysMenu toSysMenu() {
        SysMenu menu = new SysMenu();
        menu.setMenuId(this.menuId);
        menu.setMenuName(this.menuName);
        menu.setParentId(this.parentId != null ? this.parentId : 0L);
        menu.setOrderNum(this.orderNum);
        menu.setPath(this.path);
        menu.setComponent(this.component);
        menu.setQuery(this.query);
        menu.setIsFrame(StringUtils.isNotEmpty(this.isFrame) ? this.isFrame : "1");
        menu.setIsCache(StringUtils.isNotEmpty(this.isCache) ? this.isCache : "0");
        menu.setMenuType(this.menuType);
        menu.setVisible(StringUtils.isNotEmpty(this.visible) ? this.visible : "0");
        menu.setStatus(StringUtils.isNotEmpty(this.status) ? this.status : "0");
        menu.setPerms(this.perms);
        menu.setIcon(this.icon);
        menu.setRemark(this.remark);
        return menu;
    }

    // Getters and Setters
    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getIsFrame() {
        return isFrame;
    }

    public void setIsFrame(String isFrame) {
        this.isFrame = isFrame;
    }

    public String getIsCache() {
        return isCache;
    }

    public void setIsCache(String isCache) {
        this.isCache = isCache;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}