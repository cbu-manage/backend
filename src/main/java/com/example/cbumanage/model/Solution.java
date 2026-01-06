package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.SolutionStatus;
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
 * 문제 풀이 정보를 저장하는 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "solution")
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer solutionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private CbuMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    /**
     * 소스 코드
     */
    @Lob
    @Column(nullable = false)
    private String codeContent;

    /**
     * 풀이 상태 (풀이 중, 풀이 완료)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolutionStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Solution(CbuMember member, Problem problem, Language language, String codeContent, SolutionStatus status) {
        this.member = member;
        this.problem = problem;
        this.language = language;
        this.codeContent = codeContent;
        this.status = status;
    }
}
