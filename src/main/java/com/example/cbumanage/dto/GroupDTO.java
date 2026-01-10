package com.example.cbumanage.dto;


import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
/*
그룹과 관련된 DTO를 하나의 파일에서 처리합니다

1.GroupInfoDTO - 그룹의 정보를 가지고 있는 DTO입니다.
2.GroupMemberInfoDTO - GroupInfoDTO 내부에서 그룹 멤버의 정보를 표시하는 DTO 입니다
 */
public class GroupDTO {


    public static class createGroupDTO{
        private String groupName;
        private int maxActiveMembers;
        private int minActiveMembers;

    }
    /*
    그룹의 정보들을 담고 있는 DTO입니다. 그룹의 id, 그룹의 이름, 멤버 목록(DTO)를 반환합니다
     */
    @Getter
    @NoArgsConstructor
    public static class GroupInfoDTO{
        public Long groupId;
        public String groupName;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public GroupRecruitmentStatus groupRecruitmentStatus;
        public int activeMemberCount;
        public int maxActiveMembers;
        public int minActiveMembers;
        public List<GroupMemberInfoDTO>  members;


        @Builder
        public GroupInfoDTO(
                Long groupId,
                String groupName,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                GroupRecruitmentStatus groupRecruitmentStatus,
                int activeMemberCount,
                int maxActiveMembers,
                int minActiveMembers,
                List<GroupMemberInfoDTO> members
        ){
            this.groupId = groupId;
            this.groupName = groupName;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.groupRecruitmentStatus = groupRecruitmentStatus;
            this.activeMemberCount = activeMemberCount;
            this.maxActiveMembers = maxActiveMembers;
            this.minActiveMembers = minActiveMembers;
            this.members = members;
        }

    }

    /*
    그룹의 멤버들을 불러오는 DTO입니다
     */
    @Getter
    @NoArgsConstructor
    public static class GroupMemberInfoDTO{
        private Long userId;
        private String userName;
        private GroupMemberRole groupMemberRole;
        private GroupMemberStatus groupMemberStatus;
        private LocalDateTime createdAt;

        @Builder
        public GroupMemberInfoDTO(
                Long userId,
                String userName,
                GroupMemberRole groupMemberRole,
                GroupMemberStatus groupMemberStatus,
                LocalDateTime createdAt
        ){
            this.userId = userId;
            this.userName = userName;
            this.groupMemberRole = groupMemberRole;
            this.groupMemberStatus = groupMemberStatus;
            this.createdAt = createdAt;
        }
    }
}
