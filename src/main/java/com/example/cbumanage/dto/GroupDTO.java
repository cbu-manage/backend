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

    @Schema(description = "그룹 상태 변환 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupStatusRequestDTO {
        @Schema(description = "그룹의 상태 (ACTIVE: 활성, INACTIVE: 비활성)", example = "ACTIVE")
        private GroupStatus groupStatus;
    }

    @Schema(description = "그룹 모집 상태 변환 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupRecruitmentStatusRequestDTO {
        @Schema(description = "그룹의 모집상태 (OPEN: 모집중, CLOSED: 모집마감)", example = "OPEN")
        private GroupRecruitmentStatus groupRecruitmentStatus;
    }

    @Schema(description = "그룹 멤버 상태 변경 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class GroupMemberStatusRequestDTO {
        @Schema(description = "멤버 상태 (PENDING: 대기, ACTIVE: 활동, INACTIVE: 비활동)", example = "ACTIVE")
        private GroupMemberStatus groupMemberStatus;
    }

    @Schema(description = "전체 그룹 리스트 요약 정보")
    @Getter
    @NoArgsConstructor
    public static class GroupListDTO{
        @Schema(description = "그룹 고유 ID", example = "1")
        public Long groupId;
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        public String groupName;
        @Schema(description = "그룹 생성일")
        public LocalDateTime createdAt;
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        public int activeMemberCount;
        @Schema(description = "현재 그룹 모집 상태",example="OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus;
        @Schema(description = "현재 그룹 리더 ID", example="10")
        private Long leaderId;
        @Schema(description = "현재 그룹 리더 이름",example="홍길동")
        private String leaderName;

        @Builder
        public GroupListDTO(
                Long groupId,
                String groupName,
                LocalDateTime createdAt,
                int activeMemberCount,
                GroupRecruitmentStatus groupRecruitmentStatus,
                Long leaderId,
                String leaderName
        ){
            this.groupId = groupId;
            this.groupName = groupName;
            this.createdAt = createdAt;
            this.groupRecruitmentStatus = groupRecruitmentStatus;
            this.activeMemberCount = activeMemberCount;
            this.leaderId=leaderId;
            this.leaderName=leaderName;
        }
    }

    /*
    그룹의 정보들을 담고 있는 DTO입니다. 그룹의 id, 그룹의 이름, 멤버 목록(DTO),
    최대 활동 인원과 최소활동 인원,현재 활동인원을 표기합니다
     */
    @Schema(description = "그룹 상세 정보")
    @Getter
    @NoArgsConstructor
    public static class GroupInfoDTO{
        @Schema(description = "그룹 고유 ID", example = "1")
        public Long groupId;
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        public String groupName;
        @Schema(description = "그룹 생성일")
        public LocalDateTime createdAt;
        @Schema(description = "최근 수정일")
        public LocalDateTime updatedAt;
        @Schema(description = "현재 모집 상태", example = "OPEN")
        public GroupRecruitmentStatus groupRecruitmentStatus;
        @Schema(description = "현재 그룹 상태", example = "ACTIVE")
        public GroupStatus groupStatus;
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        public int activeMemberCount;
        @Schema(description = "최대 인원 제한", example = "10")
        public int maxActiveMembers;
        @Schema(description = "최소 인원 제한", example = "3")
        public int minActiveMembers;
        @Schema(description = "그룹 소속 멤버 리스트")
        public List<GroupMemberInfoDTO> members;

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
    @Schema(description = "그룹 멤버 개별 정보")
    @Getter
    @NoArgsConstructor
    public static class GroupMemberInfoDTO{
        @Schema(description = "그룹-멤버 매핑 ID", example = "50")
        private Long groupMemberId;
        @Schema(description = "유저 고유 ID", example = "10")
        private Long userId;
        @Schema(description = "유저 성명", example = "홍길동")
        private String userName;
        @Schema(description = "학년", example = "3학년")
        private String grade;
        @Schema(description = "전공 학부", example = "컴퓨터공학부")
        private String major;
        @Schema(description = "그룹 내 역할 (LEADER: 팀장, MEMBER: 팀원)", example = "MEMBER")
        private GroupMemberRole groupMemberRole;
        @Schema(description = "멤버 활동 상태", example = "ACTIVE")
        private GroupMemberStatus groupMemberStatus;
        @Schema(description = "가입/신청 일시")
        private LocalDateTime createdAt;

        @Builder
        public GroupMemberInfoDTO(
                Long groupMemberId,
                Long userId,
                String userName,
                String grade,
                String major,
                GroupMemberRole groupMemberRole,
                GroupMemberStatus groupMemberStatus,
                LocalDateTime createdAt
        ){
            this.groupMemberId = groupMemberId;
            this.userId = userId;
            this.userName = userName;
            this.grade=grade;
            this.major=major;
            this.groupMemberRole = groupMemberRole;
            this.groupMemberStatus = groupMemberStatus;
            this.createdAt = createdAt;
        }
    }

    //보고서 리스트에 사용할 PostReportPreview에 조합할 DTO입니다
    @Getter
    @NoArgsConstructor
    public static class GroupPreviewDTO{
        private Long groupId;
        private String groupName;

        @Builder
        public GroupPreviewDTO(Long groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }
    }

    @Schema(description = "신청 수락/거절 요청 DTO")
    @Getter
    @NoArgsConstructor
    public static class ApplicantActionRequestDTO {
        @Schema(description = "처리 액션 (ACCEPT: 수락, REJECT: 거절)", example = "ACCEPT", allowableValues = {"ACCEPT", "REJECT"})
        private String action;
    }
}
