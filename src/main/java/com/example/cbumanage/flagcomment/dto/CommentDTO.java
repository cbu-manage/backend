package com.example.cbumanage.flagcomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CommentDTO {

    public record FlagCommentCreateRequest(
        @NotBlank(message = "신고 사유는 필수입니다.")
        @Size(max = 500, message = "신고 사유는 500자를 넘을 수 없습니다.")
        String content
    ){}

    public record FlagCommentCreateResponse(
            Long flagCommentId,
            Long commentId,
            Long authorId,
            String content,
            LocalDateTime createdAt
    ){}

    public record FlagCommentInfoDTO(
            Long flagCommentId,
            String content,
            LocalDateTime createdAt,

            Long targetCommentId,
            String targetCommentContent,

            Long targetUserId,
            String targetUserName,
            Long targetUserGeneration,

            Long authorId,
            String authorName,
            Long authorGeneration
    ){}

    public record FlagCommentPreviewDTO(
            Long flagCommentId,
            String content,
            LocalDateTime createdAt,

            Long targetCommentId,
            String targetCommentContent,

            Long authorId,
            String authorName,
            Long authorGeneration
    ){}
}
