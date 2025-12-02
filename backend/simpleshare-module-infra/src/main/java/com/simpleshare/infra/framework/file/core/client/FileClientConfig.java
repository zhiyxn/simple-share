package com.simpleshare.infra.framework.file.core.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 文件客户端的配置
 *
 * @author simple
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface FileClientConfig {
}