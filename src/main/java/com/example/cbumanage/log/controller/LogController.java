package com.example.cbumanage.log.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.log.entity.Log;
import com.example.cbumanage.log.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "로그 생성 컨트롤러")
public class LogController {

    private final LogService logService;

    @PostMapping("/api/v1/createLog")
    @Operation(summary = "로그 생성")
    public ApiResponse<Void> createLog(@RequestBody Log log) {
        logService.createLog(log);
        return ApiResponse.success();
    }
}
