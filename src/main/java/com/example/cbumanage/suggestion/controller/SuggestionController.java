package com.example.cbumanage.suggestion.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.common.Pageables;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.suggestion.dto.SuggestionDTO;
import com.example.cbumanage.suggestion.entity.enums.SuggestionStatus;
import com.example.cbumanage.suggestion.entity.enums.SuggestionType;
import com.example.cbumanage.suggestion.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 건의 게시판(대나무숲). 전 엔드포인트 로그인 필수(SecurityConfig anyRequest().authenticated()).
 * 작성자·댓글 작성자 정보는 어떤 응답에도 없다. 운영진도 여기서는 못 본다 — 필요하면 신고 상세로.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/suggestion")
@Tag(name = "건의 게시판", description = "버그 리포트·기능 건의를 익명으로 올리고 운영진이 해결/미해결로 관리합니다.")
public class SuggestionController {

    private static final String STAFF = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', "
            + "'ROLE_MANAGER', 'ROLE_TREASURER', 'ROLE_MEMBER_MANAGER', 'ROLE_EVENT_MANAGER', "
            + "'ROLE_PROMOTION_MANAGER', 'ROLE_SECRETARY')";

    private final SuggestionService suggestionService;

    @Operation(summary = "건의 작성", description = "항상 익명으로 등록됩니다. type 은 BUG 또는 SUGGESTION.")
    @PostMapping
    public ApiResponse<SuggestionDTO.SuggestionInfoDTO> create(
            @RequestBody @Valid SuggestionDTO.SuggestionCreateRequest req,
            Authentication authentication) {
        return ApiResponse.success(suggestionService.create(req, userId(authentication)));
    }

    @Operation(summary = "건의 목록", description = "최신순. type·status 는 선택 필터. 삭제된 글 제외, content 미포함.")
    @GetMapping
    public ApiResponse<Page<SuggestionDTO.SuggestionPreviewDTO>> getList(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) SuggestionType type,
            @RequestParam(required = false) SuggestionStatus status,
            Authentication authentication) {
        Pageable pageable = Pageables.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        return ApiResponse.success(suggestionService.getList(pageable, type, status, userId(authentication)));
    }

    @Operation(summary = "건의 요약", description = "미해결·해결 건수.")
    @GetMapping("/summary")
    public ApiResponse<SuggestionDTO.SuggestionSummaryDTO> getSummary() {
        return ApiResponse.success(suggestionService.getSummary());
    }

    @Operation(summary = "건의 단건", description = "조회수가 1 오릅니다. 없거나 삭제된 글은 404.")
    @GetMapping("/{postId}")
    public ApiResponse<SuggestionDTO.SuggestionInfoDTO> get(@PathVariable Long postId, Authentication authentication) {
        try {
            return ApiResponse.success(suggestionService.get(postId, userId(authentication)));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "건의 수정", description = "작성자 본인만. 제목·내용·종류 중 넘긴 것만 바뀝니다.")
    @PatchMapping("/{postId}")
    public ApiResponse<Void> update(
            @PathVariable Long postId,
            @RequestBody @Valid SuggestionDTO.SuggestionUpdateRequest req,
            Authentication authentication) {
        try {
            suggestionService.update(postId, req, userId(authentication));
            return ApiResponse.success();
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "건의 상태 변경", description = "운영진 전부 가능. OPEN ↔ RESOLVED.")
    @PreAuthorize(STAFF)
    @PatchMapping("/{postId}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long postId,
            @RequestBody @Valid SuggestionDTO.SuggestionStatusUpdateRequest req,
            Authentication authentication) {
        try {
            suggestionService.updateStatus(postId, req.status(), userId(authentication));
            return ApiResponse.success();
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "건의 삭제", description = "작성자 또는 관리자(ADMIN·회장·부회장·MANAGER). 소프트 삭제.")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(@PathVariable Long postId, Authentication authentication) {
        try {
            suggestionService.delete(postId, userId(authentication));
            return ApiResponse.success();
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }

    @Operation(summary = "건의 댓글·답글 작성", description = "항상 익명. parentCommentId 를 주면 그 댓글의 답글(1단계만).")
    @PostMapping("/{postId}/comment")
    public ApiResponse<SuggestionDTO.SuggestionCommentCreateResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid SuggestionDTO.SuggestionCommentCreateRequest req,
            Authentication authentication) {
        try {
            return ApiResponse.success(suggestionService.createComment(postId, req, userId(authentication)));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "건의 댓글 목록", description = "작성 순, 답글 포함(parentCommentId 로 묶기). 작성자 정보 없음, isAuthor 만.")
    @GetMapping("/{postId}/comment")
    public ApiResponse<List<SuggestionDTO.SuggestionCommentDTO>> getComments(
            @PathVariable Long postId, Authentication authentication) {
        try {
            return ApiResponse.success(suggestionService.getComments(postId, userId(authentication)));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    private static Long userId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
