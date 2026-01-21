package com.example.cbumanage.model.enums;

import lombok.Getter;

@Getter
public enum ProjectFieldType {
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    DEV("개발"),
    PLANNING("기획"),
    DESIGN("디자인"),
    ETC("기타");

    private final String description;

    ProjectFieldType(String description) {
        this.description = description;
    }

}