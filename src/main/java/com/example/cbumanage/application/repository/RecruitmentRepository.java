package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.Recruitment;
import com.example.cbumanage.application.entity.enums.RecruitmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 모집 회차 레포지토리.
 */
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    // UUID로 단건 조회 (외부 경로변수)
    Optional<Recruitment> findByRecruitmentUuid(String recruitmentUuid);

    // 기수로 단건 조회 (투표 집계 시 voterCount(N) 조회)
    Optional<Recruitment> findByGeneration(Long generation);

    // 현재 진행 중인 모집 (OPEN) 조회
    Optional<Recruitment> findFirstByStatus(RecruitmentStatus status);

    // 모집 회차 목록 (최신순)
    List<Recruitment> findAllByOrderByStartedAtDesc();
}
