package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.StudyApplyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_apply", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_study_apply_study_applicant",
        columnNames = {"study_id", "applicant_id"}
    )
})
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "스터디 신청 엔티티. 부원이 스터디에 참가 신청한 내역을 관리합니다.")
public class StudyApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_apply_id")
    @Schema(description = "스터디 신청 식별자")
    @Comment("스터디 신청 식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    @Schema(description = "신청 대상 스터디")
    @Comment("신청 대상 스터디 FK")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @Schema(description = "신청자")
    @Comment("신청자 (CbuMember) FK")
    private CbuMember applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "신청 상태 (PENDING, ACCEPTED, REJECTED, CANCELLED)")
    @Comment("신청 상태")
    private StudyApplyStatus status;

    @CreatedDate
    @Column(updatable = false)
    @Schema(description = "신청 일시")
    @Comment("신청 일시")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Schema(description = "수정 일시")
    @Comment("수정 일시")
    private LocalDateTime updatedAt;

    public StudyApply(Study study, CbuMember applicant) {
        this.study = study;
        this.applicant = applicant;
        this.status = StudyApplyStatus.PENDING;
    }

    public static StudyApply create(Study study, CbuMember applicant) {
        return new StudyApply(study, applicant);
    }

    public void changeStatus(StudyApplyStatus newStatus) {
        if (this.status != StudyApplyStatus.PENDING) {
            throw new IllegalStateException("대기 상태(PENDING)의 신청만 변경할 수 있습니다.");
        }
        if (newStatus != StudyApplyStatus.ACCEPTED && newStatus != StudyApplyStatus.REJECTED) {
            throw new IllegalArgumentException("수락(ACCEPTED) 또는 거절(REJECTED)만 허용됩니다.");
        }
        this.status = newStatus;
    }

    public void cancel() {
        if (this.status != StudyApplyStatus.PENDING) {
            throw new IllegalStateException("대기 상태(PENDING)의 신청만 취소할 수 있습니다.");
        }
        this.status = StudyApplyStatus.CANCELLED;
    }

    public void reapply() {
        if (this.status != StudyApplyStatus.CANCELLED && this.status != StudyApplyStatus.REJECTED) {
            throw new IllegalStateException("취소 또는 거절된 신청만 재신청할 수 있습니다.");
        }
        this.status = StudyApplyStatus.PENDING;
    }
}
