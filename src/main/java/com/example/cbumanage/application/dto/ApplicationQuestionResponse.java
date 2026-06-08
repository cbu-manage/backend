package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.ApplicationQuestion;

public record ApplicationQuestionResponse(
        String questionUuid,
        String question,
        String description,
        Boolean isRequired,
        Integer sortOrder
) {
    public static ApplicationQuestionResponse from(ApplicationQuestion question) {
        return new ApplicationQuestionResponse(
                question.getQuestionUuid(),
                question.getQuestion(),
                question.getDescription(),
                question.getIsRequired(),
                question.getSortOrder()
        );
    }
}
