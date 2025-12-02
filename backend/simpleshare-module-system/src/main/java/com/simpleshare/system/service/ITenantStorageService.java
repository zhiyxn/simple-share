package com.simpleshare.system.service;

import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.storage.config.StorageProfile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tenant storage service abstraction.
 */
public interface ITenantStorageService {

    StorageUploadResult uploadFile(Long tenantId, MultipartFile file, String directory);

    StorageProfile getStorageProfile(Long tenantId);
}
