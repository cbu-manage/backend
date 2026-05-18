package com.example.cbumanage.gathering.repository;

import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.gathering.entity.GatheringAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringAttendanceRepository extends JpaRepository<GatheringAttendance, Long> {

    // 단건 조회 (투표 여부 확인, 재투표 처리)
    Optional<GatheringAttendance> findByGatheringIdAndMemberCbuMemberId(Long gatheringId, Long memberId);

    // 단일 모임 참석 명단 조회 - JOIN FETCH로 member 지연로딩 N+1 방지
    @Query("SELECT a FROM GatheringAttendance a JOIN FETCH a.member WHERE a.gathering.id = :gatheringId")
    List<GatheringAttendance> findAllByGatheringId(@Param("gatheringId") Long gatheringId);

    // 여러 모임의 참석 데이터 일괄 조회 - getGatherings() N+1 방지
    @Query("SELECT a FROM GatheringAttendance a JOIN FETCH a.member WHERE a.gathering.id IN :gatheringIds")
    List<GatheringAttendance> findAllByGatheringIdIn(@Param("gatheringIds") List<Long> gatheringIds);

    long countByGatheringIdAndStatus(Long gatheringId, AttendanceStatus status);
}
