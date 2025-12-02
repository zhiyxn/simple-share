package com.simpleshare.system.controller;

import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.system.domain.AiTechCardClick;
import com.simpleshare.system.dto.AiTechCardClickRequest;
import com.simpleshare.system.dto.AiTechCardClickResponse;
import com.simpleshare.system.service.IAiTechCardClickService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 前台 AiTech 卡片点击统计接口
 */
@RestController
@RequestMapping("/aitech")
@Validated
public class AiTechCardClickController {

    private final IAiTechCardClickService cardClickService;

    public AiTechCardClickController(IAiTechCardClickService cardClickService) {
        this.cardClickService = cardClickService;
    }

    @PostMapping("/cards/click")
    public AjaxResult recordClick(@Valid @RequestBody AiTechCardClickRequest request) {
        AiTechCardClick result = cardClickService.recordClick(request);
        return AjaxResult.success("点击记录成功", AiTechCardClickResponse.from(result));
    }
}
