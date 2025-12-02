package com.simpleshare.article.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

/**
 * 文章收藏实体
 *
 * 对应表：article_favorite
 *
 * @author SimpleShare
 */
@TableName("article_favorite")
public class ArticleFavorite extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 收藏ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 用户ID */
    private Long userId;

    /** 文章ID */
    private Long articleId;

    /** 收藏夹名称 */
    private String folderName;

    /** 收藏标签 */
    private String tags;

    /** 收藏备注 */
    private String notes;

    /** 作者昵称（非表字段） */
    @TableField(exist = false)
    private String authorName;

    /** 作者头像（非表字段） */
    @TableField(exist = false)
    private String authorAvatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }
}
