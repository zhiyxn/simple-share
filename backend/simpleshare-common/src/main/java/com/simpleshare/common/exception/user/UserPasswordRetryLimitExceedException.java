package com.simpleshare.common.exception.user;

/**
 * 用户错误最大次数异常类
 *
 * @author SimpleShare
 */
public class UserPasswordRetryLimitExceedException extends UserException {
    private static final long serialVersionUID = 1L;

    public UserPasswordRetryLimitExceedException(int retryLimitCount, int lockTime) {
        super(String.format("用户密码错误次数超限，账户锁定%s分钟", lockTime));
    }
}