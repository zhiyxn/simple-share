package com.simpleshare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysTenantConfig;
import com.simpleshare.system.domain.vo.TenantProtectionSettings;
import com.simpleshare.system.mapper.SysTenantConfigMapper;
import com.simpleshare.system.service.ISysTenantConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysTenantConfigServiceImpl implements ISysTenantConfigService {

    private static final String DEFAULT_COPY_POLICY = "follow_article";
    private static final String POLICY_ALLOW = "global_allow";
    private static final String POLICY_DENY = "global_deny";

    private static final String KEY_COPY_POLICY = "copy_policy";
    private static final String KEY_DISABLE_COPY = "disable_copy";
    private static final String KEY_WATERMARK_ENABLED = "watermark_enabled";
    private static final String KEY_WATERMARK_TEXT = "watermark_text";
    private static final String KEY_WATERMARK_OPACITY = "watermark_opacity";

    private final SysTenantConfigMapper tenantConfigMapper;

    public SysTenantConfigServiceImpl(SysTenantConfigMapper tenantConfigMapper) {
        this.tenantConfigMapper = tenantConfigMapper;
    }

    @Override
    public Map<String, String> selectConfigMap(Long tenantId) {
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;
        List<SysTenantConfig> configs = tenantConfigMapper.selectList(new LambdaQueryWrapper<SysTenantConfig>()
                .eq(SysTenantConfig::getTenantId, effectiveTenantId));

        Map<String, String> result = new HashMap<>();
        for (SysTenantConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    public TenantProtectionSettings selectProtectionSettings(Long tenantId) {
        Map<String, String> configMap = selectConfigMap(tenantId);

        String copyPolicy = configMap.getOrDefault(KEY_COPY_POLICY, DEFAULT_COPY_POLICY);
        if (StringUtils.isEmpty(copyPolicy)) {
            copyPolicy = DEFAULT_COPY_POLICY;
        }

        // 向后兼容历史的 disable_copy 布尔值配置
        if (!configMap.containsKey(KEY_COPY_POLICY) && configMap.containsKey(KEY_DISABLE_COPY)) {
            boolean disableCopy = Boolean.parseBoolean(configMap.get(KEY_DISABLE_COPY));
            copyPolicy = disableCopy ? POLICY_DENY : DEFAULT_COPY_POLICY;
        }

        boolean watermarkEnabled = Boolean.parseBoolean(configMap.getOrDefault(KEY_WATERMARK_ENABLED, "true"));
        String watermarkText = configMap.getOrDefault(KEY_WATERMARK_TEXT, "SimpleShare");
        double watermarkOpacity = resolveOpacity(configMap.get(KEY_WATERMARK_OPACITY));

        return new TenantProtectionSettings(copyPolicy, watermarkEnabled, watermarkText, watermarkOpacity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateConfigs(Long tenantId, List<SysTenantConfig> configs) {
        if (tenantId == null || configs == null || configs.isEmpty()) {
            return;
        }

        Map<String, SysTenantConfig> existing = new HashMap<>();
        List<SysTenantConfig> stored = tenantConfigMapper.selectList(new LambdaQueryWrapper<SysTenantConfig>()
                .eq(SysTenantConfig::getTenantId, tenantId));
        for (SysTenantConfig item : stored) {
            existing.put(item.getConfigKey(), item);
        }

        Date now = new Date();
        for (SysTenantConfig config : configs) {
            if (config == null || StringUtils.isEmpty(config.getConfigKey())) {
                continue;
            }
            config.setTenantId(tenantId);
            config.setUpdateTime(now);

            SysTenantConfig old = existing.get(config.getConfigKey());
            if (old == null) {
                config.setCreateTime(now);
                tenantConfigMapper.insert(config);
            } else {
                config.setId(old.getId());
                if (config.getCreateTime() == null) {
                    config.setCreateTime(old.getCreateTime());
                }
                tenantConfigMapper.updateById(config);
            }
        }
    }

    private double resolveOpacity(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0.3D;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0) {
                return 0D;
            }
            if (parsed > 1) {
                return 1D;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return 0.3D;
        }
    }
}
