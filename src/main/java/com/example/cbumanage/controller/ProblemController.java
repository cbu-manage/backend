package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.dto.*;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.List;

/**
 * 코딩 테스트 관련 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/post")
@Tag(name = "코딩 테스트 컨트롤러", description = "코딩 테스트 게시판 CRUD API")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    /**
     * 새로운 코딩 테스트 문제를 등록합니다.
     *
     * @param request 문제 생성 요청 데이터
     * @return 생성된 문제 정보
     */
    @PostMapping("/problems")
    @Operation(
            summary = "새 코딩 테스트 문제 Create",
            description = "새로운 코딩 테스트 문제를 등록합니다. 카테고리는 복수 선택이 가능하고, 플랫폼과 언어는 단일 선택입니다. ",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 등록 예시",
                                    value = """
                                            { "categoryIds": [1, 2],
                                              "platformId": 1,
                                              "languageId": 1,
                                              "title": "두 수의 합",
                                              "content": "두 정수 A, B를 입력받아 합을 출력하는 문제입니다.",
                                              "grade": "SILVER",
                                              "problemUrl": "https://www.acmicpc.~~",
                                              "problemStatus": "UNSOLVED" }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "문제 생성 요청 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "404", description = "카테고리 또는 플랫폼을 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<ProblemResponseDTO>> createProblem(AccessToken accessToken,
                                                                            @Valid @RequestBody ProblemCreateRequestDTO request) {
        ProblemResponseDTO response = problemService.createProblem(request, accessToken.getUserId());
        return ResultResponse.ok(SuccessCode.CREATED, response);
    }

    /**
     * 특정 ID의 문제를 수정하는 메소드.
     *
     * @param id      수정할 문제의 ID
     * @param request 수정할 문제 내용
     * @return 수정된 문제 정보
     */
    @PatchMapping("/problems/{id}")
    @Operation(summary = "문제 정보 수정",
            description = "ID에 해당하는 문제의 정보를 수정합니다. 수정할 필드만 사용하여 수정할 수 있습니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "일부 수정 예시",
                                    value = """
                                            { "problemStatus": "SOLVED" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자만 수정 가능)"),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<ProblemResponseDTO>> updateProblem(@PathVariable Long id,
                                                                            AccessToken accessToken,
                                                                            @Valid @RequestBody ProblemUpdateRequestDTO request) {
        ProblemResponseDTO response = problemService.updateProblem(id, accessToken.getUserId(), request);
        return ResultResponse.ok(SuccessCode.UPDATED, response);
    }

    /**
     * 특정 ID의 문제를 삭제하는 메소드.
     *
     * @param id 삭제할 문제의 ID
     */
    @DeleteMapping("/problems/{id}")
    @Operation(summary = "문제 삭제", description = "ID에 해당하는 문제를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자만 삭제 가능)"),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })

    public ResponseEntity<ResultResponse<Void>> deleteProblem(@PathVariable Long id,
                                                              AccessToken accessToken) {
        problemService.deleteProblem(id, accessToken.getUserId());
        return ResultResponse.ok(SuccessCode.DELETED);
    }

    /**
     * 코딩 테스트 문제 목록을 조회하는 메소드.
     *
     * @param pageable 페이지네이션 정보 (예: ?page=0&size=10&sort=createdAt,desc)
     * @param categoryId 필터링할 카테고리 ID 목록
     * @param platformId 필터링할 플랫폼 ID 목록
     * @return 페이지네이션된 문제 목록
     */
    @GetMapping("/problems")
    @Operation(summary = "코딩 테스트 문제 목록 조회",
            description = "문제 목록을 페이지네이션하여(deault걊은 10) 조회합니다. 최신 등록 순이며, 카테고리, 플랫폼으로 필터링 가능합니다",
            parameters = {
                    @Parameter(name = "page", description = "페이지 번호(0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지당 보여질 문제 수", example = "10"),
                    @Parameter(name = "sort", description = "정렬 기준", example = "기본값은 createdAt 기준 오름차순"),
                    @Parameter(name = "categoryId", description = "필터링할 카테고리 ID (복수 선택 가능, GET /categories에서 조회)", example = "1"),
                    @Parameter(name = "platformId", description = "필터링할 플랫폼 ID (복수 선택 가능, GET /platforms에서 조회)", example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 조회 성공"),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<Page<ProblemListItemDTO>>> getProblems(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "필터링할 카테고리 ID 목록 (GET /categories에서 조회 가능)", example = "1")
            @RequestParam(required = false) List<Integer> categoryId,
            @Parameter(description = "필터링할 플랫폼 ID 목록 (GET /platforms에서 조회 가능)", example = "1")
            @RequestParam(required = false) List<Integer> platformId) {
        Page<ProblemListItemDTO> problems = problemService.getProblems(pageable, categoryId, platformId);
        return ResultResponse.ok(SuccessCode.SUCCESS, problems);
    }

    /**
     * 특정 ID의 문제 상세 정보를 조회하는 메소드.
     *
     * @param id 조회할 문제의 ID
     * @return 문제 상세 정보
     */
    @GetMapping("/problems/{id}")
    @Operation(summary = "문제 상세 정보 조회", description = "ID를 사용하여 특정 문제의 상세 정보를 조회합니다. 조회할때마다 조회수가 증가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문제 조회 성공"),
            @ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<ProblemResponseDTO>> getProblem(@PathVariable Long id) {
        ProblemResponseDTO problem = problemService.getProblem(id);
        return ResultResponse.ok(SuccessCode.SUCCESS, problem);
    }

    /**
     * 모든 카테고리 목록을 조회하는 메소드.
     *
     * @return 카테고리 목록
     */
    @GetMapping("/categories")
    @Operation(summary = "모든 카테고리 목록 조회", description = "문제 필터링에 사용할 전체 카테고리 목록을 조회합니다. 문제 등록 시 categoryIds에 사용할 ID를 확인할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    })
    public ResponseEntity<ResultResponse<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> categories = problemService.getAllCategories();
        return ResultResponse.ok(SuccessCode.SUCCESS, categories);
    }

    /**
     * 모든 플랫폼 목록을 조회하는 메소드.
     *
     * @return 플랫폼 목록
     */
    @GetMapping("/platforms")
    @Operation(summary = "모든 플랫폼 목록 조회", description = "문제 필터링에 사용할 전체 플랫폼 목록을 조회합니다.")
    public ResponseEntity<ResultResponse<List<PlatformResponseDTO>>> getAllPlatforms() {
        List<PlatformResponseDTO> platforms = problemService.getAllPlatforms();
        return ResultResponse.ok(SuccessCode.SUCCESS, platforms);
    }

    /**
     * 모든 언어 목록을 조회하는 메소드.
     *
     * @return 언어 목록
     */
    @GetMapping("/languages")
    @Operation(summary = "모든 언어 목록 조회", description = "문제 필터링에 사용할 전체 언어 목록을 조회합니다.")
    public ResponseEntity<ResultResponse<List<LanguageResponseDTO>>> getAllLanguages() {
        List<LanguageResponseDTO> languages = problemService.getAllLanguages();
        return ResultResponse.ok(SuccessCode.SUCCESS, languages);
    }
}


