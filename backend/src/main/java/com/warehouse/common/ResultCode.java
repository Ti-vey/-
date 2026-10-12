package com.warehouse.common;

/**
 * 统一响应状态码。
 */
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 */
    PARAM_ERROR(400, "请求参数错误"),
    /** 未登录或 token 失效 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无访问权限 */
    FORBIDDEN(403, "无访问权限"),
    /** 业务处理失败 */
    FAIL(500, "操作失败");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
