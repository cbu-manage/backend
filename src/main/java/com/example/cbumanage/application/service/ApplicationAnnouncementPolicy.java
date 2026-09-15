package com.example.cbumanage.application.service;

import com.example.cbumanage.application.repository.RecruitmentRepository;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 합격 발표 전인지 판정한다.
 *
 * 같은 판정이 지원서 검증(회원가입 전 합격자 확인)과 지원자 본인 조회 두 곳에 필요하다.
 * 각자 들고 있으면 한쪽만 고쳤을 때 다른 쪽으로 그대로 새므로 여기 하나만 둔다.
 * 서비스 생성자를 건드리지 않으려고 정적 메서드로 둔다.
 */
public final class ApplicationAnnouncementPolicy {

    /* 날짜 경계는 동아리가 쓰는 한국 시간 기준이다. 서버 JVM 기본 존은 UTC라 그대로 두면 하루가 9시간 밀린다. */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private ApplicationAnnouncementPolicy() {
    }

    /**
     * 발표일이 지나지 않았으면 true(결과를 감춘다).
     *
     * 판단 근거가 없을 때는 감추는 쪽으로 간다. 발표일 미정도, 지원서의 기수에 해당하는
     * 모집 정보가 없는 경우도 마찬가지다. 여기서 공개 쪽으로 넘어가면 합격 여부가
     * 발표 전에 그대로 나간다.
     */
    public static boolean isBeforeAnnouncement(RecruitmentRepository recruitmentRepository, Long generation) {
        if (generation == null) {
            return true;
        }
        return recruitmentRepository.findByGeneration(generation)
                .map(recruitment -> recruitment.getAnnouncementDate() == null
                        || today().isBefore(recruitment.getAnnouncementDate()))
                .orElse(true);
    }

    /** 모집 기간·발표일 비교에 쓰는 오늘 날짜(한국 시간). */
    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
