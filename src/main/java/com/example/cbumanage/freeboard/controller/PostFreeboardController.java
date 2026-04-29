package com.example.cbumanage.freeboard.controller;

import com.example.cbumanage.freeboard.service.PostFreeboardService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/freeboard")
@Tag(name = "자유게시판 컨트롤러")
public class PostFreeboardController {

    private final PostFreeboardService postFreeboardService;

    @Operation(
            summary = "자유게시판 게시글 생성",
            description = "자유게시판 게시글을 생성합니다. 로그인 상태에서만 작성 가능합니다.<br>" +
                    "카테고리는 서버에서 8로 자동 주입되며 클라이언트 입력값은 무시됩니다.<br>" +
                    "isAnonymous가 true이면 익명으로 등록됩니다."
    )
    @PostMapping
    public ApiResponse<PostDTO.PostFreeboardCreateResponseDTO> createFreeBoard(
            @RequestBody @Valid PostDTO.PostFreeboardCreateRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(postFreeboardService.createFreeBoard(req, userId));
    }

    @Operation(
            summary = "자유게시판 목록 조회",
            description = "자유게시판 게시글 목록을 페이징으로 조회합니다.<br>" +
                    "익명 게시글은 PostFreeboardAnonymousInfoDTO(작성자 정보 없음), " +
                    "실명 게시글은 PostFreeboardInfoDTO로 반환됩니다."
    )
    @GetMapping
    public ApiResponse<Page<Object>> getFreeBoardList(
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        return ApiResponse.success(postFreeboardService.getFreeBoardList(pageable));
    }

    @Operation(
            summary = "자유게시판 단건 조회",
            description = "postId로 자유게시판 게시글을 단건 조회합니다.<br>" +
                    "익명 여부에 따라 PostFreeboardAnonymousInfoDTO 또는 PostFreeboardInfoDTO로 반환됩니다."
    )
    @GetMapping("/{postId}")
    public ApiResponse<Object> getFreeBoard(@PathVariable Long postId) {
        try {
            return ApiResponse.success(postFreeboardService.getFreeBoard(postId));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "자유게시판 게시글 수정",
            description = "자유게시판 게시글의 제목과 내용을 수정합니다.<br>" +
                    "작성자 본인만 수정 가능합니다. 익명 여부는 수정할 수 없습니다."
    )
    @PatchMapping("/{postId}")
    public ApiResponse<Void> updateFreeBoard(
            @PathVariable Long postId,
            @RequestBody PostDTO.PostUpdateDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            postFreeboardService.updateFreeBoard(req, postId, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }
}
