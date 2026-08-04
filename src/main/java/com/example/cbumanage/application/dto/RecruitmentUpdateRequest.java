package com.example.cbumanage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "모집 회차 수정 요청 (전달한 필드만 갱신, null인 필드는 그대로 유지)")
public record RecruitmentUpdateRequest(
        @Schema(description = "기수 (변경 시 연결된 지원서 질문·제출된 지원서의 기수도 함께 변경됨)", example = "30")
        @Positive(message = "기수는 음수가 될 수 없습니다.")
        Long generation,

        @Schema(description = "모집 예정 시작일", example = "2026-09-01")
        LocalDate plannedStartDate,

        @Schema(description = "모집 예정 종료일", example = "2026-09-14")
        LocalDate plannedEndDate,

        @Schema(description = "합격 발표일", example = "2026-09-21")
        LocalDate announcementDate
) {
}
