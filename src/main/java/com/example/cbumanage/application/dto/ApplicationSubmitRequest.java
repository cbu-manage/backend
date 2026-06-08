package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.RefSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApplicationSubmitRequest(
        @NotNull Long studentNumber,
        @NotBlank @Email String email,
        @NotBlank String emailAuthCode,
        @NotBlank String name,
        @NotBlank String nickname,
        @NotNull AcademicStatus grade,
        @NotBlank String major,
        @NotBlank String phoneNumber,
        @NotNull ApplicationField applicationField,
        String portfolioUrl,
        @NotNull RefSource refSource,
        String refLinkEtc,
        @NotNull Boolean canOt,
        @NotNull Boolean canWelcome,
        @NotNull
        @AssertTrue(message = "개인정보 수집에 동의해야 합니다.")
        Boolean privacyPolicy,
        @Valid List<AnswerRequest> answers,
        @Valid List<PortfolioRequest> portfolios
) {
    public record AnswerRequest(
            @NotBlank String questionUuid,
            @NotBlank String answer
    ) {
    }

    public record PortfolioRequest(
            String label,
            @NotBlank String url,
            Integer sortOrder
    ) {
    }
}
