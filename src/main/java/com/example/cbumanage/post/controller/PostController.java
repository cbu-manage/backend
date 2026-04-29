package com.example.cbumanage.post.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.post.dto.PostDTO;
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
@Tag(name = "포스트 관리 컨트롤러")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final StudyService studyService;
    private final ProjectService projectService;
    private final PostReportService postReportService;
    private final ProblemService problemService;
    private final ResourceService resourceService;
    private final PostFreeboardService postFreeboardService;

    @Operation(summary = "카테고리 별 포스트 목록 페이징 조회", description = "포스트 목록을 페이징으로 불러옵니다.")
    @GetMapping("post")
    public ApiResponse<Page<PostDTO.PostInfoDTO>> getPosts(@Parameter(description = "페이지번호") @RequestParam int page,
                                                            @Parameter(description = "페이지당 post갯수") @RequestParam int size,
                                                            @Parameter(description = "카테고리") @RequestParam int category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return ApiResponse.success(postService.getPostsByCategory(pageable, category));
    }

    @Operation(summary = "포스트 메인테이블 단건조회")
    @GetMapping("post/{postId}/post")
    public ApiResponse<PostDTO.PostInfoDTO> getPost(@PathVariable Long postId) {
        return ApiResponse.success(postService.getPostById(postId));
    }

    @Operation(summary = "포스트 단건 삭제")
    @DeleteMapping("post/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        postService.softDeletePost(postId);
        return ApiResponse.success();
    }

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
            } else {
                throw new BaseException(ErrorCode.INVALID_REQUEST);
            }
        } catch (ResponseStatusException e) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        } catch (EntityNotFoundException e) {
            throw new BaseException(ErrorCode.NOT_FOUND);
        }
    }
}
