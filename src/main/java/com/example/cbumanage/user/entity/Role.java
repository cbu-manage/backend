package com.example.cbumanage.user.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    ROLE_USER(1),
    ROLE_MANAGER(2),
    ROLE_ADMIN(3);

    public final int value;
}
