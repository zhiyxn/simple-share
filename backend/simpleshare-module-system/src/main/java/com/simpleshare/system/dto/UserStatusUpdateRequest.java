package com.simpleshare.system.dto;

/**
 * Request body for updating a single user's status.
 */
public class UserStatusUpdateRequest {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
