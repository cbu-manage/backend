package com.example.cbumanage.application.entity;

import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "모집 회차 엔티티. 기수 하나 = 모집 한 번 입니다.")
@Entity
@Table(name = "recruitment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_recruitment_generation", columnNames = "generation"),
                @UniqueConstraint(name = "uk_recruitment_uuid", columnNames = "recruitment_uuid")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_id")
    private Long id;

    @Schema(description = "모집 UUID (외부 노출용 경로변수)")
    @Column(name = "recruitment_uuid", nullable = false, unique = true, length = 36)
    private String recruitmentUuid;

    @Schema(description = "기수 (신청서/회원의 generation과 연결되는 원천)")
    @Column(nullable = false, unique = true)
    private Long generation;

    @Schema(description = "투표 자격자 수 스냅샷 (만장일치 판정 기준 N)")
    @Column(name = "voter_count", nullable = false)
    private int voterCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecruitmentStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Recruitment(Long generation, int voterCount) {
        this.recruitmentUuid = UUID.randomUUID().toString();
        this.generation = generation;
        this.voterCount = voterCount;
        this.status = RecruitmentStatus.OPEN;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 모집 시작. 시작 시점의 운영진 수를 voterCount로 고정
     */
    public static Recruitment open(Long generation, int voterCount) {
        return new Recruitment(generation, voterCount);
    }

    /**
     * 모집 마감함황
     */
    public void close() {
        this.status = RecruitmentStatus.CLOSED;
        this.endedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.status == RecruitmentStatus.OPEN;
    }
}