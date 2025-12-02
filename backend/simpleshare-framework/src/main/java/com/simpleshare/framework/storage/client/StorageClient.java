package com.simpleshare.framework.storage.client;

import com.simpleshare.framework.storage.StorageException;
import com.simpleshare.framework.storage.StorageUploadResult;

/**
 * Generic storage client abstraction.
 */
public interface StorageClient {

    StorageUploadResult upload(byte[] content, String relativePath, String contentType) throws StorageException;

    void delete(String path) throws StorageException;
}
