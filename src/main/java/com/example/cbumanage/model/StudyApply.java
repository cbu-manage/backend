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
@Table(name = "study_apply")
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
    @Schema(description = "신청 상태 (PENDING, ACCEPTED, REJECTED)")
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

    public void accept() {
        this.status = StudyApplyStatus.ACCEPTED;
    }

    public void reject() {
        this.status = StudyApplyStatus.REJECTED;
    }

    public void changeStatus(StudyApplyStatus status) {
        this.status = status;
    }
}
