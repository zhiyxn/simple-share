package com.simpleshare.system.enums;

/**
 * 邮箱验证码场景
 */
public enum EmailVerificationScene {

    REGISTER("register", "注册"),
    RESET_PASSWORD("reset_password", "密码重置"),
    LOGIN("login", "登录");

    private final String code;
    private final String description;

    EmailVerificationScene(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmailVerificationScene fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EmailVerificationScene scene : values()) {
            if (scene.code.equalsIgnoreCase(code)) {
                return scene;
            }
        }
        return null;
    }
}
