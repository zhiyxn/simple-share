package com.simpleshare.infra.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.simpleshare.common.core.domain.BaseEntity;

/**
 * 文件配置对象 infra_file_config
 *
 * @author SimpleShare
 */
@TableName("infra_file_config")
public class InfraFileConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置名 */
    @TableField("name")
    private String name;

    /** 存储器 */
    @TableField("storage")
    private Integer storage;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 是否为主配置 */
    @TableField("master")
    private Boolean master;

    /** 存储配置 */
    @TableField("config")
    private String config;

    /** 文件访问域名 - 非数据库字段，用于前端直接获取域名配置 */
    @TableField(exist = false)
    private String domain;

    /** 删除标志（0代表存在 1代表删除） */
    @TableField("deleted")
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(Boolean master) {
        this.master = master;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "InfraFileConfig{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", storage=" + storage +
                ", remark='" + remark + '\'' +
                ", master=" + master +
                ", config='" + config + '\'' +
                ", domain='" + domain + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}