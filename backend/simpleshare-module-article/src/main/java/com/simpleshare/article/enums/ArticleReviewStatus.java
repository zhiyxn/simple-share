package com.simpleshare.article.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * 审核状态枚举
 */
public enum ArticleReviewStatus {

    PENDING(0, "pending", "待审核"),
    APPROVED(1, "approved", "已通过"),
    REJECTED(2, "rejected", "未通过");

    private final int code;
    private final String key;
    private final String label;

    ArticleReviewStatus(int code, String key, String label) {
        this.code = code;
        this.key = key;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public static ArticleReviewStatus fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(PENDING);
    }

    public static ArticleReviewStatus fromKey(String key) {
        if (key == null) {
            return PENDING;
        }
        final String normalized = key.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(status -> Objects.equals(status.key, normalized))
                .findFirst()
                .orElse(PENDING);
    }
}
