package com.example.cbumanage.controller;

import com.example.cbumanage.dto.SuccessCandidateDTO;
import com.example.cbumanage.model.SuccessCandidate;
import com.example.cbumanage.service.CandidateManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "합격자 검증 컨트롤러", description = "합격자를 검증하는 컨트롤러입니다.")
public class CandidateManageController {

    @Autowired
    CandidateManageService candidateManageService;

    @PostMapping("validate")
    @Operation(summary = "학번과 닉네임을 통한 1차 인증", description = "학번과 닉네임을 가지고 합격자 명단과 일치하는지 확인합니다.")
    public SuccessCandidate validateCandidate(@RequestBody SuccessCandidateDTO successCandidateDTO) throws Exception {
        SuccessCandidate successCandidate = candidateManageService.validateCandidate(successCandidateDTO);
        return successCandidate;
    }

}
