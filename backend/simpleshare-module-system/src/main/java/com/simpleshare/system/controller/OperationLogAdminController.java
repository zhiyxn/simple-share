package com.simpleshare.system.controller;

import com.github.pagehelper.PageInfo;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.model.PageResponse;
import com.simpleshare.system.domain.SysOperationLog;
import com.simpleshare.system.dto.OperationLogQueryRequest;
import com.simpleshare.system.dto.OperationLogResponse;
import com.simpleshare.system.service.ISysOperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志管理
 */
@RestController
@RequestMapping("/system/admin/operation-logs")
public class OperationLogAdminController extends BaseController {

    private final ISysOperationLogService operationLogService;

    public OperationLogAdminController(ISysOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 分页查询操作日志
     */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:operation-log:list')")
    public AjaxResult list(OperationLogQueryRequest query) {
        List<SysOperationLog> logs = operationLogService.selectOperationLogList(query);
        PageInfo<SysOperationLog> pageInfo = new PageInfo<>(logs);
        List<OperationLogResponse> items = logs.stream()
                .map(OperationLogResponse::from)
                .collect(Collectors.toList());
        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("@ss.hasPermi('system:operation-log:clean')")
    public AjaxResult clean() {
        operationLogService.cleanOperationLog();
        return AjaxResult.success("操作日志已清空");
    }
}
