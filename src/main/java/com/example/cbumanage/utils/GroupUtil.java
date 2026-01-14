package com.example.cbumanage.utils;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.model.Group;
import com.example.cbumanage.model.GroupMember;
import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.GroupMemberRepository;
import com.example.cbumanage.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupUtil {

    private final CbuMemberRepository cbuMemberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public GroupUtil(CbuMemberRepository cbuMemberRepository, GroupMemberRepository groupMemberRepository, GroupRepository groupRepository) {
        this.cbuMemberRepository = cbuMemberRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
    }



    public GroupDTO.GroupInfoDTO toGroupInfoDTO(Group group) {
        return GroupDTO.GroupInfoDTO.builder()
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .activeMemberCount(groupRepository.countByGroupIdAndStatus(group.getId(), GroupMemberStatus.ACTIVE))
                .maxActiveMembers(group.getMaxActiveMembers())
                .minActiveMembers(group.getMinActiveMembers())
                .members(group.getMembers().stream().map(m->toGroupMemberInfoDTO(m)).toList())
                .groupRecruitmentStatus(group.getRecruitmentStatus())
                .groupStatus(group.getStatus())
                .build();
    }

    public GroupDTO.GroupMemberInfoDTO toGroupMemberInfoDTO(GroupMember member) {
        return GroupDTO.GroupMemberInfoDTO.builder()
                .groupMemberId(member.getId())
                .userId(member.getCbuMember().getCbuMemberId())
                .userName(member.getCbuMember().getName())
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
                .createdAt(group.getCreatedAt())
                .leader(toGroupMemberInfoDTO(leader))
                .build();
    }





}
