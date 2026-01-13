package com.example.cbumanage.dto;

import com.example.cbumanage.model.enums.ProblemGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문제 수정 DTO.
 */
@Getter
@NoArgsConstructor
public class ProblemUpdateRequestDTO {

    private Integer categoryId;
    private Integer platformId;
    private String title;
    private String content;
    private String inputDescription;
    private String outputDescription;
    private ProblemGrade grade;
}
