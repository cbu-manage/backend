package com.example.cbumanage.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationMyRequest(
        @NotNull Long studentNumber,
        @NotBlank @Email String email,
        @NotBlank String emailAuthCode
) {
}
