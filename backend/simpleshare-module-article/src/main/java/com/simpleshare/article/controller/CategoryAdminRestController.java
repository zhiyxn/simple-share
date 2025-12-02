package com.simpleshare.article.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.simpleshare.article.domain.Category;
import com.simpleshare.article.dto.CategoryAdminRequest;
import com.simpleshare.article.dto.CategoryAdminResponse;
import com.simpleshare.article.service.ICategoryService;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.TreeSelect;
import com.simpleshare.common.core.domain.model.PageResponse;
import com.simpleshare.common.utils.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 分类管理后台REST API
 * 功能：提供分类的增删改查与树结构等管理接口
 * 作者：小码哥（wx：xmgcode88）
 */
@RestController
@RequestMapping("/article/admin/categories")
public class CategoryAdminRestController extends BaseController {

    private final ICategoryService categoryService;

    public CategoryAdminRestController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * List categories with pagination support.
     */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('article:category:list')")
    public AjaxResult list(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "limit", defaultValue = "20") int limit,
                           @RequestParam(value = "size", required = false) Integer size,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "parentId", required = false) Long parentId) {
        int pageSize = size != null ? size : limit;
        if (pageSize <= 0) {
            pageSize = 20;
        }
        int pageNum = Math.max(page, 1);

        PageHelper.startPage(pageNum, pageSize);

        Category query = new Category();
        if (StringUtils.isNotBlank(keyword)) {
            query.setCategoryName(keyword.trim());
        }
        if (parentId != null) {
            query.setParentId(parentId);
        }
        if (StringUtils.isNotBlank(status)) {
            query.setStatus(mapStatus(status));
        }

        List<Category> categories = categoryService.selectCategoryList(query);
        PageInfo<Category> pageInfo = new PageInfo<>(categories);
        List<CategoryAdminResponse> items = categories.stream()
            .map(CategoryAdminResponse::from)
            .collect(Collectors.toList());

        return AjaxResult.success(PageResponse.from(pageInfo, items));
    }

    /**
     * Get category tree structure.
     */
    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermi('article:category:list')")
    public AjaxResult getTree() {
        List<TreeSelect> tree = categoryService.selectCategoryTreeList(new Category());
        return AjaxResult.success(tree);
    }

    /**
     * Fetch category details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('article:category:query')")
    public AjaxResult detail(@PathVariable("id") Long id) {
        Category category = categoryService.selectCategoryByCategoryId(id);
        if (category == null) {
            return AjaxResult.error("Category not found");
        }
        return AjaxResult.success(CategoryAdminResponse.from(category));
    }

    /**
     * Create a new category.
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('article:category:add')")
    public AjaxResult create(@Validated @RequestBody CategoryAdminRequest request) {
        Category category = buildCategoryEntity(new Category(), request);
        if (UserConstants.NOT_UNIQUE.equals(categoryService.checkCategoryNameUnique(category))) {
            return AjaxResult.error("Category name already exists");
        }
        categoryService.insertCategory(category);
        Category saved = categoryService.selectCategoryByCategoryId(category.getCategoryId());
        return AjaxResult.success(CategoryAdminResponse.from(saved));
    }

    /**
     * Update an existing category.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('article:category:edit')")
    public AjaxResult update(@PathVariable("id") Long id,
                             @Validated @RequestBody CategoryAdminRequest request) {
        Category existing = categoryService.selectCategoryByCategoryId(id);
        if (existing == null) {
            return AjaxResult.error("Category not found");
        }
        Category category = buildCategoryEntity(existing, request);
        category.setCategoryId(id);
        category.setParentId(request.getParentId() != null ? request.getParentId() : existing.getParentId());
        if (UserConstants.NOT_UNIQUE.equals(categoryService.checkCategoryNameUnique(category))) {
            return AjaxResult.error("Category name already exists");
        }
        categoryService.updateCategory(category);
        Category saved = categoryService.selectCategoryByCategoryId(id);
        return AjaxResult.success(CategoryAdminResponse.from(saved));
    }

    /**
     * Delete a category.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('article:category:remove')")
    public AjaxResult delete(@PathVariable("id") Long id) {
        if (categoryService.hasChildByParentId(id)) {
            return AjaxResult.warn("Category has child nodes");
        }
        if (categoryService.checkCategoryExistArticle(id)) {
            return AjaxResult.warn("Category still contains articles");
        }
        int rows = categoryService.deleteCategoryByCategoryId(id);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error("Delete failed");
    }

    private Category buildCategoryEntity(Category target, CategoryAdminRequest request) {
        target.setCategoryName(request.getName());
        target.setDescription(request.getDescription());
        target.setIcon(request.getIcon());
        target.setCategoryPath(resolveSlug(request));
        target.setOrderNum(request.getSort());
        target.setStatus(Boolean.TRUE.equals(request.getEnabled()) ? "0" : "1");
        if (request.getParentId() != null) {
            target.setParentId(request.getParentId());
        }
        return target;
    }

    private String resolveSlug(CategoryAdminRequest request) {
        if (StringUtils.isNotBlank(request.getSlug())) {
            return normalizeSlug(request.getSlug());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            return normalizeSlug(request.getName());
        }
        return null;
    }

    private String normalizeSlug(String slugSource) {
        String slug = slugSource == null ? "" : slugSource.toLowerCase(Locale.ROOT).trim();
        slug = slug.replaceAll("[^a-z0-9\\-\\s]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        return slug;
    }

    private String mapStatus(String status) {
        String normalized = status.toLowerCase(Locale.ROOT).trim();
        if ("active".equals(normalized) || "enabled".equals(normalized)) {
            return "0";
        }
        return "1";
    }
}
