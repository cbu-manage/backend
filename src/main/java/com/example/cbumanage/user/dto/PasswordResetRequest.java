package com.example.cbumanage.user.dto;

public record PasswordResetRequest(
        Long studentNumber,
        String email,
        String authCode,
        String newPassword
) {
}
