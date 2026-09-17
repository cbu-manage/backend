package com.example.cbumanage.suggestion.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "처리 상태: OPEN=미해결, RESOLVED=해결")
public enum SuggestionStatus {
    OPEN,
    RESOLVED
}
