package com.example.cbumanage.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecruitmentCreateRequest(
        @NotNull(message = "기수는 필수입니다.")
        @Positive(message = "기수는 음수가 될 수 없습니다.")
        Long generation
) {
}