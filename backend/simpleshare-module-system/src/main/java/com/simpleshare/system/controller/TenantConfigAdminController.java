package com.simpleshare.system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.common.core.controller.BaseController;
import com.simpleshare.common.core.domain.AjaxResult;
import com.simpleshare.common.core.domain.entity.SysTenant;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.aspectj.lang.annotation.TenantIgnore;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.system.config.TenantDefaultsProperties;
import com.simpleshare.system.domain.SysTenantConfig;
import com.simpleshare.system.dto.TenantSettingsRequest;
import com.simpleshare.system.dto.TenantSettingsResponse;
import com.simpleshare.system.service.ISysTenantConfigService;
import com.simpleshare.system.service.ISysTenantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 后台租户配置控制器
 */
@RestController
@RequestMapping("/system/admin/tenant-config")
@Validated
public class TenantConfigAdminController extends BaseController {

    private static final String KEY_SETTINGS_BASIC = "settings_basic";
    private static final String KEY_SETTINGS_THEME = "settings_theme";
    private static final String KEY_SETTINGS_FEATURES = "settings_features";
    private static final String KEY_SETTINGS_EMAIL = "settings_email";
    private static final String KEY_SETTINGS_STORAGE = "settings_storage";

    private static final String KEY_SITE_TITLE = "site_title";
    private static final String KEY_SITE_DESCRIPTION = "site_description";
    private static final String KEY_SITE_KEYWORDS = "site_keywords";
    private static final String KEY_SITE_LOGO = "site_logo";
    private static final String KEY_SITE_FAVICON = "site_favicon";

    private static final String KEY_COPY_POLICY = "copy_policy";
    private static final String KEY_DISABLE_COPY = "disable_copy";
    private static final String KEY_WATERMARK_ENABLED = "watermark_enabled";
    private static final String KEY_WATERMARK_TEXT = "watermark_text";
    private static final String KEY_WATERMARK_OPACITY = "watermark_opacity";
    private static final String KEY_DISABLE_RIGHT_CLICK = "disable_right_click";
    private static final String KEY_DISABLE_TEXT_SELECTION = "disable_text_selection";
    private static final String KEY_DISABLE_DEV_TOOLS = "disable_dev_tools";
    private static final String KEY_MAX_FILE_SIZE = "max_file_size";
    private static final String KEY_ALLOWED_FILE_TYPES = "allowed_file_types";

    private static final String KEY_OWNER_QR_CODE = "owner_qr_code";
    private static final String KEY_OWNER_REMARK = "owner_remark";

    private final ISysTenantConfigService tenantConfigService;
    private final ISysTenantService tenantService;
    private final ObjectMapper objectMapper;
    private final TenantDefaultsProperties tenantDefaultsProperties;

    public TenantConfigAdminController(ISysTenantConfigService tenantConfigService,
                                       ISysTenantService tenantService,
                                       ObjectMapper objectMapper,
                                       TenantDefaultsProperties tenantDefaultsProperties) {
        this.tenantConfigService = tenantConfigService;
        this.tenantService = tenantService;
        this.objectMapper = objectMapper;
        this.tenantDefaultsProperties = tenantDefaultsProperties;
    }

    @GetMapping("/settings")
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    public AjaxResult getSettings(@RequestParam(value = "tenantId", required = false) Long tenantId) {
        Long currentTenantId = resolveTenantId(tenantId);
        SysTenant tenant = tenantService.selectTenantById(currentTenantId);
        Map<String, String> configMap = tenantConfigService.selectConfigMap(currentTenantId);

        TenantSettingsResponse response = new TenantSettingsResponse();
        response.setBasic(buildBasicSettings(tenant, configMap));
        response.setTheme(buildThemeSettings(configMap));
        response.setFeatures(buildFeatureSettings(configMap));
        response.setEmail(buildEmailSettings(configMap));
        response.setOwner(buildOwnerSettings(configMap));
        response.setStorage(buildStorageSettings(configMap));

        return AjaxResult.success(response);
    }

