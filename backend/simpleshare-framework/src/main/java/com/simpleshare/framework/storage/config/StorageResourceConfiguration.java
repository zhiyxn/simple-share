package com.simpleshare.framework.storage.config;

import com.simpleshare.common.config.SimpleShareConfig;
import com.simpleshare.common.utils.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Exposes local storage directory over HTTP.
 */
@Configuration
public class StorageResourceConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = SimpleShareConfig.getUploadPath();
        if (StringUtils.isEmpty(uploadPath)) {
            return;
        }
        String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + location)
                .setCachePeriod(3600);
    }
}
