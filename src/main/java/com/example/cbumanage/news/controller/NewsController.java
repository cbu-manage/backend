package com.example.cbumanage.news.controller;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.news.dto.NewsDTO;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.service.NewsAttachmentService;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "소식 게시판", description = "동아리 공지, 행사, 뉴스레터, IT 소식을 관리하고 조회하는 API")
public class NewsController {

    private static final String AUTHENTICATED = "isAuthenticated()";
    private static final String ADMIN_ROLE = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_PRESIDENT', 'ROLE_VICE_PRESIDENT', 'ROLE_PROMOTION_MANAGER')";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String SORT_BY_CREATED_AT = "post.createdAt";

    private final NewsService newsService;
    private final NewsAttachmentService newsAttachmentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(
            summary = "소식 목록 조회",
            description = "소식 목록 화면에서 사용합니다. category를 생략하면 전체 카테고리를 조회하고, keyword를 전달하면 제목과 본문을 함께 검색합니다. "
                    + "고정 소식은 현재 페이지와 관계없이 content 배열의 앞쪽에 함께 내려갑니다. 다만 page.totalElements와 page.totalPages에는 일반 소식만 계산되므로, "
                    + "프론트에서는 content를 그대로 렌더링하고 페이지네이션은 page 값을 기준으로 처리하면 됩니다. "
                    + "본문은 Markdown 원문으로 저장될 수 있지만, 검색은 태그와 마크업을 제거한 plain text 기준으로 동작합니다."
    )
    public ApiResponse<NewsDTO.NewsListResponseDTO> getList(
            @Parameter(
                    description = "목록에 보여줄 소식 카테고리입니다. 생략하면 모든 카테고리를 조회합니다.",
                    schema = @Schema(allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            )
            @RequestParam(required = false) NewsCategory category,
            @Parameter(
                    description = "검색어입니다. 제목과 본문 plain text에서 모든 단어가 포함된 결과를 먼저 찾고, 결과가 없고 검색어가 두 단어 이상이면 일부 단어가 포함된 관련 결과를 내려줍니다. 실제 fallback 여부는 응답의 search.fallbackApplied로 확인합니다.",
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
    @Operation(summary = "소식 상세 조회", description = "소식 상세 화면에서 사용합니다. 호출할 때마다 조회수가 1 증가하며, 응답에는 증가 후 조회수가 담깁니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> getDetail(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id
    ) {
        return ApiResponse.success(newsService.getNewsDetail(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 작성", description = "소식 관리 권한이 필요합니다. title과 content는 필수이며, category를 생략하면 NOTICE로 저장됩니다. content는 Markdown 원문을 그대로 보내면 됩니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> create(Authentication authentication, @Valid @RequestBody NewsDTO.NewsCreateRequestDTO request) {
        Long userId = Long.parseLong(authentication.getName());
        return ApiResponse.success(newsService.createNews(request, userId));
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 수정", description = "소식 관리 권한이 필요합니다. 수정할 필드만 보내면 되고, 보내지 않은 필드는 기존 값이 유지됩니다. title 또는 content가 바뀌면 검색용 텍스트도 함께 갱신됩니다.")
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
    @Operation(summary = "소식 삭제", description = "소식 관리 권한이 필요합니다. 실제 row를 삭제하지 않고 목록과 상세 조회에서 보이지 않도록 처리합니다.")
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
    @Operation(summary = "소식 상단 고정 설정", description = "소식 관리 권한이 필요합니다. pinned=true이면 목록 content 앞쪽에 고정 노출되고, pinned=false이면 고정이 해제됩니다.")
    public ApiResponse<NewsDTO.NewsDetailDTO> changePinned(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody NewsDTO.NewsPinRequestDTO request
    ) {
        return ApiResponse.success(newsService.changePinned(id, request.pinned()));
    }

    @PostMapping(value = "/{id}/attachments", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(
            summary = "소식 첨부파일 추가",
            description = "소식 관리 권한이 필요합니다. 소식 하나에 여러 파일을 따로 올릴 수 있습니다. "
                    + "이미지/PDF/문서(doc, docx, ppt, pptx, xls, xlsx, hwp, hwpx, txt, csv)/zip 형식만 허용되며 파일당 최대 20MB입니다. "
                    + "원본 파일을 변형 없이 저장하고, 등록된 첨부 정보를 반환합니다."
    )
    public ApiResponse<NewsDTO.NewsAttachmentDTO> addAttachment(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success(newsAttachmentService.addAttachment(id, file));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(ADMIN_ROLE)
    @Operation(summary = "소식 첨부파일 삭제", description = "소식 관리 권한이 필요합니다. S3 객체와 첨부 정보를 함께 제거합니다.")
    public ApiResponse<Void> deleteAttachment(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "첨부파일 ID", example = "3")
            @PathVariable Long attachmentId
    ) {
        newsAttachmentService.deleteAttachment(id, attachmentId);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/attachments/{attachmentId}/download")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(AUTHENTICATED)
    @Operation(
            summary = "소식 첨부파일 다운로드 URL 발급",
            description = "잠시 후 만료되는 다운로드용 임시 URL을 발급합니다. 응답의 url로 접근하면 원본 파일명으로 내려받아집니다. "
                    + "url은 만료되므로 매 다운로드마다 이 API를 호출해 새로 발급받으면 됩니다."
    )
    public ApiResponse<NewsDTO.AttachmentDownloadDTO> downloadAttachment(
            @Parameter(description = "소식 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "첨부파일 ID", example = "3")
            @PathVariable Long attachmentId
    ) {
        return ApiResponse.success(newsAttachmentService.getDownloadUrl(id, attachmentId));
    }
}
