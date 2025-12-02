package com.simpleshare.framework.storage.client;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageException;
import com.simpleshare.framework.storage.StorageType;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.storage.config.CloudStorageConfig;

import java.io.ByteArrayInputStream;

/**
 * Qiniu cloud storage client implementation.
 */
public class QiniuStorageClient implements StorageClient {

    private final CloudStorageConfig config;
    private final UploadManager uploadManager;
    private final BucketManager bucketManager;
    private final Auth auth;
    private final String bucket;
    private final String domain;

    public QiniuStorageClient(CloudStorageConfig config) {
        if (config == null) {
            throw new StorageException("Qiniu storage configuration is required");
        }
        this.config = config;
        this.bucket = config.getBucket();
        if (StringUtils.isEmpty(bucket)) {
            throw new StorageException("Qiniu bucket must be configured");
        }
        this.auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        Configuration configuration = new Configuration(resolveRegion(config.getRegion()));
        this.uploadManager = new UploadManager(configuration);
        this.bucketManager = new BucketManager(auth, configuration);
        this.domain = resolveDomain(config);
    }

    @Override
    public StorageUploadResult upload(byte[] content, String relativePath, String contentType) {
        try {
            String objectKey = buildObjectKey(relativePath);
            String token = auth.uploadToken(bucket);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
            Response response = uploadManager.put(inputStream, objectKey, token, null, contentType);
            if (!response.isOK()) {
                throw new StorageException("Failed to upload to Qiniu: " + response.bodyString());
            }
            String url = buildUrl(objectKey);
            return new StorageUploadResult(objectKey, url, StorageType.QINIU);
        } catch (QiniuException ex) {
            throw new StorageException("Failed to upload to Qiniu", ex);
        }
    }

    @Override
    public void delete(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        try {
            bucketManager.delete(bucket, path);
        } catch (QiniuException ex) {
            // 612: no such file
            if (ex.code() != 612) {
                throw new StorageException("Failed to delete Qiniu file: " + path, ex);
            }
        }
    }

    private Region resolveRegion(String region) {
        if (StringUtils.isEmpty(region)) {
            return Region.autoRegion();
        }
        String normalized = region.trim().toLowerCase();
        switch (normalized) {
            case "z0":
            case "cn-east-1":
            case "east":
                return Region.region0();
            case "z1":
            case "cn-north-1":
            case "north":
                return Region.region1();
            case "z2":
            case "cn-south-1":
            case "south":
                return Region.region2();
            case "na0":
            case "us-north-1":
            case "north-america":
                return Region.regionNa0();
            case "as0":
            case "ap-southeast-1":
            case "southeast-asia":
                return Region.regionAs0();
            default:
                return Region.autoRegion();
        }
    }

    private String buildObjectKey(String relativePath) {
        String sanitized = sanitize(relativePath);
        String prefix = sanitize(config.getPathPrefix());
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + "/" + sanitized;
        }
        return sanitized;
    }

    private String resolveDomain(CloudStorageConfig config) {
        if (StringUtils.isNotEmpty(config.getDomain())) {
            return trimTrailingSlash(config.getDomain());
        }
        if (StringUtils.isEmpty(config.getEndpoint())) {
            throw new StorageException("Please configure Qiniu domain or endpoint");
        }
        String endpoint = config.getEndpoint().trim();
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            endpoint = "https://" + endpoint;
        }
        return trimTrailingSlash(endpoint);
    }

    private String buildUrl(String objectKey) {
        return domain + "/" + objectKey.replace("\\", "/");
    }

    private String sanitize(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        String cleaned = value.replace("\\", "/");
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    private String trimTrailingSlash(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
