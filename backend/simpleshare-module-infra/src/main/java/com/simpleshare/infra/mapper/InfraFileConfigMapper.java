package com.simpleshare.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.infra.domain.InfraFileConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件配置Mapper接口
 *
 * @author SimpleShare
 */
@Mapper
public interface InfraFileConfigMapper extends BaseMapper<InfraFileConfig> {

    /**
     * 查询文件配置列表
     *
     * @param infraFileConfig 文件配置
     * @return 文件配置集合
     */
    List<InfraFileConfig> selectInfraFileConfigList(InfraFileConfig infraFileConfig);

    /**
     * 查询主配置
     *
     * @return 主配置
     */
    InfraFileConfig selectMasterConfig();

    /**
     * 更新主配置状态
     *
     * @param id 配置ID
     * @param master 是否为主配置
     * @return 影响行数
     */
    int updateMasterStatus(@Param("id") Long id, @Param("master") Boolean master);

    /**
     * 清除所有主配置状态
     *
     * @return 影响行数
     */
    int clearAllMasterStatus();
}