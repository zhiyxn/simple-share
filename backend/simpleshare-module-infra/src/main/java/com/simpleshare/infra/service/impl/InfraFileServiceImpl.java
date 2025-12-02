package com.simpleshare.infra.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.utils.PageUtils;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.framework.storage.StorageUploadResult;
import com.simpleshare.infra.domain.InfraFile;
import com.simpleshare.infra.domain.InfraFileConfig;
import com.simpleshare.infra.mapper.InfraFileMapper;
import com.simpleshare.infra.framework.file.core.client.FileClient;
import com.simpleshare.infra.framework.file.core.client.FileClientFactory;
import com.simpleshare.infra.framework.file.core.client.FileClientConfig;
import com.simpleshare.infra.framework.file.core.client.local.LocalFileClientConfig;

import com.simpleshare.infra.framework.file.core.client.s3.S3FileClientConfig;
import com.simpleshare.infra.framework.file.core.enums.FileStorageEnum;
import com.simpleshare.infra.service.IInfraFileConfigService;
import com.simpleshare.infra.service.IInfraFileService;
import com.simpleshare.system.service.ITenantStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 文件信息Service业务层处理
 *
 * @author SimpleShare
 */
@Service
public class InfraFileServiceImpl implements IInfraFileService {

    @Autowired
    private InfraFileMapper infraFileMapper;

    @Autowired
    private IInfraFileConfigService infraFileConfigService;

    @Autowired
    private ITenantStorageService tenantStorageService;

    @Autowired
    private FileClientFactory fileClientFactory;

    /**
     * 查询文件信息
     *
     * @param id 文件信息主键
     * @return 文件信息
     */
    @Override
    public InfraFile selectInfraFileById(Long id) {
        return infraFileMapper.selectById(id);
    }

    /**
     * 查询文件信息列表
     *
     * @param infraFile 文件信息
     * @return 文件信息
     */
    @Override
    public List<InfraFile> selectInfraFileList(InfraFile infraFile) {
        return infraFileMapper.selectInfraFileList(infraFile);
    }

    /**
     * 分页查询文件信息列表
     *
     * @param infraFile 文件信息
     * @return 分页数据
     */
    @Override
    public TableDataInfo selectInfraFileListPage(InfraFile infraFile) {
        PageUtils.startPage();
        List<InfraFile> list = selectInfraFileList(infraFile);
        return PageUtils.getDataTable(list);
    }

    /**
     * 新增文件信息
     *
     * @param infraFile 文件信息
     * @return 结果
     */
    @Override
    public int insertInfraFile(InfraFile infraFile) {
        infraFile.setCreateBy(SecurityUtils.getUserId());
        return infraFileMapper.insert(infraFile);
    }

    /**
     * 修改文件信息
     *
     * @param infraFile 文件信息
     * @return 结果
     */
    @Override
    public int updateInfraFile(InfraFile infraFile) {
        infraFile.setUpdateBy(SecurityUtils.getUserId());
        return infraFileMapper.updateById(infraFile);
    }

