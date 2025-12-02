package com.simpleshare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.exception.ServiceException;
import com.simpleshare.common.utils.DateUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import com.simpleshare.system.mapper.SysTenantMapper;
import com.simpleshare.system.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户服务实现
 *
 * @author SimpleShare
 */
@Service
@TenantIgnore
public class SysTenantServiceImpl implements ISysTenantService {
    
    @Autowired
    private SysTenantMapper tenantMapper;
    
    /**
     * 查询租户列表
     *
     * @param tenant 租户信息
     * @return 租户集合
     */
    @Override
    public List<SysTenant> selectTenantList(SysTenant tenant) {
        return tenantMapper.selectTenantList(tenant);
    }
    
    /**
     * 分页查询租户列表
     *
     * @param tenant 租户信息
     * @return 租户分页数据
     */
    @Override
    public TableDataInfo selectTenantPage(SysTenant tenant) {
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(tenant.getTenantName()), SysTenant::getTenantName, tenant.getTenantName())
                .like(StringUtils.isNotEmpty(tenant.getTenantCode()), SysTenant::getTenantCode, tenant.getTenantCode())
                .eq(StringUtils.isNotEmpty(tenant.getStatus()), SysTenant::getStatus, tenant.getStatus())
                .orderByDesc(SysTenant::getCreateTime);
        
        Page<SysTenant> page = tenantMapper.selectPage(new Page<>(1, 10), queryWrapper);
        return TableDataInfo.build(page);
    }
    
    /**
     * 通过租户ID查询租户
     *
     * @param tenantId 租户ID
     * @return 租户对象信息
     */
    @Override
    public SysTenant selectTenantById(Long tenantId) {
        return tenantMapper.selectTenantById(tenantId);
    }
    
    /**
     * 通过租户编号查询租户
     *
     * @param tenantCode 租户编号
     * @return 租户对象信息
     */
    @Override
    public SysTenant selectTenantByCode(String tenantCode) {
        return tenantMapper.selectTenantByCode(tenantCode);
    }

    /**
     * 通过域名查询租户
     *
     * @param domain 域名
     * @return 租户对象信息
     */
    @Override
    public SysTenant selectTenantByDomain(String domain) {
        return tenantMapper.selectTenantByDomain(domain);
    }
    
    /**
     * 校验租户编号是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public String checkTenantCodeUnique(SysTenant tenant) {
        Long tenantId = StringUtils.isNull(tenant.getTenantId()) ? -1L : tenant.getTenantId();
        SysTenant info = tenantMapper.checkTenantCodeUnique(tenant.getTenantCode(), tenantId);
        if (StringUtils.isNotNull(info) && info.getTenantId().longValue() != tenantId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
    
    /**
     * 校验租户名称是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public String checkTenantNameUnique(SysTenant tenant) {
        Long tenantId = StringUtils.isNull(tenant.getTenantId()) ? -1L : tenant.getTenantId();
        SysTenant info = tenantMapper.checkTenantNameUnique(tenant.getTenantName(), tenantId);
        if (StringUtils.isNotNull(info) && info.getTenantId().longValue() != tenantId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验租户名称是否唯一（字符串参数）
     *
     * @param tenantName 租户名称
     * @return 结果
     */
    @Override
    public String checkTenantNameUnique(String tenantName) {
        SysTenant info = tenantMapper.checkTenantNameUnique(tenantName, -1L);
        if (StringUtils.isNotNull(info)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验域名是否唯一
     *
     * @param domain 域名
     * @return 结果
     */
    @Override
    public String checkDomainUnique(String domain) {
        SysTenant info = tenantMapper.selectTenantByDomain(domain);
        if (StringUtils.isNotNull(info)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
    
    /**
     * 新增租户
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTenant(SysTenant tenant) {
        tenant.setCreateTime(LocalDateTime.now());
        int result = tenantMapper.insertTenant(tenant);
        
        // 初始化租户数据
        if (result > 0) {
            initTenantData(tenant);
        }
        
        return result;
    }
    
    /**
     * 修改租户
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public int updateTenant(SysTenant tenant) {
        tenant.setUpdateTime(LocalDateTime.now());
        return tenantMapper.updateTenant(tenant);
    }
    
    /**
     * 批量删除租户
     *
     * @param tenantIds 需要删除的租户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTenantByIds(Long[] tenantIds) {
        for (Long tenantId : tenantIds) {
            SysTenant tenant = selectTenantById(tenantId);
            if (StringUtils.isNull(tenant)) {
                throw new ServiceException(String.format("租户%1$s不存在", tenantId));
            }
        }
        return tenantMapper.deleteTenantByIds(tenantIds);
    }
    
    /**
     * 删除租户信息
     *
     * @param tenantId 租户ID
     * @return 结果
     */
    @Override
    public int deleteTenantById(Long tenantId) {
        return tenantMapper.deleteTenantById(tenantId);
    }
    
    /**
     * 修改租户状态
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public int updateTenantStatus(SysTenant tenant) {
        return tenantMapper.updateTenant(tenant);
    }
    
    /**
     * 获取所有有效租户
     *
     * @return 租户列表
     */
    @Override
    public List<SysTenant> selectActiveTenantList() {
        return tenantMapper.selectActiveTenantList();
    }
    
    /**
     * 初始化租户数据
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    @Transactional
    public boolean initTenantData(SysTenant tenant) {
        // TODO: 初始化租户基础数据，如默认角色、菜单等
        // 这里可以根据业务需求添加初始化逻辑
        return true;
    }
}