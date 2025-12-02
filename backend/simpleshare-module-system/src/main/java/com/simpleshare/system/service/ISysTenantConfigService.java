package com.simpleshare.system.service;

import com.simpleshare.system.domain.SysTenantConfig;
import com.simpleshare.system.domain.vo.TenantProtectionSettings;

import java.util.List;
import java.util.Map;

/**
 * 租户配置服务。
 */
public interface ISysTenantConfigService {

    /**
     * 查询租户的配置键值对。
     *
     * @param tenantId 租户ID
     * @return 配置映射
     */
    Map<String, String> selectConfigMap(Long tenantId);

    /**
     * 查询租户层的保护策略配置。
     *
     * @param tenantId 租户ID
     * @return 保护配置
     */
    TenantProtectionSettings selectProtectionSettings(Long tenantId);

    /**
     * 批量保存或更新配置。
     *
     * @param tenantId 租户ID
     * @param configs  配置列表
     */
    void saveOrUpdateConfigs(Long tenantId, List<SysTenantConfig> configs);
}

