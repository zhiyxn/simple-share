package com.simpleshare.article.controller;

import com.simpleshare.article.domain.Category;
import com.simpleshare.article.service.ICategoryService;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台分类Controller
 * 作者：小码哥（wx：xmgcode88）
 */
@RestController
@RequestMapping("/article/front/categories")
public class FrontCategoryController extends BaseController {
    
    @Autowired
    private ICategoryService categoryService;
    
    /**
     * 查询前台显示的分类列表
     */
    @GetMapping
    public AjaxResult list() {
        List<Category> list = categoryService.selectFrontCategoryList();
        return success(list);
    }
    
    /**
     * 查询分类树结构
     */
    @GetMapping("/tree")
    public AjaxResult tree() {
        List<Category> categories = categoryService.selectNormalCategoryList();
        List<Category> tree = categoryService.buildCategoryTree(categories);
        return success(tree);
    }
    
    /**
     * 查询分类及其文章数量
     */
    @GetMapping("/withCount")
    public AjaxResult withCount() {
        List<Category> list = categoryService.selectCategoryWithArticleCount();
        return success(list);
    }
    
    /**
     * 获取分类详细信息
     */
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        Category category = categoryService.selectCategoryByCategoryId(categoryId);
        if (category != null && "0".equals(category.getStatus())) {
            return success(category);
        }
        return error("分类不存在或已停用");
    }
    
    /**
     * 根据分类路径查询分类
     */
    @GetMapping("/path/{categoryPath}")
    public AjaxResult getByPath(@PathVariable String categoryPath) {
        Category category = categoryService.selectCategoryByPath(categoryPath);
        if (category != null && "0".equals(category.getStatus())) {
            return success(category);
        }
        return error("分类不存在或已停用");
    }
}