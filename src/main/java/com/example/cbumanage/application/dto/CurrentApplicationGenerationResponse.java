package com.example.cbumanage.application.dto;

import java.time.LocalDate;

public record CurrentApplicationGenerationResponse(
        Long generation,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate announcementDate,
        /** 지금 지원서를 받는지 — OPEN 모집이 있고 오늘이 모집 기간 안일 때만 true */
        boolean accepting
) {
}
