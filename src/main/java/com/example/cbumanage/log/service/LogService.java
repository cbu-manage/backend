package com.example.cbumanage.log.service;

import com.example.cbumanage.log.entity.Log;
import com.example.cbumanage.log.repository.LogRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogService {
    @Autowired
    LogRepository logRepository;
    public void createLog(Log log){
        logRepository.save(log);
    }
}
