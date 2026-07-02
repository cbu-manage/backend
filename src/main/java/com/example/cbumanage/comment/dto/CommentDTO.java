package com.example.cbumanage.comment.dto;

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
    public record CommentCreateRequestDTO(String content) {}

    public record CommentCreateResponseDTO(
            Long commentId,
            Long userId,
            Long postId,
            String content,
            LocalDateTime createdAt
    ) {}

    public record CommentUpdateRequestDTO(String content) {}

    public record ReplyCreateRequestDTO(String content) {}

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
            boolean isAnonymous
    ) implements FreeBoardCommentResponse {}
}
