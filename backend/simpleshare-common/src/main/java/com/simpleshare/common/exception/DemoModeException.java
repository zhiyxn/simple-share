package com.simpleshare.common.exception;

/**
 * 演示模式异常
 *
 * @author SimpleShare
 */
public class DemoModeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DemoModeException() {
    }

    public DemoModeException(String message) {
        super(message);
    }

    public DemoModeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemoModeException(Throwable cause) {
        super(cause);
    }

    public DemoModeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}