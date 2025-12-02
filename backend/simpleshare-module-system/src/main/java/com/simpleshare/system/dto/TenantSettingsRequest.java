package com.simpleshare.system.dto;

import com.simpleshare.common.config.SimpleShareConfig;
import com.simpleshare.common.utils.StringUtils;

import java.io.Serializable;

/**
 * 租户配置请求 DTO
 */
public class TenantSettingsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private BasicSettings basic;
    private ThemeSettings theme;
    private FeatureSettings features;
    private EmailSettings email;
    private StorageSettings storage;
    private OwnerSettings owner;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public BasicSettings getBasic() {
        return basic;
    }

    public void setBasic(BasicSettings basic) {
        this.basic = basic;
    }

    public ThemeSettings getTheme() {
        return theme;
    }

    public void setTheme(ThemeSettings theme) {
        this.theme = theme;
    }

    public FeatureSettings getFeatures() {
        return features;
    }

    public void setFeatures(FeatureSettings features) {
        this.features = features;
    }

    public EmailSettings getEmail() {
        return email;
    }

    public void setEmail(EmailSettings email) {
        this.email = email;
    }

    public StorageSettings getStorage() {
        return storage;
    }

    public void setStorage(StorageSettings storage) {
        this.storage = storage;
    }

    public OwnerSettings getOwner() {
        return owner;
    }

    public void setOwner(OwnerSettings owner) {
        this.owner = owner;
    }

    public static class OwnerSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private String qrCode;
        private String remark;

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class BasicSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String code;
        private String email;
        private String phone;
        private String title;
        private String description;
        private String keywords;
        private String logo;
        private String favicon;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getFavicon() {
            return favicon;
        }

        public void setFavicon(String favicon) {
            this.favicon = favicon;
        }
    }

    public static class ThemeSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private String mode;
        private String primaryColor;
        private String successColor;
        private String warningColor;
        private String dangerColor;
        private String fontFamily;
        private Integer fontSize;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }

        public String getSuccessColor() {
            return successColor;
        }

        public void setSuccessColor(String successColor) {
            this.successColor = successColor;
        }

        public String getWarningColor() {
            return warningColor;
        }

        public void setWarningColor(String warningColor) {
            this.warningColor = warningColor;
        }

        public String getDangerColor() {
            return dangerColor;
        }

        public void setDangerColor(String dangerColor) {
            this.dangerColor = dangerColor;
        }

        public String getFontFamily() {
            return fontFamily;
        }

        public void setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
        }

        public Integer getFontSize() {
            return fontSize;
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
        }
    }

    public static class FeatureSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private Boolean userRegistration;
        private Boolean emailVerification;
        private Boolean socialLogin;
        private Boolean articleComments;
        private Boolean articleLikes;
        private Boolean articleFavorites;
        private Boolean articleShare;
        private Boolean copyProtection;
        private Boolean watermark;
        private Boolean analytics;
        private Boolean search;
        private Boolean rss;
        private Boolean sitemap;

        public Boolean getUserRegistration() {
            return userRegistration;
        }

        public void setUserRegistration(Boolean userRegistration) {
            this.userRegistration = userRegistration;
        }

        public Boolean getEmailVerification() {
            return emailVerification;
        }

        public void setEmailVerification(Boolean emailVerification) {
            this.emailVerification = emailVerification;
        }

        public Boolean getSocialLogin() {
            return socialLogin;
        }

        public void setSocialLogin(Boolean socialLogin) {
            this.socialLogin = socialLogin;
        }

        public Boolean getArticleComments() {
            return articleComments;
        }

        public void setArticleComments(Boolean articleComments) {
            this.articleComments = articleComments;
        }

        public Boolean getArticleLikes() {
            return articleLikes;
        }

        public void setArticleLikes(Boolean articleLikes) {
            this.articleLikes = articleLikes;
        }

        public Boolean getArticleFavorites() {
            return articleFavorites;
        }

        public void setArticleFavorites(Boolean articleFavorites) {
            this.articleFavorites = articleFavorites;
        }

        public Boolean getArticleShare() {
            return articleShare;
        }

        public void setArticleShare(Boolean articleShare) {
            this.articleShare = articleShare;
        }

        public Boolean getCopyProtection() {
            return copyProtection;
        }

        public void setCopyProtection(Boolean copyProtection) {
            this.copyProtection = copyProtection;
        }

        public Boolean getWatermark() {
            return watermark;
        }

        public void setWatermark(Boolean watermark) {
            this.watermark = watermark;
        }

        public Boolean getAnalytics() {
            return analytics;
        }

        public void setAnalytics(Boolean analytics) {
            this.analytics = analytics;
        }

        public Boolean getSearch() {
            return search;
        }

        public void setSearch(Boolean search) {
            this.search = search;
        }

        public Boolean getRss() {
            return rss;
        }

        public void setRss(Boolean rss) {
            this.rss = rss;
        }

        public Boolean getSitemap() {
            return sitemap;
        }

        public void setSitemap(Boolean sitemap) {
            this.sitemap = sitemap;
        }
    }

    public static class EmailSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private String host;
        private Integer port;
        private Boolean secure;
        private String username;
        private String password;
        private String from;
        private String senderName;
        private String replyTo;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Boolean getSecure() {
            return secure;
        }

        public void setSecure(Boolean secure) {
            this.secure = secure;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }
    }


    public static class StorageSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private String type;
        private Integer maxSize;
        private String[] allowedTypes;
        private LocalStorageConfig local = new LocalStorageConfig();
        private CloudStorageConfig aliyun = new CloudStorageConfig();
        private CloudStorageConfig qiniu = new CloudStorageConfig();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }

        public String[] getAllowedTypes() {
            return allowedTypes;
        }

        public void setAllowedTypes(String[] allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        public LocalStorageConfig getLocal() {
            return local;
        }

        public void setLocal(LocalStorageConfig local) {
            this.local = local;
        }

        public CloudStorageConfig getAliyun() {
            return aliyun;
        }

        public void setAliyun(CloudStorageConfig aliyun) {
            this.aliyun = aliyun;
        }

        public CloudStorageConfig getQiniu() {
            return qiniu;
        }

        public void setQiniu(CloudStorageConfig qiniu) {
            this.qiniu = qiniu;
        }

        public void ensureDefaults() {
            if (StringUtils.isEmpty(this.type)) {
                this.type = "local";
            }
            if (this.maxSize == null) {
                this.maxSize = 10;
            }
            if (this.allowedTypes == null || this.allowedTypes.length == 0) {
                this.allowedTypes = new String[]{"image", "document"};
            }
            if (this.local == null) {
                this.local = new LocalStorageConfig();
            }
            this.local.ensureDefaults();
            if (this.aliyun == null) {
                this.aliyun = new CloudStorageConfig();
            }
            this.aliyun.ensureDefaults();
            if (this.qiniu == null) {
                this.qiniu = new CloudStorageConfig();
            }
            this.qiniu.ensureDefaults();
        }

        public CloudStorageConfig resolveCloudConfig(String provider) {
            if ("qiniu".equalsIgnoreCase(provider)) {
                return this.qiniu;
            }
            return this.aliyun;
        }

        public static class LocalStorageConfig implements Serializable {
            private static final long serialVersionUID = 1L;

            private String basePath;
            private String baseUrl;
            private String pathPrefix;

            public String getBasePath() {
                return basePath;
            }

            public void setBasePath(String basePath) {
                this.basePath = basePath;
            }

            public String getBaseUrl() {
                return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public String getPathPrefix() {
                return pathPrefix;
            }

            public void setPathPrefix(String pathPrefix) {
                this.pathPrefix = pathPrefix;
            }

            public void ensureDefaults() {
                if (StringUtils.isEmpty(this.basePath)) {
                    this.basePath = SimpleShareConfig.getUploadPath();
                }
                if (StringUtils.isEmpty(this.pathPrefix)) {
                    this.pathPrefix = "";
                }
        if (StringUtils.isEmpty(this.baseUrl)) {
            this.baseUrl = "/api/upload";
        }
    }
        }

        public static class CloudStorageConfig implements Serializable {
            private static final long serialVersionUID = 1L;

            private String endpoint;
            private String bucket;
            private String accessKey;
            private String secretKey;
            private String region;
            private String domain;
            private String pathPrefix;

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getBucket() {
                return bucket;
            }

            public void setBucket(String bucket) {
                this.bucket = bucket;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public String getPathPrefix() {
                return pathPrefix;
            }

            public void setPathPrefix(String pathPrefix) {
                this.pathPrefix = pathPrefix;
            }

            public void ensureDefaults() {
                if (this.pathPrefix == null) {
                    this.pathPrefix = "";
                }
            }
        }
    }
}
