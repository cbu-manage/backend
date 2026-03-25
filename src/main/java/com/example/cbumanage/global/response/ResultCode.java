package com.example.cbumanage.global.response;


import org.springframework.http.HttpStatus;

public interface ResultCode {
    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
