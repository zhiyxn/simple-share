package com.simpleshare.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * AiTech 卡片点击统计实体
 */
@TableName("aitech_card_click")
public class AiTechCardClick extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 卡片唯一标识 */
    private String cardKey;

    /** 展示标题 */
    private String cardTitle;

    /** 前端路由路径 */
    private String cardPath;

    /** 所属分类/学科 */
    private String cardCategory;

    /** 累计点击次数 */
    private Long clickCount;

    /** 最近点击时间 */
    @TableField("last_click_time")
    private LocalDateTime lastClickTime;

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
}
