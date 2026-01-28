package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupRecruitmentStatus {
    @Schema(description = "팀원 모집 중")
    OPEN,

    @Schema(description = "팀원 모집 종료")
    CLOSED;
}
