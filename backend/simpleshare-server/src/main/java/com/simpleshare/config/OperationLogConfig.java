package com.simpleshare.config;

import com.simpleshare.logging.OperationLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 操作日志拦截器配置
 */
@Configuration
public class OperationLogConfig implements WebMvcConfigurer {

    private final OperationLogInterceptor operationLogInterceptor;
    private final OperationLogProperties operationLogProperties;

    public OperationLogConfig(OperationLogInterceptor operationLogInterceptor,
                              OperationLogProperties operationLogProperties) {
        this.operationLogInterceptor = operationLogInterceptor;
        this.operationLogProperties = operationLogProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/**");

        List<String> excludePatterns = operationLogProperties.getExcludePatterns();
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            registration.excludePathPatterns(excludePatterns.toArray(new String[0]));
        }
    }
}
