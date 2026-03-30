package com.example.cbumanage.group.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum MemberApprovalAction {
    @Schema(description = "멤버 가입 승인")
    ACCEPT,
    @Schema(description = "멤버 가입 거절")
    REJECT
}
