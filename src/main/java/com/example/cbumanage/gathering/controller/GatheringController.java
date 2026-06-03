package com.example.cbumanage.gathering.controller;

import com.example.cbumanage.gathering.dto.GatheringDTO;
import com.example.cbumanage.gathering.service.GatheringService;
import com.example.cbumanage.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gatherings")
@Tag(name = "모임 일정 관리 컨트롤러", description = "모임 일정 관리 API (회식, MT, 박람회, 행사 등)")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "모임 등록 (관리자 전용)",
            description = "관리자만 모임을 등록할 수 있습니다.\n\n" +
                    "**모임 유형(type)**\n" +
                    "| 값 | 이름 | 투표 방식 |\n" +
                    "|---|---|---|\n" +
                    "| `DINING` | 회식 | 전체 동아리원 자동 포함 (고정) |\n" +
                    "| `MT` | MT | 전체 동아리원 자동 포함 (고정) |\n" +
                    "| `FAIR` | 박람회 | 오픈 투표 (고정) |\n" +
                    "| `EVENT` | 행사 | `allMembersTarget` 값으로 결정 |\n" +
                    "| `OTHER` | 기타 | `allMembersTarget` 값으로 결정 |\n\n" +
                    "- `voteDeadline`: 투표 마감 일시. 생략하거나 null이면 마감 없음\n" +
                    "- `allMembersTarget`: EVENT·OTHER 타입일 때만 유효. true면 전체 동아리원 자동 포함 후 NOT_RESPONDED로 초기화, false(기본값)면 오픈 투표"
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
                    "- `myStatus`: 내가 해당 모임에 투표한 상태. 미투표 시 null\n" +
                    "- `voteClosed`: 투표 마감 여부 (voteDeadline이 지났으면 true)\n" +
                    "- `viewCount`: 조회수\n" +
                    "- `summary.total`: 전체 대상 인원 수\n" +
                    "- `summary.unanswered`: 아직 투표하지 않은 인원 수 (NOT_RESPONDED 상태)"
    )
    public ApiResponse<List<GatheringDTO.GatheringResponse>> getGatherings(Authentication authentication) {
        Long memberId = Long.parseLong(authentication.getName());
        return ApiResponse.success(gatheringService.getGatherings(memberId));
    }

    @GetMapping("/{gatheringId}")
    @Operation(
            summary = "모임 상세 조회",
            description = "특정 모임의 상세 정보를 반환합니다. 조회할 때마다 `viewCount`가 1 증가합니다.\n\n" +
                    "- `myStatus`: 내 투표 상태. 미투표 시 null\n" +
                    "- `summary.total`: 전체 대상 인원 수\n" +
                    "- `summary.unanswered`: 아직 투표하지 않은 인원 수 (NOT_RESPONDED 상태)"
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
            summary = "모임 수정 (관리자 전용)",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 수정할 수 있습니다. 다른 유저가 요청하면 403을 반환합니다.\n\n" +
                    "- `type`(모임 유형)은 변경 불가이므로 유형을 바꾸려면 삭제 후 재생성\n" +
                    "- `voteDeadline`을 미래 일시로 변경하면 마감된 투표를 재오픈할 수 있습니다."
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
            summary = "모임 투표 수동 마감 (관리자 전용)",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 수동으로 마감할 수 있습니다. 다른 유저가 요청하면 403을 반환합니다.\n\n" +
                    "마감 후에는 투표가 불가능합니다(`voteClosed: true`). `voteDeadline`을 미래 일시로 수정하면 재오픈됩니다."
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
            summary = "모임 삭제 (관리자 전용)",
            description = "관리자(ROLE_ADMIN)이면서 본인이 등록한 모임만 삭제할 수 있습니다. 다른 유저가 요청하면 403을 반환합니다.\n\n" +
                    "소프트 딜리트 처리됩니다. (`isDeleted: true` 로 변경, 목록·상세 조회에서 노출되지 않음)"
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
                    "**투표 가능한 status 값**\n" +
                    "| 값 | 의미 |\n" +
                    "|---|---|\n" +
                    "| `ATTENDING` | 참석 |\n" +
                    "| `NOT_ATTENDING` | 불참 |\n" +
                    "| `UNDECIDED` | 미정 |\n\n" +
                    "> `NOT_RESPONDED`는 시스템이 자동 초기화할 때 사용하는 값으로 투표 요청에 사용할 수 없습니다.\n\n" +
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
            summary = "참석 명단 조회 (일반)",
            description = "투표 결과를 상태별 멤버 목록으로 반환합니다. **이름·기수만 노출**됩니다.\n\n" +
                    "- `attendingMembers`: 참석(ATTENDING) 멤버 목록\n" +
                    "- `notAttendingMembers`: 불참(NOT_ATTENDING) 멤버 목록\n" +
                    "- `undecidedMembers`: 미정(UNDECIDED) 멤버 목록\n" +
                    "- `summary.unanswered`: 미응답(NOT_RESPONDED) 인원 수 (목록은 미제공)\n\n"
    )
    public ApiResponse<GatheringDTO.AttendanceListResponse> getAttendanceList(
            @Parameter(description = "조회할 모임 ID", example = "1") @PathVariable Long gatheringId) {
        return ApiResponse.success(gatheringService.getAttendanceList(gatheringId));
    }

    @GetMapping("/{gatheringId}/attendance/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "참석 명단 조회 (관리자 전용)",
            description = "관리자 전용. 학번·학과·학년·투표일시 및 미응답자 목록을 포함한 전체 명단을 반환합니다.\n\n" +
                    "- `attendingMembers`: 참석 멤버 목록\n" +
                    "- `notAttendingMembers`: 불참 멤버 목록\n" +
                    "- `undecidedMembers`: 미정 멤버 목록\n" +
                    "- `unansweredMembers`: 미응답 멤버 목록 (allMembersTarget=true인 모임에서만 존재)\n" +
                    "- `votedAt`: 마지막 투표 변경 시각. 미응답자는 null"
    )
    public ApiResponse<GatheringDTO.AdminAttendanceListResponse> getAttendanceListForAdmin(
            @Parameter(description = "조회할 모임 ID", example = "1") @PathVariable Long gatheringId) {
        return ApiResponse.success(gatheringService.getAttendanceListForAdmin(gatheringId));
    }

    @GetMapping("/{gatheringId}/attendance/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "참석 명단 엑셀 다운로드 (관리자 전용)",
            description = "참석 명단을 xlsx 파일로 다운로드합니다.\n\n" +
                    "- 컬럼: 이름 / 기수 / 학번 / 학과 / 학년 / 응답 / 투표일시\n" +
                    "- 정렬: 참석 → 불참 → 미정 → 미응답 순\n" +
                    "- 미응답자의 투표일시는 `--` 로 표시됩니다.\n\n" 
    )
    public ResponseEntity<byte[]> exportAttendanceToExcel(
            @Parameter(description = "조회할 모임 ID", example = "1") @PathVariable Long gatheringId) {
        byte[] excelBytes = gatheringService.exportAttendanceToExcel(gatheringId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance_" + gatheringId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
