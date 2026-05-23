package com.example.cbumanage.gathering.controller;

import com.example.cbumanage.gathering.dto.GatheringDTO;
import com.example.cbumanage.gathering.service.GatheringService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gatherings")
@Tag(name = "모임 일정 관리 컨트롤러", description = "모임 일정 관리 API (회식, 박람회 등)")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "모임 등록 (관리자 전용)",
            description = "관리자만 모임을 등록할 수 있습니다.\n\n" +
                    "- `voteDeadline`: 투표 마감 일시. 생략하거나 null로 보내면 마감 없음\n" +
                    "- `type`: DINING(회식) / FAIR(박람회) / OTHER(기타)\n" +
                    "- `allMembersTarget`: OTHER 타입일 때만 사용. true면 전체 동아리원 자동 포함, false(또는 생략)면 오픈 투표\n" +
                    "  - DINING은 무조건 전체 동아리원 포함, FAIR는 무조건 오픈 투표로 처리됨"
    )
    public ApiResponse<GatheringDTO.CreateResponse> createGathering(
            @Validated @RequestBody GatheringDTO.CreateRequest request,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return ApiResponse.success(gatheringService.createGathering(request, memberId));
    }

    @GetMapping
    @Operation(
            summary = "모임 목록 조회",
            description = "전체 모임 목록을 모임 일시 내림차순으로 반환합니다.\n\n" +
                    "- `myStatus`: 내가 해당 모임에 투표한 상태 (미투표 시 null)\n" +
                    "- `voteClosed`: 투표 마감 여부 (마감일이 지났으면 true)"
    )
    public ApiResponse<List<GatheringDTO.GatheringResponse>> getGatherings(Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return ApiResponse.success(gatheringService.getGatherings(memberId));
    }

    @GetMapping("/{gatheringId}")
    @Operation(
            summary = "모임 상세 조회",
            description = "특정 모임의 상세 정보를 반환합니다. 내 투표 상태와 참석 현황 요약 포함"
    )
    public ApiResponse<GatheringDTO.GatheringResponse> getGathering(
            @Parameter(description = "조회할 모임 ID", example = "1") @PathVariable Long gatheringId,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return ApiResponse.success(gatheringService.getGathering(gatheringId, memberId));
    }

    @PatchMapping("/{gatheringId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "모임 수정",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 수정할 수 있습니다. (다른 유저가 요청하면 403)\n" +
                    "- 투표 마감을 연장하려면 `voteDeadline`을 미래 일시로 변경하면 됩니다."
    )
    public ApiResponse<GatheringDTO.GatheringResponse> updateGathering(
            @Parameter(description = "수정할 모임 ID", example = "1") @PathVariable Long gatheringId,
            @RequestBody GatheringDTO.UpdateRequest request,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return ApiResponse.success(gatheringService.updateGathering(gatheringId, request, memberId));
    }

    @PatchMapping("/{gatheringId}/close")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "모임 마감",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 수동으로 마감할 수 있습니다. 마감 후 투표가 불가능합니다. (다른 유저가 요청하면 403)"
    )
    public ApiResponse<Void> closeGathering(
            @Parameter(description = "마감할 모임 ID", example = "1") @PathVariable Long gatheringId,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        gatheringService.closeGathering(gatheringId, memberId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{gatheringId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "모임 삭제",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 삭제할 수 있습니다. (다른 유저가 요청하면 403)\n\n소프트 딜리트 처리됩니다."
    )
    public ApiResponse<Void> deleteGathering(
            @Parameter(description = "삭제할 모임 ID", example = "1") @PathVariable Long gatheringId,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        gatheringService.deleteGathering(gatheringId, memberId);
        return ApiResponse.success();
    }

    @PostMapping("/{gatheringId}/vote")
    @Operation(
            summary = "참석 투표",
            description = "모임에 대한 참석 여부를 투표합니다.\n\n" +
                    "- `status`: ATTENDING(참석) / NOT_ATTENDING(불참) / UNDECIDED(미정)\n" +
                    "- 이미 투표한 경우 재투표하면 이전 값이 덮어씌워집니다.\n" +
                    "- 투표 마감(`voteClosed: true`)된 모임에는 투표할 수 없습니다. (400 반환)"
    )
    public ApiResponse<Void> vote(
            @Parameter(description = "투표할 모임 ID", example = "1") @PathVariable Long gatheringId,
            @Validated @RequestBody GatheringDTO.VoteRequest request,
            Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        gatheringService.vote(gatheringId, request.status(), memberId);
        return ApiResponse.success();
    }

    @GetMapping("/{gatheringId}/attendance")
    @Operation(
            summary = "참석 명단 조회",
            description = "투표 결과를 상태별 멤버 목록으로 반환합니다.\n\n" +
                    "- `attendingMembers`: 참석 멤버 목록\n" +
                    "- `notAttendingMembers`: 불참 멤버 목록\n" +
                    "- `undecidedMembers`: 미정 멤버 목록"
    )
    public ApiResponse<GatheringDTO.AttendanceListResponse> getAttendanceList(
            @Parameter(description = "조회할 모임 ID", example = "1") @PathVariable Long gatheringId) {
        return ApiResponse.success(gatheringService.getAttendanceList(gatheringId));
    }
}
