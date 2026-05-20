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
    INVALID_PASSWORD("E-AUTH-0005", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

    //그룹 에러 코드
    GROUP_NOT_FOUND("E-GROUP-0001", "그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GROUP_MEMBER_NOT_FOUND("E-GROUP-0002", "그룹 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_GROUP_LEADER("E-GROUP-0003", "그룹 리더가 아닙니다.", HttpStatus.FORBIDDEN),
    GROUP_NOT_RECRUITING("E-GROUP-0004", "모집 중인 그룹이 아닙니다.", HttpStatus.BAD_REQUEST),

    //POST 공용
    POST_NOT_FOUND("E-POST-0001", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 회원가입 신청서 에러 코드
    APPLICATION_NOT_FOUND("E-APP-0001", "신청서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    APPLICATION_DUPLICATED("E-APP-0002", "이미 신청서가 존재합니다.", HttpStatus.CONFLICT),
    APPLICATION_DECIDED("E-APP-0003", "수정기한이 지났습니다. 최종 결정된 신청서 입니다.", HttpStatus.BAD_REQUEST),
    APPLICATION_CANCELLED("E-APP-0004", "이미 취소된 신청서 입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PIN("E-APP-0005", "PIN번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_APPLICATION_STATUS("E-APP-0006", "현재 상태에서 허용되지 않는 작업입니다.", HttpStatus.BAD_REQUEST),
    QUESTION_NOT_FOUND("E-APP-0007", "질문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    FAIL_REASON_REQUIRED("E-APP-0009", "탈락 사유는 필수입니다. ", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}

