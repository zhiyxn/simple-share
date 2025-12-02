package com.simpleshare.common.enums;

/**
 * 用户类型枚举
 *
 * @author SimpleShare
 */
public enum UserTypeEnum {
    /**
     * 普通用户
     */
    NORMAL("0", "普通用户"),

    /**
     * 管理员
     */
    ADMIN("1", "管理员"),

    /**
     * 会员
     */
    VIP("3", "会员");

    private final String code;
    private final String info;

    UserTypeEnum(String code, String info) {
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
    public static UserTypeEnum getByCode(String code) {
        for (UserTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return NORMAL;
    }

    /**
     * 判断是否为会员
     */
    public static boolean isVip(String code) {
        return VIP.getCode().equals(code);
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin(String code) {
        return ADMIN.getCode().equals(code);
    }
}