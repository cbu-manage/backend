package com.example.cbumanage.group.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupStatus {
    @Schema(description = "그룹 승인 완료(활동 중)")
    APPROVED,

    @Schema(description = "그룹 승인 대기중")
    PENDING,

    @Schema(description = "그룹 승인 반려")
    REJECTED,

    @Schema(description = "활동 종료 또는 비활성화")
    INACTIVE
}
