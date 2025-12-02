package com.simpleshare.article.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.simpleshare.article.domain.Article;
import com.simpleshare.article.dto.ArticleAdminResponse;
import com.simpleshare.article.dto.ArticleBatchDeleteRequest;
import com.simpleshare.article.dto.ArticleDashboardResponse;
import com.simpleshare.article.dto.ArticlePublishRequest;
import com.simpleshare.article.dto.ArticleStatusUpdateRequest;
import com.simpleshare.article.enums.ArticleReviewStatus;
import com.simpleshare.article.service.IArticleService;
import com.simpleshare.article.service.ICategoryService;
import com.simpleshare.article.domain.Category;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.model.PageResponse;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.enums.BusinessType;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;

import com.simpleshare.common.utils.poi.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 后端文章管理Controller
 * 整合所有后端管理相关的文章功能
 * 作者：小码哥（wx：xmgcode88）
 */
@RestController
@RequestMapping("/article/admin")
public class AdminArticleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArticleController.class);

    @Autowired
    private IArticleService articleService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ICategoryService categoryService;

    // ==================== 后台管理API ====================

    /**
     * 获取文章列表（后台管理）
     */
    @GetMapping("/articles")
    @PreAuthorize("@ss.hasPermi('article:article:list')")
    public AjaxResult list(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                           @RequestParam(value = "pageSize", required = false) Integer pageSize,
                           @RequestParam(value = "page", required = false) Integer legacyPage,
                           @RequestParam(value = "size", required = false) Integer legacySize,
                           @RequestParam(value = "limit", required = false) Integer limit,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "title", required = false) String title,
                           @RequestParam(value = "titleExact", required = false) String titleExact,
                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "reviewStatus", required = false) Integer reviewStatus,
                           @RequestParam(value = "accessLevel", required = false) Integer accessLevel,
                           @RequestParam(value = "isTop", required = false) Integer isTop,
                           @RequestParam(value = "isRecommend", required = false) Integer isRecommend) {
        Integer resolvedPageSize = limit != null ? limit : (pageSize != null ? pageSize : legacySize);
        if (resolvedPageSize == null || resolvedPageSize <= 0) {
            resolvedPageSize = 10;
        }
        Integer resolvedPageNum = pageNum != null ? pageNum : legacyPage;
        if (resolvedPageNum == null || resolvedPageNum <= 0) {
            resolvedPageNum = 1;
        }

        PageHelper.startPage(resolvedPageNum, resolvedPageSize);

        Article query = new Article();
        String searchTerm = StringUtils.isNotBlank(keyword) ? keyword : title;
        if (StringUtils.isNotBlank(searchTerm)) {
            String trimmed = searchTerm.trim();
            query.setTitle(trimmed);
            query.getParams().put("keyword", trimmed);
        }
        if (StringUtils.isNotBlank(titleExact)) {
            query.getParams().put("titleExact", titleExact.trim());
        }
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }
        if (StringUtils.isNotBlank(status)) {
            query.setStatus(mapStatusToCode(status));
        }
        if (reviewStatus != null) {
            query.setReviewStatus(reviewStatus);
        }
        if (accessLevel != null) {
            query.setAccessLevel(accessLevel);
        }
        if (isTop != null) {
            query.setIsTop(isTop);
        }
        if (isRecommend != null) {
            query.setIsRecommend(isRecommend);
        }
        List<Article> articles = articleService.selectArticleList(query);
        articles.forEach(this::decorateArticlePassword);
        PageInfo<Article> pageInfo = new PageInfo<>(articles);
        List<ArticleAdminResponse> items = articles.stream()
                .map(ArticleAdminResponse::from)
                .collect(Collectors.toList());

        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 审核列表
     */
    @GetMapping("/reviews")
    @PreAuthorize("@ss.hasPermi('article:article:review')")
    public AjaxResult reviewList(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                                 @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                 @RequestParam(value = "page", required = false) Integer legacyPage,
                                 @RequestParam(value = "size", required = false) Integer legacySize,
                                 @RequestParam(value = "limit", required = false) Integer limit,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "categoryId", required = false) Long categoryId,
                                 @RequestParam(value = "reviewStatus", required = false) Integer reviewStatus) {
        Integer resolvedPageSize = limit != null ? limit : (pageSize != null ? pageSize : legacySize);
        if (resolvedPageSize == null || resolvedPageSize <= 0) {
            resolvedPageSize = 10;
        }
        Integer resolvedPageNum = pageNum != null ? pageNum : legacyPage;
        if (resolvedPageNum == null || resolvedPageNum <= 0) {
            resolvedPageNum = 1;
        }

        PageHelper.startPage(resolvedPageNum, resolvedPageSize);

        Article query = new Article();
        query.setStatus(1);
        query.setReviewStatus(reviewStatus != null ? reviewStatus : ArticleReviewStatus.PENDING.getCode());
        if (StringUtils.isNotBlank(keyword)) {
            String trimmed = keyword.trim();
            query.setTitle(trimmed);
            query.getParams().put("keyword", trimmed);
        }
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }

        List<Article> articles = articleService.selectArticleList(query);
        articles.forEach(this::decorateArticlePassword);
        PageInfo<Article> pageInfo = new PageInfo<>(articles);
        List<ArticleAdminResponse> items = articles.stream()
                .map(ArticleAdminResponse::from)
                .collect(Collectors.toList());

        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * 查询文章列表（传统分页）
     */
    @PreAuthorize("@ss.hasPermi('article:article:list')")
    @GetMapping("/list")
    public TableDataInfo listTable(Article article) {
        startPage();
        List<Article> list = articleService.selectArticleList(article);
        list.forEach(this::decorateArticlePassword);
        return getDataTable(list);
    }

    @PostMapping("/reviews/{articleId}/approve")
    @PreAuthorize("@ss.hasPermi('article:article:review')")
    public AjaxResult approveReview(@PathVariable Long articleId) {
        articleService.reviewArticle(articleId, ArticleReviewStatus.APPROVED);
        Article refreshed = articleService.selectArticleByArticleId(articleId);
        decorateArticlePassword(refreshed);
        return AjaxResult.success(ArticleAdminResponse.from(refreshed));
    }

    @PostMapping("/reviews/{articleId}/reject")
    @PreAuthorize("@ss.hasPermi('article:article:review')")
    public AjaxResult rejectReview(@PathVariable Long articleId) {
        articleService.reviewArticle(articleId, ArticleReviewStatus.REJECTED);
        Article refreshed = articleService.selectArticleByArticleId(articleId);
        decorateArticlePassword(refreshed);
        return AjaxResult.success(ArticleAdminResponse.from(refreshed));
    }

    /**
     * 获取文章详情（后台管理）
     */
    @GetMapping("/articles/{id}")
    @PreAuthorize("@ss.hasPermi('article:article:query')")
    public AjaxResult detail(@PathVariable("id") Long id) {
        Article article = articleService.selectArticleById(id);
        if (article == null) {
            return AjaxResult.error("文章不存在");
        }
        decorateArticlePassword(article);
        return AjaxResult.success(ArticleAdminResponse.from(article));
    }

    /**
     * 获取文章详细信息
     */
    @PreAuthorize("@ss.hasPermi('article:article:query')")
    @GetMapping(value = "/info/{articleId}")
    public AjaxResult getInfo(@PathVariable("articleId") Long articleId) {
        Article article = articleService.selectArticleByArticleId(articleId);
        decorateArticlePassword(article);
        return success(article);
    }

    /**
     * 获取文章详情（兼容前端路径）
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("@ss.hasPermi('article:article:query')")
    public AjaxResult adminDetail(@PathVariable("id") Long id) {
        return detail(id);
    }

    /**
     * 创建文章（后台管理）
     */
    @PostMapping("/articles")
    @PreAuthorize("@ss.hasPermi('article:article:add')")
    public AjaxResult create(@Validated @RequestBody Article article) {
        if (!articleService.checkArticleTitleUnique(article)) {
            return AjaxResult.error("新增文章失败，文章标题已存在");
        }
        AjaxResult passwordValidation = validatePasswordSettings(article, null);
        if (passwordValidation != null) {
            return passwordValidation;
        }
        article.setCreateBy(getUserId());
        article.setAuthorId(getUserId());
        article.setAuthorName(getUsername());
        int rows = articleService.insertArticle(article);
        if (rows > 0) {
            Article saved = articleService.selectArticleById(article.getId());
            decorateArticlePassword(saved);
            return AjaxResult.success(ArticleAdminResponse.from(saved));
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 新增文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Article article) {
        return toAjax(articleService.insertArticle(article));
    }

    /**
     * 更新文章（后台管理）
     */
    @PutMapping("/articles/{id}")
    @PreAuthorize("@ss.hasPermi('article:article:edit')")
    public AjaxResult update(@PathVariable("id") Long id,
                             @Validated @RequestBody Article article) {
        // 添加调试日志
        logger.info("更新文章 ID: {}, 接收到的status值: {}", id, article.getStatus());
        
        Article existing = articleService.selectArticleById(id);
        if (existing == null) {
            return AjaxResult.error("文章不存在");
        }
        article.setId(id);
        if (!articleService.checkArticleTitleUnique(article)) {
            return AjaxResult.error("修改文章失败，文章标题已存在");
        }
        AjaxResult passwordValidation = validatePasswordSettings(article, existing);
        if (passwordValidation != null) {
            return passwordValidation;
        }
        article.setUpdateBy(getUserId());
        
        // 记录更新前的状态
        logger.info("更新前文章状态: {}, 更新后文章状态: {}", existing.getStatus(), article.getStatus());
        
        int rows = articleService.updateArticle(article);
        if (rows > 0) {
            Article refreshed = articleService.selectArticleById(id);
            logger.info("更新后从数据库查询的状态: {}", refreshed.getStatus());
            decorateArticlePassword(refreshed);
            return AjaxResult.success(ArticleAdminResponse.from(refreshed));
        }
        return AjaxResult.error("修改失败");
    }

    /**
     * 修改文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Article article) {
        return toAjax(articleService.updateArticle(article));
    }

    /**
     * 删除文章（后台管理）
     */
    @DeleteMapping("/articles/{id}")
    @PreAuthorize("@ss.hasPermi('article:article:remove')")
    public AjaxResult delete(@PathVariable("id") Long id) {
        int rows = articleService.deleteArticleById(id);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("删除失败");
    }

    /**
     * 删除文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:remove')")
    @DeleteMapping("/{articleIds}")
    public AjaxResult remove(@PathVariable Long[] articleIds) {
        return toAjax(articleService.deleteArticleByArticleIds(articleIds));
    }

    /**
     * 批量删除文章
     */
    @PostMapping("/articles/batch-delete")
    @PreAuthorize("@ss.hasPermi('article:article:remove')")
    public AjaxResult batchDelete(@Validated @RequestBody ArticleBatchDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return AjaxResult.error("请选择要删除的文章");
        }
        Long[] ids = request.getIds().stream()
                .map(Long::valueOf)
                .toArray(Long[]::new);
        int rows = articleService.deleteArticleByIds(ids);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("批量删除失败");
    }

    /**
     * 状态修改
     */
    @PatchMapping("/articles/{id}/status")
    @PreAuthorize("@ss.hasPermi('article:article:edit')")
    public AjaxResult updateStatus(@PathVariable("id") Long id,
                                   @Validated @RequestBody ArticleStatusUpdateRequest request) {
        Article article = articleService.selectArticleById(id);
        if (article == null) {
            return AjaxResult.error("文章不存在");
        }
        article.setStatus(mapStatusToCode(request.getStatus()));
        articleService.updateArticleStatus(article);
        Article refreshed = articleService.selectArticleById(id);
        return AjaxResult.success(ArticleAdminResponse.from(refreshed));
    }

    /**
     * 批量状态修改
     */
    @PostMapping("/articles/batch/status")
    @PreAuthorize("@ss.hasPermi('article:article:edit')")
    public AjaxResult batchUpdateStatus(@Validated @RequestBody ArticleBatchDeleteRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return AjaxResult.error("请选择要修改的文章");
        }
        Integer statusCode = mapStatusToCode("published");
        for (String idStr : request.getIds()) {
            Long id = Long.valueOf(idStr);
            Article article = articleService.selectArticleById(id);
            if (article != null) {
                article.setStatus(statusCode);
                articleService.updateArticleStatus(article);
            }
        }
        return AjaxResult.success();
    }

    /**
     * 发布文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:publish')")
    @PutMapping("/publish/{articleId}")
    public AjaxResult publish(@PathVariable Long articleId,
                              @RequestBody(required = false) ArticlePublishRequest request) {
        Long operatorId = getUserId();
        return publishInternal(articleId, request, operatorId, null, true);
    }

    /**
     * 下线文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:offline')")
    @PutMapping("/offline/{articleId}")
    public AjaxResult offline(@PathVariable Long articleId) {
        return toAjax(articleService.offlineArticle(articleId));
    }

    /**
     * 置顶文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:top')")
    @PutMapping("/top")
    public AjaxResult top(@RequestBody Article article) {
        return toAjax(articleService.topArticle(article.getArticleId(), String.valueOf(article.getIsTop())));
    }

    /**
     * 推荐文章
     */
    @PreAuthorize("@ss.hasPermi('article:article:recommend')")
    @PutMapping("/recommend")
    public AjaxResult recommend(@RequestBody Article article) {
        return toAjax(articleService.recommendArticle(article.getArticleId(), String.valueOf(article.getIsRecommend())));
    }

    /**
     * 导出文章列表
     */
    @PreAuthorize("@ss.hasPermi('article:article:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Article article) {
        List<Article> list = articleService.selectArticleList(article);
        ExcelUtil<Article> util = new ExcelUtil<Article>(Article.class);
        util.exportExcel(response, list, "文章数据");
    }

    // ==================== 用户文章管理API ====================

    /**
     * 查询当前用户的文章列表（兼容旧路径）
     */
    @GetMapping("/user/list")
    public TableDataInfo getUserArticlesList(Article article) {
        return getUserArticles(article);
    }

    /**
     * 查询当前用户的文章列表
     */
    @GetMapping("/user/articles")
    public TableDataInfo getUserArticles(Article article) {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return getDataTable(new ArrayList<>());
        }
        
        // 设置查询条件为当前用户
        article.setCreateBy(userId);
        article.setAuthorId(userId);
        
        // 分页查询
        startPage();
        List<Article> list = articleService.selectArticleList(article);
        list.forEach(this::decorateArticlePassword);
        return getDataTable(list);
    }

    /**
     * 根据文章ID获取文章详情（仅限当前用户的文章）
     */
    @GetMapping("/user/articles/{articleId}")
    public AjaxResult getUserArticle(@PathVariable Long articleId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }
        
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article == null) {
            return error("文章不存在");
        }
        
        // 检查文章是否属于当前用户
        if (!userId.equals(article.getCreateBy())) {
            return error("无权访问此文章");
        }

        decorateArticlePassword(article);
        return success(article);
    }

    /**
     * 发布文章（仅限当前用户的文章）
     */
    @PutMapping("/user/articles/{articleId}/publish")
    public AjaxResult publishUserArticle(@PathVariable Long articleId,
                                         @RequestBody(required = false) ArticlePublishRequest request) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }

        return publishInternal(articleId, request, userId, userId, false);
    }

    /**
     * 下线文章（仅限当前用户的文章）
     */
    @PutMapping("/user/articles/{articleId}/offline")
    public AjaxResult offlineUserArticle(@PathVariable Long articleId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }
        
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article == null) {
            return error("文章不存在");
        }
        
        // 检查文章是否属于当前用户
        if (!userId.equals(article.getCreateBy())) {
            return error("无权操作此文章");
        }
        
        int result = articleService.offlineArticle(articleId);
        return toAjax(result);
    }

    /**
     * 删除文章（仅限当前用户的文章）
     */
    @DeleteMapping("/user/articles/{articleId}")
    public AjaxResult deleteUserArticle(@PathVariable Long articleId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }
        
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article == null) {
            return error("文章不存在");
        }
        
        // 检查文章是否属于当前用户
        if (!userId.equals(article.getCreateBy())) {
            return error("无权操作此文章");
        }
        
        int result = articleService.deleteArticleByArticleIds(new Long[]{articleId});
        return toAjax(result);
    }

    /**
     * 更新文章（仅限当前用户的文章）
     */
    @PutMapping("/user/articles/{articleId}")
    public AjaxResult updateUserArticle(@PathVariable Long articleId, @RequestBody Article article) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }
        
        Article existingArticle = articleService.selectArticleByArticleId(articleId);
        if (existingArticle == null) {
            return error("文章不存在");
        }
        
        // 检查文章是否属于当前用户
        if (!userId.equals(existingArticle.getCreateBy())) {
            return error("无权操作此文章");
        }
        
        // 设置文章ID和更新者
        article.setArticleId(articleId);
        article.setUpdateBy(userId);

        AjaxResult passwordValidation = validatePasswordSettings(article, existingArticle);
        if (passwordValidation != null) {
            return passwordValidation;
        }

        int result = articleService.updateArticle(article);
        if (result > 0) {
            Article refreshed = articleService.selectArticleByArticleId(articleId);
            decorateArticlePassword(refreshed);
            return success(refreshed);
        }
        return toAjax(result);
    }

    /**
     * 后台文章仪表盘统计
     */
    @GetMapping("/stats")
    @PreAuthorize("@ss.hasPermi('article:article:list')")
    public AjaxResult getAdminStats(@RequestParam(value = "range", required = false) String range,
                                    @RequestParam(value = "startDate", required = false) String startDate,
                                    @RequestParam(value = "endDate", required = false) String endDate,
                                    @RequestParam(value = "status", required = false) Integer status,
                                    @RequestParam(value = "accessLevel", required = false) Integer accessLevel,
                                    @RequestParam(value = "categoryId", required = false) Long categoryId,
                                    @RequestParam(value = "authorId", required = false) Long authorId) {
        Article query = new Article();
        query.setDelFlag("0");

        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null) {
            query.setTenantId(tenantId);
        }
        if (status != null) {
            query.setStatus(status);
        }
        if (accessLevel != null) {
            query.setAccessLevel(accessLevel);
        }
        if (categoryId != null) {
            query.setCategoryId(categoryId);
        }
        if (authorId != null) {
            query.setAuthorId(authorId);
        }

        List<Article> articles = articleService.selectArticleList(query);
        if (articles == null) {
            articles = Collections.emptyList();
        }
        List<Article> activeArticles = articles.stream()
                .filter(article -> article.getDelFlag() == null || !"1".equals(article.getDelFlag()))
                .collect(Collectors.toList());

        RangeDefinition rangeDefinition = resolveRange(range, startDate, endDate);
        ArticleDashboardResponse response = buildDashboardResponse(activeArticles, rangeDefinition);
        return AjaxResult.success(response);
    }

    /**
     * 获取当前用户的文章统计信息
     */
    @GetMapping("/user/articles/stats")
    public AjaxResult getUserArticleStats() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return unauthorized("用户未登录");
        }
        
        // 查询用户所有文章
        Article queryArticle = new Article();
        queryArticle.setCreateBy(userId);
        List<Article> allArticles = articleService.selectArticleList(queryArticle);
        
        // 统计各状态文章数量
        long totalCount = allArticles.size();
        long publishedCount = allArticles.stream().filter(this::isApprovedArticle).count();
        long draftCount = allArticles.stream().filter(a -> Integer.valueOf(0).equals(a.getStatus())).count();
        long offlineCount = allArticles.stream().filter(a -> Integer.valueOf(2).equals(a.getStatus())).count();
        long pendingReviewCount = allArticles.stream().filter(this::isPendingReviewArticle).count();
        long rejectedCount = allArticles.stream().filter(this::isRejectedArticle).count();
        
        // 统计总浏览量
        long totalViews = allArticles.stream().mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();
        
        return success(new Object() {
            public final long total = totalCount;
            public final long published = publishedCount;
            public final long draft = draftCount;
            public final long offline = offlineCount;
            public final long pending = pendingReviewCount;
            public final long rejected = rejectedCount;
            public final long views = totalViews;
        });
    }

    // ==================== 兼容性端点 ====================

    /**
     * 发布用户文章（兼容POST请求）
     */
    @PostMapping("/user/publish/{articleId}")
    public AjaxResult publishUserArticlePost(@PathVariable Long articleId,
                                             @RequestBody(required = false) ArticlePublishRequest request) {
        return publishUserArticle(articleId, request);
    }

    /**
     * 删除用户文章（兼容DELETE请求）
     */
    @DeleteMapping("/user/delete/{articleId}")
    public AjaxResult deleteUserArticleCompat(@PathVariable Long articleId) {
        return deleteUserArticle(articleId);
    }

    /**
     * 用户文章下线（兼容POST请求）
     */
    @PostMapping("/user/offline/{articleId}")
    public AjaxResult offlineUserArticlePost(@PathVariable Long articleId) {
        return offlineUserArticle(articleId);
    }

    /**
     * 获取用户文章统计（兼容性端点）
     */
    @GetMapping("/user/stats")
    public AjaxResult getUserStatsCompat() {
        return getUserArticleStats();
    }

    /**
     * 发布文章（兼容POST请求）
     */
    @PostMapping("/publish/{articleId}")
    @PreAuthorize("@ss.hasPermi('article:article:publish')")
    public AjaxResult publishPost(@PathVariable Long articleId,
                                  @RequestBody(required = false) ArticlePublishRequest request) {
        Long operatorId = getUserId();
        return publishInternal(articleId, request, operatorId, null, true);
    }

    /**
     * 下线文章（兼容POST请求）
     */
    @PostMapping("/offline/{articleId}")
    @PreAuthorize("@ss.hasPermi('article:article:offline')")
    public AjaxResult offlinePost(@PathVariable Long articleId) {
        return offline(articleId);
    }

    /**
     * 删除文章（兼容DELETE请求）
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@ss.hasPermi('article:article:remove')")
    public AjaxResult deleteCompat(@PathVariable("id") Long id) {
        return delete(id);
    }

    private AjaxResult publishInternal(Long articleId,
                                       ArticlePublishRequest request,
                                       Long operatorId,
                                       Long requiredOwnerId,
                                       boolean adminView) {
        Article existing = articleService.selectArticleByArticleId(articleId);
        if (existing == null) {
            return AjaxResult.error("文章不存在");
        }

        if (requiredOwnerId != null && !requiredOwnerId.equals(existing.getCreateBy())) {
            return AjaxResult.error("无权操作此文章");
        }

        Article payload = new Article();
        payload.setArticleId(articleId);
        if (request != null) {
            payload = request.applyTo(payload, existing);
            payload.setArticleId(articleId);
        }
        // 如果请求中没有指定status，则默认设置为发布状态
        if (payload.getStatus() == null) {
            payload.setStatus(1);
        }
        if (operatorId != null) {
            payload.setUpdateBy(operatorId);
        }
        payload.setUpdateTime(LocalDateTime.now());

        AjaxResult passwordValidation = validatePasswordSettings(payload, existing);
        if (passwordValidation != null) {
            return passwordValidation;
        }

        int updated = articleService.updateArticle(payload);
        if (updated <= 0) {
            return toAjax(updated);
        }

        if (payload.getStatus() != null && payload.getStatus() == 1) {
            articleService.publishArticle(articleId);
        }

        Article refreshed = articleService.selectArticleByArticleId(articleId);
        decorateArticlePassword(refreshed);
        if (adminView) {
            return AjaxResult.success(ArticleAdminResponse.from(refreshed));
        }
        return AjaxResult.success(refreshed);
    }

    // ==================== 工具方法 ====================

    private AjaxResult validatePasswordSettings(Article payload, Article existing) {
        if (payload == null) {
            return null;
        }

        boolean existingProtected = existing != null && StringUtils.isNotEmpty(existing.getPassword());
        boolean hasNewPassword = StringUtils.isNotEmpty(payload.getPassword());
        Boolean passwordProtected = payload.getPasswordProtected();
        Integer isPasswd = payload.getIsPasswd();
        boolean clearPassword = Boolean.TRUE.equals(payload.getClearPassword());

        if (isPasswd != null) {
            if (isPasswd == 0) {
                passwordProtected = Boolean.TRUE;
                payload.setPasswordProtected(true);
            } else if (isPasswd == 1) {
                passwordProtected = Boolean.FALSE;
                payload.setPasswordProtected(false);
                payload.setClearPassword(true);
                clearPassword = true;
            }
        }

        if (Boolean.TRUE.equals(passwordProtected)) {
            if (!hasNewPassword && !existingProtected) {
                return AjaxResult.error("文章不存在");
            }
            payload.setIsPasswd(0);
        } else if (Boolean.FALSE.equals(passwordProtected)) {
            payload.setIsPasswd(1);
            payload.setClearPassword(true);
            clearPassword = true;
        }

        if (clearPassword) {
            payload.setPassword(null);
            payload.setPasswordProtected(false);
            payload.setIsPasswd(1);
            return null;
        }

        if (hasNewPassword) {
            payload.setPasswordProtected(true);
            payload.setIsPasswd(0);
        } else if (payload.getIsPasswd() == null) {
            if (existing != null && existing.getIsPasswd() != null) {
                payload.setIsPasswd(existing.getIsPasswd());
            } else if (existingProtected) {
                payload.setIsPasswd(0);
            }
        }

        return null;
    }


    private void decorateArticlePassword(Article article) {
        if (article == null) {
            return;
        }
        Integer isPasswd = article.getIsPasswd();
        boolean protectedFlag;
        if (isPasswd != null) {
            protectedFlag = isPasswd == 0;
        } else {
            protectedFlag = StringUtils.isNotEmpty(article.getPassword());
            if (!protectedFlag && Boolean.TRUE.equals(article.getPasswordProtected())) {
                protectedFlag = true;
            }
            article.setIsPasswd(protectedFlag ? 0 : 1);
        }
        article.setPasswordProtected(protectedFlag);
        article.setPassword(null);
        article.setClearPassword(null);
    }


    private ArticleDashboardResponse buildDashboardResponse(List<Article> articles, RangeDefinition rangeDefinition) {
        ArticleDashboardResponse response = new ArticleDashboardResponse();

        ArticleDashboardResponse.Range responseRange = new ArticleDashboardResponse.Range();
        responseRange.setPreset(rangeDefinition.getPreset());
        responseRange.setStartDate(rangeDefinition.getStart().toString());
        responseRange.setEndDate(rangeDefinition.getEnd().toString());
        response.setRange(responseRange);

        ArticleDashboardResponse.Overview overview = response.getOverview();
        overview.setTotalArticles(articles.size());
        long publishedCount = articles.stream().filter(this::isApprovedArticle).count();
        overview.setPublishedArticles(publishedCount);
        overview.setDraftArticles(articles.stream().filter(a -> Integer.valueOf(0).equals(a.getStatus())).count());
        overview.setPrivateArticles(articles.stream().filter(this::isPrivateArticle).count());
        overview.setReviewPendingArticles(articles.stream().filter(this::isPendingReviewArticle).count());
        overview.setReviewRejectedArticles(articles.stream().filter(this::isRejectedArticle).count());
        long totalViews = articles.stream().mapToLong(this::safeViewCount).sum();
        overview.setTotalViews(totalViews);
        overview.setTotalLikes(articles.stream().mapToLong(a -> safeLong(a.getLikeCount())).sum());
        overview.setTotalCollects(articles.stream().mapToLong(a -> safeLong(a.getCollectCount())).sum());
        overview.setTotalComments(articles.stream().mapToLong(a -> safeLong(a.getCommentCount())).sum());
        overview.setTotalShares(articles.stream().mapToLong(this::safeShareCount).sum());
        overview.setWeeklyNewArticles(countCreatedWithinDays(articles, 7));
        overview.setTodayNewArticles(countCreatedOnDate(articles, LocalDate.now()));
        overview.setLastUpdated(LocalDateTime.now());

        LocalDate previousEnd = rangeDefinition.getStart().minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(rangeDefinition.getDays() - 1L);
        List<Article> currentRangeArticles = filterByDateRange(articles, rangeDefinition.getStart(), rangeDefinition.getEnd());
        List<Article> previousRangeArticles = filterByDateRange(articles, previousStart, previousEnd);

        long currentViews = currentRangeArticles.stream().mapToLong(this::safeViewCount).sum();
        long previousViews = previousRangeArticles.stream().mapToLong(this::safeViewCount).sum();
        long currentEngagement = currentRangeArticles.stream().mapToLong(this::calculateEngagement).sum();
        long previousEngagement = previousRangeArticles.stream().mapToLong(this::calculateEngagement).sum();

        overview.setViewGrowthRate(calcGrowthRate(currentViews, previousViews));
        overview.setEngagementGrowthRate(calcGrowthRate(currentEngagement, previousEngagement));

        buildTrend(rangeDefinition, currentRangeArticles, response.getTrend(), overview.getViewGrowthRate(), currentViews);
        buildFunnel(currentRangeArticles, response.getFunnel());

        List<Category> categories = categoryService.selectCategoryList(new Category());
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName, (a, b) -> a));
        response.setSegments(buildSegments(articles, categoryNameMap));
        response.setLeaderboards(buildLeaderboards(articles, categoryNameMap));
        response.setModeration(buildModerationSnapshot(articles));
        response.setInsights(buildInsights(overview, response.getSegments()));

        return response;
    }

    private void buildTrend(RangeDefinition rangeDefinition,
                            List<Article> rangeArticles,
                            ArticleDashboardResponse.Trend trend,
                            double wowChange,
                            long currentViews) {
        Map<LocalDate, DailyAggregate> aggregates = new LinkedHashMap<>();
        for (Article article : rangeArticles) {
            LocalDate date = resolveActivityDate(article);
            if (date == null) {
                continue;
            }
            DailyAggregate aggregate = aggregates.computeIfAbsent(date, unused -> new DailyAggregate());
            if (isApprovedArticle(article)) {
                aggregate.published += 1;
            }
            aggregate.views += safeViewCount(article);
            aggregate.likes += safeLong(article.getLikeCount());
            aggregate.comments += safeLong(article.getCommentCount());
            aggregate.shares += safeShareCount(article);
        }

        List<ArticleDashboardResponse.TrendPoint> points = new ArrayList<>();
        LocalDate cursor = rangeDefinition.getStart();
        while (!cursor.isAfter(rangeDefinition.getEnd())) {
            DailyAggregate aggregate = aggregates.get(cursor);
            ArticleDashboardResponse.TrendPoint point = new ArticleDashboardResponse.TrendPoint();
            point.setDate(cursor.toString());
            if (aggregate != null) {
                point.setPublished(aggregate.published);
                point.setViews(aggregate.views);
                point.setLikes(aggregate.likes);
                point.setComments(aggregate.comments);
                point.setShares(aggregate.shares);
            } else {
                point.setPublished(0);
                point.setViews(0);
                point.setLikes(0);
                point.setComments(0);
                point.setShares(0);
            }
            points.add(point);
            cursor = cursor.plusDays(1);
        }
        trend.setPoints(points);
        trend.setTotalViews(currentViews);
        trend.setAverageViewsPerArticle(rangeArticles.isEmpty() ? 0.0 : (double) currentViews / rangeArticles.size());
        trend.setWowChange(wowChange);
    }

    private void buildFunnel(List<Article> rangeArticles, ArticleDashboardResponse.Funnel funnel) {
        long publishedCount = rangeArticles.stream().filter(this::isApprovedArticle).count();
        long rangeViews = rangeArticles.stream().mapToLong(this::safeViewCount).sum();
        long interactions = rangeArticles.stream()
                .mapToLong(article -> safeLong(article.getLikeCount()) + safeLong(article.getCommentCount()))
                .sum();
        long collectTotal = rangeArticles.stream().mapToLong(article -> safeLong(article.getCollectCount())).sum();
        long shareTotal = rangeArticles.stream().mapToLong(this::safeShareCount).sum();

        List<ArticleDashboardResponse.FunnelStage> stages = new ArrayList<>();
        stages.add(createFunnelStage("publish", "发布", publishedCount, 1.0, "发布成功的文章数量"));
        stages.add(createFunnelStage("view", "浏览", rangeViews, calcRate(rangeViews, publishedCount), "平均每篇文章浏览量"));
        stages.add(createFunnelStage("engage", "互动", interactions, calcRate(interactions, rangeViews), "点赞 + 评论总数"));
        long baseline = interactions == 0 ? rangeViews : interactions;
        stages.add(createFunnelStage("collect", "收藏", collectTotal, calcRate(collectTotal, baseline), "收藏数"));
        long shareBaseline = collectTotal == 0 ? rangeViews : collectTotal;
        stages.add(createFunnelStage("share", "分享", shareTotal, calcRate(shareTotal, shareBaseline), "分享次数"));
        funnel.setStages(stages);
    }

    private ArticleDashboardResponse.SegmentCollection buildSegments(List<Article> articles,
                                                                     Map<Long, String> categoryNameMap) {
        ArticleDashboardResponse.SegmentCollection collection = new ArticleDashboardResponse.SegmentCollection();
        collection.setCategories(buildCategorySegments(articles, categoryNameMap));
        collection.setTags(buildTagSegments(articles));
        collection.setAccessLevels(buildAccessSegments(articles));
        return collection;
    }

    private List<ArticleDashboardResponse.SegmentItem> buildCategorySegments(List<Article> articles,
                                                                             Map<Long, String> categoryNameMap) {
        Map<String, SegmentAccumulator> categoryMap = new HashMap<>();
        for (Article article : articles) {
            String key = article.getCategoryId() != null ? String.valueOf(article.getCategoryId()) : "uncategorized";
            SegmentAccumulator accumulator = categoryMap.computeIfAbsent(key, unused -> new SegmentAccumulator());
            accumulator.key = key;
            accumulator.label = article.getCategoryId() != null
                    ? categoryNameMap.getOrDefault(article.getCategoryId(),
                    StringUtils.defaultIfEmpty(article.getCategoryName(), "未分类"))
                    : "未分类";
            accumulator.addArticle(article, this);
        }
        long total = articles.size();
        return categoryMap.values().stream()
                .sorted(Comparator.comparingLong(SegmentAccumulator::getViewCount).reversed())
                .limit(6)
                .map(acc -> acc.toSegmentItem(total))
                .collect(Collectors.toList());
    }

    private List<ArticleDashboardResponse.SegmentItem> buildTagSegments(List<Article> articles) {
        Map<String, SegmentAccumulator> tagMap = new HashMap<>();
        for (Article article : articles) {
            String tags = article.getTags();
            if (StringUtils.isBlank(tags)) {
                continue;
            }
            for (String raw : tags.split(",")) {
                String tag = raw.trim();
                if (StringUtils.isEmpty(tag)) {
                    continue;
                }
                SegmentAccumulator accumulator = tagMap.computeIfAbsent(tag, unused -> {
                    SegmentAccumulator acc = new SegmentAccumulator();
                    acc.key = tag;
                    acc.label = tag;
                    return acc;
                });
                accumulator.addArticle(article, this);
            }
        }
        long total = articles.size();
        return tagMap.values().stream()
                .sorted(Comparator.comparingLong(SegmentAccumulator::getViewCount).reversed())
                .limit(8)
                .map(acc -> acc.toSegmentItem(total))
                .collect(Collectors.toList());
    }

    private List<ArticleDashboardResponse.SegmentItem> buildAccessSegments(List<Article> articles) {
        Map<String, SegmentAccumulator> accessMap = new HashMap<>();
        for (Article article : articles) {
            Integer level = article.getAccessLevel();
            String key = level != null ? String.valueOf(level) : "0";
            SegmentAccumulator accumulator = accessMap.computeIfAbsent(key, unused -> new SegmentAccumulator());
            accumulator.key = key;
            accumulator.label = resolveAccessLabel(level);
            accumulator.addArticle(article, this);
        }
        long total = articles.size();
        return accessMap.values().stream()
                .sorted(Comparator.comparingLong(SegmentAccumulator::getArticleCount).reversed())
                .map(acc -> acc.toSegmentItem(total))
                .collect(Collectors.toList());
    }

    private ArticleDashboardResponse.Leaderboard buildLeaderboards(List<Article> articles,
                                                                   Map<Long, String> categoryNameMap) {
        ArticleDashboardResponse.Leaderboard leaderboard = new ArticleDashboardResponse.Leaderboard();
        leaderboard.setTopViews(articles.stream()
                .sorted(Comparator.comparingLong(this::safeViewCount).reversed())
                .limit(6)
                .map(article -> toArticleTile(article, categoryNameMap))
                .collect(Collectors.toList()));
        leaderboard.setTopEngagement(articles.stream()
                .sorted(Comparator.comparingLong(this::calculateEngagement).reversed())
                .limit(6)
                .map(article -> toArticleTile(article, categoryNameMap))
                .collect(Collectors.toList()));
        leaderboard.setLatestPublished(articles.stream()
                .sorted(Comparator.comparing(this::resolvePublishDateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(6)
                .map(article -> toArticleTile(article, categoryNameMap))
                .collect(Collectors.toList()));
        return leaderboard;
    }

    private ArticleDashboardResponse.ArticleTile toArticleTile(Article article,
                                                               Map<Long, String> categoryNameMap) {
        ArticleDashboardResponse.ArticleTile tile = new ArticleDashboardResponse.ArticleTile();
        tile.setArticleId(article.getArticleId());
        tile.setTitle(article.getTitle());
        tile.setSummary(StringUtils.defaultIfEmpty(article.getSummary(), ""));
        tile.setAuthorName(StringUtils.defaultIfEmpty(article.getAuthorName(), "未设置"));
        tile.setCategoryName(article.getCategoryId() != null
                ? categoryNameMap.getOrDefault(article.getCategoryId(),
                StringUtils.defaultIfEmpty(article.getCategoryName(), "未分类"))
                : "未分类");
        tile.setStatus(article.getStatus());
        tile.setAccessLevel(article.getAccessLevel());
        tile.setRecommend(Integer.valueOf(1).equals(article.getIsRecommend()));
        tile.setTop(Integer.valueOf(1).equals(article.getIsTop()));
        boolean passwordProtected = Boolean.TRUE.equals(article.getPasswordProtected())
                || (article.getIsPasswd() != null && article.getIsPasswd() == 0);
        tile.setPasswordProtected(passwordProtected);
        LocalDateTime publishDate = resolvePublishDateTime(article);
        tile.setPublishTime(publishDate != null ? publishDate.toString() : null);
        tile.setViewCount(safeViewCount(article));
        tile.setLikeCount(safeLong(article.getLikeCount()));
        tile.setCollectCount(safeLong(article.getCollectCount()));
        tile.setCommentCount(safeLong(article.getCommentCount()));
        tile.setShareCount(safeShareCount(article));
        tile.setEngagement(calculateEngagement(article));
        return tile;
    }

    private ArticleDashboardResponse.Moderation buildModerationSnapshot(List<Article> articles) {
        ArticleDashboardResponse.Moderation moderation = new ArticleDashboardResponse.Moderation();
        moderation.setPendingReviews(articles.stream().filter(this::isPendingReviewArticle).count());
        moderation.setRejectedReviews(articles.stream().filter(this::isRejectedArticle).count());
        long oldestDraftDays = articles.stream()
                .filter(a -> Integer.valueOf(0).equals(a.getStatus()) && a.getCreateTime() != null)
                .map(Article::getCreateTime)
                .map(LocalDateTime::toLocalDate)
                .map(date -> ChronoUnit.DAYS.between(date, LocalDate.now()))
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        moderation.setOldestDraftDays(oldestDraftDays);
        moderation.setRecords(articles.stream()
                .sorted(Comparator.comparing(Article::getUpdateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(8)
                .map(this::toModerationRecord)
                .collect(Collectors.toList()));
        return moderation;
    }

    private ArticleDashboardResponse.ModerationRecord toModerationRecord(Article article) {
        ArticleDashboardResponse.ModerationRecord record = new ArticleDashboardResponse.ModerationRecord();
        record.setArticleId(article.getArticleId());
        record.setTitle(article.getTitle());
        record.setReviewStatus(article.getReviewStatus());
        record.setStatus(article.getStatus());
        record.setReviewer(article.getUpdateBy() != null ? "用户#" + article.getUpdateBy() : "系统");
        LocalDateTime updateTime = article.getUpdateTime();
        record.setUpdatedAt(updateTime != null ? updateTime.toString() : null);
        return record;
    }

    private List<ArticleDashboardResponse.Insight> buildInsights(ArticleDashboardResponse.Overview overview,
                                                                 ArticleDashboardResponse.SegmentCollection segments) {
        List<ArticleDashboardResponse.Insight> insights = new ArrayList<>();
        if (overview.getDraftArticles() > overview.getPublishedArticles()) {
            insights.add(createInsight("草稿积压", "草稿数量已超过已发布文章，请及时处理。", "warning"));
        }
        if (overview.getReviewPendingArticles() > 0) {
            insights.add(createInsight("待审核内容", "当前有 " + overview.getReviewPendingArticles() + " 篇文章待审核。", "info"));
        }
        if (overview.getViewGrowthRate() < 0) {
            insights.add(createInsight("访问量下滑", "近期开启访问量环比下降，建议检查内容投放。", "danger"));
        }
        if (!segments.getCategories().isEmpty()) {
            ArticleDashboardResponse.SegmentItem topCategory = segments.getCategories().get(0);
            insights.add(createInsight("热门分类",
                    topCategory.getLabel() + " 占比 " + String.format("%.1f%%", topCategory.getPercentage() * 100),
                    "success"));
        }
        if (insights.isEmpty()) {
            insights.add(createInsight("运行健康", "指标稳定，暂无异常。", "success"));
        }
        return insights;
    }

    private ArticleDashboardResponse.Insight createInsight(String title, String description, String severity) {
        ArticleDashboardResponse.Insight insight = new ArticleDashboardResponse.Insight();
        insight.setTitle(title);
        insight.setDescription(description);
        insight.setSeverity(severity);
        return insight;
    }

    private double calcGrowthRate(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    private double calcRate(long value, long base) {
        if (base <= 0) {
            return 0.0;
        }
        return (double) value / base;
    }

    private long calculateEngagement(Article article) {
        return safeLong(article.getLikeCount())
                + safeLong(article.getCommentCount())
                + safeLong(article.getCollectCount())
                + safeShareCount(article);
    }

    private List<Article> filterByDateRange(List<Article> articles, LocalDate start, LocalDate end) {
        return articles.stream()
                .filter(article -> {
                    LocalDate date = resolveActivityDate(article);
                    return date != null && !date.isBefore(start) && !date.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    private LocalDate resolveActivityDate(Article article) {
        LocalDate publishDate = toLocalDate(article.getPublishTime());
        if (publishDate != null) {
            return publishDate;
        }
        LocalDateTime createTime = article.getCreateTime();
        if (createTime != null) {
            return createTime.toLocalDate();
        }
        return null;
    }

    private RangeDefinition resolveRange(String preset, String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        int rangeDays = resolveRangeDays(preset);
        LocalDate resolvedEnd = parseDateOrDefault(endDate, today);
        LocalDate resolvedStart = parseDateOrDefault(startDate, resolvedEnd.minusDays(rangeDays - 1L));
        if (resolvedStart.isAfter(resolvedEnd)) {
            LocalDate swap = resolvedStart;
            resolvedStart = resolvedEnd;
            resolvedEnd = swap;
        }
        int actualDays = (int) ChronoUnit.DAYS.between(resolvedStart, resolvedEnd) + 1;
        String resolvedPreset = StringUtils.isNotBlank(preset)
                ? preset
                : (actualDays <= 7 ? "7d" : actualDays <= 30 ? "30d" : "90d");
        return new RangeDefinition(resolvedStart, resolvedEnd, actualDays, resolvedPreset);
    }

    private int resolveRangeDays(String preset) {
        if (StringUtils.isBlank(preset)) {
            return 30;
        }
        String normalized = preset.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("7") || normalized.contains("week")) {
            return 7;
        }
        if (normalized.startsWith("90") || normalized.contains("quarter")) {
            return 90;
        }
        if (normalized.startsWith("180")) {
            return 180;
        }
        return 30;
    }

    private long countCreatedWithinDays(List<Article> articles, int days) {
        if (days <= 0) {
            return 0;
        }
        LocalDate threshold = LocalDate.now().minusDays(days - 1L);
        return articles.stream()
                .map(Article::getCreateTime)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(date -> !date.isBefore(threshold))
                .count();
    }

    private long countCreatedOnDate(List<Article> articles, LocalDate target) {
        if (target == null) {
            return 0;
        }
        return articles.stream()
                .map(Article::getCreateTime)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(target::equals)
                .count();
    }

    private LocalDate parseDateOrDefault(String value, LocalDate fallback) {
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private ArticleDashboardResponse.FunnelStage createFunnelStage(String key, String label, long value, double conversion, String hint) {
        ArticleDashboardResponse.FunnelStage stage = new ArticleDashboardResponse.FunnelStage();
        stage.setKey(key);
        stage.setLabel(label);
        stage.setValue(value);
        stage.setConversion(conversion);
        stage.setHint(hint);
        return stage;
    }

    private String resolveAccessLabel(Integer accessLevel) {
        if (accessLevel == null || accessLevel == 0) {
            return "公开阅读";
        }
        if (accessLevel == 1) {
            return "会员专享";
        }
        return "私密内容";
    }

    private LocalDateTime resolvePublishDateTime(Article article) {
        LocalDateTime publish = toLocalDateTime(article.getPublishTime());
        if (publish != null) {
            return publish;
        }
        return article.getCreateTime();
    }

    private boolean isPrivateArticle(Article article) {
        if (article == null) {
            return false;
        }
        Integer accessLevel = article.getAccessLevel();
        if (accessLevel != null && accessLevel > 0) {
            return true;
        }
        Integer isPasswd = article.getIsPasswd();
        return isPasswd != null && isPasswd == 0;
    }

    private long safeViewCount(Article article) {
        return article != null ? safeLong(article.getViewCount()) : 0L;
    }

    private long safeShareCount(Article article) {
        return article != null ? safeLong(article.getShareCount()) : 0L;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private ArticleReviewStatus resolveReviewStatus(Article article) {
        return ArticleReviewStatus.fromCode(article != null ? article.getReviewStatus() : null);
    }

    private boolean isApprovedArticle(Article article) {
        return resolveReviewStatus(article).isApproved();
    }

    private boolean isPendingReviewArticle(Article article) {
        return resolveReviewStatus(article).isPending();
    }

    private boolean isRejectedArticle(Article article) {
        return resolveReviewStatus(article).isRejected();
    }

    private Integer mapStatusToCode(String status) {
        if (StringUtils.isBlank(status)) {
            return 1;
        }
        String trimmed = status.trim();
        if (StringUtils.isNumeric(trimmed)) {
            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ignore) {
                return 1;
            }
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("publish") || normalized.startsWith("public")) {
            return 1;
        }
        if (normalized.startsWith("draft")) {
            return 0;
        }
        if (normalized.startsWith("offline") || normalized.startsWith("private")) {
            return 2;
        }
        return 1;
    }

    private static class RangeDefinition {
        private final LocalDate start;
        private final LocalDate end;
        private final int days;
        private final String preset;

        private RangeDefinition(LocalDate start, LocalDate end, int days, String preset) {
            this.start = start;
            this.end = end;
            this.days = days;
            this.preset = preset;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }

        public int getDays() {
            return days;
        }

        public String getPreset() {
            return preset;
        }
    }

    private static class DailyAggregate {
        private long published;
        private long views;
        private long likes;
        private long comments;
        private long shares;
    }

    private static class SegmentAccumulator {
        private String key;
        private String label;
        private long articleCount;
        private long viewCount;
        private long likeCount;
        private long commentCount;
        private long shareCount;

        void addArticle(Article article, AdminArticleController controller) {
            articleCount += 1;
            viewCount += controller.safeViewCount(article);
            likeCount += controller.safeLong(article.getLikeCount());
            commentCount += controller.safeLong(article.getCommentCount());
            shareCount += controller.safeShareCount(article);
        }

        ArticleDashboardResponse.SegmentItem toSegmentItem(long total) {
            ArticleDashboardResponse.SegmentItem item = new ArticleDashboardResponse.SegmentItem();
            item.setKey(key);
            item.setLabel(label);
            item.setArticleCount(articleCount);
            item.setViewCount(viewCount);
            item.setLikeCount(likeCount);
            item.setCommentCount(commentCount);
            item.setShareCount(shareCount);
            item.setPercentage(total > 0 ? (double) articleCount / total : 0.0);
            return item;
        }

        long getViewCount() {
            return viewCount;
        }

        long getArticleCount() {
            return articleCount;
        }
    }
}
