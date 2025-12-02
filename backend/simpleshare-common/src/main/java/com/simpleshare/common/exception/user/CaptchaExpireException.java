package com.simpleshare.common.exception.user;

/**
 * 验证码失效异常类
 *
 * @author SimpleShare
 */
public class CaptchaExpireException extends UserException {
    private static final long serialVersionUID = 1L;

    public CaptchaExpireException() {
        super("user.captcha.expire", null);
    }
}