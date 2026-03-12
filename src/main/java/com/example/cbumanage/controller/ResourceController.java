package com.example.cbumanage.controller;

import com.example.cbumanage.authentication.dto.AccessToken;
import com.example.cbumanage.dto.OgMetaPreviewDTO;
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
import org.springdoc.core.annotations.ParameterObject;
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
            description = "자료방에 새 게시글을 등록합니다. 링크는 필수이며, 제목을 생략하면 OG 파싱으로 자동 설정됩니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "자료 등록 예시",
                                    value = """
                                            { "link" : "https://programmers.co.kr/learn/challenges", "ogImage" : "https://example.com/image.png", "ogDescription" : "프로그래머스 코딩테스트 연습" }
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
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ResourceListItemDTO> resources = resourceService.getResources(pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, resources);
    }

    /**
     * 내가 작성한 자료방 게시글 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 내가 작성한 게시글 목록
     */
    @GetMapping("/my")
    @Operation(summary = "내 자료 목록 조회", description = "내가 작성한 자료방 게시글 목록을 최신 순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ResultResponse<Page<ResourceListItemDTO>>> getMyResources(
            AccessToken accessToken,
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ResourceListItemDTO> resources = resourceService.getMyResources(accessToken.getUserId(), pageable);
        return ResultResponse.ok(SuccessCode.SUCCESS, resources);
    }

    /**
     * URL에서 OG 메타 정보를 미리보기합니다. (저장 없음)
     * 자료 등록 전 og:title을 제목 입력란에 자동으로 채워주기 위해 사용합니다.
     */
    @GetMapping("/og-preview")
    @Operation(summary = "OG 데이터 미리보기", description = "URL에서 og:title, og:image, og:description을 추출합니다. 자료 등록 전 제목/이미지/설명 자동 완성에 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OG 정보 추출 성공 (파싱 실패 시 필드는 null로 반환)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)")
    })
    public ResponseEntity<ResultResponse<OgMetaPreviewDTO>> previewOg(
            @Parameter(description = "OG 정보를 추출할 외부 URL", example = "https://github.com/")
            @RequestParam String url) {
        return ResultResponse.ok(SuccessCode.SUCCESS, resourceService.previewOg(url));
    }

    /**
     * 자료방 게시글의 OG 메타 정보를 수동으로 갱신합니다.
     * 작성자 본인만 갱신할 수 있습니다.
     */
    @PatchMapping("/{id}/refresh-og")
    @Operation(summary = "OG 메타 수동 갱신", description = "해당 자료의 og:image, og:description을 URL에서 다시 파싱하여 갱신합니다. 작성자 본인만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OG 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "갱신 권한 없음 (작성자만 가능)"),
            @ApiResponse(responseCode = "404", description = "자료를 찾을 수 없음")
    })
    public ResponseEntity<ResultResponse<Void>> refreshOg(@PathVariable Long id, AccessToken accessToken) {
        resourceService.refreshOg(id, accessToken.getUserId());
        return ResultResponse.ok(SuccessCode.UPDATED);
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
