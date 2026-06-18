package com.example.cbumanage.api.v2.dto;

import com.example.cbumanage.user.dto.MyInfoResponse;

import java.util.UUID;

public record MyInfoV2Response(
        UUID userUuid,
        String name,
        String email,
        String role,
        Long studentNumber,
        String major,
        String grade,
        Long generation
) {
    public static MyInfoV2Response from(MyInfoResponse response, UUID userUuid) {
        return new MyInfoV2Response(
                userUuid,
                response.name(),
                response.email(),
                response.role(),
                response.studentNumber(),
                response.major(),
                response.grade(),
                response.generation()
        );
    }
}
