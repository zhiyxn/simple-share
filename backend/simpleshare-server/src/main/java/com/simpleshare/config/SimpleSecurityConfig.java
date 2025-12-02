package com.simpleshare.config;

import com.simpleshare.framework.security.filter.JwtAuthenticationTokenFilter;
import com.simpleshare.framework.security.handle.AuthenticationEntryPointImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security configuration that relies on YAML-managed whitelists.
 */
@Configuration
@EnableWebSecurity
public class SimpleSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSecurityConfig.class);

    private final SecurityProperties securityProperties;
    private final AuthenticationEntryPointImpl unauthorizedHandler;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    public SimpleSecurityConfig(SecurityProperties securityProperties,
                                AuthenticationEntryPointImpl unauthorizedHandler,
                                JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.securityProperties = securityProperties;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;

        logger.info("SimpleSecurityConfig initialized with SecurityProperties: {}",
                securityProperties != null ? "loaded" : "null");
        if (securityProperties != null && securityProperties.getPermitAll() != null) {
            logger.info("Loaded {} whitelist paths:", securityProperties.getPermitAll().size());
            for (String path : securityProperties.getPermitAll()) {
                logger.info("  - {}", path);
            }
        } else {
            logger.warn("No whitelist paths loaded!");
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain.");

        http
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            .and()
            .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests(authz -> {
                if (securityProperties != null && securityProperties.getPermitAll() != null) {
                    for (String pathPattern : securityProperties.getPermitAll()) {
                        if (pathPattern.contains(":")) {
                            String[] parts = pathPattern.split(":", 2);
                            String method = parts[0].trim();
                            String path = parts[1].trim();
                            logger.info("Adding permit all path with method: {} {}", method, path);
                            switch (method.toUpperCase()) {
                                case "GET":
                                    authz.antMatchers(org.springframework.http.HttpMethod.GET, path).permitAll();
                                    break;
                                case "POST":
                                    authz.antMatchers(org.springframework.http.HttpMethod.POST, path).permitAll();
                                    break;
                                case "PUT":
                                    authz.antMatchers(org.springframework.http.HttpMethod.PUT, path).permitAll();
                                    break;
                                case "DELETE":
                                    authz.antMatchers(org.springframework.http.HttpMethod.DELETE, path).permitAll();
                                    break;
                                default:
                                    logger.warn("Unsupported HTTP method: {}, treating as any method", method);
                                    authz.antMatchers(path).permitAll();
                                    break;
                            }
                        } else {
                            logger.info("Adding permit all path: {}", pathPattern);
                            authz.antMatchers(pathPattern).permitAll();
                        }
                    }
                }

                authz.anyRequest().authenticated();
            });

        logger.info("Security filter chain configured successfully.");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
