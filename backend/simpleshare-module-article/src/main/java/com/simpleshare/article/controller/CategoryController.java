package com.simpleshare.article.controller;

import com.simpleshare.article.domain.Category;
import com.simpleshare.article.service.ICategoryService;
// import com.simpleshare.common.annotation.Log;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.TreeSelect;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.enums.BusinessType;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 分类Controller
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/article/category")
public class CategoryController extends BaseController {
    
    @Autowired
    private ICategoryService categoryService;
    
    /**
     * 查询分类列表
     */
    @PreAuthorize("@ss.hasPermi('article:category:list')")
    @GetMapping("/list")
    public AjaxResult list(Category category) {
        List<Category> categories = categoryService.selectCategoryList(category);
        return success(categories);
    }
    
    /**
     * 查询分类下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(Category category) {
        List<Category> categories = categoryService.selectCategoryList(category);
        return success(categoryService.buildCategoryTreeSelect(categories));
    }
    
    /**
     * 加载对应角色分类列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        List<Category> categories = categoryService.selectCategoryList(new Category());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("menus", categoryService.buildCategoryTreeSelect(categories));
        return ajax;
    }
    
    /**
     * 导出分类列表
     */
    @PreAuthorize("@ss.hasPermi('article:category:export')")
    // @Log(title = "分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Category category) {
        List<Category> list = categoryService.selectCategoryList(category);
        ExcelUtil<Category> util = new ExcelUtil<Category>(Category.class);
        util.exportExcel(response, list, "分类数据");
    }
    
    /**
     * 获取分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('article:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return success(categoryService.selectCategoryByCategoryId(categoryId));
    }
    
    /**
     * 新增分类
     */
    @PreAuthorize("@ss.hasPermi('article:category:add')")
    // @Log(title = "分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Category category) {
        if (UserConstants.NOT_UNIQUE.equals(categoryService.checkCategoryNameUnique(category))) {
            return error("新增分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(categoryService.insertCategory(category));
    }
    
    /**
     * 修改分类
     */
    @PreAuthorize("@ss.hasPermi('article:category:edit')")
    // @Log(title = "分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Category category) {
        Long categoryId = category.getCategoryId();
        if (categoryId.equals(category.getParentId())) {
            return error("修改分类'" + category.getCategoryName() + "'失败，上级分类不能是自己");
        } else if (StringUtils.equals("1", category.getStatus()) && categoryService.hasChildByParentId(categoryId)) {
            return error("该分类包含子分类！");
        }
        if (UserConstants.NOT_UNIQUE.equals(categoryService.checkCategoryNameUnique(category))) {
            return error("修改分类'" + category.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(categoryService.updateCategory(category));
    }
    
    /**
     * 删除分类
     */
    @PreAuthorize("@ss.hasPermi('article:category:remove')")
    // @Log(title = "分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId) {
        if (categoryService.hasChildByParentId(categoryId)) {
            return warn("存在子分类,不允许删除");
        }
        if (categoryService.checkCategoryExistArticle(categoryId)) {
            return warn("分类存在文章,不允许删除");
        }
        return toAjax(categoryService.deleteCategoryByCategoryId(categoryId));
    }
    
    /**
     * 查询分类树结构
     */
    @GetMapping("/tree")
    public AjaxResult tree(Category category) {
        List<TreeSelect> tree = categoryService.selectCategoryTreeList(category);
        return success(tree);
    }
    
    /**
     * 查询正常状态的分类列表
     */
    @GetMapping("/normal")
    public AjaxResult normalList() {
        List<Category> list = categoryService.selectNormalCategoryList();
        return success(list);
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
     * 查询前台显示的分类列表
     */
    @GetMapping("/front")
    public AjaxResult frontList() {
        List<Category> list = categoryService.selectFrontCategoryList();
        return success(list);
    }
    
    /**
     * 根据分类路径查询分类
     */
    @GetMapping("/path/{categoryPath}")
    public AjaxResult getByPath(@PathVariable String categoryPath) {
        Category category = categoryService.selectCategoryByPath(categoryPath);
        return success(category);
    }
}