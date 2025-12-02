package com.simpleshare.article.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.Article;
import com.simpleshare.article.domain.ArticleFavorite;
import com.simpleshare.article.dto.ArticleFavoriteView;
import com.simpleshare.article.mapper.ArticleFavoriteMapper;
import com.simpleshare.article.mapper.ArticleMapper;
import com.simpleshare.article.service.IArticleFavoriteService;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章收藏服务实现
 *
 * @author SimpleShare
 */
@Service
public class ArticleFavoriteServiceImpl implements IArticleFavoriteService {

    private static final String DEFAULT_FOLDER = "默认收藏夹";

    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleFavorite addFavorite(Long articleId) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null) {
            throw new ServiceException("请先登录后再收藏文章");
        }

        if (articleId == null) {
            throw new ServiceException("文章ID不能为空");
        }

        Article article = articleMapper.selectArticleByArticleId(articleId);
        if (article == null) {
            throw new ServiceException("文章不存在或不可收藏");
        }
        Integer status = article.getStatus();
        if (status != null && status != 1) {
            throw new ServiceException("文章未发布，暂不可收藏");
        }

        ArticleFavorite existing = articleFavoriteMapper.selectByUserAndArticle(userId, articleId);
        if (existing != null) {
            return existing;
        }

        ArticleFavorite favorite = new ArticleFavorite();
        favorite.setArticleId(articleId);
        favorite.setUserId(userId);
        favorite.setTenantId(resolveTenantId(article));
        favorite.setFolderName(DEFAULT_FOLDER);
        favorite.setCreateBy(userId);
        favorite.setUpdateBy(userId);

        articleFavoriteMapper.insert(favorite);
        articleMapper.incrementCollectCount(articleId);
        return favorite;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavoriteByArticleId(Long articleId) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null) {
            throw new ServiceException("请先登录后再取消收藏");
        }

        if (articleId == null) {
            throw new ServiceException("文章ID不能为空");
        }

        int affected = articleFavoriteMapper.deleteByUserAndArticle(userId, articleId);
        if (affected > 0) {
            articleMapper.decrementCollectCount(articleId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavoriteById(Long favoriteId) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null) {
            throw new ServiceException("请先登录后再取消收藏");
        }

        if (favoriteId == null) {
            throw new ServiceException("收藏ID不能为空");
        }

        ArticleFavorite favorite = articleFavoriteMapper.selectById(favoriteId);
        if (favorite == null || !userId.equals(favorite.getUserId())) {
            return false;
        }

        int affected = articleFavoriteMapper.deleteById(favoriteId);
        if (affected > 0) {
            articleMapper.decrementCollectCount(favorite.getArticleId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveFavorites(List<Long> favoriteIds) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null) {
            throw new ServiceException("请先登录后再操作收藏");
        }

        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return 0;
        }

        List<ArticleFavorite> favorites = articleFavoriteMapper.selectBatchIds(favoriteIds);
        List<ArticleFavorite> ownedFavorites = favorites.stream()
                .filter(favorite -> favorite != null && userId.equals(favorite.getUserId()))
                .collect(java.util.stream.Collectors.toList());
        if (ownedFavorites.isEmpty()) {
            return 0;
        }

        java.util.List<Long> ownedIds = ownedFavorites.stream()
                .map(ArticleFavorite::getId)
                .collect(java.util.stream.Collectors.toList());

        int removed = articleFavoriteMapper.deleteByIds(userId, ownedIds);
        if (removed > 0) {
            ownedFavorites.forEach(favorite -> articleMapper.decrementCollectCount(favorite.getArticleId()));
        }
        return removed;
    }

    @Override
    public ArticleFavorite checkFavorite(Long articleId) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null || articleId == null) {
            return null;
        }
        return articleFavoriteMapper.selectByUserAndArticle(userId, articleId);
    }

    @Override
    public Page<ArticleFavoriteView> pageFavorites(int pageNum, int pageSize, String keyword, Long categoryId, String sortBy, String sortOrder) {
        Long userId = SecurityUtils.getUserIdSafely();
        if (userId == null) {
            throw new ServiceException("请先登录后再查看收藏");
        }
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 20;
        }

        Page<ArticleFavoriteView> page = new Page<>(pageNum, pageSize);
        articleFavoriteMapper.selectFavoritePage(page, userId, normalizeKeyword(keyword), categoryId, normalizeSortBy(sortBy), normalizeSortOrder(sortOrder));
        return page;
    }

    private Long resolveTenantId(Article article) {
        if (article != null && article.getTenantId() != null) {
            return article.getTenantId();
        }
        String contextTenant = TenantContextHolder.getTenantId();
        if (StringUtils.isNotEmpty(contextTenant)) {
            try {
                return Long.parseLong(contextTenant);
            } catch (NumberFormatException ignore) {
                // ignore parse error
            }
        }

        LoginUser loginUser = SecurityUtils.getLoginUserSafely();
        if (loginUser != null && loginUser.getUser() != null) {
            return loginUser.getUser().getTenantId();
        }

        return 1L;
    }

    private String normalizeKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeSortBy(String sortBy) {
        if (StringUtils.isBlank(sortBy)) {
            return "created_at";
        }
        switch (sortBy.toLowerCase()) {
            case "title":
            case "article_title":
                return "title";
            case "author":
            case "author_name":
                return "author";
            case "created_at":
            case "favorite_time":
            default:
                return "created_at";
        }
    }

    private String normalizeSortOrder(String sortOrder) {
        if (StringUtils.isBlank(sortOrder)) {
            return "desc";
        }
        String order = sortOrder.trim().toLowerCase();
        return ("asc".equals(order) || "ascending".equals(order)) ? "asc" : "desc";
    }
}
