package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.AdminApplicationListResponse;
import com.example.cbumanage.application.dto.ApplicationDetailResponse;
import com.example.cbumanage.application.dto.ApplicationFinalDecisionUpdateRequest;
import com.example.cbumanage.application.dto.ApplicationFinalizeRequest;
import com.example.cbumanage.application.entity.enums.ApplicationReview;
import com.example.cbumanage.application.dto.RecruitmentSummaryResponse;
import com.example.cbumanage.application.dto.VoteRequest;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.service.ApplicationReviewService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', 'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')")
@Tag(name = "신청서 심사 컨트롤러", description = "운영진이 신청서를 조회·심사합니다.")
public class ApplicationAdminController {

    private final ApplicationReviewService applicationReviewService;

    @GetMapping("/recruitments/{recruitmentUuid}/applications")
    @Operation(summary = "신청서 목록 조회",
            description = "모집 회차의 신청서를 분야·탭·기간·키워드로 검색합니다. 응답에 진행도 전체 voterCount(N)와 행별 투표 진행도(n)가 포함됩니다.")
    public ApiResponse<AdminApplicationListResponse> getApplications(
            @PathVariable String recruitmentUuid,
            @RequestParam(required = false) ApplicationField field,
            @RequestParam(required = false, defaultValue = "ALL") ApplicationReview tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String keyword,
            Pageable pageable,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        return ApiResponse.success(applicationReviewService.getApplications(
                recruitmentUuid, field, tab, from, to, keyword, pageable, currentUserId));
    }

    @GetMapping("/applications/{applicationUuid}")
    @Operation(summary = "신청서 상세 조회",
            description = "신청서 내용·자유서술 답변·포트폴리오·운영진 투표현황(미투표자 포함)과 내 투표를 조회합니다.")
    public ApiResponse<ApplicationDetailResponse> getDetail(
            @PathVariable String applicationUuid,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        return ApiResponse.success(applicationReviewService.getDetail(applicationUuid, currentUserId));
    }

    @PutMapping("/applications/{applicationUuid}/vote")
    @PreAuthorize("hasAnyAuthority('ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', 'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')")
    @Operation(summary = "투표 등록/수정",
            description = "합격/불합격 투표를 등록하거나 수정합니다(1인 1표). 불합격은 사유가 필수입니다.")
    public ApiResponse<Void> vote(
            @PathVariable String applicationUuid,
            @RequestBody @Valid VoteRequest request,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        applicationReviewService.vote(applicationUuid, currentUserId, request);
        return ApiResponse.success();
    }

    @GetMapping("/recruitments/{recruitmentUuid}/summary")
    @Operation(summary = "심사 대시보드 요약",
            description = "모집 컨텍스트, 상태별 카운트, 투표 진행 현황(전체찬성/보류/만장일치불합격), 후보 테이블을 제공합니다.")
    public ApiResponse<RecruitmentSummaryResponse> getSummary(
            @PathVariable String recruitmentUuid,
            Pageable pageable) {
        return ApiResponse.success(applicationReviewService.getSummary(recruitmentUuid, pageable));
    }

    @PostMapping("/recruitments/{recruitmentUuid}/applications/finalize")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @Operation(summary = "일괄 최종처리",
            description = "검토대상 신청서를 일괄 합격/불합격 처리합니다. 보류가 남아있거나 투표가 완료되지 않으면 거절됩니다.")
    public ApiResponse<Void> finalizeDecisions(
            @PathVariable String recruitmentUuid,
            @RequestBody @Valid ApplicationFinalizeRequest request,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        applicationReviewService.finalizeDecisions(recruitmentUuid, currentUserId, request);
        return ApiResponse.success();
    }

    @PatchMapping("/applications/{applicationUuid}/final-decision")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @Operation(summary = "개별 최종결정 수정",
            description = "관리자/회장/부회장이 신청서의 최종 합격·불합격·보류 상태를 수정합니다. 최종 불합격 사유는 선택입니다.")
    public ApiResponse<Void> updateFinalDecision(
            @PathVariable String applicationUuid,
            @RequestBody @Valid ApplicationFinalDecisionUpdateRequest request,
            Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        applicationReviewService.updateFinalDecision(applicationUuid, currentUserId, request);
        return ApiResponse.success();
    }
}
