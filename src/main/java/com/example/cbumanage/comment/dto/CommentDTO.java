package com.example.cbumanage.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CommentDTO {

    public record CommentInfoDTO(
            Long commentId,
            Long userId,
            Long generation,
            String userName,
            String content,
            Long parentCommentId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
    /* 댓글 본문 길이 한도. 엔티티 컬럼 길이와 같아야 한다 — 넘치면 DB 예외가 409 로 둔갑한다 */
    public static final int CONTENT_MAX_LENGTH = 1000;

    public record CommentCreateRequestDTO(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            @Size(max = CONTENT_MAX_LENGTH, message = "댓글은 1,000자를 넘을 수 없습니다.")
            String content) {}

    public record CommentCreateResponseDTO(
            Long commentId,
            Long userId,
            Long postId,
            String content,
            LocalDateTime createdAt
    ) {}

    public record CommentUpdateRequestDTO(
            @NotBlank(message = "댓글 내용은 필수입니다.")
            @Size(max = CONTENT_MAX_LENGTH, message = "댓글은 1,000자를 넘을 수 없습니다.")
            String content) {}

    public record ReplyCreateRequestDTO(
            @NotBlank(message = "답글 내용은 필수입니다.")
            @Size(max = CONTENT_MAX_LENGTH, message = "답글은 1,000자를 넘을 수 없습니다.")
            String content) {}

    public record ReplyCreateResponseDTO(
            Long replyId,
            Long userId,
            Long parentId,
            LocalDateTime createdAt,
            String content
    ) {}

    public record CommentAnonymousInfoDTO(
            Long commentId,
            String content,
            LocalDateTime createdAt,
            Long parentCommentId
    ){}

    @io.swagger.v3.oas.annotations.media.Schema(
            description = "자유게시판 댓글 응답의 공통 타입입니다. isAnonymous 값에 따라 반환 스키마가 달라집니다.",
            oneOf = {CommentDTO.FreeBoardCommentInfoDTO.class, CommentDTO.FreeBoardCommentAnonymousInfoDTO.class},
            discriminatorProperty = "isAnonymous",
            discriminatorMapping = {
                    @io.swagger.v3.oas.annotations.media.DiscriminatorMapping(value = "false", schema = CommentDTO.FreeBoardCommentInfoDTO.class),
                    @io.swagger.v3.oas.annotations.media.DiscriminatorMapping(value = "true",  schema = CommentDTO.FreeBoardCommentAnonymousInfoDTO.class)
            }
    )
    public interface FreeBoardCommentResponse {}

    @io.swagger.v3.oas.annotations.media.Schema(description = "자유게시판 실명 댓글 정보")
    public record FreeBoardCommentInfoDTO(
            Long commentId,
            Long userId,
            Long generation,
            String userName,
            String content,
            Long parentCommentId,
            LocalDateTime createdAt,
            boolean isAnonymous
    ) implements FreeBoardCommentResponse {}

    @io.swagger.v3.oas.annotations.media.Schema(description = "자유게시판 익명 댓글 정보")
    public record FreeBoardCommentAnonymousInfoDTO(
            Long commentId,
            String content,
            Long parentCommentId,
            LocalDateTime createdAt,
            boolean isAnonymous,
            @io.swagger.v3.oas.annotations.media.Schema(
                    description = "요청자가 작성자인지. 익명 댓글에서 수정·삭제 노출을 판단하는 유일한 근거다.")
            boolean isAuthor
    ) implements FreeBoardCommentResponse {}
}
