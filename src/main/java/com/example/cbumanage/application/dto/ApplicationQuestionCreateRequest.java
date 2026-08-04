package com.example.cbumanage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "지원서 질문 생성 요청")
public record ApplicationQuestionCreateRequest(
        @Schema(description = "질문 구분 타입 (기수 내 유일해야 함, 지원서 답변 제출 시 이 키로 매칭됨)", example = "MOTIVATION")
        @NotBlank(message = "질문 타입은 필수입니다.")
        String type,

        @Schema(description = "질문 본문", example = "지원 동기를 작성해 주세요.")
        @NotBlank(message = "질문 내용은 필수입니다.")
        String question,

        @Schema(description = "질문 부가 설명 (선택)", example = "500자 이내로 작성해 주세요.")
        String description,

        @Schema(description = "필수 답변 여부 (미입력 시 true)", example = "true")
        Boolean isRequired,

        @Schema(description = "질문 노출 순서 (미입력 시 해당 기수 마지막 순번 다음으로 자동 배정)", example = "1")
        @Positive(message = "순서는 1 이상이어야 합니다.")
        Integer sortOrder
) {
}
