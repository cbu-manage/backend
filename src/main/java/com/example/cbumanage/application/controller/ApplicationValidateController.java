package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.ApplicationValidateRequest;
import com.example.cbumanage.application.dto.ApplicationValidateResponse;
import com.example.cbumanage.application.service.ApplicationValidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "신청서 검증 컨트롤러", description = "승인된 신청서를 기반으로 회원가입 전 사용자를 검증합니다.")
public class ApplicationValidateController {

    private final ApplicationValidateService applicationValidateService;

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "학번과 닉네임을 통한 승인 신청서 검증",
            description = "학번과 지원 시 닉네임이 ADMIN_ACCEPTED 상태의 신청서와 일치하는지 확인합니다.")
    public ApplicationValidateResponse validate(@Valid @RequestBody ApplicationValidateRequest request) {
        return applicationValidateService.validate(request);
    }
}
