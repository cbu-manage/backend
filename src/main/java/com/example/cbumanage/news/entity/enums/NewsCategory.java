package com.example.cbumanage.news.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소식 게시판 내부 카테고리")
public enum NewsCategory {
    @Schema(description = "공지")
    NOTICE,

    @Schema(description = "이벤트")
    EVENT,

    @Schema(description = "뉴스레터")
    NEWSLETTER,

    @Schema(description = "IT소식")
    IT_NEWS
}
