package com.example.cbumanage.suggestion.dto;

import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 건의 게시판 DTO. 작성자 식별 정보(authorId·이름·기수)는 어떤 응답에도 넣지 않는다.
 * 본인 글·댓글 판별은 서버가 계산한 isAuthor 한 비트로만 한다.
 */
public class SuggestionDTO {

    /** 댓글 본문 한도 — comment.content 컬럼(1,000)과 같아야 한다 */
    public static final int COMMENT_MAX_LENGTH = 1000;

    public record SuggestionCreateRequest(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
            String title,
            @NotBlank(message = "내용은 필수입니다.")
            String content,
            @NotNull(message = "건의 종류는 필수입니다.")
            SuggestionType type
    ) {}

    /** 셋 다 선택. 넘긴 것만 바꾼다 */
    public record SuggestionUpdateRequest(
            @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
            String title,
            String content,
            SuggestionType type
    ) {}

    public record SuggestionStatusUpdateRequest(
            @NotNull(message = "상태는 필수입니다.")
            SuggestionStatus status
    ) {}

    public record SuggestionPinRequest(
            @NotNull(message = "pinned 는 필수입니다.")
            Boolean pinned
    ) {}

    @Schema(description = "건의 목록 행 (content 미포함, 작성자 정보 없음)")
    public record SuggestionPreviewDTO(
            Long postId,
            String title,
            SuggestionType type,
            SuggestionStatus status,
            @Schema(description = "상단 고정 여부 — 목록은 고정 글이 먼저 온다")
            boolean isPinned,
            LocalDateTime createdAt,
            Long viewCount,
            Long commentCount,
            @Schema(description = "요청자가 작성자인지 — 수정·삭제 버튼 노출용")
            boolean isAuthor
    ) {}

    @Schema(description = "건의 단건 (content 포함, 작성자 정보 없음)")
    public record SuggestionInfoDTO(
            Long postId,
            String title,
            String content,
            SuggestionType type,
            SuggestionStatus status,
            boolean isPinned,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt,
            Long viewCount,
            Long commentCount,
            boolean isAuthor
    ) {}

    @Schema(description = "미해결/해결 건수 — 상단 요약용")
    public record SuggestionSummaryDTO(
            long openCount,
            long resolvedCount
    ) {}

    /** parentCommentId 가 있으면 답글. 익명 여부는 받지 않는다(항상 익명) */
    public record SuggestionCommentCreateRequest(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            @Size(max = COMMENT_MAX_LENGTH, message = "댓글은 1,000자를 넘을 수 없습니다.")
            String content,
            Long parentCommentId
    ) {}

    public record SuggestionCommentCreateResponse(
            Long commentId,
            Long postId,
            Long parentCommentId,
            LocalDateTime createdAt
    ) {}

    @Schema(description = "건의 댓글 (작성자 정보 없음). 지운 댓글은 자리만 남고 content 가 안내 문구로 바뀐다")
    public record SuggestionCommentDTO(
            Long commentId,
            String content,
            Long parentCommentId,
            LocalDateTime createdAt,
            boolean isDeleted,
            boolean isAuthor
    ) {}
}
