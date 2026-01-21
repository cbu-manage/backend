package com.example.cbumanage.controller;

import com.example.cbumanage.dto.*;
import com.example.cbumanage.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/post")
@Tag(name = "코딩 테스트 문제 컨트롤러", description = "코딩 테스트 문제 CRUD 및 목록 조회 API")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * 새로운 코딩 테스트 문제를 등록합니다.
     *
     * @param userId  문제를 등록하는 회원의 ID
     * @param request 문제 생성 요청 데이터
     * @return 생성된 문제 정보
     */
    @PostMapping("/problems")
    @Operation(summary = "새 코딩 테스트 문제 등록", description = "새로운 코딩 테스트 문제를 등록합니다.")
    public ResponseEntity<ProblemResponseDTO> createProblem(@RequestParam Long userId, @Valid @RequestBody ProblemCreateRequestDTO request) {
        ProblemResponseDTO response = problemService.createProblem(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 ID의 문제를 수정하는 메소드.
     *
     * @param id      수정할 문제의 ID
     * @param userId  수정 요청을 한 회원의 ID
     * @param request 수정할 문제 내용
     * @return 수정된 문제 정보
     */
    @PatchMapping("/problems/{id}")
    @Operation(summary = "문제 정보 수정", description = "ID에 해당하는 문제의 정보를 수정합니다.")
    public ResponseEntity<ProblemResponseDTO> updateProblem(@PathVariable Integer id,
                                                            @RequestParam Long userId,
                                                            @RequestBody ProblemUpdateRequestDTO request) {
        ProblemResponseDTO response = problemService.updateProblem(id, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 ID의 문제를 삭제하는 메소드.
     *
     * @param id     삭제할 문제의 ID
     * @param userId 삭제 요청을 한 회원의 ID
     */
    @DeleteMapping("/problems/{id}")
    @Operation(summary = "문제 삭제", description = "ID에 해당하는 문제를 삭제합니다.")
    public ResponseEntity<Void> deleteProblem(@PathVariable Integer id,
                                              @RequestParam Long userId) {
        problemService.deleteProblem(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 코딩 테스트 문제 목록을 조회하는 메소드.
     *
     * @param pageable 페이지네이션 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @return 페이지네이션된 문제 목록
     */
    @GetMapping("/problems")
    @Operation(summary = "코딩 테스트 문제 목록 조회", description = "문제 목록을 조회합니다.")
    public ResponseEntity<Page<ProblemListItemDTO>> getProblems(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) List<Integer> categoryId,
            @RequestParam(required = false) List<Integer> platformId) {
        Page<ProblemListItemDTO> problems = problemService.getProblems(pageable, categoryId, platformId);
        return ResponseEntity.ok(problems);
    }

    /**
     * 특정 ID의 문제 상세 정보를 조회하는 메소드.
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
     * 모든 카테고리 목록을 조회하는 메소드.
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
     * 모든 플랫폼 목록을 조회하는 메소드.
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
     * 모든 언어 목록을 조회하는 메소드.
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


