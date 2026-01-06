package com.example.cbumanage.dto;

import com.example.cbumanage.model.Platform;
import lombok.Builder;
import lombok.Getter;

/**
 * 플랫폼 정보 응답에 사용하는 DTO입니다.
 */
@Getter
public class PlatformResponseDTO {
    private final Integer id;
    private final String name;

    @Builder
    public PlatformResponseDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static PlatformResponseDTO from(Platform platform) {
        return PlatformResponseDTO.builder()
                .id(platform.getPlatformId())
                .name(platform.getName())
                .build();
    }
}
