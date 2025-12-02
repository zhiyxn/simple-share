package com.simpleshare.infra.service.impl;

import com.simpleshare.common.config.SimpleShareConfig;
import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.common.utils.PageUtils;
import com.simpleshare.common.utils.SecurityUtils;
import com.simpleshare.common.utils.StringUtils;
import com.simpleshare.infra.domain.InfraFileConfig;
import com.simpleshare.infra.mapper.InfraFileConfigMapper;
import com.simpleshare.infra.service.IInfraFileConfigService;
import com.simpleshare.infra.framework.file.core.client.FileClient;
import com.simpleshare.infra.framework.file.core.client.FileClientFactory;
import com.simpleshare.infra.framework.file.core.client.FileClientConfig;
import com.simpleshare.infra.framework.file.core.client.local.LocalFileClientConfig;
import com.simpleshare.infra.framework.file.core.client.s3.S3FileClientConfig;
import com.simpleshare.infra.framework.file.core.enums.FileStorageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 文件配置Service业务层处理
 *
 * @author SimpleShare
 */
@Service
public class InfraFileConfigServiceImpl implements IInfraFileConfigService {

    @Autowired
    private InfraFileConfigMapper infraFileConfigMapper;

    @Autowired
    private FileClientFactory fileClientFactory;

    /**
     * 查询文件配置
     *
     * @param id 文件配置主键
     * @return 文件配置
     */
    @Override
    public InfraFileConfig selectInfraFileConfigById(Long id) {
        return infraFileConfigMapper.selectById(id);
    }

    /**
     * 查询文件配置列表
     *
     * @param infraFileConfig 文件配置
     * @return 文件配置
     */
    @Override
    public List<InfraFileConfig> selectInfraFileConfigList(InfraFileConfig infraFileConfig) {
        return infraFileConfigMapper.selectInfraFileConfigList(infraFileConfig);
    }

    /**
     * 分页查询文件配置列表
     *
     * @param infraFileConfig 文件配置
     * @return 分页数据
     */
    @Override
    public TableDataInfo selectInfraFileConfigListPage(InfraFileConfig infraFileConfig) {
        PageUtils.startPage();
        List<InfraFileConfig> list = selectInfraFileConfigList(infraFileConfig);
        return PageUtils.getDataTable(list);
    }

    /**
     * 新增文件配置
     *
     * @param infraFileConfig 文件配置
     * @return 结果
     */
    @Override
    @Transactional
    public int insertInfraFileConfig(InfraFileConfig infraFileConfig) {
        infraFileConfig.setCreateBy(SecurityUtils.getUserId());
        
        // 如果设置为主配置，先清除其他主配置
        if (Boolean.TRUE.equals(infraFileConfig.getMaster())) {
            infraFileConfigMapper.clearAllMasterStatus();
        }
        
        return infraFileConfigMapper.insert(infraFileConfig);
    }

    /**
     * 修改文件配置
     *
     * @param infraFileConfig 文件配置
     * @return 结果
     */
    @Override
    @Transactional
    public int updateInfraFileConfig(InfraFileConfig infraFileConfig) {
        infraFileConfig.setUpdateBy(SecurityUtils.getUserId());
        
        // 如果设置为主配置，先清除其他主配置
        if (Boolean.TRUE.equals(infraFileConfig.getMaster())) {
            infraFileConfigMapper.clearAllMasterStatus();
        }
        
        return infraFileConfigMapper.updateById(infraFileConfig);
    }

    /**
     * 批量删除文件配置
     *
     * @param ids 需要删除的文件配置主键
     * @return 结果
     */
    @Override
    public int deleteInfraFileConfigByIds(Long[] ids) {
        return infraFileConfigMapper.deleteBatchIds(Arrays.asList(ids));
    }

    /**
     * 删除文件配置信息
     *
     * @param id 文件配置主键
     * @return 结果
     */
    @Override
    public int deleteInfraFileConfigById(Long id) {
        return infraFileConfigMapper.deleteById(id);
    }

