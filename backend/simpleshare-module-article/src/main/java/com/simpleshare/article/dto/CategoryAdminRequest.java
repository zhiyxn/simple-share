package com.simpleshare.article.dto;

/**
 * 功能：管理员界面创建或更新分类的请求载体
 * 作者：小码哥（wx：xmgcode88）
 */
public class CategoryAdminRequest {

    /** 分类名称 */
    private String name;
    /** 分类唯一标识（短链接） */
    private String slug;
    /** 分类描述 */
    private String description;
    /** 图标标识或URL */
    private String icon;
    /** 颜色标识（如HEX） */
    private String color;
    /** 父分类ID */
    private Long parentId;
    /** 排序值（越小越靠前） */
    private Integer sort;
    /** 是否启用 */
    private Boolean enabled;

    /**
     * 获取分类名称
     * @return 分类名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置分类名称
     * @param name 分类名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取分类短链接标识
     * @return 分类短链接标识
     */
    public String getSlug() {
        return slug;
    }

    /**
     * 设置分类短链接标识
     * @param slug 短链接标识
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 获取分类描述
     * @return 分类描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置分类描述
     * @param description 分类描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取分类图标
     * @return 图标标识或URL
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置分类图标
     * @param icon 图标标识或URL
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 获取颜色标识
     * @return 颜色标识（如HEX）
     */
    public String getColor() {
        return color;
    }

    /**
     * 设置颜色标识
     * @param color 颜色标识（如HEX）
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 获取父分类ID
     * @return 父分类ID
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置父分类ID
     * @param parentId 父分类ID
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取排序值
     * @return 排序值（越小越靠前）
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置排序值
     * @param sort 排序值（越小越靠前）
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取是否启用
     * @return 是否启用
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     * @param enabled 是否启用
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
