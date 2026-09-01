package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "합격 안내 화면에 보여줄 본인 확인 정보")
public record ApplicationResultResponse(
        @Schema(description = "지원자 이름", example = "김민주")
        String name,
        @Schema(description = "가운데를 가린 학번", example = "2026****01")
        String maskedStudentNumber,
        @Schema(description = "기수", example = "31")
        Long generation
) {
    public static ApplicationResultResponse from(MemberApplication application) {
        return new ApplicationResultResponse(
                application.getName(),
                maskStudentNumber(application.getStudentNumber()),
                application.getGeneration());
    }

    /* 링크만 알면 열 수 있는 화면이라 본인 확인에 필요한 앞뒤만 남기고 가운데를 가린다. */
    private static String maskStudentNumber(Long studentNumber) {
        if (studentNumber == null) return "";
        String digits = String.valueOf(studentNumber);
        if (digits.length() <= 6) return digits;
        return digits.substring(0, 4)
                + "*".repeat(digits.length() - 6)
                + digits.substring(digits.length() - 2);
    }
}
