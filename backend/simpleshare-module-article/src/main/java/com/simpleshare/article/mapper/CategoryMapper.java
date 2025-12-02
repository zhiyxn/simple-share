package com.simpleshare.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.article.domain.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类Mapper接口
 *
 * @author SimpleShare
 */
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 查询分类列表
     *
     * @param category 分类
     * @return 分类集合
     */
    List<Category> selectCategoryList(Category category);
    
    /**
     * 根据分类ID查询分类
     *
     * @param categoryId 分类ID
     * @return 分类
     */
    Category selectCategoryByCategoryId(Long categoryId);
    
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
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    int deleteCategoryByCategoryId(Long categoryId);
    
    /**
     * 批量删除分类
     *
     * @param categoryIds 需要删除的数据ID
     * @return 结果
     */
    int deleteCategoryByCategoryIds(Long[] categoryIds);
    
    /**
     * 根据父ID查询子分类数量
     *
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    int selectChildrenCategoryById(Long parentId);
    

    
    /**
     * 修改子元素关系
     *
     * @param categories 子元素
     * @return 结果
     */
    int updateCategoryChildren(@Param("categories") List<Category> categories);
    
    /**
     * 查询分类树结构
     *
     * @param category 分类
     * @return 分类树集合
     */
    List<Category> selectCategoryTreeList(Category category);
    
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
     * 根据分类ID查询其所有子分类ID
     *
     * @param categoryId 分类ID
     * @return 子分类ID集合
     */
    List<Long> selectChildrenCategoryIds(Long categoryId);
    
    /**
     * 根据父分类ID查询其所有子分类ID
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
    int hasChildByParentId(Long categoryId);
    
    /**
     * 查询分类是否存在文章
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    int checkCategoryExistArticle(Long categoryId);
    
    /**
     * 校验分类名称是否唯一
     *
     * @param categoryName 分类名称
     * @param parentId 父分类ID
     * @return 分类信息
     */
    Category checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);
    
    /**
     * 查询热门分类列表
     *
     * @param limit 限制数量
     * @return 分类集合
     */
    List<Category> selectHotCategoryList(@Param("limit") Integer limit);
    
    /**
     * 根据父级ID查询子分类
     *
     * @param parentId 父级ID
     * @return 分类集合
     */
    List<Category> selectCategoryListByParentId(@Param("parentId") Long parentId);
    
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
    List<Category> searchCategories(@Param("keyword") String keyword);
}