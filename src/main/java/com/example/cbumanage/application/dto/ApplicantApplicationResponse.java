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
        List<ApplicationField> applicationFields,
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
    /**
     * 발표일 전에는 합불과 사유를 감춘다.
     * 지원자 본인 조회에도 status·finalDecisionReason·decidedAt이 그대로 실려
     * 발표 전에 결과를 먼저 알 수 있었다. 취소는 본인이 한 것이므로 그대로 둔다.
     */
    public ApplicantApplicationResponse hideResult() {
        if (status == ApplicationStatus.SUBMITTED || status == ApplicationStatus.CANCELLED) {
            return this;
        }
        return new ApplicantApplicationResponse(
                applicationUuid, studentNumber, email, name, nickname, grade, major, phoneNumber,
                generation, applicationFields, portfolioUrl, refSource, refLinkEtc,
                canOt, canWelcome,
                ApplicationStatus.SUBMITTED, null, submittedAt, null,
                answers, portfolios);
    }

    /**
     * 비로그인으로 받을 수 있는 응답이라 연락처를 가린다.
     * 이 조회는 학번+닉네임만 맞히면 통과하므로, 원문을 실으면 지원자 연락처가 그대로 나간다.
     * 본인이 방금 제출한 응답(submit)은 본인이 입력한 값이므로 여기 대상이 아니다.
     */
    public ApplicantApplicationResponse maskContacts() {
        return new ApplicantApplicationResponse(
                applicationUuid, studentNumber, maskEmail(email), name, nickname, grade, major,
                maskPhoneNumber(phoneNumber),
                generation, applicationFields, portfolioUrl, refSource, refLinkEtc,
                canOt, canWelcome,
                status, finalDecisionReason, submittedAt, decidedAt,
                answers, portfolios);
    }

    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() < 7) {
            return "***";
        }
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }

    private static String maskEmail(String email) {
        if (email == null) return null;

        int at = email.indexOf('@');
        if (at <= 0) return "***";

        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

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
                List.copyOf(application.getApplicationFields()),
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
