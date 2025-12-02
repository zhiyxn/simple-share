package com.simpleshare.infra.service;

import com.simpleshare.common.core.page.TableDataInfo;
import com.simpleshare.infra.domain.InfraFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件信息Service接口
 *
 * @author SimpleShare
 */
public interface IInfraFileService {

    /**
     * 查询文件信息
     *
     * @param id 文件信息主键
     * @return 文件信息
     */
    InfraFile selectInfraFileById(Long id);

    /**
     * 查询文件信息列表
     *
     * @param infraFile 文件信息
     * @return 文件信息集合
     */
    List<InfraFile> selectInfraFileList(InfraFile infraFile);

    /**
     * 分页查询文件信息列表
     *
     * @param infraFile 文件信息
     * @return 分页数据
     */
    TableDataInfo selectInfraFileListPage(InfraFile infraFile);

    /**
     * 新增文件信息
     *
     * @param infraFile 文件信息
     * @return 结果
     */
    int insertInfraFile(InfraFile infraFile);

    /**
     * 修改文件信息
     *
     * @param infraFile 文件信息
     * @return 结果
     */
    int updateInfraFile(InfraFile infraFile);

    /**
     * 批量删除文件信息
     *
     * @param ids 需要删除的文件信息主键集合
     * @return 结果
     */
    int deleteInfraFileByIds(Long[] ids);

    /**
     * 删除文件信息信息
     *
     * @param id 文件信息主键
     * @return 结果
     */
    int deleteInfraFileById(Long id);

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件信息
     */
    InfraFile uploadFile(MultipartFile file);

    /**
     * 根据路径删除文件
     *
     * @param path 文件路径
     * @return 结果
     */
    int deleteFileByPath(String path);

    /**
     * 获取文件存储统计信息
     *
     * @return 统计信息
     */
    Object getFileStatistics();

    /**
     * 上传文件（底层方法）
     *
     * @param content 文件内容
     * @param path 文件路径
     * @param type 文件类型
     * @return 文件访问URL
     */
    String uploadFileContent(byte[] content, String path, String type);

    /**
     * 删除文件（底层方法）
     *
     * @param path 文件路径
     */
    void deleteFileContent(String path);

    /**
     * 获取文件内容
     *
     * @param path 文件路径
     * @return 文件内容
     */
    byte[] getFileContent(String path);

    /**
     * 获取预签名URL
     *
     * @param path 文件路径
     * @return 预签名URL
     */
    String getPresignedUrl(String path);

    /**
     * 上传单个文件（简化方法）
     *
     * @param file 文件
     * @return 文件URL
     */
    String uploadSingleFile(MultipartFile file);

    /**
     * 批量上传文件
     *
     * @param files 文件数组
     * @return 文件URL列表
     */
    List<String> uploadMultipleFiles(MultipartFile[] files);

    /**
     * 根据URL删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    boolean deleteFileByUrl(String fileUrl);

}