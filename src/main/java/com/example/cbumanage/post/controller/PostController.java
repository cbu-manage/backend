package com.example.cbumanage.post.controller;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.service.FlagPostService;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.news.service.NewsService;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.study.service.StudyService;
import com.example.cbumanage.project.service.ProjectService;
import com.example.cbumanage.freeboard.service.PostFreeboardService;
import com.example.cbumanage.report.service.PostReportService;
import com.example.cbumanage.problem.service.ProblemService;
import com.example.cbumanage.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "공통 게시글", description = "게시판 공통 게시글 조회·삭제·신고 API입니다.")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final StudyService studyService;
    private final ProjectService projectService;
    private final PostReportService postReportService;
    private final ProblemService problemService;
    private final ResourceService resourceService;
    private final PostFreeboardService postFreeboardService;
    private final NewsService newsService;
    private final FlagPostService flagPostService;

    @Operation(summary = "게시글 목록 조회", description = "카테고리별 게시글 목록을 페이지 단위로 조회합니다.")
    @GetMapping("post")
    public ApiResponse<Page<PostDTO.PostInfoDTO>> getPosts(@Parameter(description = "페이지 번호(0부터 시작)") @RequestParam int page,
                                                            @Parameter(description = "페이지당 조회 개수") @RequestParam int size,
                                                            @Parameter(description = "게시글 카테고리 번호") @RequestParam int category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return ApiResponse.success(postService.getPostsByCategory(pageable, category));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 공통 게시글 정보를 조회합니다.")
    @GetMapping("post/{postId}/post")
    public ApiResponse<PostDTO.PostInfoDTO> getPost(@PathVariable Long postId) {
        return ApiResponse.success(postService.getPostById(postId));
    }

    @Operation(summary = "게시글 삭제", description = "작성자 또는 관리자가 게시글을 소프트 삭제합니다.")
    @DeleteMapping("post/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            postService.softDeletePost(postId, userId);
            return ApiResponse.success();
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "내 게시글 목록 조회", description = "로그인 사용자가 작성한 게시글을 카테고리별로 조회합니다.")
    @GetMapping("post/my")
    public ApiResponse<Object> getMyPosts(@RequestParam int page, @RequestParam int size,
                                           @RequestParam(required = false) Integer category,
                                           Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));

        try {
            if (category == null) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
                return ApiResponse.success(postService.getMyPosts(pageable, userId));
            } else if (category == 1) {
                return ApiResponse.success(studyService.getMyStudiesByUserId(pageable, userId, category));
            } else if (category == 2) {
                return ApiResponse.success(projectService.getMyProjectsByUserId(pageable, userId, category));
            } else if (category == 5) {
                return ApiResponse.success(problemService.getMyProblems(userId, pageable));
            } else if (category == 6) {
                return ApiResponse.success(resourceService.getMyResources(userId, pageable));
            } else if (category == 7) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
                return ApiResponse.success(postReportService.getMyPostReportPreviewDTOList(pageable, userId));
            } else if (category == 8) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
                return ApiResponse.success(postFreeboardService.getMyFreeboards(pageable, userId));
            } else if (category == PostCategory.NEWS.getValue()) {
                pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
                return ApiResponse.success(newsService.getMyNews(pageable, userId));
            } else {
                throw new BaseException(ErrorCode.INVALID_REQUEST);
            }
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }

    @Operation(summary = "게시글 신고 생성", description = "특정 게시글을 신고합니다.")
    @PostMapping("post/{postId}/flag")
    public ApiResponse<FlagPostDTO.FlagPostCreateResponse> createFlagPost(
            @PathVariable Long postId,
            @RequestBody FlagPostDTO.FlagPostCreateRequest req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        try {
            return ApiResponse.success(flagPostService.createFlagPost(postId, req, userId));
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.POST_NOT_FOUND);
        }
    }

}
