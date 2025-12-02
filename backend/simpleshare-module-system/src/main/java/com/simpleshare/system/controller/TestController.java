package com.simpleshare.system.controller;

import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/system/public")
@TenantIgnore
public class TestController {

    @GetMapping("/test")
    public AjaxResult publicTest() {
        return AjaxResult.success("Public endpoint works!");
    }
}