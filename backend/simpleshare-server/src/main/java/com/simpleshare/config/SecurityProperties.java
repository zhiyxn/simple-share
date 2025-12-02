package com.simpleshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全配置属性
 *
 * @author SimpleShare
 */
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 允许匿名访问的路径列表
     */
    private List<String> permitAll;

    public List<String> getPermitAll() {
        return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll;
    }
}