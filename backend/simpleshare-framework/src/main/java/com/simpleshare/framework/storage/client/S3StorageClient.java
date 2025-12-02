package com.simpleshare.framework.storage.client;

import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageException;
import com.simpleshare.framework.storage.StorageType;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.storage.config.CloudStorageConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * S3 compatible storage client backed by MinIO SDK.
 */
public class S3StorageClient implements StorageClient {

    private final CloudStorageConfig config;
    private final StorageType storageType;
    private final MinioClient client;
    private final String domain;

    public S3StorageClient(CloudStorageConfig config, StorageType storageType) {
        this.config = config;
        this.storageType = storageType;
        this.client = buildClient();
        this.domain = resolveDomain();
    }

    @Override
    public StorageUploadResult upload(byte[] content, String relativePath, String contentType) {
        try {
            String objectPath = buildObjectPath(relativePath);
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(objectPath)
                    .stream(new ByteArrayInputStream(content), content.length, -1);
            if (StringUtils.isNotEmpty(contentType)) {
                builder.contentType(contentType);
            }
            client.putObject(builder.build());
            String url = buildUrl(objectPath);
            return new StorageUploadResult(objectPath, url, storageType);
        } catch (Exception ex) {
            throw new StorageException("Failed to upload file to cloud storage", ex);
        }
    }

    @Override
    public void delete(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(path)
                    .build());
        } catch (Exception ex) {
            throw new StorageException("Failed to delete remote file: " + path, ex);
        }
    }

    private MinioClient buildClient() {
        if (StringUtils.isEmpty(config.getEndpoint())) {
            throw new StorageException("Cloud storage endpoint is required");
        }
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(normalizeEndpoint(config.getEndpoint()))
                .credentials(config.getAccessKey(), config.getSecretKey());
        if (StringUtils.isNotEmpty(config.getRegion())) {
            builder.region(config.getRegion());
        }
        return builder.build();
    }

    private String buildObjectPath(String relativePath) {
        String sanitized = sanitize(relativePath);
        String prefix = sanitize(config.getPathPrefix());
        if (StringUtils.isNotEmpty(prefix)) {
            return prefix + "/" + sanitized;
        }
        return sanitized;
    }

    private String buildUrl(String objectPath) {
        String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return base + "/" + objectPath.replace("\\", "/");
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "https://" + endpoint;
    }

    private String resolveDomain() {
        if (StringUtils.isNotEmpty(config.getDomain())) {
            return trimTrailingSlash(config.getDomain());
        }
        try {
            URI uri = new URI(normalizeEndpoint(config.getEndpoint()));
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            String host = uri.getHost();
            if (StringUtils.isEmpty(host)) {
                return normalizeEndpoint(config.getEndpoint());
            }
            return scheme + "://" + config.getBucket() + "." + host;
        } catch (URISyntaxException e) {
            throw new StorageException("Invalid storage endpoint: " + config.getEndpoint(), e);
        }
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
