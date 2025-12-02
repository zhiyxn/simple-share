package com.simpleshare.article.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// import com.simpleshare.common.annotation.Excel;
import com.simpleshare.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章分类对象 category
 *
 * @author SimpleShare
 */
@TableName("article_category")
public class Category extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long categoryId;

    /** 租户ID */
    // @Excel(name = "租户ID")
    private String tenantId;

    /** 分类名称 */
    // @Excel(name = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 0, max = 30, message = "分类名称不能超过30个字符")
    private String categoryName;

    /** 父分类ID */
    // @Excel(name = "父分类ID")
    private Long parentId;

    /** 祖级列表 */
    // @Excel(name = "祖级列表")
    private String ancestors;

    /** 分类路径 */
    private String categoryPath;

    /** 显示顺序 */
    // @Excel(name = "显示顺序")
    private Integer orderNum;

    /** 分类状态（0正常 1停用） */
    // @Excel(name = "分类状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 分类描述 */
    // @Excel(name = "分类描述")
    @Size(min = 0, max = 500, message = "分类描述不能超过500个字符")
    private String description;

    /** 分类图标 */
    // @Excel(name = "分类图标")
    private String icon;

    /** 分类封面图片 */
    // @Excel(name = "分类封面")
    private String coverImage;

    /** 文章数量 */
    @TableField(exist = false)
    private Integer articleCount;

    /** 父分类名称 */
    @TableField(exist = false)
    private String parentName;

    /** 子分类 */
    @TableField(exist = false)
    private List<Category> children = new ArrayList<Category>();

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public Integer getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("categoryId", getCategoryId())
                .append("tenantId", getTenantId())
                .append("categoryName", getCategoryName())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("categoryPath", getCategoryPath())
                .append("orderNum", getOrderNum())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("description", getDescription())
                .append("icon", getIcon())
                .append("coverImage", getCoverImage())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}