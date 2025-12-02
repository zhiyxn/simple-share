package com.simpleshare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.system.domain.SysSecuritySettings;
import com.simpleshare.system.mapper.SysSecuritySettingsMapper;
import com.simpleshare.system.service.ISysSecuritySettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统安全设置服务实现
 */
@Service
public class SysSecuritySettingsServiceImpl implements ISysSecuritySettingsService {

    private final SysSecuritySettingsMapper securitySettingsMapper;

    public SysSecuritySettingsServiceImpl(SysSecuritySettingsMapper securitySettingsMapper) {
        this.securitySettingsMapper = securitySettingsMapper;
    }

    @Override
    public SysSecuritySettings loadOrDefault(Long tenantId) {
        SysSecuritySettings settings = securitySettingsMapper.selectOne(new LambdaQueryWrapper<SysSecuritySettings>()
                .eq(SysSecuritySettings::getTenantId, tenantId));
        if (settings != null) {
            return settings;
        }
        return buildDefault(tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSettings(SysSecuritySettings settings) {
        SysSecuritySettings existing = securitySettingsMapper.selectOne(new LambdaQueryWrapper<SysSecuritySettings>()
                .eq(SysSecuritySettings::getTenantId, settings.getTenantId()));

        Long userId = SecurityUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            settings.setCreateBy(userId);
            settings.setUpdateBy(userId);
            settings.setCreateTime(now);
            settings.setUpdateTime(now);
            securitySettingsMapper.insert(settings);
        } else {
            settings.setId(existing.getId());
            settings.setCreateBy(existing.getCreateBy());
            settings.setCreateTime(existing.getCreateTime());
            settings.setUpdateBy(userId);
            settings.setUpdateTime(now);
            securitySettingsMapper.updateById(settings);
        }
    }

    private SysSecuritySettings buildDefault(Long tenantId) {
        SysSecuritySettings settings = new SysSecuritySettings();
        settings.setTenantId(tenantId);
        settings.setPasswordMinLength(8);
        settings.setPasswordRequireUppercase("0");
        settings.setPasswordRequireLowercase("1");
        settings.setPasswordRequireNumber("1");
        settings.setPasswordRequireSymbol("0");
        settings.setPasswordExpireDays(null);
        settings.setPasswordHistoryCount(5);
        settings.setLoginMaxAttempts(5);
        settings.setLockoutDurationMinutes(30);
        settings.setAutoUnlockEnabled("1");
        settings.setSessionTimeoutMinutes(60);
        settings.setLoginCaptchaEnabled("0");
        settings.setRememberMeEnabled("1");
        settings.setTwoFactorEnabled("0");
        settings.setTwoFactorMethods("email");
        settings.setIpWhitelist(null);
        settings.setIpBlacklist(null);
        return settings;
    }
}
