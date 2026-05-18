package com.example.cbumanage.gathering.service;

import com.example.cbumanage.gathering.dto.GatheringDTO;
import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.gathering.entity.enums.GatheringType;
import com.example.cbumanage.gathering.entity.Gathering;
import com.example.cbumanage.gathering.entity.GatheringAttendance;
import com.example.cbumanage.gathering.repository.GatheringAttendanceRepository;
import com.example.cbumanage.gathering.repository.GatheringRepository;
import com.example.cbumanage.gathering.util.GatheringMapper;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringAttendanceRepository attendanceRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final GatheringMapper gatheringMapper;

    /**
     * 모임을 등록합니다.
     * - DINING: 전체 동아리원을 UNDECIDED로 자동 초기화
     * - FAIR: 오픈 투표 (레코드 미생성)
     * - OTHER: allMembersTarget 값에 따라 결정
     */
    @Transactional
    public GatheringDTO.CreateResponse createGathering(GatheringDTO.CreateRequest request, Long memberId) {
        CbuMember author = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean allMembersTarget = resolveAllMembersTarget(request.type(), request.allMembersTarget());

        Gathering gathering = Gathering.create(
                request.title(), request.type(), request.description(),
                request.gatheringDate(), request.location(), request.voteDeadline(),
                allMembersTarget, memberId
        );
        gatheringRepository.save(gathering);

        if (allMembersTarget) {
            List<CbuMember> allMembers = cbuMemberRepository.findAll();
            List<GatheringAttendance> attendances = allMembers.stream()
                    .map(m -> GatheringAttendance.create(gathering, m, AttendanceStatus.UNDECIDED))
                    .toList();
            attendanceRepository.saveAll(attendances);
        }

        return gatheringMapper.toCreateResponse(gathering, author);
    }

    // DINING은 강제 true, FAIR는 강제 false, OTHER는 요청값 사용 (null이면 false)
    private boolean resolveAllMembersTarget(GatheringType type, Boolean requested) {
        return switch (type) {
            case DINING -> true;
            case FAIR -> false;
            case OTHER -> Boolean.TRUE.equals(requested);
        };
    }

    /**
     * 전체 모임 목록을 모임 일시 내림차순으로 조회합니다.
     * 각 모임에 대한 내 투표 상태(myStatus)와 참석 현황 요약(summary)을 포함합니다.
     * 총 3번의 쿼리로 처리됩니다. (모임 목록, 작성자 일괄, 참석 데이터 일괄)
     */
    @Transactional(readOnly = true)
    public List<GatheringDTO.GatheringResponse> getGatherings(Long memberId) {
        List<Gathering> gatherings = gatheringRepository.findAllByIsDeletedFalseOrderByGatheringDateDesc();
        List<Long> gatheringIds = gatherings.stream().map(Gathering::getId).toList();

        // 작성자 일괄 조회
        Set<Long> authorIds = gatherings.stream().map(Gathering::getAuthorId).collect(Collectors.toSet());
        Map<Long, CbuMember> authorMap = cbuMemberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(CbuMember::getCbuMemberId, m -> m));

        // 전체 모임의 참석 데이터 일괄 조회
        List<GatheringAttendance> allAttendances = attendanceRepository.findAllByGatheringIdIn(gatheringIds);

        // 모임별 참석 데이터 그룹핑
        Map<Long, List<GatheringAttendance>> attendanceByGathering = allAttendances.stream()
                .collect(Collectors.groupingBy(a -> a.getGathering().getId()));

        // 내 투표 상태 모임별 맵핑
        Map<Long, AttendanceStatus> myStatusByGathering = allAttendances.stream()
                .filter(a -> a.getMember().getCbuMemberId().equals(memberId))
                .collect(Collectors.toMap(a -> a.getGathering().getId(), GatheringAttendance::getStatus));

        return gatherings.stream()
                .map(g -> {
                    List<GatheringAttendance> gAttendances = attendanceByGathering.getOrDefault(g.getId(), List.of());
                    return gatheringMapper.toGatheringResponse(
                            g,
                            authorMap.get(g.getAuthorId()),
                            myStatusByGathering.get(g.getId()),
                            buildSummaryFromList(gAttendances)
                    );
                })
                .toList();
    }

    /**
     * 특정 모임의 상세 정보를 조회합니다.
     * 내 투표 상태와 참석 현황 요약을 포함합니다.
     */
    @Transactional(readOnly = true)
    public GatheringDTO.GatheringResponse getGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        CbuMember author = cbuMemberRepository.findById(gathering.getAuthorId()).orElse(null);
        AttendanceStatus myStatus = attendanceRepository
                .findByGatheringIdAndMemberCbuMemberId(gatheringId, memberId)
                .map(GatheringAttendance::getStatus)
                .orElse(null);
        return gatheringMapper.toGatheringResponse(gathering, author, myStatus, buildSummaryByCount(gatheringId));
    }

    /**
     * 모임 정보를 수정합니다. 작성자만 수정할 수 있습니다.
     * voteDeadline을 미래 일시로 변경하면 마감된 투표가 재오픈됩니다.
     */
    @Transactional
    public GatheringDTO.GatheringResponse updateGathering(Long gatheringId, GatheringDTO.UpdateRequest request, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        validateCreator(gathering, memberId);
        gathering.update(request.title(), request.type(), request.description(),
                request.gatheringDate(), request.location(), request.voteDeadline());
        CbuMember author = cbuMemberRepository.findById(gathering.getAuthorId()).orElse(null);
        return gatheringMapper.toGatheringResponse(gathering, author, null, buildSummaryByCount(gatheringId));
    }

    /**
     * 모임을 수동 마감합니다. 작성자만 마감할 수 있습니다.
     */
    @Transactional
    public void closeGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        validateCreator(gathering, memberId);
        gathering.close();
    }

    /**
     * 모임을 삭제합니다. 작성자만 삭제할 수 있습니다. (소프트 딜리트)
     */
    @Transactional
    public void deleteGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        validateCreator(gathering, memberId);
        gathering.delete();
    }

    /**
     * 모임 참석 여부를 투표합니다. (ATTENDING / NOT_ATTENDING / UNDECIDED)
     * 이미 투표한 경우 재투표하면 이전 값이 덮어씌워집니다.
     * 투표 마감 상태에서는 투표할 수 없습니다.
     */
    @Transactional
    public void vote(Long gatheringId, AttendanceStatus status, Long memberId) {
        Gathering gathering = findGathering(gatheringId);

        if (gathering.isVoteClosed()) {
            throw new BaseException(ErrorCode.VOTE_CLOSED);
        }

        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        attendanceRepository.findByGatheringIdAndMemberCbuMemberId(gatheringId, memberId)
                .ifPresentOrElse(
                        a -> a.updateStatus(status),
                        () -> attendanceRepository.save(GatheringAttendance.create(gathering, member, status))
                );
    }

    /**
     * 모임의 참석 명단을 상태별(참석/불참/미정)로 조회합니다.
     */
    @Transactional(readOnly = true)
    public GatheringDTO.AttendanceListResponse getAttendanceList(Long gatheringId) {
        Gathering gathering = findGathering(gatheringId);
        List<GatheringAttendance> attendances = attendanceRepository.findAllByGatheringId(gatheringId);
        return gatheringMapper.toAttendanceListResponse(gathering, attendances);
    }

    // 모임 ID로 삭제되지 않은 모임을 조회합니다. 없으면 GATHERING_NOT_FOUND 예외를 던집니다.
    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findByIdAndIsDeletedFalse(gatheringId)
                .orElseThrow(() -> new BaseException(ErrorCode.GATHERING_NOT_FOUND));
    }

    // 요청자가 작성자인지 검증합니다. 다른 유저라면 FORBIDDEN 예외를 던집니다.
    private void validateCreator(Gathering gathering, Long memberId) {
        if (!gathering.getAuthorId().equals(memberId)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }

    // 이미 로딩된 참석 리스트로 요약을 계산합니다. (getGatherings 목록 조회용)
    private GatheringDTO.AttendanceSummary buildSummaryFromList(List<GatheringAttendance> attendances) {
        Map<AttendanceStatus, Long> counts = attendances.stream()
                .collect(Collectors.groupingBy(GatheringAttendance::getStatus, Collectors.counting()));
        return GatheringDTO.AttendanceSummary.builder()
                .attending(counts.getOrDefault(AttendanceStatus.ATTENDING, 0L))
                .notAttending(counts.getOrDefault(AttendanceStatus.NOT_ATTENDING, 0L))
                .undecided(counts.getOrDefault(AttendanceStatus.UNDECIDED, 0L))
                .build();
    }

    // COUNT 쿼리로 요약을 계산합니다. (단건 조회, 수정 시 사용)
    private GatheringDTO.AttendanceSummary buildSummaryByCount(Long gatheringId) {
        return GatheringDTO.AttendanceSummary.builder()
                .attending(attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.ATTENDING))
                .notAttending(attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.NOT_ATTENDING))
                .undecided(attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.UNDECIDED))
                .build();
    }
}
