package com.simpleshare.infra.framework.file.core.client.local;

import com.simpleshare.infra.framework.file.core.client.FileClientConfig;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 本地文件客户端的配置类
 *
 * @author simple
 */
@Data
public class LocalFileClientConfig implements FileClientConfig {

    /**
     * 基础路径
     */
    @NotEmpty(message = "基础路径不能为空")
    private String basePath;

    /**
     * 自定义域名
     */
    @NotEmpty(message = "domain 不能为空")
    private String domain;

}