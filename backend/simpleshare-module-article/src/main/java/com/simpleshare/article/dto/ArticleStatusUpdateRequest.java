package com.simpleshare.article.dto;

/**
 * Request body for updating article status.
 */
public class ArticleStatusUpdateRequest {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
