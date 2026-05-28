package com.example.cbumanage.application.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "신청서 포트폴리오 URL")
@Entity
@Table(name = "application_portfolio_url")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationPortfolioUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_url_id")
    private Long id;

    @Schema(description = "신청서 ID (FK)")
    @Column(name = "member_application_id", nullable = false)
    private Long memberApplicationId;

    @Schema(description = "링크 레이블 (예: GitHub, 블로그)", example = "GitHub")
    @Column(length = 50)
    private String label;

    @Schema(description = "포트폴리오 URL", example = "https://github.com/example")
    @Column(nullable = false, length = 500)
    private String url;

    @Schema(description = "정렬 순서 (오름차순)")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    private ApplicationPortfolioUrl(Long memberApplicationId, String label,
                                    String url, Integer sortOrder) {
        this.memberApplicationId = memberApplicationId;
        this.label = label;
        this.url = url;
        this.sortOrder = sortOrder;
    }
}