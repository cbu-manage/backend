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

    @Schema(description = "소식 목록 응답. 고정 소식은 content 앞쪽에 포함되며 page.totalElements에는 일반 소식만 계산합니다.")
    public record NewsListResponseDTO(
            @Schema(description = "소식 목록. 검색 시에는 검색 조건에 맞는 고정 소식만 상단에 포함됩니다.")
            List<NewsListItemDTO> content,

            @Schema(description = "페이지 정보. 고정 소식은 totalElements와 totalPages 계산에서 제외됩니다.")
            PageInfoDTO page,

            @Schema(description = "검색 정보. keyword가 없으면 mode는 NONE입니다.")
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

    @Schema(description = "페이지 요약 정보")
    public record PageInfoDTO(
            @Schema(description = "현재 페이지 번호. 0부터 시작합니다.", example = "0")
            int number,

            @Schema(description = "요청한 페이지 크기", example = "20")
            int size,

            @Schema(description = "고정 소식을 제외한 전체 일반 소식 수", example = "42")
            long totalElements,

            @Schema(description = "고정 소식을 제외한 전체 페이지 수", example = "3")
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

    @Schema(description = "검색 처리 정보")
    public record NewsSearchInfoDTO(
            @Schema(description = "요청 검색어. 검색하지 않은 목록 조회에서는 null입니다.", example = "정기 세미나")
            String keyword,

            @Schema(description = "검색 모드. NONE=검색 없음, AND=모든 단어 포함, OR_FALLBACK=AND 결과가 0건일 때 두 단어 이상 검색어에 한해 관련 결과 조회", allowableValues = {"NONE", "AND", "OR_FALLBACK"}, example = "AND")
            NewsSearchMode mode,

            @Schema(description = "OR_FALLBACK 수행 여부. 단일 단어 검색은 AND와 OR가 같으므로 fallback을 수행하지 않습니다.", example = "false")
            boolean fallbackApplied
    ) {
        public static NewsSearchInfoDTO none(String keyword) {
            return new NewsSearchInfoDTO(keyword, NewsSearchMode.NONE, false);
        }
    }

    @Schema(description = "소식 목록 응답. 목록 화면에 필요한 값만 내려주며 본문(content)은 상세 조회에서 확인합니다.")
    public record NewsListItemDTO(
            @Schema(description = "소식 ID", example = "1")
            Long newsId,

            @Schema(description = "공통 Post 엔티티 ID. 댓글 등 Post 기반 공통 API 호출에 사용")
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

    @Schema(description = "소식 상세 응답. 본문(content), 수정 일시(updatedAt), 조회수를 포함합니다.")
    public record NewsDetailDTO(
            @Schema(description = "소식 ID", example = "1")
            Long newsId,

            @Schema(description = "공통 Post 엔티티 ID. 댓글 등 Post 기반 공통 API 호출에 사용")
            Long postId,

            @Schema(description = "작성자 회원 ID", example = "12")
            Long authorId,

            @Schema(description = "소식 제목", example = "5월 정기 세미나 안내")
            String title,

            @Schema(description = CATEGORY_DESCRIPTION, allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category,

            @Schema(description = "소식 본문", example = "5월 정기 세미나는 동아리방에서 진행합니다.")
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

    @Schema(description = "소식 작성 요청. 관리자만 사용할 수 있습니다.")
    public record NewsCreateRequestDTO(
            @Schema(description = "소식 제목", example = "5월 정기 세미나 안내")
            @NotBlank
            @Size(max = TITLE_MAX)
            String title,

            @Schema(description = "소식 본문", example = "5월 정기 세미나는 동아리방에서 진행합니다.")
            @NotBlank
            @Size(max = CONTENT_MAX)
            String content,

            @Schema(description = CATEGORY_DESCRIPTION + ". 생략하면 NOTICE로 저장됩니다.", allowableValues = {"NOTICE", "EVENT", "NEWSLETTER", "IT_NEWS"}, example = "NOTICE")
            NewsCategory category
    ) {
    }

    @Schema(description = "소식 수정 요청. 전달한 필드만 수정하며 null인 필드는 기존 값을 유지합니다.")
    public record NewsUpdateRequestDTO(
            @Schema(description = "수정할 제목. null이면 기존 제목을 유지합니다.", example = "5월 정기 세미나 장소 변경 안내")
            @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.")
            @Size(max = TITLE_MAX)
            String title,

            @Schema(description = "수정할 본문. null이면 기존 본문을 유지합니다.", example = "세미나 장소가 동아리방에서 공학관 101호로 변경되었습니다.")
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
