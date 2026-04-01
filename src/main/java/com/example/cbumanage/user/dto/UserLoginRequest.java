package com.example.cbumanage.user.dto;

public record UserLoginRequest(
        Long studentNumber,
        String password
) {
}
