package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.FinalDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 일괄 최종처리 요청.
 * 검토대상(SUBMITTED) 신청서 각각의 합/불 처리가 결정되어야 함.
 */
public record ApplicationFinalizeRequest(
        @NotEmpty(message = "결정 목록은 비어 있을 수 없습니다.")
        @Valid
        List<Item> decisions
) {
    public record Item(
            @NotNull(message = "신청서 식별자는 필수입니다.")
            String applicationUuid,
            @NotNull(message = "결정은 필수입니다.")
            FinalDecision decision
    ) {
    }
}