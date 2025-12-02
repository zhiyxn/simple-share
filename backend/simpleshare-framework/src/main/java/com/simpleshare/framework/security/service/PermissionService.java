package com.simpleshare.framework.security.service;

import com.simpleshare.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * 权限验证服务
 *
 * @author SimpleShare
 */
@Service("ss")
public class PermissionService {

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission) {
        return SecurityUtils.hasPermi(permission);
    }

    /**
     * 验证用户是否不具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public boolean lacksPermi(String permission) {
        return SecurityUtils.lacksPermi(permission);
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 以逗号分隔的权限列表
     * @return 用户是否具有以下任意一个权限
     */
    public boolean hasAnyPermi(String permissions) {
        return SecurityUtils.hasAnyPermi(permissions);
    }

    /**
     * 验证用户是否具有某个角色
     *
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public boolean hasRole(String role) {
        return SecurityUtils.hasRole(role);
    }

    /**
     * 验证用户是否不具备某角色
     *
     * @param role 角色字符串
     * @return 用户是否不具备某角色
     */
    public boolean lacksRole(String role) {
        return SecurityUtils.lacksRole(role);
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 以逗号分隔的角色列表
     * @return 用户是否具有以下任意一个角色
     */
    public boolean hasAnyRole(String roles) {
        return SecurityUtils.hasAnyRole(roles);
    }
}