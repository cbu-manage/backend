package com.example.cbumanage.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResultCode{
    /*
    읽어오기 요청 - SUCCESS
    생성 요청 - CREATED
    수정 요청 - UPDATED
    삭제 요청 - DELETED

    도메인에 따라 코드가 달라져야 할 때를 대비해 중간에 COMMON을 추가했습니다
     */
    SUCCESS("S-COMMON-SUCCESS", HttpStatus.OK,"요청 성공"),
    CREATED("S-COMMON-CREATED",HttpStatus.CREATED,"생성 성공"),
    UPDATED("S-COMMON-UPDATED",HttpStatus.OK,"수정 성공"),
    DELETED("S-COMMON-DELETED",HttpStatus.OK,"삭제 성공");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
