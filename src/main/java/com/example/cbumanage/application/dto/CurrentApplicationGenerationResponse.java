package com.example.cbumanage.application.dto;

import java.time.LocalDate;

public record CurrentApplicationGenerationResponse(
        Long generation,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate announcementDate
) {
}
