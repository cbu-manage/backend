package com.example.cbumanage.application.entity.enums;

import java.util.List;

/**
 * 신청서 목록의 상태 탭.
 * 최종결정 status로 매핑(투표집계 축과 별개).
 */
public enum ApplicationReview {
    ALL,        // 전체 (상태 필터 없음)
    REVIEWING,  // 검토 대기
    ACCEPTED,   // 합격 (가입완료 COMPLETED는 제외)
    REJECTED;   // 불합격

    /**
     * 탭을 실제 status 목록으로 변환. ALL이면 null(필터 미적용).
     */
    public List<ApplicationStatus> toStatuses() {
        return switch (this) {
            case ALL -> null;
            case REVIEWING -> List.of(ApplicationStatus.SUBMITTED);
            case ACCEPTED -> List.of(ApplicationStatus.ADMIN_ACCEPTED);
            case REJECTED -> List.of(ApplicationStatus.ADMIN_REJECTED);
        };
    }
}