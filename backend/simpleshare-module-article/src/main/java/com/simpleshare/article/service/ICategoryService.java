package com.simpleshare.article.service;

import com.simpleshare.article.domain.Category;
import com.simpleshare.common.core.domain.TreeSelect;
import com.simpleshare.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 分类Service接口
 *
 * @author SimpleShare
 */
public interface ICategoryService {
    
    /**
     * 查询分类
     *
     * @param categoryId 分类主键
     * @return 分类
     */
    Category selectCategoryByCategoryId(Long categoryId);
    
    /**
     * 查询分类列表
     *
     * @param category 分类
     * @return 分类集合
     */
    List<Category> selectCategoryList(Category category);
    
    /**
     * 分页查询分类列表
     *
     * @param category 分类
     * @return 分类分页数据
     */
    TableDataInfo selectCategoryPage(Category category);
    
    /**
     * 新增分类
     *
     * @param category 分类
     * @return 结果
     */
    int insertCategory(Category category);
    
    /**
     * 修改分类
     *
     * @param category 分类
     * @return 结果
     */
    int updateCategory(Category category);
    
    /**
     * 批量删除分类
     *
     * @param categoryIds 需要删除的分类主键集合
     * @return 结果
     */
    int deleteCategoryByCategoryIds(Long[] categoryIds);
    
    /**
     * 删除分类信息
     *
     * @param categoryId 分类主键
     * @return 结果
     */
    int deleteCategoryByCategoryId(Long categoryId);
    
    /**
     * 构建前端所需要树结构
     *
     * @param categories 分类列表
     * @return 树结构列表
     */
    List<Category> buildCategoryTree(List<Category> categories);
    
    /**
     * 构建前端所需要下拉树结构
     *
     * @param categories 分类列表
     * @return 下拉树结构列表
     */
    List<TreeSelect> buildCategoryTreeSelect(List<Category> categories);
    
    /**
     * 查询分类树结构信息
     *
     * @param category 分类信息
     * @return 分类树信息集合
     */
    List<TreeSelect> selectCategoryTreeList(Category category);
    
    /**
     * 是否存在分类子节点
     *
     * @param categoryId 分类ID
     * @return 结果 true 存在 false 不存在
     */
    boolean hasChildByCategoryId(Long categoryId);
    
    /**
     * 查询分类是否存在文章
     *
     * @param categoryId 分类ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkCategoryExistArticle(Long categoryId);
    
    /**
     * 校验分类名称是否唯一
     *
     * @param category 分类信息
     * @return 结果
     */
    String checkCategoryNameUnique(Category category);
    
    /**
     * 查询所有正常状态的分类
     *
     * @return 分类集合
     */
    List<Category> selectNormalCategoryList();
    
    /**
     * 查询分类及其文章数量
     *
     * @return 分类集合
     */
    List<Category> selectCategoryWithArticleCount();
    
    /**
     * 根据分类ID查询所有子分类ID
     *
     * @param categoryId 分类ID
     * @return 子分类ID集合
     */
    List<Long> selectChildrenCategoryIds(Long categoryId);
    
    /**
     * 根据父分类ID查询所有子分类ID
     *
     * @param parentId 父分类ID
     * @return 子分类ID集合
     */
    List<Long> selectChildrenIdsByParentId(Long parentId);
    
    /**
     * 查询前台显示的分类列表
     *
     * @return 分类集合
     */
    List<Category> selectFrontCategoryList();
    
    /**
     * 查询前台分类树结构
     *
     * @return 分类树集合
     */
    List<Category> selectFrontCategoryTree();
    
    /**
     * 根据分类路径查询分类
     *
     * @param ancestors 分类路径
     * @return 分类集合
     */
    List<Category> selectCategoryByAncestors(String ancestors);
    
    /**
     * 根据分类路径查询分类
     *
     * @param categoryPath 分类路径
     * @return 分类信息
     */
    Category selectCategoryByPath(String categoryPath);
    
    /**
     * 是否存在子分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    boolean hasChildByParentId(Long categoryId);
    
    /**
     * 查询热门分类列表
     *
     * @param limit 限制数量
     * @return 分类集合
     */
    List<Category> selectHotCategoryList(Integer limit);
    
    /**
     * 构建分类树结构（无参数版本）
     *
     * @return 分类树集合
     */
    List<Category> buildCategoryTree();
    
    /**
     * 根据父级ID查询子分类
     *
     * @param parentId 父级ID
     * @return 分类集合
     */
    List<Category> selectCategoryListByParentId(Long parentId);
    
    /**
     * 查询分类及其文章数量
     *
     * @return 分类集合
     */
    List<Category> selectCategoryListWithArticleCount();
    
    /**
     * 搜索分类
     *
     * @param keyword 关键词
     * @return 分类集合
     */
    List<Category> searchCategories(String keyword);
}