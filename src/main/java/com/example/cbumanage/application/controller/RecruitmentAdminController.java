package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.RecruitmentCreateRequest;
import com.example.cbumanage.application.dto.RecruitmentResponse;
import com.example.cbumanage.application.service.RecruitmentService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/recruitments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
@Tag(name = "모집 회차 관리 컨트롤러", description = "운영진이 모집을 시작/마감하고 조회합니다.")
public class RecruitmentAdminController {

    private final RecruitmentService recruitmentService;

    @PostMapping
    @Operation(summary = "모집 시작", description = "모집을 시작합니다. 시작 시점의 운영진 수를 투표 자격자 수(N)로 고정합니다.")
    public ApiResponse<RecruitmentResponse> open(@RequestBody(required = false) @Valid RecruitmentCreateRequest request) {
        return ApiResponse.success(recruitmentService.open(request));
    }

    @GetMapping
    @Operation(summary = "모집 회차 목록 조회", description = "모집 회차를 최신순으로 조회합니다.")
    public ApiResponse<List<RecruitmentResponse>> getAll() {
        return ApiResponse.success(recruitmentService.getAll());
    }

    @GetMapping("/current")
    @Operation(summary = "현재 진행 중인 모집 조회", description = "현재 모집(OPEN) 상태인 모집을 조회합니다.")
    public ApiResponse<RecruitmentResponse> getCurrent() {
        return ApiResponse.success(recruitmentService.getCurrent());
    }

    @PatchMapping("/{recruitmentUuid}/close")
    @Operation(summary = "모집 마감", description = "모집을 마감합니다.")
    public ApiResponse<RecruitmentResponse> close(@PathVariable String recruitmentUuid) {
        return ApiResponse.success(recruitmentService.close(recruitmentUuid));
    }
}
