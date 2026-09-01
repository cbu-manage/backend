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
        Integer sortOrder,

        @Schema(description = """
                화면에 불러올 때 받은 version. 같이 보내면 그 사이 다른 사람이 저장한 경우 409(E-COMMON-0010)로 막는다.
                생략하면 검사하지 않고 그대로 덮어쓴다.""", example = "0")
        Long version
) {
}
