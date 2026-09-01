package com.example.cbumanage.global.setting.dto;

public record OnboardingLinksResponse(
        String frontendUrl,
        // 회비 확인 및 문의 방 (키 이름은 OPEN_CHAT_URL 그대로 둔다)
        String openChatUrl,
        String discordUrl,
        String kakaoNotiUrl,
        String kakaoChatUrl
) {
}
