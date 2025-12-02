package com.simpleshare.article.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.article.domain.Article;
import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.common.utils.StringUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response model for admin article listings and detail views.
 */
public class ArticleAdminResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String id;
    private String title;
    private String summary;
    private String content;
    private String memberContent;
    private String status;
    private Integer reviewStatus;
    private String reviewStatusLabel;
    private boolean pendingReview;
    private boolean reviewApproved;
    private boolean reviewRejected;
    private String categoryId;
    private String categoryName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private boolean allowCopy;
    private boolean tieredReading;
    private String previewContent;
    private String coverImage;
    private boolean top;
    private boolean recommend;
    private boolean watermarkEnabled;
    private Integer accessLevel;
    private List<String> tags = Collections.emptyList();
    private boolean allowComments = true;
    private String createdAt;
    private String updatedAt;
    private String publishedAt;
    private boolean passwordProtected;
    private Integer isPasswd;


    public static ArticleAdminResponse from(Article article) {
        ArticleAdminResponse response = new ArticleAdminResponse();
        if (article.getIsPasswd() != null) {
            response.setIsPasswd(article.getIsPasswd());
        } else {
            response.setIsPasswd(StringUtils.isNotEmpty(article.getPassword()) ? 0 : 1);
        }
        if (article.getArticleId() != null) {
            response.setId(String.valueOf(article.getArticleId()));
        }
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setContent(article.getContent());
        response.setMemberContent(article.getMemberContent());
        response.setStatus(mapStatusToString(article.getStatus()));
        ArticleReviewStatus reviewStatus = ArticleReviewStatus.fromCode(article.getReviewStatus());
        response.setReviewStatus(reviewStatus.getCode());
        response.setReviewStatusLabel(reviewStatus.getLabel());
        response.setPendingReview(reviewStatus.isPending());
        response.setReviewApproved(reviewStatus.isApproved());
        response.setReviewRejected(reviewStatus.isRejected());
        response.setCategoryId(article.getCategoryId() != null ? String.valueOf(article.getCategoryId()) : null);
        response.setCategoryName(article.getCategoryName());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setAllowCopy("1".equals(article.getAllowCopy()));
        response.setTieredReading(!"0".equals(article.getEnableTieredRead()));
        response.setPreviewContent(article.getPreviewContent());
        response.setCoverImage(article.getCoverImage());
        response.setTags(parseTags(article.getTags()));
        response.setTop(Integer.valueOf(1).equals(article.getIsTop()));
        response.setRecommend(Integer.valueOf(1).equals(article.getIsRecommend()));
        response.setWatermarkEnabled(!"0".equals(article.getEnableWatermark()));
        response.setAllowComments(resolveAllowComments(article.getRemark()));
        response.setAccessLevel(article.getAccessLevel());
        Boolean passwordFlag = article.getPasswordProtected();
        if (passwordFlag != null) {
            response.setPasswordProtected(passwordFlag);
        } else {
            response.setPasswordProtected(StringUtils.isNotEmpty(article.getPassword()));
        }
        if (article.getCreateTime() != null) {
            response.setCreatedAt(FORMATTER.format(article.getCreateTime()));
        }
        if (article.getUpdateTime() != null) {
            response.setUpdatedAt(FORMATTER.format(article.getUpdateTime()));
        }
        if (article.getPublishTime() != null) {
            response.setPublishedAt(FORMATTER.format(article.getPublishTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        }
        return response;
    }

    private static String mapStatus(String status) {
        // 返回数据库原始值，不进行转换
        if (StringUtils.isEmpty(status)) {
            return "0"; // 默认为草稿状态
        }
        return status;
    }

    private static String mapStatusToString(Integer status) {
        // 将Integer状态转换为String
        if (status == null) {
            return "0"; // 默认为草稿状态
        }
        return String.valueOf(status);
    }

    private static List<String> parseTags(String tags) {
        if (StringUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private static boolean resolveAllowComments(String remark) {
        if (StringUtils.isEmpty(remark)) {
            return true;
        }
        try {
            JsonNode node = MAPPER.readTree(remark);
            if (node != null && node.has("allowComments")) {
                JsonNode allowNode = node.get("allowComments");
                if (allowNode.isBoolean()) {
                    return allowNode.booleanValue();
                }
                String text = allowNode.asText("");
                if (StringUtils.isNotEmpty(text)) {
                    String normalized = text.trim().toLowerCase();
                    if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
                        return false;
                    }
                    if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        String lowered = remark.toLowerCase();
        if (lowered.contains("\"allowcomments\":false") || lowered.contains("\"allow_comments\":false")) {
            return false;
        }
        if (lowered.contains("\"allowcomments\":true") || lowered.contains("\"allow_comments\":true")) {
            return true;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewStatusLabel() {
        return reviewStatusLabel;
    }

    public void setReviewStatusLabel(String reviewStatusLabel) {
        this.reviewStatusLabel = reviewStatusLabel;
    }

    public boolean isPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(boolean pendingReview) {
        this.pendingReview = pendingReview;
    }

    public boolean isReviewApproved() {
        return reviewApproved;
    }

    public void setReviewApproved(boolean reviewApproved) {
        this.reviewApproved = reviewApproved;
    }

    public boolean isReviewRejected() {
        return reviewRejected;
    }

    public void setReviewRejected(boolean reviewRejected) {
        this.reviewRejected = reviewRejected;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isAllowCopy() {
        return allowCopy;
    }

    public void setAllowCopy(boolean allowCopy) {
        this.allowCopy = allowCopy;
    }

    public boolean isTieredReading() {
        return tieredReading;
    }

    public void setTieredReading(boolean tieredReading) {
        this.tieredReading = tieredReading;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isRecommend() {
        return recommend;
    }

    public void setRecommend(boolean recommend) {
        this.recommend = recommend;
    }

    public boolean isWatermarkEnabled() {
        return watermarkEnabled;
    }

    public void setWatermarkEnabled(boolean watermarkEnabled) {
        this.watermarkEnabled = watermarkEnabled;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? Collections.emptyList() : tags;
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
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

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getIsPasswd() {
        return isPasswd;
    }

    public void setIsPasswd(Integer isPasswd) {
        this.isPasswd = isPasswd;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }
}
