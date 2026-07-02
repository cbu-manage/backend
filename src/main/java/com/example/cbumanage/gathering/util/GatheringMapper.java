package com.example.cbumanage.gathering.util;

import com.example.cbumanage.gathering.dto.GatheringDTO;
import com.example.cbumanage.gathering.entity.Gathering;
import com.example.cbumanage.gathering.entity.GatheringAttendance;
import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatheringMapper {

    public GatheringDTO.CreateResponse toCreateResponse(Gathering gathering, User author) {
        return GatheringDTO.CreateResponse.builder()
                .id(gathering.getId())
                .title(gathering.getTitle())
                .type(gathering.getType())
                .description(gathering.getDescription())
                .gatheringDate(gathering.getGatheringDate())
                .location(gathering.getLocation())
                .voteDeadline(gathering.getVoteDeadline())
                .allMembersTarget(gathering.getAllMembersTarget())
                .authorGeneration(author.getGeneration())
                .authorName(author.getName())
                .createdAt(gathering.getCreatedAt())
                .build();
    }

    public GatheringDTO.GatheringResponse toGatheringResponse(Gathering gathering, User author,
                                                               AttendanceStatus myStatus,
                                                               GatheringDTO.AttendanceSummary summary) {
        return GatheringDTO.GatheringResponse.builder()
                .id(gathering.getId())
                .title(gathering.getTitle())
                .type(gathering.getType())
                .description(gathering.getDescription())
                .gatheringDate(gathering.getGatheringDate())
                .location(gathering.getLocation())
                .voteDeadline(gathering.getVoteDeadline())
                .voteClosed(gathering.isVoteClosed())
                .allMembersTarget(gathering.getAllMembersTarget())
                .authorGeneration(author != null ? author.getGeneration() : null)
                .authorName(author != null ? author.getName() : null)
                .summary(summary)
                .myStatus(myStatus)
                .createdAt(gathering.getCreatedAt())
                .updatedAuthorId(gathering.getUpdatedAuthorId())
                .updatedAt(gathering.getUpdatedAt())
                .viewCount(gathering.getViewCount())
                .build();
    }

    // 일반 사용자용 — 이름·기수만 노출, 미응답자 목록 없음
    public GatheringDTO.AttendanceListResponse toAttendanceListResponse(Gathering gathering,
                                                                         List<GatheringAttendance> attendances,
                                                                         GatheringDTO.AttendanceSummary summary) {
        Map<AttendanceStatus, List<GatheringDTO.MemberInfo>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(
                        GatheringAttendance::getStatus,
                        Collectors.mapping(a -> toMemberInfo(a.getMember()), Collectors.toList())
                ));

        return GatheringDTO.AttendanceListResponse.builder()
                .gatheringId(gathering.getId())
                .title(gathering.getTitle())
                .gatheringDate(gathering.getGatheringDate())
                .voteDeadline(gathering.getVoteDeadline())
                .voteClosed(gathering.isVoteClosed())
                .summary(summary)
                .attendingMembers(grouped.getOrDefault(AttendanceStatus.ATTENDING, List.of()))
                .notAttendingMembers(grouped.getOrDefault(AttendanceStatus.NOT_ATTENDING, List.of()))
                .build();
    }

    // 관리자용 — 전체 상세 정보 + 미응답자(NOT_RESPONDED) 목록 포함
    public GatheringDTO.AdminAttendanceListResponse toAdminAttendanceListResponse(
            Gathering gathering,
            List<GatheringAttendance> attendances,
            GatheringDTO.AttendanceSummary summary) {

        Map<AttendanceStatus, List<GatheringDTO.AdminMemberInfo>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(
                        GatheringAttendance::getStatus,
                        Collectors.mapping(a -> toAdminMemberInfo(a.getMember(), a.getVotedAt()), Collectors.toList())
                ));

        return GatheringDTO.AdminAttendanceListResponse.builder()
                .gatheringId(gathering.getId())
                .title(gathering.getTitle())
                .gatheringDate(gathering.getGatheringDate())
                .voteDeadline(gathering.getVoteDeadline())
                .voteClosed(gathering.isVoteClosed())
                .summary(summary)
                .attendingMembers(grouped.getOrDefault(AttendanceStatus.ATTENDING, List.of()))
                .notAttendingMembers(grouped.getOrDefault(AttendanceStatus.NOT_ATTENDING, List.of()))
                .unansweredMembers(grouped.getOrDefault(AttendanceStatus.NOT_RESPONDED, List.of()))
                .build();
    }

    public GatheringDTO.MemberInfo toMemberInfo(User member) {
        return GatheringDTO.MemberInfo.builder()
                .memberId(member.getUserId())
                .name(member.getName())
                .generation(member.getGeneration())
                .build();
    }

    public GatheringDTO.AdminMemberInfo toAdminMemberInfo(User member, LocalDateTime votedAt) {
        return GatheringDTO.AdminMemberInfo.builder()
                .memberId(member.getUserId())
                .name(member.getName())
                .generation(member.getGeneration())
                .studentNumber(member.getStudentNumber())
                .major(member.getMajor())
                .grade(member.getGrade())
                .votedAt(votedAt)
                .build();
    }
}
