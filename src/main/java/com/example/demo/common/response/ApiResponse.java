package com.example.demo.common.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "操作成功";

    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                SUCCESS_CODE,
                SUCCESS_MESSAGE,
                data
        );
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(
                SUCCESS_CODE,
                SUCCESS_MESSAGE,
                null
        );
    }

    public static <T> ApiResponse<T> failure(
            int code,
            String message) {
        return new ApiResponse<>(code, message, null);
    }
}