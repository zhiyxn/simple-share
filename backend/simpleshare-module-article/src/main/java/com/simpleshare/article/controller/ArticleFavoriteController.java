package com.simpleshare.article.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.ArticleFavorite;
import com.simpleshare.article.dto.ArticleFavoriteView;
import com.simpleshare.article.service.IArticleFavoriteService;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章收藏控制器
 *
 * 提供收藏新增、取消、查询等接口
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping({"/favorites", "/article/favorites"})
public class ArticleFavoriteController extends BaseController {

    @Autowired
    private IArticleFavoriteService articleFavoriteService;

    /**
     * 分页查询收藏列表
     */
    @GetMapping
    public AjaxResult listFavorites(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                    @RequestParam(value = "size", required = false) Integer size,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    @RequestParam(value = "sortBy", required = false) String sortBy,
                                    @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        int finalPageSize = pageSize != null ? pageSize : (size != null ? size : 20);
        String resolvedSortBy = StringUtils.isNotBlank(sort) ? sort : sortBy;

        Page<ArticleFavoriteView> favoritePage = articleFavoriteService.pageFavorites(
                page,
                finalPageSize,
                keyword,
                categoryId,
                resolvedSortBy,
                sortOrder
        );

        Map<String, Object> data = new HashMap<>();
        data.put("items", favoritePage.getRecords().stream().map(this::convertView).collect(Collectors.toList()));
        data.put("total", favoritePage.getTotal());
        data.put("page", page);
        data.put("pageSize", finalPageSize);

        return AjaxResult.success(data);
    }

    /**
     * 新增收藏
     */
    @PostMapping("/{articleId}")
    public AjaxResult addFavorite(@PathVariable Long articleId) {
        ArticleFavorite favorite = articleFavoriteService.addFavorite(articleId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", true);
        data.put("isFavorited", true);
        data.put("favoriteId", favorite.getId());
        data.put("articleId", favorite.getArticleId());
        return AjaxResult.success("收藏成功", data);
    }

    /**
     * 根据收藏ID取消收藏
     */
    @DeleteMapping("/records/{favoriteId}")
    public AjaxResult removeFavorite(@PathVariable Long favoriteId) {
        boolean removed = articleFavoriteService.removeFavoriteById(favoriteId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", !removed);
        data.put("isFavorited", !removed);
        data.put("removed", removed);
        return removed ? AjaxResult.success("已移除收藏", data) : AjaxResult.warn("收藏记录不存在", data);
    }

    /**
     * 根据文章ID取消收藏
     */
    @DeleteMapping("/by-article/{articleId}")
    public AjaxResult removeFavoriteByArticle(@PathVariable Long articleId) {
        boolean removed = articleFavoriteService.removeFavoriteByArticleId(articleId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", !removed);
        data.put("isFavorited", !removed);
        data.put("removed", removed);
        return removed ? AjaxResult.success("已移除收藏", data) : AjaxResult.success("未收藏该文章", data);
    }

    /**
     * 批量取消收藏
     */
    @DeleteMapping("/batch")
    public AjaxResult batchRemove(@RequestBody(required = false) BatchRemoveRequest request) {
        if (request == null) {
            return AjaxResult.success(Collections.singletonMap("removed", 0));
        }

        List<Long> favoriteIds = sanitizeIds(request.getFavoriteIds());
        List<Long> articleIds = sanitizeIds(request.getArticleIds());
        int removed = 0;

        if (!favoriteIds.isEmpty()) {
            removed = articleFavoriteService.batchRemoveFavorites(favoriteIds);
        } else if (!articleIds.isEmpty()) {
            for (Long articleId : articleIds) {
                if (articleFavoriteService.removeFavoriteByArticleId(articleId)) {
                    removed++;
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("removed", removed);
        return AjaxResult.success("批量移除完成", data);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{articleId}/check")
    public AjaxResult checkFavorite(@PathVariable Long articleId) {
        ArticleFavorite favorite = articleFavoriteService.checkFavorite(articleId);
        boolean favorited = favorite != null;
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        data.put("isFavorited", favorited);
        if (favorited) {
            data.put("favoriteId", favorite.getId());
        }
        return AjaxResult.success(data);
    }

    private Map<String, Object> convertView(ArticleFavoriteView view) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", view.getId());
        item.put("favoriteId", view.getId());
        item.put("articleId", view.getArticleId());
        item.put("folderName", view.getFolderName());
        item.put("tags", splitTags(view.getTags()));
        item.put("notes", view.getNotes());
        item.put("created_at", formatDate(view.getCreateTime()));
        item.put("updated_at", formatDate(view.getUpdateTime()));

        Map<String, Object> article = new HashMap<>();
        article.put("id", view.getArticleId());
        article.put("title", view.getArticleTitle());
        article.put("summary", view.getArticleSummary());
        article.put("cover", view.getArticleCover());
        article.put("author", view.getArticleAuthorName());
        article.put("authorAvatar", view.getArticleAuthorAvatar());
        article.put("authorId", view.getArticleAuthorId());
        article.put("categoryId", view.getArticleCategoryId());
        article.put("categoryName", view.getArticleCategoryName());
        article.put("status", view.getArticleStatus());
        item.put("article", article);

        return item;
    }

    private List<String> splitTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return new ArrayList<>();
        }
        String[] segments = StringUtils.split(tags, ",");
        if (segments == null || segments.length == 0) {
            segments = new String[]{tags};
        }
        return java.util.Arrays.stream(segments)
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    private String formatDate(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, DateUtils.toDate(time));
    }

    private List<Long> sanitizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 批量移除请求
     */
    public static class BatchRemoveRequest {
        private List<Long> favoriteIds;
        private List<Long> articleIds;

        public List<Long> getFavoriteIds() {
            return favoriteIds;
        }

        public void setFavoriteIds(List<Long> favoriteIds) {
            this.favoriteIds = favoriteIds;
        }

        public List<Long> getArticleIds() {
            return articleIds;
        }

        public void setArticleIds(List<Long> articleIds) {
            this.articleIds = articleIds;
        }
    }
}
