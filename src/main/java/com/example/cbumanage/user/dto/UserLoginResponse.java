package com.example.cbumanage.user.dto;

public record UserLoginResponse(
        Long userId,
        String name,
        String email,
        String role
) {
}
