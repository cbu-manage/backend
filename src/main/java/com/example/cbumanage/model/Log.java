package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.LogDataType;
import com.example.cbumanage.model.enums.LogType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime date;              // 로그가 생성된 시점

    private Long loggerId;                   // 로그를 생성한 유저

    private LogType logType;                 // create, update, delete
    private LogDataType logDataType;         // name, phone_number 등등
    private String detail;                   // 로그 내용

    public Log() {
    }

    public Log(Long loggerId, LogType logType, LogDataType logDataType, String detail) {
        this.loggerId = loggerId;
        this.logType = logType;
        this.logDataType = logDataType;
        this.detail = detail;
    }
}
