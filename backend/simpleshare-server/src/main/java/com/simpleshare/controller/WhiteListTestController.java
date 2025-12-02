package com.simpleshare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 白名单测试控制器 - 用于验证白名单配置
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/system/fix")
public class WhiteListTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(WhiteListTestController.class);
    
    /**
     * 测试白名单访问
     */
    @GetMapping("/test")
    public Map<String, Object> testAccess() {
        logger.info("Test endpoint accessed successfully");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "白名单测试成功 - 可以匿名访问");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 测试需要认证的接口
     */
    @GetMapping("/auth-test")
    public Map<String, Object> authTest() {
        logger.info("Auth test endpoint accessed");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "认证测试成功 - 需要登录才能访问");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 测试POST方法白名单
     */
    @PostMapping("/test")
    public Map<String, Object> testPost(@RequestBody Map<String, Object> data) {
        logger.info("Test POST endpoint accessed with data: {}", data);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "POST白名单测试成功");
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 测试公共文章接口
     */
    @GetMapping("/articles")
    public Map<String, Object> testArticles() {
        logger.info("Test articles endpoint accessed");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "文章列表接口测试成功");
        result.put("data", new String[]{"文章1", "文章2", "文章3"});
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 测试公共分类接口
     */
    @GetMapping("/categories")
    public Map<String, Object> testCategories() {
        logger.info("Test categories endpoint accessed");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "分类列表接口测试成功");
        result.put("data", new String[]{"技术", "生活", "旅行"});
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}