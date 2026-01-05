package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.dto.*;
import com.example.cbumanage.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 코딩 테스트 문제 관련 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "코딩 테스트 문제 컨트롤러", description = "코딩 테스트 문제 CRUD 및 목록 조회 API")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * 새로운 코딩 테스트 문제를 등록합니다.
     *
     * @param accessToken 인증된 사용자의 토큰 정보
     * @param request 문제 생성 요청 데이터
     * @return 생성된 문제 정보
     */
    @PostMapping("/problems")
    @Operation(summary = "새 코딩 테스트 문제 등록", description = "새로운 코딩 테스트 문제를 등록합니다.")
    public ResponseEntity<ProblemResponseDTO> createProblem(AccessToken accessToken, @Valid @RequestBody ProblemCreateRequestDTO request) {
        Long memberId = accessToken.getUserId();
        ProblemResponseDTO response = problemService.createProblem(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 코딩 테스트 문제 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @return 페이지네이션된 문제 목록
     */
    @GetMapping("/problems")
    @Operation(summary = "코딩 테스트 문제 목록 조회", description = "문제 목록을 조회합니다.")
    public ResponseEntity<Page<ProblemListItemDTO>> getProblems(@PageableDefault(size = 10) Pageable pageable) {
        Page<ProblemListItemDTO> problems = problemService.getProblems(pageable);
        return ResponseEntity.ok(problems);
    }

    /**
     * 특정 ID의 문제 상세 정보를 조회합니다.
     *
     * @param id 조회할 문제의 ID
     * @return 문제 상세 정보
     */
    @GetMapping("/problems/{id}")
    @Operation(summary = "문제 상세 정보 조회", description = "ID를 사용하여 특정 문제의 상세 정보를 조회합니다.")
    public ResponseEntity<ProblemResponseDTO> getProblem(@PathVariable Integer id) {
        ProblemResponseDTO problem = problemService.getProblem(id);
        return ResponseEntity.ok(problem);
    }

    /**
     * 모든 카테고리 목록을 조회합니다.
     *
     * @return 카테고리 목록
     */
    @GetMapping("/categories")
    @Operation(summary = "모든 카테고리 목록 조회", description = "문제 필터링에 사용할 전체 카테고리 목록을 조회합니다.")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = problemService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 모든 플랫폼 목록을 조회합니다.
     *
     * @return 플랫폼 목록
     */
    @GetMapping("/platforms")
    @Operation(summary = "모든 플랫폼 목록 조회", description = "문제 필터링에 사용할 전체 플랫폼 목록을 조회합니다.")
    public ResponseEntity<List<PlatformResponseDTO>> getAllPlatforms() {
        List<PlatformResponseDTO> platforms = problemService.getAllPlatforms();
        return ResponseEntity.ok(platforms);
    }

    /**
     * 모든 언어 목록을 조회합니다.
     *
     * @return 언어 목록
     */
    @GetMapping("/languages")
    @Operation(summary = "모든 언어 목록 조회", description = "문제 필터링에 사용할 전체 언어 목록을 조회합니다.")
    public ResponseEntity<List<LanguageResponseDTO>> getAllLanguages() {
        List<LanguageResponseDTO> languages = problemService.getAllLanguages();
        return ResponseEntity.ok(languages);
    }
}
