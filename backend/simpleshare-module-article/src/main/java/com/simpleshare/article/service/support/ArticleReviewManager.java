package com.simpleshare.article.service.support;

import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.system.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 审核策略管理，负责判定文章是否需要人工审核。
 */
@Component
public class ArticleReviewManager {

    private static final Logger log = LoggerFactory.getLogger(ArticleReviewManager.class);

    @Autowired
    private SysUserMapper userMapper;

    @Value("${simpleshare.article.review.whitelist:}")
    private String reviewWhitelist;

    /**
     * 判断指定作者是否需要人工审核。
     *
     * @param authorId 作者ID
     * @return true 需要人工审核；false 可直接发布
     */
    public boolean requiresManualReview(Long authorId) {
        if (authorId == null) {
            log.debug("Article review check: authorId is null, require manual review");
            return true;
        }
        if (SecurityUtils.isAdmin(authorId)) {
            log.debug("Article review check: authorId {} is admin, skip review", authorId);
            return false;
        }
        Long tenantId = SecurityUtils.getTenantId();
        SysUser user = userMapper.selectUserById(authorId, tenantId);
        if (user == null) {
            user = userMapper.selectUserById(authorId, null);
        }
        if (user == null) {
            log.warn("Article review check: unable to load user {} (tenant {}), require review", authorId, tenantId);
            return true;
        }

        if (isWhitelisted(user)) {
            log.info("Article review check: authorId={} username={} matched whitelist, skip review", user.getUserId(), user.getUserName());
            return false;
        }

        Boolean flag = user.getArticleReviewRequired();
        log.info("Article review check: authorId={}, tenantId={}, articleReviewRequired={}", authorId, tenantId, flag);
        if (flag == null) {
            return true;
        }
        return flag;
    }

    /**
     * 判断文章当前状态是否可对外展示。
     */
    public boolean canExpose(Integer reviewStatus) {
        return ArticleReviewStatus.fromCode(reviewStatus).isApproved();
    }

    private boolean isWhitelisted(SysUser user) {
        if (user == null || StringUtils.isEmpty(reviewWhitelist)) {
            return false;
        }
        String[] entries = reviewWhitelist.split(",");
        if (entries.length == 0) {
            return false;
        }
        String username = user.getUserName();
        String normalizedUsername = username != null ? username.trim().toLowerCase(Locale.ROOT) : null;
        String userId = user.getUserId() != null ? String.valueOf(user.getUserId()) : null;
        for (String entry : entries) {
            if (StringUtils.isEmpty(entry)) {
                continue;
            }
            String candidate = entry.trim();
            if (candidate.isEmpty()) {
                continue;
            }
            if (userId != null && candidate.equals(userId)) {
                return true;
            }
            if (normalizedUsername != null && normalizedUsername.equals(candidate.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
