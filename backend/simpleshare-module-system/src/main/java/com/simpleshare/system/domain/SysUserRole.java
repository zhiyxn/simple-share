package com.simpleshare.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author SimpleShare
 */
@TableName("sys_user_role")
public class SysUserRole {
    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "SysUserRole{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                '}';
    }
}