package com.simpleshare.infra.framework.file.config;

import com.simpleshare.infra.framework.file.core.client.FileClientFactory;
import com.simpleshare.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author simple
 */
@Configuration
public class SimpleShareFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}