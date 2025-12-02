package com.simpleshare.system.dto;

import com.simpleshare.system.domain.AiTechCardClick;

import java.time.LocalDateTime;

/**
 * AiTech 卡片点击统计响应
 */
public class AiTechCardClickResponse {

    private String cardKey;
    private String cardTitle;
    private String cardPath;
    private String cardCategory;
    private Long clickCount;
    private LocalDateTime lastClickTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AiTechCardClickResponse from(AiTechCardClick entity) {
        if (entity == null) {
            return null;
        }
        AiTechCardClickResponse response = new AiTechCardClickResponse();
        response.setCardKey(entity.getCardKey());
        response.setCardTitle(entity.getCardTitle());
        response.setCardPath(entity.getCardPath());
        response.setCardCategory(entity.getCardCategory());
        response.setClickCount(entity.getClickCount());
        response.setLastClickTime(entity.getLastClickTime());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardPath() {
        return cardPath;
    }

    public void setCardPath(String cardPath) {
        this.cardPath = cardPath;
    }

    public String getCardCategory() {
        return cardCategory;
    }

    public void setCardCategory(String cardCategory) {
        this.cardCategory = cardCategory;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public LocalDateTime getLastClickTime() {
        return lastClickTime;
    }

    public void setLastClickTime(LocalDateTime lastClickTime) {
        this.lastClickTime = lastClickTime;
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
}
