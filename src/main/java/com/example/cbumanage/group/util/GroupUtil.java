package com.example.cbumanage.group.util;

import com.example.cbumanage.group.dto.GroupDTO;
import com.example.cbumanage.group.entity.Group;
import com.example.cbumanage.group.entity.GroupMember;
import com.example.cbumanage.group.entity.enums.GroupMemberRole;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import org.springframework.stereotype.Component;

@Component
public class GroupUtil {

    public GroupDTO.GroupInfoDTO toGroupInfoDTO(Group group) {
        return GroupDTO.GroupInfoDTO.builder()
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .activeMemberCount((int) group.getMembers().stream()
                        .filter(m -> m.getGroupMemberStatus() == GroupMemberStatus.ACTIVE)
                        .count())
                .maxMembers(group.getMaxActiveMembers())
                .minMembers(group.getMinActiveMembers())
                .members(group.getMembers().stream().map(m->toGroupMemberInfoDTO(m)).toList())
                .groupRecruitmentStatus(group.getRecruitmentStatus())
                .groupStatus(group.getStatus())
                .build();
    }

    public GroupDTO.GroupMemberInfoDTO toGroupMemberInfoDTO(GroupMember member) {
        return GroupDTO.GroupMemberInfoDTO.builder()
                .groupMemberId(member.getId())
                .userId(member.getCbuMember().getCbuMemberId())
                .userGeneration(member.getCbuMember().getGeneration())
                .userName(member.getCbuMember().getName())
                .grade(member.getCbuMember().getGrade())
                .major(member.getCbuMember().getMajor())
                .groupMemberRole(member.getGroupMemberRole())
                .groupMemberStatus(member.getGroupMemberStatus())
                .createdAt(member.getCreatedAt())
                .build();

    }

    public GroupDTO.GroupCreateResponseDTO toGroupCreateResponseDTO(Group group) {
        GroupMember leader = group.getMembers().stream()
                .filter(m -> m.getGroupMemberRole() == GroupMemberRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Leader not found"));

        return GroupDTO.GroupCreateResponseDTO.builder()
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .maxMembers(group.getMaxActiveMembers())
                .minMembers(group.getMinActiveMembers())
                .createdAt(group.getCreatedAt())
                .leader(toGroupMemberInfoDTO(leader))
                .build();
    }

    public GroupDTO.GroupPreviewDTO toGroupPreviewDTO(Group group) {
        return GroupDTO.GroupPreviewDTO.builder().
                groupId(group.getId())
                .groupName(group.getGroupName())
                .build();
    }

    public GroupDTO.GroupListDTO toGroupListDTO(Group group) {
        GroupMember leader = group.getMembers().stream()
                .filter(m -> m.getGroupMemberRole() == GroupMemberRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Leader not found"));

        return GroupDTO.GroupListDTO.builder()
                .groupId(group.getId())
                .postId(group.getPostId())
                .groupName(group.getGroupName())
                .createdAt(group.getCreatedAt())
                .groupStatus(group.getStatus())
                .groupRecruitmentStatus(group.getRecruitmentStatus())
                .activeMemberCount((int) group.getMembers().stream()
                        .filter(m -> m.getGroupMemberStatus() == GroupMemberStatus.ACTIVE)
                        .count())
                .maxMembers(group.getMaxActiveMembers() != null ? group.getMaxActiveMembers() : 0)
                .leaderId(leader != null ? leader.getCbuMember().getCbuMemberId() : null)
                .leaderGeneration(leader != null ? leader.getCbuMember().getGeneration() : null)
                .leaderName(leader != null ? leader.getCbuMember().getName() : null)
                .build();
    }

    // 내가 신청한 그룹 목록 ACTIVE=승인, PENDING=승인 대기중, REJECTED=거절됨, INACTIVE=비활동
    public GroupDTO.MyGroupApplicationListDTO toMyGroupApplicationListDTO(GroupMember groupMember) {
        Group group = groupMember.getGroup();
        GroupMember leader = group.getMembers().stream()
                .filter(m -> m.getGroupMemberRole() == GroupMemberRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Leader not found"));
        return GroupDTO.MyGroupApplicationListDTO.builder()
                .groupId(group.getId())
                .postId(group.getPostId())
                .category(group.getCategory())
                .groupName(group.getGroupName())
                .createdAt(group.getCreatedAt())
                .groupStatus(group.getStatus())
                .groupRecruitmentStatus(group.getRecruitmentStatus())
                .activeMemberCount((int) group.getMembers().stream()
                        .filter(m -> m.getGroupMemberStatus() == GroupMemberStatus.ACTIVE)
                        .count())
                .maxMembers(group.getMaxActiveMembers() != null ? group.getMaxActiveMembers() : 0)
                .leaderId(leader != null ? leader.getCbuMember().getCbuMemberId() : null)
                .leaderGeneration(leader != null ? leader.getCbuMember().getGeneration() : null)
                .leaderName(leader != null ? leader.getCbuMember().getName() : null)
                .myStatus(groupMember.getGroupMemberStatus())
                .build();
    }
}
