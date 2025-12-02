package com.simpleshare.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.ArticleFavorite;
import com.simpleshare.article.dto.ArticleFavoriteView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章收藏 Mapper
 *
 * @author SimpleShare
 */
public interface ArticleFavoriteMapper extends BaseMapper<ArticleFavorite> {

    /**
     * 根据用户及文章查询收藏记录
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 收藏记录
     */
    ArticleFavorite selectByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 删除指定用户的文章收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 影响行数
     */
    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * 批量删除收藏记录
     *
     * @param userId    用户ID
     * @param favoriteIds 收藏ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("userId") Long userId, @Param("ids") List<Long> favoriteIds);

    /**
     * 分页查询收藏记录（携带文章信息）
     *
     * @param page       分页对象
     * @param userId     用户ID
     * @param keyword    关键字
     * @param categoryId 分类ID
     * @param sortBy     排序字段
     * @param sortOrder  排序方向
     * @return 收藏视图分页数据
     */
    Page<ArticleFavoriteView> selectFavoritePage(Page<ArticleFavoriteView> page,
                                                 @Param("userId") Long userId,
                                                 @Param("keyword") String keyword,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("sortBy") String sortBy,
                                                 @Param("sortOrder") String sortOrder);
}