    /**
     * 批量删除文件信息
     *
     * @param ids 需要删除的文件信息主键
     * @return 结果
     */
    @Override
    public int deleteInfraFileByIds(Long[] ids) {
        return infraFileMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除文件信息信息
     *
     * @param id 文件信息主键
     * @return 结果
     */
    @Override
    public int deleteInfraFileById(Long id) {
        return infraFileMapper.deleteById(id);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件信息
     */
    @Override
    public InfraFile uploadFile(MultipartFile file) {
        try {
            // 确保存在主配置（若不存在则自动创建默认配置）
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();

            // 生成文件路径
            String fileName = file.getOriginalFilename();
            // 获取文件扩展名
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }
            // 生成年月日格式的目录结构
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String dateDir = dateFormat.format(new Date());
            // 生成随机ID作为文件名
            String randomId = UUID.randomUUID().toString().replaceAll("-", "");
            String path = "upload/" + dateDir + "/" + randomId + extension;
            
            // 使用文件客户端上传
            FileClient fileClient = getFileClient(masterConfig);
            String url = fileClient.upload(file.getBytes(), path, file.getContentType());

            // 创建文件记录
            InfraFile infraFile = new InfraFile();
            infraFile.setConfigId(masterConfig.getId());
            infraFile.setName(fileName);
            infraFile.setPath(path);
            infraFile.setUrl(url);
            infraFile.setType(getFileType(file.getContentType()));
            infraFile.setSize((int) file.getSize());
            infraFile.setCreateBy(SecurityUtils.getUserId());

            insertInfraFile(infraFile);
            return infraFile;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据路径删除文件
     *
     * @param path 文件路径
     * @return 结果
     */
    @Override
    public int deleteFileByPath(String path) {
        try {
            // 删除物理文件
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();
            FileClient fileClient = getFileClient(masterConfig);
            fileClient.delete(path);
        } catch (Exception e) {
            // 记录日志但不影响数据库删除
            System.err.println("删除物理文件失败: " + e.getMessage());
        }
        // 删除数据库记录
        return infraFileMapper.deleteByPath(path);
    }

    /**
     * 获取文件存储统计信息
     *
     * @return 统计信息
     */
    @Override
    public Object getFileStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 使用自定义查询方法避免MyBatis-Plus的字段映射问题
        InfraFile queryParam = new InfraFile();
        List<InfraFile> allFiles = infraFileMapper.selectInfraFileList(queryParam);
        
        // 总文件数
        long totalCount = allFiles.size();
        statistics.put("totalCount", totalCount);
        
        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        for (InfraFile file : allFiles) {
            String type = StringUtils.isNotEmpty(file.getType()) ? file.getType() : "unknown";
            typeStats.put(type, typeStats.getOrDefault(type, 0L) + 1);
        }
        statistics.put("typeStats", typeStats);
        
        // 总存储大小
        long totalSize = allFiles.stream().mapToLong(f -> f.getSize() != null ? f.getSize() : 0).sum();
        statistics.put("totalSize", totalSize);
        statistics.put("totalSizeFormatted", formatFileSize(totalSize));
        
        return statistics;
    }

    /**
     * 获取文件客户端
     *
     * @param config 文件配置
     * @return 文件客户端
     */
    private FileClient getFileClient(InfraFileConfig config) {
        FileClientConfig clientConfig = createFileClientConfig(config);
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(config.getStorage());
        fileClientFactory.createOrUpdateFileClient(config.getId(), storageEnum.getStorage(), clientConfig);
        return fileClientFactory.getFileClient(config.getId());
    }

    /**
     * 创建文件客户端配置
     *
     * @param config 文件配置
     * @return 文件客户端配置
     */
    private FileClientConfig createFileClientConfig(InfraFileConfig config) {
        Integer storageType = config.getStorage();
        Map<String, String> configMap = parseConfigJson(config.getConfig());
        
        if (storageType == 1) { // 本地存储
            LocalFileClientConfig localConfig = new LocalFileClientConfig();
            localConfig.setBasePath(configMap.get("basePath"));
            localConfig.setDomain(configMap.get("domain"));
            return localConfig;
        } else if (storageType >= 10) { // S3兼容存储
            S3FileClientConfig s3Config = new S3FileClientConfig();
            s3Config.setAccessKey(configMap.get("accessKey"));
            s3Config.setAccessSecret(configMap.get("secretKey"));
            s3Config.setBucket(configMap.get("bucket"));
            
            // 根据存储类型设置不同的配置
            if (storageType == 10) { // 七牛云
                s3Config.setDomain(configMap.get("domain"));
                s3Config.setEndpoint(S3FileClientConfig.ENDPOINT_QINIU);
            } else if (storageType == 11) { // 阿里云
                s3Config.setEndpoint(configMap.get("endpoint"));
            } else if (storageType == 12) { // MinIO
                s3Config.setEndpoint(configMap.get("endpoint"));
            } else {
                // 默认七牛云配置
                s3Config.setDomain(configMap.get("domain"));
                s3Config.setEndpoint(S3FileClientConfig.ENDPOINT_QINIU);
            }
            return s3Config;
        } else {
            throw new IllegalArgumentException("不支持的存储类型: " + storageType);
        }
    }

    /**
     * 解析配置 JSON 字符串
     *
     * @param configJson JSON 字符串
     * @return 配置 Map
     */
    private Map<String, String> parseConfigJson(String configJson) {
        try {
            if (StringUtils.isEmpty(configJson)) {
                return new HashMap<>();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(configJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("解析配置 JSON 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从URL中提取文件路径
     *
     * @param fileUrl 文件URL
     * @return 文件路径
     */
    private String extractPathFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件URL不能为空");
        }
        
        // 如果是完整URL，提取路径部分
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            try {
                java.net.URL url = new java.net.URL(fileUrl);
                String path = url.getPath();
                // 移除开头的斜杠
                return path.startsWith("/") ? path.substring(1) : path;
            } catch (Exception e) {
                throw new IllegalArgumentException("无效的文件URL: " + fileUrl, e);
            }
        }
        
        // 如果已经是路径，直接返回
        return fileUrl;
    }

    /**
     * 上传文件内容（底层方法）
     *
     * @param content 文件内容
     * @param path    文件路径
     * @param type    文件类型
     * @return 文件URL
     */
    @Override
    public String uploadFileContent(byte[] content, String path, String type) {
        try {
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();
            FileClient fileClient = getFileClient(masterConfig);
            return fileClient.upload(content, path, type);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件（底层方法）
     *
     * @param path 文件路径
     */
    @Override
    public void deleteFileContent(String path) {
        try {
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();
            FileClient fileClient = getFileClient(masterConfig);
            fileClient.delete(path);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件内容
     *
     * @param path 文件路径
     * @return 文件内容
     */
    @Override
    public byte[] getFileContent(String path) {
        try {
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();
            FileClient fileClient = getFileClient(masterConfig);
            return fileClient.getContent(path);
        } catch (Exception e) {
            throw new RuntimeException("获取文件内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取预签名URL
     *
     * @param path 文件路径
     * @return 预签名URL
     */
    @Override
    public String getPresignedUrl(String path) {
        try {
            InfraFileConfig masterConfig = infraFileConfigService.ensureMasterConfig();
            FileClient fileClient = getFileClient(masterConfig);
            return fileClient.getPresignedObjectUrl(path);
        } catch (Exception e) {
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传单个文件（简化方法）
     *
     * @param file 文件
     * @return 文件URL
     */
    @Override
    public String uploadSingleFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String path = "upload/" + System.currentTimeMillis() + "/" + fileName;
            return uploadFileContent(file.getBytes(), path, file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @return 文件URL列表
     */
    @Override
    public List<String> uploadMultipleFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadSingleFile(file));
        }
        return urls;
    }

    /**
     * 根据URL删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @Override
    public boolean deleteFileByUrl(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String path = extractPathFromUrl(fileUrl);
            deleteFileContent(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }



    /**
     * 获取文件类型
     */
    private String getFileType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return "unknown";
        }
        
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else if (contentType.contains("pdf") || contentType.startsWith("text/") || 
                   contentType.contains("document") || contentType.contains("spreadsheet") ||
                   contentType.contains("presentation")) {
            return "document";
        } else {
            return "other";
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
