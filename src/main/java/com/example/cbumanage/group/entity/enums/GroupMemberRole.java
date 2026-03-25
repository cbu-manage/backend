package com.example.cbumanage.group.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum GroupMemberRole {
    @Schema(description = "팀장(팀 생성자)")
    LEADER,

    @Schema(description = "일반 멤버")
    MEMBER;
}
