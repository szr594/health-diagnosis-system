package com.health.diagnosis.common;

import lombok.Getter;

/**
 * 业务异常：用于主动抛出可预期的业务错误，由全局异常处理器统一捕获。
 */
@Getter
public class BizException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(500, message);
    }
}
