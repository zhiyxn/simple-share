package com.simpleshare.system.service;

import com.simpleshare.system.domain.SysOperationLog;
import com.simpleshare.system.dto.OperationLogQueryRequest;

import java.util.List;

/**
 * 系统操作日志服务
 */
public interface ISysOperationLogService {

    /**
     * 记录操作日志
     */
    void record(SysOperationLog log);

    /**
     * 查询操作日志列表
     */
    List<SysOperationLog> selectOperationLogList(OperationLogQueryRequest query);

    /**
     * 清空所有操作日志
     */
    void cleanOperationLog();
}
