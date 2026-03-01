package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 신청 상태")
public enum StudyApplyStatus {
    @Schema(description = "신청 대기")
    PENDING,
    @Schema(description = "수락됨")
    ACCEPTED,
    @Schema(description = "거절됨")
    REJECTED,
    @Schema(description = "신청 취소")
    CANCELLED;
}
