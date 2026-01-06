package com.example.cbumanage.dto;

import com.example.cbumanage.model.Category;
import lombok.Builder;
import lombok.Getter;

/**
 * 카테고리 정보 응답에 사용하는 DTO입니다.
 */
@Getter
public class CategoryResponseDTO {
    private final Integer id;
    private final String name;

    @Builder
    public CategoryResponseDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static CategoryResponseDTO from(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getCategoryId())
                .name(category.getName())
                .build();
    }
}
