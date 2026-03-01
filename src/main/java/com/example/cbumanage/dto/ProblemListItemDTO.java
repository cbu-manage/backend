package com.example.cbumanage.dto;

import com.example.cbumanage.model.Problem;
import com.example.cbumanage.model.enums.ProblemGrade;
import com.example.cbumanage.model.enums.ProblemStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 문제 목록 페이지에서 개별 항목을 나타내는 DTO.
 */
@Getter
public class ProblemListItemDTO {
    private final Long problemId;
    private final String platformName;
    private final List<String> categories;
    private final String languageName;
    private final String title;
    private final String authorName;
    private final ProblemGrade grade;
    private final ProblemStatus problemStatus;
    private final Long viewCount;
    private final Long commentCount;

    @Builder
    public ProblemListItemDTO(Long problemId, String platformName, List<String> categories, String languageName,
                              String title, String authorName, ProblemGrade grade, ProblemStatus problemStatus,
                              Long viewCount, Long commentCount) {
        this.problemId = problemId;
        this.platformName = platformName;
        this.categories = categories;
        this.languageName = languageName;
        this.title = title;
        this.authorName = authorName;
        this.grade = grade;
        this.problemStatus = problemStatus;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
    }

    public static ProblemListItemDTO from(Problem problem, Long commentCount) {
        return ProblemListItemDTO.builder()
                .problemId(problem.getProblemId())
                .platformName(problem.getPlatform().getName())
                .categories(problem.getCategories().stream()
                        .map(c -> c.getName())
                        .collect(Collectors.toList()))
                .languageName(problem.getLanguage().getName())
                .title(problem.getTitle())
                .authorName(problem.getMember().getName())
                .grade(problem.getGrade())
                .problemStatus(problem.getProblemStatus())
                .viewCount(problem.getViewCount())
                .commentCount(commentCount)
                .build();
    }
}
