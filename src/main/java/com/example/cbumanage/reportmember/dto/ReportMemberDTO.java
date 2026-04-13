package com.example.cbumanage.reportmember.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReportMemberDTO {

    @Schema(description = "보고서 참여 멤버의 정보를 담는 DTO입니다")
    public record ReportMemberInfoDTO(
            Long memberId,
            String name,
            Long studentNumber,
            String major
    ) {}
}
