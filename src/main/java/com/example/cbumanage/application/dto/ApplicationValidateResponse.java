package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;

public record ApplicationValidateResponse(
        Long applicationId,
        String name,
        String nickName,
        String grade,
        String major,
        String phoneNumber,
        Long studentNumber
) {
    public static ApplicationValidateResponse from(MemberApplication application) {
        return new ApplicationValidateResponse(
                application.getId(),
                application.getName(),
                application.getNickname(),
                application.getGrade().name(),
                application.getMajor(),
                application.getPhoneNumber(),
                application.getStudentNumber()
        );
    }
}
