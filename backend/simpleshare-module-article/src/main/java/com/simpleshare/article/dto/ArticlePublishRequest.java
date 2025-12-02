package com.simpleshare.article.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.simpleshare.article.domain.Article;
import com.simpleshare.common.utils.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Request payload for publishing or updating articles with full metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArticlePublishRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String title;
    private String summary;
    private String content;
    private String memberContent;
    private String previewContent;
    private String coverImage;
    private Long categoryId;
    private List<String> tags;
    private String status;
    private Integer accessLevel;
    private Boolean allowComments;
    private Boolean pinned;
    private Boolean isRecommend;
    private Boolean tieredReading;
    private Boolean allowCopy;
    private Boolean enableWatermark;
    private Integer orderNum;
    private String seoKeywords;
    private String seoDescription;
    private Boolean passwordProtected;
    private String password;
    private Integer isPasswd;
    private Boolean clearPassword;

    public Article applyTo(Article target, Article existing) {
        if (target == null) {
            target = new Article();
        }

        if (title != null) {
            target.setTitle(title);
        }
        if (summary != null) {
            target.setSummary(summary);
        }
        if (content != null) {
            target.setContent(content);
        }
        if (memberContent != null) {
            target.setMemberContent(memberContent);
        }
        if (previewContent != null) {
            target.setPreviewContent(previewContent);
        }
        if (coverImage != null) {
            target.setCoverImage(coverImage);
        }
        if (categoryId != null) {
            target.setCategoryId(categoryId);
        }
        if (tags != null) {
            target.setTags(serializeTags(tags));
        }
        if (status != null) {
            target.setStatus(mapStatusToInteger(status));
        }
        if (accessLevel != null) {
            target.setAccessLevel(accessLevel);
        }
        if (allowCopy != null) {
            target.setAllowCopy(Boolean.TRUE.equals(allowCopy) ? "1" : "0");
        }
        if (enableWatermark != null) {
            target.setEnableWatermark(Boolean.TRUE.equals(enableWatermark) ? "1" : "0");
        }
        if (tieredReading != null) {
            target.setEnableTieredRead(Boolean.TRUE.equals(tieredReading) ? "1" : "0");
        }
        if (pinned != null) {
            target.setIsTop(Boolean.TRUE.equals(pinned) ? 1 : 0);
        }
        if (isRecommend != null) {
            target.setIsRecommend(Boolean.TRUE.equals(isRecommend) ? 1 : 0);
        }
        if (orderNum != null) {
            target.setOrderNum(orderNum);
        }
        if (seoKeywords != null) {
            target.setSeoKeywords(seoKeywords);
        }
        if (seoDescription != null) {
            target.setSeoDescription(seoDescription);
        }
        if (allowComments != null) {
            target.setRemark(mergeRemark(existing != null ? existing.getRemark() : null, allowComments));
        }
        if (passwordProtected != null) {
            target.setPasswordProtected(passwordProtected);
            if (Boolean.FALSE.equals(passwordProtected)) {
                target.setClearPassword(true);
            }
            target.setIsPasswd(Boolean.TRUE.equals(passwordProtected) ? 0 : 1);
        }
        if (clearPassword != null) {
            target.setClearPassword(clearPassword);
            if (Boolean.TRUE.equals(clearPassword)) {
                target.setPassword(null);
                target.setIsPasswd(1);
            }
        }
        if (password != null) {
            target.setPassword(password);
            target.setIsPasswd(0);
        }

        if (isPasswd != null) {
            target.setIsPasswd(isPasswd);
        }
        return target;
    }

    private String serializeTags(List<String> tagList) {
        if (tagList == null) {
            return null;
        }
        String joined = tagList.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(","));
        return joined;
    }

    private Integer mapStatusToInteger(String status) {
        if (StringUtils.isEmpty(status)) {
            return 1; // 默认为发布状态
        }
        String normalized = status.toLowerCase().trim();
        if (normalized.startsWith("publish") || normalized.startsWith("public")) {
            return 1;
        }
        if (normalized.startsWith("draft")) {
            return 0;
        }
        if (normalized.startsWith("offline") || normalized.startsWith("private")) {
            return 2;
        }
        return 1;
    }

    private String mergeRemark(String existingRemark, boolean allowCommentsFlag) {
        ObjectNode node;
        if (StringUtils.isNotEmpty(existingRemark)) {
            try {
                JsonNode parsed = MAPPER.readTree(existingRemark);
                if (parsed.isObject()) {
                    node = ((ObjectNode) parsed).deepCopy();
                } else {
                    node = MAPPER.createObjectNode();
                    node.put("legacyRemark", existingRemark);
                }
            } catch (Exception ignored) {
                node = MAPPER.createObjectNode();
                node.put("legacyRemark", existingRemark);
            }
        } else {
            node = MAPPER.createObjectNode();
        }
        node.put("allowComments", allowCommentsFlag);
        return node.toString();
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Boolean getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(Boolean isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Boolean getTieredReading() {
        return tieredReading;
    }

    public void setTieredReading(Boolean tieredReading) {
        this.tieredReading = tieredReading;
    }

    public Boolean getAllowCopy() {
        return allowCopy;
    }

    public void setAllowCopy(Boolean allowCopy) {
        this.allowCopy = allowCopy;
    }

    public Boolean getEnableWatermark() {
        return enableWatermark;
    }

    public void setEnableWatermark(Boolean enableWatermark) {
        this.enableWatermark = enableWatermark;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getSeoKeywords() {
        return seoKeywords;
    }

    public void setSeoKeywords(String seoKeywords) {
        this.seoKeywords = seoKeywords;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    public Boolean getPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(Boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
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

    public Boolean getClearPassword() {
        return clearPassword;
    }

    public void setClearPassword(Boolean clearPassword) {
        this.clearPassword = clearPassword;
    }
}
