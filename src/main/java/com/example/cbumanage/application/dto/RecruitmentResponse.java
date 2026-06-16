package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;

import java.time.LocalDateTime;

public record RecruitmentResponse(
        String recruitmentUuid,
        Long generation,
        int voterCount,
        RecruitmentStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static RecruitmentResponse from(Recruitment recruitment) {
        return new RecruitmentResponse(
                recruitment.getRecruitmentUuid(),
                recruitment.getGeneration(),
                recruitment.getVoterCount(),
                recruitment.getStatus(),
                recruitment.getStartedAt(),
                recruitment.getEndedAt()
        );
    }
}