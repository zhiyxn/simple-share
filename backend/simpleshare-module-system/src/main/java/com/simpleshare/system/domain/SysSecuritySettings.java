package com.simpleshare.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

/**
 * 系统安全设置
 */
@TableName("sys_security_settings")
public class SysSecuritySettings extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Integer passwordMinLength;

    private String passwordRequireUppercase;

    private String passwordRequireLowercase;

    private String passwordRequireNumber;

    private String passwordRequireSymbol;

    private Integer passwordExpireDays;

    private Integer passwordHistoryCount;

    private Integer loginMaxAttempts;

    private Integer lockoutDurationMinutes;

    private String autoUnlockEnabled;

    private Integer sessionTimeoutMinutes;

    private String loginCaptchaEnabled;

    private String rememberMeEnabled;

    private String twoFactorEnabled;

    private String twoFactorMethods;

    @TableField("ip_whitelist")
    private String ipWhitelist;

    @TableField("ip_blacklist")
    private String ipBlacklist;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public String getPasswordRequireUppercase() {
        return passwordRequireUppercase;
    }

    public void setPasswordRequireUppercase(String passwordRequireUppercase) {
        this.passwordRequireUppercase = passwordRequireUppercase;
    }

    public String getPasswordRequireLowercase() {
        return passwordRequireLowercase;
    }

    public void setPasswordRequireLowercase(String passwordRequireLowercase) {
        this.passwordRequireLowercase = passwordRequireLowercase;
    }

    public String getPasswordRequireNumber() {
        return passwordRequireNumber;
    }

    public void setPasswordRequireNumber(String passwordRequireNumber) {
        this.passwordRequireNumber = passwordRequireNumber;
    }

    public String getPasswordRequireSymbol() {
        return passwordRequireSymbol;
    }

    public void setPasswordRequireSymbol(String passwordRequireSymbol) {
        this.passwordRequireSymbol = passwordRequireSymbol;
    }

    public Integer getPasswordExpireDays() {
        return passwordExpireDays;
    }

    public void setPasswordExpireDays(Integer passwordExpireDays) {
        this.passwordExpireDays = passwordExpireDays;
    }

    public Integer getPasswordHistoryCount() {
        return passwordHistoryCount;
    }

    public void setPasswordHistoryCount(Integer passwordHistoryCount) {
        this.passwordHistoryCount = passwordHistoryCount;
    }

    public Integer getLoginMaxAttempts() {
        return loginMaxAttempts;
    }

    public void setLoginMaxAttempts(Integer loginMaxAttempts) {
        this.loginMaxAttempts = loginMaxAttempts;
    }

    public Integer getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(Integer lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public String getAutoUnlockEnabled() {
        return autoUnlockEnabled;
    }

    public void setAutoUnlockEnabled(String autoUnlockEnabled) {
        this.autoUnlockEnabled = autoUnlockEnabled;
    }

    public Integer getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public String getLoginCaptchaEnabled() {
        return loginCaptchaEnabled;
    }

    public void setLoginCaptchaEnabled(String loginCaptchaEnabled) {
        this.loginCaptchaEnabled = loginCaptchaEnabled;
    }

    public String getRememberMeEnabled() {
        return rememberMeEnabled;
    }

    public void setRememberMeEnabled(String rememberMeEnabled) {
        this.rememberMeEnabled = rememberMeEnabled;
    }

    public String getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(String twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorMethods() {
        return twoFactorMethods;
    }

    public void setTwoFactorMethods(String twoFactorMethods) {
        this.twoFactorMethods = twoFactorMethods;
    }

    public String getIpWhitelist() {
        return ipWhitelist;
    }

    public void setIpWhitelist(String ipWhitelist) {
        this.ipWhitelist = ipWhitelist;
    }

    public String getIpBlacklist() {
        return ipBlacklist;
    }

    public void setIpBlacklist(String ipBlacklist) {
        this.ipBlacklist = ipBlacklist;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
