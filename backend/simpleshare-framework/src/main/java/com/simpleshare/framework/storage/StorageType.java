package com.simpleshare.framework.storage;

/**
 * Supported storage providers.
 */
public enum StorageType {

    LOCAL("local"),
    ALIYUN("aliyun"),
    QINIU("qiniu");

    private final String code;

    StorageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static StorageType from(String value) {
        if (value == null) {
            return LOCAL;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "local":
                return LOCAL;
            case "aliyun":
            case "oss":
                return ALIYUN;
            case "qiniu":
                return QINIU;
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + value);
        }
    }
}
