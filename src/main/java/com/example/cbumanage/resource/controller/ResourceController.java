package com.example.cbumanage.resource.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.resource.dto.OgMetaPreviewDTO;
import com.example.cbumanage.resource.dto.ResourceCreateRequestDTO;
import com.example.cbumanage.resource.dto.ResourceListItemDTO;
import com.example.cbumanage.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "자료실", description = "자료 링크를 등록·조회·삭제하고 OG 메타데이터를 관리합니다.")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @Operation(summary = "자료 등록", description = "자료 링크를 등록하고 Open Graph 메타데이터를 함께 저장합니다.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json",
            examples = {@ExampleObject(name = "링크만 전송", value = """
                    { "link": "https://programmers.co.kr/learn/challenges" }
                    """), @ExampleObject(name = "제목 직접 입력", value = """
                    { "link": "https://programmers.co.kr/learn/challenges", "title": "2024 카카오 코딩테스트 문제 모음" }
                    """)})))
    public ApiResponse<ResourceListItemDTO> createResource(Authentication authentication,
            @Valid @org.springframework.web.bind.annotation.RequestBody ResourceCreateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(resourceService.createResource(request, userId));
    }

    @GetMapping
    @Operation(summary = "자료 목록 조회", description = "자료 목록을 페이지 단위로 조회합니다.")
    public ApiResponse<Page<ResourceListItemDTO>> getResources(
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(resourceService.getResources(pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "내 자료 목록 조회", description = "로그인 사용자가 등록한 자료 목록을 조회합니다.")
    public ApiResponse<Page<ResourceListItemDTO>> getMyResources(Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(resourceService.getMyResources(userId, pageable));
    }

    @GetMapping("/og-preview")
    @Operation(summary = "OG 메타데이터 미리보기", description = "URL의 Open Graph 메타데이터를 저장하지 않고 미리 조회합니다.")
    public ApiResponse<OgMetaPreviewDTO> previewOg(@RequestParam String url) {
        return ApiResponse.success(resourceService.previewOg(url));
    }

    @PatchMapping("/{id}/refresh-og")
    @Operation(summary = "OG 메타데이터 갱신", description = "등록된 자료의 Open Graph 메타데이터를 다시 조회해 갱신합니다.")
    public ApiResponse<Void> refreshOg(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        resourceService.refreshOg(id, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자료 삭제", description = "작성자 본인의 자료를 삭제합니다.")
    public ApiResponse<Void> deleteResource(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        resourceService.deleteResource(id, userId);
        return ApiResponse.success();
    }
}
