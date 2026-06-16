package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicantApplicationResponse(
        String applicationUuid,
        Long studentNumber,
        String email,
        String name,
        String nickname,
        AcademicStatus grade,
        String major,
        String phoneNumber,
        Long generation,
        ApplicationField applicationField,
        String portfolioUrl,
        RefSource refSource,
        String refLinkEtc,
        Boolean canOt,
        Boolean canWelcome,
        ApplicationStatus status,
        String finalDecisionReason,
        LocalDateTime submittedAt,
        LocalDateTime decidedAt,
        List<ApplicationDetailResponse.AnswerItem> answers,
        List<ApplicationDetailResponse.PortfolioItem> portfolios
) {
    public static ApplicantApplicationResponse of(
            MemberApplication application,
            List<ApplicationDetailResponse.AnswerItem> answers,
            List<ApplicationDetailResponse.PortfolioItem> portfolios
    ) {
        return new ApplicantApplicationResponse(
                application.getApplicationUuid(),
                application.getStudentNumber(),
                application.getEmail(),
                application.getName(),
                application.getNickname(),
                application.getGrade(),
                application.getMajor(),
                application.getPhoneNumber(),
                application.getGeneration(),
                application.getApplicationField(),
                application.getPortfolioUrl(),
                application.getRefSource(),
                application.getRefLinkEtc(),
                application.getCanOt(),
                application.getCanWelcome(),
                application.getStatus(),
                application.getFinalDecisionReason(),
                application.getSubmittedAt(),
                application.getDecidedAt(),
                answers,
                portfolios
        );
    }
}
