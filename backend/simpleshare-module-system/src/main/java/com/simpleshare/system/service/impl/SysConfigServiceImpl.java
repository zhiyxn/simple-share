package com.simpleshare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysConfig;
import com.simpleshare.system.mapper.SysConfigMapper;
import com.simpleshare.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author SimpleShare
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService {

    @Autowired
    private SysConfigMapper configMapper;

    @Override
    public SysConfig selectConfigById(Long configId) {
        return configMapper.selectById(configId);
    }

    @Override
    public String selectConfigByKey(String configKey) {
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig result = configMapper.selectOne(queryWrapper);
        return StringUtils.isNotNull(result) ? result.getConfigValue() : "";
    }

    @Override
    public boolean selectCaptchaEnabled() {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled)) {
            return true;
        }
        return "true".equals(captchaEnabled);
    }

    @Override
    public List<SysConfig> selectConfigList(SysConfig config) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(config.getConfigName())) {
            queryWrapper.like(SysConfig::getConfigName, config.getConfigName());
        }
        if (StringUtils.isNotEmpty(config.getConfigKey())) {
            queryWrapper.like(SysConfig::getConfigKey, config.getConfigKey());
        }
        if (StringUtils.isNotEmpty(config.getConfigType())) {
            queryWrapper.eq(SysConfig::getConfigType, config.getConfigType());
        }
        return configMapper.selectList(queryWrapper);
    }

    @Override
    public int insertConfig(SysConfig config) {
        return configMapper.insert(config);
    }

    @Override
    public int updateConfig(SysConfig config) {
        return configMapper.updateById(config);
    }

    @Override
    public void deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            configMapper.deleteById(configId);
        }
    }

    @Override
    public void loadingConfigCache() {
        // 实际项目中这里应该加载配置到缓存
    }

    @Override
    public void clearConfigCache() {
        // 实际项目中这里应该清除缓存
    }

    @Override
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    @Override
    public boolean checkConfigKeyUnique(SysConfig config) {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        SysConfig info = configMapper.selectOne(queryWrapper);
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue()) {
            return false;
        }
        return true;
    }
}