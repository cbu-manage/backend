package com.example.cbumanage.application.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_answer",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_application_answer",
                        columnNames = {"application_id", "application_question_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_answer_id")
    private Long id;

    /**
     * member_application.member_application_id 참조.
     */
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    /**
     * application_question.application_question_id 참조.
     * ID만 보관. 질문 본문은 question_snapshot으로 확인
     */
    @Column(name = "application_question_id", nullable = false)
    private Long applicationQuestionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    /**
     * 답변 시점의 질문 본문 스냅샷.
     * application_question.question은 운영진이 수정 가능하므로,
     * 답변 당시 어떤 질문이었는지 보존하기 위해 별도 저장.
     */
    @Column(name = "question_snapshot", nullable = false, length = 500)
    private String questionSnapshot;

    @Builder
    private ApplicationAnswer(Long applicationId, Long applicationQuestionId,
                              String answer, String questionSnapshot) {
        this.applicationId = applicationId;
        this.applicationQuestionId = applicationQuestionId;
        this.answer = answer;
        this.questionSnapshot = questionSnapshot;
    }

    public void updateAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("답변은 비어 있을 수 없습니다.");
        }
        this.answer = answer;
    }
}
