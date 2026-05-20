package com.example.cbumanage.news.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.news.dto.NewsDTO;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "소식 게시판", description = "공지, 이벤트, 뉴스레터, IT 소식을 노출하는 게시판 API")
public class NewsController {

    private static final String AUTHENTICATED = "isAuthenticated()";
    private static final String ADMIN_ROLE = "hasAuthority('ROLE_ADMIN')";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String SORT_BY_CREATED_AT = "post.createdAt";

    private final NewsService newsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(
            summary = "소식 목록 조회",
            description = "로그인한 사용자가 조회합니다. 고정된 소식은 페이지 번호와 관계없이 항상 목록 상단에 반복 노출됩니다. "
                    + "고정 소식은 페이지 크기와 totalElements 계산에는 포함하지 않습니다. "
                    + "keyword를 전달하면 제목과 본문 plain text 기준으로 검색합니다. 본문에 Markdown 또는 HTML이 섞여도 태그와 마크업을 제거한 검색 전용 텍스트만 인덱싱합니다. "
                    + "검색은 MySQL ngram parser 기준이며 운영 DB의 ngram_token_size=2 설정 확인이 필요합니다."
    )
    public ApiResponse<NewsDTO.NewsListResponseDTO> getList(
            @Parameter(
                    description = "소식 내부 카테고리 필터. NOTICE=공지, EVENT=이벤트, NEWSLETTER=뉴스레터, IT_NEWS=IT 소식. 생략하면 전체를 조회합니다.",
                    schema = @Schema(allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            )
            @RequestParam(required = false) NewsCategory category,
            @Parameter(
                    description = "검색어. 제목과 본문 plain text를 대상으로 AND 검색을 먼저 수행하고, 결과가 0건이며 검색어가 두 단어 이상이면 OR_FALLBACK 모드로 관련 결과를 조회합니다.",
                    example = "정기 세미나"
            )
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = SORT_BY_CREATED_AT, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(newsService.getNewsList(pageable, category, keyword));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "소식 상세 조회", description = "로그인한 사용자가 조회합니다. 호출할 때마다 조회수가 1 증가합니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> getDetail(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.success(newsService.getNewsDetail(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 작성", description = "관리자만 호출합니다. category를 생략하면 NOTICE로 저장됩니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> create(Authentication authentication, @Valid @RequestBody NewsDTO.NewsCreateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(newsService.createNews(request, userId));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 수정", description = "관리자만 호출합니다. 전달한 필드만 수정하고, null인 필드는 기존 값을 유지합니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> update(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody NewsDTO.NewsUpdateRequestDTO request
    ) {
        return ApiResponse.success(newsService.updateNews(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 삭제", description = "관리자만 호출합니다. 삭제된 소식은 목록과 상세 조회에서 제외됩니다.")
    public ApiResponse<Void> delete(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id
    ) {
        newsService.deleteNews(id);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/pin")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 상단 고정 설정", description = "관리자만 호출합니다. 고정된 소식은 모든 목록 페이지의 상단에 노출됩니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> changePinned(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody NewsDTO.NewsPinRequestDTO request
    ) {
        return ApiResponse.success(newsService.changePinned(id, request.pinned()));
    }
}
