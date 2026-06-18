package com.example.cbumanage.flagpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class FlagPostDTO {

    public record FlagPostCreateRequest(
            String content
    ){}

    public record FlagPostCreateResponse(
            Long flagPostId,
            Long postId,
            Long authorId,
            String content,
            LocalDateTime createdAt
    ){}

    //신고 정보 확인
    @Schema(description = "신고 정보 DTO")
    public record FlagPostInfoDTO(
            @Schema(description = "신고 id")
            Long flagPostId,
            @Schema(description = "신고 내용")
            String content,
            LocalDateTime createdAt,

            @Schema(description = "신고 대상 게시글 ID")
            Long targetPostId,
            @Schema(description = "신고 대상 게시글 제목")
            String targetPostTitle,
            @Schema(description = "신고 대상 게시글 내용")
            String targetPostContent,

            @Schema(description = "신고 대상 게시글 작성 유저ID")
            Long targetUserId,
            @Schema(description = "신고 대상 게시글 작성 유저 이름")
            String targetUserName,
            @Schema(description = "신고 대상 게시글 작성 유저 기수")
            Long targetUserGeneration,

            @Schema(description = "신고자 Id")
            Long authorId,
            @Schema(description = "신고자 이름")
            String authorName,
            @Schema(description = "신고자 기수")
            Long authorGeneration
    ){}

    public record FlagPostPreviewDTO(
            @Schema(description = "신고 id")
            Long flagPostId,
            @Schema(description = "신고 내용")
            String content,
            LocalDateTime createdAt,

            @Schema(description = "신고대상게시글ID")
            Long targetPostId,
            @Schema(description = "신고대상게시글이름")
            String targetPostTitle,

            @Schema(description = "신고자ID")
            Long authorId,
            @Schema(description = "신고자이름")
            String authorName,
            @Schema(description = "신고자 기수")
            Long authorGeneration

    ){}









}
