package com.simpleshare.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 参数配置表 sys_config
 *
 * @author SimpleShare
 */
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 参数主键 */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /** 参数名称 */
    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    private String configName;

    /** 参数键名 */
    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    private String configKey;

    /** 参数键值 */
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    private String configValue;

    /** 系统内置（Y是 N否） */
    private String configType;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    @Override
    public String toString() {
        return "SysConfig{" +
                "configId=" + configId +
                ", configName='" + configName + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configType='" + configType + '\'' +
                '}';
    }
}