package com.example.cbumanage.global.setting.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.setting.dto.OnboardingLinksResponse;
import com.example.cbumanage.global.setting.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding-links")
@RequiredArgsConstructor
@Tag(name = "온보딩 링크", description = "합격자 등 누구나 홈페이지·공지방·수다방·회비 문의방·디스코드 링크를 조회합니다.")
public class OnboardingLinksController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @Operation(summary = "온보딩 링크 공개 조회")
    public ApiResponse<OnboardingLinksResponse> get() {
        return ApiResponse.success(systemSettingService.getOnboardingLinks());
    }
}
