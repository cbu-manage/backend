package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.ApplicationQuestion;

public record ApplicationQuestionResponse(
        String questionUuid,
        String type,
        String question,
        String description,
        Boolean isRequired,
        Integer sortOrder,
        Long version
) {
    public static ApplicationQuestionResponse from(ApplicationQuestion question) {
        return new ApplicationQuestionResponse(
                question.getQuestionUuid(),
                question.getType(),
                question.getQuestion(),
                question.getDescription(),
                question.getIsRequired(),
                question.getSortOrder(),
                question.getVersion()
        );
    }
}
