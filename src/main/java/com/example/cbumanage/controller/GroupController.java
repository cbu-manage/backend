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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Group API", description = "그룹 생성, 가입 신청 및 관리 관련 API")
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

    @Operation(summary = "그룹 가입 요청",
            description = "로그인한 유저가 그룹에 가입 신청을 합니다. 초기 상태는 PENDING입니다.")
    @PostMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<GroupDTO.GroupMemberInfoDTO>> applyGroupMember(
            @Parameter(description = "가입할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            HttpServletRequest httpServletRequest) {
        Long memberId = extractUserIdFromCookie(httpServletRequest);
        try {
            GroupDTO.GroupMemberInfoDTO groupMemberInfoDTO = groupService.addGroupMember(groupId, memberId);
            return ResultResponse.ok(SuccessCode.CREATED, groupMemberInfoDTO);
        }catch (EntityExistsException e){
            return ResultResponse.error(ErrorCode.ALREADY_JOINED_MEMBER);
        }


    }

    @Operation(summary = "그룹 가입 취소", description = "본인이 신청한 가입 대기(PENDING) 상태를 취소합니다.")
    @DeleteMapping("/group/{groupId}/application")
    public ResponseEntity<ResultResponse<Void>> cancelGroupApplication(
            @Parameter(description = "가입 취소할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        groupService.cancelApplication(groupId, userId);
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(summary = "신청 인원 확인 (팀장 전용)", description = "그룹 리더가 현재 가입 대기 중인(PENDING) 유저 목록을 조회합니다.")
    @GetMapping("/group/{groupId}/applicants")
    public ResponseEntity<ResultResponse<List<GroupDTO.GroupMemberInfoDTO>>> getGroupApplicants(
            @Parameter(description = "조회할 그룹의 ID", example = "1")
            @PathVariable Long groupId,
            HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        List<GroupDTO.GroupMemberInfoDTO> applicants = groupService.getPendingGroupMember(groupId, userId);
        return ResultResponse.ok(SuccessCode.SUCCESS, applicants);
    }

    @Operation(
            summary = "자신이 가입한 그룹 조회하기",
            description = "유저가 현재 가입되어 있는 그룹들을 볼 수 있는 메소드 입니다.." +
                    "유저가 가입된 그룹 목록을 불러오거나(마이페이지)+보고서,모집글같은 게시글에 그룹을 추가해야할때 해당 기능을 사용합니다"
    )
    @GetMapping("/groupMember/{userId}/groups")
    public ResponseEntity<ResultResponse<List<GroupDTO.GroupInfoDTO>>> getGroupMember(
            @Parameter(description = "조회할 유저의 식별자(ID)", example = "10") @PathVariable Long userId) {
        List<GroupDTO.GroupInfoDTO> groupInfos = groupService.getGroupByMemberId(userId);
        return ResultResponse.ok(SuccessCode.SUCCESS,groupInfos);
    }

    @Operation(summary = "그룹 상세 정보 조회", description = "그룹 ID를 통해 그룹의 기본 정보를 상세 조회합니다.")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ResultResponse<GroupDTO.GroupInfoDTO>> getGroup(
            @Parameter(description = "그룹 ID", example = "1") @PathVariable Long groupId){
        GroupDTO.GroupInfoDTO groupInfoDTO = groupService.getGroupById(groupId);
        return ResultResponse.ok(SuccessCode.SUCCESS, groupInfoDTO);
    }


    @Operation(
            summary = "그룹 모집 상태 변경(팀장 전용)",
            description = "그룹의 모집 상태를 변경하는 메소드 입니다. OPEN,CLOSED로 구분됩니다"
    )
    @PatchMapping("/group/{groupId}/recruitment")
    public ResponseEntity<ResultResponse<Void>> changeGroupRecruitmentStatus
            (@PathVariable Long groupId ,
             @Parameter(description = "OPEN,CLOSED 중 원하는 상태를 보냅니다.") @RequestBody GroupDTO.GroupRecruitmentStatusRequestDTO req,
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
            summary = "멤버 상태 변경 (활동/비활동) (팀장 전용)",
            description = "그룹 운영 중에 멤버 상태가 변경될 때 사용됩니다. 팀장 전용."
    )
    @PatchMapping("/groupMember/{groupMemberId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupMemberStatus(
            @PathVariable Long groupMemberId,
            @RequestBody GroupDTO.GroupMemberStatusRequestDTO req,
            HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            if (req.getGroupMemberStatus()==GroupMemberStatus.ACTIVE) {
                groupService.activateGroupMember(groupMemberId, userId);
            }
            if (req.getGroupMemberStatus() == GroupMemberStatus.INACTIVE) {
                groupService.deactivateGroupMember(groupMemberId, userId);
            } else {
                return ResultResponse.error(ErrorCode.INVALID_REQUEST);
            }
        } catch (ResponseStatusException e) {
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "가입 신청 수락/거부 (팀장 전용)",
            description = "body의 action으로 수락(ACCEPT) 또는 거부(REJECT) 처리. 수락 시 PENDING→ACTIVE, 거부 시 그룹 멤버에서 삭제."
    )
    @PatchMapping("/groupMember/{groupMemberId}/applicant")
    public ResponseEntity<ResultResponse<Void>> handleApplicantAction(
            @Parameter(description = "그룹 멤버 고유 식별자(groupMemberId)", example = "50") @PathVariable Long groupMemberId,
            @RequestBody GroupDTO.ApplicantActionRequestDTO req, HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        try {
            if ("ACCEPT".equalsIgnoreCase(req.getAction())) {
                groupService.activateGroupMember(groupMemberId, userId);
            } else if ("REJECT".equalsIgnoreCase(req.getAction())) {
                groupService.rejectApplicant(groupMemberId, userId);
            } else {
                return ResultResponse.error(ErrorCode.INVALID_REQUEST);
            }
        } catch (ResponseStatusException e) {
            return ResultResponse.error(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            return ResultResponse.error(ErrorCode.NOT_FOUND);
        }
        return ResultResponse.ok(SuccessCode.UPDATED, null);
    }

    @Operation(
            summary = "그룹 전체 조회 하기 (관리자 전용)",
            description = "개설된 그룹들을 활성화 시키기 위해 현재 개설 되어 있는 그룹을 조회 할 수 있도록 합니다."
    )
    @GetMapping("/admin/group")
    public ResponseEntity<ResultResponse<List<GroupDTO.GroupListDTO>>> getAllGroups(HttpServletRequest httpServletRequest) {
        Long userId = extractUserIdFromCookie(httpServletRequest);
        List<GroupDTO.GroupListDTO> groupAllList = groupService.getAllGroups(userId);
        return ResultResponse.ok(SuccessCode.SUCCESS, groupAllList);
    }

    @Operation(
            summary = "그룹 상태 변경하기(관리자 전용)",
            description = "개설된 그룹의 활성/비활성 상태를 변경합니다. 운영자가 관리합니다"
    )
    @PatchMapping("/admin/group/{groupId}/status")
    public ResponseEntity<ResultResponse<Void>> changeGroupStatus(
            @PathVariable Long groupId ,
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





    /*-------------------------------------------------------------------*/
//    @Operation(
//            summary = "그룹 생성 요청",
//            description = "req과 그룹을 생성하는 userId를 받아 그룹을 생성하고 생성자를 그룹의 leader로 추가하여 반환합니다"
//    )
//    @PostMapping("/group")
//    public ResponseEntity<ResultResponse<GroupDTO.GroupCreateResponseDTO>> createGroup(@RequestBody GroupDTO.GroupCreateRequestDTO req,
//                                                                                       HttpServletRequest httpServletRequest) {
//        Long userId = extractUserIdFromCookie(httpServletRequest);
//        GroupDTO.GroupCreateResponseDTO groupCreateResponseDTO = groupService.createGroup(req,userId);
//        return ResultResponse.ok(SuccessCode.CREATED, groupCreateResponseDTO);
//    }
//
//
//    @Operation(
//            summary = "그룹 이름 검색 메소드",
//            description = "그룹을 그룹의 이름으로 검색하는 메소드 입니다."
//    )
//    @GetMapping("/group/search")
//    public ResponseEntity<ResultResponse<List<GroupDTO.GroupInfoDTO>>> searchGroupByGroupName(@RequestParam("groupName") String groupName){
//        List<GroupDTO.GroupInfoDTO> groupInfoDTOS = groupService.getGroupByGroupNameAndStatus(groupName);
//        return ResultResponse.ok(SuccessCode.SUCCESS, groupInfoDTOS);
//    }
//


















}

