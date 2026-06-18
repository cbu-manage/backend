package com.example.cbumanage.user.dto;

public record MyInfoResponse(
        Long userId,
        String name,
        String email,
        String role,
        Long studentNumber,
        String major,
        String grade,
        Long generation
) {
}
