package com.simpleshare.system.service;

import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.common.core.page.TableDataInfo;

import java.util.List;

/**
 * 租户服务接口
 *
 * @author SimpleShare
 */
public interface ISysTenantService {
    
    /**
     * 查询租户列表
     *
     * @param tenant 租户信息
     * @return 租户集合
     */
    List<SysTenant> selectTenantList(SysTenant tenant);
    
    /**
     * 分页查询租户列表
     *
     * @param tenant 租户信息
     * @return 租户分页数据
     */
    TableDataInfo selectTenantPage(SysTenant tenant);
    
    /**
     * 通过租户ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户对象信息
     */
    SysTenant selectTenantById(Long tenantId);
    
    /**
     * 通过租户编号查询租户
     *
     * @param tenantCode 租户编号
     * @return 租户对象信息
     */
    SysTenant selectTenantByCode(String tenantCode);

    /**
     * 通过域名查询租户
     *
     * @param domain 域名
     * @return 租户对象信息
     */
    SysTenant selectTenantByDomain(String domain);
    
    /**
     * 校验租户编号是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    String checkTenantCodeUnique(SysTenant tenant);
    
    /**
     * 校验租户名称是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    String checkTenantNameUnique(SysTenant tenant);

    /**
     * 校验租户名称是否唯一（字符串参数）
     *
     * @param tenantName 租户名称
     * @return 结果
     */
    String checkTenantNameUnique(String tenantName);

    /**
     * 校验域名是否唯一
     *
     * @param domain 域名
     * @return 结果
     */
    String checkDomainUnique(String domain);
    
    /**
     * 新增租户
     *
     * @param tenant 租户信息
     * @return 结果
     */
    int insertTenant(SysTenant tenant);
    
    /**
     * 修改租户
     *
     * @param tenant 租户信息
     * @return 结果
     */
    int updateTenant(SysTenant tenant);
    
    /**
     * 批量删除租户
     *
     * @param tenantIds 需要删除的租户ID
     * @return 结果
     */
    int deleteTenantByIds(Long[] tenantIds);
    
    /**
     * 删除租户信息
     *
     * @param tenantId 租户ID
     * @return 结果
     */
    int deleteTenantById(Long tenantId);
    
    /**
     * 修改租户状态
     *
     * @param tenant 租户信息
     * @return 结果
     */
    int updateTenantStatus(SysTenant tenant);
    
    /**
     * 获取所有有效租户
     *
     * @return 租户列表
     */
    List<SysTenant> selectActiveTenantList();
    
    /**
     * 初始化租户数据
     *
     * @param tenant 租户信息
     * @return 结果
     */
    boolean initTenantData(SysTenant tenant);
}