package com.simpleshare.system.dto;

import java.io.Serializable;

/**
 * 租户配置响应 DTO
 */
public class TenantSettingsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private TenantSettingsRequest.BasicSettings basic;
    private TenantSettingsRequest.ThemeSettings theme;
    private TenantSettingsRequest.FeatureSettings features;
    private TenantSettingsRequest.EmailSettings email;
    private TenantSettingsRequest.StorageSettings storage;
    private TenantSettingsRequest.OwnerSettings owner;

    public TenantSettingsRequest.BasicSettings getBasic() {
        return basic;
    }

    public void setBasic(TenantSettingsRequest.BasicSettings basic) {
        this.basic = basic;
    }

    public TenantSettingsRequest.ThemeSettings getTheme() {
        return theme;
    }

    public void setTheme(TenantSettingsRequest.ThemeSettings theme) {
        this.theme = theme;
    }

    public TenantSettingsRequest.FeatureSettings getFeatures() {
        return features;
    }

    public void setFeatures(TenantSettingsRequest.FeatureSettings features) {
        this.features = features;
    }

    public TenantSettingsRequest.EmailSettings getEmail() {
        return email;
    }

    public void setEmail(TenantSettingsRequest.EmailSettings email) {
        this.email = email;
    }

    public TenantSettingsRequest.StorageSettings getStorage() {
        return storage;
    }

    public void setStorage(TenantSettingsRequest.StorageSettings storage) {
        this.storage = storage;
    }

    public TenantSettingsRequest.OwnerSettings getOwner() {
        return owner;
    }

    public void setOwner(TenantSettingsRequest.OwnerSettings owner) {
        this.owner = owner;
    }
}
