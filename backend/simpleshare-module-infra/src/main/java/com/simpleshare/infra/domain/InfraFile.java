package com.simpleshare.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

/**
 * 文件信息对象 infra_file
 *
 * @author SimpleShare
 */
@TableName("infra_file")
public class InfraFile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文件编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置编号 */
    @TableField("config_id")
    private Long configId;

    /** 文件名 */
    @TableField("name")
    private String name;

    /** 文件路径 */
    @TableField("path")
    private String path;

    /** 文件 URL */
    @TableField("url")
    private String url;

    /** 文件类型 */
    @TableField("type")
    private String type;

    /** 文件大小 */
    @TableField("size")
    private Integer size;

    /** 删除标志（0代表存在 1代表删除） */
    @TableField("deleted")
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "InfraFile{" +
                "id=" + id +
                ", configId=" + configId +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", deleted=" + deleted +
                '}';
    }
}