    /**
     * 获取主配置
     *
     * @return 主配置
     */
    @Override
    public InfraFileConfig getMasterConfig() {
        return infraFileConfigMapper.selectMasterConfig();
    }

    /**
     * 设置主配置
     *
     * @param id 配置ID
     * @return 结果
     */
    @Override
    @Transactional
    public int setMasterConfig(Long id) {
        // 先清除所有主配置
        infraFileConfigMapper.clearAllMasterStatus();
        // 设置新的主配置
        return infraFileConfigMapper.updateMasterStatus(id, true);
    }

    /**
     * 测试配置连接
     *
     * @param id 配置ID
     * @return 测试结果
     */
    @Override
    public boolean testConfig(Long id) {
        InfraFileConfig config = selectInfraFileConfigById(id);
        if (config == null) {
            return false;
        }

        try {
            // 创建临时文件客户端进行测试
            FileClientConfig clientConfig = createFileClientConfig(config);
            FileStorageEnum storageEnum = FileStorageEnum.getByStorage(config.getStorage());
            
            // 创建测试客户端
            fileClientFactory.createOrUpdateFileClient(
                -1L, // 使用临时ID
                storageEnum.getStorage(), 
                clientConfig
            );
            FileClient testClient = fileClientFactory.getFileClient(-1L);
            
            // 尝试上传一个测试文件
            String testPath = "test/connection-test.txt";
            String testContent = "Connection test";
            testClient.upload(testContent.getBytes(), testPath, "text/plain");
            
            // 尝试删除测试文件
            testClient.delete(testPath);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public InfraFileConfig ensureMasterConfig() {
        InfraFileConfig master = getMasterConfig();
        if (master != null) {
            return master;
        }

        InfraFileConfig config = new InfraFileConfig();
        config.setName("本地存储");
        config.setStorage(1); // 本地存储类型为1，对应FileStorageEnum.LOCAL
        config.setMaster(true);
        config.setRemark("系统自动创建的默认本地存储配置");
        config.setConfig(buildDefaultLocalConfigJson());

        Long operatorId = SecurityUtils.getUserIdSafely();
        if (operatorId != null) {
            config.setCreateBy(operatorId);
            config.setUpdateBy(operatorId);
        }

        infraFileConfigMapper.clearAllMasterStatus();
        infraFileConfigMapper.insert(config);
        return config;
    }

    private String buildDefaultLocalConfigJson() {
        String basePath = SimpleShareConfig.getUploadPath();
        if (StringUtils.isEmpty(basePath)) {
            basePath = System.getProperty("java.io.tmpdir");
        }
        String normalizedBasePath = basePath.replace("\\", "\\\\");
        return String.format("{\"basePath\":\"%s\",\"domain\":\"http://localhost:8080\"}", normalizedBasePath);
    }

    /**
     * 创建文件客户端配置
     *
     * @param config 文件配置
     * @return 文件客户端配置
     */
    private FileClientConfig createFileClientConfig(InfraFileConfig config) {
        FileStorageEnum storageEnum = FileStorageEnum.getByStorage(config.getStorage());
        Map<String, String> configMap = parseConfigJson(config.getConfig());
        
        switch (storageEnum) {
            case S3:
                S3FileClientConfig s3Config = new S3FileClientConfig();
                s3Config.setAccessKey(configMap.get("accessKey"));
                s3Config.setAccessSecret(configMap.get("secretKey"));
                s3Config.setBucket(configMap.get("bucket"));
                
                // 根据存储类型设置不同的配置
                Integer storageType = config.getStorage();
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
            case LOCAL:
                LocalFileClientConfig localConfig = new LocalFileClientConfig();
                localConfig.setBasePath(configMap.get("basePath"));
                localConfig.setDomain(configMap.get("domain"));
                return localConfig;
            default:
                throw new IllegalArgumentException("不支持的存储类型: " + config.getStorage());
        }
    }

    /**
     * 解析配置JSON
     *
     * @param configJson 配置JSON字符串
     * @return 配置Map
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
}
