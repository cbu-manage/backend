package com.example.cbumanage.dto;


import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
/*
그룹과 관련된 DTO를 하나의 파일에서 처리합니다

1.GroupInfoDTO - 그룹의 정보를 가지고 있는 DTO입니다.
2.GroupMemberInfoDTO - GroupInfoDTO 내부에서 그룹 멤버의 정보를 표시하는 DTO 입니다
 */
public class GroupDTO {


    @Getter
    @NoArgsConstructor
    public static class GroupCreateRequestDTO {
        private String groupName;
        private int maxActiveMembers;
        private int minActiveMembers;
    }

    @Getter
    @NoArgsConstructor
    public static class GroupCreateResponseDTO {
        public Long groupId;
        public String groupName;
        public int maxActiveMembers;
        public int minActiveMembers;
        public LocalDateTime createdAt;
        public GroupMemberInfoDTO leader;

        @Builder
        public GroupCreateResponseDTO(Long groupId,String groupName, int maxActiveMembers, int minActiveMembers, LocalDateTime createdAt, GroupMemberInfoDTO leader) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.maxActiveMembers = maxActiveMembers;
            this.minActiveMembers = minActiveMembers;
            this.createdAt = createdAt;
            this.leader = leader;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class GroupUpdateRequestDTO{
        private String groupName;
        private int maxActiveMembers;
        private int minActiveMembers;
    }

    @Schema(description = "그룹 상태변환 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupStatusRequestDTO {
        @Schema(description = "그룹의 상태 (ACTIVE,INACTIVE")
        private GroupStatus groupStatus;
    }

    @Schema(description = "그룹의 모집상태 변환 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupRecruitmentStatusRequestDTO {
        @Schema(description = "그룹의 모집상태 (OPEN,CLOSE")
        private GroupRecruitmentStatus groupRecruitmentStatus;
    }

    @Schema(description = "그룹 멤버의 상태 변환 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupMemberStatusRequestDTO {
        @Schema(description = "그룹 멤버의 상태(PENDING,ACTIVE,INACTIVE)")
        private GroupMemberStatus groupMemberStatus;
    }

    /*
    그룹의 정보들을 담고 있는 DTO입니다. 그룹의 id, 그룹의 이름, 멤버 목록(DTO),
    최대 활동 인원과 최소활동 인원,현재 활동인원을 표기합니다
     */
    @Getter
    @NoArgsConstructor
    public static class GroupInfoDTO{
        public Long groupId;
        public String groupName;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public GroupRecruitmentStatus groupRecruitmentStatus;
        public GroupStatus  groupStatus;
        // Status가 Active인 멤버만 카운트해서 기록합니다
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
                GroupStatus  groupStatus,
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
            this.groupStatus = groupStatus;
            this.activeMemberCount = activeMemberCount;
            this.maxActiveMembers = maxActiveMembers;
            this.minActiveMembers = minActiveMembers;
            this.members = members;
        }

    }

    /*
    그룹의 멤버의 정보를 관리하는 DTO입니다
     */
    @Getter
    @NoArgsConstructor
    public static class GroupMemberInfoDTO{
        private Long groupMemberId;
        private Long userId;
        private String userName;
        private GroupMemberRole groupMemberRole;
        private GroupMemberStatus groupMemberStatus;
        private LocalDateTime createdAt;

        @Builder
        public GroupMemberInfoDTO(
                Long groupMemberId,
                Long userId,
                String userName,
                GroupMemberRole groupMemberRole,
                GroupMemberStatus groupMemberStatus,
                LocalDateTime createdAt
        ){
            this.groupMemberId = groupMemberId;
            this.userId = userId;
            this.userName = userName;
            this.groupMemberRole = groupMemberRole;
            this.groupMemberStatus = groupMemberStatus;
            this.createdAt = createdAt;
        }
    }
}
