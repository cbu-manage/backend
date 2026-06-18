package com.example.cbumanage.api.v2.dto;

import com.example.cbumanage.user.entity.Role;

import java.util.UUID;

public record MemberUpdateV2DTO(
        UUID userUuid,
        Role role,
        String name,
        String phoneNumber,
        String major,
        String grade,
        Long studentNumber,
        Long generation,
        String note,
        String kakaoNoti,
        String kakaoChat
) {
}
