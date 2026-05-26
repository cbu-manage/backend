package com.example.cbumanage.application.entity;

import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_application",
uniqueConstraints = {
        @UniqueConstraint(name = "uq_application_uuid", columnNames = "application_uuid"),
        @UniqueConstraint(name = "uq_application_generation_student",
        columnNames = {"generation", "student_number"})
},
indexes = {
        @Index(name = "idx_application_status", columnList = "status"),
        @Index(name = "idx_application_generation", columnList = "generation")
})
@Getter
@NoArgsConstructor
public class MemberApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_applicatioin_id")
    private Long id;

    @Column(name = "application_uuid", nullable = false, unique = true, length = 36)
    private String applicationUuid;

    @Column(name = "student_number", nullable = false)
    private Long studentNumber;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 32)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AcademicStatus grade;

    @Column(nullable = false, length = 255)
    private String major;

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Column(nullable = false)
    private Long generation;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_field", nullable = false, length = 40)
    private ApplicationField applicationField;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_link", nullable = false, length = 40)
    private RefSource refLink;

    @Column(name = "ref_link_etc", length = 255)
    private String refLinkEtc;

    @Column(name = "can_ot", nullable = false)
    private Boolean canOt;

    @Column(name = "privacy_policy", nullable = false)
    private Boolean privacyPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status;

    @Column(name = "final_decision_reason", columnDefinition = "TEXT")
    private String finalDecisionReason;

    /**
     * 최종 결정권자(회장)의 user_id.
     * user 객체 매핑 대신 ID만 보관
     */
    @Column(name = "final_decided_by")
    private Long finalDecidedBy;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Builder
    private MemberApplication(Long studentNumber, String email, String name, String nickname,
                              AcademicStatus grade, String major, String phoneNumber,
                              Long generation, ApplicationField applicationField,
                              String portfolioUrl, RefSource refLink, String refLinkEtc,
                              Boolean canOt, Boolean privacyPolicy) {
        this.applicationUuid = UUID.randomUUID().toString();
        this.studentNumber = studentNumber;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.grade = grade;
        this.major = major;
        this.phoneNumber = phoneNumber;
        this.generation = generation;
        this.applicationField = applicationField;
        this.portfolioUrl = portfolioUrl;
        this.refLink = refLink;
        this.refLinkEtc = refLinkEtc;
        this.canOt = canOt;
        this.privacyPolicy = privacyPolicy;
        this.status = ApplicationStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    // 지원자가 지원서를 취소하는 경우
    public void cancel() {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("제출 상태가 아닌 신청서는 취소할 수 없습니다.");
        }
        this.status = ApplicationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 지원자가 지원서를 수정하는 경우 (취소된 지원서는 수정 불가)
    public void updateByApplicant(String nickname, String email, String phoneNumber,
                                  String portfolioUrl, Boolean canOt) {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("제출 상태가 아닌 신청서는 수정할 수 없습니다.");
        }
        if (nickname != null) this.nickname = nickname;
        if (email != null) this.email = email;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (portfolioUrl != null) this.portfolioUrl = portfolioUrl;
        if (canOt != null) this.canOt = canOt;
    }

    // 관리자(회장)의 지원서 승인/반려 처리
    public void accept(Long decidedByUserId) {
        this.status = ApplicationStatus.ADMIN_ACCEPTED;
        this.finalDecidedBy = decidedByUserId;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject(Long decidedByUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("불합격 사유는 필수입니다.");
        }
        this.status = ApplicationStatus.ADMIN_REJECTED;
        this.finalDecidedBy = decidedByUserId;
        this.finalDecisionReason = reason;
        this.decidedAt = LocalDateTime.now();
    }

    // 이메일을 발송하여 지원자에게 최종 합/불 결과를 안내한 경우
    // Null이면 미발송
    public void markNotified() {
        this.notifiedAt = LocalDateTime.now();
    }


}
