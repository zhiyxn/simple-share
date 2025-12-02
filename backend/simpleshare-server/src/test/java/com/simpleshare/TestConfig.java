package com.simpleshare;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.simpleshare.system.config.MenuPermissionInitializer;

/**
 * 测试环境配置类
 * 排除在测试环境中不需要的组件
 */
@TestConfiguration
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                         classes = {
                             DatabaseFixRunner.class,
                             MenuPermissionInitializer.class
                         })
})
public class TestConfig {
    // 测试环境专用配置
}