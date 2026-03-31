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
@Tag(name = "자료방 컨트롤러", description = "자료방 게시글 등록/조회/삭제 API")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @Operation(summary = "자료 등록", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json",
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
    @Operation(summary = "자료 목록 조회")
    public ApiResponse<Page<ResourceListItemDTO>> getResources(
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(resourceService.getResources(pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "내 자료 목록 조회")
    public ApiResponse<Page<ResourceListItemDTO>> getMyResources(Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "post.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(resourceService.getMyResources(userId, pageable));
    }

    @GetMapping("/og-preview")
    @Operation(summary = "OG 데이터 미리보기")
    public ApiResponse<OgMetaPreviewDTO> previewOg(@RequestParam String url) {
        return ApiResponse.success(resourceService.previewOg(url));
    }

    @PatchMapping("/{id}/refresh-og")
    @Operation(summary = "OG 메타 수동 갱신")
    public ApiResponse<Void> refreshOg(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        resourceService.refreshOg(id, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자료 삭제")
    public ApiResponse<Void> deleteResource(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        resourceService.deleteResource(id, userId);
        return ApiResponse.success();
    }
}
