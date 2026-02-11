package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.ProblemGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 생성할 때 사용하는 DTO.
 */
@Getter
@NoArgsConstructor
public class ProblemCreateRequestDTO {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    @Schema(description = "GET /categories에서 카테고리 ID 조회", example = "1")
    private Integer categoryId;

    @NotNull(message = "플랫폼 ID는 필수입니다.")
    @Schema(description = "GET /platforms에서 플랫폼 ID 조회", example = "1")
    private Integer platformId;

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    private String inputDescription;

    private String outputDescription;

    @NotNull(message = "문제 난이도는 필수입니다.")
    @Schema(description = "문제 난이도", example = "SILVER",
            allowableValues = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "RUBY"})
    private ProblemGrade grade;
}
