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
import com.example.cbumanage.user.entity.MemberStatus;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final GatheringMapper gatheringMapper;

    /**
     * 모임을 등록합니다.
     * - DINING/MT: 전체 동아리원을 NOT_RESPONDED로 자동 초기화
     * - FAIR: 오픈 투표 (레코드 미생성)
     * - EVENT/OTHER: allMembersTarget 값에 따라 결정
     */
    @Transactional
    public GatheringDTO.CreateResponse createGathering(GatheringDTO.CreateRequest request, Long memberId) {
        User author = userRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        boolean allMembersTarget = resolveAllMembersTarget(request.type(), request.allMembersTarget());

        Gathering gathering = Gathering.create(
                request.title(), request.type(), request.description(),
                request.gatheringDate(), request.location(), request.voteDeadline(),
                allMembersTarget, memberId
        );
        gatheringRepository.save(gathering);

        if (allMembersTarget) {
            List<User> allMembers = userRepository.findAllByMemberStatus(MemberStatus.ACTIVE);
            List<GatheringAttendance> attendances = allMembers.stream()
                    .map(m -> GatheringAttendance.create(gathering, m, AttendanceStatus.NOT_RESPONDED))
                    .toList();
            attendanceRepository.saveAll(attendances);
        }

        return gatheringMapper.toCreateResponse(gathering, author);
    }

    // DINING/MT는 강제 true, FAIR는 강제 false, EVENT/OTHER는 요청값 사용 (null이면 false)
    private boolean resolveAllMembersTarget(GatheringType type, Boolean requested) {
        return switch (type) {
            case DINING, MT -> true;
            case FAIR -> false;
            case EVENT, OTHER -> Boolean.TRUE.equals(requested);
        };
    }

    /**
     * 전체 모임 목록을 모임 일시 내림차순으로 조회합니다.
     * 총 3번의 쿼리로 처리됩니다. (모임 목록, 작성자 일괄, 참석 데이터 일괄)
     */
    @Transactional(readOnly = true)
    public List<GatheringDTO.GatheringResponse> getGatherings(Long memberId) {
        List<Gathering> gatherings = gatheringRepository.findAllByIsDeletedFalseOrderByGatheringDateDesc();
        List<Long> gatheringIds = gatherings.stream().map(Gathering::getId).toList();

        Set<Long> authorIds = gatherings.stream().map(Gathering::getAuthorId).collect(Collectors.toSet());
        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getUserId, m -> m));

        List<GatheringAttendance> allAttendances = attendanceRepository.findAllByGatheringIdIn(gatheringIds);

        Map<Long, List<GatheringAttendance>> attendanceByGathering = allAttendances.stream()
                .collect(Collectors.groupingBy(a -> a.getGathering().getId()));

        Map<Long, AttendanceStatus> myStatusByGathering = allAttendances.stream()
                .filter(a -> a.getMember().getUserId().equals(memberId))
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
     * 조회 시 viewCount를 증가시킵니다.
     */
    @Transactional
    public GatheringDTO.GatheringResponse getGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        gathering.incrementViewCount();
        User author = userRepository.findById(gathering.getAuthorId()).orElse(null);
        AttendanceStatus myStatus = attendanceRepository
                .findByGatheringIdAndMemberUserId(gatheringId, memberId)
                .map(GatheringAttendance::getStatus)
                .orElse(null);
        return gatheringMapper.toGatheringResponse(gathering, author, myStatus, buildSummaryByCount(gatheringId));
    }

    /**
     * 모임 정보를 수정합니다.
     * 유형(type)은 변경 불가 — 변경이 필요하면 삭제 후 재생성해야 합니다.
     */
    @Transactional
    public GatheringDTO.GatheringResponse updateGathering(Long gatheringId, GatheringDTO.UpdateRequest request, Long memberId) {
        Gathering gathering = findGathering(gatheringId);

        if (request.type() != null && request.type() != gathering.getType()) {
            throw new BaseException(ErrorCode.GATHERING_TYPE_IMMUTABLE);
        }

        gathering.update(request.title(), request.description(),
                request.gatheringDate(), request.location(), request.voteDeadline(), memberId);

        User author = userRepository.findById(gathering.getAuthorId()).orElse(null);
        return gatheringMapper.toGatheringResponse(gathering, author, null, buildSummaryByCount(gatheringId));
    }

    /**
     * 모임을 수동 마감합니다.
     */
    @Transactional
    public void closeGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        gathering.close(memberId);
    }

    /**
     * 모임을 삭제합니다. (소프트 딜리트)
     */
    @Transactional
    public void deleteGathering(Long gatheringId, Long memberId) {
        Gathering gathering = findGathering(gatheringId);
        gathering.delete(memberId);
    }

    /**
     * 모임 참석 여부를 투표합니다. (ATTENDING / NOT_ATTENDING)
     * 이미 투표한 경우 재투표하면 이전 값이 덮어씌워집니다.
     * 투표 마감 상태에서는 투표할 수 없습니다.
     */
    @Transactional
    public void vote(Long gatheringId, AttendanceStatus status, Long memberId) {
        if (status == AttendanceStatus.NOT_RESPONDED) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }

        Gathering gathering = findGathering(gatheringId);

        if (gathering.isVoteClosed()) {
            throw new BaseException(ErrorCode.VOTE_CLOSED);
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        attendanceRepository.findByGatheringIdAndMemberUserId(gatheringId, memberId)
                .ifPresentOrElse(
                        a -> a.updateStatus(status),
                        () -> attendanceRepository.save(GatheringAttendance.createWithVote(gathering, member, status))
                );
    }

    /**
     * 참석 명단 조회 (일반 사용자용) — 이름·기수만 노출
     */
    @Transactional(readOnly = true)
    public GatheringDTO.AttendanceListResponse getAttendanceList(Long gatheringId) {
        Gathering gathering = findGathering(gatheringId);
        List<GatheringAttendance> attendances = attendanceRepository.findAllByGatheringId(gatheringId);
        return gatheringMapper.toAttendanceListResponse(gathering, attendances, buildSummaryFromList(attendances));
    }

    /**
     * 참석 명단 조회 (관리자용) — 학번·학과·학년·투표일시·미응답자 목록 포함
     */
    @Transactional(readOnly = true)
    public GatheringDTO.AdminAttendanceListResponse getAttendanceListForAdmin(Long gatheringId) {
        Gathering gathering = findGathering(gatheringId);
        List<GatheringAttendance> attendances = attendanceRepository.findAllByGatheringId(gatheringId);
        return gatheringMapper.toAdminAttendanceListResponse(gathering, attendances, buildSummaryFromList(attendances));
    }

    /**
     * 참석 명단을 엑셀 파일로 내보냅니다.
     * 참석 → 불참 → 미응답 순으로 정렬됩니다.
     */
    @Transactional(readOnly = true)
    public byte[] exportAttendanceToExcel(Long gatheringId) {
        Gathering gathering = findGathering(gatheringId);
        List<GatheringAttendance> attendances = attendanceRepository.findAllByGatheringId(gatheringId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("참석 명단");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"이름", "기수", "학번", "학과", "학년", "응답", "투표일시"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 참석 → 불참 → 미응답 순 정렬
            List<GatheringAttendance> sorted = attendances.stream()
                    .sorted(Comparator.comparingInt(a -> statusOrder(a.getStatus())))
                    .toList();

            int rowNum = 1;
            for (GatheringAttendance a : sorted) {
                rowNum = writeAttendanceRow(sheet, rowNum, a.getMember(),
                        statusLabel(a.getStatus()), a.getVotedAt());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    private int writeAttendanceRow(Sheet sheet, int rowNum, User member, String status,
                                    java.time.LocalDateTime votedAt) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(nullSafe(member.getName()));
        row.createCell(1).setCellValue(member.getGeneration() != null ? member.getGeneration() : 0L);
        row.createCell(2).setCellValue(member.getStudentNumber() != null ? member.getStudentNumber() : 0L);
        row.createCell(3).setCellValue(nullSafe(member.getMajor()));
        row.createCell(4).setCellValue(nullSafe(member.getGrade()));
        row.createCell(5).setCellValue(status);
        row.createCell(6).setCellValue(votedAt != null ? votedAt.toString() : "--");
        return rowNum + 1;
    }

    private String statusLabel(AttendanceStatus status) {
        return switch (status) {
            case ATTENDING -> "참석";
            case NOT_ATTENDING -> "불참";
            case NOT_RESPONDED -> "미응답";
        };
    }

    private int statusOrder(AttendanceStatus status) {
        return switch (status) {
            case ATTENDING -> 0;
            case NOT_ATTENDING -> 1;
            case NOT_RESPONDED -> 2;
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findByIdAndIsDeletedFalse(gatheringId)
                .orElseThrow(() -> new BaseException(ErrorCode.GATHERING_NOT_FOUND));
    }


    // 로딩된 참석 리스트로 요약을 계산합니다. NOT_RESPONDED가 unanswered에 해당합니다.
    private GatheringDTO.AttendanceSummary buildSummaryFromList(List<GatheringAttendance> attendances) {
        Map<AttendanceStatus, Long> counts = attendances.stream()
                .collect(Collectors.groupingBy(GatheringAttendance::getStatus, Collectors.counting()));
        long attending = counts.getOrDefault(AttendanceStatus.ATTENDING, 0L);
        long notAttending = counts.getOrDefault(AttendanceStatus.NOT_ATTENDING, 0L);
        long unanswered = counts.getOrDefault(AttendanceStatus.NOT_RESPONDED, 0L);
        return GatheringDTO.AttendanceSummary.builder()
                .attending(attending)
                .notAttending(notAttending)
                .unanswered(unanswered)
                .total(attending + notAttending + unanswered)
                .build();
    }

    // COUNT 쿼리로 요약을 계산합니다. (단건 조회, 수정 시 사용)
    private GatheringDTO.AttendanceSummary buildSummaryByCount(Long gatheringId) {
        long attending = attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.ATTENDING);
        long notAttending = attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.NOT_ATTENDING);
        long unanswered = attendanceRepository.countByGatheringIdAndStatus(gatheringId, AttendanceStatus.NOT_RESPONDED);
        return GatheringDTO.AttendanceSummary.builder()
                .attending(attending)
                .notAttending(notAttending)
                .unanswered(unanswered)
                .total(attending + notAttending + unanswered)
                .build();
    }
}
