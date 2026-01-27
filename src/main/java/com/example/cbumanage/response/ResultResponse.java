package com.example.cbumanage.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
/*
   상태 코드, 메세지, 반환되는 DTO를 담아서 ResponseEntity에 담아서 반환합니다
   DTO가 다양하기에 제네릭으로 받아서 사용합니다
 */
@Getter
@RequiredArgsConstructor
public class ResultResponse<T>{

    private final String code;
    private final String message;
    private final T data;

    /*
    매개변수로 DTO 가 들어오는 create,get 요청과 , 반환되는 데이터가 없어 매개변수가 없는 delete, Update 요청을 오버로딩합니다
     */
    public static <T> ResponseEntity<ResultResponse<T>> ok (SuccessCode successCode ,T data) {
        return ResponseEntity.status(successCode.getHttpStatus()).
                body(new ResultResponse<>(successCode.getCode(), successCode.getMessage(), data));
    }

    public static ResponseEntity<ResultResponse<Void>> ok(SuccessCode sc) {
        return ResponseEntity
                .status(sc.getHttpStatus())
                .body(new ResultResponse<>(sc.getCode(), sc.getMessage(), null));
    }

    /*
    에러를 처리합니다
     */
    public static <T> ResponseEntity<ResultResponse<T>> error (ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus()).
                body(new ResultResponse<>(errorCode.getCode(), errorCode.getMessage(),null ));
    }
}
