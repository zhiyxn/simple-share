package com.simpleshare.article.controller;

import com.simpleshare.article.domain.Article;
import com.simpleshare.article.service.IArticleService;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 文章草稿Controller
 * 处理匿名用户的文章草稿保存
 * 作者：小码哥（wx：xmgcode88）
 */
@RestController
@RequestMapping("/article/draft")
public class ArticleDraftController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleDraftController.class);

    @Autowired
    private IArticleService articleService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成内容哈希值
     */
    private String generateContentHash(String title, String content) {
        String contentToHash = (title != null ? title : "") + "|" + (content != null ? content : "");
        return DigestUtils.md5DigestAsHex(contentToHash.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 规范化状态值，确保与数据库枚举保持一致
     */
    private Integer normalizeStatusValue(String status) {
        if (status == null) {
            return 0;
        }
        String trimmed = status.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        switch (trimmed) {
            case "0":
            case "1":
            case "2":
                return Integer.valueOf(trimmed);
            default:
                String lower = trimmed.toLowerCase();
                if ("draft".equals(lower) || "drafted".equals(lower)) {
                    return 0;
                }
                if ("published".equals(lower) || "publish".equals(lower) || "public".equals(lower) || "true".equals(lower) || "yes".equals(lower)) {
                    return 1;
                }
                if ("offline".equals(lower) || "archived".equals(lower) || "archive".equals(lower) || "private".equals(lower)) {
                    return 2;
                }
                if ("false".equals(lower) || "no".equals(lower)) {
                    return 0;
                }
                return 0;
        }
    }

    /**
     * 检查内容是否发生变化
     */
    private boolean hasContentChanged(Long articleId, String currentHash) {
        if (articleId == null) {
            return true; // 新文章，总是需要保存
        }

        String redisKey = "article:content_hash:" + articleId;
        String lastHash = redisTemplate.opsForValue().get(redisKey);
        
        logger.info("检查文章 {} 内容变化，当前哈希: {}, 上次哈希: {}", articleId, currentHash, lastHash);

        // 如果没有上次的哈希值，说明是第一次保存或缓存已过期
        if (lastHash == null) {
            logger.info("文章 {} 没有上次的哈希值，需要保存", articleId);
            return true;
        }

        boolean changed = !currentHash.equals(lastHash);
        logger.info("文章 {} 内容是否变化: {}", articleId, changed);
        return changed;
    }

    /**
     * 更新内容哈希缓存
     */
    private void updateContentHash(Long articleId, String hash) {
        if (articleId == null) {
            return;
        }

        String redisKey = "article:content_hash:" + articleId;
        redisTemplate.opsForValue().set(redisKey, hash, 24, TimeUnit.HOURS); // 缓存24小时
    }

    /**
     * 检查是否在冷却期内（防止频繁保存）
     */
    private boolean isInCooldownPeriod(Long articleId) {
        if (articleId == null) {
            return false; // 新文章不检查冷却期
        }

        String redisKey = "article:save_cooldown:" + articleId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    /**
     * 设置保存冷却期
     */
    private void setSaveCooldown(Long articleId) {
        if (articleId == null) {
            return;
        }

        String redisKey = "article:save_cooldown:" + articleId;
        redisTemplate.opsForValue().set(redisKey, "1", 5, TimeUnit.SECONDS); // 5秒冷却期，与前端保持一致
    }

    /**
     * 创建文章草稿
     */
    @PostMapping("/article")
    public AjaxResult createDraft(@RequestBody Article article) {
        try {
            logger.info("收到文章保存请求，articleId: {}, title: {}", 
                       article.getArticleId(), 
                       article.getTitle() != null ? article.getTitle().substring(0, Math.min(20, article.getTitle().length())) : "null");
            logger.info("创建草稿时原始status: {}", article.getStatus());
            Integer normalizedStatus = article.getStatus() != null ? article.getStatus() : 0;
            if (!Integer.valueOf(0).equals(normalizedStatus)) {
                logger.warn("创建草稿接口收到非草稿状态值: {}，已强制改为草稿", normalizedStatus);
            }
            
            // 生成当前内容哈希
            String currentHash = generateContentHash(article.getTitle(), article.getContent());

            // 检查是否是新文章且内容为空
            boolean isNewEmptyArticle = article.getArticleId() == null &&
                (article.getTitle() == null || article.getTitle().trim().isEmpty()) &&
                (article.getContent() == null || article.getContent().trim().isEmpty());

            // 如果是新文章且内容为空，不允许保存
            if (isNewEmptyArticle) {
                logger.warn("文章标题和内容同时为空，拒绝保存");
                return AjaxResult.error("标题和内容不能同时为空");
            }

            // 如果是更新现有文章，检查内容是否变化和冷却期
            if (article.getArticleId() != null) {
                // 检查内容是否有变化
                if (!hasContentChanged(article.getArticleId(), currentHash)) {
                    logger.info("文章 {} 内容未变化，跳过保存", article.getArticleId());
                    return AjaxResult.success("内容未变化，无需保存", article);
                }

                // 检查是否在冷却期内
                if (isInCooldownPeriod(article.getArticleId())) {
                    logger.info("文章 {} 在冷却期内，跳过保存", article.getArticleId());
                    return AjaxResult.success("保存过于频繁，请稍后再试", article);
                }

                // 设置冷却期
                setSaveCooldown(article.getArticleId());
                logger.info("文章 {} 内容已变化，调用更新方法", article.getArticleId());
                
                // 如果有articleId，应该调用更新方法而不是创建
                return updateDraft(article);
            }

            // 获取当前登录用户ID（安全方式）
            Long userId = SecurityUtils.getUserIdSafely();
            if (userId != null) {
                article.setCreateBy(userId);
                article.setUpdateBy(userId);
            }

            // 设置为草稿状态
            article.setStatus(0);
            logger.info("创建草稿时规范化后的status: {}", article.getStatus());

            // 如果没有标题，设置默认标题
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                article.setTitle("无标题");
            }

            // 如果没有内容，设置为空字符串
            if (article.getContent() == null) {
                article.setContent("");
            }

            // 如果没有摘要，设置为空字符串
            if (article.getSummary() == null) {
                article.setSummary("");
            }

            // 设置默认值
            if (article.getAllowCopy() == null) {
                article.setAllowCopy("1");
            }
            if (article.getIsTop() == null) {
                article.setIsTop(0);
            }
            if (article.getIsRecommend() == null) {
                article.setIsRecommend(0);
            }
            if (article.getAccessLevel() == null) {
                article.setAccessLevel(0);
            }

            // 保存文章 - 新文章创建
            logger.info("创建新文章草稿");
            int result = articleService.insertArticle(article);
            if (result > 0) {
                // 更新内容哈希缓存
                updateContentHash(article.getArticleId(), currentHash);
                logger.info("新文章创建成功，articleId: {}", article.getArticleId());
                sanitizePassword(article);
                return AjaxResult.success("草稿保存成功", article);
            } else {
                logger.error("新文章创建失败");
                return AjaxResult.error("草稿保存失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("草稿保存失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章草稿
     */
    @PutMapping("/article")
    public AjaxResult updateDraft(@RequestBody Article article) {
        try {
            logger.info("收到文章更新请求，articleId: {}", article.getArticleId());
            logger.info("更新草稿时原始status: {}", article.getStatus());
            Integer normalizedStatus = article.getStatus() != null ? article.getStatus() : 0;
            if (!Integer.valueOf(0).equals(normalizedStatus)) {
                logger.warn("更新草稿接口收到非草稿状态值: {}，已强制改为草稿", normalizedStatus);
            }
            
            // 确保有文章ID
            if (article.getArticleId() == null) {
                logger.error("更新文章时articleId为空");
                return AjaxResult.error("文章ID不能为空");
            }

            // 生成当前内容哈希
            String currentHash = generateContentHash(article.getTitle(), article.getContent());

            // 检查是否有强制更新标志（通过remark字段传递）
            boolean forceUpdate = false;
            if (article.getRemark() != null && article.getRemark().contains("\"forceUpdate\":true")) {
                forceUpdate = true;
                logger.info("检测到强制更新标志，跳过内容变化检查");
            }

            // 检查内容是否有变化（除非强制更新）
            if (!forceUpdate && !hasContentChanged(article.getArticleId(), currentHash)) {
                logger.info("文章 {} 内容未变化，跳过更新", article.getArticleId());
                return AjaxResult.success("内容未变化，无需保存", article);
            }

            // 检查是否在冷却期内（强制更新时跳过）
            if (!forceUpdate && isInCooldownPeriod(article.getArticleId())) {
                logger.info("文章 {} 在冷却期内，跳过更新", article.getArticleId());
                return AjaxResult.success("保存过于频繁，请稍后再试", article);
            }

            // 设置冷却期（强制更新时不设置）
            if (!forceUpdate) {
                setSaveCooldown(article.getArticleId());
            }
            logger.info("开始更新文章 {}", article.getArticleId());

            // 获取当前登录用户ID（安全方式）
            Long userId = SecurityUtils.getUserIdSafely();
            if (userId != null) {
                article.setUpdateBy(userId);
            }

            // 设置为草稿状态（除非明确指定为发布状态）
            article.setStatus(0);
            logger.info("更新草稿时规范化后的status: {}", article.getStatus());

            // 更新文章
            int result = articleService.updateArticle(article);
            if (result > 0) {
                // 更新内容哈希缓存
                updateContentHash(article.getArticleId(), currentHash);
                logger.info("文章 {} 更新成功", article.getArticleId());
                sanitizePassword(article);
                return AjaxResult.success("草稿更新成功", article);
            } else {
                logger.error("文章 {} 更新失败", article.getArticleId());
                return AjaxResult.error("草稿更新失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("草稿更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章草稿（兼容前端路径）
     */
    @PutMapping("/update")
    public AjaxResult updateDraftCompat(@RequestBody Article article) {
        return updateDraft(article);
    }

    private void sanitizePassword(Article article) {
        if (article == null) {
            return;
        }
        boolean protectedFlag;
        if (article.getIsPasswd() != null) {
            protectedFlag = article.getIsPasswd() == 0;
        } else {
            protectedFlag = StringUtils.isNotEmpty(article.getPassword());
            article.setIsPasswd(protectedFlag ? 0 : 1);
        }
        article.setPasswordProtected(protectedFlag);
        article.setPassword(null);
        article.setClearPassword(null);
    }

}
