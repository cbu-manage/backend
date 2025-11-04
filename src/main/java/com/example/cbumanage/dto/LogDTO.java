package com.example.cbumanage.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class LogDTO {
    private Long logId;      //로그 고유 번호
    private Date date;       //로그 생성 날짜
    private String logUser;  //로그 생성 유저
    private String logData;  //로그 생성 원인
}
