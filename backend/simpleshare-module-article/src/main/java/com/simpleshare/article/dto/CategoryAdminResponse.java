package com.simpleshare.article.dto;

import com.simpleshare.article.domain.Category;
import com.simpleshare.common.utils.StringUtils;

import java.time.format.DateTimeFormatter;

/**
 * Response model used by the admin category screens.
 */
public class CategoryAdminResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String color;
    private String parentId;
    private Integer sort;
    private boolean enabled;
    private boolean visible;
    private Integer articleCount;
    private String createdAt;
    private String updatedAt;

    public static CategoryAdminResponse from(Category category) {
        CategoryAdminResponse response = new CategoryAdminResponse();
        if (category.getCategoryId() != null) {
            response.setId(String.valueOf(category.getCategoryId()));
        }
        response.setName(category.getCategoryName());
        response.setSlug(StringUtils.isNotEmpty(category.getCategoryPath()) ? category.getCategoryPath() : category.getCategoryName());
        response.setDescription(category.getDescription());
        response.setIcon(category.getIcon());
        response.setColor(null);
        response.setParentId(category.getParentId() != null ? String.valueOf(category.getParentId()) : null);
        response.setSort(category.getOrderNum());
        response.setEnabled(!"1".equals(category.getStatus()));
        response.setVisible(!"1".equals(category.getStatus()));
        response.setArticleCount(category.getArticleCount());
        if (category.getCreateTime() != null) {
            response.setCreatedAt(FORMATTER.format(category.getCreateTime()));
        }
        if (category.getUpdateTime() != null) {
            response.setUpdatedAt(FORMATTER.format(category.getUpdateTime()));
        }
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Integer getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
