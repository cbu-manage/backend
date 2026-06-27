package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.ApplicantApplicationResponse;
import com.example.cbumanage.application.dto.ApplicationCancelRequest;
import com.example.cbumanage.application.dto.ApplicationMyRequest;
import com.example.cbumanage.application.dto.ApplicationQuestionResponse;
import com.example.cbumanage.application.dto.ApplicationSubmitRequest;
import com.example.cbumanage.application.dto.CurrentApplicationGenerationResponse;
import com.example.cbumanage.application.service.ApplicationApplicantService;
import com.example.cbumanage.application.service.ApplicationQuestionService;
import com.example.cbumanage.application.service.RecruitmentService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "지원서", description = "지원자가 학교 이메일 인증 후 지원서를 작성·조회·취소합니다.")
public class ApplicationApplicantController {

    private final ApplicationApplicantService applicationApplicantService;
    private final ApplicationQuestionService applicationQuestionService;
    private final RecruitmentService recruitmentService;

    @GetMapping("/generation/current")
    @Operation(summary = "현재 지원 기수 조회",
            description = "현재 진행 중인 모집 회차의 신청 기수를 조회합니다.")
    public ApiResponse<CurrentApplicationGenerationResponse> getCurrentGeneration() {
        return ApiResponse.success(recruitmentService.getCurrentApplicationGeneration());
    }

    @GetMapping("/questions/current")
    @Operation(summary = "현재 모집 지원서 질문 조회",
            description = "현재 진행 중인 모집 회차의 필수 질문 목록을 조회합니다.")
    public ApiResponse<List<ApplicationQuestionResponse>> getCurrentQuestions() {
        return ApiResponse.success(applicationQuestionService.getCurrentQuestions());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "지원서 작성", description = "학교 이메일 인증코드 검증 후 현재 진행 중인 모집에 지원서를 제출합니다.")
    public ApiResponse<ApplicantApplicationResponse> submit(
            @RequestBody @Valid ApplicationSubmitRequest request) {
        return ApiResponse.success(applicationApplicantService.submit(request));
    }

    @PostMapping("/my")
    @Operation(summary = "내 지원서 조회", description = "학번과 학교 이메일 인증코드 검증 후 본인이 작성한 지원서를 조회합니다.")
    public ApiResponse<ApplicantApplicationResponse> getMyApplication(
            @RequestBody @Valid ApplicationMyRequest request) {
        return ApiResponse.success(applicationApplicantService.getMyApplication(request));
    }

    @DeleteMapping("/{applicationUuid}")
    @Operation(summary = "지원서 취소", description = "학번과 학교 이메일 인증코드 검증 후 본인이 제출한 지원서를 취소합니다.")
    public ApiResponse<Void> cancel(
            @PathVariable String applicationUuid,
            @RequestBody @Valid ApplicationCancelRequest request) {
        applicationApplicantService.cancel(applicationUuid, request);
        return ApiResponse.success();
    }
}
