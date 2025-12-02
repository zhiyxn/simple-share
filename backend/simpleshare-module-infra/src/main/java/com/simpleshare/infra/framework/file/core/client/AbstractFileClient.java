package com.simpleshare.infra.framework.file.core.client;

import cn.hutool.core.util.StrUtil;

/**
 * 文件客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author simple
 */
public abstract class AbstractFileClient<Config extends FileClientConfig> implements FileClient {

    /**
     * 客户端编号
     */
    protected Long id;
    /**
     * 文件配置
     */
    protected Config config;

    public AbstractFileClient(Long id, Config config) {
        this.id = id;
        this.config = config;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInit();

    public final void refresh(Config config) {
        // 判断是否更新
        if (this.config.equals(config)) {
            return;
        }
        this.config = config;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * 格式化文件的 URL 返回
     *
     * @param domain 域名
     * @param path 文件路径
     * @return 文件的 URL
     */
    protected String formatFileUrl(String domain, String path) {
        return StrUtil.format("{}/{}", domain, path);
    }

}