package com.health.diagnosis.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装类。
 *
 * <p>code=200 表示成功；其余为业务/系统错误码。</p>
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：200 成功 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
