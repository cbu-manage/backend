package com.example.cbumanage.controller;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name="그룹 컨트롤러")
public class GroupController {
    private GroupService groupService;

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(
            summary = "그룹 생성 요청",
            description = "req과 그룹을 생성하는 userId를 받아 그룹을 생성하고 생성자를 그룹의 leader로 추가하여 반환합니다"
    )
    @PostMapping("/group")
    public ResponseEntity<ResultResponse<GroupDTO.GroupCreateResponseDTO>> createGroup(@RequestBody GroupDTO.GroupCreateRequestDTO req,
                                                                                       @Parameter(description = "그룹을 생성하는 팀장의 id입니다 자동으로 팀장으로 추가되며, 추후에 토큰을 통한 자동 주입으로 수정할 예정입니다") @RequestParam Long userId){
        GroupDTO.GroupCreateResponseDTO groupCreateResponseDTO = groupService.createGroup(req,userId);
        return ResultResponse.ok(SuccessCode.CREATED, groupCreateResponseDTO);
    }

    @Operation(
            summary = "그룹 참가 요청",
            description = "유저가 그룹에 참가 신청하는 메소드 입니다. Inactive,Member로 설정된 멤버가 그룹의 멤버 리스트에 추가됩니다"
    )
    @PostMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<GroupDTO.GroupMemberInfoDTO>> applyGroupMember(@PathVariable Long groupId,
                                                                                        @Parameter(description ="그룹에 참가신청하는 유저의 id입니다. status가 pending인 멤버로 멤버에 추가됩니다. 추후 토큰을 통한 자동 주입으로 수정할 예정입니다" )@RequestParam Long memberId){
        GroupDTO.GroupMemberInfoDTO groupMemberInfoDTO = groupService.addGroupMember(groupId,memberId);
        return ResultResponse.ok(SuccessCode.CREATED, groupMemberInfoDTO);
    }

    @Operation(
            summary = "그룹 검색 메소드",
            description = "그룹을 groupId를 사용해 검색하는 메소드 입니다."
    )
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<GroupDTO.GroupInfoDTO>> getGroup(@PathVariable Long groupId){
        GroupDTO.GroupInfoDTO groupInfoDTO = groupService.getGroupById(groupId);
        return ResultResponse.ok(SuccessCode.SUCCESS, groupInfoDTO);
    }

    @Operation(
            summary = "그룹 이름 검색 메소드",
            description = "그룹을 그룹의 이름으로 검색하는 메소드 입니다."
    )
    @GetMapping("/group/search")
    public ResponseEntity<ResultResponse<List<GroupDTO.GroupInfoDTO>>> searchGroupByGroupName(@RequestParam("groupName") String groupName){
        List<GroupDTO.GroupInfoDTO> groupInfoDTOS = groupService.getGroupByGroupNameAndStatus(groupName);
        return ResultResponse.ok(SuccessCode.SUCCESS, groupInfoDTOS);
    }

    @Operation(
            summary = "유저 소속 그룹 검색",
            description = "유저가 현재 가입되어 있는 그룹들을 볼 수 있는 메소드 입니다.." +
                    "유저가 가입된 그룹 목록을 불러오거나(마이페이지)+보고서,모집글같은 게시글에 그룹을 추가해야할때 해당 기능을 사용합니다"


    )
    @GetMapping("/groupMember/{userId}/groups")
    public ResponseEntity<ResultResponse<List<GroupDTO.GroupInfoDTO>>> getGroupMember(@PathVariable Long userId)
                                                                                      {
        List<GroupDTO.GroupInfoDTO> groupInfos = groupService.getGroupByMemberId(userId);
        return ResultResponse.ok(SuccessCode.SUCCESS,groupInfos);
    }


    @Operation(
            summary = "그룹 모집상태 변경",
            description = "그룹의 모집상태를 변경하는 메소드 입니다. OPEN,CLOSE로 구분됩니다"
    )
    @PatchMapping("/group/{groupId}/recruitment")
    public ResponseEntity<ResultResponse<Void>> changeGroupRecruitmentStatus(@PathVariable Long groupId ,
                                                                             @Parameter(description = "OPEN,CLOSE 중 원하는 상태를 보냅니다.") @RequestBody GroupDTO.GroupRecruitmentStatusRequestDTO req){
        if (req.getGroupRecruitmentStatus() == GroupRecruitmentStatus.CLOSED){
            groupService.closeGroupRecruitment(groupId);
        }
        if(req.getGroupRecruitmentStatus() == GroupRecruitmentStatus.OPEN){
            groupService.openGroupRecruitment(groupId);
        }
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "그룹상태 변경 메소드",
            description = "그룹의 활성/비활성 상태를 변경시키는 메소드입니다. 운영자가 관리합니다"
    )
    @PatchMapping("/group/{groupId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupStatus(@PathVariable Long groupId ,@Parameter(description = "ACTIVE,INACTIVE로 구분되며 원하는 상태를 보냅니다") @RequestBody GroupDTO.GroupStatusRequestDTO req){
        if (req.getGroupStatus() == GroupStatus.ACTIVE){
            groupService.activateGroupStatus(groupId);
        }
        if(req.getGroupStatus() == GroupStatus.INACTIVE){
            groupService.deactivateGroupStatus(groupId);
        }
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "멤버 상태 변경",
            description = "멤버의 상태를 변경시키는 메소드입니다. 가입신청하여PENDING상태인 member를 ACTIVE상태로 변경시키거나, 그만둔 멤버를 INACTIVE상태로 변경시킵니다. 팀장이 관리합니다"
    )
    @PatchMapping("/groupMember/{groupMemberId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupMemberStatus(@PathVariable Long groupMemberId , @RequestBody GroupDTO.GroupMemberStatusRequestDTO req){
        if (req.getGroupMemberStatus() == GroupMemberStatus.ACTIVE){
            groupService.activateGroupMember(groupMemberId);
        }
        if(req.getGroupMemberStatus() == GroupMemberStatus.INACTIVE){
            groupService.deactivateGroupMember(groupMemberId);
        }
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }















}

