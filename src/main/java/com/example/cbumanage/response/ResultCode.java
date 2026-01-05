package com.example.cbumanage.response;


import org.springframework.http.HttpStatus;

public interface ResultCode {
    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
