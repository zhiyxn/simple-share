package com.simpleshare.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.simpleshare.system.domain.SysMenu;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理响应DTO
 */
public class MenuAdminResponse {

    /** 菜单ID */
    private Long menuId;

    /** 菜单名称 */
    private String menuName;

    /** 父菜单ID */
    private Long parentId;

    /** 父菜单名称 */
    private String parentName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 是否为外链（0是 1否） */
    private String isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    private String isCache;

    /** 菜单类型（M目录 C菜单 F按钮） */
    private String menuType;

    /** 菜单状态（0显示 1隐藏） */
    private String visible;

    /** 菜单状态（0正常 1停用） */
    private String status;

    /** 权限标识 */
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 子菜单 */
    private List<MenuAdminResponse> children;

    public MenuAdminResponse() {
    }

    /**
     * 从SysMenu实体转换
     */
    public static MenuAdminResponse fromSysMenu(SysMenu menu) {
        MenuAdminResponse response = new MenuAdminResponse();
        response.setMenuId(menu.getMenuId());
        response.setMenuName(menu.getMenuName());
        response.setParentId(menu.getParentId());
        response.setOrderNum(menu.getOrderNum());
        response.setPath(menu.getPath());
        response.setComponent(menu.getComponent());
        response.setQuery(menu.getQuery());
        response.setIsFrame(menu.getIsFrame());
        response.setIsCache(menu.getIsCache());
        response.setMenuType(menu.getMenuType());
        response.setVisible(menu.getVisible());
        response.setStatus(menu.getStatus());
        response.setPerms(menu.getPerms());
        response.setIcon(menu.getIcon());
        response.setRemark(menu.getRemark());
        response.setCreateTime(menu.getCreateTime());
        response.setUpdateTime(menu.getUpdateTime());
        
        // 转换子菜单
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            response.setChildren(menu.getChildren().stream()
                    .map(MenuAdminResponse::fromSysMenu)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }

    /**
     * 批量转换
     */
    public static List<MenuAdminResponse> fromSysMenuList(List<SysMenu> menus) {
        return menus.stream()
                .map(MenuAdminResponse::fromSysMenu)
                .collect(Collectors.toList());
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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<MenuAdminResponse> getChildren() {
        return children;
    }

    public void setChildren(List<MenuAdminResponse> children) {
        this.children = children;
    }
}