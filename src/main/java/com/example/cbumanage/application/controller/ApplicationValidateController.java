package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.ApplicationResultResponse;
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
@Tag(name = "지원서 검증", description = "승인된 지원서를 기반으로 회원가입 전 사용자를 검증합니다.")
public class ApplicationValidateController {

    private final ApplicationValidateService applicationValidateService;

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "승인된 지원서 검증",
            description = "학번과 지원 시 닉네임이 최종 승인된 지원서와 일치하는지 확인합니다.")
    public ApplicationValidateResponse validate(@Valid @RequestBody ApplicationValidateRequest request) {
        return applicationValidateService.validate(request);
    }

    @GetMapping("/applications/{applicationUuid}/result")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "합격 안내 정보 조회",
            description = "합격 안내 메일 링크로 여는 화면에서 본인 확인용으로 사용합니다. "
                    + "학번은 가운데를 가려서 내려주며, 합격한 지원서가 아니면 404입니다.")
    public ApplicationResultResponse getResult(@PathVariable String applicationUuid) {
        return applicationValidateService.getAcceptedResult(applicationUuid);
    }
}
