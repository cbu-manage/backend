package com.example.cbumanage.user.dto;

import com.example.cbumanage.candidate.entity.SuccessCandidate;
import com.example.cbumanage.user.entity.Role;

public record UserCreateRequest(
        String name,
        Long studentNumber,
        Role role,
        String phoneNumber,
        String major,
        Long generation,
        String grade
) {
    public static UserCreateRequest from(SuccessCandidate candidate, Long generation) {
        return new UserCreateRequest(
                candidate.getName(),
                candidate.getStudentNumber(),
                Role.ROLE_USER,
                candidate.getPhoneNumber(),
                candidate.getMajor(),
                generation,
                candidate.getGrade()
        );
    }
}
