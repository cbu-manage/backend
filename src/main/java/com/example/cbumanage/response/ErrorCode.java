package com.example.cbumanage.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
에러를 처리하는 코드입니다
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResultCode {
    INVALID_REQUEST("E-COMMON-0001", HttpStatus.BAD_REQUEST, "잘못된 요청"),
    UNAUTHORIZED("E-AUTH-0001", HttpStatus.UNAUTHORIZED, "인증 필요"),
    FORBIDDEN("E-AUTH-0002", HttpStatus.FORBIDDEN, "권한 없음"),
    NOT_FOUND("E-COMMON-0002", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없음"),
    DUPLICATE_RESOURCE("E-COMMON-0003", HttpStatus.CONFLICT, "중복 리소스");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

}

