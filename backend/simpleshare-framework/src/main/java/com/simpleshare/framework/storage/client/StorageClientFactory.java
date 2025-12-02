package com.simpleshare.framework.storage.client;

import com.simpleshare.framework.storage.StorageType;
import com.simpleshare.framework.storage.config.StorageProfile;
import org.springframework.stereotype.Component;

/**
 * Factory for creating storage clients from profile configuration.
 */
@Component
public class StorageClientFactory {

    public StorageClient create(StorageProfile profile) {
        StorageType type = profile.getType();
        if (type == StorageType.LOCAL) {
            return new LocalStorageClient(profile.getLocal());
        }
        if (type == StorageType.ALIYUN) {
            return new S3StorageClient(profile.getCloud(), type);
        }
        if (type == StorageType.QINIU) {
            return new QiniuStorageClient(profile.getCloud());
        }
        throw new IllegalArgumentException("Unsupported storage type: " + type);
    }
}
