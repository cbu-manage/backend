package com.example.cbumanage.comment.controller;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.comment.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "댓글 관리 컨트롤러")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "코멘트 작성 요청")
    @PostMapping("post/{postId}/comment")
    public ApiResponse<CommentDTO.CommentCreateResponseDTO> createComment(@RequestBody CommentDTO.CommentCreateRequestDTO req,
                                                                          @PathVariable Long postId,
                                                                          Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(commentService.createComment(req, userId, postId));
    }

    @Operation(summary = "댓글 목록 반환")
    @GetMapping("post/{postId}/comment")
    public ApiResponse<List<CommentDTO.CommentInfoDTO>> getComments(@PathVariable Long postId) {
        return ApiResponse.success(commentService.getComments(postId));
    }

    @Operation(
            summary = "익명 댓글 목록 반환",
            description = "익명 게시글(isAnonymous=true)의 댓글 목록을 작성자 정보 없이 반환합니다.<br>" +
                    "익명 게시글이 아닌 postId로 요청 시 400을 반환합니다."
    )
    @GetMapping("post/{postId}/comment/anonymous")
    public ApiResponse<List<CommentDTO.CommentAnonymousInfoDTO>> getAnonymousComments(@PathVariable Long postId) {
        try {
            return ApiResponse.success(commentService.getAnonymousComments(postId));
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "답글 추가")
    @PostMapping("comment/{commentId}/reply")
    public ApiResponse<CommentDTO.ReplyCreateResponseDTO> createReply(@RequestBody CommentDTO.ReplyCreateRequestDTO req,
                                                                      @Parameter(description = "답글을 추가할 댓글의 ID") @PathVariable Long commentId,
                                                                      Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(commentService.createReply(req, userId, commentId));
    }

    @Operation(summary = "댓글 변경")
    @PatchMapping("comment/{commentId}")
    public ApiResponse<Void> updateComment(@RequestBody CommentDTO.CommentUpdateRequestDTO req,
                                           @PathVariable Long commentId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            commentService.updateComment(commentId, req, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }

    @Operation(summary = "댓글 삭제(softDelete)")
    @DeleteMapping("comment/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            commentService.deleteComment(commentId, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
    }
}
