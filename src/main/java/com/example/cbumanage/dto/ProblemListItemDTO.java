package com.example.cbumanage.dto;

import com.example.cbumanage.model.Problem;
import com.example.cbumanage.model.enums.ProblemGrade;
import lombok.Builder;
import lombok.Getter;

/**
 * 문제 목록의 개별 항목을 나타내는 DTO.
 */
@Getter
public class ProblemListItemDTO {
    private final Integer problemId;
    private final String platformName;
    private final String categoryName;
    private final String title;
    private final String authorName;
    private final ProblemGrade grade;
    // 추후 Solution 기능 구현 시 해결 여부, 사용 언어, 댓글 수 추가

    @Builder
    public ProblemListItemDTO(Integer problemId, String platformName, String categoryName, String title, String authorName, ProblemGrade grade) {
        this.problemId = problemId;
        this.platformName = platformName;
        this.categoryName = categoryName;
        this.title = title;
        this.authorName = authorName;
        this.grade = grade;
    }

    public static ProblemListItemDTO from(Problem problem) {
        return ProblemListItemDTO.builder()
                .problemId(problem.getProblemId())
                .platformName(problem.getPlatform().getName())
                .categoryName(problem.getCategory().getName())
                .title(problem.getTitle())
                .authorName(problem.getMember().getName())
                .grade(problem.getGrade())
                .build();
    }
}
