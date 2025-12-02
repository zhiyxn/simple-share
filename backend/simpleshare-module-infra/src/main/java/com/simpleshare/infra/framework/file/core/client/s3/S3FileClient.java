package com.simpleshare.infra.framework.file.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.simpleshare.infra.framework.file.core.client.AbstractFileClient;
import io.minio.*;
import io.minio.http.Method;

import java.io.ByteArrayInputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 基于 S3 协议的文件客户端，实现 MinIO、阿里云、腾讯云、七牛云、华为云等云服务
 *
 * S3 协议的云服务，采用亚马逊提供的 aws-java-sdk-s3 库
 * 但是，由于 aws-java-sdk-s3 库过于复杂，所以采用 MinIO 提供的 minio 库
 *
 * @author simple
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private MinioClient client;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 endpoint
        if (StrUtil.containsAnyIgnoreCase(config.getEndpoint(), "https://", "http://")) {
            config.setEndpoint(config.getEndpoint());
        } else {
            config.setEndpoint("https://" + config.getEndpoint());
        }
        // 初始化客户端
        client = MinioClient.builder()
                .endpoint(buildEndpointURL()) // Endpoint
                .credentials(config.getAccessKey(), config.getAccessSecret()) // 认证密钥
                .region(buildRegion()) // Region
                .build();
        // 设置 BucketName 配置
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_TENCENT)
                || config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_VOLCES)) {
            try {
                client.ignoreCertCheck(); // 忽略证书校验，解决 HTTPS 证书报错的问题
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 基于 endpoint 构建调用云服务的 URL 地址
     *
     * @return URL 地址
     */
    private String buildEndpointURL() {
        // 如果已经是 http 或者 https，直接返回
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        // 拼接 https://
        return "https://" + config.getEndpoint();
    }

    /**
     * 基于 bucket + endpoint 构建访问的 Domain 地址
     *
     * @return Domain 地址
     */
    private String buildDomain() {
        // 如果已经配置 domain，则直接返回
        if (StrUtil.isNotEmpty(config.getDomain())) {
            return config.getDomain();
        }
        // 未配置 domain 的情况下，基于 bucket + endpoint 进行拼接
        String domain = config.getBucket() + "." + config.getEndpoint();
        if (!domain.startsWith("http")) {
            domain = "https://" + domain;
        }
        return domain;
    }

    /**
     * 基于 endpoint 构建 region 地区
     *
     * @return region 地区
     */
    private String buildRegion() {
        // 阿里云必须有 region，否则会报错
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_ALIYUN)) {
            return StrUtil.subAfter(config.getEndpoint(), "oss-", false)
                    .replaceAll(".aliyuncs.com", "");
        }
        // 腾讯云必须有 region，否则会报错
        if (config.getEndpoint().contains(S3FileClientConfig.ENDPOINT_TENCENT)) {
            return StrUtil.subAfter(config.getEndpoint(), "cos.", false)
                    .replaceAll(".myqcloud.com", "");
        }
        return null;
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 执行上传
        client.putObject(PutObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .object(path) // 相对路径作为 key
                .stream(new ByteArrayInputStream(content), content.length, -1) // 文件内容
                .contentType(type)
                .build());
        // 拼接返回路径
        return buildDomain() + "/" + path;
    }

    @Override
    public void delete(String path) throws Exception {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .object(path) // 相对路径作为 key
                .build());
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        GetObjectResponse response = client.getObject(GetObjectArgs.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .object(path) // 相对路径作为 key
                .build());
        return IoUtil.readBytes(response);
    }

    @Override
    public String getPresignedObjectUrl(String path) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(config.getBucket())
                .object(path)
                .expiry(10, TimeUnit.MINUTES) // 过期时间 10 分钟
                .build());
    }

}