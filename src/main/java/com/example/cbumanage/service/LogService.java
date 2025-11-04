package com.example.cbumanage.service;

import com.example.cbumanage.model.Log;
import com.example.cbumanage.repository.LogRepository;
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
