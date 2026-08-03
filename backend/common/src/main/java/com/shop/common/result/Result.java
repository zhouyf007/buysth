package com.shop.common.result;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = "success";
        result.data = data;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}

