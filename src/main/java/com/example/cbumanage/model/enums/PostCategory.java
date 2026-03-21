package com.example.cbumanage.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum PostCategory {

    @Schema(description = "스터디 모집 게시판")
    STUDY(1),

    @Schema(description = "프로젝트 모집 게시판")
    PROJECT(2),

    @Schema(description = "보고서 업로드 게시판")
    REPORT(7);

    private final int value;

    PostCategory(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
