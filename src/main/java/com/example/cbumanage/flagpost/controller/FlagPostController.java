package com.example.cbumanage.flagpost.controller;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.service.FlagPostService;
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
@RequestMapping("/api/v1/flag/post")
@Tag(name = "게시글 신고 컨트롤러")
public class FlagPostController {

    private final FlagPostService flagPostService;

    @Operation(summary = "게시글 신고 목록 페이징 조회", description = "신고된 게시글 목록을 페이징으로 불러옵니다. ADMIN 또는 MANAGER 권한이 필요합니다.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @GetMapping
    public ApiResponse<Page<FlagPostDTO.FlagPostPreviewDTO>> getFlagPostPreviews(
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return ApiResponse.success(flagPostService.getFlagPostPreviews(pageable));
    }

    @Operation(summary = "게시글 신고 단일 조회", description = "신고 ID로 신고 상세 정보를 조회합니다. ADMIN 또는 MANAGER 권한이 필요합니다.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @GetMapping("/{flagPostId}")
    public ApiResponse<FlagPostDTO.FlagPostInfoDTO> getFlagPostInfo(@PathVariable Long flagPostId) {
        try {
            return ApiResponse.success(flagPostService.getFlagPostInfo(flagPostId));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "게시글 신고 처리", description = "해당 postId를 가진 모든 신고를 처리(소프트 삭제)합니다. ADMIN 또는 MANAGER 권한이 필요합니다.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    @PatchMapping("/{postId}/resolve")
    public ApiResponse<Void> resolveFlagsByPostId(@PathVariable Long postId) {
        try {
            flagPostService.resolveFlagsByPostId(postId);
            return ApiResponse.success();
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.POST_NOT_FOUND);
        }
    }
}
