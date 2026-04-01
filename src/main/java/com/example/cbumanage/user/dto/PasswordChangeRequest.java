package com.example.cbumanage.user.dto;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {
}
