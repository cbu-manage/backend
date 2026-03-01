package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.dto.ResourceCreateRequestDTO;
import com.example.cbumanage.dto.ResourceListItemDTO;
import com.example.cbumanage.response.ResultResponse;
import com.example.cbumanage.response.SuccessCode;
import com.example.cbumanage.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 자료방 관련 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "자료방 컨트롤러", description = "자료방 게시글 등록/조회/삭제 API")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * 자료방 게시글을 등록합니다.
     *
     * @param request 게시글 생성 요청 데이터 (제목, 링크)
     * @return 생성된 게시글 정보
     */
    @PostMapping
    @Operation(
            summary = "자료 등록",
            description = "자료방에 새 게시글을 등록합니다. 제목과 링크는 필수입니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "자료 등록 예시",
                                    value = """ 
                                            { "title" : "2024 카카오 코딩테스트 문제 모음", "link" : "https://programmers.co.kr/learn/challenges" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "자료 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<ResourceListItemDTO>> createResource(
            AccessToken accessToken,
            @Valid @org.springframework.web.bind.annotation.RequestBody ResourceCreateRequestDTO request) {
        ResourceListItemDTO response = resourceService.createResource(request, accessToken.getUserId());
        return ResultResponse.ok(SuccessCode.CREATED, response);
    }

    /**
     * 자료방 게시글 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 게시글 목록 (제목, 작성자, 작성 시간, 링크)
     */
    @GetMapping
    @Operation(
            summary = "자료 목록 조회",
            description = "자료방 게시글 목록을 최신 순으로 조회합니다. 게시글의 link 필드로 외부 링크 이동이 가능합니다.",
            parameters = {
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지당 게시글 수 (기본값 20)", example = "20")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    public ResponseEntity<ResultResponse<Page<ResourceListItemDTO>>> getResources(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ResourceListItemDTO> resources = resourceService.getResources(pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, resources);
    }

    /**
     * 자료방 게시글을 삭제합니다. (소프트 딜리트)
     *
     * @param id 삭제할 게시글 ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "자료 삭제", description = "ID에 해당하는 자료를 삭제합니다. 작성자 본인만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자료 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자만 삭제 가능)"),
            @ApiResponse(responseCode = "404", description = "자료를 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<Void>> deleteResource(@PathVariable Long id,
                                                               AccessToken accessToken) {
        resourceService.deleteResource(id, accessToken.getUserId());
        return ResultResponse.ok(SuccessCode.DELETED);
    }
}
