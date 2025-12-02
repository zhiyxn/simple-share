package com.simpleshare.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.article.domain.Category;
import com.simpleshare.article.mapper.CategoryMapper;
import com.simpleshare.article.service.ICategoryService;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.domain.TreeSelect;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.common.core.page.PageDomain;
import com.simpleshare.common.core.page.TableSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类Service业务层处理
 *
 * @author SimpleShare
 */
@Service
public class CategoryServiceImpl implements ICategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    /**
     * 查询分类
     *
     * @param categoryId 分类主键
     * @return 分类
     */
    @Override
    public Category selectCategoryByCategoryId(Long categoryId) {
        return categoryMapper.selectCategoryByCategoryId(categoryId);
    }
    
    /**
     * 查询分类列表
     *
     * @param category 分类
     * @return 分类
     */
    @Override
    public List<Category> selectCategoryList(Category category) {
        return categoryMapper.selectCategoryList(category);
    }
    
    /**
     * 分页查询分类列表
     *
     * @param category 分类
     * @return 分类分页数据
     */
    @Override
    public TableDataInfo selectCategoryPage(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(category.getCategoryName()), Category::getCategoryName, category.getCategoryName())
                .eq(category.getParentId() != null, Category::getParentId, category.getParentId())
                .eq(StringUtils.isNotEmpty(category.getStatus()), Category::getStatus, category.getStatus())
                .orderByAsc(Category::getOrderNum)
                .orderByDesc(Category::getCreateTime);
        
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<Category> page = categoryMapper.selectPage(new Page<>(pageDomain.getPageNum(), pageDomain.getPageSize()), queryWrapper);
        return TableDataInfo.build(page);
    }
    
    /**
     * 新增分类
     *
     * @param category 分类
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCategory(Category category) {
        // 设置租户ID
        category.setTenantId(TenantContextHolder.getTenantId());
        // 设置创建信息
        category.setCreateTime(java.time.LocalDateTime.now());
        category.setCreateBy(SecurityUtils.getUserId());
        
        // 如果父分类为空，设置为0（顶级分类）
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        
        // 设置祖级列表
        setAncestors(category);
        
        return categoryMapper.insertCategory(category);
    }
    
    /**
     * 修改分类
     *
     * @param category 分类
     * @return 结果
     */
    @Override
    @Transactional
    public int updateCategory(Category category) {
        Category newParentCategory = categoryMapper.selectCategoryByCategoryId(category.getParentId());
        Category oldCategory = categoryMapper.selectCategoryByCategoryId(category.getCategoryId());
        if (StringUtils.isNotNull(newParentCategory) && StringUtils.isNotNull(oldCategory)) {
            String newAncestors = newParentCategory.getAncestors() + "," + newParentCategory.getCategoryId();
            String oldAncestors = oldCategory.getAncestors();
            category.setAncestors(newAncestors);
            updateCategoryChildren(category.getCategoryId(), newAncestors, oldAncestors);
        }
        
        category.setUpdateTime(java.time.LocalDateTime.now());
        category.setUpdateBy(SecurityUtils.getUserId());
        
        return categoryMapper.updateCategory(category);
    }
    
    /**
     * 批量删除分类
     *
     * @param categoryIds 需要删除的分类主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCategoryByCategoryIds(Long[] categoryIds) {
        for (Long categoryId : categoryIds) {
            Category category = selectCategoryByCategoryId(categoryId);
            if (hasChildByParentId(categoryId)) {
                throw new ServiceException(String.format("%1$s存在子分类,不允许删除", category.getCategoryName()));
            }
            if (checkCategoryExistArticle(categoryId)) {
                throw new ServiceException(String.format("%1$s分类存在文章,不允许删除", category.getCategoryName()));
            }
        }
        return categoryMapper.deleteCategoryByCategoryIds(categoryIds);
    }
    
    /**
     * 删除分类信息
     *
     * @param categoryId 分类主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCategoryByCategoryId(Long categoryId) {
        return categoryMapper.deleteCategoryByCategoryId(categoryId);
    }
    
    /**
     * 构建前端所需要树结构
     *
     * @param categories 分类列表
     * @return 树结构列表
     */
    @Override
    public List<Category> buildCategoryTree(List<Category> categories) {
        List<Category> returnList = new ArrayList<>();
        List<Long> tempList = new ArrayList<>();
        for (Category category : categories) {
            tempList.add(category.getCategoryId());
        }
        for (Iterator<Category> iterator = categories.iterator(); iterator.hasNext();) {
            Category category = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getParentId())) {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty()) {
            returnList = categories;
        }
        return returnList;
    }
    
    /**
     * 构建前端所需要下拉树结构
     *
     * @param categories 分类列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildCategoryTreeSelect(List<Category> categories) {
        List<Category> categoryTrees = buildCategoryTree(categories);
        return categoryTrees.stream().map(this::buildTreeSelect).collect(Collectors.toList());
    }
    
    /**
     * 构建TreeSelect对象
     *
     * @param category 分类对象
     * @return TreeSelect对象
     */
    private TreeSelect buildTreeSelect(Category category) {
        TreeSelect treeSelect = new TreeSelect();
        treeSelect.setId(category.getCategoryId());
        treeSelect.setLabel(category.getCategoryName());
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            treeSelect.setChildren(category.getChildren().stream().map(this::buildTreeSelect).collect(Collectors.toList()));
        }
        return treeSelect;
    }
    
    /**
     * 是否存在分类子节点
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public boolean hasChildByCategoryId(Long categoryId) {
        int result = categoryMapper.hasChildByParentId(categoryId);
        return result > 0;
    }
    
    /**
     * 是否存在分类子节点
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public boolean hasChildByParentId(Long categoryId) {
        int result = categoryMapper.hasChildByParentId(categoryId);
        return result > 0;
    }
    
    /**
     * 查询分类是否存在文章
     *
     * @param categoryId 分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkCategoryExistArticle(Long categoryId) {
        int result = categoryMapper.checkCategoryExistArticle(categoryId);
        return result > 0;
    }
    
    /**
     * 校验分类名称是否唯一
     *
     * @param category 分类信息
     * @return 结果
     */
    @Override
    public String checkCategoryNameUnique(Category category) {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        Category info = categoryMapper.checkCategoryNameUnique(category.getCategoryName(), category.getParentId());
        if (StringUtils.isNotNull(info) && info.getCategoryId().longValue() != categoryId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
    
    /**
     * 查询分类树结构信息
     *
     * @param category 分类信息
     * @return 分类树信息集合
     */
    @Override
    public List<TreeSelect> selectCategoryTreeList(Category category) {
        List<Category> categories = categoryMapper.selectCategoryTreeList(category);
        return buildCategoryTreeSelect(categories);
    }
    
    /**
     * 查询正常状态的分类列表
     *
     * @return 分类列表
     */
    @Override
    public List<Category> selectNormalCategoryList() {
        return categoryMapper.selectNormalCategoryList();
    }
    
    /**
     * 查询分类及其文章数量
     *
     * @return 分类列表
     */
    @Override
    public List<Category> selectCategoryWithArticleCount() {
        return categoryMapper.selectCategoryWithArticleCount();
    }
    
    /**
     * 根据分类ID查询所有子分类ID
     *
     * @param categoryId 分类ID
     * @return 子分类ID列表
     */
    @Override
    public List<Long> selectChildrenCategoryIds(Long categoryId) {
        return categoryMapper.selectChildrenCategoryIds(categoryId);
    }
    
    /**
     * 根据分类ID查询所有子分类ID
     *
     * @param categoryId 分类ID
     * @return 子分类ID列表
     */
    @Override
    public List<Long> selectChildrenIdsByParentId(Long categoryId) {
        return categoryMapper.selectChildrenIdsByParentId(categoryId);
    }
    
    /**
     * 查询前台显示的分类列表
     *
     * @return 分类列表
     */
    @Override
    public List<Category> selectFrontCategoryList() {
        return categoryMapper.selectFrontCategoryList();
    }
    
    /**
     * 查询前台分类树结构
     *
     * @return 分类树集合
     */
    @Override
    public List<Category> selectFrontCategoryTree() {
        List<Category> categories = selectFrontCategoryList();
        return buildCategoryTree(categories);
    }
    
    /**
     * 根据分类路径查询分类
     *
     * @param categoryPath 分类路径
     * @return 分类信息
     */
    @Override
    public Category selectCategoryByPath(String categoryPath) {
        return categoryMapper.selectCategoryByPath(categoryPath);
    }
    
    /**
     * 根据分类路径查询分类
     *
     * @param ancestors 分类路径
     * @return 分类集合
     */
    @Override
    public List<Category> selectCategoryByAncestors(String ancestors) {
        return categoryMapper.selectCategoryByAncestors(ancestors);
    }
    
    /**
     * 递归列表
     */
    private void recursionFn(List<Category> list, Category t) {
        // 得到子节点列表
        List<Category> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Category tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }
    
    /**
     * 得到子节点列表
     */
    private List<Category> getChildList(List<Category> list, Category t) {
        List<Category> tlist = new ArrayList<>();
        Iterator<Category> it = list.iterator();
        while (it.hasNext()) {
            Category n = it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getCategoryId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }
    
    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<Category> list, Category t) {
        return getChildList(list, t).size() > 0;
    }
    
    /**
     * 设置祖级列表
     */
    private void setAncestors(Category category) {
        Category parentCategory = categoryMapper.selectCategoryByCategoryId(category.getParentId());
        if (StringUtils.isNotNull(parentCategory)) {
            category.setAncestors(parentCategory.getAncestors() + "," + category.getParentId());
        } else {
            category.setAncestors("0");
        }
    }
    
    /**
     * 修改子元素关系
     *
     * @param categoryId   被修改的分类ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors) {
        List<Long> childrenIds = categoryMapper.selectChildrenCategoryIds(categoryId);
        List<Category> children = new ArrayList<>();
        for (Long childId : childrenIds) {
            Category child = categoryMapper.selectCategoryByCategoryId(childId);
            if (child != null) {
                child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
                children.add(child);
            }
        }
        if (children.size() > 0) {
            categoryMapper.updateCategoryChildren(children);
        }
    }
    
    /**
     * 查询热门分类列表
     *
     * @param limit 限制数量
     * @return 分类集合
     */
    @Override
    public List<Category> selectHotCategoryList(Integer limit) {
        return categoryMapper.selectHotCategoryList(limit);
    }
    
    /**
     * 构建分类树结构（无参数版本）
     *
     * @return 分类树集合
     */
    @Override
    public List<Category> buildCategoryTree() {
        List<Category> categories = selectCategoryList(new Category());
        return buildCategoryTree(categories);
    }
    
    /**
     * 根据父级ID查询子分类
     *
     * @param parentId 父级ID
     * @return 分类集合
     */
    @Override
    public List<Category> selectCategoryListByParentId(Long parentId) {
        return categoryMapper.selectCategoryListByParentId(parentId);
    }
    
    /**
     * 查询分类及其文章数量
     *
     * @return 分类集合
     */
    @Override
    public List<Category> selectCategoryListWithArticleCount() {
        return categoryMapper.selectCategoryListWithArticleCount();
    }
    
    /**
     * 搜索分类
     *
     * @param keyword 关键词
     * @return 分类集合
     */
    @Override
    public List<Category> searchCategories(String keyword) {
        return categoryMapper.searchCategories(keyword);
    }
}