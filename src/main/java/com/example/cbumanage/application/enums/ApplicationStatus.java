package com.example.cbumanage.application.enums;

/**
 * 신청서의 상태와 생명주기 입니다.(내림차순)
 */
public enum ApplicationStatus {
    SUBMITTED,      // 제출 완료, 검토 전
    REVIEWING,      // 검토 중(운영진 투표 진행 중)
    ADMIN_ACCEPTED, // 합격 결정
    ADMIN_REJECTED, // 불합격 결정
    NOTIFIED,       // 합/불 통보 완료
    COMPLETED,      // 회원가입 완료
    CANCELLED       // 지원 취소
}
