package com.example.cbumanage.user.dto;

import com.example.cbumanage.user.entity.Role;

import java.util.UUID;

public record UserLoginResponse(
        UUID userUuid,
        String userName,
        String password,
        Role role
) {
}
