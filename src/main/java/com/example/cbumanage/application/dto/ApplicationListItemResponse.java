package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.FinalDecision;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신청서 목록.
 * 목록 화면에서 추가 상세 호출 없이 투표 현황과 참석 여부를 표시할 수 있도록
 * 행 단위 집계 값을 함께 내려준다.
 */
public record ApplicationListItemResponse(
        String applicationUuid,
        FinalDecision finalDecision,
        FinalDecision suggestedDecision,
        String name,
        Long studentNumber,
        String major,
        List<ApplicationField> applicationFields,
        LocalDateTime submittedAt,
        long voteProgress,
        long passCount,
        long failCount,
        boolean myReviewed,
        Boolean canOt,
        Boolean canWelcome,
        String note,
        ApplicationStatus status
) {
    public static ApplicationListItemResponse of(
            MemberApplication application,
            long passCount,
            long failCount,
            boolean myReviewed,
            FinalDecision finalDecision,
            FinalDecision suggestedDecision,
            String note) {
        return new ApplicationListItemResponse(
                application.getApplicationUuid(),
                finalDecision,
                suggestedDecision,
                application.getName(),
                application.getStudentNumber(),
                application.getMajor(),
                List.copyOf(application.getApplicationFields()),
                application.getSubmittedAt(),
                passCount + failCount,
                passCount,
                failCount,
                myReviewed,
                application.getCanOt(),
                application.getCanWelcome(),
                note,
                application.getStatus()
        );
    }
}
