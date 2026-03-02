package com.example.cbumanage.dto;

import com.example.cbumanage.model.Problem;
import com.example.cbumanage.model.enums.ProblemGrade;
import com.example.cbumanage.model.enums.ProblemStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 문제 상세 페이지 조회 요청 응답에 사용하는 DTO.
 */
@Getter
public class ProblemResponseDTO {

    private final Long problemId;
    private final String authorName;
    private final Long authorGeneration;
    private final List<String> categories;
    private final String platformName;
    private final String languageName;
    private final String title;
    private final String content;
    // private final String inputDescription;
    // private final String outputDescription;
    private final ProblemGrade grade;
    private final String problemUrl;
    private final ProblemStatus problemStatus;
    private final Long viewCount;
    private final Long commentCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public ProblemResponseDTO(Long problemId, String authorName, Long authorGeneration, List<String> categories,
                              String platformName, String languageName, String title, String content, ProblemGrade grade,
                              String problemUrl, ProblemStatus problemStatus, Long viewCount, Long commentCount,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.problemId = problemId;
        this.authorName = authorName;
        this.authorGeneration = authorGeneration;
        this.categories = categories;
        this.platformName = platformName;
        this.languageName = languageName;
        this.title = title;
        this.content = content;
        this.grade = grade;
        this.problemUrl = problemUrl;
        this.problemStatus = problemStatus;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Problem 엔티티를 ProblemResponse DTO로 변환합니다.
     *
     * @param problem 변환할 Problem 엔티티
     * @return 변환된 ProblemResponse DTO
     */
    public static ProblemResponseDTO from(Problem problem, Long commentCount) {
        return ProblemResponseDTO.builder()
                .problemId(problem.getProblemId())
                .authorName(problem.getMember().getName())
                .authorGeneration(problem.getMember().getGeneration())
                .platformName(problem.getPlatform().getName())
                .categories(problem.getCategories().stream()
                        .map(c -> c.getName())
                        .collect(Collectors.toList()))
                .languageName(problem.getLanguage().getName())
                .title(problem.getTitle())
                .content(problem.getContent())
                //.inputDescription(problem.getInputDescription())
                //.outputDescription(problem.getOutputDescription())
                .grade(problem.getGrade())
                .problemUrl(problem.getProblemUrl())
                .problemStatus(problem.getProblemStatus())
                .viewCount(problem.getViewCount())
                .commentCount(commentCount)
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}
