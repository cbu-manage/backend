package com.example.cbumanage.application.entity.enums;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_vote",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_vote",
        columnNames = {"application_member_id", "voter_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long id;

    @Column(name = "member_application_id", nullable = false)
    private Long memberApplicationId;

    // 투표한 운영진의 user_id
    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoteResult decision;

    // FAIL인 경우 필수, PASS인 경우 NULL 허용
    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private ApplicationVote(Long memberApplicationId, Long voterId,
                            VoteResult decision, String reason) {
        validateReason(decision, reason);
        this.memberApplicationId = memberApplicationId;
        this.voterId = voterId;
        this.decision = decision;
        this.reason = reason;
    }

    public void change(VoteResult decision, String reason) {
        validateReason(decision, reason);
        this.decision = decision;
        this.reason = reason;
    }

    private static void validateReason(VoteResult decision, String reason) {
        if (decision == VoteResult.FAIL && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("FAIL 투표는 사유가 필수입니다.");
        }
    }
}
