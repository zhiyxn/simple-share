package com.simpleshare.common.core.domain.model;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * Lightweight DTO for returning paginated data to the frontend.
 */
public class PageResponse<T> {

    private List<T> items;
    private long total;
    private int page;
    private int limit;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrev;

    public PageResponse() {
    }

    public PageResponse(List<T> items, long total, int page, int limit, int totalPages, boolean hasNext, boolean hasPrev) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    public static <T> PageResponse<T> from(PageInfo<?> pageInfo, List<T> items) {
        PageResponse<T> response = new PageResponse<>();
        response.setItems(items);
        response.setTotal(pageInfo.getTotal());
        response.setPage(pageInfo.getPageNum());
        response.setLimit(pageInfo.getPageSize());
        response.setTotalPages(pageInfo.getPages());
        response.setHasNext(pageInfo.getPageNum() < pageInfo.getPages());
        response.setHasPrev(pageInfo.getPageNum() > 1);
        return response;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
    }
}
