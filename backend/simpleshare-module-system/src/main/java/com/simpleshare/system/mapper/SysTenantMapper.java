package com.simpleshare.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户表 数据层
 *
 * @author SimpleShare
 */
@Mapper
@TenantIgnore
public interface SysTenantMapper extends BaseMapper<SysTenant> {
    
    /**
     * 查询租户列表
     *
     * @param tenant 租户信息
     * @return 租户集合
     */
    List<SysTenant> selectTenantList(SysTenant tenant);
    
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
     * @param tenantCode 租户编号
     * @param tenantId   租户ID
     * @return 结果
     */
    SysTenant checkTenantCodeUnique(@Param("tenantCode") String tenantCode, @Param("tenantId") Long tenantId);
    
    /**
     * 校验租户名称是否唯一
     *
     * @param tenantName 租户名称
     * @param tenantId   租户ID
     * @return 结果
     */
    SysTenant checkTenantNameUnique(@Param("tenantName") String tenantName, @Param("tenantId") Long tenantId);
    
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
     * 删除租户
     *
     * @param tenantId 租户ID
     * @return 结果
     */
    int deleteTenantById(Long tenantId);
    
    /**
     * 批量删除租户
     *
     * @param tenantIds 需要删除的租户ID
     * @return 结果
     */
    int deleteTenantByIds(Long[] tenantIds);
    
    /**
     * 获取所有有效租户
     *
     * @return 租户列表
     */
    List<SysTenant> selectActiveTenantList();
}