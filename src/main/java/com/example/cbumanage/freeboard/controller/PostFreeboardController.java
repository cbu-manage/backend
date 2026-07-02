package com.example.cbumanage.freeboard.controller;

import com.example.cbumanage.comment.dto.CommentDTO;
import com.example.cbumanage.comment.service.CommentService;
import com.example.cbumanage.freeboard.service.PostFreeboardService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "자유게시판", description = "자유게시판 게시글을 작성·조회·수정합니다.")
public class PostFreeboardController {

    private final PostFreeboardService postFreeboardService;
    private final CommentService commentService;

    @Operation(
            summary = "자유게시판 게시글 생성",
            description = """
                    자유게시판 게시글을 생성합니다. 로그인 상태에서만 작성 가능합니다.

                    **카테고리**: 서버에서 8로 자동 주입되며 클라이언트 입력값은 무시됩니다.

                    **익명 여부**: isAnonymous가 true이면 익명 게시글로 등록됩니다.
                    익명 게시글은 목록/단건 조회 시 작성자 정보(authorId, authorName, authorGeneration)가 반환되지 않습니다.
                    isAnonymous는 생성 이후 수정할 수 없습니다.
                    """
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
            description = """
                    자유게시판 게시글 목록을 최신순으로 페이징 조회합니다. 인증 없이 누구나 조회 가능합니다.
                    삭제된 게시글(isDeleted=true)은 제외되며, content는 포함되지 않습니다.

                    **응답 스키마**: isAnonymous 값에 따라 두 가지 DTO 중 하나로 반환됩니다.
                    - isAnonymous=false → PostFreeboardPreviewDTO (작성자 정보 포함, content 없음)
                    - isAnonymous=true → PostFreeboardAnonymousPreviewDTO (작성자 정보 없음, content 없음)
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(
            schema = @Schema(oneOf = {
                    PostDTO.PostFreeboardPreviewDTO.class,
                    PostDTO.PostFreeboardAnonymousPreviewDTO.class
            }),
            examples = {
                    @ExampleObject(name = "실명 게시글 (isAnonymous=false)", value = """
                            {
                              "postId": 1,
                              "title": "안녕하세요",
                              "createdAt": "2025-01-01T12:00:00",
                              "authorId": 42,
                              "authorName": "김건우",
                              "authorGeneration": 10,
                              "viewCount": 15,
                              "commentCount": 3,
                              "isAnonymous": false
                            }"""),
                    @ExampleObject(name = "익명 게시글 (isAnonymous=true)", value = """
                            {
                              "postId": 2,
                              "title": "익명으로 작성한 글",
                              "createdAt": "2025-01-01T12:00:00",
                              "viewCount": 5,
                              "commentCount": 1,
                              "isAnonymous": true
                            }""")
            }
    ))
    @GetMapping
    public ApiResponse<Page<PostDTO.PostFreeboardPreviewResponse>> getFreeBoardList(
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        return ApiResponse.success(postFreeboardService.getFreeBoardList(pageable));
    }

    @Operation(
            summary = "자유게시판 단건 조회",
            description = """
                    postId로 자유게시판 게시글을 단건 조회합니다. 인증 없이 누구나 조회 가능합니다.

                    **응답 스키마**: isAnonymous 값에 따라 두 가지 DTO 중 하나로 반환됩니다.
                    - isAnonymous=false → PostFreeboardInfoDTO (authorId, authorName, authorGeneration 포함)
                    - isAnonymous=true → PostFreeboardAnonymousInfoDTO (작성자 정보 필드 없음)

                    존재하지 않는 postId 요청 시 404를 반환합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = @Content(
            schema = @Schema(oneOf = {
                    PostDTO.PostFreeboardInfoDTO.class,
                    PostDTO.PostFreeboardAnonymousInfoDTO.class
            }),
            examples = {
                    @ExampleObject(name = "실명 게시글 (isAnonymous=false)", value = """
                            {
                              "postId": 1,
                              "title": "안녕하세요",
                              "content": "게시글 내용입니다.",
                              "createdAt": "2025-01-01T12:00:00",
                              "authorId": 42,
                              "authorName": "김건우",
                              "authorGeneration": 10,
                              "viewCount": 15,
                              "commentCount": 3,
                              "isAnonymous": false
                            }"""),
                    @ExampleObject(name = "익명 게시글 (isAnonymous=true)", value = """
                            {
                              "postId": 2,
                              "title": "익명으로 작성한 글",
                              "content": "익명 게시글 내용입니다.",
                              "createdAt": "2025-01-01T12:00:00",
                              "viewCount": 5,
                              "commentCount": 1,
                              "isAnonymous": true
                            }""")
            }
    ))
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.PostFreeboardResponse> getFreeBoard(@PathVariable Long postId) {
        try {
            return ApiResponse.success(postFreeboardService.getFreeBoard(postId));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "자유게시판 댓글 작성",
            description = """
                    자유게시판 게시글에 댓글을 작성합니다. 로그인 상태에서만 작성 가능합니다.

                    **익명 처리**: isAnonymous 파라미터로 익명 여부를 선택할 수 있습니다. (default: false)
                    단, 게시글 자체가 익명(isAnonymous=true)이면 파라미터 값에 관계없이 댓글도 무조건 익명으로 생성됩니다.
                    """
    )
    @PostMapping("/{postId}/comment")
    public ApiResponse<CommentDTO.CommentCreateResponseDTO> createFreeBoardComment(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "false") boolean isAnonymous,
            @RequestBody CommentDTO.CommentCreateRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            return ApiResponse.success(commentService.createFreeBoardComment(req, userId, postId, isAnonymous));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "자유게시판 댓글 목록 조회",
            description = """
                    자유게시판 게시글의 댓글 목록을 조회합니다. 인증 없이 누구나 조회 가능합니다.

                    **응답 스키마**: 댓글별 isAnonymous 값에 따라 두 가지 DTO 중 하나로 반환됩니다.
                    - isAnonymous=false → FreeBoardCommentInfoDTO (작성자 정보 포함)
                    - isAnonymous=true → FreeBoardCommentAnonymousInfoDTO (작성자 정보 없음)
                    """
    )
    @GetMapping("/{postId}/comment")
    public ApiResponse<java.util.List<CommentDTO.FreeBoardCommentResponse>> getFreeBoardComments(
            @PathVariable Long postId) {
        try {
            return ApiResponse.success(commentService.getFreeBoardComments(postId));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(
            summary = "자유게시판 게시글 수정",
            description = """
                    자유게시판 게시글의 제목과 내용을 수정합니다.

                    **권한**: 작성자 본인만 수정 가능합니다. 다른 사용자가 수정 시도 시 403을 반환합니다.

                    **제약**: isAnonymous(익명 여부)는 수정할 수 없습니다. title과 content만 변경 가능합니다.
                    """
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
