package com.simpleshare.framework.storage.client;

import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageException;
import com.simpleshare.framework.storage.StorageType;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.storage.config.LocalStorageConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local filesystem storage client.
 */
public class LocalStorageClient implements StorageClient {

    private final LocalStorageConfig config;

    public LocalStorageClient(LocalStorageConfig config) {
        this.config = config;
    }

    @Override
    public StorageUploadResult upload(byte[] content, String relativePath, String contentType) {
        try {
            String objectPath = buildObjectPath(relativePath);
            Path root = Paths.get(config.getBasePath());
            Path target = root.resolve(objectPath.replace("/", File.separator));
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            String url = buildUrl(objectPath);
            return new StorageUploadResult(objectPath, url, StorageType.LOCAL);
        } catch (IOException ex) {
            throw new StorageException("Failed to store file locally", ex);
        }
    }

    @Override
    public void delete(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        try {
            Path root = Paths.get(config.getBasePath());
            Path target = root.resolve(path.replace("/", File.separator));
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new StorageException("Failed to delete local file: " + path, ex);
        }
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
        String baseUrl = config.getBaseUrl();
        if (StringUtils.isEmpty(baseUrl)) {
            return objectPath;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBase + "/" + objectPath.replace("\\", "/");
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
}
