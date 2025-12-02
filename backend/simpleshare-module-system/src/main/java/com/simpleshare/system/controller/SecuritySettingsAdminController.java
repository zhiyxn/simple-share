package com.simpleshare.system.controller;

import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysSecuritySettings;
import com.simpleshare.system.dto.SecuritySettingsRequest;
import com.simpleshare.system.dto.SecuritySettingsResponse;
import com.simpleshare.system.service.ISysSecuritySettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统安全设置管理接口
 */
@RestController
@RequestMapping("/system/admin/security-settings")
public class SecuritySettingsAdminController extends BaseController {

    private final ISysSecuritySettingsService securitySettingsService;

    public SecuritySettingsAdminController(ISysSecuritySettingsService securitySettingsService) {
        this.securitySettingsService = securitySettingsService;
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:security:query')")
    public AjaxResult getSettings() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null || tenantId <= 0) {
            tenantId = 1L;
        }
        SysSecuritySettings settings = securitySettingsService.loadOrDefault(tenantId);
        return AjaxResult.success(convertResponse(settings));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('system:security:edit')")
    public AjaxResult updateSettings(@Validated @RequestBody SecuritySettingsRequest request) {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null || tenantId <= 0) {
            tenantId = 1L;
        }

        SysSecuritySettings entity = new SysSecuritySettings();
        entity.setTenantId(tenantId);

        SecuritySettingsRequest.PasswordPolicy passwordPolicy = request.getPasswordPolicy();
        entity.setPasswordMinLength(passwordPolicy.getMinLength());
        entity.setPasswordRequireUppercase(toFlag(passwordPolicy.getRequireUppercase()));
        entity.setPasswordRequireLowercase(toFlag(passwordPolicy.getRequireLowercase()));
        entity.setPasswordRequireNumber(toFlag(passwordPolicy.getRequireNumber()));
        entity.setPasswordRequireSymbol(toFlag(passwordPolicy.getRequireSymbol()));
        entity.setPasswordExpireDays(passwordPolicy.getExpireDays());
        entity.setPasswordHistoryCount(passwordPolicy.getHistoryCount());

        SecuritySettingsRequest.LoginProtection loginProtection = request.getLoginProtection();
        entity.setLoginMaxAttempts(loginProtection.getMaxAttempts());
        entity.setLockoutDurationMinutes(loginProtection.getLockoutMinutes());
        entity.setAutoUnlockEnabled(toFlag(loginProtection.getAutoUnlock()));
        entity.setLoginCaptchaEnabled(toFlag(loginProtection.getCaptchaEnabled()));
        entity.setSessionTimeoutMinutes(loginProtection.getSessionTimeoutMinutes());
        entity.setRememberMeEnabled(toFlag(loginProtection.getRememberMeEnabled()));
        entity.setTwoFactorEnabled(toFlag(loginProtection.getTwoFactorEnabled()));
        entity.setTwoFactorMethods(loginProtection.getTwoFactorMethods() != null
                ? String.join(",", loginProtection.getTwoFactorMethods())
                : null);

        SecuritySettingsRequest.AccessControl accessControl = request.getAccessControl();
        entity.setIpWhitelist(listToString(accessControl.getIpWhitelist()));
        entity.setIpBlacklist(listToString(accessControl.getIpBlacklist()));

        securitySettingsService.saveSettings(entity);
        return AjaxResult.success("安全设置已更新");
    }

    private SecuritySettingsResponse convertResponse(SysSecuritySettings settings) {
        SecuritySettingsResponse response = new SecuritySettingsResponse();

        SecuritySettingsResponse.PasswordPolicy passwordPolicy = new SecuritySettingsResponse.PasswordPolicy();
        passwordPolicy.setMinLength(settings.getPasswordMinLength());
        passwordPolicy.setRequireUppercase(isTrue(settings.getPasswordRequireUppercase()));
        passwordPolicy.setRequireLowercase(isTrue(settings.getPasswordRequireLowercase()));
        passwordPolicy.setRequireNumber(isTrue(settings.getPasswordRequireNumber()));
        passwordPolicy.setRequireSymbol(isTrue(settings.getPasswordRequireSymbol()));
        passwordPolicy.setExpireDays(settings.getPasswordExpireDays());
        passwordPolicy.setHistoryCount(settings.getPasswordHistoryCount());
        response.setPasswordPolicy(passwordPolicy);

        SecuritySettingsResponse.LoginProtection loginProtection = new SecuritySettingsResponse.LoginProtection();
        loginProtection.setMaxAttempts(settings.getLoginMaxAttempts());
        loginProtection.setLockoutMinutes(settings.getLockoutDurationMinutes());
        loginProtection.setAutoUnlock(isTrue(settings.getAutoUnlockEnabled()));
        loginProtection.setCaptchaEnabled(isTrue(settings.getLoginCaptchaEnabled()));
        loginProtection.setSessionTimeoutMinutes(settings.getSessionTimeoutMinutes());
        loginProtection.setRememberMeEnabled(isTrue(settings.getRememberMeEnabled()));
        loginProtection.setTwoFactorEnabled(isTrue(settings.getTwoFactorEnabled()));
        loginProtection.setTwoFactorMethods(splitToList(settings.getTwoFactorMethods()));
        response.setLoginProtection(loginProtection);

        SecuritySettingsResponse.AccessControl accessControl = new SecuritySettingsResponse.AccessControl();
        accessControl.setIpWhitelist(splitLines(settings.getIpWhitelist()));
        accessControl.setIpBlacklist(splitLines(settings.getIpBlacklist()));
        response.setAccessControl(accessControl);

        return response;
    }

    private String toFlag(Boolean value) {
        return Boolean.TRUE.equals(value) ? "1" : "0";
    }

    private boolean isTrue(String flag) {
        return "1".equals(flag);
    }

    private List<String> splitToList(String value) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private List<String> splitLines(String value) {
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split("\n"))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private String listToString(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining("\n"));
    }
}
