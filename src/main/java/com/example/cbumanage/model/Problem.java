package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.ProblemGrade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 코딩 테스트 문제 정보를 저장하는 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "problem")
public class Problem {

    /**
     * 문제 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private CbuMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

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

    /**
     * 입력에 대한 설명
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputDescription;

    /**
     * 출력에 대한 설명
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String outputDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemGrade grade;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Problem(CbuMember member, Category category, Platform platform, String title, String content, String inputDescription, String outputDescription, ProblemGrade grade) {
        this.member = member;
        this.category = category;
        this.platform = platform;
        this.title = title;
        this.content = content;
        this.inputDescription = inputDescription;
        this.outputDescription = outputDescription;
        this.grade = grade;
    }

    /**
     * 문제 정보를 수정하는 메소드. null이 아닌 필드만 업데이트.
     *
     * @param category 수정한 카테고리
     * @param platform 수정한 플랫폼
     * @param title 수정한 제목
     * @param content 수정한 내용
     * @param inputDescription 수정한 입력 설명
     * @param outputDescription 수정한 출력 설명
     * @param grade 수정한 난이도
     */
    public void update(Category category, Platform platform, String title, String content, String inputDescription, String outputDescription, ProblemGrade grade) {
        if (category != null) {
            this.category = category;
        }
        if (platform != null) {
            this.platform = platform;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (inputDescription != null) {
            this.inputDescription = inputDescription;
        }
        if (outputDescription != null) {
            this.outputDescription = outputDescription;
        }
        if (grade != null) {
            this.grade = grade;
        }
    }
}
