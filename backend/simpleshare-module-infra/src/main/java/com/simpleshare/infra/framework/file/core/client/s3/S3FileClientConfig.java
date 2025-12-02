package com.simpleshare.infra.framework.file.core.client.s3;

import com.simpleshare.infra.framework.file.core.client.FileClientConfig;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * S3 文件客户端的配置类
 *
 * @author simple
 */
@Data
public class S3FileClientConfig implements FileClientConfig {

    public static final String ENDPOINT_QINIU = "s3-cn-south-1.qiniucs.com";
    public static final String ENDPOINT_ALIYUN = "oss-cn-beijing.aliyuncs.com";
    public static final String ENDPOINT_TENCENT = "cos.ap-beijing.myqcloud.com";
    public static final String ENDPOINT_VOLCES = "tos-s3-cn-beijing.volces.com";

    /**
     * 节点地址
     */
    @NotEmpty(message = "endpoint 不能为空")
    private String endpoint;
    /**
     * 自定义域名
     */
    private String domain;
    /**
     * 存储 Bucket
     */
    @NotEmpty(message = "bucket 不能为空")
    private String bucket;

    /**
     * 访问 Key
     */
    @NotEmpty(message = "accessKey 不能为空")
    private String accessKey;
    /**
     * 访问 Secret
     */
    @NotEmpty(message = "accessSecret 不能为空")
    private String accessSecret;

    @AssertTrue(message = "七牛云必须配置 domain")
    public boolean isDomainValid() {
        // 如果是七牛，必须带有 domain
        if (endpoint.contains(ENDPOINT_QINIU)) {
            return domain != null;
        }
        return true;
    }

}