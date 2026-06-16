package com.example.cbumanage.application.repository;

import com.example.cbumanage.application.entity.ApplicationVote;
import com.example.cbumanage.application.entity.enums.VoteResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 지원서 투표여부 확인 레포지토
 */
public interface ApplicationVoteRepository extends JpaRepository<ApplicationVote, Long> {

    // 운영진이 특정 신청서에 이미 투표했는지 (한 명당 1표 제약)
    Optional<ApplicationVote> findByMemberApplicationIdAndVoterId(Long memberApplicationId, Long voterId);

    // 신청서에 대한 모든 운영진 투표 보기(운영진별 의견 보기)
    List<ApplicationVote> findByMemberApplicationId(Long memberApplicationId);

    // 신청서 목록에서 현재 운영진의 검토 여부 일괄 조회
    List<ApplicationVote> findByMemberApplicationIdInAndVoterId(List<Long> memberApplicationIds, Long voterId);

    /* 집계 함수 (만장일치 찬성(ALL_PASS)/만장일치 불함(ALL_REJECT) 판정용) */

    // 신청서별 PASS/FAIL 건수
    long countByMemberApplicationIdAndDecision(Long memberApplicationId, VoteResult decision);

    /**
     * 여러 신청서의 PASS/FAIL 집계.
     * 결과 형식: [applicationId, decision, count]
     */
    @Query("""
           SELECT v.memberApplicationId, v.decision, COUNT(v)
           FROM ApplicationVote v
           WHERE v.memberApplicationId IN :applicationIds
           GROUP BY v.memberApplicationId, v.decision
           """)
    List<Object[]> countByApplicationIdsGroupByDecision(@Param("applicationIds") List<Long> applicationIds);
}
