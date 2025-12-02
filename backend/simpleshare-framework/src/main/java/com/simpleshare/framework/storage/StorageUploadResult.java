package com.simpleshare.framework.storage;

import java.io.Serializable;

/**
 * Result of a storage upload operation.
 */
public class StorageUploadResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String path;
    private final String url;
    private final StorageType storageType;

    public StorageUploadResult(String path, String url, StorageType storageType) {
        this.path = path;
        this.url = url;
        this.storageType = storageType;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public StorageType getStorageType() {
        return storageType;
    }
}
