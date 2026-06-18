package com.example.cbumanage.user.dto;

import java.util.UUID;

public record UserLoginResponse(
        UUID userUuid,
        String name,
        String email,
        String role
) {
}
