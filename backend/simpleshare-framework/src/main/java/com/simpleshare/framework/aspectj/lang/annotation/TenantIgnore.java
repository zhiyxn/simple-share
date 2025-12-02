package com.simpleshare.framework.aspectj.lang.annotation;

import java.lang.annotation.*;

/**
 * 租户忽略注解
 * 用于标识某些操作不需要进行租户隔离
 *
 * @author SimpleShare
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {
    /**
     * 是否忽略租户隔离
     */
    boolean value() default true;
}