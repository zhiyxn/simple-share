package com.simpleshare.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.ArticleFavorite;
import com.simpleshare.article.dto.ArticleFavoriteView;

import java.util.List;

/**
 * 文章收藏服务接口
 *
 * @author SimpleShare
 */
public interface IArticleFavoriteService {

    /**
     * 新增收藏
     *
     * @param articleId 文章ID
     * @return 新增后的收藏记录
     */
    ArticleFavorite addFavorite(Long articleId);

    /**
     * 根据文章ID取消收藏（当前登录用户）
     *
     * @param articleId 文章ID
     * @return 是否成功移除
     */
    boolean removeFavoriteByArticleId(Long articleId);

    /**
     * 根据收藏ID取消收藏（当前登录用户）
     *
     * @param favoriteId 收藏ID
     * @return 是否成功移除
     */
    boolean removeFavoriteById(Long favoriteId);

    /**
     * 批量删除收藏
     *
     * @param favoriteIds 收藏ID集合
     * @return 成功删除数量
     */
    int batchRemoveFavorites(List<Long> favoriteIds);

    /**
     * 检查当前用户是否收藏了指定文章
     *
     * @param articleId 文章ID
     * @return 收藏记录，未收藏则返回 null
     */
    ArticleFavorite checkFavorite(Long articleId);

    /**
     * 分页查询收藏记录
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param keyword    关键词
     * @param categoryId 分类ID
     * @param sortBy     排序字段
     * @param sortOrder  排序方向
     * @return 收藏视图分页数据
     */
    Page<ArticleFavoriteView> pageFavorites(int pageNum, int pageSize, String keyword, Long categoryId, String sortBy, String sortOrder);
}
