package com.warehouse.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }
}
