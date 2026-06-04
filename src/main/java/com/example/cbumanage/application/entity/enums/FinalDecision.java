package com.example.cbumanage.application.entity.enums;

/**
 * 일괄 최종처리에서 각 신청서에 내리는 입력값(요청 전용).
 * 신청서의 영속 상태인 ApplicationStatus와 구분된다.
 * HOLD(보류)가 하나라도 있으면 일괄처리는 거절된다. -> 다시 생각..해야할듯합니다요
 */
public enum FinalDecision {
    ACCEPT,
    REJECT,
    HOLD
}