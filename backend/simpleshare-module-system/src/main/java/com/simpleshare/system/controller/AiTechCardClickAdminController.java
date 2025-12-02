package com.simpleshare.system.controller;

import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.system.domain.AiTechCardClick;
import com.simpleshare.system.dto.AiTechCardClickResponse;
import com.simpleshare.system.service.IAiTechCardClickService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端 AiTech 卡片点击统计接口
 */
@RestController
@RequestMapping("/system/admin/aitech/clicks")
public class AiTechCardClickAdminController {

    private final IAiTechCardClickService cardClickService;

    public AiTechCardClickAdminController(IAiTechCardClickService cardClickService) {
        this.cardClickService = cardClickService;
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:aitech:click:list')")
    public AjaxResult list(@RequestParam(value = "tenantId", required = false) Long tenantId) {
        List<AiTechCardClick> entities = cardClickService.listCardClicks(tenantId);
        List<AiTechCardClickResponse> responses = entities.stream()
                .map(AiTechCardClickResponse::from)
                .collect(Collectors.toList());
        return AjaxResult.success(responses);
    }

    @GetMapping("/top")
    @PreAuthorize("@ss.hasPermi('system:aitech:click:list')")
    public AjaxResult top(@RequestParam(value = "limit", defaultValue = "6") int limit,
                          @RequestParam(value = "tenantId", required = false) Long tenantId) {
        List<AiTechCardClick> entities = cardClickService.listTopCardClicks(tenantId, limit);
        List<AiTechCardClickResponse> responses = entities.stream()
                .map(AiTechCardClickResponse::from)
                .collect(Collectors.toList());
        return AjaxResult.success(responses);
    }
}
