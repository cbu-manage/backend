package com.example.cbumanage.study.controller;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.study.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "스터디 모집 컨트롤러", description = "스터디 모집 게시글 CRUD 및 목록 조회 API")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @Operation(summary = "스터디 게시글 생성",
            description = "스터디 모집 게시글을 생성합니다. 작성자가 팀장이 되며 게시글과 함께 그룹이 자동으로 생성됩니다.")
    @PostMapping("/post/study")
    public ApiResponse<PostDTO.PostStudyCreateResponseDTO> createPostStudy(
            @RequestBody @Valid PostDTO.PostStudyCreateRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(studyService.createPostStudy(req, userId));
    }

    @Operation(summary = "스터디 게시글 전체 목록 페이징 조회",
            description = "카테고리별 스터디 전체 목록을 페이징 조회합니다. 최신순 정렬.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "category", description = "카테고리 번호 (스터디: 1)", example = "1")
    })
    @GetMapping("/post/study")
    public ApiResponse<Page<PostDTO.StudyListDTO>> getStudies(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        return ApiResponse.success(studyService.getPostsByCategory(pageable, category));
    }

    @Operation(summary = "내가 작성한 스터디 게시글 목록 조회",
            description = "로그인한 사용자가 작성한 스터디 게시글만 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "category", description = "카테고리 번호 (스터디: 1)", example = "1")
    })
    @GetMapping("/post/study/me")
    public ApiResponse<Page<PostDTO.StudyListDTO>> getMyStudies(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam int category,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("post.createdAt")));
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(studyService.getMyStudiesByUserId(pageable, userId, category));
    }

    @Operation(summary = "스터디 게시글 상세 조회",
            description = "스터디 게시글의 상세 정보를 조회합니다. 비로그인 사용자도 조회 가능합니다.")
    @GetMapping("/post/study/{postId}")
    public ApiResponse<PostDTO.StudyInfoDetailDTO> getPostStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (authentication != null) ? Long.parseLong(authentication.getName()) : null;
        return ApiResponse.success(studyService.getStudyByPostId(postId, userId));
    }

    @Operation(summary = "스터디 게시글 수정",
            description = "제목, 내용, 태그, 스터디명, 최대 인원을 수정합니다. 작성자 본인만 가능합니다.")
    @PatchMapping("/post/study/{postId}")
    public ApiResponse<Void> updateStudy(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            @RequestBody @Valid PostDTO.PostStudyUpdateRequestDTO req,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        studyService.updatePostStudy(req, postId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "스터디 게시글 삭제",
            description = "Soft Delete 처리합니다. 게시글과 함께 생성된 그룹도 함께 삭제됩니다. 작성자 본인만 가능합니다.")
    @DeleteMapping("/post/study/{postId}")
    public ApiResponse<Void> deletePost(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        studyService.softDeletePost(postId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "스터디 태그별 목록 페이징 조회",
            description = "태그명이 정확히 일치하는 스터디 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "10"),
            @Parameter(name = "tag", description = "검색할 태그 문자열 (정확히 일치하는 태그명)", example = "Spring")
    })
    @GetMapping("/post/study/filter")
    public ApiResponse<Page<PostDTO.StudyListDTO>> filterStudiesByTag(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(name = "tag") String tag) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
        return ApiResponse.success(studyService.searchByTag(tag, pageable));
    }

    @Operation(summary = "스터디 모집 마감",
            description = "모집을 마감합니다. PENDING 신청자는 일괄 거절되고 그룹 모집이 종료됩니다. " +
                    "팀장만 가능하며, 최소 1명 이상 수락자(ACTIVE)가 필요합니다.")
    @PostMapping("/post/study/{postId}/close")
    public ApiResponse<Void> closeStudyRecruitment(
            @Parameter(description = "게시글 ID", example = "100") @PathVariable Long postId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        studyService.closeStudyRecruitment(postId, userId);
        return ApiResponse.success();
    }
}
