package com.example.cbumanage.gathering.util;

import com.example.cbumanage.gathering.dto.GatheringDTO;
import com.example.cbumanage.gathering.entity.Gathering;
import com.example.cbumanage.gathering.entity.GatheringAttendance;
import com.example.cbumanage.gathering.entity.enums.AttendanceStatus;
import com.example.cbumanage.member.entity.CbuMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatheringMapper {

    public GatheringDTO.CreateResponse toCreateResponse(Gathering gathering, CbuMember author) {
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

    public GatheringDTO.GatheringResponse toGatheringResponse(Gathering gathering, CbuMember author,
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
                .build();
    }

    public GatheringDTO.AttendanceListResponse toAttendanceListResponse(Gathering gathering,
                                                                         List<GatheringAttendance> attendances) {
        Map<AttendanceStatus, List<GatheringDTO.MemberInfo>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(
                        GatheringAttendance::getStatus,
                        Collectors.mapping(a -> toMemberInfo(a.getMember()), Collectors.toList())
                ));

        List<GatheringDTO.MemberInfo> attending = grouped.getOrDefault(AttendanceStatus.ATTENDING, List.of());
        List<GatheringDTO.MemberInfo> notAttending = grouped.getOrDefault(AttendanceStatus.NOT_ATTENDING, List.of());
        List<GatheringDTO.MemberInfo> undecided = grouped.getOrDefault(AttendanceStatus.UNDECIDED, List.of());

        return GatheringDTO.AttendanceListResponse.builder()
                .gatheringId(gathering.getId())
                .title(gathering.getTitle())
                .gatheringDate(gathering.getGatheringDate())
                .voteDeadline(gathering.getVoteDeadline())
                .voteClosed(gathering.isVoteClosed())
                .summary(GatheringDTO.AttendanceSummary.builder()
                        .attending(attending.size())
                        .notAttending(notAttending.size())
                        .undecided(undecided.size())
                        .build())
                .attendingMembers(attending)
                .notAttendingMembers(notAttending)
                .undecidedMembers(undecided)
                .build();
    }

    public GatheringDTO.MemberInfo toMemberInfo(CbuMember member) {
        return GatheringDTO.MemberInfo.builder()
                .memberId(member.getCbuMemberId())
                .name(member.getName())
                .major(member.getMajor())
                .grade(member.getGrade())
                .build();
    }
}
