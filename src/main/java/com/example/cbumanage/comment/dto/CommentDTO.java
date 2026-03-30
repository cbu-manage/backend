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

    public record ReplyInfoDTO(
            Long id,
            Long userId,
            String userName,
            Long generation,
            String content,
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
}
