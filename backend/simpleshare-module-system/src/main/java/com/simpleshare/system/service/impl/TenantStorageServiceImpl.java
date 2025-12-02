package com.simpleshare.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageException;
import com.simpleshare.framework.storage.StorageType;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.framework.storage.client.StorageClient;
import com.simpleshare.framework.storage.client.StorageClientFactory;
import com.simpleshare.framework.storage.config.CloudStorageConfig;
import com.simpleshare.framework.storage.config.LocalStorageConfig;
import com.simpleshare.framework.storage.config.StorageProfile;
import com.simpleshare.framework.tenant.TenantContextHolder;
import com.simpleshare.system.dto.TenantSettingsRequest;
import com.simpleshare.system.service.ISysTenantConfigService;
import com.simpleshare.system.service.ITenantStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TenantStorageServiceImpl implements ITenantStorageService {

    private static final String KEY_SETTINGS_STORAGE = "settings_storage";
    private static final String KEY_MAX_FILE_SIZE = "max_file_size";
    private static final String KEY_ALLOWED_FILE_TYPES = "allowed_file_types";

    private static final DateTimeFormatter DATE_FOLDER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final Map<String, String> EXTENSION_CATEGORY;

    static {
        Map<String, String> mapping = new HashMap<>();
        register(mapping, "image", "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico");
        register(mapping, "document", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "rtf");
        register(mapping, "audio", "mp3", "wav", "aac", "ogg", "flac", "m4a");
        register(mapping, "video", "mp4", "mov", "avi", "wmv", "mkv", "flv", "webm");
        EXTENSION_CATEGORY = Collections.unmodifiableMap(mapping);
    }

    private final ISysTenantConfigService tenantConfigService;
    private final ObjectMapper objectMapper;
    private final StorageClientFactory storageClientFactory;

    public TenantStorageServiceImpl(ISysTenantConfigService tenantConfigService,
                                    ObjectMapper objectMapper,
                                    StorageClientFactory storageClientFactory) {
        this.tenantConfigService = tenantConfigService;
        this.objectMapper = objectMapper;
        this.storageClientFactory = storageClientFactory;
    }

    @Override
    public StorageUploadResult uploadFile(Long tenantId, MultipartFile file, String directory) {
        Long effectiveTenantId = resolveTenantId(tenantId);
        StorageProfile profile = getStorageProfile(effectiveTenantId);
        validateFile(profile, file);

        StorageClient client = storageClientFactory.create(profile);
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new StorageException("读取上传文件失败", e);
        }

        String relativePath = buildRelativePath(directory, file.getOriginalFilename());
        String contentType = file.getContentType();
        return client.upload(content, relativePath, contentType);
    }

    @Override
    public StorageProfile getStorageProfile(Long tenantId) {
        Long effectiveTenantId = resolveTenantId(tenantId);
        Map<String, String> configMap = tenantConfigService.selectConfigMap(effectiveTenantId);
        TenantSettingsRequest.StorageSettings storageSettings = buildStorageSettings(configMap);
        return convertToProfile(storageSettings);
    }

    private TenantSettingsRequest.StorageSettings buildStorageSettings(Map<String, String> configMap) {
        TenantSettingsRequest.StorageSettings storage = readStorageSettings(configMap.get(KEY_SETTINGS_STORAGE));
        if (StringUtils.isEmpty(storage.getType())) {
            storage.setType("local");
        }
        Integer maxSize = storage.getMaxSize();
        if (maxSize == null || maxSize <= 0) {
            storage.setMaxSize(parseInteger(configMap.get(KEY_MAX_FILE_SIZE), 10));
        }
        if (storage.getAllowedTypes() == null || storage.getAllowedTypes().length == 0) {
            String allowed = configMap.get(KEY_ALLOWED_FILE_TYPES);
            if (StringUtils.isNotEmpty(allowed)) {
                storage.setAllowedTypes(splitCsv(allowed));
            }
        }
        storage.ensureDefaults();
        return storage;
    }

    private StorageProfile convertToProfile(TenantSettingsRequest.StorageSettings storage) {
        StorageType type = StorageType.from(storage.getType());
        TenantSettingsRequest.StorageSettings.LocalStorageConfig local = storage.getLocal();
        LocalStorageConfig localConfig = new LocalStorageConfig(
                local.getBasePath(),
                normalizeBaseUrl(local.getBaseUrl()),
                sanitizePath(local.getPathPrefix())
        );

        CloudStorageConfig cloudConfig = null;
        if (!StorageType.LOCAL.equals(type)) {
            TenantSettingsRequest.StorageSettings.CloudStorageConfig source = storage.resolveCloudConfig(storage.getType());
            if (source == null) {
                throw new StorageException("云存储配置缺失");
            }
            validateCloudConfig(type, source);
            cloudConfig = new CloudStorageConfig(
                    source.getEndpoint(),
                    source.getBucket(),
                    source.getAccessKey(),
                    source.getSecretKey(),
                    source.getRegion(),
                    source.getDomain(),
                    sanitizePath(source.getPathPrefix())
            );
        }

        List<String> allowed = storage.getAllowedTypes() == null
                ? Collections.emptyList()
                : Arrays.stream(storage.getAllowedTypes())
                .filter(StringUtils::isNotEmpty)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());

        int maxSize = storage.getMaxSize() != null ? storage.getMaxSize() : 10;
        return new StorageProfile(type, localConfig, cloudConfig, maxSize, allowed);
    }

    private void validateFile(StorageProfile profile, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("上传文件不能为空");
        }
        long maxBytes = profile.getMaxSizeMb() * 1024L * 1024L;
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new StorageException("文件大小超过限制：" + profile.getMaxSizeMb() + "MB");
        }
        List<String> allowed = profile.getAllowedTypes();
        if (allowed != null && !allowed.isEmpty()) {
            String category = resolveCategory(file.getOriginalFilename(), file.getContentType());
            if (!allowed.contains(category)) {
                throw new StorageException("当前存储不允许此文件类型：" + category);
            }
        }
    }

    private String resolveCategory(String filename, String contentType) {
        String extension = extractExtension(filename);
        if (extension != null) {
            String category = EXTENSION_CATEGORY.get(extension);
            if (category != null) {
                return category;
            }
        }
        if (StringUtils.isNotEmpty(contentType)) {
            String lower = contentType.toLowerCase(Locale.ROOT);
            if (lower.startsWith("image/")) {
                return "image";
            }
            if (lower.startsWith("video/")) {
                return "video";
            }
            if (lower.startsWith("audio/")) {
                return "audio";
            }
            if (lower.contains("pdf") || lower.startsWith("text/")) {
                return "document";
            }
        }
        return "other";
    }

    private String buildRelativePath(String directory, String originalFilename) {
        String sanitizedDir = sanitizePath(directory);
        String extension = extractExtension(originalFilename);
        if (extension == null) {
            extension = "bin";
        }
        String dateSegment = DATE_FOLDER.format(LocalDate.now());
        String unique = UUID.randomUUID().toString().replace("-", "");
        String filename = unique + "." + extension;
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(sanitizedDir)) {
            builder.append(sanitizedDir).append('/');
        }
        builder.append(dateSegment).append('/').append(filename);
        return builder.toString();
    }

    private static void register(Map<String, String> mapping, String category, String... extensions) {
        for (String ext : extensions) {
            mapping.put(ext, category);
        }
    }

    private String extractExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return null;
        }
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return null;
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String sanitizePath(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        String sanitized = value.replace("\\", "/").trim();
        sanitized = sanitized.replaceAll("/+", "/");
        while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (StringUtils.isEmpty(baseUrl)) {
            return "/upload";
        }
        return baseUrl.trim();
    }

    private void validateCloudConfig(StorageType type, TenantSettingsRequest.StorageSettings.CloudStorageConfig source) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isEmpty(source.getEndpoint())) {
            missing.add("endpoint");
        }
        if (StringUtils.isEmpty(source.getBucket())) {
            missing.add("bucket");
        }
        if (StringUtils.isEmpty(source.getAccessKey())) {
            missing.add("accessKey");
        }
        if (StringUtils.isEmpty(source.getSecretKey())) {
            missing.add("secretKey");
        }
        if (!missing.isEmpty()) {
            throw new StorageException("" + type.getCode() + " 存储配置缺失: " + String.join(",", missing));
        }
    }

    private TenantSettingsRequest.StorageSettings readStorageSettings(String json) {
        if (StringUtils.isEmpty(json)) {
            return new TenantSettingsRequest.StorageSettings();
        }
        try {
            return objectMapper.readValue(json, TenantSettingsRequest.StorageSettings.class);
        } catch (Exception e) {
            return new TenantSettingsRequest.StorageSettings();
        }
    }

    private String[] splitCsv(String csv) {
        if (StringUtils.isEmpty(csv)) {
            return new String[0];
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);
    }

    private int parseInteger(String value, int fallback) {
        if (StringUtils.isEmpty(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Long resolveTenantId(Long tenantId) {
        if (tenantId != null) {
            return tenantId;
        }
        String contextId = TenantContextHolder.getTenantId();
        if (StringUtils.isNotEmpty(contextId)) {
            try {
                return Long.valueOf(contextId);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1L;
    }
}
