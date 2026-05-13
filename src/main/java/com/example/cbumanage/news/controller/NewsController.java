package com.example.cbumanage.news.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.news.dto.NewsDTO;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "소식 게시판 컨트롤러")
public class NewsController {

    private static final String AUTHENTICATED = "isAuthenticated()";
    private static final String ADMIN_ROLE = "hasAuthority('ROLE_ADMIN')";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String SORT_BY_CREATED_AT = "post.createdAt";

    private final NewsService newsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "소식 목록 조회", description = "로그인한 사용자가 조회할 수 있습니다. 고정된 소식은 목록 상단에 함께 노출됩니다.")
    public ApiResponse<Page<NewsDTO.NewsListItemDTO>> getList(
            @RequestParam(required = false) NewsCategory category,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = SORT_BY_CREATED_AT, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(newsService.getNewsList(pageable, category));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "소식 상세 조회", description = "로그인한 사용자가 조회할 수 있습니다. 상세 조회 시 조회수가 증가합니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> getDetail(@PathVariable Long id) {
        return ApiResponse.success(newsService.getNewsDetail(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 작성", description = "관리자만 호출할 수 있습니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> create(Authentication authentication, @Valid @RequestBody NewsDTO.NewsCreateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(newsService.createNews(request, userId));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 수정", description = "관리자만 호출할 수 있습니다. 전달한 필드만 수정됩니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> update(@PathVariable Long id, @Valid @RequestBody NewsDTO.NewsUpdateRequestDTO request) {
        return ApiResponse.success(newsService.updateNews(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 삭제", description = "관리자만 호출할 수 있습니다.")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/pin")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 상단 고정 설정", description = "관리자만 호출할 수 있습니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> changePinned(@PathVariable Long id, @Valid @RequestBody NewsDTO.NewsPinRequestDTO request) {
        return ApiResponse.success(newsService.changePinned(id, request.pinned()));
    }
}
