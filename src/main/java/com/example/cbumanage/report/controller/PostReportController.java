package com.example.cbumanage.report.controller;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.report.service.PostReportHWPService;
import com.example.cbumanage.report.service.PostReportService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report")
@Tag(name = "보고서", description = "활동 보고서 게시글을 작성·조회·수정·승인하고 파일로 내보냅니다.")
public class PostReportController {
    private final PostReportService postReportService;
    private final PostReportHWPService postReportHWPService;

    @Operation(
            summary = "보고서 작성",
            description = "게시글과 보고서 정보를 한 번의 요청으로 생성합니다. 카테고리는 서버에서 보고서 게시판 값으로 처리됩니다."
    )
    @PostMapping()
    public ApiResponse<PostDTO.PostReportCreateResponseDTO> createPostReport(
            @Parameter(description = "보고서 작성 요청입니다. reportImage에는 파일 업로드 API가 반환한 이미지 URL을 전달합니다.")
            @RequestBody @Valid PostDTO.PostReportCreateRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        PostDTO.PostReportCreateResponseDTO responseDTO = postReportService.createPostReport(req, userId);
        return ApiResponse.success(responseDTO);
    }

    @Operation(
            summary = "보고서 목록 조회",
            description = "보고서 게시글 목록을 페이지 단위로 조회합니다. 응답에는 게시글, 작성자, 그룹 요약 정보가 포함됩니다."
    )
    @GetMapping
    public ApiResponse<Page<PostDTO.PostReportPreviewDTO>> getPostReportPreviews(
            @RequestParam int page,
            @RequestParam int size,
            @Parameter(description = "활동 시작일 (포함, yyyy-MM-dd)", example = "2025-01-01") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "활동 종료일 (포함, yyyy-MM-dd)", example = "2025-12-31") @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end   = endDate   != null ? endDate.atTime(LocalTime.MAX) : null;
        Page<PostDTO.PostReportPreviewDTO> postReportPreviewDTOs = postReportService.getPostReportPreviewDTOList(pageable, userId, start, end);
        return ApiResponse.success(postReportPreviewDTOs);
    }

    @Operation(
            summary = "그룹별 보고서 목록 조회",
            description = "그룹 ID 기준으로 해당 그룹의 보고서 게시글 목록을 페이지 단위로 조회합니다."
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
            summary = "보고서 상세 조회",
            description = "게시글 ID 기준으로 보고서 상세 정보를 조회합니다. 응답에는 게시글, 보고서, 그룹 정보가 함께 포함됩니다."
    )
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.PostReportViewDTO> getPostReportView(@PathVariable Long postId, Authentication authentication){
        Long userId = Long.parseLong(authentication.getName());
        try{
            PostDTO.PostReportViewDTO postReportView = postReportService.getPostReportViewDTO(postId, userId);
            return ApiResponse.success(postReportView);
        }catch (ResponseStatusException e){
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "보고서 수정", description = "작성자 본인의 보고서 게시글 내용을 수정합니다.")
    @PatchMapping("/{postId}")
    public ApiResponse<Void> updatePostReport(@PathVariable Long postId,
                                                                 @RequestBody @Valid PostDTO.PostReportUpdateRequestDTO req,
                                                                 Authentication authentication){
        Long userId = Long.parseLong(authentication.getName());
        try {
            postReportService.updatePostReport(req,postId,userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e){
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }

    @Operation(summary = "보고서 승인", description = "보고서 ID를 기준으로 승인 상태를 변경합니다.")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @PatchMapping("/{postId}/accept")
    public ApiResponse<Void> acceptPostReport(@PathVariable Long postId, Authentication authentication){
        Long userId = Long.parseLong(authentication.getName());
        try {
            postReportService.acceptReport(postId,userId);
            return ApiResponse.success();
        }
        catch (ResponseStatusException e){
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        catch (EntityNotFoundException e){
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "보고서 HWP 다운로드",
            description = "보고서 게시글의 내용을 바탕으로 HWP 파일을 생성하여 즉시 다운로드합니다.<br>" +
                    "클라이언트에서는 responseType: 'blob' 으로 받아 Blob 처리 후 다운로드해야 합니다."
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
            summary = "그룹 보고서 ZIP 다운로드",
            description = "특정 그룹의 보고서를 모두 HWP 파일로 생성하여 ZIP으로 묶어 다운로드합니다.<br>" +
                    "클라이언트에서는 responseType: 'blob' 으로 받아 Blob 처리 후 다운로드해야 합니다."
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
}
