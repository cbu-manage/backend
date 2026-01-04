package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.ProblemGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 생성을 요청할 때 사용하는 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProblemCreateRequestDTO {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Integer categoryId;

    @NotNull(message = "플랫폼 ID는 필수입니다.")
    private Integer platformId;

    @NotBlank(message = "문제 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문제 내용은 필수입니다.")
    private String content;

    private String inputDescription;

    private String outputDescription;

    @NotNull(message = "문제 난이도는 필수입니다.")
    private ProblemGrade grade;
}
