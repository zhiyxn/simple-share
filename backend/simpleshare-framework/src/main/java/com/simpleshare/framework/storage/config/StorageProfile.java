package com.simpleshare.framework.storage.config;

import com.simpleshare.framework.storage.StorageType;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Unified storage profile derived from tenant configuration.
 */
public class StorageProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StorageType type;
    private final LocalStorageConfig local;
    private final CloudStorageConfig cloud;
    private final int maxSizeMb;
    private final List<String> allowedTypes;

    public StorageProfile(StorageType type,
                          LocalStorageConfig local,
                          CloudStorageConfig cloud,
                          int maxSizeMb,
                          List<String> allowedTypes) {
        this.type = type;
        this.local = local;
        this.cloud = cloud;
        this.maxSizeMb = maxSizeMb;
        this.allowedTypes = allowedTypes == null ? Collections.emptyList() : Collections.unmodifiableList(allowedTypes);
    }

    public StorageType getType() {
        return type;
    }

    public LocalStorageConfig getLocal() {
        return local;
    }

    public CloudStorageConfig getCloud() {
        return cloud;
    }

    public int getMaxSizeMb() {
        return maxSizeMb;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public boolean isLocal() {
        return StorageType.LOCAL.equals(type);
    }
}
