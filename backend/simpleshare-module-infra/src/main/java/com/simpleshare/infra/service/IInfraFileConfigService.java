package com.simpleshare.infra.service;

import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.infra.domain.InfraFileConfig;

import java.util.List;

/**
 * 文件配置Service接口
 *
 * @author SimpleShare
 */
public interface IInfraFileConfigService {

    /**
     * 查询文件配置
     *
     * @param id 文件配置主键
     * @return 文件配置
     */
    InfraFileConfig selectInfraFileConfigById(Long id);

    /**
     * 查询文件配置列表
     *
     * @param infraFileConfig 文件配置
     * @return 文件配置集合
     */
    List<InfraFileConfig> selectInfraFileConfigList(InfraFileConfig infraFileConfig);

    /**
     * 分页查询文件配置列表
     *
     * @param infraFileConfig 文件配置
     * @return 分页数据
     */
    TableDataInfo selectInfraFileConfigListPage(InfraFileConfig infraFileConfig);

    /**
     * 新增文件配置
     *
     * @param infraFileConfig 文件配置
     * @return 结果
     */
    int insertInfraFileConfig(InfraFileConfig infraFileConfig);

    /**
     * 修改文件配置
     *
     * @param infraFileConfig 文件配置
     * @return 结果
     */
    int updateInfraFileConfig(InfraFileConfig infraFileConfig);

    /**
     * 批量删除文件配置
     *
     * @param ids 需要删除的文件配置主键集合
     * @return 结果
     */
    int deleteInfraFileConfigByIds(Long[] ids);

    /**
     * 删除文件配置信息
     *
     * @param id 文件配置主键
     * @return 结果
     */
    int deleteInfraFileConfigById(Long id);

    /**
     * 获取主配置
     *
     * @return 主配置
     */
    InfraFileConfig getMasterConfig();

    /**
     * 设置主配置
     *
     * @param id 配置ID
     * @return 结果
     */
    int setMasterConfig(Long id);

    /**
     * 测试配置连接
     *
     * @param id 配置ID
     * @return 测试结果
     */
    boolean testConfig(Long id);

    /**
     * 确保存在主配置，如不存在则创建默认本地配置
     *
     * @return 主配置
     */
    InfraFileConfig ensureMasterConfig();
}
