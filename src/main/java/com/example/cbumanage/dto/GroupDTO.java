package com.example.cbumanage.dto;


import com.example.cbumanage.model.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
/*
그룹과 관련된 DTO를 하나의 파일에서 처리합니다

1.GroupInfoDTO - 그룹의 정보를 가지고 있는 DTO입니다.
2.GroupMemberInfoDTO - GroupInfoDTO 내부에서 그룹 멤버의 정보를 표시하는 DTO 입니다
 */
public class GroupDTO {

    public record GroupCreateRequestDTO(
         String groupName,
         int maxMembers,
         int minMembers
    ){}

    @Builder
    public record GroupCreateResponseDTO(
         Long groupId,
         String groupName,
         int maxMembers,
         int minMembers,
         LocalDateTime createdAt,
         GroupMemberInfoDTO leader
    ){}

    public record GroupUpdateRequestDTO(
         String groupName,
         Integer maxMembers,
         int minMembers
    ){}

    @Schema(description = "그룹 승인 여부 요청 DTO")
    public record GroupReviewRequestDTO (
        @Schema(description = "그룹 승인 여부 (APPROVE: 승인, REJECT: 반려)", example = "REJECT")
        GroupApprovalAction action,
        @Schema(description = "반려시 사유", example="너무 많이 개설해서")
        String reason
    ){}

