package com.simpleshare.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleshare.infra.domain.InfraFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件信息Mapper接口
 *
 * @author SimpleShare
 */
@Mapper
public interface InfraFileMapper extends BaseMapper<InfraFile> {

    /**
     * 查询文件信息列表
     *
     * @param infraFile 文件信息
     * @return 文件信息集合
     */
    List<InfraFile> selectInfraFileList(InfraFile infraFile);

    /**
     * 根据路径删除文件记录
     *
     * @param path 文件路径
     * @return 影响行数
     */
    int deleteByPath(@Param("path") String path);

    /**
     * 根据配置ID查询文件列表
     *
     * @param configId 配置ID
     * @return 文件列表
     */
    List<InfraFile> selectByConfigId(@Param("configId") Long configId);

    /**
     * 统计文件总大小
     *
     * @param configId 配置ID
     * @return 总大小（字节）
     */
    Long sumFileSizeByConfigId(@Param("configId") Long configId);
}