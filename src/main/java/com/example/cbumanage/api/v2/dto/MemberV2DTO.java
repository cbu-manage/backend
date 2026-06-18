package com.example.cbumanage.api.v2.dto;

import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;

import java.util.UUID;

public record MemberV2DTO(
        UUID userUuid,
        Role role,
        String name,
        String phoneNumber,
        String major,
        String grade,
        Long studentNumber,
        Long generation,
        String note,
        Boolean due,
        String email
) {
    public static MemberV2DTO from(User user) {
        return new MemberV2DTO(
                user.getUserUuid(),
                user.getRole(),
                user.getName(),
                user.getPhoneNumber(),
                user.getMajor(),
                user.getGrade(),
                user.getStudentNumber(),
                user.getGeneration(),
                user.getNote(),
                user.getMemberStatus() == MemberStatus.ACTIVE,
                user.getEmail()
        );
    }
}
