package com.simpleshare.article.controller;

import com.simpleshare.article.domain.Category;
import com.simpleshare.article.service.ICategoryService;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共分类API Controller
 * 映射前端期望的 /api/categories 路径
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/article/categories")
public class PublicCategoryController extends BaseController {
    
    @Autowired
    private ICategoryService categoryService;
    
    /**
     * 查询分类列表（前台使用）
     */
    @GetMapping
    public AjaxResult list() {
        List<Category> list = categoryService.selectCategoryList(new Category());
        return success(list);
    }
    
    /**
     * 根据分类ID查询分类详情
     */
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        Category category = categoryService.selectCategoryByCategoryId(categoryId);
        return success(category);
    }
    
    /**
     * 查询热门分类列表
     */
    @GetMapping("/hot")
    public AjaxResult hotList(@RequestParam(defaultValue = "10") Integer limit) {
        List<Category> list = categoryService.selectHotCategoryList(limit);
        return success(list);
    }
    
    /**
     * 查询分类树结构
     */
    @GetMapping("/tree")
    public AjaxResult tree() {
        List<Category> list = categoryService.buildCategoryTree();
        return success(list);
    }
    
    /**
     * 根据父级ID查询子分类
     */
    @GetMapping("/children/{parentId}")
    public AjaxResult children(@PathVariable Long parentId) {
        List<Category> list = categoryService.selectCategoryListByParentId(parentId);
        return success(list);
    }
    
    /**
     * 查询分类及其文章数量
     */
    @GetMapping("/with-count")
    public AjaxResult listWithCount() {
        List<Category> list = categoryService.selectCategoryListWithArticleCount();
        return success(list);
    }
    
    /**
     * 搜索分类
     */
    @GetMapping("/search")
    public AjaxResult search(@RequestParam String keyword) {
        List<Category> list = categoryService.searchCategories(keyword);
        return success(list);
    }
}
