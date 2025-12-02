package com.simpleshare.common.utils;

import com.simpleshare.common.constant.HttpStatus;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.common.exception.ServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.PatternMatchUtils;

import java.util.Collection;
import java.util.Set;

/**
 * 安全服务工具类
 *
 * @author SimpleShare
 */
public class SecurityUtils {
    /**
     * 用户ID
     **/
    public static Long getUserId() {
        try {
            LoginUser loginUser = getLoginUser();
            return loginUser != null ? loginUser.getUserId() : null;
        } catch (Exception e) {
            throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取部门ID
     **/
    public static Long getDeptId() {
        try {
            LoginUser loginUser = getLoginUser();
            return loginUser != null ? loginUser.getDeptId() : null;
        } catch (Exception e) {
            throw new ServiceException("获取部门ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取租户ID
     **/
    public static Long getTenantId() {
        try {
            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                return null;
            }
            SysUser user = loginUser.getUser();
            return user != null ? user.getTenantId() : null;
        } catch (Exception e) {
            throw new ServiceException("获取租户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户账户
     **/
    public static String getUsername() {
        try {
            LoginUser loginUser = getLoginUser();
            return loginUser != null ? loginUser.getUsername() : null;
        } catch (Exception e) {
            throw new ServiceException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            Authentication authentication = getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                return (LoginUser) principal;
            }
            
            return null;
        } catch (Exception e) {
            throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 安全获取用户信息，不抛出异常
     **/
    public static LoginUser getLoginUserSafely() {
        try {
            Authentication authentication = getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                return (LoginUser) principal;
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全获取用户ID，不抛出异常
     **/
    public static Long getUserIdSafely() {
        try {
            LoginUser loginUser = getLoginUserSafely();
            return loginUser != null ? loginUser.getUserId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查用户是否已登录
     **/
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   authentication.getPrincipal() instanceof LoginUser;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public static boolean hasPermi(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (StringUtils.isNull(loginUser) || StringUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 验证用户是否不具备某权限，与 hasPermi逻辑相反
     *
     * @param permission 权限字符串
     * @return 用户是否不具备某权限
     */
    public static boolean lacksPermi(String permission) {
        return hasPermi(permission) != true;
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 以 PERMISSION_DELIMETER 为分隔符的权限列表
     * @return 用户是否具有以下任意一个权限
     */
    public static boolean hasAnyPermi(String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (StringUtils.isNull(loginUser) || StringUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(",")) {
            if (permission != null && hasPermissions(authorities, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有某个角色
     *
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public static boolean hasRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (StringUtils.isNull(loginUser) || StringUtils.isEmpty(loginUser.getUser().getRoles())) {
            return false;
        }
        for (com.simpleshare.common.core.domain.entity.SysRole sysRole : loginUser.getUser().getRoles()) {
            String roleKey = sysRole.getRoleKey();
            if ("admin".equals(roleKey) || roleKey.equals(StringUtils.trim(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否不具备某角色，与 isRole逻辑相反。
     *
     * @param role 角色名称
     * @return 用户是否不具备某角色
     */
    public static boolean lacksRole(String role) {
        return hasRole(role) != true;
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 以 ROLE_NAMES_DELIMETER 为分隔符的角色列表
     * @return 用户是否具有以下任意一个角色
     */
    public static boolean hasAnyRole(String roles) {
        if (StringUtils.isEmpty(roles)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (StringUtils.isNull(loginUser) || StringUtils.isEmpty(loginUser.getUser().getRoles())) {
            return false;
        }
        for (String role : roles.split(",")) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     *
     * @param authorities 权限列表
     * @param permission  权限字符串
     * @return 用户是否具备某权限
     */
    private static boolean hasPermissions(Collection<String> authorities, String permission) {
        return authorities.stream().filter(StringUtils::isNotEmpty)
                .anyMatch(x -> "*:*:*".equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }
}
