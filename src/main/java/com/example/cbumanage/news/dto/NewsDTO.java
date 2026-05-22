package com.example.cbumanage.news.dto;

import com.example.cbumanage.news.entity.News;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public class NewsDTO {

    private static final int TITLE_MAX = 200;
    private static final int CONTENT_MAX = 20_000;
    private static final String CATEGORY_DESCRIPTION =
            "소식 내부 카테고리. NOTICE=공지, EVENT=이벤트, NEWSLETTER=뉴스레터, IT_NEWS=IT 소식";

    public enum NewsSearchMode {
        NONE,
        AND,
        OR_FALLBACK
    }

    @Schema(description = "소식 목록 화면 응답입니다. content 배열에는 고정 소식과 일반 소식이 함께 담기고, page는 일반 소식 기준 페이지 정보입니다.")
    public record NewsListResponseDTO(
            @Schema(description = "화면에 표시할 소식 목록입니다. 고정 소식이 있으면 배열 앞쪽에 먼저 들어오므로, 프론트에서는 별도 병합 없이 순서대로 렌더링하면 됩니다.")
            List<NewsListItemDTO> content,

            @Schema(description = "일반 소식 기준 페이지 정보입니다. 고정 소식은 모든 페이지 상단에 반복 노출될 수 있어 totalElements와 totalPages 계산에서 제외됩니다.")
            PageInfoDTO page,

            @Schema(description = "검색 처리 결과입니다. 검색어가 없으면 mode는 NONE, fallbackApplied는 false입니다.")
            NewsSearchInfoDTO search
    ) {
        public static NewsListResponseDTO from(Page<NewsListItemDTO> page, NewsSearchInfoDTO search) {
            return new NewsListResponseDTO(
                    page.getContent(),
                    PageInfoDTO.from(page),
                    search
            );
        }
    }

    @Schema(description = "목록 페이지네이션 정보")
    public record PageInfoDTO(
            @Schema(description = "현재 페이지 번호. 0부터 시작합니다.", example = "0")
            int number,

            @Schema(description = "요청한 페이지 크기", example = "20")
            int size,

            @Schema(description = "검색과 카테고리 필터가 적용된 일반 소식 수입니다. 고정 소식은 포함하지 않습니다.", example = "42")
            long totalElements,

            @Schema(description = "검색과 카테고리 필터가 적용된 일반 소식 기준 전체 페이지 수입니다.", example = "3")
            int totalPages,

            @Schema(description = "첫 페이지 여부", example = "true")
            boolean first,

            @Schema(description = "마지막 페이지 여부", example = "false")
            boolean last
    ) {
        public static PageInfoDTO from(Page<?> page) {
            return new PageInfoDTO(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast()
            );
        }
    }

    @Schema(description = "검색 처리 정보. 검색 결과 안내 문구가 필요하면 이 값을 기준으로 판단합니다.")
    public record NewsSearchInfoDTO(
            @Schema(description = "요청한 검색어입니다. 검색하지 않은 목록 조회에서는 null입니다.", example = "정기 세미나")
            String keyword,

            @Schema(description = "검색 모드입니다. NONE은 검색 없음, AND는 모든 단어가 포함된 결과, OR_FALLBACK은 AND 결과가 0건일 때 일부 단어가 포함된 관련 결과입니다.", allowableValues = {"NONE", "AND", "OR_FALLBACK"}, example = "AND")
            NewsSearchMode mode,

            @Schema(description = "관련 결과 fallback을 수행했는지 여부입니다. true이면 프론트에서 '관련 결과를 표시합니다' 같은 안내를 보여줄 수 있습니다. 단일 단어 검색은 fallback을 수행하지 않습니다.", example = "false")
            boolean fallbackApplied
    ) {
        public static NewsSearchInfoDTO none(String keyword) {
            return new NewsSearchInfoDTO(keyword, NewsSearchMode.NONE, false);
        }
    }

    @Schema(description = "소식 목록 아이템입니다. 목록에서는 본문(content)을 내려주지 않으며, 본문은 상세 조회에서 확인합니다.")
    public record NewsListItemDTO(
            @Schema(description = "소식 ID입니다. 소식 상세/수정/삭제/고정 API의 path variable로 사용합니다.", example = "1")
            Long newsId,

            @Schema(description = "공통 Post ID입니다. 댓글처럼 Post 기반 공통 API를 호출할 때 사용합니다.", example = "15")
            Long postId,

            @Schema(description = "작성자 회원 ID", example = "12")
            Long authorId,

            @Schema(description = "소식 제목", example = "5월 정기 세미나 안내")
            String title,

            @Schema(description = CATEGORY_DESCRIPTION, allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category,

            @Schema(description = "작성 일시")
            LocalDateTime createdAt,

            @Schema(description = "누적 조회수", example = "42")
            Long viewCount,

            @Schema(description = "상단 고정 여부. true인 소식은 모든 목록 페이지의 상단에 반복 노출됩니다.", example = "false")
            boolean pinned
    ) {
        public static NewsListItemDTO from(News news) {
            return new NewsListItemDTO(
                    news.getNewsId(),
                    news.getPostId(),
                    news.getAuthorId(),
                    news.getTitle(),
                    news.getCategory(),
                    news.getCreatedAt(),
                    news.getViewCount(),
                    news.isPinned()
            );
        }
    }

    @Schema(description = "소식 상세 화면 응답입니다. 본문(content), 수정 일시(updatedAt), 조회수를 포함합니다.")
    public record NewsDetailDTO(
            @Schema(description = "소식 ID입니다. 소식 수정/삭제/고정 API의 path variable로 사용합니다.", example = "1")
            Long newsId,

            @Schema(description = "공통 Post ID입니다. 댓글처럼 Post 기반 공통 API를 호출할 때 사용합니다.", example = "15")
            Long postId,

            @Schema(description = "작성자 회원 ID", example = "12")
            Long authorId,

            @Schema(description = "소식 제목", example = "5월 정기 세미나 안내")
            String title,

            @Schema(description = CATEGORY_DESCRIPTION, allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category,

            @Schema(description = "소식 본문입니다. Markdown 원문이 그대로 내려갈 수 있으므로 프론트에서 Markdown 렌더러로 표시하면 됩니다.", example = "5월 정기 세미나는 **동아리방**에서 진행합니다.")
            String content,

            @Schema(description = "작성 일시")
            LocalDateTime createdAt,

            @Schema(description = "수정 일시")
            LocalDateTime updatedAt,

            @Schema(description = "누적 조회수", example = "42")
            Long viewCount,

            @Schema(description = "상단 고정 여부", example = "false")
            boolean pinned
    ) {
        public static NewsDetailDTO from(News news) {
            return from(news, news.getViewCount());
        }

        /** 조회수를 별도로 지정해 응답 DTO를 생성합니다. */
        public static NewsDetailDTO from(News news, long viewCount) {
            return new NewsDetailDTO(
                    news.getNewsId(),
                    news.getPostId(),
                    news.getAuthorId(),
                    news.getTitle(),
                    news.getCategory(),
                    news.getContent(),
                    news.getCreatedAt(),
                    news.getUpdatedAt(),
                    viewCount,
                    news.isPinned()
            );
        }
    }

    @Schema(description = "소식 작성 요청입니다. 관리자만 사용할 수 있습니다.")
    public record NewsCreateRequestDTO(
            @Schema(description = "소식 제목", example = "5월 정기 세미나 안내")
            @NotBlank
            @Size(max = TITLE_MAX)
            String title,

            @Schema(description = "소식 본문입니다. Markdown 원문을 그대로 보내면 됩니다.", example = "5월 정기 세미나는 **동아리방**에서 진행합니다.")
            @NotBlank
            @Size(max = CONTENT_MAX)
            String content,

            @Schema(description = CATEGORY_DESCRIPTION + ". 생략하면 NOTICE로 저장됩니다.", allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category
    ) {
    }

    @Schema(description = "소식 수정 요청입니다. 전달한 필드만 수정하며 null인 필드는 기존 값을 유지합니다.")
    public record NewsUpdateRequestDTO(
            @Schema(description = "수정할 제목. null이면 기존 제목을 유지합니다.", example = "5월 정기 세미나 장소 변경 안내")
            @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.")
            @Size(max = TITLE_MAX)
            String title,

            @Schema(description = "수정할 본문입니다. null이면 기존 본문을 유지합니다. Markdown 원문을 그대로 보내면 됩니다.", example = "세미나 장소가 **공학관 101호**로 변경되었습니다.")
            @Pattern(regexp = "(?s).*\\S.*", message = "내용은 공백일 수 없습니다.")
            @Size(max = CONTENT_MAX)
            String content,

            @Schema(description = CATEGORY_DESCRIPTION + ". null이면 기존 카테고리를 유지합니다.", allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category
    ) {
    }

    @Schema(description = "소식 상단 고정 변경 요청")
    public record NewsPinRequestDTO(
            @Schema(description = "true이면 모든 소식 목록 페이지 상단에 고정하고, false이면 고정을 해제합니다.", example = "true")
            @NotNull
            Boolean pinned
    ) {
    }
}
