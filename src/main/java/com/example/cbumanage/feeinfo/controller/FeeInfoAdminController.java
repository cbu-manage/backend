package com.example.cbumanage.feeinfo.controller;

import com.example.cbumanage.feeinfo.dto.FeeInfoRequest;
import com.example.cbumanage.feeinfo.dto.FeeInfoResponse;
import com.example.cbumanage.feeinfo.service.FeeInfoService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/settings/fee-info")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_TREASURER')")
@Tag(name = "회비 안내 설정", description = "운영진(회장·부회장·총무·어드민)이 회비·입금 계좌 안내를 조회·등록·수정합니다.")
public class FeeInfoAdminController {

    private final FeeInfoService feeInfoService;

    @GetMapping
    @Operation(summary = "회비 안내 조회 (관리자용)")
    public ApiResponse<FeeInfoResponse> get() {
        return ApiResponse.success(feeInfoService.get());
    }

    @PutMapping
    @Operation(summary = "회비 안내 등록/수정",
            description = "값이 아직 없으면 새로 생성하고, 있으면 갱신합니다 (전체 필드를 매번 함께 전달).")
    public ApiResponse<FeeInfoResponse> update(@RequestBody @Valid FeeInfoRequest request) {
        return ApiResponse.success(feeInfoService.update(request));
    }
}
