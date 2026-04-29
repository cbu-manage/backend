package com.example.cbumanage.post.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {

    @Schema(description = "스터디 모집 게시판")
    STUDY(1),

    @Schema(description = "프로젝트 모집 게시판")
    PROJECT(2),

    @Schema(description = "코딩테스트 문제 게시판")
    PROBLEM(5),

    @Schema(description = "자료실 게시판")
    RESOURCE(6),

    @Schema(description = "보고서 업로드 게시판")
    REPORT(7),

    @Schema(description = "자유(익명) 게시판")
    FREEBOARD(8);

    private final int value;
}
