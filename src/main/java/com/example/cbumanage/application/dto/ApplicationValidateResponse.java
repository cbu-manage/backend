package com.example.cbumanage.application.dto;

import com.example.cbumanage.application.entity.MemberApplication;
import io.swagger.v3.oas.annotations.media.Schema;

public record ApplicationValidateResponse(
        Long applicationId,
        String name,
        String nickName,
        String grade,
        String major,
        @Schema(description = "가운데를 가린 연락처. 이 API는 비로그인으로 열려 있어 원문을 주지 않는다.",
                example = "010-****-2345")
        String phoneNumber,
        Long studentNumber
) {
    public static ApplicationValidateResponse from(MemberApplication application) {
        return new ApplicationValidateResponse(
                application.getId(),
                application.getName(),
                application.getNickname(),
                application.getGrade().name(),
                application.getMajor(),
                maskPhoneNumber(application.getPhoneNumber()),
                application.getStudentNumber()
        );
    }

    /**
     * 이 응답은 비로그인으로 받을 수 있다. 원문을 그대로 실으면 학번+닉네임만 맞히면
     * 지원자 연락처가 그대로 새어나간다. 본인 확인에 필요한 만큼만 남기고 가운데를 가린다.
     * 운영진이 원문을 봐야 하는 심사 화면은 인증이 걸린 별도 API를 쓴다.
     */
    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.length() < 7) {
            // 형식을 알 수 없으면 아무것도 흘리지 않는다
            return "***";
        }
        String head = digits.substring(0, 3);
        String tail = digits.substring(digits.length() - 4);
        return head + "-****-" + tail;
    }
}
