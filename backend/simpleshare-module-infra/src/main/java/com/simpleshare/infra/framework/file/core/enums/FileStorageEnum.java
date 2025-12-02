package com.simpleshare.infra.framework.file.core.enums;

import com.simpleshare.infra.framework.file.core.client.FileClient;
import com.simpleshare.infra.framework.file.core.client.FileClientConfig;
import com.simpleshare.infra.framework.file.core.client.local.LocalFileClient;
import com.simpleshare.infra.framework.file.core.client.local.LocalFileClientConfig;
import com.simpleshare.infra.framework.file.core.client.s3.S3FileClient;
import com.simpleshare.infra.framework.file.core.client.s3.S3FileClientConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件存储器枚举
 *
 * @author simple
 */
@AllArgsConstructor
@Getter
public enum FileStorageEnum {

    LOCAL(1, LocalFileClientConfig.class, LocalFileClient.class),

    S3(10, S3FileClientConfig.class, S3FileClient.class),
    ;

    /**
     * 存储器
     */
    private final Integer storage;

    /**
     * 配置类
     */
    private final Class<? extends FileClientConfig> configClass;
    /**
     * 客户端类
     */
    private final Class<? extends FileClient> clientClass;

    public static FileStorageEnum getByStorage(Integer storage) {
        for (FileStorageEnum storageEnum : values()) {
            if (storageEnum.getStorage().equals(storage)) {
                return storageEnum;
            }
        }
        return null;
    }

}