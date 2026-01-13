package com.example.cbumanage.dto;

import com.example.cbumanage.model.Language;
import lombok.Builder;
import lombok.Getter;

/**
 * 언어 정보 응답에 사용하는 DTO입니다.
 */
@Getter
public class LanguageResponseDTO {
    private final Integer id;
    private final String name;

    @Builder
    public LanguageResponseDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static LanguageResponseDTO from(Language language) {
        return LanguageResponseDTO.builder()
                .id(language.getLanguageId())
                .name(language.getName())
                .build();
    }
}
