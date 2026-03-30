package com.example.cbumanage.problem.dto;

import com.example.cbumanage.problem.entity.enums.ProblemGrade;
import com.example.cbumanage.problem.entity.enums.ProblemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 문제 수정 DTO.
 */
@Getter
@NoArgsConstructor
public class ProblemUpdateRequestDTO {

    private List<Integer> categoryIds;
    private Integer platformId;
    private Integer languageId;
    private String title;
    private String content;
    // private String inputDescription;
    // private String outputDescription;
    private ProblemGrade grade;
    private String problemUrl;
    private ProblemStatus problemStatus;
}
