package com.simpleshare.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.Article;
import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.article.mapper.ArticleMapper;
import com.simpleshare.article.service.IArticleFavoriteService;
import com.simpleshare.article.service.IArticleService;
import com.simpleshare.article.service.support.ArticleFileUrlResolver;
import com.simpleshare.article.service.support.ArticleReviewManager;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.common.core.page.PageDomain;
import com.simpleshare.common.core.page.TableSupport;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文章Service业务层处理
 *
 * @author SimpleShare
 */
@Service
public class ArticleServiceImpl implements IArticleService {
    
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private IArticleFavoriteService articleFavoriteService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArticleReviewManager articleReviewManager;

    @Autowired
    private ArticleFileUrlResolver articleFileUrlResolver;
    
    /**
     * 查询文章
     *
     * @param articleId 文章主键
     * @return 文章
     */
    @Override
    public Article selectArticleByArticleId(Long articleId) {
        Article article = articleMapper.selectArticleByArticleId(articleId);
        articleFileUrlResolver.normalize(article);
        return article;
    }
    
    /**
     * 查询文章列表
     *
     * @param article 文章
     * @return 文章
     */
    @Override
    public List<Article> selectArticleList(Article article) {
        List<Article> articles = articleMapper.selectArticleList(article);
        populateAuthorNames(articles);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 分页查询文章列表
     *
     * @param article 文章
     * @return 文章分页数据
     */
    @Override
    public TableDataInfo selectArticlePage(Article article) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(article.getTitle()), Article::getTitle, article.getTitle())
                .eq(article.getCategoryId() != null, Article::getCategoryId, article.getCategoryId())
                .eq(article.getAuthorId() != null, Article::getAuthorId, article.getAuthorId())
                .eq(article.getStatus() != null, Article::getStatus, article.getStatus())
                .eq(article.getReviewStatus() != null, Article::getReviewStatus, article.getReviewStatus())
                .eq(article.getIsTop() != null, Article::getIsTop, article.getIsTop())
                .eq(article.getIsRecommend() != null, Article::getIsRecommend, article.getIsRecommend())
                .orderByDesc(Article::getCreateTime);
        
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<Article> page = articleMapper.selectPage(new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize()), queryWrapper);
        populateAuthorNames(page.getRecords());
        articleFileUrlResolver.normalize(page.getRecords());
        return TableDataInfo.build(page);
    }
    
    /**
     * 新增文章
     *
     * @param article 文章
     * @return 结果
     */
    @Override
    @Transactional
    public int insertArticle(Article article) {
        // 保存前自动设置字段
        article.preInsert();

        // 设置租户ID
        try {
            article.setTenantId(Long.valueOf(TenantContextHolder.getTenantId()));
        } catch (Exception e) {
            article.setTenantId(1L); // 默认租户ID
        }

        // 初始化统计数据
        if (article.getViewCount() == null) {
            article.setViewCount(0L);
        }
        if (article.getLikeCount() == null) {
            article.setLikeCount(0L);
        }
        if (article.getCollectCount() == null) {
            article.setCollectCount(0L);
        }
        if (article.getCommentCount() == null) {
            article.setCommentCount(0L);
        }

        if (article.getReviewStatus() == null) {
            article.setReviewStatus(ArticleReviewStatus.PENDING.getCode());
        }

        applyReviewStatusPolicy(article, null);

        normalizeAccessControlFields(article);
        processPasswordBeforePersist(article, null);

        // 如果是发布状态，设置发布时间
        if (Integer.valueOf(1).equals(article.getStatus())) {
            article.setPublishTime(DateUtils.getNowDate());
        }

        return articleMapper.insertArticle(article);
    }
    
    /**
     * 修改文章
     *
     * @param article 文章
     * @return 结果
     */
    @Override
    @Transactional
    public int updateArticle(Article article) {
        // 如果状态改为发布且之前没有发布时间，设置发布时间
        if (Integer.valueOf(1).equals(article.getStatus()) && article.getPublishTime() == null) {
            article.setPublishTime(DateUtils.getNowDate());
        }

        Article existing = null;
        if (article.getArticleId() != null) {
            existing = articleMapper.selectArticleByArticleId(article.getArticleId());
        }

        normalizeAccessControlFields(article);
        processPasswordBeforePersist(article, existing);
        applyReviewStatusPolicy(article, existing);

        return articleMapper.updateArticle(article);
    }
    
    /**
     * 批量删除文章
     *
     * @param articleIds 需要删除的文章主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteArticleByArticleIds(Long[] articleIds) {
        return articleMapper.deleteArticleByArticleIds(articleIds);
    }
    
    /**
     * 删除文章信息
     *
     * @param articleId 文章主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteArticleByArticleId(Long articleId) {
        return articleMapper.deleteArticleByArticleId(articleId);
    }
    
    /**
     * 查询已发布的文章列表（前台使用）
     *
     * @param article 文章
     * @return 文章集合
     */
    @Override
    public List<Article> selectPublishedArticleList(Article article) {
        article.setStatus(1); // 只查询已发布的文章
        List<Article> articles = articleMapper.selectPublishedArticleList(article);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 分页查询已发布的文章列表（前台使用）
     *
     * @param article 文章
     * @return 文章分页数据
     */
    @Override
    public TableDataInfo selectPublishedArticlePage(Article article) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, "1") // 只查询已发布的文章
                .eq(Article::getReviewStatus, ArticleReviewStatus.APPROVED.getCode()) // 只展示审核通过的文章
                .eq(Article::getDelFlag, "0") // 排除已删除的文章
                .like(StringUtils.isNotEmpty(article.getTitle()), Article::getTitle, article.getTitle())
                .eq(article.getCategoryId() != null, Article::getCategoryId, article.getCategoryId())
                .eq(article.getAuthorId() != null, Article::getAuthorId, article.getAuthorId())
                .eq(article.getIsTop() != null, Article::getIsTop, article.getIsTop())
                .eq(article.getIsRecommend() != null, Article::getIsRecommend, article.getIsRecommend())
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishTime);

        // 显示所有文章（包括会员文章），权限验证在文章详情页进行
        // 这样可以让用户看到完整的文章列表，并在前端显示VIP标识
        
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<Article> page = articleMapper.selectPage(new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize()), queryWrapper);
        populateAuthorNames(page.getRecords());
        articleFileUrlResolver.normalize(page.getRecords());
        return TableDataInfo.build(page);
    }

    private void normalizeAccessControlFields(Article article) {
        if (article == null) {
            return;
        }

        if (StringUtils.isEmpty(article.getEnableTieredRead())) {
            article.setEnableTieredRead("1");
        }

        if (StringUtils.isEmpty(article.getEnableWatermark())) {
            article.setEnableWatermark("1");
        }

        if (!"1".equals(article.getEnableTieredRead())) {
            article.setPreviewContent(null);
            return;
        }

        if (StringUtils.isNotEmpty(article.getPreviewContent())) {
            return;
        }

        if (StringUtils.isNotEmpty(article.getSummary())) {
            article.setPreviewContent(article.getSummary());
            return;
        }

        if (StringUtils.isNotEmpty(article.getContent())) {
            String plainText = article.getContent().replaceAll("<[^>]+>", "");
            String preview = StringUtils.substring(plainText, 0, Math.min(plainText.length(), 500));
            article.setPreviewContent(preview);
        }
    }

    private void processPasswordBeforePersist(Article article, Article existing) {
        if (article == null) {
            return;
        }

        Integer resolvedIsPasswd = article.getIsPasswd();
        Boolean passwordProtected = article.getPasswordProtected();
        if (passwordProtected != null) {
            resolvedIsPasswd = Boolean.TRUE.equals(passwordProtected) ? 0 : 1;
        }

        if (Boolean.TRUE.equals(article.getClearPassword())) {
            article.setPassword(null);
            article.setClearPassword(null);
            article.setPasswordProtected(Boolean.FALSE);
            article.setIsPasswd(1);
            return;
        }

        if (StringUtils.isNotEmpty(article.getPassword())) {
            String rawPassword = article.getPassword();
            if (existing != null && StringUtils.isNotEmpty(existing.getPassword())
                    && SecurityUtils.matchesPassword(rawPassword, existing.getPassword())) {
                article.setPassword(existing.getPassword());
            } else {
                article.setPassword(SecurityUtils.encryptPassword(rawPassword));
            }
            article.setClearPassword(null);
            article.setPasswordProtected(Boolean.TRUE);
            article.setIsPasswd(0);
            return;
        }

        if (resolvedIsPasswd == null) {
            if (existing != null) {
                if (existing.getIsPasswd() != null) {
                    resolvedIsPasswd = existing.getIsPasswd();
                } else if (StringUtils.isNotEmpty(existing.getPassword())) {
                    resolvedIsPasswd = 0;
                }
            }
        }

        if (resolvedIsPasswd == null) {
            resolvedIsPasswd = 1;
        }

        article.setIsPasswd(resolvedIsPasswd);
        if (passwordProtected == null) {
            article.setPasswordProtected(resolvedIsPasswd == 0);
        }

        article.setPassword(null);
        article.setClearPassword(null);
    }
    
    /**
     * 发布文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int publishArticle(Long articleId) {
        Article existing = articleMapper.selectArticleByArticleId(articleId);
        if (existing == null) {
            throw new ServiceException("文章不存在");
        }

        Article article = new Article();
        article.setArticleId(articleId);
        article.setStatus(1);
        applyReviewStatusPolicy(article, existing);
        return articleMapper.updateArticle(article);
    }
    
    /**
     * 下线文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int offlineArticle(Long articleId) {
        Article article = new Article();
        article.setArticleId(articleId);
        article.setStatus(2);
        return articleMapper.updateArticle(article);
    }
    
    /**
     * 置顶文章
     *
     * @param articleId 文章ID
     * @param isTop     是否置顶
     * @return 结果
     */
    @Override
    @Transactional
    public int topArticle(Long articleId, String isTop) {
        Article article = new Article();
        article.setArticleId(articleId);
        article.setIsTop(Integer.valueOf(isTop));
        return articleMapper.updateArticle(article);
    }
    
    /**
     * 推荐文章
     *
     * @param articleId   文章ID
     * @param isRecommend 是否推荐
     * @return 结果
     */
    @Override
    @Transactional
    public int recommendArticle(Long articleId, String isRecommend) {
        Article article = new Article();
        article.setArticleId(articleId);
        article.setIsRecommend(Integer.valueOf(isRecommend));
        return articleMapper.updateArticle(article);
    }
    
    /**
     * 增加文章浏览次数
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    public int incrementViewCount(Long articleId) {
        return articleMapper.incrementViewCount(articleId);
    }
    
    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int likeArticle(Long articleId) {
        return articleMapper.incrementLikeCount(articleId);
    }
    
    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int unlikeArticle(Long articleId) {
        return articleMapper.decrementLikeCount(articleId);
    }
    
    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int collectArticle(Long articleId) {
        articleFavoriteService.addFavorite(articleId);
        return 1;
    }
    
    /**
     * 取消收藏文章
     *
     * @param articleId 文章ID
     * @return 结果
     */
    @Override
    @Transactional
    public int uncollectArticle(Long articleId) {
        return articleFavoriteService.removeFavoriteByArticleId(articleId) ? 1 : 0;
    }
    
    /**
     * 查询推荐文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    @Override
    public List<Article> selectRecommendArticleList(Integer limit) {
        List<Article> articles = articleMapper.selectRecommendArticleList(limit);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 查询热门文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    @Override
    public List<Article> selectHotArticleList(Integer limit) {
        List<Article> articles = articleMapper.selectHotArticleList(limit);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 查询最新文章列表
     *
     * @param limit 限制数量
     * @return 文章集合
     */
    @Override
    public List<Article> selectLatestArticleList(Integer limit) {
        List<Article> articles = articleMapper.selectLatestArticleList(limit);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 查询相关文章列表
     *
     * @param categoryId 分类ID
     * @param articleId  当前文章ID
     * @param limit      限制数量
     * @return 文章集合
     */
    @Override
    public List<Article> selectRelatedArticleList(Long categoryId, Long articleId, Integer limit) {
        List<Article> articles = articleMapper.selectRelatedArticleList(categoryId, articleId, limit);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 根据作者ID查询文章列表
     *
     * @param authorId 作者ID
     * @return 文章集合
     */
    @Override
    public List<Article> selectArticleListByAuthorId(Long authorId) {
        List<Article> articles = articleMapper.selectArticleListByAuthorId(authorId);
        populateAuthorNames(articles);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 搜索文章
     *
     * @param keyword 关键词
     * @return 文章集合
     */
    @Override
    public List<Article> searchArticles(String keyword) {
        List<Article> articles = articleMapper.searchArticles(keyword);
        articleFileUrlResolver.normalize(articles);
        return articles;
    }
    
    /**
     * 分页搜索文章
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 文章分页数据
     */
    @Override
    public TableDataInfo searchArticlesPage(String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, "1") // 只搜索已发布的文章
                .and(wrapper -> wrapper.like(Article::getTitle, keyword)
                        .or().like(Article::getSummary, keyword)
                        .or().like(Article::getContent, keyword))
                .orderByDesc(Article::getPublishTime);
        
        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        articleFileUrlResolver.normalize(page.getRecords());
        return TableDataInfo.build(page);
    }
    
    /**
     * 校验文章标题是否唯一
     *
     * @param article 文章信息
     * @return 结果
     */
    @Override
    public String checkTitleUnique(Article article) {
        Long articleId = StringUtils.isNull(article.getArticleId()) ? -1L : article.getArticleId();
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getTitle, article.getTitle())
                .ne(Article::getArticleId, articleId);
        
        Article info = articleMapper.selectOne(queryWrapper);
        if (StringUtils.isNotNull(info)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
    
    /**
     * 根据分类ID统计文章数量
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Override
    public int countArticleByCategoryId(Long categoryId) {
        return articleMapper.countArticleByCategoryId(categoryId);
    }

    /**
     * 查询文章（使用ID字段）
     *
     * @param id 文章主键
     * @return 文章
     */
    @Override
    public Article selectArticleById(Long id) {
        Article article = articleMapper.selectArticleByArticleId(id);
        articleFileUrlResolver.normalize(article);
        return article;
    }

    /**
     * 删除文章（使用ID字段）
     *
     * @param id 文章主键
     * @return 结果
     */
    @Override
    public int deleteArticleById(Long id) {
        return articleMapper.deleteArticleByArticleId(id);
    }

    /**
     * 批量删除文章（使用ID字段）
     *
     * @param ids 需要删除的文章主键集合
     * @return 结果
     */
    @Override
    public int deleteArticleByIds(Long[] ids) {
        return articleMapper.deleteArticleByArticleIds(ids);
    }

    /**
     * 校验文章标题是否唯一
     *
     * @param article 文章信息
     * @return 结果
     */
    @Override
    public boolean checkArticleTitleUnique(Article article) {
        return UserConstants.UNIQUE.equals(checkTitleUnique(article));
    }

    /**
     * 更新文章状态
     *
     * @param article 文章信息
     * @return 结果
     */
    @Override
    public int updateArticleStatus(Article article) {
        if (article == null || article.getArticleId() == null) {
            throw new ServiceException("文章ID不能为空");
        }
        Integer status = article.getStatus();
        if (status != null) {
            if (status == 1) {
                return publishArticle(article.getArticleId());
            }
            if (status == 2) {
                return offlineArticle(article.getArticleId());
            }
        }
        return articleMapper.updateArticle(article);
    }

    @Override
    @Transactional
    public int reviewArticle(Long articleId, ArticleReviewStatus status) {
        if (status == null) {
            throw new ServiceException("审核状态不能为空");
        }
        Article existing = articleMapper.selectArticleByArticleId(articleId);
        if (existing == null) {
            throw new ServiceException("文章不存在");
        }
        Article article = new Article();
        article.setArticleId(articleId);
        article.setReviewStatus(status.getCode());
        if (status.isApproved()) {
            article.setStatus(1);
            article.setPublishTime(new Date());
        } else if (status.isRejected()) {
            article.setStatus(0);
        }
        return articleMapper.updateArticle(article);
    }

    private void applyReviewStatusPolicy(Article article, Article existing) {
        if (article == null || !Integer.valueOf(1).equals(article.getStatus())) {
            return;
        }

        boolean alreadyPublished = existing != null && Integer.valueOf(1).equals(existing.getStatus());
        if (alreadyPublished) {
            return;
        }

        Long authorId = resolveAuthorIdForReview(article, existing);
        boolean requiresReview = true;
        if (authorId != null) {
            requiresReview = articleReviewManager.requiresManualReview(authorId);
        }

        if (requiresReview) {
            article.setReviewStatus(ArticleReviewStatus.PENDING.getCode());
        } else {
            article.setReviewStatus(ArticleReviewStatus.APPROVED.getCode());
            if (article.getPublishTime() == null) {
                article.setPublishTime(DateUtils.getNowDate());
            }
        }
    }

    private Long resolveAuthorIdForReview(Article article, Article existing) {
        if (article != null) {
            Long authorId = article.getAuthorId();
            if (authorId != null && authorId > 0) {
                return authorId;
            }
            Long createBy = article.getCreateBy();
            if (createBy != null && createBy > 0) {
                return createBy;
            }
        }

        if (existing != null) {
            Long authorId = existing.getAuthorId();
            if (authorId != null && authorId > 0) {
                return authorId;
            }
            Long createBy = existing.getCreateBy();
            if (createBy != null && createBy > 0) {
                return createBy;
            }
        }

        return SecurityUtils.getUserIdSafely();
    }

    private void populateAuthorNames(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        Set<Long> userIds = new HashSet<>();
        for (Article article : articles) {
            if (article == null) {
                continue;
            }
            Long authorId = article.getAuthorId();
            if (authorId != null && authorId > 0) {
                userIds.add(authorId);
            } else if (article.getCreateBy() != null) {
                userIds.add(article.getCreateBy());
            }
        }

        if (userIds.isEmpty()) {
            return;
        }

        List<Long> idList = new ArrayList<>(userIds);
        Long tenantId = null;
        try {
            String tenant = TenantContextHolder.getTenantId();
            if (StringUtils.isNotEmpty(tenant)) {
                tenantId = Long.valueOf(tenant);
            }
        } catch (Exception ignored) {
        }

        List<SysUser> users = sysUserMapper.selectUsersByIds(idList, tenantId);
        if (users == null || users.isEmpty()) {
            return;
        }

        Map<Long, String> nicknameMap = new HashMap<>();
        for (SysUser user : users) {
            if (user == null || user.getUserId() == null || StringUtils.isEmpty(user.getNickName())) {
                continue;
            }
            nicknameMap.putIfAbsent(user.getUserId(), user.getNickName());
        }

        if (nicknameMap.isEmpty()) {
            return;
        }

        for (Article article : articles) {
            if (article == null) {
                continue;
            }
            Long key = article.getAuthorId();
            if (key == null || key <= 0) {
                key = article.getCreateBy();
            }
            if (key == null) {
                continue;
            }
            String nickname = nicknameMap.get(key);
            if (StringUtils.isNotEmpty(nickname)) {
                article.setAuthorName(nickname);
            }
        }
    }
}
