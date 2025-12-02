package com.simpleshare.framework.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger配置
 *
 * @author SimpleShare
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(name = "springfox.documentation.spring.web.plugins.Docket")
public class SwaggerConfig {

    /** 是否开启swagger */
    @Value("${swagger.enabled:true}")
    private boolean enabled;

    /** 请求前缀 */
    @Value("${swagger.pathMapping:/dev-api}")
    private String pathMapping;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                // 是否启用Swagger
                .enable(enabled)
                // 用来创建该API的基本信息，展示在文档的页面中（自定义展示的信息）
                .apiInfo(apiInfo())
                // 设置哪些接口暴露给Swagger展示
                .select()
                // 扫描所有有注解的api，用这种方式更灵活
                .apis(RequestHandlerSelectors.basePackage("com.simpleshare"))
                // 扫描所有 .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 扫描指定包中的controller .apis(RequestHandlerSelectors.basePackage("com.simpleshare.*.controller"))
                // 过滤路径
                .paths(PathSelectors.any())
                .build()
                .pathMapping(pathMapping);
    }

    /**
     * 添加摘要信息
     */
    private ApiInfo apiInfo() {
        // 用ApiInfoBuilder进行定制
        return new ApiInfoBuilder()
                // 设置标题
                .title("SimpleShare API文档")
                // 描述
                .description("SimpleShare后台管理系统API文档")
                // 作者信息
                .contact(new Contact("SimpleShare", "https://github.com/simpleshare", "admin@simpleshare.com"))
                // 版本号
                .version("1.0.0")
                .build();
    }
}