package com.example.cbumanage.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "지원서 질문 수정 요청 (전달한 필드만 갱신, null인 필드는 그대로 유지)")
public record ApplicationQuestionUpdateRequest(
        @Schema(description = "질문 본문", example = "지원 동기를 작성해 주세요.")
        String question,

        @Schema(description = "질문 부가 설명", example = "500자 이내로 작성해 주세요.")
        String description,

        @Schema(description = "필수 답변 여부", example = "true")
        Boolean isRequired,

        @Schema(description = "질문 노출 순서", example = "1")
        @Positive(message = "순서는 1 이상이어야 합니다.")
        Integer sortOrder
) {
}