    @PutMapping("/settings")
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    public AjaxResult updateSettings(@RequestBody TenantSettingsRequest requestBody) {
        Long tenantId = resolveTenantId(requestBody.getTenantId());
        SysTenant tenant = tenantService.selectTenantById(tenantId);
        if (tenant == null) {
            return AjaxResult.error("租户不存在");
        }

        applyBasicSettings(tenant, requestBody.getBasic());
        tenant.setUpdateBy(getUserId());
        tenant.setUpdateTime(LocalDateTime.now());
        tenantService.updateTenant(tenant);

        List<SysTenantConfig> configs = buildConfigEntries(tenantId, requestBody);
        tenantConfigService.saveOrUpdateConfigs(tenantId, configs);

        return AjaxResult.success();
    }

    @PostMapping("/email/test")
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    @TenantIgnore
    public AjaxResult testEmailConnection(@RequestBody TenantSettingsRequest.EmailSettings emailSettings) {
        if (emailSettings == null) {
            return AjaxResult.error("邮箱配置不能为空");
        }
        if (StringUtils.isEmpty(emailSettings.getHost())) {
            return AjaxResult.error("SMTP服务器地址不能为空");
        }
        if (emailSettings.getPort() == null || emailSettings.getPort() <= 0) {
            return AjaxResult.error("SMTP端口号不正确");
        }
        if (StringUtils.isEmpty(emailSettings.getUsername())) {
            return AjaxResult.error("SMTP用户名不能为空");
        }
        if (StringUtils.isEmpty(emailSettings.getPassword())) {
            return AjaxResult.error("SMTP密码不能为空");
        }
        if (StringUtils.isEmpty(emailSettings.getFrom())) {
            return AjaxResult.error("发件人邮箱不能为空");
        }
        return AjaxResult.success("邮箱配置验证成功");
    }

