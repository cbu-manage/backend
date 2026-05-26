package com.example.cbumanage.application.entity.enums;

/**
 * 신청서의 상태와 생명주기 입니다.(내림차순)
 */
public enum ApplicationStatus {
    SUBMITTED,      // 제출 완료, 검토 전
    CANCELLED,      // 지원 취소
    HOLD,           // 보류 상태(만장일치 불합과 전체 찬성 상태 제외 모두)
    ALL_REJECTED,   // 만장일치 불합
    ALL_PASS,       // 만장일치 찬성
    ADMIN_ACCEPTED, // 최종 합격 결정
    ADMIN_REJECTED, // 최종 불합격 결정
    NOTIFIED,       // 합/불 안내 완료
    COMPLETED,      // 회원가입 완료

}
