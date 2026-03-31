package com.example.cbumanage.user.dto;

public record UserSignUpRequest(
        String email,
        String password,
        String name,
        Long studentNumber,
        String nickName
) {
}
