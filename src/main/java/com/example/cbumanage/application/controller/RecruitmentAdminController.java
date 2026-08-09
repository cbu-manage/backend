package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.RecruitmentCreateRequest;
import com.example.cbumanage.application.dto.RecruitmentResponse;
import com.example.cbumanage.application.dto.RecruitmentUpdateRequest;
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
@Tag(name = "모집 회차", description = "운영진이 모집 회차를 시작·마감·조회합니다.")
public class RecruitmentAdminController {

    private final RecruitmentService recruitmentService;

    @PostMapping
    @Operation(summary = "모집 시작", description = "모집을 시작합니다. 시작 시점의 운영진 수를 투표 자격자 수(N)로 고정합니다.")
    public ApiResponse<RecruitmentResponse> open(@RequestBody(required = false) @Valid RecruitmentCreateRequest request) {
        return ApiResponse.success(recruitmentService.open(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', 'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')")
    @Operation(summary = "모집 회차 목록 조회", description = "모집 회차를 최신순으로 조회합니다. 질문 관리 API(`/admin/recruitments/{recruitmentUuid}/questions`)에 필요한 recruitmentUuid를 여기서 확인할 수 있습니다.")
    public ApiResponse<List<RecruitmentResponse>> getAll() {
        return ApiResponse.success(recruitmentService.getAll());
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', 'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')")
    @Operation(summary = "현재 진행 중인 모집 조회", description = "현재 모집(OPEN) 상태인 모집을 조회합니다. 질문 관리 API에 필요한 recruitmentUuid를 여기서 확인할 수 있습니다.")
    public ApiResponse<RecruitmentResponse> getCurrent() {
        return ApiResponse.success(recruitmentService.getCurrent());
    }

    @PatchMapping("/{recruitmentUuid}")
    @Operation(summary = "모집 회차 정보 수정",
            description = """
                    기수·모집 기간(예정 시작일/종료일)·발표일을 수정합니다. 요청 바디에 값을 넣은 필드만 갱신되고,
                    null이거나 생략한 필드는 기존 값이 유지됩니다.

                    기수(`generation`)를 변경하면 해당 기수에 연결된 지원서 질문(`ApplicationQuestion`)과
                    이미 제출된 지원서(`MemberApplication`)의 기수 값도 함께 변경됩니다. 변경하려는 기수가
                    이미 다른 모집 회차에서 쓰이고 있으면 `RECRUITMENT_DUPLICATED`가 발생합니다.
                    """)
    public ApiResponse<RecruitmentResponse> update(
            @PathVariable String recruitmentUuid,
            @RequestBody @Valid RecruitmentUpdateRequest request) {
        return ApiResponse.success(recruitmentService.update(recruitmentUuid, request));
    }

    @PatchMapping("/{recruitmentUuid}/close")
    @Operation(summary = "모집 마감", description = "모집을 마감합니다.")
    public ApiResponse<RecruitmentResponse> close(@PathVariable String recruitmentUuid) {
        return ApiResponse.success(recruitmentService.close(recruitmentUuid));
    }
}
