package com.simpleshare.system.dto;

/**
 * Response wrapper containing the freshly generated password.
 */
public class ResetPasswordResponse {

    private String password;

    public ResetPasswordResponse() {
    }

    public ResetPasswordResponse(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
