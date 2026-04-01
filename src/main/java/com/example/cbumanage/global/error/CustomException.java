package com.example.cbumanage.global.error;

public class CustomException extends BaseException {

    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
