package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.ProblemGrade;
import com.example.cbumanage.model.enums.ProblemStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 코딩 테스트 문제 정보를 저장하는 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "problem")
@SQLDelete(sql = "UPDATE problem SET deleted_at = CURRENT_TIMESTAMP WHERE problem_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Problem {

    /**
     * 문제 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private CbuMember member;

    /**
     ** 알고리즘 키워드 (다중 선택)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "problem_category",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    /**
     * 문제 풀이에 사용한 언어 (단일 선택)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    /**
     * 문제 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 문제 본문 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

   /* *//**
     * 입력에 대한 설명
     *//*
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputDescription;

    *//**
     * 출력에 대한 설명
     *//*
    @Column(columnDefinition = "TEXT", nullable = false)
    private String outputDescription;

*/

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemGrade grade;


    /**
     * 문제 링크
     */
    @Column(length = 500)
    private String problemUrl;

    /**
     * 문제 풀이 상태(해결/미해결)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemStatus problemStatus;

    /**
     * 조회수
     */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    @ColumnDefault("0")
    private Long viewCount = 0L;


    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Builder
    public Problem(CbuMember member, List<Category> categories, Platform platform, Language language, String title, String content,
                   String inputDescription, String outputDescription, ProblemGrade grade, String problemUrl, ProblemStatus problemStatus) {
        this.member = member;
        this.categories = (categories != null) ? categories : new ArrayList<>();
        this.platform = platform;
        this.language = language;
        this.title = title;
        this.content = content;
        //this.inputDescription = inputDescription;
        //this.outputDescription = outputDescription;
        this.grade = grade;
        this.problemUrl = problemUrl;
        this.problemStatus = problemStatus;
        this.viewCount = 0L;
    }

    /**
     * 문제 정보를 수정하는 메소드. null이 아닌 필드만 업데이트.
     */
    public void update(List<Category> categories, Platform platform, Language language,
                       String title, String content, String inputDescription, String outputDescription,
                       ProblemGrade grade, String problemUrl, ProblemStatus problemStatus) {
        if (categories != null) {
            this.categories = categories;
        }
        if (platform != null) {
            this.platform = platform;
        }
        if (language != null) {
            this.language = language;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
//        if (inputDescription != null) {
//            this.inputDescription = inputDescription;
//        }
//        if (outputDescription != null) {
//            this.outputDescription = outputDescription;
//        }
        if (grade != null) {
            this.grade = grade;
        }
        if (problemUrl != null) {
            this.problemUrl = problemUrl;
        }
        if (problemStatus != null) {
            this.problemStatus = problemStatus;
        }
    }
}
