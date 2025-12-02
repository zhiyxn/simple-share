package com.simpleshare.article.service;

import com.simpleshare.article.domain.Article;
import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 文章Service接口
 *
 * @author SimpleShare
 */
public interface IArticleService {
    
    /**
     * 查询文章
     *
     * @param articleId 文章主键
     * @return 文章
     */
    Article selectArticleByArticleId(Long articleId);
    
    /**
     * 查询文章列表
     *
     * @param article 文章
     * @return 文章集合
     */
    List<Article> selectArticleList(Article article);
    
    /**
     * 分页查询文章列表
     *
     * @param article 文章
     * @return 文章分页数据
     */
    TableDataInfo selectArticlePage(Article article);
    
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
     * 批量删除文章
     *
     * @param articleIds 需要删除的文章主键集合
     * @return 结果
     */
    int deleteArticleByArticleIds(Long[] articleIds);
    
    /**
     * 删除文章信息
     *
     * @param articleId 文章主键
     * @return 结果
     */
    int deleteArticleByArticleId(Long articleId);
    
    /**
     * 查询已发布的文章列表（前台使用）
     *
     * @param article 文章
     * @return 文章集合
     */
    List<Article> selectPublishedArticleList(Article article);
    
    /**
     * 分页查询已发布的文章列表（前台使用）
     *
     * @param article 文章
     * @return 文章分页数据
     */
    TableDataInfo selectPublishedArticlePage(Article article);
    
    /**
     * 发布文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int publishArticle(Long articleId);
    
    /**
     * 下线文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int offlineArticle(Long articleId);
    
    /**
     * 置顶文章
     *
     * @param articleId 文章ID
     * @param isTop     是否置顶
     * @return 结果
     */
    int topArticle(Long articleId, String isTop);
    
    /**
     * 推荐文章
     *
     * @param articleId   文章ID
     * @param isRecommend 是否推荐
     * @return 结果
     */
    int recommendArticle(Long articleId, String isRecommend);
    
    /**
     * 增加文章浏览次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int incrementViewCount(Long articleId);
    
    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int likeArticle(Long articleId);
    
    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int unlikeArticle(Long articleId);
    
    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int collectArticle(Long articleId);
    
    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    int uncollectArticle(Long articleId);
    
    /**
     * 查询推荐文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectRecommendArticleList(Integer limit);
    
    /**
     * 查询热门文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectHotArticleList(Integer limit);
    
    /**
     * 查询最新文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    List<Article> selectLatestArticleList(Integer limit);
    
    /**
     * 查询相关文章列表
     *
     * @param categoryId 分类ID
     * @param articleId  当前文章ID
     * @param limit      限制数量
     * @return 文章集合
     */
    List<Article> selectRelatedArticleList(Long categoryId, Long articleId, Integer limit);
    
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
    List<Article> searchArticles(String keyword);
    
    /**
     * 分页搜索文章
     *
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 文章分页数据
     */
    TableDataInfo searchArticlesPage(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 校验文章标题是否唯一
     *
     * @param article 文章信息
     * @return 结果
     */
    String checkTitleUnique(Article article);
    
    /**
     * 根据分类ID统计文章数量
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    int countArticleByCategoryId(Long categoryId);

    /**
     * 查询文章（使用ID字段）
     *
     * @param id 文章主键
     * @return 文章
     */
    Article selectArticleById(Long id);

    /**
     * 删除文章（使用ID字段）
     *
     * @param id 文章主键
     * @return 结果
     */
    int deleteArticleById(Long id);

    /**
     * 批量删除文章（使用ID字段）
     *
     * @param ids 需要删除的文章主键集合
     * @return 结果
     */
    int deleteArticleByIds(Long[] ids);

    /**
     * 校验文章标题是否唯一
     *
     * @param article 文章信息
     * @return 结果
     */
    boolean checkArticleTitleUnique(Article article);

    /**
     * 更新文章状态
     *
     * @param article 文章信息
     * @return 结果
     */
    int updateArticleStatus(Article article);

    /**
     * 审核文章
     *
     * @param articleId 文章ID
     * @param status    审核结论
     * @return 结果
     */
    int reviewArticle(Long articleId, ArticleReviewStatus status);
}
