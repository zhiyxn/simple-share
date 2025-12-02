package com.simpleshare.system.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * AiTech 卡片点击上报请求
 */
public class AiTechCardClickRequest {

    @NotBlank(message = "卡片唯一标识不能为空")
    @Size(max = 100, message = "卡片标识不能超过100字符")
    private String cardKey;

    @NotBlank(message = "卡片标题不能为空")
    @Size(max = 200, message = "卡片标题不能超过200字符")
    private String cardTitle;

    @NotBlank(message = "卡片路径不能为空")
    @Size(max = 255, message = "卡片路径不能超过255字符")
    private String cardPath;

    @Size(max = 100, message = "卡片分类不能超过100字符")
    private String cardCategory;

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
}
