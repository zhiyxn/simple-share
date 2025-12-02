package com.simpleshare.article.controller;

import com.simpleshare.article.domain.Article;
import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.article.service.IArticleService;
import com.simpleshare.common.constant.HttpStatus;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysUser;
import com.simpleshare.common.core.domain.model.LoginUser;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.system.domain.vo.TenantProtectionSettings;
import com.simpleshare.system.service.ISysTenantConfigService;
import com.simpleshare.system.service.ITenantStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 前端门户文章Controller
 * 整合所有前端门户相关的文章功能
 * 作者：小码哥（wx：xmgcode88）
 */
@RestController
@RequestMapping("/article")
public class FrontArticleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FrontArticleController.class);
    
    @Autowired
    private IArticleService articleService;

    @Autowired
    private ISysTenantConfigService tenantConfigService;

    @Autowired
    private ITenantStorageService tenantStorageService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // ==================== 公共文章API ====================

    /**
     * 查询已发布的文章列表（前台使用）
     */
    @GetMapping("/articles")
    public TableDataInfo list(Article article) {
        TableDataInfo data = articleService.selectPublishedArticlePage(article);
        sanitizeArticleTable(data);
        return data;
    }

    /**
     * 查询已发布的文章列表（兼容旧路径）
     */
    @GetMapping("/list")
    public TableDataInfo listCompat(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "limit", defaultValue = "12") int limit,
                                   @RequestParam(value = "featured", required = false) Boolean featured,
                                   @RequestParam(value = "status", required = false) String status) {
        Article article = new Article();
        if ("published".equals(status)) {
            article.setStatus(1);
        }
        TableDataInfo data = articleService.selectPublishedArticlePage(article);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 根据文章ID查询文章详情
     */
    @GetMapping("/articles/{articleId}")
    public AjaxResult getInfo(@PathVariable Long articleId,
                              @RequestParam(value = "password", required = false) String providedPassword,
                              @RequestParam(value = "includeMember", required = false, defaultValue = "false") boolean includeMember) {
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "文章不存在");
        }

        boolean originalHasMemberContent = StringUtils.isNotEmpty(article.getMemberContent());
        article.setHasMemberContent(originalHasMemberContent);

        LoginUser loginUser = SecurityUtils.getLoginUserSafely();
        Long currentUserId = loginUser != null ? loginUser.getUserId() : null;
        SysUser currentUser = loginUser != null ? loginUser.getUser() : null;
        boolean isAdmin = currentUserId != null && SecurityUtils.isAdmin(currentUserId);
        boolean isAuthor = currentUserId != null && (
                (article.getAuthorId() != null && article.getAuthorId().equals(currentUserId))
                        || (article.getCreateBy() != null && article.getCreateBy().equals(currentUserId))
        );
        boolean isMember = currentUser != null && currentUser.isVip() && !currentUser.isVipExpired();
        boolean managementAccess = isAdmin || isAuthor;
        boolean privilegedContentAccess = managementAccess || isMember;
        boolean passwordBypass = managementAccess;
        Integer status = article.getStatus();
        boolean isPublished = status != null && status.intValue() == 1;
        ArticleReviewStatus reviewStatus = ArticleReviewStatus.fromCode(article.getReviewStatus());
        boolean reviewApproved = reviewStatus.isApproved();

        if (!isPublished && !managementAccess) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "文章不存在");
        }

        if (!reviewApproved && !managementAccess) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "文章不存在");
        }

        int requiredLevel = article.getAccessLevel() != null ? article.getAccessLevel() : 0;
        int userLevel = resolveUserAccessLevel(loginUser);

        TenantProtectionSettings protectionSettings = tenantConfigService.selectProtectionSettings(article.getTenantId());
        applyCopyPolicy(article, protectionSettings);

        boolean tieredEnabled = requiredLevel > 0 && !"0".equals(article.getEnableTieredRead());
        String previewContent = resolvePreviewContent(article);
        article.setPreviewContent(previewContent);

        Integer isPasswd = article.getIsPasswd();
        boolean passwordProtected;
        if (isPasswd != null) {
            passwordProtected = isPasswd == 0;
        } else {
            passwordProtected = StringUtils.isNotEmpty(article.getPassword());
            article.setIsPasswd(passwordProtected ? 0 : 1);
        }
        boolean passwordVerified = !passwordProtected || passwordBypass;
        if (passwordProtected && !passwordBypass) {
            if (StringUtils.isNotEmpty(providedPassword)
                    && SecurityUtils.matchesPassword(providedPassword, article.getPassword())) {
                passwordVerified = true;
            } else {
                article.setIsPasswd(0);
                article.setPasswordProtected(true);
                article.setPasswordVerified(false);
                article.setPreviewContent(null);
                article.setPreviewOnly(Boolean.TRUE);
                article.setFullReadable(false);
                article.setHasAccess(false);
                article.setAccessDeniedReason("�������Ѽ��ܣ�����������鿴");
                article.setContent(null);
                article.setMemberContent(null);
                article.setPassword(null);
            }
        }

        if (passwordProtected && !passwordVerified && !passwordBypass) {
            article.setIsPasswd(0);
            article.setPasswordProtected(true);
            article.setPasswordVerified(false);
            article.setPreviewContent(null);
            article.setPreviewOnly(Boolean.FALSE);
            article.setFullReadable(false);
            article.setHasAccess(true);
            article.setAccessDeniedReason("文章已加密，请输入访问密码后阅读全文。");
            article.setContent(null);
            article.setMemberContent(null);
            article.setPassword(null);
            article.setMemberContentLocked(originalHasMemberContent && !privilegedContentAccess);
            return success(article);
        }

        if (privilegedContentAccess) {
            article.setFullReadable(true);
            article.setHasAccess(true);
            article.setPreviewOnly(Boolean.FALSE);
            article.setAccessDeniedReason(null);
        } else {
            boolean fullReadable = resolveFullReadable(article, tieredEnabled, loginUser, userLevel >= requiredLevel);
            article.setFullReadable(fullReadable);

            // 检查访问权限
            boolean hasAccess = requiredLevel <= userLevel;

            // 设置访问权限字段
            article.setHasAccess(hasAccess);

            if (!hasAccess) {
                // 对于会员文章，普通用户只能看到预览内容
                article.setPreviewOnly(Boolean.TRUE);
                article.setContent(previewContent);
                article.setAccessDeniedReason("该文章为会员专享，请开通会员后阅读全文");
                article.setMemberContent(null);
            } else if (!fullReadable) {
                article.setPreviewOnly(Boolean.TRUE);
                article.setContent(previewContent);
                article.setMemberContent(null);
            } else {
                article.setPreviewOnly(Boolean.FALSE);
            }
        }

        boolean allowMemberContent = includeMember
                && (isMember || isAuthor)
                && Boolean.TRUE.equals(article.getFullReadable())
                && Boolean.TRUE.equals(article.getHasAccess());
        if (!allowMemberContent) {
            article.setMemberContent(null);
        }

        article.setPasswordProtected(passwordProtected);
        article.setIsPasswd(passwordProtected ? 0 : 1);
        article.setPasswordVerified(passwordVerified);
        article.setPassword(null);

        if (!isMember && !isAuthor) {
            article.setMemberContent(null);
        }

        article.setMemberContentLocked(originalHasMemberContent && !privilegedContentAccess);

        // 处理文章内容中的图片URL域名替换
        article = processArticleImageUrls(article);

        if (isPublished && (!passwordProtected || passwordVerified)) {
            articleService.incrementViewCount(articleId);
        }
        return success(article);
    }

    /**
     * 根据文章ID查询文章详情（兼容旧路径）
     */
    @GetMapping("/detail/{articleId}")
    public AjaxResult getDetail(@PathVariable Long articleId,
                                @RequestParam(value = "password", required = false) String providedPassword,
                                @RequestParam(value = "includeMember", required = false, defaultValue = "false") boolean includeMember) {
        return getInfo(articleId, providedPassword, includeMember);
    }
    
    /**
     * 查询推荐文章列表
     */
    @GetMapping("/articles/recommend")
    public AjaxResult recommendList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectRecommendArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询热门文章列表
     */
    @GetMapping("/articles/hot")
    public AjaxResult hotList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectHotArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询最新文章列表
     */
    @GetMapping("/articles/latest")
    public AjaxResult latestList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectLatestArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询相关文章列表
     */
    @GetMapping("/articles/related")
    public AjaxResult relatedList(@RequestParam Long categoryId, @RequestParam Long articleId, @RequestParam(defaultValue = "5") Integer limit) {
        List<Article> list = articleService.selectRelatedArticleList(categoryId, articleId, limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 根据分类查询文章列表
     */
    @GetMapping("/articles/category/{categoryId}")
    public TableDataInfo categoryArticles(@PathVariable Long categoryId, Article article) {
        article.setCategoryId(categoryId);
        TableDataInfo data = articleService.selectPublishedArticlePage(article);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 根据作者ID查询文章列表
     */
    @GetMapping("/articles/author/{authorId}")
    public AjaxResult authorArticles(@PathVariable Long authorId) {
        List<Article> list = articleService.selectArticleListByAuthorId(authorId);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 搜索文章
     */
    @GetMapping("/articles/search")
    public TableDataInfo search(@RequestParam String keyword, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        TableDataInfo data = articleService.searchArticlesPage(keyword, pageNum, pageSize);
        enhanceSearchResults(data, keyword);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 增加文章浏览次数
     */
    @PostMapping("/articles/{articleId}/view")
    public AjaxResult incrementView(@PathVariable Long articleId) {
        articleService.incrementViewCount(articleId);
        return success();
    }
    
    /**
     * 点赞文章
     */
    @PostMapping("/articles/{articleId}/like")
    public AjaxResult like(@PathVariable Long articleId) {
        articleService.likeArticle(articleId);
        return success();
    }
    
    /**
     * 取消点赞文章
     */
    @DeleteMapping("/articles/{articleId}/like")
    public AjaxResult unlike(@PathVariable Long articleId) {
        articleService.unlikeArticle(articleId);
        return success();
    }
    
    /**
     * 收藏文章
     */
    @PostMapping("/articles/{articleId}/collect")
    public AjaxResult collect(@PathVariable Long articleId) {
        articleService.collectArticle(articleId);
        return success();
    }
    
    /**
     * 取消收藏文章
     */
    @DeleteMapping("/articles/{articleId}/collect")
    public AjaxResult uncollect(@PathVariable Long articleId) {
        articleService.uncollectArticle(articleId);
        return success();
    }
    
    /**
     * 发布文章
     */
    @PatchMapping("/articles/{articleId}/publish")
    public AjaxResult publish(@PathVariable Long articleId, @RequestBody(required = false) Article article) {
        try {
            // 如果有文章设置数据，先更新文章信息
            if (article != null) {
                article.setArticleId(articleId);
                
                // 设置默认值
                if (article.getAllowCopy() == null) {
                    article.setAllowCopy("1");
                }
                if (article.getAccessLevel() == null) {
                    article.setAccessLevel(0);
                }
                
                // 获取当前登录用户ID
                Long userId = SecurityUtils.getUserIdSafely();
                if (userId != null) {
                    article.setUpdateBy(userId);
                }
                
                // 记录接收到的状态值
                logger.info("发布文章 - articleId: {}, 接收到的status: {}", articleId, article.getStatus());
                
                // 更新文章信息（包括状态）
                articleService.updateArticle(article);
                
                // 如果没有指定状态，默认设置为已发布
                if (article.getStatus() == null) {
                    logger.info("发布文章 - articleId: {} 未指定状态，默认设置为已发布(1)", articleId);
                    articleService.publishArticle(articleId);
                } else {
                    // 根据前端发送的状态值设置文章状态
                    Integer status = article.getStatus();
                    logger.info("发布文章 - articleId: {} 根据前端状态值设置为: {}", articleId, status);
                    
                    // 创建只包含状态更新的Article对象
                    Article statusUpdate = new Article();
                    statusUpdate.setArticleId(articleId);
                    statusUpdate.setStatus(status);
                    if (status == 1) {
                        statusUpdate.setPublishTime(new Date());
                    }
                    articleService.updateArticle(statusUpdate);
                }
            } else {
                // 没有文章设置数据，直接发布（设置为已发布）
                logger.info("发布文章 - articleId: {} 无设置数据，默认设置为已发布(1)", articleId);
                articleService.publishArticle(articleId);
            }
            
            return success();
        } catch (Exception e) {
            logger.error("发布文章失败，articleId: {}", articleId, e);
            return AjaxResult.error("发布失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传文章图片
     */
    @PostMapping("/articles/upload-image")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请选择要上传的文件");
        }
        Long tenantId = null;
        String contextTenant = TenantContextHolder.getTenantId();
        if (StringUtils.isNotEmpty(contextTenant)) {
            try {
                tenantId = Long.valueOf(contextTenant);
            } catch (NumberFormatException ignored) {
            }
        }
        StorageUploadResult result = tenantStorageService.uploadFile(tenantId, file, "articles");
        Map<String, Object> data = new HashMap<>();
        data.put("url", result.getUrl());
        data.put("path", result.getPath());
        data.put("storageType", result.getStorageType().getCode());
        return success(data);
    }

    // ==================== 前台文章API（兼容旧路径） ====================

    /**
     * 查询已发布的文章列表（前台使用）
     */
    @GetMapping("/front/articles")
    public TableDataInfo frontList(Article article) {
        TableDataInfo data = articleService.selectPublishedArticlePage(article);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 获取文章详细信息（前台使用）
     */
    @GetMapping(value = "/front/articles/{articleId}")
    public AjaxResult getFrontInfo(@PathVariable("articleId") Long articleId) {
        return getInfo(articleId, null, false);
    }
    
    /**
     * 查询推荐文章列表
     */
    @GetMapping("/front/articles/recommend")
    public AjaxResult frontRecommendList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectRecommendArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询热门文章列表
     */
    @GetMapping("/front/articles/hot")
    public AjaxResult frontHotList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectHotArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询最新文章列表
     */
    @GetMapping("/front/articles/latest")
    public AjaxResult frontLatestList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Article> list = articleService.selectLatestArticleList(limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 查询相关文章列表
     */
    @GetMapping("/front/articles/related")
    public AjaxResult frontRelatedList(@RequestParam Long categoryId, @RequestParam Long articleId, @RequestParam(defaultValue = "5") Integer limit) {
        List<Article> list = articleService.selectRelatedArticleList(categoryId, articleId, limit);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 根据分类查询文章列表
     */
    @GetMapping("/front/articles/category/{categoryId}")
    public TableDataInfo frontCategoryArticles(@PathVariable Long categoryId, Article article) {
        article.setCategoryId(categoryId);
        TableDataInfo data = articleService.selectPublishedArticlePage(article);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 根据作者ID查询文章列表
     */
    @GetMapping("/front/articles/author/{authorId}")
    public AjaxResult frontAuthorArticles(@PathVariable Long authorId) {
        List<Article> list = articleService.selectArticleListByAuthorId(authorId);
        sanitizeArticleList(list);
        return success(list);
    }
    
    /**
     * 搜索文章
     */
    @GetMapping("/front/articles/search")
    public TableDataInfo frontSearch(@RequestParam String keyword, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        TableDataInfo data = articleService.searchArticlesPage(keyword, pageNum, pageSize);
        sanitizeArticleTable(data);
        return data;
    }
    
    /**
     * 点赞文章
     */
    @PostMapping("/front/articles/like/{articleId}")
    public AjaxResult frontLike(@PathVariable Long articleId) {
        return toAjax(articleService.likeArticle(articleId));
    }
    
    /**
     * 取消点赞文章
     */
    @DeleteMapping("/front/articles/like/{articleId}")
    public AjaxResult frontUnlike(@PathVariable Long articleId) {
        return toAjax(articleService.unlikeArticle(articleId));
    }
    
    /**
     * 收藏文章
     */
    @PostMapping("/front/articles/collect/{articleId}")
    public AjaxResult frontCollect(@PathVariable Long articleId) {
        return toAjax(articleService.collectArticle(articleId));
    }
    
    /**
     * 取消收藏文章
     */
    @DeleteMapping("/front/articles/collect/{articleId}")
    public AjaxResult frontUncollect(@PathVariable Long articleId) {
        return toAjax(articleService.uncollectArticle(articleId));
    }

    // ==================== 草稿相关API ====================

    /**
     * 创建文章草稿（兼容旧路径）
     */
    @PostMapping("/draft/create")
    public AjaxResult createDraftCompat(@RequestBody Article article) {
        return createDraft(article);
    }

    /**
     * 创建文章草稿（前台使用）
     */
    @PostMapping("/front/draft/article")
    public AjaxResult createDraft(@RequestBody Article article) {
        try {
            logger.info("收到文章保存请求，articleId: {}, title: {}", 
                       article.getArticleId(), 
                       article.getTitle() != null ? article.getTitle().substring(0, Math.min(20, article.getTitle().length())) : "null");
            
            // 生成当前内容哈希
            String currentHash = generateContentHash(article);

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
    @PutMapping("/front/draft/article")
    public AjaxResult updateDraft(@RequestBody Article article) {
        try {
            logger.info("收到文章更新请求，articleId: {}", article.getArticleId());
            
            // 确保有文章ID
            if (article.getArticleId() == null) {
                logger.error("更新文章时articleId为空");
                return AjaxResult.error("文章ID不能为空");
            }

            // 生成当前内容哈希
            String currentHash = generateContentHash(article);

            // 检查内容是否有变化
            if (!hasContentChanged(article.getArticleId(), currentHash)) {
                logger.info("文章 {} 内容未变化，跳过更新", article.getArticleId());
                return AjaxResult.success("内容未变化，无需保存", article);
            }

            // 检查是否在冷却期内
            if (isInCooldownPeriod(article.getArticleId())) {
                logger.info("文章 {} 在冷却期内，跳过更新", article.getArticleId());
                return AjaxResult.success("保存过于频繁，请稍后再试", article);
            }

            // 设置冷却期
            setSaveCooldown(article.getArticleId());
            logger.info("开始更新文章 {}", article.getArticleId());

            // 获取当前登录用户ID（安全方式）
            Long userId = SecurityUtils.getUserIdSafely();
            if (userId != null) {
                article.setUpdateBy(userId);
            }

            // 设置为草稿状态（除非明确指定为发布状态）
            if (!Integer.valueOf(1).equals(article.getStatus())) {
                article.setStatus(0);
            }

            // 更新文章
            int result = articleService.updateArticle(article);
            if (result > 0) {
                // 更新内容哈希缓存
                updateContentHash(article.getArticleId(), currentHash);
                logger.info("文章 {} 更新成功", article.getArticleId());
                return AjaxResult.success("草稿更新成功", article);
            } else {
                logger.error("文章 {} 更新失败", article.getArticleId());
                return AjaxResult.error("草稿更新失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("草稿更新失败：" + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 生成内容哈希值
     */
    private String generateContentHash(Article article) {
        StringBuilder builder = new StringBuilder();
        appendHashComponent(builder, article.getTitle());
        appendHashComponent(builder, article.getSummary());
        appendHashComponent(builder, article.getContent());
        appendHashComponent(builder, article.getPreviewContent());
        appendHashComponent(builder, article.getCategoryId());
        appendHashComponent(builder, article.getAccessLevel());
        appendHashComponent(builder, article.getEnableTieredRead());
        appendHashComponent(builder, article.getAllowCopy());
        appendHashComponent(builder, article.getEnableWatermark());
        appendHashComponent(builder, article.getIsTop());
        appendHashComponent(builder, article.getIsRecommend());
        appendHashComponent(builder, article.getTags());
        appendHashComponent(builder, article.getRemark());
        appendHashComponent(builder, article.getStatus());
        return DigestUtils.md5DigestAsHex(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendHashComponent(StringBuilder builder, Object value) {
        builder.append('|');
        if (value != null) {
            builder.append(value);
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

        return !currentHash.equals(lastHash);
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

    private void sanitizeArticleTable(TableDataInfo data) {
        if (data == null || data.getRows() == null) {
            return;
        }
        for (Object row : data.getRows()) {
            if (row instanceof Article) {
                sanitizeArticleForPreview((Article) row);
            }
        }
    }

    private void enhanceSearchResults(TableDataInfo data, String keyword) {
        if (data == null || data.getRows() == null || StringUtils.isEmpty(keyword)) {
            return;
        }
        String loweredKeyword = keyword.toLowerCase();
        for (Object row : data.getRows()) {
            if (!(row instanceof Article)) {
                continue;
            }
            Article article = (Article) row;
            if (containsIgnoreCase(article.getTitle(), loweredKeyword) ||
                containsIgnoreCase(article.getSummary(), loweredKeyword)) {
                continue;
            }
            String snippet = buildSnippetFromContent(article.getContent(), loweredKeyword);
            if (StringUtils.isNotEmpty(snippet)) {
                article.setSummary(snippet);
            }
        }
    }

    private boolean containsIgnoreCase(String text, String loweredKeyword) {
        if (StringUtils.isEmpty(text) || StringUtils.isEmpty(loweredKeyword)) {
            return false;
        }
        return text.toLowerCase().contains(loweredKeyword);
    }

    private String buildSnippetFromContent(String content, String loweredKeyword) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(loweredKeyword)) {
            return null;
        }
        String plain = content.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        if (plain.isEmpty()) {
            return null;
        }
        String lowerPlain = plain.toLowerCase();
        int index = lowerPlain.indexOf(loweredKeyword);
        if (index < 0) {
            return null;
        }
        int radius = 40;
        int start = Math.max(0, index - radius);
        int end = Math.min(plain.length(), index + loweredKeyword.length() + radius);
        String snippet = plain.substring(start, end).trim();
        StringBuilder builder = new StringBuilder();
        if (start > 0) {
            builder.append("… ");
        }
        builder.append(snippet);
        if (end < plain.length()) {
            builder.append(" …");
        }
        return builder.toString();
    }

    private void sanitizeArticleList(List<Article> articles) {
        if (articles == null) {
            return;
        }
        for (Article article : articles) {
            sanitizeArticleForPreview(article);
        }
    }

    private void sanitizeArticleForPreview(Article article) {
        if (article == null) {
            return;
        }
        boolean hasMemberContent = StringUtils.isNotEmpty(article.getMemberContent());
        article.setHasMemberContent(hasMemberContent);
        article.setMemberContentLocked(false);
        article.setContent(null);
        article.setMemberContent(null);
        article.setPreviewContent(null);
        article.setRemark(null);
        article.setAllowCopy(null);
        boolean protectedFlag;
        if (article.getIsPasswd() != null) {
            protectedFlag = article.getIsPasswd() == 0;
        } else {
            protectedFlag = StringUtils.isNotEmpty(article.getPassword());
            article.setIsPasswd(protectedFlag ? 0 : 1);
        }
        article.setPasswordProtected(protectedFlag);
        article.setPassword(null);
        article.setPasswordVerified(null);
        article.setClearPassword(null);
    }

    private void applyCopyPolicy(Article article, TenantProtectionSettings protectionSettings) {
        if (protectionSettings == null) {
            return;
        }

        String policy = protectionSettings.getCopyPolicy();
        if (StringUtils.isEmpty(policy)) {
            policy = "follow_article";
        }

        boolean articleAllowCopy = !"0".equals(article.getAllowCopy());
        boolean allowCopy;
        switch (policy) {
            case "global_allow":
                allowCopy = true;
                break;
            case "global_deny":
                allowCopy = false;
                break;
            default:
                allowCopy = articleAllowCopy;
                break;
        }

        article.setAllowCopy(allowCopy ? "1" : "0");
        boolean articleWatermark = !"0".equals(article.getEnableWatermark());
        boolean finalWatermark = protectionSettings.isWatermarkEnabled() && articleWatermark;
        article.setEnableWatermark(finalWatermark ? "1" : "0");
    }

    private String resolvePreviewContent(Article article) {
        if (StringUtils.isNotEmpty(article.getPreviewContent())) {
            return article.getPreviewContent();
        }
        if (StringUtils.isNotEmpty(article.getSummary())) {
            return article.getSummary();
        }
        if (StringUtils.isNotEmpty(article.getContent())) {
            return StringUtils.substring(article.getContent(), 0, Math.min(article.getContent().length(), 800));
        }
        return "";
    }

    private boolean resolveFullReadable(Article article, boolean tieredEnabled, LoginUser loginUser, boolean hasMembershipAccess) {
        if (!hasMembershipAccess) {
            return false;
        }

        if (!tieredEnabled) {
            return true;
        }

        if (loginUser == null) {
            return false;
        }

        if (SecurityUtils.isAdmin(loginUser.getUserId())) {
            return true;
        }

        SysUser sysUser = loginUser.getUser();
        if (sysUser == null) {
            return false;
        }

        if (sysUser.getVipExpireTime() == null) {
            return true;
        }

        return sysUser.getVipExpireTime().after(new Date());
    }

    private int resolveUserAccessLevel(LoginUser loginUser) {
        if (loginUser == null) {
            return 0;
        }

        if (SecurityUtils.isAdmin(loginUser.getUserId())) {
            return Integer.MAX_VALUE;
        }

        SysUser sysUser = loginUser.getUser();
        if (sysUser == null) {
            return 0;
        }

        if (!sysUser.isVip() || sysUser.isVipExpired()) {
            return 0;
        }

        return Integer.MAX_VALUE;
    }

    /**
     * 处理文章内容中的图片URL域名替换
     * 将旧的域名替换为新的域名
     */
    private Article processArticleImageUrls(Article article) {
        if (article == null) {
            return article;
        }

        // 定义域名映射
        String oldDomain = "http://aipic.dataguan.com";
        String newDomain = "https://aipic.easyjx.cn";

        // 处理主内容
        if (StringUtils.isNotEmpty(article.getContent())) {
            String updatedContent = article.getContent()
                    .replace("src=\"" + oldDomain, "src=\"" + newDomain)
                    .replace("src='" + oldDomain, "src='" + newDomain);
            article.setContent(updatedContent);
        }

        // 处理预览内容
        if (StringUtils.isNotEmpty(article.getPreviewContent())) {
            String updatedPreviewContent = article.getPreviewContent()
                    .replace("src=\"" + oldDomain, "src=\"" + newDomain)
                    .replace("src='" + oldDomain, "src='" + newDomain);
            article.setPreviewContent(updatedPreviewContent);
        }

        // 处理会员内容
        if (StringUtils.isNotEmpty(article.getMemberContent())) {
            String updatedMemberContent = article.getMemberContent()
                    .replace("src=\"" + oldDomain, "src=\"" + newDomain)
                    .replace("src='" + oldDomain, "src='" + newDomain);
            article.setMemberContent(updatedMemberContent);
        }

        // 处理封面图片
        if (StringUtils.isNotEmpty(article.getCoverImage()) && article.getCoverImage().startsWith(oldDomain)) {
            article.setCoverImage(article.getCoverImage().replace(oldDomain, newDomain));
        }

        return article;
    }
}
