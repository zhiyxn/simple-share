package com.simpleshare.framework.storage.config;

import java.io.Serializable;

/**
 * Local storage configuration.
 */
public class LocalStorageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String basePath;
    private final String baseUrl;
    private final String pathPrefix;

    public LocalStorageConfig(String basePath, String baseUrl, String pathPrefix) {
        this.basePath = basePath;
        this.baseUrl = baseUrl;
        this.pathPrefix = pathPrefix;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }
}
