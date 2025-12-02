package com.simpleshare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysOperationLog;
import com.simpleshare.system.dto.OperationLogQueryRequest;
import com.simpleshare.system.mapper.SysOperationLogMapper;
import com.simpleshare.system.service.ISysOperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
public class SysOperationLogServiceImpl implements ISysOperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    public SysOperationLogServiceImpl(SysOperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void record(SysOperationLog log) {
        if (log == null) {
            return;
        }
        operationLogMapper.insert(log);
    }

    @Override
    public List<SysOperationLog> selectOperationLogList(OperationLogQueryRequest query) {
        int page = query.getPage();
        int pageSize = query.getPageSize();
        PageHelper.startPage(page, pageSize);

        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getRequestUri())) {
            wrapper.like(SysOperationLog::getRequestUri, query.getRequestUri().trim());
        }
        if (StringUtils.isNotBlank(query.getRequestMethod())) {
            wrapper.eq(SysOperationLog::getRequestMethod, query.getRequestMethod().trim().toUpperCase());
        }
        if (StringUtils.isNotBlank(query.getOperatorName())) {
            wrapper.like(SysOperationLog::getOperatorName, query.getOperatorName().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysOperationLog::getStatus, query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getBusinessType())) {
            wrapper.eq(SysOperationLog::getBusinessType, query.getBusinessType().trim());
        }
        LocalDateTime startTime = query.getStartTime();
        if (startTime != null) {
            wrapper.ge(SysOperationLog::getCreateTime, startTime);
        }
        LocalDateTime endTime = query.getEndTime();
        if (endTime != null) {
            wrapper.le(SysOperationLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public void cleanOperationLog() {
        operationLogMapper.delete(null);
    }
}
