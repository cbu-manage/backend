package com.example.cbumanage.application.dto;

import org.springframework.data.domain.Page;

/**
 * 신청서 목록 응답.
 * voterCount(N)는 전체 투표인 수
 */
public record AdminApplicationListResponse(
        int voterCount,
        Page<ApplicationListItemResponse> applications
) {
}