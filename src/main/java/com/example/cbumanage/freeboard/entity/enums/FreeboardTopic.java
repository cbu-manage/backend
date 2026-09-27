package com.example.cbumanage.freeboard.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자유게시판 말머리. 게시판 종류를 뜻하는 PostCategory 와는 다른 축이다.
 * 이름이 겹치면 목록 필터가 게시판 번호와 섞이므로 topic 으로 부른다.
 */
@Getter
@RequiredArgsConstructor
public enum FreeboardTopic {

    @Schema(description = "일상")
    DAILY("일상"),

    @Schema(description = "질문")
    QUESTION("질문"),

    @Schema(description = "잡담")
    CHAT("잡담"),

    @Schema(description = "홍보")
    PROMOTION("홍보");

    private final String label;
}
