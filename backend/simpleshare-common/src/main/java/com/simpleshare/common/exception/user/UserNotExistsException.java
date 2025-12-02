package com.simpleshare.common.exception.user;

/**
 * 用户不存在异常类
 *
 * @author SimpleShare
 */
public class UserNotExistsException extends UserException {
    private static final long serialVersionUID = 1L;

    public UserNotExistsException() {
        super("用户不存在");
    }
}