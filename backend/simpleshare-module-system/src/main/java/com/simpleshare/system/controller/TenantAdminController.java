package com.simpleshare.system.controller;

import com.simpleshare.common.constant.UserConstants;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.utils.poi.ExcelUtil;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import com.simpleshare.system.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 租户信息
 *
 * @author SimpleShare
 */
@RestController
@RequestMapping("/system/admin/tenants")
@TenantIgnore
public class TenantAdminController extends BaseController {
    
    @Autowired
    private ISysTenantService tenantService;
    
    /**
     * 获取租户列表
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTenant tenant) {
        startPage();
        List<SysTenant> list = tenantService.selectTenantList(tenant);
        return getDataTable(list);
    }
    
    /**
     * 导出租户列表
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:export')")
    // @Log(title = "租户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysTenant tenant) {
        List<SysTenant> list = tenantService.selectTenantList(tenant);
        ExcelUtil<SysTenant> util = new ExcelUtil<SysTenant>(SysTenant.class);
        util.exportExcel(response, list, "租户数据");
    }
    
    /**
     * 根据租户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping(value = "/{tenantId}")
    public AjaxResult getInfo(@PathVariable("tenantId") Long tenantId) {
        return success(tenantService.selectTenantById(tenantId));
    }
    
    /**
     * 新增租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:add')")
    // @Log(title = "租户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTenant tenant) {
        if (UserConstants.NOT_UNIQUE.equals(tenantService.checkTenantCodeUnique(tenant))) {
            return error("新增租户'" + tenant.getTenantName() + "'失败，租户编号已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(tenantService.checkTenantNameUnique(tenant))) {
            return error("新增租户'" + tenant.getTenantName() + "'失败，租户名称已存在");
        }
        tenant.setCreateBy(getUserId());
        return toAjax(tenantService.insertTenant(tenant));
    }
    
    /**
     * 修改租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    // @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTenant tenant) {
        if (UserConstants.NOT_UNIQUE.equals(tenantService.checkTenantCodeUnique(tenant))) {
            return error("修改租户'" + tenant.getTenantName() + "'失败，租户编号已存在");
        } else if (UserConstants.NOT_UNIQUE.equals(tenantService.checkTenantNameUnique(tenant))) {
            return error("修改租户'" + tenant.getTenantName() + "'失败，租户名称已存在");
        }
        tenant.setUpdateBy(getUserId());
        return toAjax(tenantService.updateTenant(tenant));
    }
    
    /**
     * 删除租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:remove')")
    // @Log(title = "租户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tenantIds}")
    public AjaxResult remove(@PathVariable Long[] tenantIds) {
        return toAjax(tenantService.deleteTenantByIds(tenantIds));
    }
    
    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    // @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysTenant tenant) {
        tenant.setUpdateBy(getUserId());
        return toAjax(tenantService.updateTenantStatus(tenant));
    }
    
    /**
     * 获取租户选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<SysTenant> tenants = tenantService.selectActiveTenantList();
        return success(tenants);
    }
}

