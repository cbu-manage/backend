package com.example.cbumanage.problem.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.problem.dto.*;
import com.example.cbumanage.problem.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@Tag(name = "코딩 테스트 컨트롤러", description = "코딩 테스트 게시판 CRUD API")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/problems")
    @Operation(summary = "새 코딩 테스트 문제 Create", requestBody = @RequestBody(required = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "문제 등록 예시", value = """
                    { "categoryIds": [1, 2], "platformId": 1, "languageId": 1, "title": "두 수의 합",
                      "content": "두 정수 A, B를 입력받아 합을 출력하는 문제입니다.", "grade": "SILVER",
                      "problemUrl": "https://www.acmicpc.~~", "problemStatus": "UNSOLVED" }
                    """))))
    public ApiResponse<ProblemResponseDTO> createProblem(Authentication authentication,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProblemCreateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(problemService.createProblem(request, userId));
    }

    @PatchMapping("/problems/{postId}")
    @Operation(summary = "문제 정보 수정")
    public ApiResponse<ProblemResponseDTO> updateProblem(@PathVariable Long postId, Authentication authentication,
            @Valid @RequestBody ProblemUpdateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(problemService.updateProblem(postId, userId, request));
    }

    @DeleteMapping("/problems/{postId}")
    @Operation(summary = "문제 삭제")
    public ApiResponse<Void> deleteProblem(@PathVariable Long postId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        problemService.deleteProblem(postId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/problems")
    @Operation(summary = "코딩 테스트 문제 목록 조회")
    public ApiResponse<Page<ProblemListItemDTO>> getProblems(
            @ParameterObject @PageableDefault(size = 10, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) List<Integer> categoryId,
            @RequestParam(required = false) List<Integer> platformId) {
        return ApiResponse.success(problemService.getProblems(pageable, categoryId, platformId));
    }

    @GetMapping("/problems/my")
    @Operation(summary = "내 코딩 테스트 문제 목록 조회")
    public ApiResponse<Page<ProblemListItemDTO>> getMyProblems(Authentication authentication,
            @ParameterObject @PageableDefault(size = 10, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(problemService.getMyProblems(userId, pageable));
    }

    @GetMapping("/problems/{postId}")
    @Operation(summary = "문제 상세 정보 조회")
    public ApiResponse<ProblemResponseDTO> getProblem(@PathVariable Long postId) {
        return ApiResponse.success(problemService.getProblem(postId));
    }

    @GetMapping("/categories")
    @Operation(summary = "모든 카테고리 목록 조회")
    public ApiResponse<List<CategoryResponseDTO>> getAllCategories() {
        return ApiResponse.success(problemService.getAllCategories());
    }

    @GetMapping("/platforms")
    @Operation(summary = "모든 플랫폼 목록 조회")
    public ApiResponse<List<PlatformResponseDTO>> getAllPlatforms() {
        return ApiResponse.success(problemService.getAllPlatforms());
    }

    @GetMapping("/languages")
    @Operation(summary = "모든 언어 목록 조회")
    public ApiResponse<List<LanguageResponseDTO>> getAllLanguages() {
        return ApiResponse.success(problemService.getAllLanguages());
    }
}
