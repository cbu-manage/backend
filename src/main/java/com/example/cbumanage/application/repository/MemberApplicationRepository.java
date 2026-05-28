package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 지원서 제출한 멤버 보기
 */
public interface MemberApplicationRepository extends JpaRepository<MemberApplication, Long> {

    /* 단건조회 */

    // 지원서 UUID로 지원서 조회 (path variable)
    Optional<MemberApplication> findByApplicationUuid(String memberApplicationUuid);

    // 학번 + 기수로 조회 (본인인증, 신청 현황 확인 시)
    Optional<MemberApplication> findByStudentNumberAndGeneration(Long studentNumber, Long generation);

    /* 다건조회 */

    // 기수별 신청서 목록 (최신순)
    Page<MemberApplication> findByGenerationOrderBySubmittedAtDesc(Long generation, Pageable pageable);

    // 기수 + 상태 필터링 (운영진 대시보드용)
    Page<MemberApplication> findByGenerationAndStatusOrderBySubmittedAtDesc(
            Long generation, ApplicationStatus status, Pageable pageable);

    /**
     * 기수 + 여러 상태 (예: 최종 결정 대기 = HOLD, ALL_REJECT, ALL_PASS).
     */
    @Query("""
           SELECT m FROM MemberApplication m
           WHERE m.generation = :generation
             AND m.status IN :statuses
           ORDER BY m.submittedAt DESC
           """)
    List<MemberApplication> findByGenerationAndStatusIn(
            @Param("generation") Long generation,
            @Param("statuses") List<ApplicationStatus> statuses);

    // === 집계 ===

    // 기수별 신청서 총 건수
    long countByGeneration(Long generation);

    // 기수별 상태별 건수 (대시보드 요약 카드)
    long countByGenerationAndStatus(Long generation, ApplicationStatus status);


}
