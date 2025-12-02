package com.simpleshare.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.article.domain.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章Mapper接口
 *
 * @author SimpleShare
 */
public interface ArticleMapper extends BaseMapper<Article> {
    
    /**
     * 查询文章列表
     *
     * @param article 文章
     * @return 文章集合
     */
    List<Article> selectArticleList(Article article);
    
    /**
     * 根据文章ID查询文章
     *
     * @param articleId 文章ID
     * @return 文章
     */
    Article selectArticleByArticleId(Long articleId);
    
    /**
     * 新增文章
     *
     * @param article 文章
     * @return 结果
     */
    int insertArticle(Article article);
    
    /**
     * 修改文章
     *
     * @param article 文章
     * @return 结果
     */
    int updateArticle(Article article);
    
    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int deleteArticleByArticleId(Long articleId);
    
    /**
     * 批量删除文章
     *
     * @param articleIds 需要删除的数据ID
     * @return 结果
     */
    int deleteArticleByArticleIds(Long[] articleIds);
    
    /**
     * 查询已发布的文章列表（前台使用）
     *
     * @param article 文章
     * @return 文章集合
     */
    List<Article> selectPublishedArticleList(Article article);
    
    /**
     * 根据分类ID查询文章数量
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    int countArticleByCategoryId(Long categoryId);
    
    /**
     * 增加文章浏览次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int incrementViewCount(Long articleId);
    
    /**
     * 增加文章点赞次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int incrementLikeCount(Long articleId);
    
    /**
     * 减少文章点赞次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int decrementLikeCount(Long articleId);
    
    /**
     * 增加文章收藏次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int incrementCollectCount(Long articleId);
    
    /**
     * 减少文章收藏次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int decrementCollectCount(Long articleId);
    
    /**
     * 查询推荐文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectRecommendArticleList(@Param("limit") Integer limit);
    
    /**
     * 查询热门文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectHotArticleList(@Param("limit") Integer limit);
    
    /**
     * 查询最新文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectLatestArticleList(@Param("limit") Integer limit);
    
    /**
     * 查询相关文章列表
     *
     * @param categoryId 分类ID
     * @param articleId  当前文章ID
     * @param limit      限制数量
     * @return 文章集合
     */
    List<Article> selectRelatedArticleList(@Param("categoryId") Long categoryId, @Param("articleId") Long articleId, @Param("limit") Integer limit);
    
    /**
     * 根据作者ID查询文章列表
     *
     * @param authorId 作者ID
     * @return 文章集合
     */
    List<Article> selectArticleListByAuthorId(Long authorId);
    
    /**
     * 搜索文章
     *
     * @param keyword 关键词
     * @return 文章集合
     */
    List<Article> searchArticles(@Param("keyword") String keyword);
}