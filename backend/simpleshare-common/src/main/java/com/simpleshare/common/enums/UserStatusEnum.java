package com.simpleshare.common.enums;

/**
 * 用户状态枚举
 *
 * @author SimpleShare
 */
public enum UserStatusEnum {
    /**
     * 正常
     */
    NORMAL("0", "正常"),

    /**
     * 停用
     */
    DISABLED("1", "停用");

    private final String code;
    private final String info;

    UserStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

    /**
     * 根据代码获取枚举
     */
    public static UserStatusEnum getByCode(String code) {
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return NORMAL;
    }

    /**
     * 判断是否为正常状态
     */
    public static boolean isNormal(String code) {
        return NORMAL.getCode().equals(code);
    }
}