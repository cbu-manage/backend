package com.example.cbumanage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 자료방 게시글 생성 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResourceCreateRequestDTO {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "자료 제목", example = "2024 카카오 코딩테스트 문제 모음")
    private String title;

    @NotBlank(message = "링크는 필수입니다.")
    @Schema(description = "자료 외부 링크", example = "https://programmers.co.kr/learn/challenges")
    private String link;
}
