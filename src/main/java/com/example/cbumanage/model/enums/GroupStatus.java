package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupStatus {
    @Schema(description = "그룹 활동 중")
    ACTIVE,

    @Schema(description = "그룹 활동 종료")
    INACTIVE;

}