    private Long resolveTenantId(Long tenantId) {
        if (tenantId != null && tenantId > 0) {
            return tenantId;
        }
        String contextId = TenantContextHolder.getTenantId();
        if (StringUtils.isNotEmpty(contextId)) {
            try {
                return Long.parseLong(contextId);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1L;
    }

    private String defaultSiteTitle() {
        String title = tenantDefaultsProperties.getSiteTitle();
        if (StringUtils.isNotEmpty(title)) {
            return title.trim();
        }
        return "默认租户";
    }

    private String defaultSiteDescription() {
        String description = tenantDefaultsProperties.getSiteDescription();
        if (StringUtils.isNotEmpty(description)) {
            return description.trim();
        }
        return "租户默认配置";
    }

    private String defaultSiteKeywords() {
        String title = defaultSiteTitle();
        return title + ",内容分享";
    }

    private String defaultCopyPolicy() {
        String policy = tenantDefaultsProperties.getCopyPolicy();
        if (StringUtils.isNotEmpty(policy)) {
            return policy.trim();
        }
        return "follow_article";
    }

    private boolean defaultWatermarkEnabled() {
        return tenantDefaultsProperties.isEnableWatermark();
    }

    private double defaultWatermarkOpacity() {
        double value = tenantDefaultsProperties.getWatermarkOpacity();
        if (value < 0D) {
            return 0D;
        }
        if (value > 1D) {
            return 1D;
        }
        return value;
    }

    private String defaultWatermarkText() {
        String text = tenantDefaultsProperties.getWatermarkText();
        if (StringUtils.isNotEmpty(text)) {
            return text.trim();
        }
        return defaultSiteTitle();
    }

    private int defaultMaxFileSize() {
        return tenantDefaultsProperties.getMaxFileSize();
    }

    private List<String> defaultAllowedFileTypes() {
        return tenantDefaultsProperties.getNormalizedAllowedFileTypes();
    }

    private boolean defaultDisableRightClick() {
        return tenantDefaultsProperties.isDisableRightClick();
    }

    private boolean defaultDisableTextSelection() {
        return tenantDefaultsProperties.isDisableTextSelection();
    }

    private boolean defaultDisableDevTools() {
        return tenantDefaultsProperties.isDisableDevTools();
    }

    private TenantSettingsRequest.BasicSettings buildBasicSettings(SysTenant tenant, Map<String, String> configMap) {
        TenantSettingsRequest.BasicSettings basic = new TenantSettingsRequest.BasicSettings();
        String defaultTitle = defaultSiteTitle();
        String defaultDescription = defaultSiteDescription();
        String defaultKeywords = defaultSiteKeywords();
        basic.setName(tenant != null && StringUtils.isNotEmpty(tenant.getTenantName()) ? tenant.getTenantName() : defaultTitle);
        basic.setCode(tenant != null && StringUtils.isNotEmpty(tenant.getTenantCode()) ? tenant.getTenantCode() : "default");
        basic.setPhone(tenant != null ? tenant.getContactPhone() : null);
        basic.setEmail(configMap.getOrDefault("contact_email", ""));
        basic.setTitle(configMap.getOrDefault(KEY_SITE_TITLE, defaultTitle));
        basic.setDescription(configMap.getOrDefault(KEY_SITE_DESCRIPTION, defaultDescription));
        basic.setKeywords(configMap.getOrDefault(KEY_SITE_KEYWORDS, defaultKeywords));
        basic.setLogo(configMap.getOrDefault(KEY_SITE_LOGO, ""));
        basic.setFavicon(configMap.getOrDefault(KEY_SITE_FAVICON, ""));
        return basic;
    }

    private TenantSettingsRequest.ThemeSettings buildThemeSettings(Map<String, String> configMap) {
        TenantSettingsRequest.ThemeSettings theme = readJson(configMap.get(KEY_SETTINGS_THEME), TenantSettingsRequest.ThemeSettings.class, new TenantSettingsRequest.ThemeSettings());
        if (theme.getMode() == null) {
            theme.setMode("light");
        }
        if (theme.getPrimaryColor() == null) {
            theme.setPrimaryColor("#409EFF");
        }
        if (theme.getSuccessColor() == null) {
            theme.setSuccessColor("#67C23A");
        }
        if (theme.getWarningColor() == null) {
            theme.setWarningColor("#E6A23C");
        }
        if (theme.getDangerColor() == null) {
            theme.setDangerColor("#F56C6C");
        }
        if (theme.getFontFamily() == null) {
            theme.setFontFamily("system");
        }
        if (theme.getFontSize() == null) {
            theme.setFontSize(14);
        }
        return theme;
    }

    private TenantSettingsRequest.FeatureSettings buildFeatureSettings(Map<String, String> configMap) {
        TenantSettingsRequest.FeatureSettings features = readJson(configMap.get(KEY_SETTINGS_FEATURES), TenantSettingsRequest.FeatureSettings.class, new TenantSettingsRequest.FeatureSettings());
        if (features.getUserRegistration() == null) {
            features.setUserRegistration(tenantDefaultsProperties.isAllowRegistration());
        }
        if (features.getEmailVerification() == null) {
            features.setEmailVerification(tenantDefaultsProperties.isRequireEmailVerification());
        }
        if (features.getSocialLogin() == null) {
            features.setSocialLogin(Boolean.FALSE);
        }
        if (features.getArticleComments() == null) {
            features.setArticleComments(tenantDefaultsProperties.isEnableComments());
        }
        if (features.getArticleLikes() == null) {
            features.setArticleLikes(tenantDefaultsProperties.isEnableLikes());
        }
        if (features.getArticleFavorites() == null) {
            features.setArticleFavorites(tenantDefaultsProperties.isEnableLikes());
        }
        if (features.getArticleShare() == null) {
            features.setArticleShare(tenantDefaultsProperties.isEnableSharing());
        }
        if (features.getCopyProtection() == null) {
            String copyPolicy = configMap.getOrDefault(KEY_COPY_POLICY, defaultCopyPolicy());
            features.setCopyProtection("global_deny".equalsIgnoreCase(copyPolicy));
        }
        if (features.getWatermark() == null) {
            features.setWatermark(Boolean.parseBoolean(configMap.getOrDefault(KEY_WATERMARK_ENABLED, String.valueOf(defaultWatermarkEnabled()))));
        }
        if (features.getAnalytics() == null) {
            features.setAnalytics(Boolean.TRUE);
        }
        if (features.getSearch() == null) {
            features.setSearch(Boolean.TRUE);
        }
        if (features.getRss() == null) {
            features.setRss(Boolean.TRUE);
        }
        if (features.getSitemap() == null) {
            features.setSitemap(Boolean.TRUE);
        }
        return features;
    }

    private TenantSettingsRequest.EmailSettings buildEmailSettings(Map<String, String> configMap) {
        TenantSettingsRequest.EmailSettings email = readJson(configMap.get(KEY_SETTINGS_EMAIL), TenantSettingsRequest.EmailSettings.class, new TenantSettingsRequest.EmailSettings());
        if (email.getPort() == null) {
            email.setPort(587);
        }
        if (email.getSecure() == null) {
            email.setSecure(Boolean.FALSE);
        }
        return email;
    }

    private TenantSettingsRequest.OwnerSettings buildOwnerSettings(Map<String, String> configMap) {
        TenantSettingsRequest.OwnerSettings owner = new TenantSettingsRequest.OwnerSettings();
        owner.setQrCode(defaultString(configMap.get(KEY_OWNER_QR_CODE), ""));
        owner.setRemark(defaultString(configMap.get(KEY_OWNER_REMARK), ""));
        return owner;
    }

    private TenantSettingsRequest.StorageSettings buildStorageSettings(Map<String, String> configMap) {
        TenantSettingsRequest.StorageSettings storage = readJson(configMap.get(KEY_SETTINGS_STORAGE), TenantSettingsRequest.StorageSettings.class, new TenantSettingsRequest.StorageSettings());
        if (StringUtils.isEmpty(storage.getType())) {
            storage.setType("local");
        }
        Integer configuredMaxSize = parseInteger(configMap.get(KEY_MAX_FILE_SIZE), 10);
        if (storage.getMaxSize() == null || storage.getMaxSize() <= 0) {
            storage.setMaxSize(configuredMaxSize);
        }
        if (storage.getAllowedTypes() == null || storage.getAllowedTypes().length == 0) {
            String allowed = configMap.get(KEY_ALLOWED_FILE_TYPES);
            if (StringUtils.isNotEmpty(allowed)) {
                storage.setAllowedTypes(splitCsv(allowed));
            }
        }
        storage.ensureDefaults();
        return storage;
    }

    private void applyBasicSettings(SysTenant tenant, TenantSettingsRequest.BasicSettings basic) {
        if (basic == null) {
            return;
        }
        if (StringUtils.isNotEmpty(basic.getName())) {
            tenant.setTenantName(basic.getName().trim());
        }
        if (StringUtils.isNotEmpty(basic.getPhone())) {
            tenant.setContactPhone(basic.getPhone().trim());
        }
    }

    private List<SysTenantConfig> buildConfigEntries(Long tenantId, TenantSettingsRequest requestBody) {
        List<SysTenantConfig> configs = new ArrayList<>();
        TenantSettingsRequest.BasicSettings basic = requestBody.getBasic();
        if (basic != null) {
            putConfig(configs, tenantId, KEY_SETTINGS_BASIC, toJson(basic));
            if (StringUtils.isNotEmpty(basic.getEmail())) {
                putConfig(configs, tenantId, "contact_email", basic.getEmail().trim());
            }
            putConfig(configs, tenantId, KEY_SITE_TITLE, defaultString(basic.getTitle(), defaultSiteTitle()));
            putConfig(configs, tenantId, KEY_SITE_DESCRIPTION, defaultString(basic.getDescription(), defaultSiteDescription()));
            putConfig(configs, tenantId, KEY_SITE_KEYWORDS, defaultString(basic.getKeywords(), defaultSiteKeywords()));
            putConfig(configs, tenantId, KEY_SITE_LOGO, defaultString(basic.getLogo(), ""));
            putConfig(configs, tenantId, KEY_SITE_FAVICON, defaultString(basic.getFavicon(), ""));
        }

        TenantSettingsRequest.ThemeSettings theme = requestBody.getTheme();
        if (theme != null) {
            putConfig(configs, tenantId, KEY_SETTINGS_THEME, toJson(theme));
        }

        TenantSettingsRequest.FeatureSettings features = requestBody.getFeatures();
        if (features != null) {
            putConfig(configs, tenantId, KEY_SETTINGS_FEATURES, toJson(features));
            putConfig(configs, tenantId, KEY_COPY_POLICY, resolveCopyPolicy(features));
            putConfig(configs, tenantId, KEY_DISABLE_COPY, String.valueOf(Boolean.TRUE.equals(features.getCopyProtection())));
            if (requestBody.getBasic() != null) {
                String watermarkText = defaultString(requestBody.getBasic().getTitle(), defaultWatermarkText());
                putConfig(configs, tenantId, KEY_WATERMARK_TEXT, watermarkText);
            }
        }

        TenantSettingsRequest.EmailSettings email = requestBody.getEmail();
        if (email != null) {
            putConfig(configs, tenantId, KEY_SETTINGS_EMAIL, toJson(email));
        }

        TenantSettingsRequest.OwnerSettings owner = requestBody.getOwner();
        String ownerQr = owner != null ? defaultString(owner.getQrCode(), "") : "";
        String ownerRemark = owner != null ? defaultString(owner.getRemark(), "") : "";
        putConfig(configs, tenantId, KEY_OWNER_QR_CODE, ownerQr);
        putConfig(configs, tenantId, KEY_OWNER_REMARK, ownerRemark);

        TenantSettingsRequest.StorageSettings storage = requestBody.getStorage();
        if (storage != null) {
            storage.ensureDefaults();
            putConfig(configs, tenantId, KEY_SETTINGS_STORAGE, toJson(storage));
            if (storage.getMaxSize() != null) {
                putConfig(configs, tenantId, KEY_MAX_FILE_SIZE, storage.getMaxSize().toString());
            }
            if (storage.getAllowedTypes() != null && storage.getAllowedTypes().length > 0) {
                putConfig(configs, tenantId, KEY_ALLOWED_FILE_TYPES, String.join(",", storage.getAllowedTypes()));
            }
        }
        if (storage == null || storage.getMaxSize() == null) {
            putConfig(configs, tenantId, KEY_MAX_FILE_SIZE, String.valueOf(defaultMaxFileSize()));
        }
        if (storage == null || storage.getAllowedTypes() == null || storage.getAllowedTypes().length == 0) {
            List<String> allowedTypes = defaultAllowedFileTypes();
            if (!allowedTypes.isEmpty()) {
                putConfig(configs, tenantId, KEY_ALLOWED_FILE_TYPES, String.join(",", allowedTypes));
            }
        }

        String disableRightClick = features != null
            ? String.valueOf(Boolean.TRUE.equals(features.getCopyProtection()))
            : String.valueOf(defaultDisableRightClick());
        putConfig(configs, tenantId, KEY_DISABLE_RIGHT_CLICK, disableRightClick);
        String disableTextSelection = features != null
            ? String.valueOf(Boolean.TRUE.equals(features.getCopyProtection()))
            : String.valueOf(defaultDisableTextSelection());
        putConfig(configs, tenantId, KEY_DISABLE_TEXT_SELECTION, disableTextSelection);
        putConfig(configs, tenantId, KEY_DISABLE_DEV_TOOLS, String.valueOf(defaultDisableDevTools()));

        if (features != null && features.getWatermark() != null) {
            putConfig(configs, tenantId, KEY_WATERMARK_ENABLED, String.valueOf(features.getWatermark()));
        } else {
            putConfig(configs, tenantId, KEY_WATERMARK_ENABLED, String.valueOf(defaultWatermarkEnabled()));
        }
        if (features != null) {
            putConfig(configs, tenantId, KEY_WATERMARK_TEXT, defaultString(requestBody.getBasic() != null ? requestBody.getBasic().getTitle() : null, defaultWatermarkText()));
            putConfig(configs, tenantId, KEY_WATERMARK_OPACITY, String.valueOf(defaultWatermarkOpacity()));
        } else {
            putConfig(configs, tenantId, KEY_WATERMARK_TEXT, defaultWatermarkText());
            putConfig(configs, tenantId, KEY_WATERMARK_OPACITY, String.valueOf(defaultWatermarkOpacity()));
        }
        return configs;
    }

    private String resolveCopyPolicy(TenantSettingsRequest.FeatureSettings features) {
        if (features == null) {
            return defaultCopyPolicy();
        }
        if (Boolean.TRUE.equals(features.getCopyProtection())) {
            return "global_deny";
        }
        return defaultCopyPolicy();
    }

    private void putConfig(List<SysTenantConfig> configs, Long tenantId, String key, String value) {
        if (key == null || value == null) {
            return;
        }
        SysTenantConfig config = new SysTenantConfig();
        config.setTenantId(tenantId);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType("tenant_settings");
        config.setUpdateTime(new Date(System.currentTimeMillis()));
        configs.add(config);
    }

    private String toJson(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private <T> T readJson(String json, Class<T> clazz, T defaultValue) {
        if (StringUtils.isEmpty(json)) {
            return defaultValue;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.isNotEmpty(value) ? value : fallback;
    }

    private Integer parseInteger(String value, int fallback) {
        if (StringUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String[] splitCsv(String csv) {
        if (StringUtils.isEmpty(csv)) {
            return new String[0];
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);
    }
}
