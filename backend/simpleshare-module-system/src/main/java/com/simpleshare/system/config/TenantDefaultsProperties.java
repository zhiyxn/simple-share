package com.simpleshare.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租户默认配置属性
 */
@Component
@ConfigurationProperties(prefix = "simpleshare.tenant-defaults")
public class TenantDefaultsProperties {

    private String siteTitle;
    private String siteDescription;
    private boolean allowRegistration = true;
    private boolean requireEmailVerification = false;
    private boolean enableComments = true;
    private boolean enableLikes = true;
    private boolean enableSharing = true;
    private int maxFileSize = 10;
    private List<String> allowedFileTypes = new ArrayList<>();
    private String copyPolicy = "follow_article";
    private boolean enableWatermark = true;
    private String watermarkText;
    private double watermarkOpacity = 0.3D;
    private boolean disableRightClick = false;
    private boolean disableTextSelection = false;
    private boolean disableDevTools = false;
    private Theme theme = new Theme();

    public String getSiteTitle() {
        return siteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public void setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }

    public boolean isRequireEmailVerification() {
        return requireEmailVerification;
    }

    public void setRequireEmailVerification(boolean requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
    }

    public boolean isEnableComments() {
        return enableComments;
    }

    public void setEnableComments(boolean enableComments) {
        this.enableComments = enableComments;
    }

    public boolean isEnableLikes() {
        return enableLikes;
    }

    public void setEnableLikes(boolean enableLikes) {
        this.enableLikes = enableLikes;
    }

    public boolean isEnableSharing() {
        return enableSharing;
    }

    public void setEnableSharing(boolean enableSharing) {
        this.enableSharing = enableSharing;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(List<String> allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public String getCopyPolicy() {
        return copyPolicy;
    }

    public void setCopyPolicy(String copyPolicy) {
        this.copyPolicy = copyPolicy;
    }

    public boolean isEnableWatermark() {
        return enableWatermark;
    }

    public void setEnableWatermark(boolean enableWatermark) {
        this.enableWatermark = enableWatermark;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public double getWatermarkOpacity() {
        return watermarkOpacity;
    }

    public void setWatermarkOpacity(double watermarkOpacity) {
        this.watermarkOpacity = watermarkOpacity;
    }

    public boolean isDisableRightClick() {
        return disableRightClick;
    }

    public void setDisableRightClick(boolean disableRightClick) {
        this.disableRightClick = disableRightClick;
    }

    public boolean isDisableTextSelection() {
        return disableTextSelection;
    }

    public void setDisableTextSelection(boolean disableTextSelection) {
        this.disableTextSelection = disableTextSelection;
    }

    public boolean isDisableDevTools() {
        return disableDevTools;
    }

    public void setDisableDevTools(boolean disableDevTools) {
        this.disableDevTools = disableDevTools;
    }

    public List<String> getNormalizedAllowedFileTypes() {
        List<String> fileTypes = allowedFileTypes != null ? allowedFileTypes : Collections.emptyList();
        return fileTypes.stream()
            .filter(Objects::nonNull)
            .flatMap(item -> Arrays.stream(item.split(",")))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        String resolvedTitle = siteTitle != null ? siteTitle.trim() : "";
        String resolvedDescription = siteDescription != null ? siteDescription.trim() : "";
        String resolvedWatermark = watermarkText != null && !watermarkText.trim().isEmpty()
            ? watermarkText.trim()
            : resolvedTitle;
        map.put("siteTitle", resolvedTitle);
        map.put("siteDescription", resolvedDescription);
        map.put("allowRegistration", allowRegistration);
        map.put("requireEmailVerification", requireEmailVerification);
        map.put("enableComments", enableComments);
        map.put("enableLikes", enableLikes);
        map.put("enableSharing", enableSharing);
        map.put("maxFileSize", maxFileSize);
        map.put("allowedFileTypes", new ArrayList<>(getNormalizedAllowedFileTypes()));
        map.put("copyPolicy", copyPolicy);
        map.put("enableWatermark", enableWatermark);
        map.put("watermarkText", resolvedWatermark);
        map.put("watermarkOpacity", watermarkOpacity);
        map.put("disableRightClick", disableRightClick);
        map.put("disableTextSelection", disableTextSelection);
        map.put("disableDevTools", disableDevTools);
        map.put("theme", theme != null ? theme.toMap() : Collections.emptyMap());
        return map;
    }

    public static class Theme {
        private String primaryColor = "#409EFF";
        private String secondaryColor = "#67C23A";
        private String backgroundColor = "#FFFFFF";
        private String textColor = "#303133";

        public String getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }

        public String getSecondaryColor() {
            return secondaryColor;
        }

        public void setSecondaryColor(String secondaryColor) {
            this.secondaryColor = secondaryColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public String getTextColor() {
            return textColor;
        }

        public void setTextColor(String textColor) {
            this.textColor = textColor;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> themeMap = new HashMap<>();
            themeMap.put("primaryColor", primaryColor);
            themeMap.put("secondaryColor", secondaryColor);
            themeMap.put("backgroundColor", backgroundColor);
            themeMap.put("textColor", textColor);
            return themeMap;
        }
    }
}
