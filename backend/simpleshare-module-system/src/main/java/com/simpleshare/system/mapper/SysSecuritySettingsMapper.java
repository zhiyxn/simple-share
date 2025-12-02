package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.system.domain.SysSecuritySettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统安全设置数据访问
 */
@Mapper
public interface SysSecuritySettingsMapper extends BaseMapper<SysSecuritySettings> {
}
