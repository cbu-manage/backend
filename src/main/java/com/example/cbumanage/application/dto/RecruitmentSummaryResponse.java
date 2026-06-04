package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.enums.ApplicationField;
import com.example.cbumanage.application.entity.enums.ApplicationStatus;
import com.example.cbumanage.application.entity.enums.FinalDecision;
import org.springframework.data.domain.Slice;

import java.util.Map;

/**
 * 대시보드 요약 (모집 컨텍스트 + 상태별 카운트 + 투표 진행상황 + 후보 테이블).
 * 후보 테이블은 무한스크롤로 제공한다.
 */
public record RecruitmentSummaryResponse(
        RecruitmentResponse recruitment,
        Map<ApplicationStatus, Long> countByStatus,
        VoteCards voteCards,
        Slice<CandidateRow> candidates
) {
    /** 3분할 카드 (실시간 투표 집계, 합 = 전체 신청자(CANCELLED 제외)) */
    public record VoteCards(
            long total,
            long allPass,   // 자격자 전원 PASS
            long hold,      // 혼합 (투표 미완료 포함)
            long allReject  // 자격자 전원 FAIL
    ) {
    }

    /** 후보 테이블 (최종결정 대상) */
    public record CandidateRow(
            String applicationUuid,
            String name,
            Long studentNumber,
            String major,
            ApplicationField applicationField,
            long passCount,
            long failCount,
            FinalDecision suggestedDecision   // 만장일치 집계를 통한 드롭다운 초기값
    ) {
    }
}