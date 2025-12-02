package com.simpleshare.system.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 系统安全设置请求
 */
public class SecuritySettingsRequest {

    @Valid
    @NotNull(message = "密码策略不能为空")
    private PasswordPolicy passwordPolicy;

    @Valid
    @NotNull(message = "登录防护不能为空")
    private LoginProtection loginProtection;

    @Valid
    @NotNull(message = "访问控制不能为空")
    private AccessControl accessControl;

    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicy;
    }

    public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }

    public LoginProtection getLoginProtection() {
        return loginProtection;
    }

    public void setLoginProtection(LoginProtection loginProtection) {
        this.loginProtection = loginProtection;
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    public static class PasswordPolicy {
        @Min(value = 4, message = "密码最小长度不能小于4")
        private Integer minLength;
        private Boolean requireUppercase;
        private Boolean requireLowercase;
        private Boolean requireNumber;
        private Boolean requireSymbol;
        private Integer expireDays;
        private Integer historyCount;

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public Boolean getRequireUppercase() {
            return requireUppercase;
        }

        public void setRequireUppercase(Boolean requireUppercase) {
            this.requireUppercase = requireUppercase;
        }

        public Boolean getRequireLowercase() {
            return requireLowercase;
        }

        public void setRequireLowercase(Boolean requireLowercase) {
            this.requireLowercase = requireLowercase;
        }

        public Boolean getRequireNumber() {
            return requireNumber;
        }

        public void setRequireNumber(Boolean requireNumber) {
            this.requireNumber = requireNumber;
        }

        public Boolean getRequireSymbol() {
            return requireSymbol;
        }

        public void setRequireSymbol(Boolean requireSymbol) {
            this.requireSymbol = requireSymbol;
        }

        public Integer getExpireDays() {
            return expireDays;
        }

        public void setExpireDays(Integer expireDays) {
            this.expireDays = expireDays;
        }

        public Integer getHistoryCount() {
            return historyCount;
        }

        public void setHistoryCount(Integer historyCount) {
            this.historyCount = historyCount;
        }
    }

    public static class LoginProtection {
        @Min(value = 1, message = "最大尝试次数不能小于1")
        private Integer maxAttempts;
        private Integer lockoutMinutes;
        private Boolean autoUnlock;
        private Boolean captchaEnabled;
        private Integer sessionTimeoutMinutes;
        private Boolean rememberMeEnabled;
        private Boolean twoFactorEnabled;
        private List<@Size(min = 2, max = 32) String> twoFactorMethods;

        public Integer getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Integer getLockoutMinutes() {
            return lockoutMinutes;
        }

        public void setLockoutMinutes(Integer lockoutMinutes) {
            this.lockoutMinutes = lockoutMinutes;
        }

        public Boolean getAutoUnlock() {
            return autoUnlock;
        }

        public void setAutoUnlock(Boolean autoUnlock) {
            this.autoUnlock = autoUnlock;
        }

        public Boolean getCaptchaEnabled() {
            return captchaEnabled;
        }

        public void setCaptchaEnabled(Boolean captchaEnabled) {
            this.captchaEnabled = captchaEnabled;
        }

        public Integer getSessionTimeoutMinutes() {
            return sessionTimeoutMinutes;
        }

        public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
            this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        }

        public Boolean getRememberMeEnabled() {
            return rememberMeEnabled;
        }

        public void setRememberMeEnabled(Boolean rememberMeEnabled) {
            this.rememberMeEnabled = rememberMeEnabled;
        }

        public Boolean getTwoFactorEnabled() {
            return twoFactorEnabled;
        }

        public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
            this.twoFactorEnabled = twoFactorEnabled;
        }

        public List<String> getTwoFactorMethods() {
            return twoFactorMethods;
        }

        public void setTwoFactorMethods(List<String> twoFactorMethods) {
            this.twoFactorMethods = twoFactorMethods;
        }
    }

    public static class AccessControl {
        private List<String> ipWhitelist;
        private List<String> ipBlacklist;

        public List<String> getIpWhitelist() {
            return ipWhitelist;
        }

        public void setIpWhitelist(List<String> ipWhitelist) {
            this.ipWhitelist = ipWhitelist;
        }

        public List<String> getIpBlacklist() {
            return ipBlacklist;
        }

        public void setIpBlacklist(List<String> ipBlacklist) {
            this.ipBlacklist = ipBlacklist;
        }
    }
}
