package com.example.cbumanage.dto;

import com.example.cbumanage.model.Problem;
import com.example.cbumanage.model.enums.ProblemGrade;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문제 정보 응답에 사용하는 DTO.
 */
@Getter
public class ProblemResponseDTO {

    private final Long problemId;
    private final String authorName;
    private final String categoryName;
    private final String platformName;
    private final String title;
    private final String content;
    private final String inputDescription;
    private final String outputDescription;
    private final ProblemGrade grade;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public ProblemResponseDTO(Long problemId, String authorName, String categoryName, String platformName, String title, String content, String inputDescription, String outputDescription, ProblemGrade grade, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.problemId = problemId;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.platformName = platformName;
        this.title = title;
        this.content = content;
        this.inputDescription = inputDescription;
        this.outputDescription = outputDescription;
        this.grade = grade;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Problem 엔티티를 ProblemResponse DTO로 변환합니다.
     *
     * @param problem 변환할 Problem 엔티티
     * @return 변환된 ProblemResponse DTO
     */
    public static ProblemResponseDTO from(Problem problem) {
        return ProblemResponseDTO.builder()
                .problemId(problem.getProblemId())
                .authorName(problem.getMember().getName())
                .categoryName(problem.getCategory().getName())
                .platformName(problem.getPlatform().getName())
                .title(problem.getTitle())
                .content(problem.getContent())
                .inputDescription(problem.getInputDescription())
                .outputDescription(problem.getOutputDescription())
                .grade(problem.getGrade())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}