    @Schema(description = "그룹 모집 상태 변환 요청 DTO")
    public record GroupRecruitmentStatusRequestDTO(
        @Schema(description = "그룹의 모집상태 (OPEN: 모집중, CLOSED: 모집마감)", example = "OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus
    ){}

    @Schema(description = "그룹 멤버 상태 변경 요청 DTO")
    public record GroupMemberStatusRequestDTO (
        @Schema(description = "멤버 상태 (PENDING: 대기, ACTIVE: 활동, INACTIVE: 비활동, REJECTED:가입 거절)", example = "ACTIVE")
        GroupMemberStatus groupMemberStatus,
        @Schema(description = "가입 거절 사유")
        String memberRejectReason
    ){}

    @Schema(description = "전체 그룹 리스트 요약 정보")
    @Builder
    public record GroupListDTO(
        @Schema(description = "그룹 고유 ID", example = "1")
        Long groupId,
        @Schema(description = "연결된 게시글 ID. 목록에서 해당 프로젝트/스터디 상세로 이동 시 사용. 없으면 null", example = "101")
        Long postId,
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        String groupName,
        @Schema(description = "그룹 생성일")
        LocalDateTime createdAt,
        @Schema(description = "현재 활동 중인 인원 수. maxMembers와 함께 N/M 형태 표시용", example = "5")
        int activeMemberCount,
        @Schema(description = "최대 모집 인원 수. 활동수와 함께 N/M 형태 표시용", example = "10")
        int maxMembers,
        @Schema(description = "현재 그룹 상태", example = "REJECTED")
        GroupStatus groupStatus,
        @Schema(description = "승인 반려 사유", example = "너무 많이 만들어서")
        String rejectReason,
        @Schema(description = "현재 그룹 모집 상태",example="OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus,
        @Schema(description = "현재 그룹 리더 ID", example="10")
        Long leaderId,
        @Schema(description = "현재 그룹 리더 기수", example="15")
        Long leaderGeneration,
        @Schema(description = "현재 그룹 리더 이름",example="홍길동")
        String leaderName
    ){}

    @Schema(description = "내가 신청한 그룹 목록. myStatus로 승인/대기/거절/비활동 구분. 프론트에서 라벨·버튼 분기용.")
    @Builder
    public record MyGroupApplicationListDTO (
        @Schema(description = "그룹 고유 ID", example = "1")
        Long groupId,
        @Schema(description = "연결된 게시글 ID. 해당 프로젝트/스터디 상세 이동 시 사용", example = "101")
        Long postId,
        @Schema(description = "그룹 카테고리 번호 (스터디=1, 프로젝트=2)", example = "2")
        Integer category,
        @Schema(description = "그룹명")
        String groupName,
        @Schema(description = "그룹 생성일")
        LocalDateTime createdAt,
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        int activeMemberCount,
        @Schema(description = "최대 모집 인원 수", example = "10")
        int maxMembers,
        @Schema(description = "그룹 활성화 상태", example = "ACTIVE")
        GroupStatus groupStatus,
        @Schema(description = "그룹 모집 상태", example = "OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus,
        @Schema(description = "리더 ID", example = "10")
        Long leaderId,
        @Schema(description = "리더 기수", example = "10")
        Long leaderGeneration,
        @Schema(description = "리더 이름")
        String leaderName,
        @Schema(description = "내 신청/가입 상태. PENDING=승인 대기중, ACTIVE=승인, REJECTED=거절됨, INACTIVE=비활동. 프론트 라벨·버튼(신청취소/다시신청 등) 분기용", example = "PENDING")
        GroupMemberStatus myStatus,
        @Schema(description = "가입 거절 사유")
        String memberRejectReason
    ){}

    /*
    그룹의 정보들을 담고 있는 DTO입니다. 그룹의 id, 그룹의 이름, 멤버 목록(DTO),
    최대 활동 인원과 최소활동 인원,현재 활동인원을 표기합니다
     */
    @Schema(description = "그룹 상세 정보")
    @Builder
    public record GroupInfoDTO(
        @Schema(description = "그룹 고유 ID", example = "1")
        Long groupId,
        @Schema(description = "그룹명", example = "AI 에이전트를 활용한 프로젝트 팀원 모집")
        String groupName,
        @Schema(description = "그룹 생성일")
        LocalDateTime createdAt,
        @Schema(description = "최근 수정일")
        LocalDateTime updatedAt,
        @Schema(description = "현재 모집 상태", example = "OPEN")
        GroupRecruitmentStatus groupRecruitmentStatus,
        @Schema(description = "현재 그룹 상태", example = "APPROVED")
        GroupStatus groupStatus,
        @Schema(description = "승인 반려 사유", example = "너무 많이 만들어서")
        String rejectReason,
        @Schema(description = "현재 활동 중인 인원 수", example = "5")
        int activeMemberCount,
        @Schema(description = "최대 인원 제한", example = "10")
        int maxMembers,
        @Schema(description = "최소 인원 제한", example = "3")
        int minMembers,
        @Schema(description = "그룹 소속 멤버 리스트")
        List<GroupMemberInfoDTO> members
    ){}

    /*
    그룹의 멤버의 정보를 관리하는 DTO입니다
     */
    @Schema(description = "그룹 멤버 개별 정보")
    @Builder
    public record GroupMemberInfoDTO(
        @Schema(description = "그룹-멤버 매핑 ID", example = "50")
        Long groupMemberId,
        @Schema(description = "유저 고유 ID", example = "10")
        Long userId,
        @Schema(description = "유저 기수",example = "15")
        Long userGeneration,
        @Schema(description = "유저 성명", example = "홍길동")
        String userName,
        @Schema(description = "학년", example = "3학년")
        String grade,
        @Schema(description = "전공 학부", example = "컴퓨터공학부")
        String major,
        @Schema(description = "그룹 내 역할 (LEADER: 팀장, MEMBER: 팀원)", example = "MEMBER")
        GroupMemberRole groupMemberRole,
        @Schema(description = "멤버 활동 상태", example = "ACTIVE")
        GroupMemberStatus groupMemberStatus,
        @Schema(description = "가입 거절 사유", example= "모집 요건과 맞지 않아서")
        String memberRejectReason,
        @Schema(description = "가입/신청 일시")
        LocalDateTime createdAt
    ){}

    //보고서 리스트에 사용할 PostReportPreview에 조합할 DTO입니다
    @Builder
    public record GroupPreviewDTO(
         Long groupId,
         String groupName
    ){}

    @Schema(description = "신청 수락/거절 요청 DTO")
    public record ApplicantActionRequestDTO(
        @Schema(description = "처리 액션 (ACCEPT: 수락, REJECT: 거절)", example = "REJECT", allowableValues = {"ACCEPT", "REJECT"})
        MemberApprovalAction action,
        @Schema(description = "가입 거절 사유(ACCEPT 시 NULL)")
        String memberRejectReason
    ){}
}
