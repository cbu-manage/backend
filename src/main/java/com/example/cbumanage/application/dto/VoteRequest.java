package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.VoteResult;
import jakarta.validation.constraints.NotNull;

/**
 * 내 투표 등록/수정. FAIL이면 reason 필수(서비스에서 검증).
 */
public record VoteRequest(
        @NotNull(message = "투표 결과는 필수입니다.")
        VoteResult decision,
        String reason
) {
}