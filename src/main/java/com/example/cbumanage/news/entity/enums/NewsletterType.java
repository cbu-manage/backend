package com.example.cbumanage.news.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "뉴스레터 세부 분류. category가 NEWSLETTER인 소식에만 설정됩니다.")
public enum NewsletterType {
    @Schema(description = "주간")
    WEEKLY,

    @Schema(description = "특집")
    SPECIAL,

    @Schema(description = "공지")
    NOTICE
}
