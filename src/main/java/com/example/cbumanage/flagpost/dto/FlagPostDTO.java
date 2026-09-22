package com.example.cbumanage.flagpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class FlagPostDTO {

    public record FlagPostCreateRequest(
            @NotBlank(message = "신고 사유는 필수입니다.")
            @Size(max = 500, message = "신고 사유는 500자를 넘을 수 없습니다.")
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
            @Schema(description = "신고 대상 게시글 카테고리(PostCategory value) — 원문 링크용")
            int targetPostCategory,

            @Schema(description = "신고 대상 게시글 작성 유저ID — 익명 글이면 null")
            Long targetUserId,
            @Schema(description = "신고 대상 게시글 작성 유저 이름 — 익명 글이면 null")
            String targetUserName,
            @Schema(description = "신고 대상 게시글 작성 유저 기수 — 익명 글이면 null")
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
            @Schema(description = "신고 대상 게시글 카테고리(PostCategory value) — 원문 링크용")
            int targetPostCategory,

            @Schema(description = "신고자ID")
            Long authorId,
            @Schema(description = "신고자이름")
            String authorName,
            @Schema(description = "신고자 기수")
            Long authorGeneration

    ){}









}
