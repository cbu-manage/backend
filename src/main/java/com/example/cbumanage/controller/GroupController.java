package com.example.cbumanage.controller;

import com.example.cbumanage.dto.GroupDTO;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import com.example.cbumanage.model.enums.GroupRecruitmentStatus;
import com.example.cbumanage.model.enums.GroupStatus;
import com.example.cbumanage.response.ErrorCode;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.GroupService;
import com.example.cbumanage.utils.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name="그룹 컨트롤러")
public class GroupController {
    private final GroupService groupService;
    private final JwtProvider jwtProvider;

    @Autowired
    public GroupController(GroupService groupService, JwtProvider jwtProvider) {
        this.groupService = groupService;
        this.jwtProvider = jwtProvider;
    }


    //쿠키에서 userId를 추출하는 코드 입니다
    private Long extractUserIdFromCookie(HttpServletRequest httpServletRequest) {
        String token = null;

        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN not found");
        }

        Map<String, Object> tokenInfo;
        try {
            tokenInfo = jwtProvider.parseJwt(
                    token,
                    Map.of(
                            "user_id", Long.class,
                            "student_number", Long.class,
                            "role", JSONArray.class,
                            "permissions", JSONArray.class
                    )
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }

        Long user_id = (Long) tokenInfo.get("user_id");

        return user_id;

    }

    @Operation(
            summary = "그룹 생성 요청",
            description = "req과 그룹을 생성하는 userId를 받아 그룹을 생성하고 생성자를 그룹의 leader로 추가하여 반환합니다"
    )
    @PostMapping("/group")
    public ResponseEntity<ResultResponse<GroupDTO.GroupCreateResponseDTO>> createGroup(@RequestBody GroupDTO.GroupCreateRequestDTO req,
                                                                                       HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        GroupDTO.GroupCreateResponseDTO groupCreateResponseDTO = groupService.createGroup(req,userId);
        return ResultResponse.ok(SuccessCode.CREATED, groupCreateResponseDTO);
    }

    @Operation(
            summary = "그룹 참가 요청",
            description = "유저가 그룹에 참가 신청하는 메소드 입니다. Inactive,Member로 설정된 멤버가 그룹의 멤버 리스트에 추가됩니다"
    )
    @PostMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<GroupDTO.GroupMemberInfoDTO>> applyGroupMember(@PathVariable Long groupId,
                                                                                        HttpServletRequest httpServletRequest) {
        Long memberId = extractUserIdFromCookie(httpServletRequest);
        try {
            GroupDTO.GroupMemberInfoDTO groupMemberInfoDTO = groupService.addGroupMember(groupId, memberId);
            return ResultResponse.ok(SuccessCode.CREATED, groupMemberInfoDTO);
        }catch (EntityExistsException e){
            return ResultResponse.error(ErrorCode.ALREADY_JOINED_MEMBER);
        }


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
                                                                             @Parameter(description = "OPEN,CLOSE 중 원하는 상태를 보냅니다.") @RequestBody GroupDTO.GroupRecruitmentStatusRequestDTO req,
                                                                             HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            if (req.getGroupRecruitmentStatus() == GroupRecruitmentStatus.CLOSED){
                groupService.closeGroupRecruitment(groupId,userId);
            }
            if(req.getGroupRecruitmentStatus() == GroupRecruitmentStatus.OPEN){
                groupService.openGroupRecruitment(groupId,userId);
            }
        }
        catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e) {
            ErrorCode code = ErrorCode.NOT_FOUND;
            return ResponseEntity
                    .status(code.getHttpStatus())
                    .body(new ResultResponse<>(
                            code.getCode(),
                            code.getMessage() + ": " + e.getMessage(),
                            null
                    ));
        }

        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "그룹상태 변경 메소드",
            description = "그룹의 활성/비활성 상태를 변경시키는 메소드입니다. 운영자가 관리합니다"
    )
    @PatchMapping("/group/{groupId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupStatus(@PathVariable Long groupId ,
                                                                  @Parameter(description = "ACTIVE,INACTIVE로 구분되며 원하는 상태를 보냅니다") @RequestBody GroupDTO.GroupStatusRequestDTO req,
                                                                  HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            if (req.getGroupStatus() == GroupStatus.ACTIVE){
                groupService.activateGroupStatus(groupId,userId);
            }
            if(req.getGroupStatus() == GroupStatus.INACTIVE){
                groupService.deactivateGroupStatus(groupId,userId);
            }
        }
        catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }

        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "멤버 상태 변경",
            description = "멤버의 상태를 변경시키는 메소드입니다. 가입신청하여PENDING상태인 member를 ACTIVE상태로 변경시키거나, 그만둔 멤버를 INACTIVE상태로 변경시킵니다. 팀장이 관리합니다"
    )
    @PatchMapping("/groupMember/{groupMemberId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupMemberStatus(@PathVariable Long groupMemberId ,
                                                                        @RequestBody GroupDTO.GroupMemberStatusRequestDTO req,
                                                                        HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            if (req.getGroupMemberStatus() == GroupMemberStatus.ACTIVE){
                groupService.activateGroupMember(groupMemberId,userId);
            }
            if(req.getGroupMemberStatus() == GroupMemberStatus.INACTIVE){
                groupService.deactivateGroupMember(groupMemberId,userId);
            }
        }
        catch (ResponseStatusException e){
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }

        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }















}

