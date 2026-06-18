package com.example.cbumanage.application.dto;

import jakarta.validation.constraints.Positive;

public record RecruitmentCreateRequest(
        @Positive(message = "기수는 음수가 될 수 없습니다.")
        Long generation
) {
}
