package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupApprovalAction {
    @Schema(description = "그룹 승인")
    APPROVE,
    @Schema(description = "그룹 반려")
    REJECT
}
