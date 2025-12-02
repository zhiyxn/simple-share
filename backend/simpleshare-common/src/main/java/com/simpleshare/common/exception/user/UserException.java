package com.simpleshare.common.exception.user;

import com.simpleshare.common.exception.ServiceException;

/**
 * 用户信息异常类
 *
 * @author SimpleShare
 */
public class UserException extends ServiceException {
    private static final long serialVersionUID = 1L;

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Integer code) {
        super(message, code);
    }
}