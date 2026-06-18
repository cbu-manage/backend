package com.example.cbumanage.api.v2;

import com.example.cbumanage.api.v2.dto.PostReportV2DTO;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.report.service.PostReportHWPService;
import com.example.cbumanage.report.service.PostReportService;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/report")
@Tag(name = "V2 보고서 게시글 관리 컨트롤러", description = "프론트 v1 계약과 분리된 UUID 기반 보고서 API")
public class PostReportV2Controller {

    private final PostReportService postReportService;
    private final PostReportHWPService postReportHWPService;
    private final UserRepository userRepository;

    @Operation(
            summary = "V2 보고서 게시글 생성",
            description = "memberIds 대신 memberUuids로 참여자를 전달합니다. 응답의 사용자 식별자는 userUuid/authorUuid/memberUuid로 반환됩니다."
    )
    @PostMapping
    public ApiResponse<PostDTO.PostReportCreateResponseDTO> createPostReport(
            @Parameter(description = "보고서 생성 요청. 참여자는 memberUuids로 전달합니다.")
            @RequestBody @Valid PostReportV2DTO.CreateRequest req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PostDTO.PostReportCreateResponseDTO responseDTO =
                postReportService.createPostReport(PostReportV2DTO.toV1(req, toUserIds(req.memberUuids())), userId);
        return ApiResponse.success(responseDTO);
    }

    @Operation(
            summary = "V2 보고서 게시글 미리보기 페이징 조회",
            description = "v1과 동일한 필터 정책을 쓰되 응답 사용자 식별자는 UUID 필드명으로 변환됩니다."
    )
    @GetMapping
    public ApiResponse<Page<PostDTO.PostReportPreviewDTO>> getPostReportPreviews(
            @RequestParam int page,
            @RequestParam int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PostDTO.PostReportPreviewDTO> postReportPreviewDTOs = postReportService.getPostReportPreviewDTOList(pageable, userId);
        return ApiResponse.success(postReportPreviewDTOs);
    }

    @Operation(
            summary = "V2 그룹별 보고서 게시글 미리보기 페이징 조회",
            description = "groupId 기준으로 보고서 목록을 조회합니다. 응답 사용자 식별자는 UUID 필드명으로 변환됩니다."
    )
    @GetMapping("/group/{groupId}")
    public ApiResponse<Page<PostDTO.PostReportPreviewDTO>> getPostReportPreviewsByGroup(
            @PathVariable Long groupId,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<PostDTO.PostReportPreviewDTO> postReportPreviewDTOs = postReportService.getGroupPostReportPreviewDTOList(pageable, groupId);
        return ApiResponse.success(postReportPreviewDTOs);
    }

    @Operation(
            summary = "V2 보고서 게시글 단건 조회",
            description = "게시글/보고서/그룹 상세를 조회합니다. 응답 사용자 식별자는 UUID 필드명으로 변환됩니다."
    )
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.PostReportViewDTO> getPostReportView(@PathVariable Long postId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            PostDTO.PostReportViewDTO postReportView = postReportService.getPostReportViewDTO(postId, userId);
            return ApiResponse.success(postReportView);
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "V2 보고서 게시글 수정",
            description = "memberIds 대신 memberUuids로 참여자를 전달합니다."
    )
    @PatchMapping("/{postId}")
    public ApiResponse<Void> updatePostReport(@PathVariable Long postId,
                                              @RequestBody @Valid PostReportV2DTO.UpdateRequest req,
                                              Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            postReportService.updatePostReport(PostReportV2DTO.toV1(req, toUserIds(req.memberUuids())), postId, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }

    @Operation(summary = "V2 보고서 승인", description = "관리자 권한으로 보고서 승인 상태를 변경합니다.")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @PatchMapping("/{postId}/accept")
    public ApiResponse<Void> acceptPostReport(@PathVariable Long postId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            postReportService.acceptReport(postId, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "V2 보고서 한글 파일 추출",
            description = "보고서 게시글의 내용을 바탕으로 HWP 파일을 생성하여 즉시 다운로드합니다."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @GetMapping("/{postId}/export")
    public ResponseEntity<byte[]> exportPostReportToHWP(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            PostReportHWPService.HWPExportResult result = postReportHWPService.exportToHWP(postId, userId);
            byte[] hwpBytes = result.hwpBytes();
            String fileName = result.title() + ".hwp";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );
            headers.setContentType(MediaType.parseMediaType("application/haansofthwp"));
            headers.setContentLength(hwpBytes.length);

            return ResponseEntity.ok().headers(headers).body(hwpBytes);
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Operation(
            summary = "V2 그룹 보고서 전체 ZIP 추출",
            description = "특정 그룹의 보고서를 모두 HWP 파일로 생성하여 ZIP으로 묶어 다운로드합니다."
    )
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @GetMapping("/export/group/{groupId}")
    public ResponseEntity<?> exportGroupReportsToZip(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            PostReportHWPService.ZipExportResult result = postReportHWPService.exportGroupToZip(groupId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(result.fileName(), StandardCharsets.UTF_8)
                            .build()
            );
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentLength(result.zipBytes().length);

            return ResponseEntity.ok().headers(headers).body(result.zipBytes());
        } catch (PostReportHWPService.ZipPartialFailureException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("failedReports", e.getFailedReports()));
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        } catch (Exception e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
    }

    private List<Long> toUserIds(List<UUID> memberUuids) {
        if (memberUuids == null) {
            return List.of();
        }
        return memberUuids.stream()
                .map(memberUuid -> userRepository.findByUserUuidAndDeletedAtIsNull(memberUuid)
                        .map(User::getUserId)
                        .orElseThrow(() -> new EntityNotFoundException("Member Not Found: " + memberUuid)))
                .toList();
    }
}
