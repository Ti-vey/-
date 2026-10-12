package com.warehouse.common;

/**
 * 业务异常：用于在业务规则不满足时中断流程，并返回明确的提示信息。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
