package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum ApplicantAction {
    @Schema(description = "가입 승인")
    ACCEPT,
    @Schema(description ="가입 거절")
    REJECT
}
