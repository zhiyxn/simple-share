package com.simpleshare.article.domain.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装文章搜索过滤条件
 */
public class ArticleSearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private String author;

    private List<String> tags;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 前端传递的排序字段标识，例如 publishedAt、viewCount 等
     */
    private String sortField;

    /**
     * 排序方向：asc 或 desc
     */
    private String sortOrder;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        if (tags == null) {
            this.tags = null;
        } else {
            this.tags = new ArrayList<>(tags);
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
