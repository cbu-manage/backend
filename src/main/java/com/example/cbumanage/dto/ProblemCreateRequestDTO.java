package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.ProblemGrade;
import com.example.cbumanage.model.enums.ProblemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 문제 생성할 때 사용하는 DTO.
 */
@Getter
@NoArgsConstructor
public class ProblemCreateRequestDTO {

    @NotNull(message = "카테고리(키워드)는 최소 1개 이상 선택해야 합니다..")
    @Schema(description = "GET /categories에서 카테고리 ID 조회", example = "[1, 2]")
    private List<Integer> categoryIds;

    @NotNull(message = "플랫폼 선택은 필수입니다.")
    @Schema(description = "GET /platforms에서 플랫폼 ID 조회", example = "1")
    private Integer platformId;

    @NotNull(message = "언어 선택은 필수입니다.")
    @Schema(description = "GET /languages에서 언어 ID 조회", example = "1")
    private Integer languageId;

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    // private String inputDescription;

    // private String outputDescription;

    @NotNull(message = "문제 난이도는 필수입니다.")
    @Schema(description = "문제 난이도", example = "SILVER",
            allowableValues = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "RUBY"})
    private ProblemGrade grade;

    @NotBlank(message = "문제 링크는 필수입니다.")
    @Schema(description = "외부 문제 링크를 넣습니다", example = "https://www.bacmic.~~")
    private String problemUrl;

    @NotNull(message = "풀이 상태는 필수입니다.")
    @Schema(description = "풀이 상태", example = "UNSOLVED", allowableValues = {"UNSOLVED", "SOLVED"})
    private ProblemStatus problemStatus;
}
