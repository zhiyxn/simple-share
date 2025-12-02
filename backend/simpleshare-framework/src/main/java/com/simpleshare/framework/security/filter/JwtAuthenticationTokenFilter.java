package com.simpleshare.framework.security.filter;

import com.simpleshare.common.constant.CacheConstants;
import com.simpleshare.common.constant.Constants;
import com.simpleshare.common.utils.JwtUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.redis.RedisCache;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.framework.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author SimpleShare
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 启用JWT认证
        String requestUri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.info("JWT Filter处理请求: {}, Method: {}, Auth Header: {}",
                 requestUri, request.getMethod(),
                 authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");

        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            // 如果当前SecurityContext中没有认证信息，则设置认证信息
            if (StringUtils.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                tokenService.verifyToken(loginUser);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("JWT认证成功，设置用户认证信息: {}, 路径: {}", loginUser.getUser().getUserName(), requestUri);
            }
        } else {
            log.warn("JWT认证失败，无法获取用户信息，请求路径: {}, Auth Header: {}",
                     requestUri, authHeader != null ? "Present" : "Missing");
        }
        chain.doFilter(request, response);
    }
}