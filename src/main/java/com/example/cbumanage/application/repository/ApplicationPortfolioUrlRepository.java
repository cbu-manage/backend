package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.ApplicationPortfolioUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 신청서 포트폴리오 URL 레포지토리.
 */
public interface ApplicationPortfolioUrlRepository extends JpaRepository<ApplicationPortfolioUrl, Long> {

    // 신청서별 포트폴리오 URL 목록 (정렬 순서 오름차순)
    List<ApplicationPortfolioUrl> findByMemberApplicationIdOrderBySortOrderAsc(Long memberApplicationId);
}