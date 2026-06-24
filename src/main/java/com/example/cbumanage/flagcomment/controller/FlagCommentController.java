package com.example.cbumanage.flagcomment.controller;

import com.example.cbumanage.flagcomment.dto.CommentDTO;
import com.example.cbumanage.flagcomment.service.FlagCommentService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flag/comment")
@Tag(name = "댓글 신고", description = "신고된 댓글을 조회하고 처리합니다.")
public class FlagCommentController {

    private final FlagCommentService flagCommentService;

    @Operation(summary = "댓글 신고 목록 조회", description = "신고된 댓글 목록을 페이지 단위로 조회합니다.")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @GetMapping
    public ApiResponse<Page<CommentDTO.FlagCommentPreviewDTO>> getFlagCommentPreviews(
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return ApiResponse.success(flagCommentService.getFlagCommentPreviews(pageable));
    }

    @Operation(summary = "댓글 신고 상세 조회", description = "신고 ID로 댓글 신고 상세 정보를 조회합니다.")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @GetMapping("/{flagCommentId}")
    public ApiResponse<CommentDTO.FlagCommentInfoDTO> getFlagCommentInfo(@PathVariable Long flagCommentId) {
        try {
            return ApiResponse.success(flagCommentService.getFlagCommentInfo(flagCommentId));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "댓글 신고 처리", description = "대상 댓글의 미처리 신고를 처리 완료 상태로 변경합니다.")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT')")
    @PatchMapping("/{commentId}/resolve")
    public ApiResponse<Void> resolveFlagsByCommentId(@PathVariable Long commentId) {
        try {
            flagCommentService.resolveFlagsByCommentId(commentId);
            return ApiResponse.success();
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }
}
