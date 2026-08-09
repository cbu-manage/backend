package com.example.cbumanage.application.controller;

import com.example.cbumanage.application.dto.ApplicationQuestionCreateRequest;
import com.example.cbumanage.application.dto.ApplicationQuestionResponse;
import com.example.cbumanage.application.dto.ApplicationQuestionUpdateRequest;
import com.example.cbumanage.application.service.ApplicationQuestionService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/recruitments/{recruitmentUuid}/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', 'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')")
@Tag(name = "지원서 질문 관리", description = "운영진이 모집 회차별 지원서 질문 템플릿을 생성·수정·삭제합니다.")
public class ApplicationQuestionAdminController {

    private final ApplicationQuestionService applicationQuestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "지원서 질문 생성",
            description = """
                    해당 모집 회차(기수)에 새 지원서 질문을 추가합니다.

                    - `type`은 지원자가 답변 제출 시 `answers` 맵의 키로 사용하는 값으로, 같은 기수 내에서 유일해야 합니다
                      (중복 시 `QUESTION_TYPE_DUPLICATED`). 생성 후에는 변경할 수 없습니다.
                    - `sortOrder`를 지정하지 않으면 해당 기수의 마지막 순번 다음 번호로 자동 배정됩니다.
                    - 생성된 질문은 이후 `GET /api/v1/applications/questions/current` 조회부터 바로 노출되며,
                      **이미 제출된 지원서에는 소급 적용되지 않습니다.**
                    """)
    public ApiResponse<ApplicationQuestionResponse> create(
            @PathVariable String recruitmentUuid,
            @RequestBody @Valid ApplicationQuestionCreateRequest request) {
        return ApiResponse.success(applicationQuestionService.createQuestion(recruitmentUuid, request));
    }

    @PatchMapping("/{questionUuid}")
    @Operation(summary = "지원서 질문 수정",
            description = """
                    질문 본문/설명/필수여부/순서를 수정합니다. 요청 바디에 값을 넣은 필드만 갱신되고,
                    `null`이거나 생략한 필드는 기존 값이 유지됩니다.

                    이미 이 질문에 답변을 제출한 지원서가 있어도, 그 답변은 제출 당시 질문 본문(`questionSnapshot`)을
                    그대로 보존하므로 여기서 질문을 수정해도 과거 답변 조회 결과는 바뀌지 않습니다.
                    """)
    public ApiResponse<ApplicationQuestionResponse> update(
            @PathVariable String recruitmentUuid,
            @PathVariable String questionUuid,
            @RequestBody @Valid ApplicationQuestionUpdateRequest request) {
        return ApiResponse.success(applicationQuestionService.updateQuestion(recruitmentUuid, questionUuid, request));
    }

    @DeleteMapping("/{questionUuid}")
    @Operation(summary = "지원서 질문 삭제",
            description = """
                    질문을 소프트 삭제합니다. 삭제된 질문은 이후 지원서 작성 화면(질문 목록 조회)에서 제외됩니다.

                    이미 제출된 지원서의 답변 데이터는 삭제되지 않고 그대로 남습니다(질문만 신규 지원자에게 더 이상 노출되지 않음).

                    주의: `type`은 (기수, type) 단위로 DB에 유니크 제약이 걸려 있어, 삭제된 질문이라도 같은 `type`을
                    같은 기수에서 재사용해 새 질문을 만들 수 없습니다.
                    """)
    public ApiResponse<Void> delete(
            @PathVariable String recruitmentUuid,
            @PathVariable String questionUuid) {
        applicationQuestionService.deleteQuestion(recruitmentUuid, questionUuid);
        return ApiResponse.success();
    }
}
