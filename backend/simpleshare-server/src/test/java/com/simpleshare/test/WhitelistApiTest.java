package com.simpleshare.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 白名单配置测试：校验安全配置中允许匿名访问的路径。
 */
class WhitelistApiTest {

    private static List<String> permitAllPaths = Collections.emptyList();

    @BeforeAll
    static void loadPermitAllPaths() throws IOException {
        ClassPathResource resource = new ClassPathResource("application.yml");
        if (!resource.exists()) {
            throw new IllegalStateException("无法找到 application.yml 文件，白名单配置测试无法执行");
        }

        try (InputStream inputStream = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(inputStream);
            permitAllPaths = extractPermitAllPaths(data);
        }
    }

    @Test
    void whitelistShouldNotBeEmpty() {
        assertThat(permitAllPaths)
                .as("白名单配置不能为空")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void whitelistShouldContainCoreAuthEndpoints() {
        assertThat(permitAllPaths)
                .as("白名单应包含核心认证相关接口")
                .contains(
                        "POST:/api/login",
                        "POST:/api/register",
                        "POST:/api/auth/login",
                        "POST:/api/auth/register",
                        "GET:/api/auth/user-info",
                        "GET:/api/user-info"
                );
    }

    @Test
    void whitelistShouldCoverPublicTenantEndpoints() {
        assertThat(permitAllPaths)
                .as("白名单应允许租户公开查询接口")
                .contains(
                        "GET:/public/tenants/by-domain",
                        "GET:/public/tenants/**/config",
                        "GET:/public/tenants/active",
                        "GET:/public/tenants/check-domain",
                        "GET:/tenants/by-domain"
                );
    }

    @Test
    void whitelistShouldIncludeDocumentationEndpoints() {
        assertThat(permitAllPaths)
                .as("白名单应允许开放接口文档访问")
                .contains(
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/doc.html",
                        "/favicon.ico"
                );
    }

    @Test
    void whitelistShouldNotContainDuplicates() {
        Set<String> uniqueEntries = new HashSet<>(permitAllPaths);
        assertThat(uniqueEntries.size())
                .as("白名单配置不应包含重复项")
                .isEqualTo(permitAllPaths.size());
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractPermitAllPaths(Object yamlData) {
        if (!(yamlData instanceof Map)) {
            return Collections.emptyList();
        }

        Map<String, Object> root = (Map<String, Object>) yamlData;
        Object securitySection = root.get("security");
        if (!(securitySection instanceof Map)) {
            return Collections.emptyList();
        }

        Object permitAllSection = ((Map<String, Object>) securitySection).get("permitAll");
        if (!(permitAllSection instanceof List)) {
            return Collections.emptyList();
        }

        return ((List<?>) permitAllSection).stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
