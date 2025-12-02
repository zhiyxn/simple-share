package com.simpleshare.framework.storage.config;

import java.io.Serializable;

/**
 * Cloud storage configuration for S3 compatible providers.
 */
public class CloudStorageConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String endpoint;
    private final String bucket;
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String domain;
    private final String pathPrefix;

    public CloudStorageConfig(String endpoint, String bucket, String accessKey, String secretKey,
                              String region, String domain, String pathPrefix) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.domain = domain;
        this.pathPrefix = pathPrefix;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

    public String getDomain() {
        return domain;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }
}
