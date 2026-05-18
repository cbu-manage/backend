package com.example.cbumanage.flagcomment.eto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class CommentDTO {

    public record FlagCommentCreateRequest(
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
