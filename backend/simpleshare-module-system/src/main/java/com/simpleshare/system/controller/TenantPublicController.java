package com.simpleshare.system.controller;

import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import com.simpleshare.system.config.TenantDefaultsProperties;
import com.simpleshare.system.service.ISysTenantConfigService;
import com.simpleshare.system.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户公开接口，统一提供前台可访问的租户与配置数据。
 */
@RestController
@RequestMapping("/system/public/tenants")
@TenantIgnore
public class TenantPublicController extends BaseController {

    @Autowired
    private ISysTenantService tenantService;

    @Autowired
    private ISysTenantConfigService tenantConfigService;

    @Autowired
    private TenantDefaultsProperties tenantDefaultsProperties;

    private static final String KEY_OWNER_QR_CODE = "owner_qr_code";
    private static final String KEY_OWNER_REMARK = "owner_remark";

    /**
     * 根据请求域名获取当前租户信息。
     */
    @GetMapping("/current")
    public AjaxResult getCurrentTenant(HttpServletRequest request) {
        String domain = request != null ? request.getServerName() : null;
        if (StringUtils.isEmpty(domain)) {
            domain = "localhost";
        }

        if (isLocalDomain(domain)) {
            SysTenant defaultTenant = tenantService.selectTenantByDomain(domain);
            if (defaultTenant == null) {
                defaultTenant = createDefaultTenant(request);
                if (defaultTenant == null) {
                    defaultTenant = buildFallbackTenant(domain);
                }
            }
            return AjaxResult.success(convertToFrontendTenant(defaultTenant));
        }

        SysTenant tenant = tenantService.selectTenantByDomain(domain);
        if (tenant == null) {
            return AjaxResult.error("租户不存在");
        }
        return AjaxResult.success(convertToFrontendTenant(tenant));
    }

    /**
     * 获取当前租户的配置。
     */
    @GetMapping("/current/config")
    public AjaxResult getTenantConfig(HttpServletRequest request) {
        Long tenantId = resolveTenantId(request);
        Map<String, Object> config = getDefaultConfig();
        Map<String, String> overrides = tenantConfigService.selectConfigMap(tenantId);
        applyConfigOverrides(config, overrides);
        return AjaxResult.success(config);
    }

    /**
     * 获取当前租户的站长联系方式（免登录）
     */
    @GetMapping("/current/owner")
    public AjaxResult getTenantOwner(HttpServletRequest request) {
        Long tenantId = resolveTenantId(request);
        Map<String, String> config = tenantConfigService.selectConfigMap(tenantId);
        Map<String, String> result = new HashMap<>(2);
        result.put("qrCode", StringUtils.defaultString(config.get(KEY_OWNER_QR_CODE), ""));
        result.put("remark", StringUtils.defaultString(config.get(KEY_OWNER_REMARK), ""));
        return AjaxResult.success(result);
    }

    /**
     * 根据域名获取租户信息（可选域名参数，默认读取请求域名）。
     */
    @GetMapping("/by-domain")
    public AjaxResult getTenantByDomain(@RequestParam(value = "domain", required = false) String domain,
                                        HttpServletRequest request) {
        String resolved = StringUtils.isNotEmpty(domain) ? domain.trim() : request != null ? request.getServerName() : null;
        if (StringUtils.isEmpty(resolved)) {
            resolved = "localhost";
        }
        SysTenant tenant = tenantService.selectTenantByDomain(resolved);
        if (tenant == null) {
            tenant = buildFallbackTenant(resolved);
        }
        return AjaxResult.success(convertToPublicTenant(tenant));
    }

    /**
     * 检查租户名称是否可用。
     */
    @GetMapping("/check-name")
    public AjaxResult checkNameAvailability(@RequestParam String name) {
        boolean available = "0".equals(tenantService.checkTenantNameUnique(name));
        return AjaxResult.success().put("available", available);
    }

    /**
     * 检查域名是否可用。
     */
    @GetMapping("/check-domain")
    public AjaxResult checkDomainAvailability(@RequestParam String domain) {
        boolean available = "0".equals(tenantService.checkDomainUnique(domain));
        return AjaxResult.success().put("available", available);
    }

