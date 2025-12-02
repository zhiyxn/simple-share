package com.simpleshare.system.dto;

import java.util.List;

/**
 * Request body for batch deletion of users.
 */
public class UserBatchDeleteRequest {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
