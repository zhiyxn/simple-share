package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.system.domain.SysEmailVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证码数据访问
 */
@Mapper
public interface SysEmailVerificationMapper extends BaseMapper<SysEmailVerification> {
}
