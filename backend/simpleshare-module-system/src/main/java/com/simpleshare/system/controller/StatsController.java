package com.simpleshare.system.controller;

import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计信息控制器
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/system/stats")
@TenantIgnore
public class StatsController {

    /**
     * 获取统计信息
     */
    @GetMapping
    public AjaxResult getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("articles", 150);
        stats.put("users", 25);
        stats.put("comments", 89);
        stats.put("views", 1234);
        stats.put("likes", 456);

        return AjaxResult.success("获取统计信息成功", stats);
    }

    // 注意：不在此处添加以 /api 开头的路径，避免与前端代理路径重复
}