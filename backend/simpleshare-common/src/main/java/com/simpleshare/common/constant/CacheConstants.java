package com.simpleshare.common.constant;

/**
 * 缓存的key 常量
 *
 * @author SimpleShare
 */
public class CacheConstants {
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 刷新令牌 redis key
     */
    public static final String REFRESH_TOKEN_KEY = "refresh_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 租户信息缓存 redis key
     */
    public static final String TENANT_INFO_KEY = "tenant_info:";

    /**
     * 用户权限缓存 redis key
     */
    public static final String USER_PERMISSIONS_KEY = "user_permissions:";

    /**
     * 用户角色缓存 redis key
     */
    public static final String USER_ROLES_KEY = "user_roles:";
}
