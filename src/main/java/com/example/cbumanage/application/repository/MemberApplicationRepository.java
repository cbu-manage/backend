package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.MemberApplication;
import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    Optional<MemberApplication> findFirstByStudentNumberAndEmailOrderBySubmittedAtDesc(
            Long studentNumber, String email);

    // 학번 + 닉네임 + 상태로 조회 (회원가입 전 승인자 검증)
    Optional<MemberApplication> findByStudentNumberAndNicknameAndStatus(
            Long studentNumber, String nickname, ApplicationStatus status);

    /* 다건조회 */

    /**
     * 운영진 대시보드 신청서 검색 (화면 A).
     * 모든 필터는 선택값이며 null이면 해당 조건을 무시한다.
     * - field: 지원분야
     * - statuses: 탭(상태) 묶음. null이면 전체
     * - from/to: 제출 시각 범위
     * - keyword: 이름 또는 학번 부분일치
     */
    @Query("""
           SELECT m FROM MemberApplication m
           WHERE m.generation = :generation
             AND (:field IS NULL OR m.applicationField = :field)
             AND (:statuses IS NULL OR m.status IN :statuses)
             AND (:from IS NULL OR m.submittedAt >= :from)
             AND (:to IS NULL OR m.submittedAt <= :to)
             AND (:keyword IS NULL
                  OR m.name LIKE CONCAT('%', :keyword, '%')
                  OR CAST(m.studentNumber AS string) LIKE CONCAT('%', :keyword, '%'))
           """)
    Page<MemberApplication> searchForAdmin(
            @Param("generation") Long generation,
            @Param("field") ApplicationField field,
            @Param("statuses") List<ApplicationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 기수별 신청서 목록 (최신순)
    Page<MemberApplication> findByGenerationOrderBySubmittedAtDesc(Long generation, Pageable pageable);

    // 기수 + 상태 필터링 (후보 테이블 無限스크롤용)
    Slice<MemberApplication> findByGenerationAndStatusOrderBySubmittedAtDesc(
            Long generation, ApplicationStatus status, Pageable pageable);

    // 기수 + 특정 상태 제외
    List<MemberApplication> findByGenerationAndStatusNot(Long generation, ApplicationStatus status);

    /**
     * 기수 + 상태 (예: 최종 결정 대기 = HOLD, ALL_REJECT, ALL_PASS).
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

    // 기수당 신청서 총 건수
    long countByGeneration(Long generation);

    // 기수당 각 상태 건수 (대시보드 요약)
    long countByGenerationAndStatus(Long generation, ApplicationStatus status);

    /**
     * 기수당 상태별 건수 일괄 집계 (대시보드 요약).
     * 결과 형식: [status, count]
     */
    @Query("""
           SELECT m.status, COUNT(m)
           FROM MemberApplication m
           WHERE m.generation = :generation
           GROUP BY m.status
           """)
    List<Object[]> countByStatusGroupedForGeneration(@Param("generation") Long generation);

}
