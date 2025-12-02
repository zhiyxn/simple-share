package com.simpleshare.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.simpleshare.common.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 *
 * @author SimpleShare
 */
@Component
public class CommonMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 创建者 - 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getUserIdSafely();
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        }

        // 更新者 - 获取当前登录用户ID
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 更新者 - 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getUserIdSafely();
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        }
    }
}