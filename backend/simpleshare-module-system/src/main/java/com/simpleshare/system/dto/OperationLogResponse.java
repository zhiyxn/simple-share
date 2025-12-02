package com.simpleshare.system.dto;

import com.simpleshare.system.domain.SysOperationLog;

import java.time.format.DateTimeFormatter;

/**
 * 操作日志响应 DTO
 */
public class OperationLogResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;

    private Long tenantId;

    private String traceId;

    private String title;

    private String businessType;

    private String className;

    private String methodName;

    private String requestMethod;

    private String requestUri;

    private String requestParams;

    private String requestBody;

    private Integer responseStatus;

    private Integer status;

    private String errorMessage;

    private Long operatorId;

    private String operatorName;

    private String operatorType;

    private String clientIp;

    private String userAgent;

    private Long duration;

    private String requestId;

    private String createTime;

    public static OperationLogResponse from(SysOperationLog log) {
        OperationLogResponse response = new OperationLogResponse();
        response.setId(log.getId());
        response.setTenantId(log.getTenantId());
        response.setTraceId(log.getTraceId());
        response.setTitle(log.getTitle());
        response.setBusinessType(log.getBusinessType());
        response.setClassName(log.getClassName());
        response.setMethodName(log.getMethodName());
        response.setRequestMethod(log.getRequestMethod());
        response.setRequestUri(log.getRequestUri());
        response.setRequestParams(log.getRequestParams());
        response.setRequestBody(log.getRequestBody());
        response.setResponseStatus(log.getResponseStatus());
        response.setStatus(log.getStatus());
        response.setErrorMessage(log.getErrorMessage());
        response.setOperatorId(log.getOperatorId());
        response.setOperatorName(log.getOperatorName());
        response.setOperatorType(log.getOperatorType());
        response.setClientIp(log.getClientIp());
        response.setUserAgent(log.getUserAgent());
        response.setDuration(log.getDuration());
        response.setRequestId(log.getRequestId());
        if (log.getCreateTime() != null) {
            response.setCreateTime(FORMATTER.format(log.getCreateTime()));
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
