package com.simpleshare.system.domain.vo;

import java.io.Serializable;

/**
 * 租户层内容保护配置聚合。
 */
public class TenantProtectionSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private String copyPolicy;

    private boolean watermarkEnabled;

    private String watermarkText;

    private double watermarkOpacity;

    public TenantProtectionSettings(String copyPolicy, boolean watermarkEnabled, String watermarkText, double watermarkOpacity) {
        this.copyPolicy = copyPolicy;
        this.watermarkEnabled = watermarkEnabled;
        this.watermarkText = watermarkText;
        this.watermarkOpacity = watermarkOpacity;
    }

    public String getCopyPolicy() {
        return copyPolicy;
    }

    public void setCopyPolicy(String copyPolicy) {
        this.copyPolicy = copyPolicy;
    }

    public boolean isWatermarkEnabled() {
        return watermarkEnabled;
    }

    public void setWatermarkEnabled(boolean watermarkEnabled) {
        this.watermarkEnabled = watermarkEnabled;
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
}

