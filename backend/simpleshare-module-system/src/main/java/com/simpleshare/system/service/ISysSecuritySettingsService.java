package com.simpleshare.system.service;

import com.simpleshare.system.domain.SysSecuritySettings;

/**
 * 系统安全设置服务
 */
public interface ISysSecuritySettingsService {

    /**
     * 查询租户的安全设置，不存在时返回默认配置
     */
    SysSecuritySettings loadOrDefault(Long tenantId);

    /**
     * 保存安全设置
     */
    void saveSettings(SysSecuritySettings settings);
}
