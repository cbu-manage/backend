package com.example.cbumanage.application.entity;

import com.example.cbumanage.application.entity.enums.AcademicStatus;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.RefSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원가입 지원서 엔티티")
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
    @Schema(description = "지원서 고유 ID (PK)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_application_id")
    private Long id;

    @Schema(description = "지원서 UUID (외부 노출용)")
    @Column(name = "application_uuid", nullable = false, unique = true, length = 36)
    private String applicationUuid;

    @Schema(description = "학번")
    @Column(name = "student_number", nullable = false)
    private Long studentNumber;

    @Schema(description = "이메일 주소")
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 32)
    private String name;

    @Schema(description = "본인확인용 닉네임(비속어 필터 적용 필요)")
    @Column(nullable = false, length = 32)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Schema(description = "학년", example = "FRESHMAN, SOPHOMORE, JUNIOR, SENIOR, GRADUATE")
    @Column(nullable = false, length = 40)
    private AcademicStatus grade;

    @Column(nullable = false, length = 255)
    private String major;

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Schema(description = "기수(자동 증가 알고리즘 적용 필요)")
    @Column(nullable = false)
    private Long generation;

    @Schema(description = "희망하는 분야", example = "STUDY, DEV, DESIGN, PLAN")
    @Enumerated(EnumType.STRING)
    @Column(name = "application_field", nullable = false, length = 40)
    private ApplicationField applicationField;

    @Schema(description = "포트폴리오나 깃허브가 있다면 작성 (선택)")
    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Schema(description = "알게 된 경로 (선택)", example = "FRIEND, SNS,ETC")
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_source", nullable = false, length = 40)
    private RefSource refSource;

    @Schema(description = "알게 된 경로가 '기타'인 경우 작성")
    @Column(name = "ref_link_etc", length = 255)
    private String refLinkEtc;

    @Schema(description = "오티 참석 여부")
    @Column(name = "can_ot", nullable = false)
    private Boolean canOt;

    @Schema(description = "환영회 참석 여부")
    @Column(name = "can_welcome", nullable = false)
    private Boolean canWelcome;

    @Schema(description = "개인정보 수집 동의 여부")
    @Column(name = "privacy_policy", nullable = false)
    private Boolean privacyPolicy;

    @Schema(description = "신청서의 상태", example = "SUBMITTED, CANCELLED, ALL_REJECT, ETC")
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
                              String portfolioUrl, RefSource refSource, String refLinkEtc,
                              Boolean canOt, Boolean canWelcome, Boolean privacyPolicy) {
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
        this.refSource = refSource;
        this.refLinkEtc = refLinkEtc;
        this.canOt = canOt;
        this.canWelcome = canWelcome;
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
                                  String portfolioUrl, Boolean canOt, Boolean canWelcome) {
        if (this.status != ApplicationStatus.SUBMITTED) {
            throw new IllegalStateException("제출 상태가 아닌 신청서는 수정할 수 없습니다.");
        }
        if (nickname != null) this.nickname = nickname;
        if (email != null) this.email = email;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (portfolioUrl != null) this.portfolioUrl = portfolioUrl;
        if (canOt != null) this.canOt = canOt;
        if (canWelcome != null) this.canWelcome = canWelcome;
    }

    // 관리자(회장)의 지원서 승인/반려 처리
    public void accept(Long decidedByUserId) {
        this.status = ApplicationStatus.ADMIN_ACCEPTED;
        this.finalDecidedBy = decidedByUserId;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject(Long decidedByUserId, String reason) {
        this.status = ApplicationStatus.ADMIN_REJECTED;
        this.finalDecidedBy = decidedByUserId;
        this.finalDecisionReason = reason;
        this.decidedAt = LocalDateTime.now();
    }

    public void hold(Long decidedByUserId, String reason) {
        this.status = ApplicationStatus.HOLD;
        this.finalDecidedBy = decidedByUserId;
        this.finalDecisionReason = reason;
        this.decidedAt = LocalDateTime.now();
    }

    // 이메일을 발송하여 지원자에게 최종 합/불 결과를 안내한 경우
    // Null이면 미발송
    public void markNotified() {
        this.notifiedAt = LocalDateTime.now();
    }

    // 승인된 신청서 기반 회원가입이 완료된 경우
    public void completeRegistration() {
        if (this.status != ApplicationStatus.ADMIN_ACCEPTED) {
            throw new IllegalStateException("최종 합격 상태의 신청서만 회원가입 완료 처리할 수 있습니다.");
        }
        this.status = ApplicationStatus.COMPLETED;
    }


}
