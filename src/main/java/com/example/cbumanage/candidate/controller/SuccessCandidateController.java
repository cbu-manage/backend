package com.example.cbumanage.candidate.controller;

import com.example.cbumanage.candidate.service.SuccessCandidateSyncService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "합격자 관리 컨트롤러", description = "합격자 명단을 다루는 컨트롤러입니다.")
public class SuccessCandidateController {

    private final SuccessCandidateSyncService successCandidateSyncService;

    @PostMapping("candidate/sync")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "스프레드시트 -> 데이터베이스 데이터 연동", description = "스프레드시트의 데이터를 데이터베이스에 주입합니다.")
    public ApiResponse<Void> candidateSync() {
        successCandidateSyncService.syncSuccessCandidatesFromGoogleSheet();
        return ApiResponse.success();
    }
}
