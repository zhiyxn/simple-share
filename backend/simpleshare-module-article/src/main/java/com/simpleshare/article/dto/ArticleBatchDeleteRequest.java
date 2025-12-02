package com.simpleshare.article.dto;

import java.util.List;

/**
 * Request body for batch deleting articles.
 */
public class ArticleBatchDeleteRequest {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
