package com.example.cbumanage.dto;


import com.example.cbumanage.model.enums.*;
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
        private int maxMembers;
        private int minMembers;
    }

    @Getter
    @NoArgsConstructor
    public static class GroupCreateResponseDTO {
        private Long groupId;
        private String groupName;
        private int maxMembers;
        private int minMembers;
        private LocalDateTime createdAt;
        private GroupMemberInfoDTO leader;

        @Builder
        public GroupCreateResponseDTO(Long groupId,String groupName, int maxMembers, int minMembers, LocalDateTime createdAt, GroupMemberInfoDTO leader) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.maxMembers = maxMembers;
            this.minMembers = minMembers;
            this.createdAt = createdAt;
            this.leader = leader;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class GroupUpdateRequestDTO{
        private String groupName;
        private Integer maxMembers;
        private int minMembers;
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
        @Schema(description = "멤버 상태 (PENDING: 대기, ACTIVE: 활동, INACTIVE: 비활동, REJECTED:가입 거절)", example = "ACTIVE")
        private GroupMemberStatus groupMemberStatus;
    }

    @Schema(description = "전체 그룹 리스트 요약 정보")
    @Getter
    @NoArgsConstructor
    public static class GroupListDTO{
        @Schema(description = "그룹 고유 ID", example = "1")
        private Long groupId;
        @Schema(description = "연결된 게시글 ID. 목록에서 해당 프로젝트/스터디 상세로 이동 시 사용. 없으면 null", example = "101")
        private Long postId;
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        private String groupName;
        @Schema(description = "그룹 생성일")
        private LocalDateTime createdAt;
        @Schema(description = "현재 활동 중인 인원 수. maxMembers와 함께 N/M 형태 표시용", example = "5")
        private int activeMemberCount;
        @Schema(description = "최대 모집 인원 수. 활동수와 함께 N/M 형태 표시용", example = "10")
        private int maxMembers;
        @Schema(description = "현재 그룹 활성화 상태", example = "ACTIVE")
        GroupStatus groupStatus;
        @Schema(description = "현재 그룹 모집 상태",example="OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus;
        @Schema(description = "현재 그룹 리더 ID", example="10")
        private Long leaderId;
        @Schema(description = "현재 그룹 리더 기수", example="15")
        private Long leaderGeneration;
        @Schema(description = "현재 그룹 리더 이름",example="홍길동")
        private String leaderName;

        @Builder
        public GroupListDTO(
                Long groupId,
                Long postId,
                String groupName,
                LocalDateTime createdAt,
                int activeMemberCount,
                int maxMembers,
                GroupStatus groupStatus,
                GroupRecruitmentStatus groupRecruitmentStatus,
                Long leaderId,
                Long leaderGeneration,
                String leaderName
        ){
            this.groupId = groupId;
            this.postId = postId;
            this.groupName = groupName;
            this.createdAt = createdAt;
            this.groupStatus = groupStatus;
            this.groupRecruitmentStatus = groupRecruitmentStatus;
            this.activeMemberCount = activeMemberCount;
            this.maxMembers = maxMembers;
            this.leaderId=leaderId;
            this.leaderGeneration=leaderGeneration;
            this.leaderName=leaderName;
        }
    }

    @Schema(description = "내가 신청한 그룹 목록. myStatus로 승인/대기/거절/비활동 구분. 프론트에서 라벨·버튼 분기용.")
    @Getter
    @NoArgsConstructor
    public static class MyGroupApplicationListDTO {
        @Schema(description = "그룹 고유 ID", example = "1")
        private Long groupId;
        @Schema(description = "연결된 게시글 ID. 해당 프로젝트/스터디 상세 이동 시 사용", example = "101")
        private Long postId;
        @Schema(description = "그룹명")
        private String groupName;
        @Schema(description = "그룹 생성일")
        private LocalDateTime createdAt;
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        private int activeMemberCount;
        @Schema(description = "최대 모집 인원 수", example = "10")
        private int maxMembers;
        @Schema(description = "그룹 활성화 상태", example = "ACTIVE")
        private GroupStatus groupStatus;
        @Schema(description = "그룹 모집 상태", example = "OPEN")
        private GroupRecruitmentStatus groupRecruitmentStatus;
        @Schema(description = "리더 ID", example = "10")
        private Long leaderId;
        @Schema(description = "리더 기수", example = "10")
        private Long leaderGeneration;
        @Schema(description = "리더 이름")
        private String leaderName;
        @Schema(description = "내 신청/가입 상태. PENDING=승인 대기중, ACTIVE=승인, REJECTED=거절됨, INACTIVE=비활동. 프론트 라벨·버튼(신청취소/다시신청 등) 분기용", example = "PENDING")
        private GroupMemberStatus myStatus;

        @Builder
        public MyGroupApplicationListDTO(Long groupId, Long postId, String groupName, LocalDateTime createdAt,
                                         int activeMemberCount, int maxMembers, GroupStatus groupStatus,
                                         GroupRecruitmentStatus groupRecruitmentStatus, Long leaderId, Long leaderGeneration, String leaderName,
                                         GroupMemberStatus myStatus) {
            this.groupId = groupId;
            this.postId = postId;
            this.groupName = groupName;
            this.createdAt = createdAt;
            this.activeMemberCount = activeMemberCount;
            this.maxMembers = maxMembers;
            this.groupStatus = groupStatus;
            this.groupRecruitmentStatus = groupRecruitmentStatus;
            this.leaderId = leaderId;
            this.leaderGeneration = leaderGeneration;
            this.leaderName = leaderName;
            this.myStatus = myStatus;
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
        private Long groupId;
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        private String groupName;
        @Schema(description = "그룹 생성일")
        private LocalDateTime createdAt;
        @Schema(description = "최근 수정일")
        private LocalDateTime updatedAt;
        @Schema(description = "현재 모집 상태", example = "OPEN")
        private GroupRecruitmentStatus groupRecruitmentStatus;
        @Schema(description = "현재 그룹 상태", example = "ACTIVE")
        private GroupStatus groupStatus;
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        private int activeMemberCount;
        @Schema(description = "최대 인원 제한", example = "10")
        private int maxMembers;
        @Schema(description = "최소 인원 제한", example = "3")
        private int minMembers;
        @Schema(description = "그룹 소속 멤버 리스트")
        private List<GroupMemberInfoDTO> members;

        @Builder
        public GroupInfoDTO(
                Long groupId,
                String groupName,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                GroupRecruitmentStatus groupRecruitmentStatus,
                GroupStatus  groupStatus,
                int activeMemberCount,
                int maxMembers,
                int minMembers,
                List<GroupMemberInfoDTO> members
        ){
            this.groupId = groupId;
            this.groupName = groupName;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.groupRecruitmentStatus = groupRecruitmentStatus;
            this.groupStatus = groupStatus;
            this.activeMemberCount = activeMemberCount;
            this.maxMembers = maxMembers;
            this.minMembers = minMembers;
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
        @Schema(description = "유저 기수",example = "15")
        private Long userGeneration;
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
                Long userGeneration,
                String userName,
                String grade,
                String major,
                GroupMemberRole groupMemberRole,
                GroupMemberStatus groupMemberStatus,
                LocalDateTime createdAt
        ){
            this.groupMemberId = groupMemberId;
            this.userId = userId;
            this.userGeneration = userGeneration;
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
        private ApplicantAction action;
    }
}
