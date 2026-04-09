package com.example.cbumanage.group.controller;

import com.example.cbumanage.group.dto.GroupDTO;
import com.example.cbumanage.group.entity.enums.GroupMemberStatus;
import com.example.cbumanage.group.entity.enums.GroupStatus;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.group.entity.enums.MemberApprovalAction;
import com.example.cbumanage.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@Tag(name = "Group API", description = "그룹 생성, 가입 신청 및 관리 관련 API")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /* ============================================================
     * [ 일반 유저 및 본인 관련 API ]
     * ============================================================ */
    @Operation(summary = "그룹 가입 요청",
            description = "로그인한 유저가 그룹에 가입 신청을 합니다. 초기 상태는 PENDING입니다.")
    @PostMapping("/{groupId}/members")
    public ApiResponse<GroupDTO.GroupMemberInfoDTO> applyGroupMember(
            @Parameter(description = "가입할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        GroupDTO.GroupMemberInfoDTO groupMemberInfoDTO = groupService.addGroupMember(groupId, memberId);
        return ApiResponse.success(groupMemberInfoDTO);
    }

    @Operation(summary = "그룹 가입 취소", description = "본인이 신청한 가입 대기(PENDING) 상태를 취소합니다.")
    @DeleteMapping("/{groupId}/members/me")
    public ApiResponse<Void> cancelGroupApplication(
            @Parameter(description = "가입 취소할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        groupService.cancelApplication(groupId, userId);
        return ApiResponse.success();
    }

    @Operation(
            summary = "자신이 가입한 그룹 조회",
            description = "가입 완료(ACTIVE)된 그룹 목록 조회. 응답에 postId(연결 게시글 ID), activeMemberCount, maxActiveMembers 포함. " +
                    "postId로 해당 프로젝트/스터디 상세 이동, 인원은 활동수/최대인원(예: 2/4) 표시용."
    )
    @GetMapping("/my")
    public ApiResponse<List<GroupDTO.GroupListDTO>> getMyJoinedGroups(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<GroupDTO.GroupListDTO> groupInfos = groupService.getJoinedGroups(userId);
        return ApiResponse.success(groupInfos);
    }

    @Operation(
            summary = "내가 신청한 그룹 목록 (승인/대기/거절/비활동)",
            description = "본인이 신청한 그룹을 카테고리별로 조회."+
                    "myStatus로 프론트에서 라벨 분기: PENDING=승인 대기중, ACTIVE=승인, REJECTED=거절됨, INACTIVE=비활동. " +
                    "신청 취소는 myStatus가 PENDING일 때만 노출"
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "category", description = "카테고리 번호 (스터디=1, 프로젝트=2). 미입력시(null) 전체보기", example = "2")
    })
    @GetMapping("/my/applications")
    public ApiResponse<Page<GroupDTO.MyGroupApplicationListDTO>> getMyAppliedGroups(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer category,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Order.desc("createdAt"))
        );
        Page<GroupDTO.MyGroupApplicationListDTO> list =
                groupService.getMyAppliedGroupsByCategory(userId, category, pageable);
        return ApiResponse.success(list);
    }

    @Operation(summary = "그룹 상세 정보 조회", description = "그룹 ID를 통해 그룹의 기본 정보를 상세 조회합니다.")
    @GetMapping("/{groupId}")
    public ApiResponse<GroupDTO.GroupInfoDTO> getGroup(
            @Parameter(description = "그룹 ID", example = "1") @PathVariable Long groupId){
        GroupDTO.GroupInfoDTO groupInfoDTO = groupService.getGroupById(groupId);
        return ApiResponse.success(groupInfoDTO);
    }

    /* ============================================================
     * [ 팀장(Leader) 전용 API ]
     * ============================================================ */
    @Operation(
            summary = "신청 인원 확인 (팀장 전용)",
            description = "가입 대기(PENDING) 유저만 조회. 수락/거절 버튼용. 전체 상태가 필요하면 GET .../applicants/overview 사용."
    )
    @GetMapping("/{groupId}/applicants")
    public ApiResponse<List<GroupDTO.GroupMemberInfoDTO>> getGroupApplicants(
            @Parameter(description = "조회할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<GroupDTO.GroupMemberInfoDTO> applicants = groupService.getPendingGroupMember(groupId, userId);
        return ApiResponse.success(applicants);
    }

    @Operation(
            summary = "신청 인원 상태 전체 보기(팀장 전용)",
            description = "그룹 팀원(MEMBER) 전원을 상태별로 조회. PENDING/ACTIVE/REJECTED/INACTIVE 모두 포함."
    )
    @GetMapping("/{groupId}/applicants/overview")
    public ApiResponse<List<GroupDTO.GroupMemberInfoDTO>> getGroupApplicantsOverview(
            @Parameter(description = "조회할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<GroupDTO.GroupMemberInfoDTO> overview = groupService.getGroupApplicantsOverview(groupId, userId);
        return ApiResponse.success(overview);
    }


    @Operation(
            summary = "그룹 모집 상태 변경 (팀장 전용)",
            description = "요청 body에 OPEN 또는 CLOSED 전달. 그룹 모집 상태 변경 시 연결된 프로젝트/스터디 게시글의 recruiting도 함께 동기화됨."+
                    "만약 모집마감 시 pending상태가 남아 있는 경우 모두 rejected 처리"
    )
    @PatchMapping("/{groupId}/recruitment")
    public ApiResponse<Void> changeGroupRecruitmentStatus
            (@PathVariable Long groupId ,
             @Parameter(description = "OPEN,CLOSED 중 원하는 상태를 보냅니다.") @RequestBody GroupDTO.GroupRecruitmentStatusRequestDTO req,
             Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        groupService.updateGroupRecruitment(groupId, userId, req.groupRecruitmentStatus());
        return ApiResponse.success();
    }

    @Operation(
            summary = "멤버 상태 변경 (활동/비활동) (팀장 전용)",
            description = "그룹 운영 중에 멤버 상태가 변경될 때 사용됩니다. 팀장 전용."
    )
    @PatchMapping("/members/{groupMemberId}/status")
    public ApiResponse<Void> changeGroupMemberStatus(
            @PathVariable Long groupMemberId,
            @RequestBody GroupDTO.GroupMemberStatusRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        groupService.updateStatusGroupMember(groupMemberId, userId, req.groupMemberStatus(), req.memberRejectReason());
        return ApiResponse.success();
    }

    @Operation(
            summary = "가입 신청 수락/거부 (팀장 전용)",
            description = "body의 action으로 수락(ACCEPT) 또는 거부(REJECT) 처리. 수락 시 PENDING→ACTIVE, 거부 시 PENDING->REJECTED."
    )
    @PatchMapping("/members/{groupMemberId}/applicant")
    public ApiResponse<Void> handleApplicantAction(
            @Parameter(description = "그룹 멤버 고유 식별자(groupMemberId)", example = "50") @PathVariable Long groupMemberId,
            @RequestBody GroupDTO.ApplicantActionRequestDTO req, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        GroupMemberStatus targetStatus = (req.action() == MemberApprovalAction.ACCEPT)
                ? GroupMemberStatus.ACTIVE
                : GroupMemberStatus.REJECTED;
        groupService.updateStatusGroupMember(groupMemberId, userId, targetStatus,req.memberRejectReason());
        return ApiResponse.success();
    }

    /* ============================================================
     * [ 관리자(Admin) 전용 API ]
     * ============================================================ */
    @Operation(
            summary = "전체 그룹 상태 카테고리별로 조회 하기 (관리자 전용)",
            description = "개설된 그룹들을 상태 카테고리별로 분류하여 확인할 수 있습니다."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "groupStatus", description = "그룹 승인 상태 필터 (미입력시 전체) " +
                    "ACTIVE=승인완료, PENDING=승인 대기중, REJECTED=승인 거절, " +
                    "RESUBMITTED=승인 재요청, INACTIVE=활동종료", example = "ACTIVE")
    })
    @GetMapping("/admin")
    public ApiResponse<Page<GroupDTO.GroupListDTO>> getAllGroups(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) GroupStatus groupStatus,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<GroupDTO.GroupListDTO> groupAllList = groupService.getAllGroups(userId, groupStatus, pageable);
        return ApiResponse.success(groupAllList);
    }

    @Operation(
            summary = "그룹 승인 여부 변경하기(관리자 전용)",
            description = "개설된 그룹의 승인 상태(APPROVE/REJECT) 여부를 변경하며 반려시 사유를 추가합니다. 운영자가 관리합니다 "
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{groupId}/admin/status")
    public ApiResponse<Void> changeGroupStatus(
            @PathVariable Long groupId ,
            @Parameter(description = "APPROVE,REJECT 구분되며 원하는 상태를 보냅니다") @RequestBody GroupDTO.GroupReviewRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        groupService.updateGroupStatusAdmin(groupId, userId, req);
        return ApiResponse.success();
    }
}
