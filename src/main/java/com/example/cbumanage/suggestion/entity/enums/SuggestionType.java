package com.example.cbumanage.suggestion.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건의 종류: BUG=버그 리포트, SUGGESTION=기능 건의·불편 사항")
public enum SuggestionType {
    BUG,
    SUGGESTION
}
