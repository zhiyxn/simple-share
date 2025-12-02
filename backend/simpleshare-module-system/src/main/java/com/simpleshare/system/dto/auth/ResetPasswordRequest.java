package com.simpleshare.system.dto.auth;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "重置凭证不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度应为6-64个字符")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private Long tenantId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
