package com.example.cbumanage.global.setting.dto;

import jakarta.validation.constraints.Pattern;

/**
 * 전달한 항목만 갱신한다(null은 기존값 유지).
 * 여기 담긴 주소가 승인 안내 메일에 그대로 들어가므로 형식은 서버에서 확인한다.
 */
public record OnboardingLinksRequest(
        @Pattern(regexp = URL_OR_BLANK, message = "frontendUrl 은 http(s):// 로 시작해야 합니다.")
        String frontendUrl,
        // 회비 확인 및 문의 방 (키 이름은 OPEN_CHAT_URL 그대로 둔다)
        @Pattern(regexp = URL_OR_BLANK, message = "openChatUrl 은 http(s):// 로 시작해야 합니다.")
        String openChatUrl,
        @Pattern(regexp = URL_OR_BLANK, message = "discordUrl 은 http(s):// 로 시작해야 합니다.")
        String discordUrl,
        @Pattern(regexp = URL_OR_BLANK, message = "kakaoNotiUrl 은 http(s):// 로 시작해야 합니다.")
        String kakaoNotiUrl,
        @Pattern(regexp = URL_OR_BLANK, message = "kakaoChatUrl 은 http(s):// 로 시작해야 합니다.")
        String kakaoChatUrl
) {
    /** 비워서 지우는 것은 허용한다 */
    private static final String URL_OR_BLANK = "^$|^https?://.+";
}
