package com.simpleshare.framework.tenant;

import com.simpleshare.common.utils.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 租户过滤器
 * 从请求头中提取租户信息并设置到上下文中
 *
 * @author SimpleShare
 */
@Component
@Order(1)
public class TenantFilter implements Filter {
    
    private static final String TENANT_ID_HEADER = "Tenant-Id";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // 从请求头中获取租户ID
            String tenantId = httpRequest.getHeader(TENANT_ID_HEADER);
            
            // 如果请求头中没有租户ID，尝试从参数中获取
            if (StringUtils.isEmpty(tenantId)) {
                tenantId = httpRequest.getParameter("tenantId");
            }
            
            // 设置租户ID到上下文
            if (StringUtils.isNotEmpty(tenantId)) {
                TenantContextHolder.setTenantId(tenantId);
            }
            
            // 继续执行过滤器链
            chain.doFilter(request, response);
        } finally {
            // 清除租户上下文
            TenantContextHolder.clear();
        }
    }
    
    @Override
    public void destroy() {
        // 销毁操作
    }
}