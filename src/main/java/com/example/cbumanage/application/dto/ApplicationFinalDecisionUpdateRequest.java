package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.FinalDecision;
import jakarta.validation.constraints.NotNull;

public record ApplicationFinalDecisionUpdateRequest(
        @NotNull FinalDecision decision,
        String reason
) {
}
