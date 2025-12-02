package com.simpleshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for operation log interceptor.
 */
@Component
@ConfigurationProperties(prefix = "operation-log")
public class OperationLogProperties {

    /**
     * Paths that should be excluded from operation log interceptor.
     */
    private List<String> excludePatterns = new ArrayList<>();

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
}
