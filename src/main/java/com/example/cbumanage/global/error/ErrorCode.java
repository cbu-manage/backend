package com.example.cbumanage.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
에러를 처리하는 코드입니다
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("E-COMMON-0001", "잘못된 요청입니다. ", HttpStatus.BAD_REQUEST),
    NOT_FOUND("E-COMMON-0002", "리소스를 찾을 수 없음", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("E-COMMON-0003", "중복 리소스", HttpStatus.CONFLICT),
    NOT_ALLOWED_FILETYPE("E-COMMON-0004","잘못된 파일 타입", HttpStatus.CONFLICT),
    ALREADY_JOINED_MEMBER("E-COMMON-0005","이미 가입된 멤버",HttpStatus.CONFLICT),

    UNAUTHORIZED("E-AUTH-0001", "인증이 필요합니다. ", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E-AUTH-0002", "권한이 없습니다. ", HttpStatus.FORBIDDEN),
    SUCCESS_MEMBER_NOT_FOUND("E-AUTH-0003", "합격자 중 존재하지 않습니다. ", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("E-AUTH-0004", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("E-AUTH-0005", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}

