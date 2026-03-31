package com.example.cbumanage.candidate.controller;

import com.example.cbumanage.candidate.dto.SuccessCandidateDTO;
import com.example.cbumanage.candidate.entity.SuccessCandidate;
import com.example.cbumanage.candidate.service.CandidateManageService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Tag(name = "합격자 검증 컨트롤러", description = "합격자를 검증하는 컨트롤러입니다.")
public class CandidateManageController {

    private final CandidateManageService candidateManageService;

    @PostMapping("validate")
    @Operation(summary = "학번과 닉네임을 통한 1차 인증", description = "학번과 닉네임을 가지고 합격자 명단과 일치하는지 확인합니다.")
    public ApiResponse<SuccessCandidate> validateCandidate(@RequestBody SuccessCandidateDTO successCandidateDTO) {
        SuccessCandidate successCandidate = candidateManageService.validateCandidate(successCandidateDTO);
        return ApiResponse.success(successCandidate);
    }
}
