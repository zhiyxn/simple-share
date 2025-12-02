package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.system.domain.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志 Mapper
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
