package com.simpleshare.article.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
// import com.simpleshare.common.annotation.Excel;
import com.simpleshare.common.core.domain.BaseEntity;
import com.simpleshare.common.utils.SecurityUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 文章对象 article
 *
 * @author SimpleShare
 */
@TableName("article")
public class Article extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文章ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long articleId;

    /** 租户ID */
    // @Excel(name = "租户ID")
    private Long tenantId;

    /** 文章标题 */
    // @Excel(name = "文章标题")
    @NotBlank(message = "文章标题不能为空")
    @Size(min = 0, max = 200, message = "文章标题不能超过200个字符")
    private String title;

    /** 文章摘要 */
    // @Excel(name = "文章摘要")
    @Size(min = 0, max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    /** 文章内容 */
    private String content;

    /** 会员内容 */
    @TableField("member_content")
    private String memberContent;

    /** 封面图片 */
    // @Excel(name = "封面图片")
    private String coverImage;

    /** 分类ID */
    // @Excel(name = "分类ID")
    private Long categoryId;

    /** 分类名称 */
    @TableField(exist = false)
    private String categoryName;

    /** 作者ID */
    // @Excel(name = "作者ID")
    private Long authorId;

    /** 作者名称 */
    @TableField(exist = false)
    private String authorName;

    /** 文章状态（0草稿 1已发布 2已下线） */
    // @Excel(name = "文章状态", readConverterExp = "0=草稿,1=已发布,2=已下线")
    private Integer status;

    // 访问级别（0=普通 1=会员）
    /** 审核状态（0待审核 1通过 2未通过） */
    @TableField("review_status")
    private Integer reviewStatus;

    // 访问级别�?=普�?1=会员�?
    @TableField("access_level")
    private Integer accessLevel;

    /** 是否启用分级阅读（0否 1是） */
    @TableField("enable_tiered_read")
    private String enableTieredRead;

    /** 允许复制（0不允许 1允许） */
    // @Excel(name = "允许复制", readConverterExp = "0=不允许,1=允许")
    private String allowCopy;

    /** 预览内容 */
    @TableField("preview_content")
    private String previewContent;

    /** 是否置顶（0否 1是） */
    // @Excel(name = "是否置顶", readConverterExp = "0=否,1=是")
    @TableField("is_top")
    private Integer isTop;

    /** 是否推荐（0否 1是） */
    // @Excel(name = "是否推荐", readConverterExp = "0=否,1=是")
    @TableField("is_recommend")
    private Integer isRecommend;

    /** 浏览次数 */
    // @Excel(name = "浏览次数")
    private Long viewCount;

    /** 点赞次数 */
    // @Excel(name = "点赞次数")
    private Long likeCount;

    /** 收藏次数 */
    // @Excel(name = "收藏次数")
    private Long collectCount;

    /** 评论次数 */
    // @Excel(name = "评论次数")
    private Long commentCount;

    /** 分享次数 */
    @TableField("share_count")
    private Long shareCount;

    /** 排序 */
    // @Excel(name = "排序")
    private Integer orderNum;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** SEO关键词 */
    private String seoKeywords;

    /** SEO描述 */
    private String seoDescription;

    /** 文章访问密码（BCrypt加密） */
    private String password;

    /** 是否启用密码：0启用 1不启用 */
    @TableField("is_passwd")
    private Integer isPasswd;

    /** 是否开启密码保护（瞬时字段） */
    @TableField(exist = false)
    private Boolean passwordProtected;

    /** 是否已校验密码（瞬时字段） */
    @TableField(exist = false)
    private Boolean passwordVerified;

    /** 是否清除密码（瞬时字段） */
    @TableField(exist = false)
    private Boolean clearPassword;

    /** 标签，逗号分隔 */
    private String tags;

    /** 是否启用水印（0否 1是） */
    @TableField("enable_watermark")
    private String enableWatermark;

    /** 删除标志（0代表存在 1代表删除） */
    @TableField("del_flag")
    private String delFlag;

    /** 预览模式 */
    @TableField(exist = false)
    private Boolean previewOnly;

    /** 当前用户是否具备查看全文的权限（非持久化字段） */
    @TableField(exist = false)
    private Boolean fullReadable;

    /** 当前用户是否有访问权限（非持久化字段） */
    @TableField(exist = false)
    private Boolean hasAccess;

    /** 访问被拒绝的原因（非持久化字段） */
    @TableField(exist = false)
    private String accessDeniedReason;

    @TableField(exist = false)
    private Boolean hasMemberContent;

    @TableField(exist = false)
    private Boolean memberContentLocked;

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public Long getId() {
        return articleId;
    }

    public void setId(Long id) {
        this.articleId = id;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMemberContent() {
        return memberContent;
    }

    public void setMemberContent(String memberContent) {
        this.memberContent = memberContent;
    }

    public String getContent() {
        return content;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void setAllowCopy(String allowCopy) {
        this.allowCopy = allowCopy;
    }

    public String getAllowCopy() {
        return allowCopy;
    }

    public String getEnableTieredRead() {
        return enableTieredRead;
    }

    public void setEnableTieredRead(String enableTieredRead) {
        this.enableTieredRead = enableTieredRead;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Integer getIsRecommend() {
        return isRecommend;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setCollectCount(Long collectCount) {
        this.collectCount = collectCount;
    }

    public Long getCollectCount() {
        return collectCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setSeoKeywords(String seoKeywords) {
        this.seoKeywords = seoKeywords;
    }

    public String getSeoKeywords() {
        return seoKeywords;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsPasswd() {
        return isPasswd;
    }

    public void setIsPasswd(Integer isPasswd) {
        this.isPasswd = isPasswd;
    }

    public Boolean getPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(Boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    public Boolean getPasswordVerified() {
        return passwordVerified;
    }

    public void setPasswordVerified(Boolean passwordVerified) {
        this.passwordVerified = passwordVerified;
    }

    public Boolean getClearPassword() {
        return clearPassword;
    }

    public void setClearPassword(Boolean clearPassword) {
        this.clearPassword = clearPassword;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public String getEnableWatermark() {
        return enableWatermark;
    }

    public void setEnableWatermark(String enableWatermark) {
        this.enableWatermark = enableWatermark;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Boolean getPreviewOnly() {
        return previewOnly;
    }

    public void setPreviewOnly(Boolean previewOnly) {
        this.previewOnly = previewOnly;
    }

    public Boolean getFullReadable() {
        return fullReadable;
    }

    public void setFullReadable(Boolean fullReadable) {
        this.fullReadable = fullReadable;
    }

    public Boolean getHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(Boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public String getAccessDeniedReason() {
        return accessDeniedReason;
    }

    public void setAccessDeniedReason(String accessDeniedReason) {
        this.accessDeniedReason = accessDeniedReason;
    }

    public Boolean getHasMemberContent() {
        return hasMemberContent;
    }

    public void setHasMemberContent(Boolean hasMemberContent) {
        this.hasMemberContent = hasMemberContent;
    }

    public Boolean getMemberContentLocked() {
        return memberContentLocked;
    }

    public void setMemberContentLocked(Boolean memberContentLocked) {
        this.memberContentLocked = memberContentLocked;
    }

    // BaseEntity 相关方法
    public java.time.LocalDateTime getCreateTime() {
        return super.getCreateTime();
    }

    public void setCreateTime(java.time.LocalDateTime createTime) {
        super.setCreateTime(createTime);
    }

    public java.time.LocalDateTime getUpdateTime() {
        return super.getUpdateTime();
    }

    public void setUpdateTime(java.time.LocalDateTime updateTime) {
        super.setUpdateTime(updateTime);
    }

    public Long getCreateBy() {
        return super.getCreateBy();
    }

    public void setCreateBy(Long createBy) {
        super.setCreateBy(createBy);
    }

    public Long getUpdateBy() {
        return super.getUpdateBy();
    }

    public void setUpdateBy(Long updateBy) {
        super.setUpdateBy(updateBy);
    }

    public String getRemark() {
        return super.getRemark();
    }

    public void setRemark(String remark) {
        super.setRemark(remark);
    }

    /**
     * 保存前自动设置作者ID
     */
    public void preInsert() {
        // 如果作者ID为空，设置为当前登录用户ID或默认值
        if (this.authorId == null) {
            Long currentUserId = SecurityUtils.getUserIdSafely();
            this.authorId = currentUserId != null ? currentUserId : 1L; // 默认管理员ID
        }

        // 如果租户ID为空，设置为默认值
        if (this.tenantId == null) {
            this.tenantId = 1L;
        }

        if (this.enableTieredRead == null) {
            this.enableTieredRead = "1";
        }

        if (this.enableWatermark == null) {
            this.enableWatermark = "1";
        }

        if (!"1".equals(this.enableTieredRead)) {
            this.previewContent = null;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("articleId", getArticleId())
                .append("tenantId", getTenantId())
                .append("title", getTitle())
                .append("summary", getSummary())
                .append("content", getContent())
                .append("memberContent", getMemberContent())
                .append("coverImage", getCoverImage())
                .append("categoryId", getCategoryId())
                .append("authorId", getAuthorId())
                .append("status", getStatus())
                .append("reviewStatus", getReviewStatus())
                .append("enableTieredRead", getEnableTieredRead())
                .append("allowCopy", getAllowCopy())
                .append("previewContent", getPreviewContent())
                .append("isTop", getIsTop())
                .append("isRecommend", getIsRecommend())
                .append("viewCount", getViewCount())
                .append("likeCount", getLikeCount())
                .append("collectCount", getCollectCount())
                .append("commentCount", getCommentCount())
                .append("shareCount", getShareCount())
                .append("orderNum", getOrderNum())
                .append("publishTime", getPublishTime())
                .append("seoKeywords", getSeoKeywords())
                .append("seoDescription", getSeoDescription())
                .append("enableWatermark", getEnableWatermark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("previewOnly", getPreviewOnly())
                .append("fullReadable", getFullReadable())
                .append("hasAccess", getHasAccess())
                .append("hasMemberContent", getHasMemberContent())
                .append("memberContentLocked", getMemberContentLocked())
                .append("accessDeniedReason", getAccessDeniedReason())
                .append("remark", getRemark())
                .toString();
    }
}
