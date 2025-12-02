package com.simpleshare.common.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.simpleshare.common.constant.HttpStatus;
import com.simpleshare.common.utils.StringUtils;

import java.util.HashMap;
import java.util.Objects;

/**
 * 统一返回结果封装类
 * 标准格式：{code: 状态码, msg: 返回消息, data: 数据对象, timestamp: 时间戳}
 *
 * @author SimpleShare
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    public static final String CODE_TAG = "code";

    /** 返回内容 */
    public static final String MSG_TAG = "msg";

    /** 数据对象 */
    public static final String DATA_TAG = "data";

    /** 时间戳 */
    public static final String TIMESTAMP_TAG = "timestamp";

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public AjaxResult() {
        super.put(TIMESTAMP_TAG, System.currentTimeMillis());
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public AjaxResult(int code, String msg) {
        this();
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public AjaxResult(int code, String msg, Object data) {
        this();
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success() {
        return AjaxResult.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static AjaxResult warn(String msg) {
        return AjaxResult.warn(msg, null);
    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static AjaxResult warn(String msg, Object data) {
        return new AjaxResult(HttpStatus.WARN, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static AjaxResult error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 错误消息
     */
    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 错误消息
     */
    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(HttpStatus.ERROR, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 错误消息
     */
    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    /**
     * 返回未授权消息
     *
     * @param msg 返回内容
     * @return 未授权消息
     */
    public static AjaxResult unauthorized(String msg) {
        return new AjaxResult(HttpStatus.UNAUTHORIZED, msg, null);
    }

    /**
     * 是否为成功消息
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return Objects.equals(HttpStatus.SUCCESS, this.get(CODE_TAG));
    }

    /**
     * 是否为警告消息
     *
     * @return 结果
     */
    public boolean isWarn() {
        return Objects.equals(HttpStatus.WARN, this.get(CODE_TAG));
    }

    /**
     * 是否为错误消息
     *
     * @return 结果
     */
    public boolean isError() {
        return Objects.equals(HttpStatus.ERROR, this.get(CODE_TAG));
    }

    /**
     * 方便链式调用
     *
     * @param key   键
     * @param value 值
     * @return 数据对象
     */
    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
    
    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public Integer getCode() {
        return (Integer) this.get(CODE_TAG);
    }
    
    /**
     * 获取返回消息
     *
     * @return 返回消息
     */
    public String getMsg() {
        return (String) this.get(MSG_TAG);
    }
    
    /**
     * 获取数据对象
     *
     * @return 数据对象
     */
    public Object getData() {
        return this.get(DATA_TAG);
    }
    
    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    public Long getTimestamp() {
        return (Long) this.get(TIMESTAMP_TAG);
    }
    
    /**
     * 设置数据对象
     *
     * @param data 数据对象
     * @return AjaxResult
     */
    public AjaxResult setData(Object data) {
        super.put(DATA_TAG, data);
        return this;
    }
}
