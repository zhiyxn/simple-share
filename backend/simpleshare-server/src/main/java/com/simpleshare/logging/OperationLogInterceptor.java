package com.simpleshare.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.domain.SysOperationLog;
import com.simpleshare.system.service.ISysOperationLogService;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 操作日志拦截器
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = OperationLogInterceptor.class.getName() + ".START_TIME";

    private final ISysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public OperationLogInterceptor(ISysOperationLogService operationLogService, ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }

        // 不记录OPTIONS等预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        SysOperationLog log = new SysOperationLog();
        log.setTenantId(SecurityUtils.getTenantId());
        log.setTraceId(resolveTraceId());
        log.setRequestId(request.getHeader("X-Request-ID"));
        log.setTitle(resolveTitle(handlerMethod));
        log.setBusinessType(resolveBusinessType(request.getMethod()));
        log.setClassName(handlerMethod.getBeanType().getName());
        log.setMethodName(handlerMethod.getMethod().getName());
        log.setRequestMethod(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setRequestParams(toJsonSafe(extractRequestParams(request)));
        log.setRequestBody(extractRequestBody(request));

        int responseStatus = response.getStatus();
        log.setResponseStatus(responseStatus);

        boolean success = ex == null && responseStatus < 500;
        log.setStatus(success ? 1 : 0);
        if (!success) {
            log.setErrorMessage(resolveErrorMessage(ex, responseStatus));
        }

        Long operatorId = SecurityUtils.getUserIdSafely();
        log.setOperatorId(operatorId);
        log.setOperatorName(SecurityUtils.getUsername());
        log.setOperatorType(resolveOperatorType());
        log.setClientIp(resolveClientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));

        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            log.setDuration(System.currentTimeMillis() - startTime);
        }

        operationLogService.record(log);
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (StringUtils.isNotBlank(traceId)) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveTitle(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().getName();
    }

    private String resolveBusinessType(String method) {
        if (HttpMethod.POST.matches(method)) {
            return "INSERT";
        }
        if (HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method)) {
            return "UPDATE";
        }
        if (HttpMethod.DELETE.matches(method)) {
            return "DELETE";
        }
        return "OTHER";
    }

    private Map<String, Object> extractRequestParams(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        Map<String, Object> flatParams = new HashMap<>(paramMap.size());
        paramMap.forEach((key, values) -> {
            if (values == null) {
                flatParams.put(key, null);
            } else if (values.length == 1) {
                flatParams.put(key, values[0]);
            } else {
                flatParams.put(key, values);
            }
        });
        return flatParams;
    }

    private String extractRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] body = wrapper.getContentAsByteArray();
            if (body != null && body.length > 0) {
                try {
                    String payload = new String(body, wrapper.getCharacterEncoding());
                    return payload.length() > 4000 ? payload.substring(0, 4000) : payload;
                } catch (UnsupportedEncodingException e) {
                    return "UNSUPPORTED_ENCODING";
                }
            }
        }
        return null;
    }

    private String resolveErrorMessage(Exception ex, int status) {
        if (ex != null) {
            return StringUtils.abbreviate(ex.getMessage(), 1000);
        }
        if (status >= 400) {
            return "HTTP Status " + status;
        }
        return null;
    }

    private String resolveOperatorType() {
        if (SecurityUtils.isAdmin(SecurityUtils.getUserIdSafely())) {
            return "ADMIN";
        }
        return "USER";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(header)) {
            return header.split(",")[0].trim();
        }
        header = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(header)) {
            return header.trim();
        }
        return request.getRemoteAddr();
    }

    private String toJsonSafe(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            return json.length() > 4000 ? json.substring(0, 4000) : json;
        } catch (JsonProcessingException e) {
            String fallback = value.toString();
            return fallback.length() > 4000 ? fallback.substring(0, 4000) : fallback;
        }
    }
}
