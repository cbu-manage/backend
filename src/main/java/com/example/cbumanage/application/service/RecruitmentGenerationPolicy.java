package com.example.cbumanage.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 모집 시즌에 따라 신청 기수를 자동 산정합니다.
 *
 * 기본 기준:
 * - 2026년 여름방학 모집 = 29기
 * - 2026년 2학기 모집 = 30기
 * - 2026년 겨울방학 모집 = 31기
 * - 2027년 1학기 모집 = 32기
 */
@Component
public class RecruitmentGenerationPolicy {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final Clock clock;
    private final int anchorYear;
    private final RecruitmentSeason anchorSeason;
    private final long anchorGeneration;

    public RecruitmentGenerationPolicy(
            @Value("${cbu.recruitment.generation.anchor-year:2026}") int anchorYear,
            @Value("${cbu.recruitment.generation.anchor-season:SUMMER_BREAK}") RecruitmentSeason anchorSeason,
            @Value("${cbu.recruitment.generation.anchor-generation:29}") long anchorGeneration) {
        this(Clock.system(DEFAULT_ZONE), anchorYear, anchorSeason, anchorGeneration);
    }

    RecruitmentGenerationPolicy(Clock clock, int anchorYear,
                                RecruitmentSeason anchorSeason, long anchorGeneration) {
        this.clock = clock;
        this.anchorYear = anchorYear;
        this.anchorSeason = anchorSeason;
        this.anchorGeneration = anchorGeneration;
    }

    public long currentGeneration() {
        return generationFor(LocalDate.now(clock));
    }

    long generationFor(LocalDate date) {
        int currentSerial = seasonSerial(seasonYear(date), RecruitmentSeason.from(date));
        int anchorSerial = seasonSerial(anchorYear, anchorSeason);
        return anchorGeneration + currentSerial - anchorSerial;
    }

    RecruitmentSeason currentSeason() {
        return RecruitmentSeason.from(LocalDate.now(clock));
    }

    private int seasonYear(LocalDate date) {
        if (RecruitmentSeason.from(date) == RecruitmentSeason.WINTER_BREAK && date.getMonthValue() <= 2) {
            return date.getYear() - 1;
        }
        return date.getYear();
    }

    private int seasonSerial(int year, RecruitmentSeason season) {
        return year * RecruitmentSeason.values().length + season.order;
    }

    public enum RecruitmentSeason {
        FIRST_SEMESTER(0),
        SUMMER_BREAK(1),
        SECOND_SEMESTER(2),
        WINTER_BREAK(3);

        private final int order;

        RecruitmentSeason(int order) {
            this.order = order;
        }

        static RecruitmentSeason from(LocalDate date) {
            return switch (date.getMonthValue()) {
                case 3, 4, 5 -> FIRST_SEMESTER;
                case 6, 7, 8 -> SUMMER_BREAK;
                case 9, 10, 11 -> SECOND_SEMESTER;
                case 12, 1, 2 -> WINTER_BREAK;
                default -> throw new IllegalArgumentException("지원하지 않는 월입니다: " + date.getMonthValue());
            };
        }
    }
}