    /**
     * 获取活跃租户列表。
     */
    @GetMapping("/active")
    public AjaxResult getActiveTenants() {
        List<SysTenant> tenants = tenantService.selectActiveTenantList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysTenant tenant : tenants) {
            result.add(convertToPublicTenant(tenant));
        }
        return AjaxResult.success(result);
    }

    /**
     * 获取指定租户的配置。
     */
    @GetMapping("/{tenantId}/config")
    public AjaxResult getTenantConfig(@PathVariable("tenantId") Long tenantId) {
        Map<String, Object> config = getDefaultConfig();
        Map<String, String> overrides = tenantConfigService.selectConfigMap(tenantId);
        applyConfigOverrides(config, overrides);
        return AjaxResult.success(config);
    }

    private boolean isLocalDomain(String domain) {
        return "localhost".equalsIgnoreCase(domain) || "127.0.0.1".equals(domain);
    }

    private Map<String, Object> convertToFrontendTenant(SysTenant tenant) {
        Map<String, Object> frontendTenant = new HashMap<>();
        frontendTenant.put("tenantId", tenant.getTenantId());
        frontendTenant.put("tenantCode", tenant.getTenantCode());
        frontendTenant.put("tenantName", tenant.getTenantName());
        frontendTenant.put("domain", tenant.getDomain());
        frontendTenant.put("contactUserName", tenant.getContactUserName());
        frontendTenant.put("contactPhone", tenant.getContactPhone());
        frontendTenant.put("companyName", tenant.getCompanyName());
        frontendTenant.put("licenseNumber", tenant.getLicenseNumber());
        frontendTenant.put("address", tenant.getAddress());
        frontendTenant.put("intro", tenant.getIntro());
        frontendTenant.put("remark", tenant.getRemark());
        frontendTenant.put("status", tenant.getStatus());
        frontendTenant.put("delFlag", tenant.getDelFlag());
        frontendTenant.put("createBy", tenant.getCreateBy());
        frontendTenant.put("createTime", tenant.getCreateTime());
        frontendTenant.put("updateBy", tenant.getUpdateBy());
        frontendTenant.put("updateTime", tenant.getUpdateTime());
        frontendTenant.put("id", tenant.getTenantId() != null ? String.valueOf(tenant.getTenantId()) : null);
        frontendTenant.put("name", tenant.getTenantName());
        frontendTenant.put("description", tenant.getIntro());
        frontendTenant.put("logo", null);
        frontendTenant.put("favicon", null);
        frontendTenant.put("subdomain", null);
        frontendTenant.put("plan", "free");
        frontendTenant.put("maxUsers", 1000);
        frontendTenant.put("maxArticles", 10000);
        frontendTenant.put("maxStorage", 1024);
        frontendTenant.put("usedStorage", 0);
        frontendTenant.put("userCount", 1);
        frontendTenant.put("articleCount", 0);
        frontendTenant.put("createdAt", tenant.getCreateTime() != null ? tenant.getCreateTime().toString() : null);
        frontendTenant.put("updatedAt", tenant.getUpdateTime() != null ? tenant.getUpdateTime().toString() : null);
        frontendTenant.put("expiredAt", tenant.getExpireTime() != null ? tenant.getExpireTime().toString() : null);

        Map<String, Object> owner = new HashMap<>();
        owner.put("id", "1");
        owner.put("username", "admin");
        owner.put("email", "admin@simpleshare.com");
        owner.put("nickname", "系统管理员");
        frontendTenant.put("owner", owner);
        return frontendTenant;
    }

    private Map<String, Object> convertToPublicTenant(SysTenant tenant) {
        Map<String, Object> publicTenant = new HashMap<>();
        publicTenant.put("id", tenant.getTenantId());
        publicTenant.put("name", tenant.getTenantName());
        publicTenant.put("domain", tenant.getDomain());
        publicTenant.put("status", tenant.getStatus());
        publicTenant.put("description", tenant.getIntro());
        publicTenant.put("createdAt", tenant.getCreateTime());
        publicTenant.put("updatedAt", tenant.getUpdateTime());
        return publicTenant;
    }

    private Map<String, Object> getDefaultConfig() {
        return new HashMap<>(tenantDefaultsProperties.toMap());
    }

    private void applyConfigOverrides(Map<String, Object> base, Map<String, String> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }

        if (StringUtils.isNotEmpty(overrides.get("copy_policy"))) {
            base.put("copyPolicy", overrides.get("copy_policy"));
        } else if (overrides.containsKey("disable_copy")) {
            boolean disabled = Boolean.parseBoolean(overrides.get("disable_copy"));
            base.put("copyPolicy", disabled ? "global_deny" : "global_allow");
        }

        if (overrides.containsKey("watermark_enabled")) {
            base.put("enableWatermark", Boolean.parseBoolean(overrides.get("watermark_enabled")));
        }

        if (StringUtils.isNotEmpty(overrides.get("watermark_text"))) {
            base.put("watermarkText", overrides.get("watermark_text"));
        }

        if (overrides.containsKey("watermark_opacity")) {
            base.put("watermarkOpacity", parseDouble(overrides.get("watermark_opacity"), 0.3D));
        }

        if (overrides.containsKey("disable_right_click")) {
            base.put("disableRightClick", Boolean.parseBoolean(overrides.get("disable_right_click")));
        }

        if (overrides.containsKey("disable_text_selection")) {
            base.put("disableTextSelection", Boolean.parseBoolean(overrides.get("disable_text_selection")));
        }

        if (overrides.containsKey("disable_dev_tools")) {
            base.put("disableDevTools", Boolean.parseBoolean(overrides.get("disable_dev_tools")));
        }
    }

    private double parseDouble(String value, double defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0) {
                return 0D;
            }
            if (parsed > 1) {
                return 1D;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Long resolveTenantId(HttpServletRequest request) {
        String domain = request != null ? request.getServerName() : null;
        if (StringUtils.isEmpty(domain)) {
            return 1L;
        }
        SysTenant tenant = tenantService.selectTenantByDomain(domain);
        if (tenant != null) {
            return tenant.getTenantId();
        }
        return 1L;
    }

    private String resolveSiteTitle() {
        String title = tenantDefaultsProperties.getSiteTitle();
        if (StringUtils.isNotEmpty(title)) {
            return title.trim();
        }
        return "默认租户";
    }

    private String resolveSiteDescription() {
        String description = tenantDefaultsProperties.getSiteDescription();
        if (StringUtils.isNotEmpty(description)) {
            return description.trim();
        }
        return "租户默认配置";
    }

    private SysTenant createDefaultTenant(HttpServletRequest request) {
        String domain = request != null ? request.getServerName() : "localhost";
        SysTenant tenant = new SysTenant();
        tenant.setTenantCode("default");
        tenant.setTenantName("默认租户");
        tenant.setDomain(domain);
        tenant.setStatus("0");
        tenant.setDelFlag("0");
        tenant.setCreateBy(1L);
        tenant.setContactUserName("系统管理员");
        tenant.setContactPhone("13800138000");
        String siteTitle = resolveSiteTitle();
        String siteDescription = resolveSiteDescription();
        tenant.setCompanyName(siteTitle);
        String intro = StringUtils.isNotEmpty(siteDescription)
            ? siteDescription
            : siteTitle + " 默认租户，用于本地开发测试";
        tenant.setIntro(intro);

        int result = tenantService.insertTenant(tenant);
        if (result > 0) {
            return tenantService.selectTenantByDomain(domain);
        }
        return null;
    }

    private SysTenant buildFallbackTenant(String domain) {
        SysTenant fallback = new SysTenant();
        fallback.setTenantId(1L);
        fallback.setTenantCode("default");
        String siteTitle = resolveSiteTitle();
        String siteDescription = resolveSiteDescription();
        fallback.setTenantName(siteTitle);
        fallback.setDomain(domain);
        fallback.setStatus("0");
        String fallbackIntro = StringUtils.isNotEmpty(siteDescription)
            ? siteDescription
            : siteTitle + " 默认配置";
        fallback.setIntro(fallbackIntro);
        return fallback;
    }
}




