package com.example.cbumanage.global.common;

import com.example.cbumanage.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 완료되었습니다. ", data);
    } // 요청 성공

    public static <T> ApiResponse<T> success() {
        return success(null);
    } // 요청 성공(반환 값 없는 경우)

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    } // 요청 실패
}
