package com.example.cbumanage.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationValidateRequest(
        @NotNull(message = "학번은 필수입니다.")
        Long studentNumber,
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickName
) {
}
