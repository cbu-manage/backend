package com.example.cbumanage.global.setting.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.setting.dto.OnboardingLinksRequest;
import com.example.cbumanage.global.setting.dto.OnboardingLinksResponse;
import com.example.cbumanage.global.setting.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
@Tag(name = "시스템 설정", description = "관리자가 온보딩 링크 등 시스템 설정을 조회·수정합니다.")
public class SystemSettingAdminController {

    private final SystemSettingService systemSettingService;

    @GetMapping("/onboarding-links")
    @Operation(summary = "온보딩 링크 조회", description = "합격/승인 안내 메일에 사용되는 홈페이지·오픈채팅·디스코드 링크를 조회합니다.")
    public ApiResponse<OnboardingLinksResponse> getOnboardingLinks() {
        return ApiResponse.success(systemSettingService.getOnboardingLinks());
    }

    @PutMapping("/onboarding-links")
    @Operation(summary = "온보딩 링크 수정", description = "합격/승인 안내 메일에 사용되는 홈페이지·오픈채팅·디스코드 링크를 DB에 저장합니다.")
    public ApiResponse<OnboardingLinksResponse> updateOnboardingLinks(
            @RequestBody OnboardingLinksRequest request) {
        return ApiResponse.success(systemSettingService.updateOnboardingLinks(request));
    }
}
