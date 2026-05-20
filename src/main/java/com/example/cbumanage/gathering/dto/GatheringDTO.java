package com.example.cbumanage.gathering.dto;

import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.gathering.entity.enums.GatheringType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class GatheringDTO {

    @Builder
    @Schema(description = "모임 등록 요청")
    public record CreateRequest(
            @Schema(description = "모임 제목", example = "신입생 환영 회식") String title,
            @NotNull @Schema(description = "모임 유형 (DINING: 회식, FAIR: 박람회, OTHER: 기타)", example = "DINING") GatheringType type,
            @Schema(description = "모임 설명", example = "올해 신입생을 환영하는 자리입니다.") String description,
            @Schema(description = "모임 일시 (ISO 8601)", example = "2024-03-15T18:00:00") LocalDateTime gatheringDate,
            @Schema(description = "모임 장소", example = "학교 식당 1층") String location,
            @Schema(description = "투표 마감 일시. null이면 마감 없음", example = "2024-03-10T23:59:59", nullable = true) LocalDateTime voteDeadline,
            @Schema(description = "OTHER 타입일 때만 사용. true면 전체 동아리원 자동 포함, false면 오픈 투표. DINING은 무조건 true, FAIR는 무조건 false로 적용됨", nullable = true) Boolean allMembersTarget
    ) {}

    @Builder
    @Schema(description = "모임 수정 요청 (작성자만 가능)")
    public record UpdateRequest(
            @Schema(description = "모임 제목", example = "신입생 환영 회식") String title,
            @Schema(description = "모임 유형 (DINING: 회식, FAIR: 박람회, OTHER: 기타)", example = "DINING") GatheringType type,
            @Schema(description = "모임 설명", example = "올해 신입생을 환영하는 자리입니다.") String description,
            @Schema(description = "모임 일시 (ISO 8601)", example = "2024-03-15T18:00:00") LocalDateTime gatheringDate,
            @Schema(description = "모임 장소", example = "학교 식당 1층") String location,
            @Schema(description = "투표 마감 일시. null이면 마감 없음 / 마감된 경우 미래 일시로 변경하면 투표 재오픈됨", example = "2024-03-10T23:59:59", nullable = true) LocalDateTime voteDeadline
    ) {}

    @Builder
    @Schema(description = "참석 투표 요청")
    public record VoteRequest(
            @Schema(description = "투표 상태 (ATTENDING: 참석, NOT_ATTENDING: 불참, UNDECIDED: 미정)", example = "ATTENDING") AttendanceStatus status
    ) {}

    @Builder
    @Schema(description = "참석 현황 요약")
    public record AttendanceSummary(
            @Schema(description = "참석 인원 수", example = "10") long attending,
            @Schema(description = "불참 인원 수", example = "3") long notAttending,
            @Schema(description = "미정 인원 수", example = "2") long undecided
    ) {}

    @Builder
    @Schema(description = "멤버 정보")
    public record MemberInfo(
            @Schema(description = "멤버 ID", example = "1") Long memberId,
            @Schema(description = "이름", example = "홍길동") String name,
            @Schema(description = "학과", example = "컴퓨터공학부") String major,
            @Schema(description = "학년", example = "2") String grade
    ) {}

    @Builder
    @Schema(description = "모임 등록 응답")
    public record CreateResponse(
            @Schema(description = "모임 ID", example = "1") Long id,
            @Schema(description = "모임 제목", example = "신입생 환영 회식") String title,
            @Schema(description = "모임 유형 (DINING / FAIR / OTHER)", example = "DINING") GatheringType type,
            @Schema(description = "모임 설명", example = "올해 신입생을 환영하는 자리입니다.") String description,
            @Schema(description = "모임 일시", example = "2024-03-15T18:00:00") LocalDateTime gatheringDate,
            @Schema(description = "모임 장소", example = "학교 식당 1층") String location,
            @Schema(description = "투표 마감 일시. null이면 마감 없음", example = "2024-03-10T23:59:59", nullable = true) LocalDateTime voteDeadline,
            @Schema(description = "전체 동아리원 대상 여부. true면 전체 동아리원 포함, false면 오픈 투표", example = "true") Boolean allMembersTarget,
            @Schema(description = "작성자 기수", example = "15") Long authorGeneration,
            @Schema(description = "작성자 이름", example = "홍길동") String authorName,
            @Schema(description = "모임 등록 일시", example = "2024-03-01T10:00:00") LocalDateTime createdAt
    ) {}

    @Builder
    @Schema(description = "모임 목록 응답")
    public record GatheringResponse(
            @Schema(description = "모임 ID", example = "1") Long id,
            @Schema(description = "모임 제목", example = "신입생 환영 회식") String title,
            @Schema(description = "모임 유형 (DINING / FAIR / OTHER)", example = "DINING") GatheringType type,
            @Schema(description = "모임 설명", example = "올해 신입생을 환영하는 자리입니다.") String description,
            @Schema(description = "모임 일시", example = "2024-03-15T18:00:00") LocalDateTime gatheringDate,
            @Schema(description = "모임 장소", example = "학교 식당 1층") String location,
            @Schema(description = "투표 마감 일시. null이면 마감 없음", example = "2024-03-10T23:59:59", nullable = true) LocalDateTime voteDeadline,
            @Schema(description = "투표 마감 여부 (마감일이 지났으면 true)", example = "false") boolean voteClosed,
            @Schema(description = "전체 동아리원 대상 여부. true면 전체 동아리원 포함, false면 오픈 투표", example = "true") Boolean allMembersTarget,
            @Schema(description = "작성자 기수", example = "15") Long authorGeneration,
            @Schema(description = "작성자 이름", example = "홍길동") String authorName,
            @Schema(description = "참석 현황 요약") AttendanceSummary summary,
            @Schema(description = "내 투표 상태 (ATTENDING / NOT_ATTENDING / UNDECIDED). 미투표 시 null", example = "ATTENDING", nullable = true) AttendanceStatus myStatus,
            @Schema(description = "모임 등록 일시", example = "2024-03-01T10:00:00") LocalDateTime createdAt
    ) {}

    @Builder
    @Schema(description = "참석 명단 응답")
    public record AttendanceListResponse(
            @Schema(description = "모임 ID", example = "1") Long gatheringId,
            @Schema(description = "모임 제목", example = "신입생 환영 회식") String title,
            @Schema(description = "모임 일시", example = "2024-03-15T18:00:00") LocalDateTime gatheringDate,
            @Schema(description = "투표 마감 일시. null이면 마감 없음", nullable = true) LocalDateTime voteDeadline,
            @Schema(description = "투표 마감 여부", example = "false") boolean voteClosed,
            @Schema(description = "참석 현황 요약") AttendanceSummary summary,
            @Schema(description = "참석 멤버 목록") List<MemberInfo> attendingMembers,
            @Schema(description = "불참 멤버 목록") List<MemberInfo> notAttendingMembers,
            @Schema(description = "미정 멤버 목록") List<MemberInfo> undecidedMembers
    ) {}
}
