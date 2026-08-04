package com.example.cbumanage.feeinfo.controller;

import com.example.cbumanage.feeinfo.dto.FeeInfoResponse;
import com.example.cbumanage.feeinfo.service.FeeInfoService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fee-info")
@RequiredArgsConstructor
@Tag(name = "회비 안내", description = "지원자 등 누구나 회비·입금 계좌 안내를 조회합니다.")
public class FeeInfoController {

    private final FeeInfoService feeInfoService;

    @GetMapping
    @Operation(summary = "회비 안내 공개 조회")
    public ApiResponse<FeeInfoResponse> get() {
        return ApiResponse.success(feeInfoService.get());
    }
}
