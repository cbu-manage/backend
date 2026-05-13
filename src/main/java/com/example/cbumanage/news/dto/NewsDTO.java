package com.example.cbumanage.news.dto;

import com.example.cbumanage.news.entity.News;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class NewsDTO {

    private static final int TITLE_MAX = 200;
    private static final int CONTENT_MAX = 20_000;

    @Schema(description = "본문(content) 미포함. 본문은 상세 조회로.")
    public record NewsListItemDTO(
            Long newsId,

            @Schema(description = "공통 Post 엔티티 ID. 댓글 등 Post 기반 공통 API 호출에 사용")
            Long postId,

            Long authorId,
            String title,
            @Schema(description = "소식 내부 카테고리")
            NewsCategory category,
            LocalDateTime createdAt,
            Long viewCount,

            @Schema(description = "고정 여부")
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

    @Schema(description = "본문(content) 과 수정 시각(updatedAt) 포함")
    public record NewsDetailDTO(
            Long newsId,

            @Schema(description = "공통 Post 엔티티 ID. 댓글 등 Post 기반 공통 API 호출에 사용")
            Long postId,

            Long authorId,
            String title,
            @Schema(description = "소식 내부 카테고리")
            NewsCategory category,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,

            @Schema(description = "누적 조회수")
            Long viewCount,

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

    @Schema(description = "소식 작성 요청")
    public record NewsCreateRequestDTO(
            @NotBlank
            @Size(max = TITLE_MAX)
            String title,

            @NotBlank
            @Size(max = CONTENT_MAX)
            String content,

            @Schema(description = "소식 내부 카테고리. 생략하면 NOTICE로 저장됩니다.")
            NewsCategory category
    ) {
    }

    @Schema(description = "수정할 필드만 전달합니다.")
    public record NewsUpdateRequestDTO(
            @Pattern(regexp = "(?s).*\\S.*", message = "제목은 공백일 수 없습니다.")
            @Size(max = TITLE_MAX)
            String title,

            @Pattern(regexp = "(?s).*\\S.*", message = "내용은 공백일 수 없습니다.")
            @Size(max = CONTENT_MAX)
            String content,

            @Schema(description = "소식 내부 카테고리. null이면 기존 값 유지")
            NewsCategory category
    ) {
    }

    @Schema(description = "상단 고정 여부를 변경합니다.")
    public record NewsPinRequestDTO(
            @Schema(description = "상단 고정 여부")
            @NotNull
            Boolean pinned
    ) {
    }
}
