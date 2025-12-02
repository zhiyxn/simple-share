package com.simpleshare.system.service;

import com.simpleshare.system.domain.SysEmailVerification;
import com.simpleshare.system.enums.EmailVerificationScene;

/**
 * 邮箱验证码业务接口
 */
public interface IEmailVerificationService {

    /**
     * 发送验证码
     *
     * @param tenantId   租户ID
     * @param email      邮箱地址
     * @param scene      验证场景
     * @param ipAddress  请求IP
     * @param userAgent  用户代理
     * @return 验证码记录
     */
    SysEmailVerification sendCode(Long tenantId, String email, EmailVerificationScene scene, String ipAddress, String userAgent);

    /**
     * 校验验证码
     *
     * @param tenantId 租户ID
     * @param email    邮箱地址
     * @param scene    验证场景
     * @param code     验证码
     * @return 验证码记录
     */
    SysEmailVerification verifyCode(Long tenantId, String email, EmailVerificationScene scene, String code);

    /**
     * 根据令牌查找验证记录
     */
    SysEmailVerification findByToken(Long tenantId, String token);

    /**
     * 将验证码标记为已使用/失效
     */
    void markAsConsumed(Long id);
}
