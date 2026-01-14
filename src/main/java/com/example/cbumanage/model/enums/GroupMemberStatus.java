package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupMemberStatus {
    @Schema(description = "가입대기")
    PENDING,
    @Schema(description = "활동상태")
    ACTIVE,
    @Schema(description = "활동중단")
    INACTIVE;
}
