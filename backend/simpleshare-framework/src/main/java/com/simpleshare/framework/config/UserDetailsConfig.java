package com.simpleshare.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户详情服务配置
 *
 * @author SimpleShare
 */
@Configuration
public class UserDetailsConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if ("admin".equals(username)) {
                return User.withUsername("admin")
                        .password(passwordEncoder().encode("admin123"))
                        .authorities("ROLE_ADMIN")
                        .build();
            }
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username);
        };
    }
}