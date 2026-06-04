package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * 신청서 목록
 * voteProgress(n)는 엔티티 외부에서 집계.
 */
public record ApplicationListItemResponse(
        String applicationUuid,
        String name,
        Long studentNumber,
        String major,
        ApplicationField applicationField,
        LocalDateTime submittedAt,
        long voteProgress,
        ApplicationStatus status
) {
    public static ApplicationListItemResponse of(MemberApplication application, long voteProgress) {
        return new ApplicationListItemResponse(
                application.getApplicationUuid(),
                application.getName(),
                application.getStudentNumber(),
                application.getMajor(),
                application.getApplicationField(),
                application.getSubmittedAt(),
                voteProgress,
                application.getStatus()
        );
    }
}