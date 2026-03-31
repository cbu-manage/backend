package com.example.cbumanage.user.dto;

public record UserLoginResponse(
        String name,
        String email,
        String role
) {
}
