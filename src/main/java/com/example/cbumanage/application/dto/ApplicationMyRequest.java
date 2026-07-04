package com.example.cbumanage.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationMyRequest(
        @NotNull Long studentNumber,
        @NotBlank String nickname
) { }
