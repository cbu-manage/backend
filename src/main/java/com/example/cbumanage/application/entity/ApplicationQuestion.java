package com.example.cbumanage.application.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "기수별 신청서 질문 템플릿")
@Entity
@Table(name = "application_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationQuestion {

    @Schema(description = "질문 고유 ID (PK)")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_question_id")
    private Long id;

    @Schema(description = "질문 UUID (외부 노출용)", example =
            "550e8400-e29b-41d4-a716-446655440000")
    @Column(name = "question_uuid", nullable = false, unique = true, length = 36)
    private String questionUuid;

    @Schema(description = "기수 번호", example = "12")
    @Column(nullable = false)
    private Long generation;

    @Schema(description = "질문 본문", example = "지원 동기를 작성해 주세요.")
    @Column(nullable = false, length = 255)
    private String question;

    @Schema(description = "질문 부가 설명 (선택)", example = "500자 이내로 작성해 주세요.")
    @Column(length = 500)
    private String description;

    @Schema(description = "필수 답변 여부", example = "true")
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Schema(description = "질문 노출 순서 (오름차순 정렬)", example = "1")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Schema(description = "생성일시")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "최종 수정일시")
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Schema(description = "삭제일시 (소프트 딜리트)")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    /**
     *
     * @param generation 기수 번호
     * @param question 질문 내용
     * @param description 부연 설명(선택)
     * @param isRequired 필수 여부
     * @param sortOrder 순서(오름차순 정렬)
     */
    @Builder
    public ApplicationQuestion(Long generation, String question,
                               String description, Boolean isRequired, Integer sortOrder) {
        this.questionUuid = UUID.randomUUID().toString();
        this.generation = generation;
        this.question = question;
        this.description = description;
        this.isRequired = isRequired != null ? isRequired : true;
        this.sortOrder = sortOrder;
    }

    public void update(String question, String description, Boolean isRequired, Integer sortOrder) {
        if (question != null) this.question = question;
        if (description != null) this.description = description;
        if (isRequired != null) this.isRequired = isRequired;
        if (sortOrder != null) this.sortOrder = sortOrder;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
